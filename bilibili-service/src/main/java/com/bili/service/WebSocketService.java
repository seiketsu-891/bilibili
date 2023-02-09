package com.bili.service;

import com.alibaba.fastjson.JSONObject;
import com.bili.domain.Danmu;
import com.bili.domain.constant.RocketMQConstant;
import com.bili.service.util.RocketMQUtil;
import com.bili.service.util.TokenUtil;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/websocket/{token}")
public class WebSocketService {
    public static final ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);
    private static ApplicationContext APPLICATION_CONTEXT;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Session session;
    private String sessionId;
    private Long userId;


    public static void setApplicationContext(ApplicationContext applicationContext) {
        APPLICATION_CONTEXT = applicationContext;
    }

    @OnOpen
    // This method is called when a client establishes a WebSocket connection to the server
    public void openConnection(Session session, @PathParam("token") String token) {
        try {
            this.userId = TokenUtil.verifyToken(token);
        } catch (Exception e) {
        }
        RedisTemplate<String, String> redisTemplate = (RedisTemplate) APPLICATION_CONTEXT.getBean("redisTemplate");
        String sessionId = session.getId();
        this.session = session;
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            // There could be situations where although the session has changed, the session ID remains the same.
            // This could happen if the previous session was closed abruptly, or the server crashes and a new session is established with the same ID.
            // So we need to use the remove method first.
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId, this);
        } else {
            WEBSOCKET_MAP.put(sessionId, this);
            ONLINE_COUNT.getAndIncrement();
        }
        logger.info("用户连接成功:" + sessionId + ", 当前在线人数为：" + ONLINE_COUNT);
        try {
            // tell front end that the web socket has been successfully connected to
            this.sendMessage("0");
        } catch (Exception e) {
            logger.error("连接异常");
        }
    }

    @OnClose
    public void disConnect() {
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            WEBSOCKET_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement();
        }
    }

    @OnMessage
    public void onMessage(String msg) {
        logger.info("user:" + sessionId + ", msg:" + msg);
        if (!StringUtil.isNullOrEmpty(msg)) {
            // send the message to all the online clients
            try {
                for (Map.Entry<String, WebSocketService> entry : WEBSOCKET_MAP.entrySet()) {
                    WebSocketService webSocketService = entry.getValue();
                    DefaultMQProducer danmusProducer = (DefaultMQProducer) APPLICATION_CONTEXT.getBean("danmusProducer");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sessionID", sessionId);
                    jsonObject.put("message", msg);
                    Message message = new Message(RocketMQConstant.TOPIC_DANMUS, jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
                    RocketMQUtil.asyncSendMsg(danmusProducer, message);
                }
                // save danmu to database
                if (this.userId != null) {
                    Danmu danmu = JSONObject.parseObject(msg, Danmu.class);
                    danmu.setUserId(userId);
                    danmu.setCreateTime(new Date());
                    DanmuService danmuService = (DanmuService) APPLICATION_CONTEXT.getBean("danmuService");
                    danmuService.addDanmu(danmu);
                    // save to redis
                    danmuService.addDanmusToRedis(danmu);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Throwable error) {

    }

    public void sendMessage(String msg) throws IOException {
        this.session.getBasicRemote().sendText(msg);
    }

    public Session getSession() {
        return session;
    }

    public String getSessionId() {
        return sessionId;
    }
}

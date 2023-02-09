package com.bili.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/websocket")
public class WebSocketService {
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);
    private static final ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();
    private static ApplicationContext APPLICATION_CONTEXT;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Session session;
    private String sessionId;


    public static void setApplicationContext(ApplicationContext applicationContext) {
        APPLICATION_CONTEXT = applicationContext;
    }

    @OnOpen
    // This method is called when a client establishes a WebSocket connection to the server
    public void openConnection(Session session) {
//        RedisTemplate<String, String> redisTemplate = APPLICATION_CONTEXT.getBean(RedisTemplate.class);
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
    }

    @OnError
    public void onError(Throwable error) {

    }

    public void sendMessage(String msg) throws IOException {
        this.session.getBasicRemote().sendText(msg);
    }
}

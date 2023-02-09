package com.bili.service.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bili.domain.UserFollowing;
import com.bili.domain.UserMoment;
import com.bili.domain.constant.RocketMQConstant;
import com.bili.service.UserFollowingService;
import com.bili.service.WebSocketService;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RocketMQConfig {
    @Value("${rocketmq.name.server.address}")
    private String nameServerAddr;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private UserFollowingService userFollowingService;


    @Bean("momentsProducer")
    public DefaultMQProducer momentsProducer() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(RocketMQConstant.GROUP_MOMENTS);
        producer.setSendMsgTimeout(60000);
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMQConstant.GROUP_MOMENTS);
        consumer.setNamesrvAddr(nameServerAddr);
        consumer.subscribe(RocketMQConstant.TOPIC_MOMENTS, "*");
        // The consumer is configured to register a message listener,
        // which will be called every time the consumer receives a message.
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                // 1.get the message body and parse it into a UserMoment object.
                MessageExt msg = msgs.get(0);
                if (msg == null) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                String bodyStr = new String(msg.getBody());
                UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr), UserMoment.class);

                // 2. retrieve a list of the users who are following the user who posted the moment.
                Long userId = userMoment.getUserId();
                List<UserFollowing> fanList = userFollowingService.getUserFollowers(userId);

                // 3. for each of the followers, get the list of the moments that the followers has
                //    subscribed to from a Redis cache and add the new moment to its list
                for (UserFollowing fan : fanList) {
                    String key = "subscribed-" + fan.getUserId();
                    String subscribedListStr = redisTemplate.opsForValue().get(key);
                    List<UserMoment> subscribedList;
                    if (StringUtil.isNullOrEmpty(subscribedListStr)) {
                        subscribedList = new ArrayList<>();
                    } else {
                        subscribedList = JSONArray.parseArray(subscribedListStr, UserMoment.class);
                    }
                    // update the cache with the modified list of moments
                    subscribedList.add(userMoment);
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(subscribedList));
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }

    @Bean("danmusProducer")
    public DefaultMQProducer danmuProducer() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(RocketMQConstant.GROUP_DANMUS);
        producer.setSendMsgTimeout(60000);
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    @Bean("danmusConsumer")
    public DefaultMQPushConsumer danmuConsumer() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMQConstant.GROUP_DANMUS);
        consumer.setNamesrvAddr(nameServerAddr);
        consumer.subscribe(RocketMQConstant.TOPIC_DANMUS, "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                // convert message to jsonObject
                MessageExt msg = msgs.get(0);
                byte[] msgByte = msg.getBody();
                String bodyStr = new String(msgByte);
                JSONObject jsonObject = JSONObject.parseObject(bodyStr);

                // retrieve the sessionId and message from the producer
                String sessionId = jsonObject.getString("sessionId");
                String message = jsonObject.getString("message");

                // send messages
                WebSocketService webSocketService = WebSocketService.WEBSOCKET_MAP.get(sessionId);
                if (webSocketService.getSession().isOpen()) {
                    try {
                        webSocketService.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }
}
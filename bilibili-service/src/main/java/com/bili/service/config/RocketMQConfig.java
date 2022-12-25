package com.bili.service.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bili.domain.UserFollowing;
import com.bili.domain.UserMoment;
import com.bili.domain.constant.UserMomentsConstant;
import com.bili.service.UserFollowingService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import javax.annotation.Resource;
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
    public DefaultMQProducer momentsProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_MOMENTS);
        producer.setSendMsgTimeout(10000);
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_MOMENTS);
        consumer.setNamesrvAddr(nameServerAddr);
        consumer.subscribe(UserMomentsConstant.TOPIC_MOMENTS, "*");

        // The consumer is configured to register a message listener,
        // which will be called every time the consumer receives a message.
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messageExts, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                // 1.get the message body and parse it into a UserMoment object.
                MessageExt msg = messageExts.get(0);
                if(msg == null) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                String bodyStr = new String(msg.getBody());
                UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr), UserMoment.class);

                // 2. retrieve a list of the users who are following the user who posted the moment.
                Long userId = userMoment.getUserId();
                List<UserFollowing> followers = userFollowingService.getUserFollowers(userId);

                // 3. for each of the followers, get the list of the moments that the followers has
                //    subscribed to from a Redis cache and add the new moment to its list
                for (UserFollowing follower : followers) {
                    String key = UserMomentsConstant.REDIS_KEY_PREFIX+ follower.getUserId();
                    String subListStr = redisTemplate.opsForValue().get(key);
                    List<UserMoment> subList = new ArrayList<>();
                    if(subListStr != null && !subListStr.isEmpty()){
                        subList = JSONArray.parseArray(subListStr, UserMoment.class);
                    }
                    subList.add(userMoment);

                    // update the cache with the modified list of moments
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(subList));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }

        });
        consumer.start();
        return consumer;
    }
}
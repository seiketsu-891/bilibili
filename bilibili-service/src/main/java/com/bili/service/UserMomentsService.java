package com.bili.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bili.dao.UserMomentsDao;
import com.bili.domain.UserMoment;
import com.bili.domain.constant.UserMomentsConstant;
import com.bili.service.util.RocketMQUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserMomentsService {
    @Resource
    private  UserMomentsDao userMomentsDao;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ApplicationContext applicationContext;

    public void addUserMoments(UserMoment userMoment) throws Exception{
        userMoment.setCreateTime(new Date());
        userMomentsDao.addUserMoments(userMoment);
        DefaultMQProducer producer = applicationContext.getBean("momentsProducer", DefaultMQProducer.class);
        Message msg = new Message(UserMomentsConstant.TOPIC_MOMENTS, JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8));
        RocketMQUtil.syncSendMsg(producer, msg);
    }

    public List<UserMoment> getUserSubscribedMoments(Long userId) {
        String key = UserMomentsConstant.REDIS_KEY_PREFIX + userId;
        String subListStr =  redisTemplate.opsForValue().get(key);
        List<UserMoment> subMoments = new ArrayList<>();
        if(subListStr != null && !subListStr.isEmpty()){
            subMoments =  JSONArray.parseArray(subListStr, UserMoment.class);
        }
        return subMoments;
    }
}

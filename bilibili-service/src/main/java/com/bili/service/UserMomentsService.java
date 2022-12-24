package com.bili.service;

import com.alibaba.fastjson.JSONObject;
import com.bili.dao.UserMomentsDao;
import com.bili.domain.UserMoment;
import com.bili.domain.constant.UserMomentsConstant;
import com.bili.service.util.RocketMQUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class UserMomentsService {
    @Resource
    private  UserMomentsDao userMomentsDao;
    @Resource
    private ApplicationContext applicationContext;
    
    public void addUserMoments(UserMoment userMoment) throws Exception{
        userMoment.setCreteTime(new Date());
        userMomentsDao.addUserMoments(userMoment);
        DefaultMQProducer producer = applicationContext.getBean("momentsProducer", DefaultMQProducer.class);
        Message msg = new Message(UserMomentsConstant.TOPIC_MOMENTS, JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8));
        RocketMQUtil.syncSendMsg(producer, msg);
    }
}

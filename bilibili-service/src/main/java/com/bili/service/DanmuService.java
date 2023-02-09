package com.bili.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bili.dao.DanmuDao;
import com.bili.domain.Danmu;
import io.netty.util.internal.StringUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DanmuService {
    @Resource
    private DanmuDao danmuDao;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public void addDanmu(Danmu danmu) {
        danmuDao.addDanmu(danmu);
    }

    public List<Danmu> getDanmus(Map<String, Object> params) {
        return danmuDao.getDamus(params);
    }

    public void addDanmusToRedis(Danmu danmu) {
        String key = "danmu-video-" + danmu.getVideoId();
        String value = redisTemplate.opsForValue().get(key);
        List<Danmu> list = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(value)) {
            list = JSONArray.parseArray(value, Danmu.class);
        }
        list.add(danmu);
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(danmu));
    }
}

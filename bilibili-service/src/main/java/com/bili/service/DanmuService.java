package com.bili.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bili.dao.DanmuDao;
import com.bili.domain.Danmu;
import io.netty.util.internal.StringUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DanmuService {
    private final String DANMU_REDIS_KEY = "danmu-video-";
    @Resource
    private DanmuDao danmuDao;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public void addDanmu(Danmu danmu) {
        danmuDao.addDanmu(danmu);
    }

    @Async
    public void asyncDanmu(Danmu danmu) {
        danmuDao.addDanmu(danmu);
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

    public List<Danmu> getDanmus(Long videoId, String startTime, String endTime) throws ParseException {
        List<Danmu> list;

        // first try to retrieve the list of damus from redis;
        String key = DANMU_REDIS_KEY + videoId;
        String value = redisTemplate.opsForValue().get(key);
        if (!StringUtil.isNullOrEmpty(value)) {
            list = JSONArray.parseArray(value, Danmu.class);
            if (!StringUtil.isNullOrEmpty(startTime) && !StringUtil.isNullOrEmpty(endTime)) {
                // parse strings to Date type;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDate = sdf.parse(startTime);
                Date endDate = sdf.parse(endTime);

                // filer the list by startTime and endTime;
                List<Danmu> filteredList = new ArrayList<>();
                for (Danmu danmu : list) {
                    Date createTime = danmu.getCreateTime();
                    if (createTime.after(startDate) && createTime.before(endDate)) {
                        filteredList.add(danmu);
                    }
                }
                return filteredList;
            } else {
                // if the startTime is null or endTime is null return the list directly;
                return list;
            }
        }
        
        // retrieve the list of damus from the normal database;
        Map<String, Object> params = new HashMap<>();
        params.put("videoId", videoId);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        list = danmuDao.getDamus(params);
        // save the retrieved data to redis;
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));
        return list;
    }
}

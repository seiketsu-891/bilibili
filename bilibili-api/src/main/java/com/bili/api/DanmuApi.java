package com.bili.api;

import com.bili.api.support.UserSupport;
import com.bili.domain.Danmu;
import com.bili.domain.JsonResponse;
import com.bili.service.DanmuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class DanmuApi {
    @Resource
    private DanmuService danmuService;
    @Resource
    private UserSupport userSupport;

    @GetMapping("/danmus")
    public JsonResponse<List<Danmu>> getDammus(@RequestParam Long videoId,
                                               String startTime,
                                               String endTime) throws Exception {
        List<Danmu> list;
        try {
            userSupport.getCurrentUserId();
            list = danmuService.getDanmus(videoId, startTime, endTime);
        } catch (Exception e) {
            // a vistor can't filter the danmus by startTime and endTime;
            list = danmuService.getDanmus(videoId, null, null);
        }
        return new JsonResponse<>(list);
    }
}

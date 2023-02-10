package com.bili.api;

import com.bili.domain.JsonResponse;
import com.bili.service.ElasticsearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class SystemApi {
    @Resource
    private ElasticsearchService elasticsearchService;

    @GetMapping("/contents")
    public JsonResponse<List<Map<String, Object>>> getContents(@RequestParam String keyword,
                                                               @RequestParam Integer pageNum,
                                                               @RequestParam Integer pageSize) throws IOException {
        List<Map<String, Object>> list = elasticsearchService.getContents(keyword, pageNum, pageSize);
        return new JsonResponse<>(list);
    }
}

package com.bili.service;

import com.bili.dao.repository.UserInfoRepository;
import com.bili.dao.repository.VideoRepository;
import com.bili.domain.UserInfo;
import com.bili.domain.Video;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ElasticsearchService {
    @Resource
    private VideoRepository videoRepository;
    @Resource
    private UserInfoRepository userInfoRepository;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    public Video getVideo(String keyword) {
        return videoRepository.findAByTitleLike(keyword);
    }

    public void addVideo(Video video) {
        videoRepository.save(video);
    }

    public void deleteAll() {
        videoRepository.deleteAll();
    }

    public void addUserInfo(UserInfo userInfo) {
        userInfoRepository.save(userInfo);
    }

    public List<Map<String, Object>> getContents(String keyword, Integer pageNo, Integer pageSize) throws IOException {
        String[] indices = {"videos", "user-infos"};
        String[] cols = {"title", "nick", "description"};
        // searchBuilder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(pageNo - 1);
        sourceBuilder.size(pageSize - 1);
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery(keyword, cols[0], cols[1], cols[2]);
        sourceBuilder.query(matchQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // searchRequest
        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.source(sourceBuilder);

        // highlight the keywords
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        for (String col : cols) {
            highlightBuilder.fields().add(new HighlightBuilder.Field(col));
        }
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style=\"color: red\">");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        //start searching
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<Map<String, Object>> list = new ArrayList<>();

        // iterate the search results
        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits) {
            Map<String, HighlightField> highlightBuilderFields = hit.getHighlightFields();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            for (String col : cols) {
                HighlightField field = highlightBuilderFields.get(col);
                if (field != null) {
                    Text[] fragments = field.fragments();
                    String str = Arrays.toString(fragments);
                    // removing the square brackets from the beginning and end of the string.
                    str = str.substring(1, str.length() - 1);
                    sourceAsMap.put(col, str);
                }
            }

            list.add(sourceAsMap);
        }
        return list;
    }
}

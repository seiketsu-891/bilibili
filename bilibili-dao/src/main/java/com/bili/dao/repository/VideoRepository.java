package com.bili.dao.repository;

import com.bili.domain.Video;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoRepository extends ElasticsearchRepository<Video, Long> {
    Video findAByTitleLike(String keyword);
}

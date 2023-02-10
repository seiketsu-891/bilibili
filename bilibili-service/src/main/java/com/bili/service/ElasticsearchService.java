package com.bili.service;

import com.bili.dao.repository.VideoRepository;
import com.bili.domain.Video;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ElasticsearchService {
    @Resource
    private VideoRepository videoRepository;

    public Video getVideo(String keyword) {
        return videoRepository.findAByTitleLike(keyword);
    }

    public void addVideo(Video video) {
        videoRepository.save(video);
    }

    public void deleteAll() {
        videoRepository.deleteAll();
    }
}

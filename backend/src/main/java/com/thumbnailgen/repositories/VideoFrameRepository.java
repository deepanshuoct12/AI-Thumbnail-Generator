package com.thumbnailgen.repositories;

import com.thumbnailgen.entities.VideoFrame;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VideoFrameRepository extends MongoRepository<VideoFrame, String> {
    List<VideoFrame> findByVideoIdOrderByScoreDesc(String videoId, Pageable pageable);
}

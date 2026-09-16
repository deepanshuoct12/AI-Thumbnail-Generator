package com.thumbnailgen.repositories;

import com.thumbnailgen.entities.Video;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VideoRepository extends MongoRepository<Video, String> {
}

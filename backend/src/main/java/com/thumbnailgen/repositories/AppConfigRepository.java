package com.thumbnailgen.repositories;

import com.thumbnailgen.entities.AppConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppConfigRepository extends MongoRepository<AppConfig, String> {
}

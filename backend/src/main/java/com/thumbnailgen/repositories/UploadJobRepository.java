package com.thumbnailgen.repositories;

import com.thumbnailgen.entities.UploadJob;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UploadJobRepository extends MongoRepository<UploadJob, String> {
}

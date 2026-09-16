package com.thumbnailgen.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "upload_jobs")
@Getter
@Setter
@NoArgsConstructor
public class UploadJob {
    @Id
    private String id;

    private String originalFilename;
    private String videoPath;
    private JobStatus status;
    private Instant createdAt;
    private Instant completedAt;
}

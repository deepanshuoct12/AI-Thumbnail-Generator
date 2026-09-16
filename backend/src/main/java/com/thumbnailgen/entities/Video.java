package com.thumbnailgen.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "videos")
@Getter
@Setter
@NoArgsConstructor
public class Video {
    @Id
    private String id;

    private String filename;
    private String videoPath;
    private String fileFormat;
    private VideoStatus status;

    private String style;
    private String resolution;
    private int count;

    private Instant createdAt;
    private Instant completedAt;
}

package com.thumbnailgen.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "video_frames")
@Getter
@Setter
@NoArgsConstructor
public class VideoFrame {
    @Id
    private String id;

    private String videoId;
    private String framePath;
    private double timestampSeconds;
    private double score;
    private double sharpness;
    private double brightness;
    private double contrast;
    private double colorfulness;
    private double faceScore;
}

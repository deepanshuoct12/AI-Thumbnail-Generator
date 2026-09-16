package com.thumbnailgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VideoThumbnailApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoThumbnailApplication.class, args);
    }
}

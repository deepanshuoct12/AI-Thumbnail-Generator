package com.thumbnailgen.controllers;

import com.thumbnailgen.controllers.responses.FrameResponse;
import com.thumbnailgen.controllers.responses.VideoResponse;
import com.thumbnailgen.controllers.responses.VideoUploadResponse;
import com.thumbnailgen.entities.AppConfig;
import com.thumbnailgen.entities.Video;
import com.thumbnailgen.entities.VideoFrame;
import com.thumbnailgen.entities.VideoStatus;
import com.thumbnailgen.events.ThumbnailJobEvent;
import com.thumbnailgen.repositories.AppConfigRepository;
import com.thumbnailgen.repositories.VideoFrameRepository;
import com.thumbnailgen.repositories.VideoRepository;
import com.thumbnailgen.services.ThumbnailEventProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class VideoController {

    @Value("${app.upload-dir}")
    private String uploadDir;

    private final VideoRepository videoRepository;
    private final VideoFrameRepository frameRepository;
    private final AppConfigRepository appConfigRepository;
    private final ThumbnailEventProducer producer;

    public VideoController(VideoRepository videoRepository,
                           VideoFrameRepository frameRepository,
                           AppConfigRepository appConfigRepository,
                           ThumbnailEventProducer producer) {
        this.videoRepository = videoRepository;
        this.frameRepository = frameRepository;
        this.appConfigRepository = appConfigRepository;
        this.producer = producer;
    }

    @GetMapping("/config")
    public ResponseEntity<AppConfig> getConfig() {
        return ResponseEntity.ok(appConfigRepository.findById("main").orElseThrow());
    }

    @PostMapping("/videos/upload")
    public ResponseEntity<VideoUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("style") String style,
            @RequestParam("resolution") String resolution,
            @RequestParam(value = "count", defaultValue = "5") int count) throws Exception {

        AppConfig config = appConfigRepository.findById("main").orElseThrow();

        String original = file.getOriginalFilename();
        String safeName = System.currentTimeMillis() + "_" + (original == null ? "video" : original);

        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        if (!config.getSupportedFormats().contains(ext)) {
            throw new RuntimeException("Unsupported file format: " + ext);
        }
        if (!config.getSupportedStyles().contains(style)) {
            throw new RuntimeException("Unsupported style: " + style);
        }
        if (!config.getSupportedResolutions().contains(resolution)) {
            throw new RuntimeException("Unsupported resolution: " + resolution);
        }
        if (count < 1 || count > 5) {
            throw new RuntimeException("Count must be between 1 and 5");
        }

        Path dir = Path.of(uploadDir);
        Files.createDirectories(dir);
        Path target = dir.resolve(safeName);
        file.transferTo(target.toFile());

        Video video = new Video();
        video.setFilename(original);
        video.setVideoPath(target.toString());
        video.setFileFormat(ext);
        video.setStatus(VideoStatus.PENDING);
        video.setStyle(style);
        video.setResolution(resolution);
        video.setCount(count);
        video.setCreatedAt(Instant.now());
        Video saved = videoRepository.save(video);

        producer.publish(new ThumbnailJobEvent(
            saved.getId(),
            saved.getVideoPath(),
            saved.getFilename(),
            saved.getStyle(),
            saved.getResolution(),
            saved.getCount()
        ));

        return ResponseEntity.ok(new VideoUploadResponse(saved.getId(), saved.getStatus().name()));
    }

    @GetMapping("/videos/{id}")
    public ResponseEntity<VideoResponse> getVideo(@PathVariable String id) {
        Video video = videoRepository.findById(id).orElseThrow();
        List<VideoFrame> top = frameRepository.findByVideoIdOrderByScoreDesc(id, PageRequest.of(0, video.getCount()));
        return ResponseEntity.ok(new VideoResponse(
            video.getId(),
            video.getFilename(),
            video.getFileFormat(),
            video.getStatus().name(),
            video.getStyle(),
            video.getResolution(),
            video.getCount(),
            top.stream().map(f -> new FrameResponse(
                f.getId(),
                f.getVideoId(),
                f.getFramePath(),
                f.getTimestampSeconds(),
                f.getScore(),
                f.getSharpness(),
                f.getBrightness(),
                f.getContrast(),
                f.getColorfulness(),
                f.getFaceScore()
            )).toList()
        ));
    }

    @GetMapping("/videos/{videoId}/frame/{frameId}")
    public ResponseEntity<byte[]> getFrame(@PathVariable String videoId, @PathVariable String frameId) throws Exception {
        VideoFrame f = frameRepository.findById(frameId).orElseThrow();
        if (!f.getVideoId().equals(videoId)) {
            throw new RuntimeException("Frame does not belong to video");
        }
        Path p = Path.of(f.getFramePath());
        byte[] bytes = Files.readAllBytes(p);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(bytes);
    }

    @GetMapping("/videos/{videoId}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable String videoId, @RequestParam String frameId) throws Exception {
        VideoFrame f = frameRepository.findById(frameId).orElseThrow();
        if (!f.getVideoId().equals(videoId)) {
            throw new RuntimeException("Frame does not belong to video");
        }
        FileSystemResource res = new FileSystemResource(f.getFramePath());
        String filename = "thumbnail_" + f.getId() + ".jpg";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.IMAGE_JPEG)
            .body(res);
    }
}

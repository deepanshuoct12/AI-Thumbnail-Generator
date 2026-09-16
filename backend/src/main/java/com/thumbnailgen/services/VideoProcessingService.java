package com.thumbnailgen.services;

import com.thumbnailgen.entities.Video;
import com.thumbnailgen.entities.VideoFrame;
import com.thumbnailgen.entities.VideoStatus;
import com.thumbnailgen.events.ThumbnailJobEvent;
import com.thumbnailgen.repositories.VideoFrameRepository;
import com.thumbnailgen.repositories.VideoRepository;
import com.thumbnailgen.services.dto.ScoreResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoProcessingService {

    private static final Logger log = LoggerFactory.getLogger(VideoProcessingService.class);

    private final VideoRepository videoRepository;
    private final VideoFrameRepository frameRepository;
    private final FfmpegService ffmpegService;
    private final AiScoringService aiScoringService;

    public VideoProcessingService(VideoRepository videoRepository,
                                  VideoFrameRepository frameRepository,
                                  FfmpegService ffmpegService,
                                  AiScoringService aiScoringService) {
        this.videoRepository = videoRepository;
        this.frameRepository = frameRepository;
        this.ffmpegService = ffmpegService;
        this.aiScoringService = aiScoringService;
    }

    @KafkaListener(topics = "${app.topic}", groupId = "thumbnail-processors", concurrency = "3")
    public void processEvent(ThumbnailJobEvent event, Acknowledgment ack) {
        String videoId = event.videoId();
        Video video = videoRepository.findById(videoId).orElseThrow();
        try {
            video.setStatus(VideoStatus.EXTRACTING);
            videoRepository.save(video);

            List<FfmpegService.FrameInfo> frames = ffmpegService.extractFrames(Path.of(video.getVideoPath()), videoId, video.getResolution());

            Map<String, VideoFrame> frameMap = new HashMap<>();
            for (FfmpegService.FrameInfo info : frames) {
                Path p = info.path();
                VideoFrame f = new VideoFrame();
                f.setVideoId(videoId);
                f.setFramePath(p.toString());
                f.setTimestampSeconds(info.timestampSeconds());
                frameMap.put(p.getFileName().toString(), f);
            }
            frameRepository.saveAll(frameMap.values());

            video.setStatus(VideoStatus.SCORING);
            videoRepository.save(video);

            List<Path> framePaths = frames.stream().map(FfmpegService.FrameInfo::path).toList();
            List<ScoreResponse> scores = aiScoringService.scoreFrames(framePaths, video.getStyle());
            for (ScoreResponse s : scores) {
                VideoFrame f = frameMap.get(s.filename());
                if (f != null) {
                    f.setScore(s.score());
                    f.setSharpness(s.sharpness());
                    f.setBrightness(s.brightness());
                    f.setContrast(s.contrast());
                    f.setColorfulness(s.colorfulness());
                    f.setFaceScore(s.face_score());
                }
            }
            frameRepository.saveAll(frameMap.values());

            video.setStatus(VideoStatus.COMPLETED);
            video.setCompletedAt(Instant.now());
        } catch (Exception e) {
            log.error("Processing failed for video {}", videoId, e);
            video.setStatus(VideoStatus.FAILED);
        }
        videoRepository.save(video);
        ack.acknowledge();
    }
}

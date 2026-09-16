package com.thumbnailgen.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FfmpegService {

    @Value("${app.frames-dir}")
    private String framesDir;

    private final Map<String, Integer> resolutionWidths = Map.of(
        "360p", 640,
        "480p", 854,
        "720p", 1280,
        "1080p", 1920
    );

    private static final Pattern PTS_TIME_PATTERN = Pattern.compile("pts_time:\\s*([0-9.]+)");

    public record FrameInfo(Path path, double timestampSeconds) {}

    @PostConstruct
    public void init() throws Exception {
        Files.createDirectories(Path.of(framesDir));
    }

    public Path getVideoFramesDir(String videoId) throws Exception {
        Path dir = Path.of(framesDir, videoId);
        Files.createDirectories(dir);
        return dir;
    }

    public List<FrameInfo> extractFrames(Path video, String videoId, String resolution) throws Exception {
        Path outputDir = getVideoFramesDir(videoId);
        Path pattern = outputDir.resolve("frame-%04d.jpg");
        int width = resolutionWidths.getOrDefault(resolution, 854);
        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg", "-y", "-i", video.toString(),
            "-vf", "select='eq(pict_type\\,I)+gt(scene\\,0.3)',showinfo,scale=" + width + ":-1",
            "-fps_mode", "vfr",
            "-q:v", "2",
            pattern.toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        List<Double> timestamps = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = PTS_TIME_PATTERN.matcher(line);
                if (m.find()) {
                    timestamps.add(Double.parseDouble(m.group(1)));
                }
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("ffmpeg failed with exit code " + exitCode);
        }
        List<Path> framePaths;
        try (Stream<Path> paths = Files.list(outputDir)) {
            framePaths = paths
                .filter(p -> p.toString().endsWith(".jpg"))
                .sorted()
                .collect(Collectors.toList());
        }
        List<FrameInfo> result = new ArrayList<>();
        for (int i = 0; i < framePaths.size(); i++) {
            double ts = i < timestamps.size() ? timestamps.get(i) : i * 0.5;
            result.add(new FrameInfo(framePaths.get(i), ts));
        }
        return result;
    }
}

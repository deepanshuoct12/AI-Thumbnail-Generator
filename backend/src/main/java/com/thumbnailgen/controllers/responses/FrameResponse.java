package com.thumbnailgen.controllers.responses;

public record FrameResponse(
    String id,
    String videoId,
    String framePath,
    double timestampSeconds,
    double score,
    double sharpness,
    double brightness,
    double contrast,
    double colorfulness,
    double faceScore
) {
}

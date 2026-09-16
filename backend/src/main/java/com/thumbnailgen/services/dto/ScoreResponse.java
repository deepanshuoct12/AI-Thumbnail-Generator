package com.thumbnailgen.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScoreResponse(
    double score,
    double sharpness,
    double brightness,
    double contrast,
    double colorfulness,
    double face_score,
    int width,
    int height,
    String filename
) {
}

package com.thumbnailgen.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScoreBatchResponse(
    List<ScoreResponse> results
) {
}

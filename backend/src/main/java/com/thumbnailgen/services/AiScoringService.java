package com.thumbnailgen.services;

import com.thumbnailgen.services.dto.ScoreBatchResponse;
import com.thumbnailgen.services.dto.ScoreResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.file.Path;
import java.util.List;

@Service
public class AiScoringService {

    private final RestClient restClient;

    public AiScoringService(@Value("${app.ai-service.url}") String aiUrl) {
        this.restClient = RestClient.create(aiUrl);
    }

    public List<ScoreResponse> scoreFrames(List<Path> framePaths, String style) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        for (Path p : framePaths) {
            parts.add("files", new FileSystemResource(p));
        }
        ScoreBatchResponse resp = restClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/score/batch")
                .queryParam("style", style)
                .build())
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(parts)
            .retrieve()
            .toEntity(ScoreBatchResponse.class)
            .getBody();
        return resp != null ? resp.results() : List.of();
    }
}

package com.thumbnailgen.controllers.responses;

import java.util.List;

public record VideoResponse(
    String id,
    String filename,
    String fileFormat,
    String status,
    String style,
    String resolution,
    int count,
    List<FrameResponse> frames
) {
}

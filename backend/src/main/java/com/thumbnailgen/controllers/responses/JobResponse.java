package com.thumbnailgen.controllers.responses;

import java.util.List;

public record JobResponse(
    String id,
    String filename,
    String status,
    List<FrameResponse> frames
) {
}

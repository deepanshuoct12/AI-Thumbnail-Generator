package com.thumbnailgen.events;

public record ThumbnailJobEvent(
    String videoId,
    String videoPath,
    String filename,
    String style,
    String resolution,
    int count
) {
}

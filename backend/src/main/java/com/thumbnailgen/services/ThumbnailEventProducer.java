package com.thumbnailgen.services;

import com.thumbnailgen.events.ThumbnailJobEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ThumbnailEventProducer {

    private final KafkaTemplate<String, ThumbnailJobEvent> kafkaTemplate;

    @Value("${app.topic}")
    private String topic;

    public ThumbnailEventProducer(KafkaTemplate<String, ThumbnailJobEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ThumbnailJobEvent event) {
        kafkaTemplate.send(topic, event.videoId(), event);
    }
}

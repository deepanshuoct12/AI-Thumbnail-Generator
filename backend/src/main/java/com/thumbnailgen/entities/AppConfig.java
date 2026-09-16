package com.thumbnailgen.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "app_config")
@Getter
@Setter
@NoArgsConstructor
public class AppConfig {
    @Id
    private String id;

    private List<String> supportedFormats;
    private List<String> supportedStyles;
    private List<String> supportedResolutions;
}

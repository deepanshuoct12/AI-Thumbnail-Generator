package com.thumbnailgen.config;

import com.thumbnailgen.entities.AppConfig;
import com.thumbnailgen.repositories.AppConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AppConfigSeeder {

    @Bean
    CommandLineRunner seedAppConfig(AppConfigRepository repository) {
        return args -> {
            if (!repository.existsById("main")) {
                AppConfig config = new AppConfig();
                config.setId("main");
                config.setSupportedFormats(List.of("mp4", "mov", "avi", "mkv", "webm"));
                config.setSupportedStyles(List.of("natural", "dark", "bright", "colorful", "sharp", "face-focus"));
                config.setSupportedResolutions(List.of("360p", "480p", "720p", "1080p"));
                repository.save(config);
            }
        };
    }
}

package be.ghostwritertje.nuclearr.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class NuclearrConfig {

    @Autowired
    ResourceLoader resourceLoader;

    @Value("${nuclearr.config.path}")
    private String configPath;

    @PostConstruct
    public void initializeDefaultConfig() throws IOException {
        Path propertyPath = Path.of(configPath, "application.yaml");
        if (!Files.exists(propertyPath)) {
            Files.copy(resourceLoader.getResource("classpath:config/application-example.yaml").getFile().toPath(), propertyPath);
        }
    }
}

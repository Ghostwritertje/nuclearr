package be.ghostwritertje.nuclearr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "nuclearr")
@Data
public class NuclearrConfiguration {

    private List<String> trackers = new ArrayList<>();

    private TransmissionConfiguration transmission;


    private boolean hardlinksEnabled;

    private Integer batchSize;


    @Data
    private static class TransmissionConfiguration {
        private String url;
        private String user;
        private String password;
    }
}
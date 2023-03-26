package be.ghostwritertje.nuclearr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "nuclearr")
@Data
public class NuclearrConfiguration {

    private Set<String> trackers = new HashSet<>();

    private TransmissionConfiguration transmission;


    private boolean hardlinksEnabled;
    private boolean removeEnabled;

    private Integer batchSize;


    @Data
    private static class TransmissionConfiguration {
        private String url;
        private String user;
        private String password;
    }
}
package be.ghostwritertje.nuclearr;

import be.ghostwritertje.nuclearr.config.YamlPropertyLoaderFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableR2dbcRepositories
@PropertySource(value = "file:${nuclearr.config.path}/application.yaml", factory = YamlPropertyLoaderFactory.class, ignoreResourceNotFound = true)
@EnableScheduling
public class NuclearrApplication {

    public static void main(String[] args) {
        SpringApplication.run(NuclearrApplication.class, args);
    }

}

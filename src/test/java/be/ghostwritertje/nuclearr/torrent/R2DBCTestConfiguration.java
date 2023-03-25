package be.ghostwritertje.nuclearr.torrent;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

@Configuration
@Profile("test")
@Primary
public class R2DBCTestConfiguration extends AbstractR2dbcConfiguration {

    @Value("${nuclearr.config.path:/config}")
    private String configPath;

    @Bean
    @Profile("test")
    @Override
    public ConnectionFactory connectionFactory() {
        return new H2ConnectionFactory(
                H2ConnectionConfiguration.builder()
                        .url("~/test-db")
                        .username("sa")
                        .password("sa")
                        .build()
        );
    }
}

package be.ghostwritertje.nuclearr.config;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@Primary
public class R2DBCConfiguration extends AbstractR2dbcConfiguration {

    @Value("${nuclearr.config.path:/config}")
    private String configPath;

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        return new H2ConnectionFactory(
                H2ConnectionConfiguration.builder()
                        .file(this.configPath + "/nuclearrdb")
                        .username("sa")
                        .password("sa")
                        .build()
        );
    }
}

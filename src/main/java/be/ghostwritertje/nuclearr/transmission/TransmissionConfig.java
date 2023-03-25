package be.ghostwritertje.nuclearr.transmission;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TransmissionConfig {

    @Value("${nuclearr.transmission.url}")
    private String transmissionUrl;
    @Value("${nuclearr.transmission.user}")
    private String transmissionUser;
    @Value("${nuclearr.transmission.password}")
    private String transmissionPassword;
    @Bean
    public WebClient transmissionClient() {
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder().codecs(
                                clientCodecConfigurer ->
                                        clientCodecConfigurer.defaultCodecs().maxInMemorySize(100000000))
                        .build()
                )
                .baseUrl(this.transmissionUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(this.transmissionUser, this.transmissionPassword))
                .build();
    }


}

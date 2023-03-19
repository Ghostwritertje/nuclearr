package be.ghostwritertje.nuclearr.transmission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransmissionAdapter {

    public static final String TRANSMISSION_SESSION_ID_HEADER = "X-Transmission-Session-Id";
    private String transmissionSessionId = "";
    private final WebClient transmissionClient;


    public Mono<List<TransmissionTorrent>> retrieveAllTorrents() {
        return this.sendRequest(new TransmissionRequest())
                .flatMap(transmissionResponse -> Mono.just(transmissionResponse.getArguments().getTorrents()));
    }

    public Mono<Void> removeTorrent(Integer id) {
        TransmissionRequest request = TransmissionRequest.builder()
                .method(TransmissionRequest.TORRENT_REMOVE)
                .arguments(TransmissionArguments.builder()
                        .fields(null)
                        .ids(List.of(id))
                        .deleteLocalData(true)
                        .build())
                .build();
        return sendRequest(request)
                .then(Mono.fromRunnable(() -> log.info("Removed torrent with id {} from transmission", id)));
    }

    public Mono<TransmissionTorrent> getDetails(Integer id) {
        TransmissionRequest request = TransmissionRequest.builder()
                .arguments(TransmissionArguments.builder()
                        .fields(TransmissionArguments.ALL_FIELDS)
                        .ids(List.of(id))
                        .build())
                .build();
        return sendRequest(request)
                .flatMap(transmissionResponse -> transmissionResponse.getArguments().getTorrents().stream().findFirst().map(Mono::just).orElseGet(Mono::empty));
    }

    private Mono<TransmissionResponse> sendRequest(TransmissionRequest request) {
        return this.getResponse(request)
                .onErrorResume(InvalidSessionIdException.class, e -> getResponse(request));
    }

    private Mono<TransmissionResponse> getResponse(TransmissionRequest request) {
        return transmissionClient.post()
                .uri("/transmission/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TRANSMISSION_SESSION_ID_HEADER, transmissionSessionId)
                .body(Mono.just(request), TransmissionRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.value() == HttpStatus.CONFLICT.value(),
                        clientResponse -> {
                            this.transmissionSessionId = clientResponse.headers().header(TRANSMISSION_SESSION_ID_HEADER).stream().findFirst().orElse(transmissionSessionId);
                            return Mono.just(new InvalidSessionIdException());
                        })
                .bodyToMono(TransmissionResponse.class);
    }

    private static class InvalidSessionIdException extends Throwable {

    }
}

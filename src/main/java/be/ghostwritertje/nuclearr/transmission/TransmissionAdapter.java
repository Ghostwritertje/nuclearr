package be.ghostwritertje.nuclearr.transmission;

import be.ghostwritertje.nuclearr.config.NuclearrConfiguration;
import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrent;
import be.ghostwritertje.nuclearr.internaltorrent.TorrentClientAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Level;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransmissionAdapter implements TorrentClientAdapter {

    public static final String TRANSMISSION_SESSION_ID_HEADER = "X-Transmission-Session-Id";
    private final WebClient transmissionClient;
    private final TransmissionMapper transmissionMapper;
    private String transmissionSessionId = "";

    private final NuclearrConfiguration nuclearrConfiguration;

    public Mono<List<TransmissionTorrent>> retrieveAllTorrents() {
        return this.sendRequest(new TransmissionRequest())
                .flatMap(transmissionResponse -> Mono.just(transmissionResponse.getArguments().getTorrents()));
    }


    @Override
    public Flux<InternalTorrent> getTorrents() {
        return this.retrieveAllTorrents()
                .log("transmission", Level.FINE)
                .flatMapMany(list -> Flux.fromStream(list.stream()))
                .onBackpressureBuffer()
                .buffer(nuclearrConfiguration.getBatchSize())
                .doOnEach(ignored -> log.info("Retrieving details of {}} items from Transmission {}", nuclearrConfiguration.getBatchSize(), ignored.getType()))
                .flatMap(tt -> this.getDetails(tt.stream().map(TransmissionTorrent::getId).toArray(Integer[]::new)))
                .map(transmissionMapper::mapInternalTorrent);
    }

    @Override
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

    public Flux<TransmissionTorrent> getDetails(Integer... id) {
        TransmissionRequest request = TransmissionRequest.builder()
                .arguments(TransmissionArguments.builder()
                        .fields(TransmissionArguments.ALL_FIELDS)
                        .ids(List.of(id))
                        .build())
                .build();
        return sendRequest(request)
                .flatMapMany(transmissionResponse -> Flux.fromIterable(transmissionResponse.getArguments().getTorrents()))
                .map(transmissionTorrent -> {
                    transmissionTorrent.getFiles()
                            .forEach(transmissionFile -> transmissionFile.setName(transmissionFile.getName()));
                    return transmissionTorrent;
                });
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

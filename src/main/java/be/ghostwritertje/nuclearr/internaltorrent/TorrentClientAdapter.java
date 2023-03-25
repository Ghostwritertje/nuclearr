package be.ghostwritertje.nuclearr.internaltorrent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TorrentClientAdapter {

    Flux<InternalTorrent> getTorrents();

    Mono<Void> removeTorrent(Integer id);
}

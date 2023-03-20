package be.ghostwritertje.nuclearr.internaltorrent;

import be.ghostwritertje.nuclearr.transmission.TransmissionTorrent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TorrentClientAdapter<X extends InternalTorrent.InternalTorrentFile> {

    Flux<InternalTorrent<X>> getTorrents();

    Mono<Void> removeTorrent(Integer id);
}

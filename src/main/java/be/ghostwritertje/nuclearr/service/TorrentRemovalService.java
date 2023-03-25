package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.config.NuclearrConfiguration;
import be.ghostwritertje.nuclearr.internaltorrent.TorrentClientAdapter;
import be.ghostwritertje.nuclearr.removed.Removed;
import be.ghostwritertje.nuclearr.removed.RemovedService;
import be.ghostwritertje.nuclearr.representation.Representation;
import be.ghostwritertje.nuclearr.representation.RepresentationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TorrentRemovalService {

    private final RemovedService removedService;
    private final TorrentClientAdapter torrentClientAdapter;
    private final RepresentationService representationService;

    private final NuclearrConfiguration nuclearrConfiguration;


    public Mono<Void> removeTorrents() {
        long minimumSeedTime = TimeUnit.DAYS.toSeconds(30);

        return representationService.findAll()
                .filter(masterTorrentDto -> Objects.nonNull(masterTorrentDto.getSeedTime()))
                .filter(masterTorrentDto -> masterTorrentDto.getSeedTime() > minimumSeedTime)
                .filter(masterTorrentDto -> Objects.nonNull(masterTorrentDto.getHardlinks()))
                .filter(masterTorrentDto -> masterTorrentDto.getHardlinks() < 2)
                .filter(masterTorrentDto -> nuclearrConfiguration.getTrackers().containsAll(Arrays.asList(masterTorrentDto.getTrackers())))
                .flatMap(this::removeTorrent)
                .doOnEach(ignored -> log.info("Removing torrent "))
                .map(Removed::getTransmissionId)
                .flatMap(this::removeTorrent)
                .then(Mono.fromRunnable(() -> log.info("Finished removing torrents")));
    }

    private Flux<Removed> removeTorrent(Representation representation) {
        return Flux.fromArray(representation.getChildTorrentTransmissionIds())
                .flatMap(id -> this.remove(id, representation));
    }

    private Mono<Removed> remove(Integer id, Representation representation) {
        return removedService.findRemovedByTransmissionId(id)
                .switchIfEmpty(removedService.save(Removed.builder()
                        .name(representation.getName())
                        .seedTime(representation.getSeedTime())
                        .hardlinks(representation.getHardlinks())
                        .trackers(Arrays.stream(representation.getTrackers()).distinct().collect(Collectors.joining(", ")))
                        .transmissionId(id)
                        .build()));
    }

    public Mono<Void> removeTorrent(Integer transmissionId) {
        if (!nuclearrConfiguration.isHardlinksEnabled()) {
            return Mono.fromRunnable(() -> log.info("Not removing torrent with id {} (removing is DISABLED)", transmissionId));
        }
        return torrentClientAdapter.removeTorrent(transmissionId);
    }
}

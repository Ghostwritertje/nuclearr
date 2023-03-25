package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.config.NuclearrConfiguration;
import be.ghostwritertje.nuclearr.internaltorrent.TorrentClientAdapter;
import be.ghostwritertje.nuclearr.removed.Removed;
import be.ghostwritertje.nuclearr.presentation.TorrentSupportDto;
import be.ghostwritertje.nuclearr.presentation.TrackerDto;
import be.ghostwritertje.nuclearr.removed.RemovedService;
import be.ghostwritertje.nuclearr.transmission.TransmissionAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TorrentRemovalService {

    private final RemovedService removedService;
    private final TorrentClientAdapter<?> torrentClientAdapter;
    public final Set<String> trackers;
    private final RepresentationService representationService;

    public TorrentRemovalService(
            RemovedService removedService,
            TorrentClientAdapter<?> torrentClientAdapter,
            RepresentationService representationService,
            NuclearrConfiguration nuclearrConfiguration) {
        this.removedService = removedService;
        this.torrentClientAdapter = torrentClientAdapter;
        this.trackers = new HashSet<>(nuclearrConfiguration.getTrackers());
        this.representationService = representationService;
    }

    public Mono<Void> removeTorrents() {
        long minimumSeedTime = TimeUnit.DAYS.toSeconds(30);

        return representationService.represent()
                .filter(masterTorrentDto -> masterTorrentDto.getLowestSeedTime() > minimumSeedTime)
                .filter(masterTorrentDto -> masterTorrentDto.getMaxHardLinks() < 2)
                .filter(masterTorrentDto -> trackers.containsAll(masterTorrentDto.getAllTrackers()))
                .flatMap(masterTorrentDto -> removeTorrent(masterTorrentDto).then(Mono.just(masterTorrentDto)))
                .flatMap(masterTorrentDto -> Flux.fromStream(masterTorrentDto.getChildTorrentDtos().stream()))
                .flatMap(childTorrentDto -> removeTorrent(childTorrentDto).then(Mono.just(childTorrentDto)))
                .then(Mono.fromRunnable(() -> log.info("Finished removing torrents")));
    }

    private Mono<Removed> removeTorrent(TorrentSupportDto masterTorrentDto) {
        return removedService.findRemovedByTransmissionId(masterTorrentDto.getTransmissionId()) //todo: should work with hash maybe
                .switchIfEmpty(
                        removedService.save(Removed.builder()
                                        .name(masterTorrentDto.getName())
                                        .seedTime(masterTorrentDto.getSeedTime())
                                        .hardlinks(masterTorrentDto.getId())
                                        .trackers(masterTorrentDto.getTrackerList().stream().map(TrackerDto::getName).distinct().collect(Collectors.joining(", ")))
                                        .transmissionId(masterTorrentDto.getTransmissionId())
                                        .build())
                                .log()
                                .flatMap(removed -> torrentClientAdapter.removeTorrent(removed.getTransmissionId()).then(Mono.just(removed))));
    }

    public Mono<Void> removeTorrent(Integer transmissionId) {
        return torrentClientAdapter.removeTorrent(transmissionId);
    }
}

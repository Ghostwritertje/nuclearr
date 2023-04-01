package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.config.NuclearrConfiguration;
import be.ghostwritertje.nuclearr.fileitem.FileItem;
import be.ghostwritertje.nuclearr.fileitem.FileItemService;
import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrence;
import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrenceService;
import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrent;
import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrentFile;
import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrentMapper;
import be.ghostwritertje.nuclearr.internaltorrent.TorrentClientAdapter;
import be.ghostwritertje.nuclearr.torrent.Torrent;
import be.ghostwritertje.nuclearr.torrent.TorrentService;
import be.ghostwritertje.nuclearr.tracker.Tracker;
import be.ghostwritertje.nuclearr.tracker.TrackerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TorrentImporterService {
    public static final Pattern TRACKER_PATTERN = Pattern.compile("//([^/]+)(:\\d+)?/");
    private final TorrentClientAdapter torrentClientAdapter;
    private final TorrentService torrentService;
    private final TrackerService trackerService;
    private final FileItemService fileItemService;
    private final FileItemOccurrenceService fileItemOccurrenceService;
    private final InternalTorrentMapper internalTorrentMapper;

    private final TrackerExtractor trackerExtractor;
    private final NuclearrConfiguration nuclearrConfiguration;

    private Flux<String> extractTrackerList(InternalTorrent internalTorrent) {
        return Flux.fromStream(internalTorrent.getTrackerList().stream())
                .map(this.trackerExtractor::extract);
    }

    public Mono<Void> importTorrents() {
        log.info("Importing torrents");
        ConnectableFlux<InternalTorrent> internalTorrentFlux = torrentClientAdapter.getTorrents()
                .publish();

        ConnectableFlux<Torrent> torrentFlux = internalTorrentFlux
                .map(internalTorrentMapper::mapInternalTorrent)
                .buffer(nuclearrConfiguration.getBatchSize())
                .flatMap(torrentService::saveAll)
                .publish();


        Flux<FileItem> fileItemFlux = internalTorrentFlux.flatMap(internalTorrent -> Flux.fromIterable(internalTorrent.getFiles()))
                .map(InternalTorrentFile::getName)
                .map(s -> FileItem.builder().path(s).build())
                .distinct(FileItem::getPath)
                .buffer(nuclearrConfiguration.getBatchSize())
                .flatMap(fileItemService::saveAll);

        Flux<FileItemOccurrence> fileItemOccurrenceFlux = Flux.zip(internalTorrentFlux.onBackpressureBuffer(), torrentFlux.onBackpressureBuffer())
                .map(tuple -> {
                    log.debug("tuple {} - {}", tuple.getT1().getId(), tuple.getT2().getTransmissionId());
                    return tuple;
                })
                .flatMap(tuple2 -> Flux.fromStream(tuple2.getT1().getFiles().stream().map(file -> internalTorrentMapper.mapFileItemOccurrence(tuple2.getT2(), file.getName()))))
                .buffer(nuclearrConfiguration.getBatchSize())
                .flatMap(fileItemOccurrenceService::saveAll);

        Flux<Tracker> trackerFlux = Flux.zip(internalTorrentFlux.onBackpressureBuffer(), torrentFlux.onBackpressureBuffer())
                .flatMap(tuple -> extractTrackerList(tuple.getT1()).map(tracker -> internalTorrentMapper.mapTracker(tuple.getT2(), tracker)))
                .buffer(nuclearrConfiguration.getBatchSize())
                .flatMap(trackerService::saveAll);

        internalTorrentFlux.connect();
        torrentFlux.connect();

        return Flux.merge(fileItemFlux.then(), fileItemOccurrenceFlux.then(), trackerFlux.then())
                .then(Mono.fromRunnable(() -> log.info("finished importing everything")));
    }
}

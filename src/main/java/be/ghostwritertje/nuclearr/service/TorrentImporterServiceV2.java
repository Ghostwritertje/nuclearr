package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.fileitem.FileItem;
import be.ghostwritertje.nuclearr.fileitem.FileItemService;
import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrence;
import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrenceService;
import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrent;
import be.ghostwritertje.nuclearr.internaltorrent.TorrentClientAdapter;
import be.ghostwritertje.nuclearr.torrent.Torrent;
import be.ghostwritertje.nuclearr.torrent.TorrentService;
import be.ghostwritertje.nuclearr.tracker.Tracker;
import be.ghostwritertje.nuclearr.tracker.TrackerService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TorrentImporterServiceV2 {
    public static final Pattern TRACKER_PATTERN = Pattern.compile("\\/\\/(\\w+\\.\\w+\\.*\\w*)(\\:\\d+)*\\/");
    private final TorrentClientAdapter<?> torrentClientAdapter;
    private final TorrentService torrentService;
    private final TrackerService trackerService;
    private final FileItemService fileItemService;
    private final FileItemOccurrenceService fileItemOccurrenceService;

    private final StopWatch stopWatch;

    @Scheduled(fixedDelay = 3600, initialDelay = 2, timeUnit = TimeUnit.SECONDS)
    public void importTorrents() {
        log.info("Importing torrents");
        ConnectableFlux<? extends InternalTorrent<?>> internalTorrentFlux = torrentClientAdapter.getTorrents()
                .publish();

        ConnectableFlux<Torrent> torrentFlux = internalTorrentFlux
                .map(TorrentImporterServiceV2::mapInternalTorrent)
                .buffer(250)
                .map(torrentService::saveAll)
                .doOnEach(ignored -> log.info("saved some Torrents {}", ignored.getType()))
                .flatMap(Flux::concat)
                .publish();


        internalTorrentFlux.flatMap(internalTorrent -> Flux.fromIterable(internalTorrent.getFiles()))
                .map(InternalTorrent.InternalTorrentFile::getName)
                .map(s -> FileItem.builder().path(s).build())
                .distinct(FileItem::getPath)
                .buffer(250)
                .map(fileItemService::saveAll)
                .doOnEach(ignored -> log.info("saved some FileItems {}", ignored.getType()))
                .flatMap(Flux::concat)
                .subscribe(ignored -> log.debug("Created fileItem {}", ignored),
                        ignored -> log.error("Failed creating fileItem", ignored),
                        () -> log.info("finished importing fileItems"));

        Flux.zip(internalTorrentFlux.onBackpressureBuffer(), torrentFlux.onBackpressureBuffer())
                .map(tuple -> {
                    log.debug("tuple {} - {}", tuple.getT1().getId(), tuple.getT2().getTransmissionId());
                    return tuple;
                })
                .flatMap(tuple2 -> Flux.fromStream(tuple2.getT1().getFiles().stream().map(file -> mapFileItemOccurrence(tuple2.getT2(), file.getName()))))
                .buffer(250)
                .map(fileItemOccurrenceService::saveAll)
                .doOnEach(ignored -> log.info("saved some fileItemOccurrences {}", ignored.getType()))
                .flatMap(Flux::concat)
                .subscribe(ignored -> {
                        },
                        ignored -> log.error("Failed creating fileoccurrence", ignored),
                        () -> log.info("finished importing fileItemOccurrences")
                );

        Flux.zip(internalTorrentFlux.onBackpressureBuffer(), torrentFlux.onBackpressureBuffer())
                .flatMap(tuple -> extractTrackerList(tuple.getT1()).map(tracker -> mapTracker(tuple.getT2(), tracker)))
                .buffer(250)
                .map(trackerService::saveAll)
                .doOnEach(ignored -> log.info("saved some trackers {}", ignored.getType()))
                .flatMap(Flux::concat).subscribe(ignored -> {
                        },
                        ignored -> log.error("Failed creating trackers", ignored),
                        () -> log.info("finished importing trackers")
                );

        internalTorrentFlux.connect();
        torrentFlux.connect();
    }

    private static Flux<String> extractTrackerList(InternalTorrent<?> internalTorrent) {
        //todo this mapping should occurr in clientAdapter
        return Flux.fromStream(Arrays.stream(internalTorrent.getTrackerList().split("\n"))
                .map(tracker -> {
                    Matcher matcher = TRACKER_PATTERN.matcher(tracker);
                    if (matcher.find()) {
                        return matcher.group(1);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(StringUtils::hasText));
    }

    private static Tracker mapTracker(Torrent torrent, String s) {
        return Tracker.builder()
                .torrentId(torrent.getId())
                .name(s)
                .build();
    }

    private static FileItemOccurrence mapFileItemOccurrence(Torrent torrent, String path) {
        return FileItemOccurrence.builder()
                .fileItemPath(path)
                .torrentId(torrent.getId())
                .build();
    }

    private static FileItem mapFileItem(InternalTorrent<?> internalTorrent, InternalTorrent.InternalTorrentFile internalTorrentFile) {
        return FileItem.builder()
                .path(internalTorrent.getDownloadDir() + "/" + internalTorrentFile.getName()) //todo: path mapping should occurr in clientAdapter
                .build();
    }

    private static Torrent mapInternalTorrent(InternalTorrent<?> internalTorrent) {
        return Torrent.builder()
                .name(internalTorrent.getName())
                .hash(internalTorrent.getHashString())
                .transmissionId(internalTorrent.getId())
                .seedTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond() - internalTorrent.getAddedDate())
                .build();
    }
}

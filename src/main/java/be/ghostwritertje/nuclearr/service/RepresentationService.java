package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.domain.FileItemOccurrence;
import be.ghostwritertje.nuclearr.domain.Torrent;
import be.ghostwritertje.nuclearr.presentation.ChildTorrentDto;
import be.ghostwritertje.nuclearr.presentation.FileDto;
import be.ghostwritertje.nuclearr.presentation.MasterTorrentDto;
import be.ghostwritertje.nuclearr.presentation.TrackerDto;
import be.ghostwritertje.nuclearr.repo.FileItemOccurrenceRepository;
import be.ghostwritertje.nuclearr.repo.FileItemRepository;
import be.ghostwritertje.nuclearr.repo.TorrentRepository;
import be.ghostwritertje.nuclearr.repo.TrackerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepresentationService {

    private final TorrentRepository torrentRepository;
    private final FileItemOccurrenceRepository fileItemOccurrenceRepository;
    private final FileItemRepository fileItemRepository;
    private final TrackerRepository trackerRepository;

    public Flux<MasterTorrentDto> represent() {
        return this.torrentRepository.findAll()
                .flatMap(torrent -> {
                    Flux<FileItemOccurrence> fileItemOccurrencesByTorrentId = fileItemOccurrenceRepository.findFileItemOccurrencesByTorrentId(torrent.getId());


                    Flux<FileItemOccurrence> childFileItems = fileItemOccurrencesByTorrentId
                            .map(FileItemOccurrence::getFileItemId)
                            .collectList()
                            .map(fileIds -> this.fileItemOccurrenceRepository.findFileItemOccurrencesByFileItemIdInAndTorrentIdIsNot(fileIds, torrent.getId()))
                            .flatMapMany(Flux::concat);
                    Flux<Torrent> childTorrentFlux = childFileItems
                            .map(FileItemOccurrence::getTorrentId)
                            .flatMap(torrentRepository::getTorrentById);
                    Mono<List<ChildTorrentDto>> masterFlux = Flux.zip(childTorrentFlux, getFiles(childFileItems), getTrackers(childTorrentFlux))
                            .map(tuple2 -> (ChildTorrentDto) ChildTorrentDto.builder()
                                    .id(tuple2.getT1().getId())
                                    .name(tuple2.getT1().getName())
                                    .seedTime(tuple2.getT1().getSeedTime())
                                    .transmissionId(tuple2.getT1().getTransmissionId())
                                    .files(tuple2.getT2())
                                    .trackerList(tuple2.getT3())
                                    .build())
                            .collectList();
                    return Flux.zip(masterFlux, getFiles(fileItemOccurrencesByTorrentId), getTrackerList(torrent))
                            .map(tuple2 -> MasterTorrentDto.builder()
                                    .id(torrent.getId())
                                    .name(torrent.getName())
                                    .transmissionId(torrent.getTransmissionId())
                                    .seedTime(torrent.getSeedTime())
                                    .childTorrentDtos(tuple2.getT1())
                                    .files(tuple2.getT2())
                                    .trackerList(tuple2.getT3())
                                    .build()
                            );
                });

    }

    private Mono<List<TrackerDto>> getTrackerList(Torrent torrent) {
        return this.trackerRepository.findAllByTorrentId(torrent.getId())
                .map(tracker -> TrackerDto.builder()
                        .name(tracker.getName())
                        .build())
                .collectList();
    }

    private Mono<List<TrackerDto>> getTrackers(Flux<Torrent> torrentFlux) {
        return torrentFlux.flatMap(torrent -> this.trackerRepository.findAllByTorrentId(torrent.getId()))
                .map(tracker -> TrackerDto.builder()
                        .name(tracker.getName())
                        .build())
                .collectList();
    }
    private Mono<List<FileDto>> getFiles(Flux<FileItemOccurrence> fileItemOccurrencesByTorrentId) {
        return fileItemOccurrencesByTorrentId.flatMap(fio -> this.fileItemRepository.findById(fio.getFileItemId()))
                .map(fileItem -> FileDto.builder()
                        .id(fileItem.getId())
                        .hardlinks(fileItem.getHardlinks())
                        .path(fileItem.getPath())
                        .build())
                .collectList();
    }
}

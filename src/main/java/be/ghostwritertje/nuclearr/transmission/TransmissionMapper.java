package be.ghostwritertje.nuclearr.transmission;

import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrent;
import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrentFile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransmissionMapper {

    public InternalTorrent mapInternalTorrent(TransmissionTorrent transmissionTorrent) {
        return InternalTorrent.builder()
                .id(transmissionTorrent.getId())
                .downloadDir(transmissionTorrent.getDownloadDir())
                .files(mapInternalTorrentFiles(transmissionTorrent))
                .hashString(transmissionTorrent.getHashString())
                .name(transmissionTorrent.getName())
                .seedTime(mapSeedTime(transmissionTorrent))
                .trackerList(mapTrackers(transmissionTorrent))
                .build();
    }

    private List<String> mapTrackers(TransmissionTorrent transmissionTorrent) {
        return Arrays.stream(transmissionTorrent.getTrackerList().split("\n"))
                .filter(StringUtils::hasText)
                .toList();
    }

    private long mapSeedTime(TransmissionTorrent transmissionTorrent) {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond() - transmissionTorrent.getAddedDate();
    }

    private List<InternalTorrentFile> mapInternalTorrentFiles(TransmissionTorrent transmissionTorrent) {
        return transmissionTorrent.getFiles()
                .stream()
                .map(transmissionFile -> mapInternalTorrentFile(transmissionFile, transmissionTorrent.getDownloadDir()))
                .collect(Collectors.toList());
    }

    private InternalTorrentFile mapInternalTorrentFile(TransmissionTorrent.TransmissionFile transmissionFile, String downloadDir) {
        return InternalTorrentFile.builder()
                .bytesCompleted(transmissionFile.getBytesCompleted())
                .length(transmissionFile.getLength())
                .name(Paths.get(downloadDir, transmissionFile.getName()).toString())
                .build();
    }
}

package be.ghostwritertje.nuclearr.transmission;

import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransmissionMapperTest {

    private final TransmissionMapper mapper = new TransmissionMapper();
    @Test
    void mapInternalTorrent_filename() {

        TransmissionTorrent.TransmissionFile file = TransmissionTorrent.TransmissionFile.builder()
                .bytesCompleted(500L)
                .name("filename.jpg")
                .length(9000L)
                .build();

        TransmissionTorrent transmissionTorrent = TransmissionTorrent.builder()
                .id(12)
                .downloadDir("/downloads")
                .files(List.of(file))
                .hashString("hash")
                .name("name")
                .addedDate(100L)
                .trackerList("google.com")
                .build();


        InternalTorrent result = mapper.mapInternalTorrent(transmissionTorrent);

        assertEquals("/downloads/filename.jpg", result.getFiles().get(0).getName());
    }
    @Test
    void mapInternalTorrent_seedTime() {

        TransmissionTorrent transmissionTorrent = TransmissionTorrent.builder()
                .addedDate(LocalDateTime.now().minusDays(15).atZone(ZoneId.systemDefault()).toEpochSecond())
                .files(List.of())
                .build();


        InternalTorrent result = mapper.mapInternalTorrent(transmissionTorrent);

        assertEquals(Duration.ofDays(15).toSeconds(), result.getSeedTime());
    }
    @Test
    void mapInternalTorrent() {
        TransmissionTorrent.TransmissionFile file = TransmissionTorrent.TransmissionFile.builder()
                .bytesCompleted(500L)
                .name("filename.jpg")
                .length(9000L)
                .build();

        TransmissionTorrent transmissionTorrent = TransmissionTorrent.builder()
                .id(12)
                .downloadDir("/downloads")
                .files(List.of(file))
                .hashString("hash")
                .addedDate(1790L)
                .name("name")
                .trackerList("google.com")
                .build();


        InternalTorrent result = mapper.mapInternalTorrent(transmissionTorrent);
        assertEquals(12, result.getId());
        assertEquals("/downloads", result.getDownloadDir());
        assertEquals("hash", result.getHashString());
        assertEquals("name", result.getName());
        assertEquals("google.com", result.getTrackerList());
    }
}
package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.fileitem.FileItem;
import be.ghostwritertje.nuclearr.hardlinks.HardlinkFinder;
import be.ghostwritertje.nuclearr.internaltorrent.TorrentClientAdapter;
import be.ghostwritertje.nuclearr.removed.RemovedService;
import be.ghostwritertje.nuclearr.torrent.TorrentService;
import be.ghostwritertje.nuclearr.transmission.TransmissionResponse;
import be.ghostwritertje.nuclearr.transmission.TransmissionTorrent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

@SuppressWarnings("ReactiveStreamsUnusedPublisher")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureWireMock(port = 9999)
class NuclearrITTest {

    public static final String DOWNLOAD_DIR = "/downloads/nuclearr";
    public static final String TRACKER = "http://github.cc/announce/sofjsidf";
    @Autowired
    private RemovalJob removalJob;

    @Autowired
    private TorrentClientAdapter torrentClientAdapter;

    @MockBean
    private HardlinkFinder hardlinkFinder;

    @Autowired
    private RemovedService removedService;
    @Autowired
    private TorrentService torrentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static Stream<Arguments> testRemovedTorrent_parameterized() {
        return Stream.of(
                Arguments.of("torrent : older than 30 days | configured tracker | no hardlinks : should be removed", LocalDateTime.now().minusDays(31), 1, "http://github.cc/announce/sofjsidf", true),
                Arguments.of("torrent : older than 30 days | configured tracker | no hardlinks : should be removed", LocalDateTime.now().minusDays(31), 1, "http://github.cc/announce/sofjsidf", true),
                Arguments.of("torrent : YOUNGER than 30 days | configured tracker | no hardlinks : should NOT be removed", LocalDateTime.now().minusDays(29), 1, "http://github.cc/announce/sofjsidf", false),
                Arguments.of("torrent : older than 30 days | configured tracker | 2 hardlinks : should NOT be removed", LocalDateTime.now().minusDays(31), 2, "http://github.cc/announce/sofjsidf", false),
                Arguments.of("torrent : older than 30 days | UNconfigured tracker | no hardlinks : should NOT be removed", LocalDateTime.now().minusDays(31), 1, "http://amazon.cc/announce/sofjsidf", false)
        );
    }

    @BeforeEach
    public void setup() {
        StepVerifier.create(this.torrentService.deleteAll())
                .verifyComplete();
        StepVerifier.create(this.removedService.deleteAll())
                .verifyComplete();
    }

    @Test
    public void contextLoads() {
        stubFor(WireMock.post("/transmission/rpc").willReturn(ok()
                .withHeader("Content-Type", "application/json; charset=UTF-8")
                .withBody("{ \"arguments\": { \"torrents\": [] }}")));


        StepVerifier.create(torrentClientAdapter.getTorrents())
                .verifyComplete();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    public void testRemovedTorrent_parameterized(String name, LocalDateTime dateAdded, int hardlinks, String tracker, boolean removed) throws JsonProcessingException {
        stubFor(WireMock.post("/transmission/rpc").willReturn(ok()
                .withHeader("Content-Type", "application/json; charset=UTF-8")
                .withBody(this.createResponse(transmissionTorrent(dateAdded, tracker, DOWNLOAD_DIR, "filename.mkv")))));

        Mockito.when(this.hardlinkFinder.findHardLinks(Mockito.any()))
                .thenAnswer(invocationOnMock -> {
                    FileItem fileItem = (FileItem) invocationOnMock.getArguments()[0];
                    return Mono.just(fileItem.toBuilder()
                            .hardlinks(hardlinks)
                            .build());
                });


        StepVerifier.create(removalJob.removeTorrentFlux())
                .verifyComplete();

        StepVerifier.create(this.removedService.findAll())
                .expectNextCount(removed ? 1 : 0)
                .verifyComplete();
    }

    @Test
    @DisplayName("a torrent should not be removed if it has cross-seeds that cannot be removed")
    public void testCrossSeed() throws JsonProcessingException {
        TransmissionTorrent torrentThatCanBeRemoved = transmissionTorrent(LocalDateTime.now().minusDays(31), TRACKER, DOWNLOAD_DIR, "filename1.mkv", "filename2.mkv");
        TransmissionTorrent torrentThatCanNotBeRemoved = transmissionTorrent(LocalDateTime.now().minusDays(31), "http://amazon.cc/announce/sofjsidf", DOWNLOAD_DIR, "filename1.mkv");

        stubFor(WireMock.post("/transmission/rpc").willReturn(ok()
                .withHeader("Content-Type", "application/json; charset=UTF-8")
                .withBody(this.createResponse(torrentThatCanBeRemoved, torrentThatCanNotBeRemoved))));

        Mockito.when(this.hardlinkFinder.findHardLinks(Mockito.any()))
                .thenAnswer(invocationOnMock -> {
                    FileItem fileItem = (FileItem) invocationOnMock.getArguments()[0];
                    return Mono.just(fileItem.toBuilder()
                            .hardlinks(1)
                            .build());
                });


        StepVerifier.create(removalJob.removeTorrentFlux())
                .verifyComplete();

        StepVerifier.create(this.removedService.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    private byte[] createResponse(TransmissionTorrent... transmissionTorrent) throws JsonProcessingException {
        var transmissionResponse = TransmissionResponse.builder()
                .arguments(TransmissionResponse.InnerResponse.builder()
                        .torrents(List.of(transmissionTorrent))
                        .build())
                .build();
        return this.objectMapper.writeValueAsBytes(transmissionResponse);
    }

    private static TransmissionTorrent transmissionTorrent(LocalDateTime addedDate, String tracker, String downloadDir, String... filenames) {
        return TransmissionTorrent.builder()
                .id(15)
                .downloadDir(downloadDir)
                .hashString(UUID.randomUUID().toString())
                .addedDate(addedDate.atZone(ZoneId.systemDefault()).toEpochSecond())
                .name("test-name.mkv")
                .trackerList(tracker)
                .files(Arrays.stream(filenames).map(filename -> TransmissionTorrent.TransmissionFile.builder()
                                .name(filename)
                                .build())
                        .toList())
                .build();
    }

}
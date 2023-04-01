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
                Arguments.of(LocalDateTime.now().minusDays(31), 1, "http://github.cc/announce/sofjsidf", true),
                Arguments.of(LocalDateTime.now().minusDays(31), 1, "http://github.cc/announce/sofjsidf", true),
                Arguments.of(LocalDateTime.now().minusDays(29), 1, "http://github.cc/announce/sofjsidf", false),
                Arguments.of(LocalDateTime.now().minusDays(31), 2, "http://github.cc/announce/sofjsidf", false),
                Arguments.of(LocalDateTime.now().minusDays(31), 1, "http://amazon.cc/announce/sofjsidf", false)
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

    @ParameterizedTest
    @MethodSource
    public void testRemovedTorrent_parameterized(LocalDateTime dateAdded, int hardlinks, String tracker, boolean removed) throws JsonProcessingException {
        stubFor(WireMock.post("/transmission/rpc").willReturn(ok()
                .withHeader("Content-Type", "application/json; charset=UTF-8")
                .withBody(this.createResponse(dateAdded, tracker))));

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

    private byte[] createResponse(LocalDateTime addedDate, String... trackers) throws JsonProcessingException {
        var transmissionResponse = TransmissionResponse.builder()
                .arguments(TransmissionResponse.InnerResponse.builder()
                        .torrents(List.of(TransmissionTorrent.builder()
                                .id(15)
                                .downloadDir("/downloads/nuclearr")
                                .hashString(UUID.randomUUID().toString())
                                .addedDate(addedDate.atZone(ZoneId.systemDefault()).toEpochSecond())
                                .name("test-name.mkv")
                                .trackerList(String.join("\n", trackers))
                                .files(List.of(TransmissionTorrent.TransmissionFile.builder()
                                        .name("test-file.mkv")
                                        .build()))
                                .build()))
                        .build())
                .build();
        return this.objectMapper.writeValueAsBytes(transmissionResponse);
    }

}
package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.fileitem.FileItem;
import be.ghostwritertje.nuclearr.hardlinks.HardlinkFinder;
import be.ghostwritertje.nuclearr.internaltorrent.TorrentClientAdapter;
import be.ghostwritertje.nuclearr.removed.RemovedService;
import be.ghostwritertje.nuclearr.torrent.TorrentService;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @Test
    public void testRemovedTorrent() {
        stubFor(WireMock.post("/transmission/rpc").willReturn(ok()
                .withHeader("Content-Type", "application/json; charset=UTF-8")
                .withBody("{\n" +
                        "  \"arguments\": {\n" +
                        "    \"torrents\": [\n" +
                        "      {\n" +
                        "        \"activityDate\": 1678989177,\n" +
                        "        \"addedDate\": 1658986238,\n" +
                        "        \"files\": [\n" +
                        "          {\n" +
                        "            \"bytesCompleted\": 2679554722,\n" +
                        "            \"length\": 2679554722,\n" +
                        "            \"name\": \"True.Lies.S01E03.Separate.Pairs.1080p.AMZN.WEB-DL.DDP5.1.H.264-NTb.mkv\"\n" +
                        "          }\n" +
                        "        ],\n" +
                        "        \"id\": 567,\n" +
                        "        \"downloadDir\": \"/downloads/sonarr\",\n" +
                        "        \"hashString\": \"000ab75485580a2e6728a9a37ec40c01307c21e0\",\n" +
                        "        \"name\": \"True.Lies.S01E03.Separate.Pairs.1080p.AMZN.WEB-DL.DDP5.1.H.264-NTb.mkv\",\n" +
                        "        \"secondsSeeding\": 236354,\n" +
                        "        \"startDate\": 1680110055,\n" +
                        "        \"trackerList\": \"https://github.cc/announce/123THISISATEST\\n\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}")));

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
                .expectNextCount(1L)
                .verifyComplete();
    }

}
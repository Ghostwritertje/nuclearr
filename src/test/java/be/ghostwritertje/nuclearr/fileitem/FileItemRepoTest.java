package be.ghostwritertje.nuclearr.fileitem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.stream.IntStream;

@ActiveProfiles("test")
@SpringBootTest
class FileItemRepoTest {

    @Autowired
    private FileItemRepo fileItemRepo;

    @Autowired
    private FileItemRepository fileItemRepository;

    @BeforeEach
    public void cleanup() {
        StepVerifier.create(fileItemRepository.deleteAll()).verifyComplete();
    }


    @Test
    @Timeout(1)
    public void saveAll() {
        Flux<FileItem> result = Flux.fromStream(IntStream.range(1, 1001).boxed())
                .map(i -> FileItem.builder()
                        .hardlinks(null)
                        .path("path" + i)
                        .build())
                .buffer(150)
                .flatMap(list -> fileItemRepo.saveAll(list));

        StepVerifier.create(result)
                .expectNextCount(1000)
                .verifyComplete();
    }
}
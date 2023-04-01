package be.ghostwritertje.nuclearr.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackerExtractorTest {

    private final TrackerExtractor extractor = new TrackerExtractor();

    @ParameterizedTest(name = "test tracker {1}")
    @CsvSource(value = {
            "https://amazon.club/tracker.php/876786dsfsdf/announce;amazon.club",
            "https://subdomain.amazon.club/tracker.php/876786dsfsdf/announce;subdomain.amazon.club",
            "http://tracker.github.com:4000/786as98d76fs876f9sd6f/announce;tracker.github.com",
            "https://google.club:443/tracker.php/87987987df897f9/announce;google.club"},
            delimiter = ';')
    public void testTracker(String source, String expected) {
        String result = this.extractor.extract(source);
        assertEquals(expected, result);
    }

}
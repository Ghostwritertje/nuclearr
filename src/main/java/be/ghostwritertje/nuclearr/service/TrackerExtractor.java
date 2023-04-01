package be.ghostwritertje.nuclearr.service;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TrackerExtractor {
    public static final Pattern TRACKER_PATTERN = Pattern.compile("/([^/:]+)+(:\\d+)?/");

    public String extract(String trackerString) {
        String result = null;
        Matcher matcher = TRACKER_PATTERN.matcher(trackerString);
        if (matcher.find()) {
            result = matcher.group(1);
        }

        return result != null ? result : "ERROR";
    }
}

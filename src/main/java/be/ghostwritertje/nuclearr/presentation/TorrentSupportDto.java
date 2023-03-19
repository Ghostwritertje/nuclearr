package be.ghostwritertje.nuclearr.presentation;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Data
@SuperBuilder
public abstract class TorrentSupportDto {
    private Integer id;
    private Integer transmissionId;
    private String name;
    private Long seedTime;
    @Builder.Default
    private List<FileDto> files = new ArrayList<>();
    @Builder.Default
    private List<TrackerDto> trackerList = new ArrayList<>();


    public int getMaxHardLinks() {
        return this.getFiles().stream().map(FileDto::getHardlinks)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }
}

package be.ghostwritertje.nuclearr.presentation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class MasterTorrentDto extends TorrentSupportDto {


    @Builder.Default
    private List<ChildTorrentDto> childTorrentDtos = new ArrayList<>();


//    public getMinimumSeedTime

    public int getMaxHardLinks() {
        return Stream.concat(this.getFiles().stream().map(FileDto::getHardlinks), this.childTorrentDtos.stream()
                        .map(ChildTorrentDto::getFiles)
                        .flatMap(Collection::stream)
                        .map(FileDto::getHardlinks))
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    public Long getLowestSeedTime() {
        return Stream.concat(Stream.of(this.getSeedTime()), this.childTorrentDtos.stream()
                        .map(TorrentSupportDto::getSeedTime)
                        .filter(Objects::nonNull))
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);
    }

    public List<String> getAllTrackers() {
        return Stream.concat(this.getTrackerList().stream(), this.childTorrentDtos.stream().map(TorrentSupportDto::getTrackerList).flatMap(Collection::stream))
                .map(TrackerDto::getName)
                .distinct()
                .collect(Collectors.toList());
    }

}

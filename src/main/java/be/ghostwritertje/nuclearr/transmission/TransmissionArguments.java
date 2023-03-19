package be.ghostwritertje.nuclearr.transmission;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@Data
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransmissionArguments {

    public static final List<String> ALL_FIELDS = Arrays.asList("id", "trackerList", "name", "downloadDir", "torrentFile", "startDate", "addedDate", "files", "hashString");
    public static final List<String> SIMPLE_FIELDS = Arrays.asList("id", "name", "hashString", "addedDate");


    @Builder.Default
    @With
    @JsonInclude(value= JsonInclude.Include.NON_NULL, content = JsonInclude.Include.NON_EMPTY)
    private final List<String> fields = SIMPLE_FIELDS;

    @JsonInclude(value= JsonInclude.Include.NON_NULL, content = JsonInclude.Include.NON_EMPTY)
    private List<Integer> ids;

    @JsonInclude(value= JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "delete-local-data")
    private Boolean deleteLocalData;
}

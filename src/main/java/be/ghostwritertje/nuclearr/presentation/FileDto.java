package be.ghostwritertje.nuclearr.presentation;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Data
@Builder
public class FileDto {

    Integer id;

    String path;
    private Integer hardlinks;

    public Integer getHardlinks() {
        return this.hardlinks != null ? this.hardlinks : 99;
    }
}

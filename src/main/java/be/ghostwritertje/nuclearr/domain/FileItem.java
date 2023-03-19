package be.ghostwritertje.nuclearr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_item")
public class FileItem {

    @Id
    @Column("id")
    Integer id;

    @Column("path")
    String path;

    @Column("hard_links")
    Integer hardlinks;

}

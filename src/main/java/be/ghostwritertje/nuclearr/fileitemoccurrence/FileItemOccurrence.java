package be.ghostwritertje.nuclearr.fileitemoccurrence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_item_occurrence")
@Builder
public class FileItemOccurrence {

    @Id
    @Column("id")
    Integer id;

    @Column("file_item_path")
    String fileItemPath;

    @Column("torrent_id")
    Integer torrentId;
}

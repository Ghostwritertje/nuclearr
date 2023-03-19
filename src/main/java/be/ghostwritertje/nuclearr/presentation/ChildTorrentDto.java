package be.ghostwritertje.nuclearr.presentation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ChildTorrentDto extends TorrentSupportDto{

}

package project.server.entities.train;

import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class TrainTimeEntity {
    private int trainNo;
    private Integer trainStationIndex;
    private String departAt;
}

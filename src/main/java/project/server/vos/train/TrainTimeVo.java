package project.server.vos.train;

import lombok.Data;
import project.server.entities.train.TrainTimeEntity;

@Data
public class TrainTimeVo extends TrainTimeEntity {
    private String stationName;
}

package project.server.vos.train;

import lombok.Data;
import project.server.entities.train.TrainChargeEntity;

@Data
public class TrainChargeVo extends TrainChargeEntity {
    private String departName;
    private String arriveName;
}

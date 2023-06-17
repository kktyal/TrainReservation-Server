package project.server.entities.train;

import lombok.Data;

@Data
public class TrainChargeEntity {
    private Integer depart;
    private Integer arrive;
    private Integer charge;
    private Integer chargeVip;
}

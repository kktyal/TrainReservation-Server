package project.server.entities.train;

import lombok.Data;

@Data
public class TrainChargeEntity {
    private int depart;
    private int arrive;
    private Integer charge;
    private Integer chargeVip;
}

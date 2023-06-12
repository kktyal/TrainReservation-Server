package project.server.vos.train;

import lombok.Data;
import project.server.entities.train.ReservationEntity;

@Data
public class ReservationVo extends ReservationEntity {
    private int trainNo;
    private int carriage;
    private String seat;

}

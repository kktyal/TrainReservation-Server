package project.server.vos.train;

import lombok.Data;
import project.server.entities.train.ReservationEntity;

@Data
public class CntVo extends ReservationEntity {
    private int adult;
    private int kid;
    private int old;
}


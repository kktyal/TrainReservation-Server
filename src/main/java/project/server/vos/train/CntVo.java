package project.server.vos.train;

import lombok.Data;
import project.server.entities.train.ReservationEntity;

@Data
public class CntVo extends ReservationEntity {
    private Integer adult;
    private Integer child;
    private Integer old;

    public Integer sum(){
        return adult+ child +old;
    }
}


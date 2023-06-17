package project.server.vos.train;

import lombok.Data;
import project.server.entities.train.ReservationEntity;

@Data
public class CntVo extends ReservationEntity {
    private Integer adult;
    private Integer kid;
    private Integer old;

    public Integer sum(){
        return adult+kid+old;
    }
}


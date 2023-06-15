package project.server.vos.train;

import lombok.Data;
import lombok.EqualsAndHashCode;
import project.server.entities.train.ReservationEntity;

@Data
//@EqualsAndHashCode(callSuper = true)
public class ReservationVo extends ReservationEntity {
    private Integer trainNo;
    private Integer carriage;
    private String seat;
    private String age;
    private Integer price;
    private Integer ticketCnt;
    private String ticketId;

}

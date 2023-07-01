package project.server.vos.train;

import lombok.Data;
import lombok.EqualsAndHashCode;
import project.server.entities.train.ReservationEntity;


//@EqualsAndHashCode(callSuper = true)
@Data
public class ReservationVo extends ReservationEntity {
    private Integer trainNo;
    private Integer carriage;
    private String seat;
    private String age;
    private Integer price;
    private Integer discountedPrice;
    private Integer ticketCnt;
    private String ticketId;

}

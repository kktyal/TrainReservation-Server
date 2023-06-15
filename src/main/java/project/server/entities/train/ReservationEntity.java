package project.server.entities.train;

import lombok.Data;

import java.util.Date;

@Data
public class ReservationEntity {
    private String reservationId;
    private String departTime;
    private String departStation;
    private String arriveTime;
    private String arriveStation;
    private String date;
    private Date expiredDate;
    private Integer memberId;
    private Boolean disuse;
    private String paymentId;
}

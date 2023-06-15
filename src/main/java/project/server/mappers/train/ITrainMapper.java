package project.server.mappers.train;

import org.apache.ibatis.annotations.Mapper;
import project.server.entities.train.*;
import project.server.vos.train.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ITrainMapper {
    int test();
    Optional<TrainStationEntity>findByName(String name);

    List<ReservationVo> findSoldSeat(ApiVo apiVo);


    int updateDisuse(Date nowDate);
    int updateReservationPaymentId(ReservationEntity entity);


    List<TrainTimeVo> selectTime(int trainNo);
    Optional<TrainChargeEntity> selectCharge(TrainChargeVo trainChargeVo);

    List<ReservationVo>duplicatedTicket(ReservationVo vo);

    Optional<ReservationEntity> selectReservationId(String id);
    Optional<TicketEntity> selectTicketId(String id);
    Optional<PaymentEntity> selectPaymentId(String id);

    List<ReservationVo> showReservation(String reservationId);
    List<ReservationVo> selectReservationByMemberId (Integer memberId);

    int saveReservation(ReservationEntity reservation);
    int saveTicket(TicketEntity ticket);
    int savePayment(PaymentEntity payment);

    List<TicketEntity> selectTicketByReservationId(String reservationId);

    int updateRefundToPayment(String paymentId);

    int updateCancelToReservation(String reservationId);

}

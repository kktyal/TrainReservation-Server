package project.server.mappers.train;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import project.server.entities.train.*;
import project.server.vos.train.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ITrainMapper {
    int test();
    Optional<TrainStationEntity>findByName(String name);
    Optional<TrainChargeEntity> selectCharge(TrainChargeVo trainChargeVo);
    Optional<ReservationEntity> selectReservationId(String id);
    Optional<TicketEntity> selectTicketId(String id);
    Optional<PaymentEntity> selectPaymentId(String id);

    List<ReservationVo> findSoldSeat(ApiVo apiVo);
    List<TrainTimeVo> selectTime(int trainNo);
    List<ReservationVo>duplicatedTicket(ReservationVo vo);
    List<ReservationVo> showReservation(String reservationId);
    List<ReservationVo> selectReservationByMemberId (Integer memberId);
    List<TicketEntity> selectTicketByReservationId(String reservationId);
    List<String> selectStandardSeats();
    List<String> selectPremiumSeats();


    @Select("select `station_name` from `train_station`")
    List<String> selectTrainStations();
    @Select("select `train_no` from `train_no`")
    List<Integer> selectTrainNos();

    int saveReservation(ReservationEntity reservation);
    int saveTicket(TicketEntity ticket);
    int savePayment(PaymentEntity payment);
    int updateDisuse(Date nowDate);
    int updateReservationPaymentId(ReservationEntity entity);
    int updateRefundToPayment(String paymentId);
    int updateCancelToReservation(String reservationId);

}

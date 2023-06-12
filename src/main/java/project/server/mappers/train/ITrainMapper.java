package project.server.mappers.train;

import org.apache.ibatis.annotations.Mapper;
import project.server.entities.train.ReservationEntity;
import project.server.entities.train.TicketEntity;
import project.server.entities.train.TrainChargeEntity;
import project.server.entities.train.TrainStationEntity;
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


    List<TrainTimeVo> selectTime(int trainNo);

    Optional<TrainChargeEntity> selectCharge(TrainChargeVo trainChargeVo);

    Optional<ReservationEntity> duplicatedMemberId(String id);
    Optional<ReservationEntity> duplicatedTicketId(String id);
    int saveReservation(ReservationEntity reservation);
    int saveTicket(TicketEntity ticket);
}

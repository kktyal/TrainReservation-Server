package project.server.entities.train;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
public class TicketEntity {

    private String ticketId;
    private Integer trainNo;
    private Integer carriage;
    private String seat;
    private String reservationId;
    private String age;
    private Integer price;
}

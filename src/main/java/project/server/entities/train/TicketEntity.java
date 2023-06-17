package project.server.entities.train;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
public class TicketEntity {

    private String ticketId;
    private int trainNo;
    private int carriage;
    private String seat;
    private String reservationId;
    private String age;
    private int price;
}

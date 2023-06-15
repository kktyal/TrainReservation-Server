package project.server.entities.train;

import lombok.Data;

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

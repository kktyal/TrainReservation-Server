package project.server.entities.train;

import lombok.Data;

@Data
public class TicketEntity {

    private String ticketId;
    private Integer trainNo;
    private Integer carriage;
    private String seat;
    private String reservationId;
    private String age;
    private Integer price;
    private Integer discountedPrice;
}

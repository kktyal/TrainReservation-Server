package project.server.entities.train;

import lombok.Data;

@Data
public class PaymentEntity {
    private String paymentId;
    private int reservationId;
    private int totalPrice;
    private boolean refund;
}

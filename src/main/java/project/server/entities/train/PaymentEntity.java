package project.server.entities.train;

import lombok.Data;

@Data
public class PaymentEntity {
    private String paymentId;
    private int totalPrice;
    private Boolean refund;
}

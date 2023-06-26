package project.server.entities.train;

import lombok.Data;

import java.util.Date;

@Data
public class PaymentEntity {
    private String paymentId;
    private int totalPrice;
    private Boolean refund;
    private Date createdDate;
}

package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponse extends  BaseModel{
    private String message;
    private long senderAccountId;
    private long receiverAccountId;
    private double amount;

}

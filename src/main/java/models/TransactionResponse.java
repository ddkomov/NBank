package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse extends BaseModel {
    private long id;
    private String type;
    private String timestamp;
    private double amount;
    private long relatedAccountId;
}

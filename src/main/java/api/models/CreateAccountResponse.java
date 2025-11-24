package api.models;

import api.requests.skeleton.interfaces.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAccountResponse extends BaseModel implements Identifiable {

    private long id;
    private String accountNumber;
    private double balance;
    private List<TransactionResponse> transactions;


}

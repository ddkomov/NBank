package utils;

import models.AccountResponse;
import java.util.List;
import java.util.Optional;

public class AccountUtils {

    public static Optional<AccountResponse> getById(List<AccountResponse> accounts, long id) {
        return accounts.stream()
                .filter(account -> account.getId() == id)
                .findFirst();
    }
}
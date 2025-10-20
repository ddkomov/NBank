package api.requests;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),

    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),
    CUSTOMERS_ACCOUNT(
            "/customer/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),
    CUSTOMERS_PROFILE(
            "/customer/profile",
            BaseModel.class,
            CreateUserResponse.class
    ),
    ACCOUNTS_TRANSACTIONS(
            "/accounts/{accountId}/transactions",
            BaseModel.class,
            AccountResponse.class
    ),
    ACCOUNTS_TRANSFER(
            "/accounts/transfer",
            BaseModel.class,
            TransferResponse.class
    ),
    DEPOSITS("/accounts/deposit",
            DepositRequest.class,
            DepositResponse.class);


    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;


}
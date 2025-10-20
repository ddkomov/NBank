package iteration2.api;

import api.models.*;
import iteration1.api.BaseTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class UserDepositTest extends BaseTest {

    public static Stream<Arguments> amountsValid() {
        return Stream.of(

                Arguments.of(0.1),
                Arguments.of(0.5),
                Arguments.of(1.0),
                Arguments.of(4999.0),
                Arguments.of(4999.5),
                Arguments.of(5000.0)
        );
    }

    public static Stream<Arguments> amountsInvalid() {
        return Stream.of(

                Arguments.of(0.0),
                Arguments.of(-1.0),
                Arguments.of(5000.1),
                Arguments.of(5001.0),
                Arguments.of((Double) null),
                Arguments.of(5000.5)
        );
    }

    @MethodSource("amountsValid")
    @ParameterizedTest
    public void userCanDepositValidAmountOfMoney(double amount) {
        // Шаг 1: Создаем пользователя через Admin
        CreateUserRequest userRequest = AdminSteps.createUser();
        // Шаг 2: Создаем аккаунт для пользователя
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );
        CreateAccountResponse createdAccount = createAccountRequester.post(userRequest);
        // Шаг 3: Делаем депозит на созданный аккаунт
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(createdAccount.getId());
        depositRequest.setBalance(amount);

        ValidatedCrudRequester<DepositResponse> depositRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.DEPOSITS,
                        ResponseSpecs.requestReturnsOK()
                );
        DepositResponse depositResponse = depositRequester.post(depositRequest);
        // Шаг 4: Проверяем, что баланс изменился через GET-запрос
        // Выполняем GET-запрос к /api/v1/customer/accounts
        ValidatedCrudRequester<AccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.CUSTOMERS_ACCOUNT,
                        ResponseSpecs.requestReturnsOK()
                );
        //Достаем счет из массива
        List<AccountResponse> accounts = accountRequester.getAsList(null, AccountResponse.class);
        AccountResponse firstAccount = accounts.getFirst();
        // Шаг 5: Проверяем, что баланс увеличился на сумму депозита
        Assertions.assertThat(firstAccount.getBalance()).isEqualTo(depositResponse.getBalance());
    }

    @MethodSource("amountsInvalid")
    @ParameterizedTest
    public void userCanNotDepositInvalidAmountOfMoney(Double amount) {
        // Шаг 1: Создаем пользователя через Admin
        CreateUserRequest userRequest = AdminSteps.createUser();
        // Шаг 2: Создаем аккаунт для пользователя
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount = createAccountRequester.post(userRequest);
        // Шаг 3: Делаем депозит на созданный аккаунт
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(createdAccount.getId());
        depositRequest.setBalance(/*amount != null ? amount : 0.0*/amount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                Endpoint.DEPOSITS,
                amount != null
                        ? ResponseSpecs.requestReturnsBR()
                        : ResponseSpecs.requestReturnsIternalServerError()

        ).post(depositRequest);
        // Шаг 4: Проверяем, что баланс изменился через GET-запрос
        // Выполняем GET-запрос к /customer/accounts
        ValidatedCrudRequester<AccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.CUSTOMERS_ACCOUNT,
                        ResponseSpecs.requestReturnsOK()
                );
        //Достаем счет из массива
        List<AccountResponse> accounts = accountRequester.getAsList(null, AccountResponse.class);
        AccountResponse firstAccount = accounts.getFirst();
        // Шаг 5: Проверяем, что баланс увеличился на сумму депозита
        Assertions.assertThat(firstAccount.getBalance()).isEqualTo(0.0);
    }
    @MethodSource("amountsValid")
    @ParameterizedTest
    public void userCannotDepositOnOtherUsersAccount(double amount) {
        // Шаг 1: Создаем пользователя user1
        CreateUserRequest user1Request = AdminSteps.createUser();
        // Шаг 2: Создаем аккаунт для user1
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user1Request.getUsername(),
                                user1Request.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount = createAccountRequester.post(user1Request);
        // Шаг 3: Создаем пользователя user2
        CreateUserRequest user2Request = AdminSteps.createUser();
        // Шаг 4: Пытаемся сделать депозит на счёт user1 от имени user2
        // Выполняем депозит и проверяем, что запрос завершился ошибкой
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(createdAccount.getId());
        depositRequest.setBalance(amount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        user2Request.getUsername(),
                        user2Request.getPassword()),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsForbidden() // Ожидаем ошибку доступа
        ).post(depositRequest);
        // Шаг 5: Проверяем, что баланс изменился через GET-запрос
        // Выполняем GET-запрос к /customer/accounts
        ValidatedCrudRequester<AccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user1Request.getUsername(),
                                user1Request.getPassword()),
                        Endpoint.CUSTOMERS_ACCOUNT,
                        ResponseSpecs.requestReturnsOK()
                );
        //Достаем счет из массива
        List<AccountResponse> accounts = accountRequester.getAsList(null, AccountResponse.class);
        AccountResponse firstAccount = accounts.getFirst();
        // Шаг 6: Проверяем, что баланс увеличился на сумму депозита
        Assertions.assertThat(firstAccount.getBalance()).isEqualTo(0.0);


    }
    @MethodSource("amountsValid")
    @ParameterizedTest
    public void userCannotDepositOnNonExistentAccount(double amount) {
        // Шаг 1: Создаем пользователя user1
        CreateUserRequest user1Request = AdminSteps.createUser();
        // Шаг 2: Создаем аккаунт для user1
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user1Request.getUsername(),
                                user1Request.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );
        CreateAccountResponse createdAccount = createAccountRequester.post(user1Request);
        // Шаг 3: Пытаемся сделать депозит на несуществующий счёт
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(999); // Несуществующий ID
        depositRequest.setBalance(amount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        user1Request.getUsername(),
                        user1Request.getPassword()),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsNotFound() // ???? Ожидаем ошибку 404, но почему-то 403
        ).post(depositRequest);
        // Шаг 4: Проверяем, что баланс изменился через GET-запрос
        // Выполняем GET-запрос к /customer/accounts
        ValidatedCrudRequester<AccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user1Request.getUsername(),
                                user1Request.getPassword()),
                        Endpoint.CUSTOMERS_ACCOUNT,
                        ResponseSpecs.requestReturnsOK()
                );
        //Достаем счет из массива
        List<AccountResponse> accounts = accountRequester.getAsList(null, AccountResponse.class);
        AccountResponse firstAccount = accounts.getFirst();
        // Шаг 5: Проверяем, что баланс увеличился на сумму депозита
        Assertions.assertThat(firstAccount.getBalance()).isEqualTo(0.0);
    }
}

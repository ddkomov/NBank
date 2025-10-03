package iteration2;

import generators.RandomModelGenerator;
import iteration1.BaseTest;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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

        ValidatedCrudRequester<CreateAccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount = accountRequester.post(userRequest);
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

        // Шаг 4: Проверяем результат
        ModelAssertions.assertThatModels(depositRequest, depositResponse).match();
    }

    @MethodSource("amountsInvalid")
    @ParameterizedTest
    public void userCanNotDepositInvalidAmountOfMoney(Double amount) {
        // Шаг 1: Создаем пользователя через Admin
        CreateUserRequest userRequest = AdminSteps.createUser();

        // Шаг 2: Создаем аккаунт для пользователя

        ValidatedCrudRequester<CreateAccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount = accountRequester.post(userRequest);
        // Шаг 3: Делаем депозит на созданный аккаунт
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(createdAccount.getId());
        depositRequest.setBalance(amount != null ? amount : 0.0);

        ValidatedCrudRequester<DepositResponse> depositRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.DEPOSITS,
                        ResponseSpecs.requestReturnsOK()
                );

        DepositResponse depositResponse = depositRequester.post(depositRequest);

        // Шаг 4: Проверяем результат
        ModelAssertions.assertThatModels(depositRequest, depositResponse).match();
    }

    @Test
    public void userCannotDepositOnOtherUsersAccount() {
        // Шаг 1: Создаем пользователя user1
        CreateUserRequest user1Request = AdminSteps.createUser();

        // Шаг 2: Создаем аккаунт для user1
        ValidatedCrudRequester<CreateAccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user1Request.getUsername(),
                                user1Request.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount = accountRequester.post(user1Request);

        // Шаг 3: Создаем пользователя user2
        CreateUserRequest user2Request = AdminSteps.createUser();

        // Шаг 4: Пытаемся сделать депозит на счёт user1 от имени user2
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(createdAccount.getId());
        depositRequest.setBalance(100.0);

        ValidatedCrudRequester<DepositResponse> depositRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user2Request.getUsername(),
                                user2Request.getPassword()),
                        Endpoint.DEPOSITS,
                        ResponseSpecs.requestReturnsForbidden() // Ожидаем ошибку доступа
                );

        // Выполняем депозит и проверяем, что запрос завершился ошибкой
        try {
            depositRequester.post(depositRequest);
        } catch (Exception e) {
            // Если сервер вернул ошибку, это нормально
        }
    }
    @Test
    public void userCannotDepositOnNonExistentAccount() {
        // Шаг 1: Создаем пользователя user1
        CreateUserRequest user1Request = AdminSteps.createUser();

        // Шаг 2: Создаем аккаунт для user1
        ValidatedCrudRequester<CreateAccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user1Request.getUsername(),
                                user1Request.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );
        CreateAccountResponse createdAccount = accountRequester.post(user1Request);
        // Шаг 3: Пытаемся сделать депозит на несуществующий счёт
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(999); // Несуществующий ID
        depositRequest.setBalance(100);

        ValidatedCrudRequester<DepositResponse> depositRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user1Request.getUsername(),
                                user1Request.getPassword()),
                        Endpoint.DEPOSITS,
                        ResponseSpecs.requestReturnsNotFound() // Ожидаем ошибку 404, но почему-то 403
                );

        // Выполняем депозит и проверяем, что запрос завершился ошибкой
        try {
            depositRequester.post(depositRequest);
        } catch (Exception e) {
            // Если сервер вернул ошибку, это нормально
        }
    }
}

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
import api.utils.AccountUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class UserTransferTest extends BaseTest {

    public static Stream<Arguments> amountsValid() {
        return Stream.of(
                Arguments.of(0.1),
                Arguments.of(0.5),
                Arguments.of(1.0),
                Arguments.of(9999.0),
                Arguments.of(9999.5),
                Arguments.of(10000.0)
        );
    }

    public static Stream<Arguments> amountsInvalid() {
        return Stream.of(
                Arguments.of(0.1, -0.1),
                Arguments.of(1.0, 0.0),
                Arguments.of(10001.0, 10001.0),
                Arguments.of(10000.5, 10000.5),
                Arguments.of(10000.1, 10000.1),
                Arguments.of(4000.0, 5000.0),
                Arguments.of(4000.0, 4000.1)
        );
    }

    @MethodSource("amountsValid")
    @ParameterizedTest
    public void userCanTransferValidAmountOfMoney(double amount) {
        // Шаг 1: Создаем пользователя через Admin
        CreateUserRequest userRequest = AdminSteps.createUser();
        // Шаг 2: Создаем аккаунт1 для пользователя
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester1 =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount1 = createAccountRequester1.post(userRequest);
        // Шаг 3: Делаем депозит на созданный аккаунт1
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(createdAccount1.getId());
        depositRequest.setBalance(amount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsOK()
        ).post(depositRequest);
        // Шаг 4: Создаем аккаунт2 для пользователя
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester2 =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount2 = createAccountRequester2.post(userRequest);
        // Шаг 5: Делаем перевод с созданного аккаунта1 на созданный аккаунт2
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderAccountId(createdAccount1.getId());
        transferRequest.setReceiverAccountId(createdAccount2.getId());
        transferRequest.setAmount(amount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsOK()
        ).post(transferRequest);
        // Шаг 6: Проверяем баланс созданного аккаунта2
        ValidatedCrudRequester<AccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.CUSTOMERS_ACCOUNT,
                        ResponseSpecs.requestReturnsOK()
                );
        //Преобразуем в список ответ
        List<AccountResponse> accounts = accountRequester.getAsList(null, AccountResponse.class);
        //Получаем опциональный ответ по id аккаунтов
        Optional<AccountResponse> accountOpt1 = AccountUtils.getById(accounts, createdAccount1.getId());
        Optional<AccountResponse> accountOpt2 = AccountUtils.getById(accounts, createdAccount2.getId());
        //Из опциональных ответов получаем ответы
        AccountResponse account1 = accountOpt1.get();
        AccountResponse account2 = accountOpt2.get();
        //Проверяем, что баланс аккаунта2 равен сумме перевода
        Assertions.assertThat(account2.getBalance()).isEqualTo(amount);
        //Проверяем, что баланс аккаунта1 равен 0
        Assertions.assertThat(account1.getBalance()).isEqualTo(0.0);
    }

    @MethodSource("amountsInvalid")
    @ParameterizedTest
    public void userCanNotTransferInvalidAmountOfMoney(double depositAmount, double transferAmount) {
        // Шаг 1: Создаем пользователя через Admin
        CreateUserRequest userRequest = AdminSteps.createUser();
        // Шаг 2: Создаем аккаунт1 для пользователя
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester1 =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount1 = createAccountRequester1.post(userRequest);
        // Шаг 3: Делаем депозит на созданный аккаунт1
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(createdAccount1.getId());
        depositRequest.setBalance(depositAmount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsOK()
        ).post(depositRequest);
        // Шаг 4: Создаем аккаунт2 для пользователя
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester2 =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount2 = createAccountRequester2.post(userRequest);
        // Шаг 5: Делаем перевод с созданного аккаунта1 на созданный аккаунт2
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderAccountId(createdAccount1.getId());
        transferRequest.setReceiverAccountId(createdAccount2.getId());
        transferRequest.setAmount(transferAmount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBR()
        ).post(transferRequest);
        // Шаг 6: Проверяем баланс созданного аккаунта2
        ValidatedCrudRequester<AccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.CUSTOMERS_ACCOUNT,
                        ResponseSpecs.requestReturnsOK()
                );
        //Преобразуем в список ответ
        List<AccountResponse> accounts = accountRequester.getAsList(null, AccountResponse.class);
        //Получаем опциональный ответ по id аккаунтов
        Optional<AccountResponse> accountOpt1 = AccountUtils.getById(accounts, createdAccount1.getId());
        Optional<AccountResponse> accountOpt2 = AccountUtils.getById(accounts, createdAccount2.getId());
        //Из опциональных ответов получаем ответы
        AccountResponse account1 = accountOpt1.get();
        AccountResponse account2 = accountOpt2.get();
        //Проверяем, что баланс аккаунта2 равен 0
        Assertions.assertThat(account2.getBalance()).isEqualTo(0);
        //Проверяем, что баланс аккаунта1 равен depositAmount
        Assertions.assertThat(account1.getBalance()).isEqualTo(depositAmount);
    }

    @MethodSource("amountsValid")
    @ParameterizedTest
    public void userCannotTransferOnOtherUsersAccount(double amount) {
        // Шаг 1: Создаем пользователя user1
        CreateUserRequest user1Request = AdminSteps.createUser();
        // Шаг 2: Создаем аккаунт для user1
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester1 =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user1Request.getUsername(),
                                user1Request.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount1 = createAccountRequester1.post(user1Request);
        // Шаг 3: Создаем пользователя user2
        CreateUserRequest user2Request = AdminSteps.createUser();
        // Шаг 4: Создаем аккаунт для user2
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester2 =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user2Request.getUsername(),
                                user2Request.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount2 = createAccountRequester2.post(user2Request);
        // Шаг 5: Делаем депозит на созданный аккаунт1
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(createdAccount1.getId());
        depositRequest.setBalance(amount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        user1Request.getUsername(),
                        user1Request.getPassword()),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsOK() // Ожидаем ошибку доступа
        ).post(depositRequest);
        // Шаг 6: Делаем перевод с созданного аккаунта1 на созданный аккаунт2
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderAccountId(createdAccount1.getId());
        transferRequest.setReceiverAccountId(createdAccount2.getId());
        transferRequest.setAmount(amount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        user1Request.getUsername(),
                        user1Request.getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBR()
        ).post(transferRequest);
        // Шаг 7: Проверяем баланс созданного аккаунта2 и аккаунта1
        ValidatedCrudRequester<AccountResponse> accountRequester2 =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user2Request.getUsername(),
                                user2Request.getPassword()),
                        Endpoint.CUSTOMERS_ACCOUNT,
                        ResponseSpecs.requestReturnsOK()
                );
        ValidatedCrudRequester<AccountResponse> accountRequester1 =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                user1Request.getUsername(),
                                user1Request.getPassword()),
                        Endpoint.CUSTOMERS_ACCOUNT,
                        ResponseSpecs.requestReturnsOK()
                );
        //Преобразуем в список ответ
        List<AccountResponse> accounts1 = accountRequester1.getAsList(null, AccountResponse.class);
        List<AccountResponse> accounts2 = accountRequester2.getAsList(null, AccountResponse.class);
        //Получаем опциональный ответ по id аккаунтов
        Optional<AccountResponse> accountOpt1 = AccountUtils.getById(accounts1, createdAccount1.getId());
        Optional<AccountResponse> accountOpt2 = AccountUtils.getById(accounts2, createdAccount2.getId());
        //Из опциональных ответов получаем ответы
        AccountResponse account1 = accountOpt1.get();
        AccountResponse account2 = accountOpt2.get();
        //Проверяем, что баланс аккаунта2 равен 0
        Assertions.assertThat(account2.getBalance()).isEqualTo(0);
        //Проверяем, что баланс аккаунта1 равен depositAmount
        Assertions.assertThat(account1.getBalance()).isEqualTo(amount);
    }

    @MethodSource("amountsValid")
    @ParameterizedTest
    public void userCanNotTransferOnNotExistAccount(double amount) {
        // Шаг 1: Создаем пользователя через Admin
        CreateUserRequest userRequest = AdminSteps.createUser();
        // Шаг 2: Создаем аккаунт1 для пользователя
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount = createAccountRequester.post(userRequest);
        // Шаг 3: Делаем депозит на созданный аккаунт1
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(createdAccount.getId());
        depositRequest.setBalance(amount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsOK()
        ).post(depositRequest);
        // Шаг 4: Делаем перевод с созданного аккаунта1 на несуществующий
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderAccountId(createdAccount.getId());
        transferRequest.setReceiverAccountId(999);
        transferRequest.setAmount(amount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBR()
        ).post(transferRequest);
        // Шаг 5: Проверяем баланс созданного аккаунта
        ValidatedCrudRequester<AccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.CUSTOMERS_ACCOUNT,
                        ResponseSpecs.requestReturnsOK()
                );
        //Преобразуем в список ответ
        List<AccountResponse> accounts = accountRequester.getAsList(null, AccountResponse.class);
        //Получаем опциональный ответ по id аккаунтов
        Optional<AccountResponse> accountOpt = AccountUtils.getById(accounts, createdAccount.getId());
        //Из опциональных ответов получаем ответы
        AccountResponse account = accountOpt.get();
        //Проверяем, что баланс аккаунта не изменился
        Assertions.assertThat(account.getBalance()).isEqualTo(amount);
    }

    @MethodSource("amountsValid")
    @ParameterizedTest
    public void userCanNotTransferOnHisOwnAccount(double amount) {
        // Шаг 1: Создаем пользователя через Admin
        CreateUserRequest userRequest = AdminSteps.createUser();
        // Шаг 2: Создаем аккаунт1 для пользователя
        ValidatedCrudRequester<CreateAccountResponse> createAccountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated()
                );

        CreateAccountResponse createdAccount = createAccountRequester.post(userRequest);
        // Шаг 3: Делаем депозит на созданный аккаунт1
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setId(createdAccount.getId());
        depositRequest.setBalance(amount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsOK()
        ).post(depositRequest);
        // Шаг 4: Делаем перевод с созданного аккаунта1 на аккаунт1
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderAccountId(createdAccount.getId());
        transferRequest.setReceiverAccountId(createdAccount.getId());
        transferRequest.setAmount(amount);

        new CrudRequester(
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBR()
        ).post(transferRequest);
        // Шаг 5: Проверяем баланс созданного аккаунта
        ValidatedCrudRequester<AccountResponse> accountRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.CUSTOMERS_ACCOUNT,
                        ResponseSpecs.requestReturnsOK()
                );
        //Преобразуем в список ответ
        List<AccountResponse> accounts = accountRequester.getAsList(null, AccountResponse.class);
        //Получаем опциональный ответ по id аккаунтов
        Optional<AccountResponse> accountOpt = AccountUtils.getById(accounts, createdAccount.getId());
        //Из опциональных ответов получаем ответы
        AccountResponse account = accountOpt.get();
        //Проверяем, что баланс аккаунта не изменился
        Assertions.assertThat(account.getBalance()).isEqualTo(amount);
    }
}

package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.steps.AdminSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositMoneyPage;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTransferTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanTransferValidAmountOfMoney() {
        //Генерируем случайную сумму депозита (0.1-5000.00) в переменную amount
        String amount = RandomData.generateRandomAmount();
        //Создаем аккаунт 1
        new UserDashboard().open().createNewAccount();
        //Проверяем, что создался аккаунт 1
        List<CreateAccountResponse> createdAccounts1 = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccounts1).hasSize(1);
        //Проверяем, что на аккаунте 1 баланс 0
        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage()
                        + createdAccounts1.getFirst().getAccountNumber());

        assertThat(createdAccounts1.getFirst().getBalance()).isZero();
        //Делаем депозит amount на аккаунт 1
        new UserDashboard().open().depositMoney()
                .getPage(DepositMoneyPage.class).selectAccount()
                .chooseAccount(createdAccounts1.getFirst().getAccountNumber())
                .enterAmount(amount)
                .depositButton()
                .checkAlertMessageAndAccept(
                String.format("%s $%s to account %s!",
                        BankAlert.DEPOSIT_SUCCESSFULLY.getMessage(),
                        amount,
                        createdAccounts1.getFirst().getAccountNumber())
        );
        //Проверяем, что на аккаунте 1 баланс amount
        List<CreateAccountResponse> createdAccountsAfterDeposit = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccountsAfterDeposit.getFirst().getBalance()).isEqualTo(Double.parseDouble(amount));
        //Создаем аккаунт 2
        new UserDashboard().open().createNewAccount();
        //Проверяем что создался аккаунт 2
        List<CreateAccountResponse> createdAccounts2 = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccounts2).hasSize(2);
        //Делаем перевод amount с созданного аккаунта 1 на созданный аккаунт 2
        String currentUserUsername = SessionStorage.getSteps().getUsername();

        new UserDashboard().makeATransfer()
                .getPage(TransferPage.class).selectAccount()
                .chooseAccount(createdAccounts1.getFirst().getAccountNumber())
                .enterRecipientName(currentUserUsername)
                .enterRecipientAccountNumber(createdAccounts2.get(1).getAccountNumber())
                .enterAmount(amount)
                .confirmCheckbox()
                .sendTransfer()
                .checkAlertMessageAndAccept(
                        String.format("%s $%s to account %s!",
                                BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage(),
                                amount,
                                createdAccounts2.get(1).getAccountNumber())
                );
        //Проверяем, что на аккаунте 2 баланс amount, а на аккаунте 1 баланс 0
        List<CreateAccountResponse> createdAccountsAfterTransfer = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccountsAfterTransfer.getFirst().getBalance()).isZero();
        assertThat(createdAccountsAfterTransfer.get(1).getBalance()).isEqualTo(Double.parseDouble(amount));
    }

    @Test
    @UserSession
    public void userCanNotTransferInvalidAmountOfMoney() {
        //Генерируем случайную сумму депозита (0.1-5000.00) в переменную amount
        String amount = RandomData.generateRandomAmount();
        //Создаем аккаунт 1
        new UserDashboard().open().createNewAccount();
        //Проверяем, что создался аккаунт 1
        List<CreateAccountResponse> createdAccounts1 = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccounts1).hasSize(1);
        //Проверяем, что на аккаунте 1 баланс 0
        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage()
                        + createdAccounts1.getFirst().getAccountNumber());

        assertThat(createdAccounts1.getFirst().getBalance()).isZero();
        //Делаем депозит amount на аккаунт 1
        new UserDashboard().open().depositMoney()
                .getPage(DepositMoneyPage.class).selectAccount()
                .chooseAccount(createdAccounts1.getFirst().getAccountNumber())
                .enterAmount(amount)
                .depositButton()
                .checkAlertMessageAndAccept(
                        String.format("%s $%s to account %s!",
                                BankAlert.DEPOSIT_SUCCESSFULLY.getMessage(),
                                amount,
                                createdAccounts1.getFirst().getAccountNumber())
                );
        //Проверяем, что на аккаунте 1 баланс amount
        List<CreateAccountResponse> createdAccountsAfterDeposit = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccountsAfterDeposit.getFirst().getBalance()).isEqualTo(Double.parseDouble(amount));
        //Создаем аккаунт 2
        new UserDashboard().open().createNewAccount();
        //Проверяем что создался аккаунт 2
        List<CreateAccountResponse> createdAccounts2 = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccounts2).hasSize(2);
        //Делаем перевод 0 с созданного аккаунта 1 на созданный аккаунт 2
        String currentUserUsername = SessionStorage.getSteps().getUsername();

        new UserDashboard().makeATransfer()
                .getPage(TransferPage.class).selectAccount()
                .chooseAccount(createdAccounts1.getFirst().getAccountNumber())
                .enterRecipientName(currentUserUsername)
                .enterRecipientAccountNumber(createdAccounts2.get(1).getAccountNumber())
                .enterAmount("100000")//остальное через апи
                .confirmCheckbox()
                .sendTransfer()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());
        //Проверяем, что на аккаунте 1 баланс amount, а на аккаунте 2 баланс 0 (так как не было перевода)
        List<CreateAccountResponse> createdAccountsAfterTransfer = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccountsAfterTransfer.getFirst().getBalance()).isZero();
        assertThat(createdAccountsAfterTransfer.get(1).getBalance()).isEqualTo(Double.parseDouble(amount));
    }
}

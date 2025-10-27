package iteration2.ui;

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

import static org.assertj.core.api.Assertions.assertThat;

public class UserTransferTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanTransferValidAmountOfMoney() {
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
        //Делаем депозит 2000 на аккаунт 1
        new UserDashboard().open().depositMoney()
                .getPage(DepositMoneyPage.class).selectAccount()
                .chooseAccount(createdAccounts1.getFirst().getAccountNumber())
                .enterAmount("2000")//хардкод так как проверяем только одно значение, остальное через апи
                .depositButton()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage()
                        + " $2000 to account " + createdAccounts1.getFirst().getAccountNumber() + "!");//хардкод так как проверяем только одно значение, остальное через апи
        //Проверяем, что на аккаунте 1 баланс 2000
        List<CreateAccountResponse> createdAccountsAfterDeposit = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccountsAfterDeposit.getFirst().getBalance()).isEqualTo(2000);
        //Создаем аккаунт 2
        new UserDashboard().open().createNewAccount();
        //Проверяем что создался аккаунт 2
        List<CreateAccountResponse> createdAccounts2 = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccounts2).hasSize(2);
        //Делаем перевод 2000 с созданного аккаунта 1 на созданный аккаунт 2
        String currentUserUsername = SessionStorage.getSteps().getUsername();

        new UserDashboard().makeATransfer()
                .getPage(TransferPage.class).selectAccount()
                .chooseAccount(createdAccounts1.getFirst().getAccountNumber())
                .enterRecipientName(currentUserUsername)
                .enterRecipientAccountNumber(createdAccounts2.get(1).getAccountNumber())
                .enterAmount("2000")//хардкод так как проверяем только одно значение, остальное через апи
                .confirmCheckbox()
                .sendTransfer()
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage() +
                        " $2000 to account " + createdAccounts2.get(1).getAccountNumber() + "!");//хардкод так как проверяем только одно значение, остальное через апи
        //Проверяем, что на аккаунте 2 баланс 2000, а на аккаунте 1 баланс 0
        List<CreateAccountResponse> createdAccountsAfterTransfer = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccountsAfterTransfer.getFirst().getBalance()).isZero();
        assertThat(createdAccountsAfterTransfer.get(1).getBalance()).isEqualTo(2000);
    }

    @Test
    @UserSession
    public void userCanNotTransferInvalidAmountOfMoney() {
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
        //Делаем депозит 2000 на аккаунт 1
        new UserDashboard().open().depositMoney()
                .getPage(DepositMoneyPage.class).selectAccount()
                .chooseAccount(createdAccounts1.getFirst().getAccountNumber())
                .enterAmount("2000")//хардкод так как проверяем только одно значение, остальное через апи
                .depositButton()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage()
                        + " $2000 to account " + createdAccounts1.getFirst().getAccountNumber() + "!");//хардкод так как проверяем только одно значение, остальное через апи
        //Проверяем, что на аккаунте 1 баланс 2000
        List<CreateAccountResponse> createdAccountsAfterDeposit = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccountsAfterDeposit.getFirst().getBalance()).isEqualTo(2000);
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
                .enterAmount("0")//хардкод так как проверяем только одно значение, остальное через апи
                .confirmCheckbox()
                .sendTransfer()
                .checkAlertMessageAndAccept(BankAlert.INVALID_TRANSFER.getMessage());
        //Проверяем, что на аккаунте 1 баланс 2000, а на аккаунте 2 баланс 0 (так как не было перевода)
        List<CreateAccountResponse> createdAccountsAfterTransfer = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccountsAfterTransfer.getFirst().getBalance()).isZero();
        assertThat(createdAccountsAfterTransfer.get(1).getBalance()).isEqualTo(2000);

    }
}

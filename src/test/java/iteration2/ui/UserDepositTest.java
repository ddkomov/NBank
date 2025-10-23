package iteration2.ui;

import api.models.CreateAccountResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositMoneyPage;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDepositTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanDepositValidAmountOfMoney() {
        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage()
                        + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();

        new UserDashboard().open().depositMoney()
                .getPage(DepositMoneyPage.class).selectAccount()
                .chooseAccount(createdAccounts.getFirst().getAccountNumber())
                .enterAmount("2000")//хардкод так как проверяем только одно значение, остальное через апи
                .depositButton()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage()
                        + " $2000 to account " + createdAccounts.getFirst().getAccountNumber() +"!");//хардкод так как проверяем только одно значение, остальное через апи

        List<CreateAccountResponse> createdAccounts2 = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccounts2.getFirst().getBalance()).isEqualTo(2000);
    }

    @Test
    @UserSession
    public void userCanNotDepositInvalidAmountOfMoney() {
        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage()
                        + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();

        new UserDashboard().open().depositMoney()
                .getPage(DepositMoneyPage.class).selectAccount()
                .chooseAccount(createdAccounts.getFirst().getAccountNumber())
                .enterAmount("0")//хардкод так как проверяем только одно значение, остальное через апи
                .depositButton()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_ENTER_A_VALID_AMOUNT.getMessage());

        List<CreateAccountResponse> createdAccounts2 = SessionStorage.getSteps()
                .getAllAccounts();

        assertThat(createdAccounts2.getFirst().getBalance()).isZero();
    }
}

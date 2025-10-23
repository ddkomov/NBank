package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class DepositMoneyPage extends BasePage<DepositMoneyPage> {
    private SelenideElement depositButton = $(Selectors.byXpath("//button[contains(text(),'Deposit')]"));
    private SelenideElement selectAccount = $(Selectors.byXpath("//select[@class='form-control account-selector']"));

    private SelenideElement enterAmount = $(Selectors.byXpath("//input[@class='form-control deposit-input']"));


    @Override
    public String url() {
        return "/deposit";
    }


    public DepositMoneyPage depositButton() {
        depositButton.click();
        return this;
    }

    public DepositMoneyPage selectAccount() {
        selectAccount.click();
        return this;
    }

    public DepositMoneyPage enterAmount(String amount) {
        enterAmount.sendKeys(amount);
        return this;
    }

    public DepositMoneyPage chooseAccount(String accountNumber) {
        String numberStr = accountNumber.replaceAll("[^0-9]", "");
        SelenideElement createdAccountNumber = $(Selectors.byXpath("(//select[@class='form-control account-selector']//option[@value='" + numberStr + "'])"));
        createdAccountNumber.click();
        return this;
    }
}

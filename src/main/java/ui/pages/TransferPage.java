package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class TransferPage extends BasePage<TransferPage> {
    private SelenideElement recipientName = $(Selectors.byXpath("//input[@placeholder='Enter recipient name']"));
    private SelenideElement selectAccount = $(Selectors.byXpath("//select[@class='form-control account-selector']"));
    private SelenideElement enterAmount = $(Selectors.byXpath("//input[@placeholder='Enter amount']"));
    private SelenideElement recipientAccountNumber = $(Selectors.byXpath("//input[@placeholder='Enter recipient account number']"));
    private SelenideElement confirmCheckbox = $(Selectors.byId("confirmCheck"));
    private SelenideElement sendTransfer = $(Selectors.withText("Send Transfer"));



    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage sendTransfer() {
        sendTransfer.click();
        return this;
    }

    public TransferPage confirmCheckbox() {
        confirmCheckbox.click();
        return this;
    }

    public TransferPage enterRecipientAccountNumber(String number) {
        recipientAccountNumber.click();
        recipientAccountNumber.sendKeys(number);
        return this;
    }

    public TransferPage enterRecipientName(String name) {
        recipientName.click();
        recipientName.sendKeys(name);
        return this;
    }

    public TransferPage selectAccount() {
        selectAccount.click();
        return this;
    }

    public TransferPage enterAmount(String amount) {
        enterAmount.click();
        enterAmount.sendKeys(amount);
        return this;
    }

    public TransferPage chooseAccount(String accountNumber) {
        String numberStr = accountNumber.replaceAll("[^0-9]", "");
        SelenideElement createdAccountNumber = $(Selectors.byXpath("(//select[@class='form-control account-selector']//option[@value='" + numberStr + "'])"));
        createdAccountNumber.click();
        return this;
    }

}

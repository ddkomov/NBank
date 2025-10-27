package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class EditProfilePage extends BasePage<EditProfilePage>{

    private SelenideElement enterNewName = $(Selectors.byXpath("//input[@placeholder='Enter new name']"));
    private SelenideElement saveChanges = $(Selectors.withText("Save Changes"));


    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfilePage enterNewName(String newName) throws InterruptedException {
        enterNewName.click();
        Thread.sleep(1000);
        enterNewName.sendKeys(newName);
        return this;
    }

    public EditProfilePage saveChanges(){
        saveChanges.click();
        return this;
    }


}

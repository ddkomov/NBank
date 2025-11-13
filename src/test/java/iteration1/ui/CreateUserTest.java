package iteration1.ui;

import api.requests.steps.AdminSteps;
import com.codeborne.selenide.*;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import common.annotations.AdminSession;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.WebDriverWait;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;
import ui.pages.BasePage;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CreateUserTest extends BaseUiTest {


    @Test
    @AdminSession
    public void adminCanCreateUserTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        AdminPanel adminPanel = new AdminPanel().open();
        adminPanel.createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage());

        // ✅ Исправлено: используем WebDriverRunner
        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(10));
        wait.until(driver -> adminPanel.getAllUsers().stream()
                .anyMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));

        // Проверка
        assertTrue(adminPanel.getAllUsers().stream()
                .anyMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));

        // API проверка
        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().orElseThrow();
        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }
    @Test
    @AdminSession
    public void adminCanNotCreateUserWithInvalidDataTest() {

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        assertTrue(new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
                .getAllUsers().stream().noneMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));
        //ШАГ 5: проверка, что юзер создан на API
        long usersWithSameUsernameAsNewUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername())).count();

        assertThat(usersWithSameUsernameAsNewUser).isZero();

    }
}

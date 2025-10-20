package iteration1.ui;

import api.requests.steps.AdminSteps;
import com.codeborne.selenide.*;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import api.specs.RequestSpecs;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import java.util.Arrays;
import java.util.Map;

import static com.codeborne.selenide.Condition.*;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateUserTest extends BaseUiTest {

    @Test
    public void adminCanCreateUserTest() {
        //ШАГ 1: админ залогинился в банке
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);
        //ШАГ 2: админ создает юзера в банке
        //ШАГ 3: проверка, что алерт "✅ User created successfully!"
        //ШАГ 4: проверка, что юзер отображается на UI
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
                .getAllUsers().findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(visible);

        //ШАГ 5: проверка, что юзер создан на API

        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();
        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }
    @Test
    public void adminCanNotCreateUserWithInvalidDataTest() {
        //ШАГ 1: админ залогинился в банке
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);
        //ШАГ 2: админ создает юзера в банке
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
                .getAllUsers().findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(exist);
        //ШАГ 5: проверка, что юзер создан на API
        long usersWithSaneUsernameAsNewUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername())).count();

        assertThat(usersWithSaneUsernameAsNewUser).isZero();

    }
}

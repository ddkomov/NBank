package iteration2.ui;


import api.models.CreateUserResponse;
import api.requests.Endpoint;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;

public class CustomerManagementTest extends BaseUiTest {

    @Test
    @UserSession
    public void changeCustomerName() throws InterruptedException {
        String newName = "New Name";
        //Изменяем имя пользователя
        new UserDashboard().open()
                .userInfo()
                .getPage(EditProfilePage.class).enterNewName(newName)
                .saveChanges().checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());
        //Вытаскиваем пользователя из хранилища
        var currentUser = SessionStorage.getSteps();

        ValidatedCrudRequester<CreateUserResponse> userRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                currentUser.getUsername(),
                                currentUser.getPassword()),
                        Endpoint.CUSTOMERS_PROFILE,
                        ResponseSpecs.requestReturnsOK()
                );

        CreateUserResponse updatedProfile = userRequester.get(null);
        //Проверка изменения имени
        Assertions.assertThat(updatedProfile.getName())
                .as("Проверка обновления имени пользователя")
                .withFailMessage("Ожидалось имя: '%s', но получено: '%s'", newName, updatedProfile.getName())
                .isEqualTo(newName);
    }
}

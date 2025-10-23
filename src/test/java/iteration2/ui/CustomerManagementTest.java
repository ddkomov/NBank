package iteration2.ui;


import common.annotations.UserSession;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;

public class CustomerManagementTest extends BaseUiTest {

    @Test
    @UserSession
    public void changeCustomerName(){
        String name = "New Name";

        new UserDashboard().open()
                .userInfo()
                .getPage(EditProfilePage.class).enterNewName(name)
                .saveChanges().checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());
        //Проверка изменения имени

    }
}

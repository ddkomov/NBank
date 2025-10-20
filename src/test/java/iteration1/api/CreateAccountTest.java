package iteration1.api;

import api.models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import api.requests.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import static io.restassured.RestAssured.given;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccount() {
        CreateUserRequest userRequest =
                AdminSteps.createUser();

        new CrudRequester(RequestSpecs.authAsUser(
                userRequest.getUsername(),
                userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        //запросить все счета и проверить, что он создан
    }
}


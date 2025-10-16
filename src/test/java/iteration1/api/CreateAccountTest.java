package iteration1.api;

import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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


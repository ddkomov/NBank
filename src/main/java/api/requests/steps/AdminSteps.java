package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.Endpoint;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.helpers.StepLogger;
import common.storage.SessionStorage;

import java.util.List;

public class AdminSteps {
    public static CreateUserRequest createUser() {

        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);
        return StepLogger.log("Admin create users", () -> {
            //создание пользователя
            new ValidatedCrudRequester<CreateUserResponse>(
                    RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_USER,
                    ResponseSpecs.entityWasCreated())
                    .post(userRequest);

            return userRequest;
        });
    }

    public static List<CreateUserResponse> getAllUsers() {
        return StepLogger.log("Admin get all users", () -> {
            return new ValidatedCrudRequester<CreateUserResponse>(
                    RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_USER,
                    ResponseSpecs.requestReturnsOK()).getAll(CreateUserResponse[].class);
        });
    }
}

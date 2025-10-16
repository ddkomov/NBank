package iteration2.api;

import iteration1.api.BaseTest;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.UpdateProfileRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import requests.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CustomerManagementTests extends BaseTest {

    @Test
    public void changeCustomerName() {
        // Шаг 1: Создаем пользователя через Admin
        CreateUserRequest userRequest = AdminSteps.createUser();
        // Шаг 2: Проверяем начальное значение поля name
        ValidatedCrudRequester<CreateUserResponse> userProfileBeforeRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.CUSTOMERS_PROFILE,
                        ResponseSpecs.requestReturnsOK()
                );

        CreateUserResponse userProfileBeforeResponse = userProfileBeforeRequester.get(null);
        Assertions.assertThat(userProfileBeforeResponse.getName()).isEqualTo(null);
        // Шаг 3: Меняем имя пользователя
        UpdateProfileRequest profileRequest = new UpdateProfileRequest();
        profileRequest.setName("New Name");

        new CrudRequester(
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                Endpoint.CUSTOMERS_PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).put(profileRequest);
        // Шаг 4: Проверяем, что имя изменилось
        ValidatedCrudRequester<CreateUserResponse> userProfileAfterRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        Endpoint.CUSTOMERS_PROFILE,
                        ResponseSpecs.requestReturnsOK()
                );

        CreateUserResponse userProfileAfterResponse = userProfileAfterRequester.get(null);
        Assertions.assertThat(userProfileAfterResponse.getName()).isEqualTo(profileRequest.getName());
    }
}

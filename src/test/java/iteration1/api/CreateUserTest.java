package iteration1.api;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.awt.*;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;


public class CreateUserTest extends BaseTest {


    @Test
    public void adminCanCreateUserWithValidData() {
        CreateUserRequest createUserRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER,
                        ResponseSpecs.entityWasCreated())
                .post(createUserRequest);


        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();

    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                // Пустое имя → две ошибки (в любом порядке)
                Arguments.of("    ", "Password33$", "USER", "username",
                        hasItems(
                                "Username cannot be blank",
                                "Username must contain only letters, digits, dashes, underscores, and dots"
                        )),
                // Слишком короткое → одна ошибка
                Arguments.of("ab", "Password33$", "USER", "username",
                        hasItem("Username must be between 3 and 15 characters")),
                // Символ % → недопустимый символ
                Arguments.of("abc%", "Password33$", "USER", "username",
                        hasItem("Username must contain only letters, digits, dashes, underscores, and dots")),
                // Символ $ → недопустимый символ
                Arguments.of("abc$", "Password33$", "USER", "username",
                        hasItem("Username must contain only letters, digits, dashes, underscores, and dots"))
        );
    }


    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void adminCanNotCreateUserWithInvalidData(
            String username,
            String password,
            String role,
            String errorKey,
            Matcher<?> errorMatcher) {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorMatcher)
        ).post(createUserRequest);
    }
}

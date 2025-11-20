package api.requests.skeleton.requesters;

import api.models.CreateUserResponse;
import api.requests.skeleton.interfaces.GetAllEndpointInterface;
import api.specs.RequestSpecs;
import common.helpers.StepLogger;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.Endpoint;
import api.requests.skeleton.HttpRequest;
import api.requests.skeleton.interfaces.CrudEndpointInterface;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {

    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    @Step("POST запрос на {endpoint} с телом {model}")
    public ValidatableResponse post(BaseModel model) {
       return StepLogger.log("POST запрос на " + endpoint.getUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .post(endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }
    @Override
    @Step("GET запрос на {endpoint} с параметром {id}")
    public ValidatableResponse get(Long id) {
        String url = endpoint.getUrl();

//        // Проверяем, есть ли в URL шаблон вида {xxx}
//        if (url.contains("{")) {
//            // Ищем первый шаблон {xxx} и заменяем его на id
//            String pattern = url.replaceAll("^.*?\\{([^}]*)\\}.*?$", "$1"); // Извлекаем имя параметра
//            url = url.replaceFirst("\\{[^}]*\\}", String.valueOf(id));
//        } else {
//            // Если нет шаблона — добавляем /{id} в конец
//            url += "/{id}";
//        }

        if (id != null && url.contains("{")) {
            url = url.replaceFirst("\\{[^}]*\\}", String.valueOf(id));
        }

        return given()
                .spec(requestSpecification)
                .get(url)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    @Step("DELETE запрос на {endpoint} с параметром {id}")
    public ValidatableResponse delete(long id) {
        return given()
                .spec(requestSpecification)
                .pathParam("id", id)
                .delete(endpoint.getUrl() + "/{id}")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }


    @Override
    @Step("PUT запрос на {endpoint} с телом {model}")
    public ValidatableResponse put(BaseModel model) {
        var body = model == null ? "" : model;
        return given()
                .spec(requestSpecification)
                .body(body)
                .put(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    @Step("GET запрос на {endpoint} с параметром clazz ")
    public ValidatableResponse getAll(Class<?> clazz) {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}

package requests.skeleton.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.Endpoint;
import requests.skeleton.HttpRequest;
import requests.skeleton.interfaces.CrudEndpointInterface;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface {

    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        var body = model == null ? "" : model;
        return given()
                .spec(requestSpecification)
                .body(body)
                .post(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
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
    public ValidatableResponse update(long id, BaseModel model) {
        var body = model == null ? "" : model;
        return given()
                .spec(requestSpecification)
                .pathParam("id", id)
                .body(body)
                .put(endpoint.getUrl() + "/{id}")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}

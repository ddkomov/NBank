package requests.skeleton.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.Endpoint;
import requests.skeleton.HttpRequest;
import requests.skeleton.interfaces.CrudEndpointInterface;

import java.util.List;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndpointInterface {
    private CrudRequester crudRequester;

    public ValidatedCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T post(BaseModel model) {
        return (T) crudRequester.post(model).extract().as(endpoint.getResponseModel());

    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(Long id) {
        return (T) crudRequester.get(id).extract().as(endpoint.getResponseModel());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void delete(long id) {
        crudRequester.delete(id).extract().jsonPath(); // Можно добавить проверку статуса
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T update(long id, BaseModel model) {
        return (T) crudRequester.update(id, model).extract().as(endpoint.getResponseModel());
    }

    public <R> List<R> getAsList(Long id, Class<R> itemType) {
        return crudRequester.get(id).extract().jsonPath().getList("", itemType);
    }
}

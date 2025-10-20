package api.requests.skeleton.interfaces;

import api.models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get(Long id);
    Object delete(long id);
    Object put(BaseModel model);
}

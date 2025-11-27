package api.utils;

import api.models.AccountResponse;
import api.requests.skeleton.interfaces.Identifiable;

import java.util.List;
import java.util.Optional;

public class AccountUtils {
    // Identifiable необходим для получения объекта по id, если на вход методу подаются объекты разных типов, но имплементирующие Identifiable
    // нужен только дата моделям, у которых есть id и геттеры\сеттеры для него
    public static <T extends Identifiable> Optional<T> getById(List<T> items, long id) {
        return items.stream()
                .filter(item -> item.getId() == id)
                .findFirst();
    }
}
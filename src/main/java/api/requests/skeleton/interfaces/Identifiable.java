package api.requests.skeleton.interfaces;

public interface Identifiable {
    long getId(); // Для обобщённого метода getById, работающего с любым типом, реализующим Identifiable
}

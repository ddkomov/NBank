package api.generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Locale;
import java.util.Random;

public class RandomData {
    private RandomData() {
    }

    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(3) + "%$#";
    }

    public static String generateRandomAmount() {
        Random random = new Random();
        double amount = 0.1 + (5000 - 0.1) * random.nextDouble(); // диапазон [0.1, 5000]
        return String.format(Locale.ENGLISH, "%.2f", amount); // округляем до 2 знаков после запятой
    }
}

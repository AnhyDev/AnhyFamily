package ink.anh.family.fdetails.symbol;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UUIDToUniqueString {

    public static String getUniqueStringFromUUID(UUID uuid) {
        try {
            // Отримуємо байти UUID
            String uuidString = uuid.toString();

            // Хешуємо UUID за допомогою SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(uuidString.getBytes(StandardCharsets.UTF_8));

            // Конвертуємо хеш в унікальний рядок довжиною 6 символів (латинські букви верхнього регістру)
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                int index = hash[i] & 0x1F; // Використовуємо перші 5 біт для отримання значення від 0 до 31
                char c = (char) ('A' + index % 26); // Обмежуємо до латинських букв верхнього регістру
                result.append(c);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}

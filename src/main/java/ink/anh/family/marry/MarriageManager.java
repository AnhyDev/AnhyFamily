package ink.anh.family.marry;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ink.anh.family.AnhyFamily;

public class MarriageManager {

    private static MarriageManager instance;
    private List<Marry> marryList;

    private MarriageManager(AnhyFamily plugin) {
        marryList = new ArrayList<>();
    }

    public static synchronized MarriageManager getInstance(AnhyFamily plugin) {
        if (instance == null) {
            instance = new MarriageManager(plugin);
        }
        return instance;
    }

    public void reload() {
        marryList.clear();
    }

    public synchronized boolean add(Player bride1, Player bride2, Player priest, int surnameChoice, String[] chosenSurname) {
        // Перевірка, чи один з гравців вже бере участь у шлюбі
        if (contains(bride1) || contains(bride2)) {
            return false;
        }

        // Створення нового об'єкту Marry
        Marry marry = new Marry(bride1, bride2, priest, surnameChoice, chosenSurname);

        // Додавання об'єкту Marry до списку
        return marryList.add(marry);
    }

    public synchronized boolean contains(Object obj) {
        for (Marry marry : marryList) {
            if (marry.isParticipant(obj)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean remove(Object obj) {
        return marryList.removeIf(marry -> marry.isParticipant(obj));
    }

    public synchronized List<Marry> getMarryList() {
        return marryList;
    }

    public synchronized Marry getMarryElement(Object obj) {
        for (Marry marry : marryList) {
            if (marry.isParticipant(obj)) {
                return marry;
            }
        }
        return null;
    }

    public synchronized boolean infoMarryElement() {
        if (!marryList.isEmpty()) {
            Bukkit.getLogger().info("Заявки на свадьбу:");
            int i = 1;
            for (Marry marry : marryList) {
                String info = i + ". Marry: " + marry.getBride1().getName() + " and " + marry.getBride2().getName();
                Bukkit.getLogger().info(info);
                i++;
            }
        } else {
            Bukkit.getLogger().info("Заявки на свадьбу отсутствуют");
        }
        return true;
    }
}

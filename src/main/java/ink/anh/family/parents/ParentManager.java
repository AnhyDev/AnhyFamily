package ink.anh.family.parents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import ink.anh.family.AnhyFamily;

public class ParentManager {

    private static ParentManager instance;
    
    private List<UUID[]> parents;

    private ParentManager(AnhyFamily plugin) {
        parents = new ArrayList<>();
    }

    /**
     * Повертає екземпляр FamilyManager. Якщо екземпляр ще не створено,
     * створює новий екземпляр із заданим плагіном.
     *
     * @param plugin екземпляр плагіна Mystery
     * @return екземпляр FamilyManager
     */
    public static synchronized ParentManager getInstance(AnhyFamily plugin) {
        if (instance == null) {
            instance = new ParentManager(plugin);
        }
        return instance;
    }

    /**
     * Очищує список.
     */
    public void reload() {
    	parents.clear();
    }

    public synchronized int addOrUpdateParent(UUID uuid1, UUID uuid2) {
        if (uuid1.equals(uuid2)) {
            return -1;
        }

        UUID[] parentElement = findParentElement(uuid1);
        if (parentElement != null) {
            if (parentElement[1] == null) {
                parentElement[1] = uuid2;
                return 1;
            } else if (parentElement[1].equals(uuid2) || (parentElement[2] != null && parentElement[2].equals(uuid2))) {
                return 3;
            } else if (parentElement[2] == null) {
                parentElement[2] = uuid2;
                return 2;
            } else {
                return 0;
            }
        } else {
            UUID[] newElement = new UUID[3];
            newElement[0] = uuid1;
            newElement[1] = uuid2;
            parents.add(newElement);
            return 1;
        }
    }

    public synchronized boolean removeParent(UUID uuid1) {
        UUID[] parentElement = findParentElement(uuid1);
        if (parentElement != null) {
            parents.remove(parentElement);
            return true;
        }
        return false;
    }

    public synchronized UUID[] getParentElement(UUID uuid1) {
        return findParentElement(uuid1);
    }

    public synchronized boolean infoParentElement() {
    	if (!parents.isEmpty()) {
    		Bukkit.getLogger().info("Заявки на усыновление:");
    		int i = 1;
        	for (UUID[] parentPair : parents) {
        	    String parentPairString = Arrays.toString(parentPair);
        	    Bukkit.getLogger().info(i + ". Parents: " + parentPairString);
        	}
    	} else {
    		Bukkit.getLogger().info("Заявки на усыновление отсутствуют");
    	}
    	return true;
    }


    private UUID[] findParentElement(UUID uuid1) {
        for (UUID[] element : parents) {
            if (element[0].equals(uuid1)) {
                return element;
            }
        }
        return null;
    }
}

package ink.anh.family.fplayer;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import com.google.gson.Gson;

import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.fplayer.permissions.AbstractPermission;
import ink.anh.family.fplayer.permissions.ActionsPermissions;
import ink.anh.family.fplayer.permissions.PermissionManager;

public class PlayerFamily {

    private UUID root;
    private Gender gender; 
    private String loverCaseName;
    private String[] lastName;
    private String[] oldLastName;
    private UUID father;
    private UUID mother;
    private UUID spouse;
    private Set<UUID> children = new HashSet<>();
    private UUID familyId = null;
    private Map<ActionsPermissions, AbstractPermission> permissionsMap = new HashMap<>();

	public PlayerFamily(UUID root, Gender gender, String loverCaseName, String[] lastName, String[] oldLastName, UUID father, UUID mother, UUID spouse,
            Set<UUID> children, UUID familyId, Map<ActionsPermissions, AbstractPermission> permissionsMap) {
        
        this.root = root;
        this.gender = gender;
        this.loverCaseName = loverCaseName;
        this.lastName = lastName;
        this.oldLastName = oldLastName;
        this.father = father;
        this.mother = mother;
        this.spouse = spouse;
        this.children = children;
        this.familyId = familyId;
        this.permissionsMap = permissionsMap;
    }
    
    public PlayerFamily(UUID root, String loverCaseName) {
        this.root = root;
        this.gender = Gender.UNDECIDED;
        this.loverCaseName = loverCaseName;
        this.lastName = new String[2];
        this.oldLastName = new String[2];
        this.father = null;
        this.mother = null;
        this.spouse = null;
        this.children = new HashSet<>();
        this.familyId = null;
        this.permissionsMap = PermissionManager.createDefaultPermissionsMap(this);;
    }

	public static PlayerFamily getMyFamily(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        PlayerFamily fam = gson.fromJson(str , PlayerFamily.class);
        return fam;
    }

    public String getCurrentSurname() {
        if (lastName == null || lastName.length == 0) {
            return "";
        }
        if (lastName.length == 1 || gender == Gender.MALE || gender == Gender.NON_BINARY) {
            return lastName[0] != null ? lastName[0] : "";
        } else {
            return (lastName[1] != null) ? lastName[1] : lastName[0] != null ? lastName[0] : "";
        }
    }

    public UUID getRoot() {
        return root;
    }

    public void setRoot(UUID root) {
        this.root = root;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public UUID getFather() {
        return father;
    }

    public UUID getMother() {
        return mother;
    }

    public String getLoverCaseName() {
        return loverCaseName;
    }

    public String[] getLastName() {
        return lastName;
    }

    public String[] getOldLastName() {
        return oldLastName;
    }

    public UUID getSpouse() {
        return spouse;
    }

    public void setFather(UUID father) {
        this.father = father;
    }

    public void setMother(UUID mother) {
        this.mother = mother;
    }

    public Set<UUID> getChildren() {
        return children;
    }

    public void setLoverCaseName(String displayName) {
        this.loverCaseName = displayName;
    }

    public void setLastName(String[] lastName) {
        this.lastName = lastName;
    }

    public void setOldLastName(String[] oldLastName) {
        this.oldLastName = oldLastName;
    }

    public void setSpouse(UUID spouse) {
        this.spouse = spouse;
    }

    public void setChildren(Set<UUID> children) {
        this.children = children;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
    }

    public boolean addChild(UUID childUuid) {
        if (childUuid == null) {
            return false; // Перевірка на null для вхідного параметра
        }

        // Ініціалізуємо множину, якщо вона ще не була створена
        if (children == null) {
            children = new HashSet<>();
        }

        // Перевіряємо, чи дитина вже є у списку
        if (children.contains(childUuid)) {
            return false; // Дитина вже є у списку
        }

        // Додаємо дитину до множини
        children.add(childUuid);
        return true; // Дитина успішно додана
    }

    public Map<ActionsPermissions, AbstractPermission> getPermissionsMap() {
		return permissionsMap;
	}

    public void setPermissionsMap(Map<ActionsPermissions, AbstractPermission> permissionsMap) {
		this.permissionsMap = permissionsMap;
	}

    // Метод для додавання дозволів
    public void addPermission(ActionsPermissions action, AbstractPermission permission) {
        permissionsMap.put(action, permission);
    }

    // Метод для отримання дозволів
    public AbstractPermission getPermission(ActionsPermissions action) {
        if (!permissionsMap.containsKey(action)) {
            AbstractPermission newPermission = PermissionManager.createPermission(action, this);
            if (newPermission != null) {
                permissionsMap.put(action, newPermission);
            }
        }
        return permissionsMap.get(action);
    }

    // Метод для видалення дозволів
    public void removePermission(ActionsPermissions action) {
        permissionsMap.remove(action);
    }

    public boolean isFamilyMember(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return uuid.equals(father) || uuid.equals(mother) || children.contains(uuid);
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        String str = gson.toJson(this);
        return str;
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlayerFamily other = (PlayerFamily) obj;
        return Objects.equals(root, other.root);
    }

    public String getRootrNickName() {
        OfflinePlayer player = Bukkit.getPlayer(this.root);
        if (player == null) player = Bukkit.getOfflinePlayer(this.root);
        return (player != null) ? player.getName() : "Unknown";
    }
}

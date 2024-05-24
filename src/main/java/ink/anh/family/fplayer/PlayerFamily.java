package ink.anh.family.fplayer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import com.google.gson.Gson;

import ink.anh.family.gender.Gender;

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
    private UUID parentFamilyId = null;
    private Set<UUID> childFamilyIds = new HashSet<>();
    private UUID dynastyId = null;

    public PlayerFamily(UUID root, Gender gender, String displayName, String[] lastName, String[] oldLastName, UUID father, UUID mother, UUID spouse, 
    		Set<UUID> children) {
    	
        this.root = root;
        this.gender = gender;
        this.loverCaseName = displayName != null && displayName.length() > 0 ? displayName : getRootrNickName();
        this.lastName = lastName;
        this.oldLastName = oldLastName;
        this.father = father;
        this.mother = mother;
        this.spouse = spouse;
        this.children = children;
    }

    public PlayerFamily(UUID root, Gender gender, String loverCaseName, String[] lastName, String[] oldLastName, UUID father, UUID mother, UUID spouse,
    		Set<UUID> children, UUID familyId, UUID parentFamilyId, Set<UUID> childFamilyIds, UUID dynastyId) {
    	
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
        this.parentFamilyId = parentFamilyId;
        this.dynastyId = dynastyId;
        this.childFamilyIds = childFamilyIds;
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

    public UUID getParentFamilyId() {
        return parentFamilyId;
    }

    public void setParentFamilyId(UUID parentFamilyId) {
        this.parentFamilyId = parentFamilyId;
    }

    public UUID getDynastyId() {
        return dynastyId;
    }

    public void setDynastyId(UUID dynastyId) {
        this.dynastyId = dynastyId;
    }

    public Set<UUID> getChildFamilyIds() {
        return childFamilyIds;
    }

    public void setChildFamilyIds(Set<UUID> childFamilyIds) {
        this.childFamilyIds = childFamilyIds;
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

    public boolean addChildFamilyId(UUID childFamilyId) {
        if (childFamilyId == null) {
            return false; // Перевірка на null для вхідного параметра
        }

        // Ініціалізуємо множину, якщо вона ще не була створена
        if (childFamilyIds == null) {
            childFamilyIds = new HashSet<>();
        }

        // Перевіряємо, чи родина дитини вже є у списку
        if (childFamilyIds.contains(childFamilyId)) {
            return false; // Родина дитини вже є у списку
        }

        // Додаємо родину дитини до множини
        childFamilyIds.add(childFamilyId);
        return true; // Родина дитини успішно додана
    }

    public FamilyRelationType checkUUIDRelation(UUID uuid) {
        if (uuid == null) {
            return FamilyRelationType.NOT_FOUND;
        }
        if (uuid.equals(familyId)) {
            return FamilyRelationType.FAMILY_ID;
        }
        if (uuid.equals(parentFamilyId)) {
            return FamilyRelationType.PARENT_FAMILY_ID;
        }
        if (childFamilyIds.contains(uuid)) {
            return FamilyRelationType.CHILD_FAMILY_IDS;
        }
        if (uuid.equals(dynastyId)) {
            return FamilyRelationType.DYNASTY_ID;
        }
        return FamilyRelationType.NOT_FOUND;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        String str = gson.toJson(this);
        return str;
    }

    public static String uuidSetToString(Set<UUID> uuidSet) {
        return uuidSet.stream()
                      .map(UUID::toString)
                      .collect(Collectors.joining(","));
    }

    public static Set<UUID> stringToUuidSet(String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(uuidString.split(","))
                     .filter(str -> !str.isEmpty())
                     .map(str -> {
                         try {
                             return UUID.fromString(str);
                         } catch (IllegalArgumentException e) {
                             return null;
                         }
                     })
                     .filter(Objects::nonNull)
                     .collect(Collectors.toSet());
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

package ink.anh.family.common;

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

public class Family {

	private UUID root;
    private Gender gender; 
	private String displayName;
	private String[] lastName = new String[2];
	private String[] oldLastName = new String[2];
	private UUID father;
	private UUID mother;
	private UUID spouse;
	private Set<UUID> children = new HashSet<>();
	
	public Family(UUID root, Gender gender, String displayName, String[] lastName, String[] oldLastName, UUID father, UUID mother, UUID spouse, Set<UUID> children) {
		this.root = root;
		this.gender = gender;
		this.displayName = displayName != null && displayName.length() > 0 ? displayName : getRootrNickName();
		this.lastName = lastName;
		this.oldLastName = oldLastName;
		this.father = father;
		this.mother = mother;
		this.spouse = spouse;
		this.children = children;
	}
	
	public static Family getMyFamily(String str) {
		if (str == null || str.isEmpty()) {
			return null;
		}
		Gson gson = new Gson();
		Family fam = gson.fromJson(str , Family.class);
		return fam;
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

	public String getDisplayName() {
		return displayName;
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

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setLastName(String []lastName) {
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
		Family other = (Family) obj;
		return Objects.equals(root, other.root);
	}

	public String getRootrNickName() {
        OfflinePlayer player = Bukkit.getOfflinePlayer(this.root);
        return (player != null) ? player.getName() : "Unknown";
    }
}

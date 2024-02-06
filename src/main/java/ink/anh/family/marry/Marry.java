package ink.anh.family.marry;

import java.util.UUID;

import org.bukkit.entity.Player;

public class Marry {

    private Player bride1;   // Player першого нареченого
    private Player bride2;   // Player другого нареченого
    private Player priest;   // Player священника
    private boolean consent1; // Згода першого нареченого
    private boolean consent2; // Згода другого нареченого
    private int surnameChoice; // Вибір прізвища
    private String[] chosenSurname; 

    public Marry(Player bride1, Player bride2, Player priest, int surnameChoice, String[] chosenSurname) {
        this.bride1 = bride1;
        this.bride2 = bride2;
        this.priest = priest;
        this.consent1 = false;
        this.consent2 = false;
        this.surnameChoice = surnameChoice;
        this.chosenSurname = chosenSurname;
    }

	public Player getBride1() {
		return bride1;
	}

	public Player getBride2() {
		return bride2;
	}

	public Player getPriest() {
		return priest;
	}

	public boolean isConsent1() {
		return consent1;
	}

	public boolean isConsent2() {
		return consent2;
	}
	
    public boolean areBothConsentsGiven() {
        return consent1 && consent2;
    }

	public void setConsent1(boolean consent1) {
		this.consent1 = consent1;
	}

	public void setConsent2(boolean consent2) {
		this.consent2 = consent2;
	}

	public int getSurnameChoice() {
		return surnameChoice;
	}

	public void setSurnameChoice(int surnameChoice) {
		this.surnameChoice = surnameChoice;
	}

	public String[] getChosenSurname() {
		return chosenSurname;
	}

	public void setChosenSurname(String[] chosenSurname) {
		this.chosenSurname = chosenSurname;
	}

	@Override
	public int hashCode() {
	    int result = 17;
	    UUID uuid1 = bride1 != null ? bride1.getUniqueId() : null;
	    UUID uuid2 = bride2 != null ? bride2.getUniqueId() : null;
	    
	    int result1 = 31 * result + (uuid1 != null ? uuid1.hashCode() : 0);
	    int result2 = 31 * result + (uuid2 != null ? uuid2.hashCode() : 0);

	    return result1 + result2;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
	        return true;
	    }
	    if (obj == null || getClass() != obj.getClass()) {
	        return false;
	    }

	    Marry other = (Marry) obj;
	    UUID uuid1 = bride1 != null ? bride1.getUniqueId() : null;
	    UUID uuid2 = bride2 != null ? bride2.getUniqueId() : null;
	    UUID otherUuid1 = other.getBride1() != null ? other.getBride1().getUniqueId() : null;
	    UUID otherUuid2 = other.getBride2() != null ? other.getBride2().getUniqueId() : null;

	    return (uuid1.equals(otherUuid1) && uuid2.equals(otherUuid2)) ||
	           (uuid1.equals(otherUuid2) && uuid2.equals(otherUuid1));
	}

	public boolean isParticipant(Object obj) {
		if (obj == null) {
			return false;
		}
		
		UUID uuid = null;
		
		if (obj instanceof UUID) {
			uuid = (UUID) obj;
		} else if (obj instanceof Player) {
			Player player = (Player) obj;
			uuid = player.getUniqueId();
		} else if (obj instanceof String) {
	        try {
	            uuid = UUID.fromString((String) obj);
	        } catch (IllegalArgumentException e) {
	            return false;
	        }
	    }
		
        if (uuid == null) {
            return false;
        }

        return (bride1 != null && bride1.getUniqueId().equals(uuid)) ||
               (bride2 != null && bride2.getUniqueId().equals(uuid));
    }
}

package ink.anh.family.marriage;

import java.util.UUID;

import org.bukkit.entity.Player;

public class MarryPublic extends MarryBase {

    private Player priest;   // Player священника
    private boolean consent1; // Згода першого нареченого
    private boolean consent2; // Згода другого нареченого
    private int surnameChoice; // Вибір прізвища

    public MarryPublic(Player bride1, Player bride2, Player priest, int surnameChoice, String[] chosenSurname) {
        super(bride1, bride2, chosenSurname);
        this.priest = priest;
        this.consent1 = false;
        this.consent2 = false;
        this.surnameChoice = surnameChoice;
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

    @Override
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

    @Override
    public int hashCode() {
        int result = super.hashCode();
        UUID uuid1 = getProposer() != null ? getProposer().getUniqueId() : null;
        UUID uuid2 = getReceiver() != null ? getReceiver().getUniqueId() : null;

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
        if (!super.equals(obj)) {
            return false;
        }

        MarryPublic other = (MarryPublic) obj;
        UUID uuid1 = getProposer() != null ? getProposer().getUniqueId() : null;
        UUID uuid2 = getReceiver() != null ? getReceiver().getUniqueId() : null;
        UUID otherUuid1 = other.getProposer() != null ? other.getProposer().getUniqueId() : null;
        UUID otherUuid2 = other.getReceiver() != null ? other.getReceiver().getUniqueId() : null;

        return (uuid1.equals(otherUuid1) && uuid2.equals(otherUuid2)) ||
               (uuid1.equals(otherUuid2) && uuid2.equals(otherUuid1));
    }
}

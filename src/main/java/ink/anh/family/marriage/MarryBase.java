package ink.anh.family.marriage;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;

public abstract class MarryBase {

    private Player proposer;   // Player, який пропонує одруження
    private Player receiver;   // Player, якому пропонують одруження
    private String[] chosenSurname; // Обране прізвище

    public MarryBase(Player proposer, Player receiver, String[] chosenSurname) {
        this.proposer = proposer;
        this.receiver = receiver;
        this.chosenSurname = chosenSurname;
    }

    public Player getProposer() {
        return proposer;
    }

    public Player getReceiver() {
        return receiver;
    }

    public String[] getChosenSurname() {
        return chosenSurname;
    }

    public void setChosenSurname(String[] chosenSurname) {
        this.chosenSurname = chosenSurname;
    }

    @Override
    public int hashCode() {
        return Objects.hash(proposer);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MarryBase other = (MarryBase) obj;
        return Objects.equals(proposer, other.proposer);
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

        return (proposer != null && proposer.getUniqueId().equals(uuid)) ||
               (receiver != null && receiver.getUniqueId().equals(uuid));
    }

    public abstract boolean areBothConsentsGiven();
}

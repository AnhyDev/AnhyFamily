package ink.anh.family.marriage;

import java.util.Objects;

import org.bukkit.entity.Player;

public class MarryPrivate {

    private Player proposer;   // Player, який пропонує одруження
    private Player receiver;   // Player, якому пропонують одруження
    private String[] chosenSurname; // Обране прізвище

    public MarryPrivate(Player proposer, Player receiver, String[] chosenSurname) {
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
		MarryPrivate other = (MarryPrivate) obj;
		return Objects.equals(proposer, other.proposer);
	}
    
}

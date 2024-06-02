package ink.anh.family.marriage;

import org.bukkit.entity.Player;

public class MarryPrivate extends MarryBase {

    public MarryPrivate(Player proposer, Player receiver, String[] chosenSurname) {
        super(proposer, receiver, chosenSurname);
    }

    @Override
    public boolean areBothConsentsGiven() {
        return true;
    }
}

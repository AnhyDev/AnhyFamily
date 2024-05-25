package ink.anh.family.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fplayer.PlayerFamily;

public class DivorceEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player1;
    private final Player player2;
    private final PlayerFamily playerFamily1;
    private final PlayerFamily playerFamily2;
    private final FamilyDetails familyDetails;
    private final ActionInitiator initiator;
    private boolean isCancelled;

    public DivorceEvent(Player player1, Player player2, PlayerFamily playerFamily1, PlayerFamily playerFamily2, FamilyDetails familyDetails, ActionInitiator initiator) {
        this.player1 = player1;
        this.player2 = player2;
        this.playerFamily1 = playerFamily1;
        this.playerFamily2 = playerFamily2;
        this.familyDetails = familyDetails;
        this.initiator = initiator;
        this.isCancelled = false;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public PlayerFamily getPlayerFamily1() {
        return playerFamily1;
    }

    public PlayerFamily getPlayerFamily2() {
        return playerFamily2;
    }

	public FamilyDetails getFamilyDetails() {
		return familyDetails;
	}

    public ActionInitiator getInitiator() {
        return initiator;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

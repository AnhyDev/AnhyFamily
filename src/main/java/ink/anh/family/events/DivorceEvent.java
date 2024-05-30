package ink.anh.family.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fplayer.PlayerFamily;

public class DivorceEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerFamily initiator;
    private final PlayerFamily spouse;
    private final FamilyDetails familyDetails;
    private final ActionInitiator initiatorAction;
    private boolean isCancelled;

    public DivorceEvent(PlayerFamily initiator, PlayerFamily spouse, FamilyDetails familyDetails, ActionInitiator initiatorAction) {
    	
        this.initiator = initiator;
        this.spouse = spouse;
        this.familyDetails = familyDetails;
        this.initiatorAction = initiatorAction;
        this.isCancelled = false;
    }

    public PlayerFamily getInitiator() {
        return initiator;
    }

    public PlayerFamily getSpouse() {
        return spouse;
    }

	public FamilyDetails getFamilyDetails() {
		return familyDetails;
	}

    public ActionInitiator getInitiatorAction() {
        return initiatorAction;
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

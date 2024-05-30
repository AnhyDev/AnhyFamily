package ink.anh.family.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fplayer.PlayerFamily;

public class FamilySeparationEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerFamily playerFamily;
    private final FamilyDetails familyDetails;
    private final ActionInitiator initiator;
    private boolean isCancelled;

    public FamilySeparationEvent(PlayerFamily playerFamily, FamilyDetails familyDetails, ActionInitiator initiator) {
        this.playerFamily = playerFamily;
        this.familyDetails = familyDetails;
        this.initiator = initiator;
        this.isCancelled = false;
    }

    public PlayerFamily getPlayerFamily() {
        return playerFamily;
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

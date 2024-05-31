package ink.anh.family.events;

import java.util.Set;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fplayer.PlayerFamily;

/**
 * Event that occurs when a family separation happens.
 * This event is triggered when a family is separated for any reason,
 * and it provides details about the separation including the affected family,
 * the details of the family, the modified families, the reason for the separation,
 * and the initiator of the action.
 */
public class FamilySeparationEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerFamily playerFamily;
    private final FamilyDetails familyDetails;
    private final Set<PlayerFamily> modifiedFamilies;
    private final FamilySeparationReason separationReason;
    private final ActionInitiator initiator;
    private boolean isCancelled;

    /**
     * Constructs a new FamilySeparationEvent.
     * 
     * @param playerFamily the family involved in the separation
     * @param familyDetails the details of the family
     * @param modifiedFamilies the set of modified families due to the separation
     * @param separationReason the reason for the separation
     * @param initiator the initiator of the action
     */
    public FamilySeparationEvent(PlayerFamily playerFamily, FamilyDetails familyDetails, Set<PlayerFamily> modifiedFamilies,
            FamilySeparationReason separationReason, ActionInitiator initiator) {
        this.playerFamily = playerFamily;
        this.familyDetails = familyDetails;
        this.modifiedFamilies = modifiedFamilies;
        this.separationReason = separationReason;
        this.initiator = initiator;
        this.isCancelled = false;
    }

    /**
     * Gets the family involved in the separation.
     * 
     * @return the player family
     */
    public PlayerFamily getPlayerFamily() {
        return playerFamily;
    }

    /**
     * Gets the details of the family.
     * 
     * @return the family details
     */
    public FamilyDetails getFamilyDetails() {
        return familyDetails;
    }

    /**
     * Gets the set of modified families due to the separation.
     * If the {@code separationReason} is {@code FamilySeparationReason.FULL_SEPARATION}, 
     * then this set is informational only, and any modifications to it will not affect the final result.
     * 
     * @return the modified families
     */
    public Set<PlayerFamily> getModifiedFamilies() {
        return modifiedFamilies;
    }

    /**
     * Gets the reason for the separation.
     * 
     * @return the separation reason
     */
    public FamilySeparationReason getSeparationReason() {
        return separationReason;
    }

    /**
     * Gets the initiator of the action.
     * 
     * @return the initiator
     */
    public ActionInitiator getInitiator() {
        return initiator;
    }

    /**
     * Checks if the event is cancelled.
     * 
     * @return true if the event is cancelled, false otherwise
     */
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Sets the cancellation state of this event.
     * 
     * @param isCancelled true to cancel the event, false to uncancel
     */
    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    /**
     * Gets the list of handlers for this event.
     * 
     * @return the handlers
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the static handler list for this event.
     * 
     * @return the handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

package ink.anh.family.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fplayer.PlayerFamily;

public class AdoptionEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player parent1;
    private final Player parent2;
    private final Player child;
    private final PlayerFamily parentFamily1;
    private final PlayerFamily parentFamily2;
    private final PlayerFamily childFamily;
    private final FamilyDetails familyDetails;
    private final ActionInitiator initiator;
    private boolean isCancelled;

    public AdoptionEvent(Player parent1, Player parent2, Player child, PlayerFamily parentFamily1, PlayerFamily parentFamily2, PlayerFamily childFamily,
    		FamilyDetails familyDetails, ActionInitiator initiator) {
        this.parent1 = parent1;
        this.parent2 = parent2;
        this.child = child;
        this.parentFamily1 = parentFamily1;
        this.parentFamily2 = parentFamily2;
        this.childFamily = childFamily;
        this.familyDetails = familyDetails;
        this.initiator = initiator;
        this.isCancelled = false;
    }

    public Player getParent1() {
        return parent1;
    }

    public Player getParent2() {
        return parent2;
    }

    public Player getChild() {
        return child;
    }

    public PlayerFamily getParentFamily1() {
        return parentFamily1;
    }

    public PlayerFamily getParentFamily2() {
        return parentFamily2;
    }

    public PlayerFamily getChildFamily() {
        return childFamily;
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

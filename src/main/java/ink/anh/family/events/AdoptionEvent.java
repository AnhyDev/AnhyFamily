package ink.anh.family.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fplayer.PlayerFamily;

public class AdoptionEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerFamily[] adoptersFamily;
    private final PlayerFamily adoptedFamily;
    private final FamilyDetails[] adoptersFamilyDetails;
    private final FamilyDetails adoptedFamilyDetails;
    private final ActionInitiator initiator;
    private boolean isCancelled;

    public AdoptionEvent(PlayerFamily[] adoptersFamily, PlayerFamily adoptedFamily,
    		FamilyDetails[] adoptersDetails, FamilyDetails adoptedDetails, ActionInitiator initiator) {
        this.adoptersFamily = adoptersFamily;
        this.adoptedFamily = adoptedFamily;
        this.adoptersFamilyDetails = adoptersDetails;
        this.adoptedFamilyDetails = adoptedDetails;
        this.initiator = initiator;
        this.isCancelled = false;
    }

    public PlayerFamily[] getAdoptersFamily() {
        return adoptersFamily;
    }

    public PlayerFamily getAdoptedFamily() {
        return adoptedFamily;
    }

	public FamilyDetails[] getAdoptersFamilyDetails() {
		return adoptersFamilyDetails;
	}

	public FamilyDetails getAdoptedFamilyDetails() {
		return adoptedFamilyDetails;
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

package ink.anh.family.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ink.anh.family.fplayer.PlayerFamily;

public class GenderSelectEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final ActionInitiator initiator;
    private final PlayerFamily playerFamily;
    private boolean isCancelled;

    public GenderSelectEvent(PlayerFamily playerFamily, String gender, ActionInitiator initiator) {
        this.initiator = initiator;
        this.playerFamily = playerFamily;
        this.isCancelled = false;
    }

    public ActionInitiator getInitiator() {
        return initiator;
    }

    public PlayerFamily getPlayerFamily() {
        return playerFamily;
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

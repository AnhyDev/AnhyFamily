package ink.anh.family.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FamilyBreakEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ActionInitiator initiator;
    private boolean isCancelled;

    public FamilyBreakEvent(Player player, ActionInitiator initiator) {
        this.player = player;
        this.initiator = initiator;
        this.isCancelled = false;
    }

    public Player getPlayer1() {
        return player;
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

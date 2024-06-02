package ink.anh.family.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ink.anh.family.fplayer.PlayerFamily;

public class MarriageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player priest;
    private final PlayerFamily playerFamily1;
    private final PlayerFamily playerFamily2;
    private final ActionInitiator initiator;
    private boolean isCancelled;
    private String cancellationReason; // Причина переривання події

    public MarriageEvent(Player priest, PlayerFamily playerFamily1, PlayerFamily playerFamily2, ActionInitiator initiator) {
        this.priest = priest;
        this.playerFamily1 = playerFamily1;
        this.playerFamily2 = playerFamily2;
        this.initiator = initiator;
        this.isCancelled = false;
        this.cancellationReason = "";
    }

    public Player getPriest() {
        return priest;
    }

    public PlayerFamily getPlayerFamily1() {
        return playerFamily1;
    }

    public PlayerFamily getPlayerFamily2() {
        return playerFamily2;
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

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void cancellEvent(String cancellationReason) {
        this.isCancelled = true;
        this.cancellationReason = cancellationReason;
    }

    public void restoreEvent(String reason) {
        this.isCancelled = false;
        this.cancellationReason = reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

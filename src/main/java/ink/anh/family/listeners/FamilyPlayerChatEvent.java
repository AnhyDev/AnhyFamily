package ink.anh.family.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ink.anh.family.AnhyFamily;

public class FamilyPlayerChatEvent implements Listener {


	 AnhyFamily familiPlugin;

	public FamilyPlayerChatEvent(AnhyFamily familiPlugin) {
		this.familiPlugin = familiPlugin;
	}

	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
		;
	}
}

package ink.anh.family.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ink.anh.family.AnhyFamily;
import ink.anh.family.marriage.ActionsBridesPublic;

public class AnswerBridesChatListener implements Listener {


	private AnhyFamily familiPlugin;

	public AnswerBridesChatListener(AnhyFamily familiPlugin) {
		this.familiPlugin = familiPlugin;
	}

	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
		new ActionsBridesPublic(familiPlugin).accept(event);
	}
}

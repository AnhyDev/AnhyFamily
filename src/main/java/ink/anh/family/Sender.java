package ink.anh.family;

import org.bukkit.command.CommandSender;

import ink.anh.api.messages.MessageChat;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;

public abstract class Sender {

	public AnhyFamily familiPlugin;

	public Sender(AnhyFamily familiPlugin) {
		this.familiPlugin = familiPlugin;
	}

	public void sendMessage(MessageForFormatting textForFormatting, MessageType type, CommandSender... senders) {
		sendMessage(textForFormatting, type, true, senders);
	}

	public void sendMessage(MessageForFormatting textForFormatting, MessageType type, boolean addPluginName, CommandSender... senders) {
	    for (CommandSender sender : senders) {
	    	if (sender != null) {
	    		MessageChat.sendMessage(familiPlugin.getGlobalManager(), sender, textForFormatting, type, addPluginName);
	    	}
	    }
	}
}

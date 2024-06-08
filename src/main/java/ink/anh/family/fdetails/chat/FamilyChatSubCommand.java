package ink.anh.family.fdetails.chat;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

import ink.anh.family.AnhyFamily;

public class FamilyChatSubCommand {
	
    private AnhyFamily familiPlugin;

    public FamilyChatSubCommand(AnhyFamily familiPlugin) {
        this.familiPlugin = familiPlugin;
    }

    public boolean onCommand(Player player, String[] args) {

        CompletableFuture.runAsync(() -> {
            FamilyChatManager chatManager = new FamilyChatManager(familiPlugin, player, args);
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                case "other":
                case "o":
                    chatManager.sendMessageToOtherFamily();
                    break;
                case "access":
                    chatManager.setChatAccess();
                    break;
                case "default":
                    chatManager.setDefaultChatAccess();
                    break;
                default:
                    chatManager.sendMessage();
                }
            } else {
                chatManager.sendMessage();
            }
        });

        return true;
    }
}

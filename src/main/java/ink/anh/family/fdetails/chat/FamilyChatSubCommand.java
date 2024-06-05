package ink.anh.family.fdetails.chat;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

import ink.anh.api.messages.Sender;
import ink.anh.family.GlobalManager;

public class FamilyChatSubCommand extends Sender {

    public FamilyChatSubCommand() {
        super(GlobalManager.getInstance());
    }

    public boolean onCommand(Player player, String[] args) {

        CompletableFuture.runAsync(() -> {
            FamilyChatManager chatManager = new FamilyChatManager(player, args);
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

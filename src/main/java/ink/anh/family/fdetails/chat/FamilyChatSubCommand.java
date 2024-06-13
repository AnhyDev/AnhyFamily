package ink.anh.family.fdetails.chat;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;

public class FamilyChatSubCommand extends Sender {
	
    private AnhyFamily familiPlugin;

    public FamilyChatSubCommand(AnhyFamily familiPlugin) {
        super(GlobalManager.getInstance());
        this.familiPlugin = familiPlugin;
    }

    public boolean onCommand(Player player, String[] args) {

        CompletableFuture.runAsync(() -> {
            FamilyChatManager chatManager = new FamilyChatManager(familiPlugin, player, args);
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                case "access":
                    chatManager.setChatAccess();
                    break;
                case "default":
                    chatManager.setDefaultChatAccess();
                    break;
                case "check":
                	chatManager.checkAccess();
                    break;
                default:
                    chatManager.sendMessageWithConditions();
                }
            } else {
            	String commandUsage = "\n| /fchat access <args> \n| /fchat default <args> \n| /fchat <message> \n| /fchat #<RPEFIX> <message> \n| /fchat @<NickName> <message> \n| /fchat check <NickName>";
                sendMessage(new MessageForFormatting("family_err_command_format", 
                		new String[] {commandUsage}), MessageType.WARNING, player);
            }
        });

        return true;
    }
}

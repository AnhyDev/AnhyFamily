package ink.anh.family.fdetails.hugs;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;

public class FamilyHugsSubCommand extends Sender {
    
    private AnhyFamily familyPlugin;

    public FamilyHugsSubCommand(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
    }

    public boolean onCommand(Player player, Command cmd, String[] args) {
        CompletableFuture.runAsync(() -> {
            try {
                FamilyHugsManager hugsManager = new FamilyHugsManager(familyPlugin, player, cmd, args);
                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                        case "access":
                            hugsManager.setHugsAccess();
                            break;
                        case "default":
                            hugsManager.setDefaultHugsAccess();
                            break;
                        case "check":
                            hugsManager.checkHugsAccess();
                            break;
                        case "defaultcheck":
                            hugsManager.checkDefaultHugsAccess();
                            break;
                        default:
                            hugsManager.sendMessageWithConditions();
                    }
                } else {
                    String commandUsage = "\n| /fhugs access <args> \n| /fhugs default <args> \n| /fhugs <message> \n| /fhugs #<PREFIX> <message> \n| /fhugs @<NickName> <message> \n| /fhugs check <NickName> \n| /fhugs defaultcheck <children|parents>";
                    sendMessage(new MessageForFormatting("family_err_command_format", new String[]{commandUsage}), MessageType.WARNING, player);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return true;
    }
}

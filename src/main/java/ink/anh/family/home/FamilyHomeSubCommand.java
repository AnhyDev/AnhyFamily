package ink.anh.family.home;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.api.messages.MessageForFormatting;

public class FamilyHomeSubCommand extends Sender {
	
    private AnhyFamily familyPlugin;

    public FamilyHomeSubCommand(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
    }

    public boolean onCommand(Player player, String[] args) {

        CompletableFuture.runAsync(() -> {
            FamilyHomeManager homeManager = new FamilyHomeManager(familyPlugin, player, args);
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                case "set":
                    homeManager.setHome();
                    break;
                case "accept":
                    homeManager.setAccept();
                    break;
                case "tp":
                    homeManager.tpHome();
                    break;
                case "child":
                    homeManager.childHome();
                    break;
                case "parent":
                    homeManager.parentHome();
                    break;
                case "access":
                	homeManager.setHomeAccess();
                    break;
                case "default":
                    homeManager.setDefaultHomeAccess();
                    break;
                default:
                    sendMessage(new MessageForFormatting("family_err_command_format /fhome [set|tp|child|parent|access|default]", new String[] {}), MessageType.WARNING, player);
                }
            } else {
                homeManager.tpHome();
            }
        });

        return true;
    }
}

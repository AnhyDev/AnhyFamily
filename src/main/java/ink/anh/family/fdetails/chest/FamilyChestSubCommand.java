package ink.anh.family.fdetails.chest;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;

public class FamilyChestSubCommand extends Sender {

    private AnhyFamily familyPlugin;

    public FamilyChestSubCommand(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
    }

    public boolean onCommand(Player player, String[] args) {

        CompletableFuture.runAsync(() -> {
        	FamilyChestManager chestManager = new FamilyChestManager(familyPlugin, player, args);
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                case "set":
                	chestManager.setChestLocation();
                    break;
                case "accept":
                	chestManager.setAcceptChestLocation();
                    break;
                case "open":
                	chestManager.openChest();
                    break;
                case "other":
                case "o":
                	chestManager.openChestForOtherFamily();
                    break;
                case "access":
                	chestManager.setChestAccess();
                    break;
                case "default":
                	chestManager.setChestAccessDefault();
                    break;
                default:
                	chestManager.openChest();
                }
            } else {
                ;
            }
        });

        return true;
    }
}

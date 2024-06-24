package ink.anh.family.fdetails.chest;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import ink.anh.family.AnhyFamily;

public class FamilyChestSubCommand {

    private AnhyFamily familyPlugin;

    public FamilyChestSubCommand(AnhyFamily familyPlugin) {
        this.familyPlugin = familyPlugin;
    }

    public boolean onCommand(Player player, Command cmd, String[] args) {

        CompletableFuture.runAsync(() -> {
            try {
                FamilyChestManager chestManager = new FamilyChestManager(familyPlugin, player, cmd, args);
                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                        case "set":
                            chestManager.setChest();
                            break;
                        case "accept":
                            chestManager.requestAccept();
                            break;
                        case "refuse":
                        	chestManager.requestRejected();
                            break;
                        case "access":
                            chestManager.setChestAccess();
                            break;
                        case "default":
                            chestManager.setChestAccessDefault();
                            break;
                        case "check":
                            chestManager.checkAccess();
                            break;
                        case "defaultcheck":
                            chestManager.checkDefaultAccess();
                            break;
                        default:
                            chestManager.openChestWithConditions();
                    }
                } else {
                    chestManager.openChestWithConditions();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return true;
    }
}

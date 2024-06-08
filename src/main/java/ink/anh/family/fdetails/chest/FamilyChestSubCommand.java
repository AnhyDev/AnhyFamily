package ink.anh.family.fdetails.chest;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

import ink.anh.family.AnhyFamily;

public class FamilyChestSubCommand {

    private AnhyFamily familyPlugin;

    public FamilyChestSubCommand(AnhyFamily familyPlugin) {
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
                case "access":
                    chestManager.setChestAccess();
                    break;
                case "default":
                    chestManager.setChestAccessDefault();
                    break;
                default:
                    chestManager.openChestWithConditions();
                }
            } else {
                chestManager.openChest();
            }
        });

        return true;
    }
}

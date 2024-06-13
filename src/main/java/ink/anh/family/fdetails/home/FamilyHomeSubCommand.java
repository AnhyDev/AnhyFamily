package ink.anh.family.fdetails.home;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;

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
                    case "access":
                        homeManager.setHomeAccess();
                        break;
                    case "default":
                        homeManager.setDefaultHomeAccess();
                        break;
                    case "check":
                    	homeManager.checkAccess();
                        break;
                    default:
                        homeManager.tpHomeWithConditions();
                }
            } else {
                homeManager.tpHomeWithConditions();
            }
        });

        return true;
    }
}

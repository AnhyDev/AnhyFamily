package ink.anh.family.fdetails.home;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
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

    public boolean onCommand(Player player, Command cmd, String[] args) {

        CompletableFuture.runAsync(() -> {
            try {
                FamilyHomeManager homeManager = new FamilyHomeManager(familyPlugin, player, cmd, args);
                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                        case "set":
                            homeManager.setHome();
                            break;
                        case "accept":
                            homeManager.requestAccept();
                            break;
                        case "refuse":
                            homeManager.requestRejected();
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
                        case "defaultcheck":
                            homeManager.checkDefaultAccess();
                            break;
                        default:
                            homeManager.tpHomeWithConditions();
                    }
                } else {
                    homeManager.tpHomeWithConditions();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return true;
    }
}

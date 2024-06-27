package ink.anh.family.fdetails.symbol;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;

public class FamilySymbolSubCommand extends Sender {

    private AnhyFamily familyPlugin;

    public FamilySymbolSubCommand(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
    }

    public boolean onCommand(Player player, Command cmd, String[] args) {

        CompletableFuture.runAsync(() -> {
            try {
            	FamilySymbolManager symbolManager = new FamilySymbolManager(familyPlugin, player, cmd, args);
                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                        case "set":
                        	symbolManager.setSymbol();
                            break;
                        case "accept":
                        	symbolManager.acceptSymbol();
                            break;
                        case "refuse":
                        	symbolManager.rejectSymbolRequest();
                            break;
                        default:
                        	symbolManager.getPrefix();
                    }
                } else {
                	symbolManager.getPrefix();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return true;
    }
}

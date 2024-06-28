package ink.anh.family.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.hugs.FamilyHugsManager;

public class PlayerInteractionListener implements Listener {

    private final AnhyFamily familyPlugin;

    public PlayerInteractionListener(AnhyFamily familyPlugin) {
        this.familyPlugin = familyPlugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer();
            Player target = (Player) event.getRightClicked();
            
            if (!(new FamilyHugsManager(familyPlugin, player, null, new String[] {}).tryHug(target))) {
            	//Logger.info(familyPlugin, "FamilyHugsManager false");
            } else {
            	//Logger.info(familyPlugin, "FamilyHugsManager true");
            }
        }
    }
}

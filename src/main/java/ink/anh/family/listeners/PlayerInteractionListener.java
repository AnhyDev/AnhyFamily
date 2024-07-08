package ink.anh.family.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.hugs.FamilyHugsManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInteractionListener implements Listener {

    private final AnhyFamily familyPlugin;
    private final Map<UUID, Long> lastInteractionTimes = new HashMap<>();
    private final long debounceTime = 100; // 100 milliseconds debounce time

    public PlayerInteractionListener(AnhyFamily familyPlugin) {
        this.familyPlugin = familyPlugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();

            // Debounce check
            if (lastInteractionTimes.containsKey(playerId)) {
                long lastTime = lastInteractionTimes.get(playerId);
                if ((currentTime - lastTime) < debounceTime) {
                    return; // Ignore duplicate event
                }
            }

            lastInteractionTimes.put(playerId, currentTime);

            Player target = (Player) event.getRightClicked();
            
            boolean iHugs = new FamilyHugsManager(familyPlugin, player, null, new String[] {}).tryHug(target);
            if (!iHugs) {
                //Logger.info(familyPlugin, "FamilyHugsManager false");
            } else {
                //Logger.info(familyPlugin, "FamilyHugsManager true");
            }
        }
    }
}

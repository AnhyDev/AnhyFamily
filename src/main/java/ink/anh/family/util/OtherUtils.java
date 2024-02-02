package ink.anh.family.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ink.anh.family.AnhyFamily;

public class OtherUtils {

	public static boolean isPlayerWithinRadius(Player player, Location center, double radius) {
		
		if (radius <= 0) {
			return true;
		}
		
	    Location playerLocation = player.getLocation();
	    World centerWorld = center.getWorld();

	    return playerLocation.getWorld().equals(centerWorld) && 
	           playerLocation.distance(center) <= radius;
	}

	public static Player[] getPlayersWithinRadius(Location center, double radius) {

	    if (radius <= 0) {
	        // Якщо радіус менше або дорівнює нулю, повертаємо всіх онлайн гравців
	        return Bukkit.getServer().getOnlinePlayers().toArray(new Player[0]);
	    }

	    List<Player> playersWithinRadius = new ArrayList<>();
	    World centerWorld = center.getWorld();

	    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
	        Location playerLocation = player.getLocation();

	        if (playerLocation.getWorld().equals(centerWorld) && 
	            playerLocation.distance(center) <= radius) {
	            playersWithinRadius.add(player);
	        }
	    }
	    return playersWithinRadius.toArray(new Player[0]);
	}
	
	public static void sendAsyncPlayerChatEvent(AnhyFamily plugin, Player player, String message, long delay) {
	    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
	        AsyncPlayerChatEvent chatEvent = new AsyncPlayerChatEvent(true, player, message, new HashSet<>(Bukkit.getOnlinePlayers()));
	        Bukkit.getPluginManager().callEvent(chatEvent);

	        if (!chatEvent.isCancelled()) {
	            String formattedMessage = String.format(chatEvent.getFormat(), chatEvent.getPlayer().getDisplayName(), chatEvent.getMessage());
	            for (Player recipient : chatEvent.getRecipients()) {
	                recipient.sendMessage("\n" + formattedMessage);
	            }
	        }
	    }, delay);
	}
}

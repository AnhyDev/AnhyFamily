package ink.anh.family.fdetails.hugs;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.PacketType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ZombieArmAnimator {

    private static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public static void setZombieArms(Player player, boolean active) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, player.getEntityId());

        WrappedDataWatcher watcher = new WrappedDataWatcher(player);
        WrappedDataWatcherObject obj = new WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class));

        byte b = 0x00;
        if (active) {
            b = 0x20; // флаг для "arms raised" як у зомбі
        }
        watcher.setObject(obj, b);
        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toggleZombieArms(Player player, long duration, JavaPlugin plugin) {
        setZombieArms(player, true);

        Bukkit.getScheduler().runTaskLater(plugin, () -> setZombieArms(player, false), duration);
    }
}

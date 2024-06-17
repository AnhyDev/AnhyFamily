package ink.anh.family.fdetails.chest;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import ink.anh.api.enums.Access;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.FamilyConfig;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.AbstractDetailsManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.TypeTargetComponent;
import ink.anh.family.util.FamilyUtils;

public class FamilyChestManager extends AbstractDetailsManager {

    private static Map<UUID, ChestRequest> chestRequests = new ConcurrentHashMap<>();
    private static final Map<Integer, UUID> locationToUUIDMap = new ConcurrentHashMap<>();

    public FamilyChestManager(AnhyFamily familyPlugin, Player player, Command cmd, String[] args) {
        super(familyPlugin, player, cmd, args);
    }

    @Override
    protected String getDefaultCommand() {
        return "fchest";
    }

    @Override
    protected String getInvalidAccessMessage() {
        return "family_err_no_access_chest";
    }

    @Override
    protected String getComponentAccessSetMessageKey(TypeTargetComponent component) {
        return "family_chest_access_set";
    }

    @Override
    protected String getDefaultAccessSetMessageKey(TypeTargetComponent component) {
        return "family_default_chest_access_set";
    }

    @Override
    protected String getDefaultAccessCheckMessageKey(TypeTargetComponent component) {
        return "family_default_chest_access_check";
    }

    @Override
    protected boolean canPerformAction(FamilyDetails details, Object additionalParameter) {
        return true;
    }

    @Override
    protected TypeTargetComponent getTypeTargetComponent() {
        return TypeTargetComponent.CHEST;
    }

    @Override
    protected void setComponentAccess(AccessControl accessControl, Access access, TypeTargetComponent component) {
        accessControl.setChestAccess(access);
    }

    @Override
    protected void performAction(FamilyDetails details) {
        if (!canOpenChest(details)) {
            return;
        }

        if (!isLocationWithinHomeRadius(details, details.getFamilyChest().getChestLocation())) {
            sendMessage(new MessageForFormatting("family_err_chest_home_distance", new String[] {}), MessageType.WARNING, player);
            return;
        }
        
        // Виконуємо основну дію
        FamilyChestOpenManager.getInstance().openFamilyChest(player, details);
    }
    
    public void setChest() {
        Block targetBlock = getTargetBlock(player, 5);

        if (targetBlock == null || !isAllowedChestBlock(targetBlock)) {
            sendMessage(new MessageForFormatting("family_err_invalid_chest_location", new String[] {}), MessageType.WARNING, player);
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            //Location homeLocation = details.getHomeLocation();
            Location targetLocation = targetBlock.getLocation();

            /*if (homeLocation == null) {
                sendMessage(new MessageForFormatting("family_err_home_not_set", new String[] {}), MessageType.WARNING, player);
                return;
            }*/

            if (isLocationWithinHomeRadius(details, targetLocation)) {
                PlayerFamily playerFamily = FamilyUtils.getFamily(player);
                UUID spouseUUID = playerFamily.getSpouse();
                if (spouseUUID == null) {
                    sendMessage(new MessageForFormatting("family_err_no_spouse", new String[] {}), MessageType.WARNING, player);
                    return;
                }

                chestRequests.put(details.getFamilyId(), new ChestRequest(targetLocation, player.getUniqueId()));
                sendMessage(new MessageForFormatting("family_chest_request_sent", new String[] {}), MessageType.NORMAL, player);
                
                if (playerFamily.getSpouse() != null) {
                    Player spouse = Bukkit.getPlayer(playerFamily.getSpouse());
                    if (spouse != null && spouse.isOnline()) {
                        sendMessage(new MessageForFormatting("family_chest_accept_sent", new String[] {"/" + command + " accept"}), MessageType.NORMAL, spouse);
                    }
                }

                // Запуск таймера на 60 секунд
                Bukkit.getScheduler().runTaskLater(familyPlugin, () -> {
                    if (chestRequests.containsKey(details.getFamilyId())) {
                        chestRequests.remove(details.getFamilyId());
                        sendMessage(new MessageForFormatting("family_err_request_chest_not_confirmed", new String[] {}), MessageType.WARNING, player);
                    }
                }, 1200L);
            } else {
                sendMessage(new MessageForFormatting("family_err_chest_home_distance", new String[] {}), MessageType.WARNING, player);
            }
        });
    }

    public void setAccept() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            UUID familyId = details.getFamilyId();
            ChestRequest request = chestRequests.get(familyId);
            if (request != null && !request.getRequesterUUID().equals(player.getUniqueId())) {
                // Видалення старого запису з мапи
                locationToUUIDMap.values().removeIf(uuid -> uuid.equals(familyId));

                // Додавання нового запису з новою локацією
                int newLocationHash = getLocationHash(request.getLocation());
                locationToUUIDMap.put(newLocationHash, familyId);

                // Оновлення локації скрині у FamilyDetails
                if (details.getFamilyChest() == null) {
                    Chest chest = new Chest();
                    details.setFamilyChest(chest);
                }
                details.getFamilyChest().setChestLocation(request.getLocation());
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.FAMILY_CHEST);
                chestRequests.remove(familyId);

                Player[] players = new Player[]{player, Bukkit.getPlayer(request.getRequesterUUID())};
                sendMessage(new MessageForFormatting("family_chest_set", new String[]{}), MessageType.NORMAL, players);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_pending_request", new String[]{}), MessageType.WARNING, player);
            }
        });
    }

    public void openChestWithConditions() {
        handleActionWithConditions();
    }

    public static boolean isLocationWithinHomeRadius(FamilyDetails details, Location location) {
        if (details == null || location == null) {
            return false;
        }
        
        Location homeLocation = details.getHomeLocation();
        
        if (homeLocation == null) {
            return false;
        }

        FamilyConfig config = GlobalManager.getInstance().getFamilyConfig();
        int radius = config.getChestDistanceToHome();

        if (!homeLocation.getWorld().equals(location.getWorld())) {
            return false;
        }

        double distance = homeLocation.distance(location);
        return distance <= radius;
    }

    private Block getTargetBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() != Material.AIR) {
                break;
            }
        }
        return lastBlock;
    }

    private boolean isAllowedChestBlock(Block block) {
        FamilyConfig config = GlobalManager.getInstance().getFamilyConfig();
        Material type = block.getType();
        return config.getChestBlocks().contains(type);
    }

    public void attemptOpenFamilyChest(Location location) {
        UUID familyId = getUUIDFromLocation(location);
        if (familyId == null) {
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getFamilyDetails(familyId), details -> {
            Chest familyChest = details.getFamilyChest();
            
            // Перевірка наявності сімейної скрині та її локації
            if (familyChest == null || familyChest.getChestLocation() == null) {
                sendMessage(new MessageForFormatting("family_err_chest_not_set", new String[]{}), MessageType.WARNING, player);
                return;
            }

            PlayerFamily playerFamily = FamilyUtils.getFamily(player);
            if (details.hasAccess(playerFamily, TypeTargetComponent.CHEST)) { 
            	if (!player.hasPermission(Permissions.FAMILY_CHEST_CLICK) && !GlobalManager.getInstance().getFamilyConfig().isChestClick()) {
                    sendMessage(new MessageForFormatting("family_err_chest_click_disabled", new String[]{}), MessageType.WARNING, player);
            		return;
            	}
                FamilyChestOpenManager.getInstance().openFamilyChest(player, details);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_access_chest", new String[]{}), MessageType.WARNING, player);
            }
        });
    }

    private boolean canOpenChest(FamilyDetails details) {
        FamilyConfig config = GlobalManager.getInstance().getFamilyConfig();
        Chest familyChest = details.getFamilyChest();

        // Перевірка наявності сімейної скрині та її локації
        if (familyChest == null || familyChest.getChestLocation() == null) {
            sendMessage(new MessageForFormatting("family_err_chest_not_set", new String[]{}), MessageType.WARNING, player);
            return false;
        }

        Location chestLocation = familyChest.getChestLocation();

        // Перевірка дистанції до скрині
        
        if (!player.hasPermission(Permissions.FAMILY_CHEST_IGNORE_DISTANCE) && config.getChestDistance() > 0) {
            if (player.getLocation().distance(chestLocation) > config.getChestDistance()) {
                sendMessage(new MessageForFormatting("family_err_chest_distance_restriction", new String[]{}), MessageType.WARNING, player);
                return false;
            }
        }

        // Перевірка світу, де знаходиться скриня
        if (!player.hasPermission(Permissions.FAMILY_CHEST_IGNORE_WORLD) && config.isChestWorld()) {
            if (!player.getWorld().equals(chestLocation.getWorld())) {
                sendMessage(new MessageForFormatting("family_err_chest_world_restriction", new String[]{}), MessageType.WARNING, player);
                return false;
            }
        }

        return true;
    }

    public void setChestAccess() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/" + command + " access <NickName> <allow|deny|default>"}), MessageType.WARNING, player);
            return;
        }
        setAccess(args[1], args[2], TypeTargetComponent.CHEST);
    }

    public void setChestAccessDefault() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/" + command + " default <children|parents> <allow|deny>"}), MessageType.WARNING, player);
            return;
        }
        setDefaultAccess(args[1], args[2], TypeTargetComponent.CHEST);
    }

    public void checkAccess() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/" + command + " check <NickName>"}), MessageType.WARNING, player);
            return;
        }
        checkAccess(args[1], TypeTargetComponent.CHEST);
    }

    public void checkDefaultAccess() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/" + command + " defaultcheck <children|parents>"}), MessageType.WARNING, player);
            return;
        }
        checkDefaultAccess(args[1], TypeTargetComponent.CHEST);
    }

    // Отримати хеш для мапи з локації
    public static int getLocationHash(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        int worldHash = location.getWorld().hashCode();
        return Objects.hash(worldHash, x, y, z);
    }

    // перевірити наявність локації у мапі
    public static boolean isFamilyChest(Location location) {
        return locationToUUIDMap.containsKey(getLocationHash(location));
    }

    // отримати сымейний UUID за локацыэю
    public static UUID getUUIDFromLocation(Location location) {
        if (location == null) {
            return null;
        }
        int locationHash = getLocationHash(location);
        return locationToUUIDMap.get(locationHash);
    }

    // заповнення мапи при старты сервера
    public static void setLocationToUUIDMap(Map<Integer, UUID> newMap) {
        locationToUUIDMap.clear();
        locationToUUIDMap.putAll(newMap);
    }

    private static class ChestRequest {
        private final UUID requesterUUID;
        private Location location;

        public ChestRequest(Location location, UUID requesterUUID) {
            this.requesterUUID = requesterUUID;
            this.location = location;
        }

        public Location getLocation() {
            return location;
        }

        public UUID getRequesterUUID() {
            return requesterUUID;
        }
    }
}

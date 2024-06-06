package ink.anh.family.fdetails.chest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import ink.anh.api.enums.Access;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsActionInterface;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.symbol.FamilySymbolManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;

public class FamilyChestManager extends Sender {

    private static Map<UUID, ChestRequest> chestRequests = new HashMap<>();
    private static final Map<Integer, UUID> locationToUUIDMap = new HashMap<>();

    private AnhyFamily familyPlugin;
    private Player player;
    private String[] args;

    public FamilyChestManager(AnhyFamily familyPlugin, Player player, String[] args) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
        this.player = player;
        this.args = args;
    }

    public void setChestLocation() {
        Block targetBlock = getTargetBlock(player, 5);
        
        if (targetBlock == null || !isSolidBlock(targetBlock)) {
            sendMessage(new MessageForFormatting("family_err_invalid_chest_location", new String[] {}), MessageType.WARNING, player);
            return;
        }
        
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            GlobalManager manager = (GlobalManager) libraryManager;
            Location homeLocation = details.getHomeLocation();
            Location targetLocation = targetBlock.getLocation();

            if (homeLocation == null) {
                sendMessage(new MessageForFormatting("family_err_home_not_set", new String[] {}), MessageType.WARNING, player);
                return;
            }

            if (targetLocation.distance(homeLocation) > 20) {
                sendMessage(new MessageForFormatting("family_err_chest_too_far_from_home", new String[] {}), MessageType.WARNING, player);
                return;
            }

            if (details.getFamilyChest().getChestLocation() == null || details.canChangeHome(manager.getFamilyConfig().getHomeChangeTimeoutMinutes())) {
                PlayerFamily playerFamily = FamilyUtils.getFamily(player);
                UUID spouseUUID = playerFamily.getSpouse();
                if (spouseUUID == null) {
                    sendMessage(new MessageForFormatting("family_err_no_spouse", new String[] {}), MessageType.WARNING, player);
                    return;
                }

                chestRequests.put(details.getFamilyId(), new ChestRequest(targetLocation, player.getUniqueId()));
                sendMessage(new MessageForFormatting("family_chest_request_sent", new String[] {}), MessageType.NORMAL, player);

                // Запуск таймера на 60 секунд
                Bukkit.getScheduler().runTaskLater(familyPlugin, () -> {
                    if (chestRequests.containsKey(details.getFamilyId())) {
                        chestRequests.remove(details.getFamilyId());
                        sendMessage(new MessageForFormatting("family_err_request_chest_not_confirmed", new String[] {}), MessageType.WARNING, player);
                    }
                }, 1200L); 
            } else {
                sendMessage(new MessageForFormatting("family_err_chest_already_set", new String[] {}), MessageType.WARNING, player);
            }
        });
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

    private boolean isSolidBlock(Block block) {
        Material type = block.getType();
        return type.isSolid() && type != Material.WATER && type != Material.LAVA;
    }

    public void setAcceptChestLocation() {
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
                details.getFamilyChest().setChestLocation(request.getLocation());
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.HOME_LOCATION);
                chestRequests.remove(familyId);

                Player[] players = new Player[] {player, Bukkit.getPlayer(request.getRequesterUUID())};
                sendMessage(new MessageForFormatting("family_chest_set", new String[] {}), MessageType.NORMAL, players);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_pending_request", new String[] {}), MessageType.WARNING, player);
            }
        });
    }

    // Метод для відкриття сімейної скрині
    public void openChest() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            PlayerFamily playerFamily = FamilyUtils.getFamily(player);
            if (details.hasAccessChest(playerFamily)) {
                FamilyChestOpenManager.getInstance().openFamilyChest(player, details);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_access_chest", new String[] {}), MessageType.WARNING, player);
            }
        });
    }

    // Метод для відкриття скрині іншої сім'ї
    public void openChestForOtherFamily() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fchest other <prefix>"}), MessageType.WARNING, player);
            return;
        }

        String symbol = args[1].toUpperCase();
        if (symbol.length() < 3 || symbol.length() > 6 || !symbol.matches("[A-Z]+")) {
            sendMessage(new MessageForFormatting("family_err_prefix_not_found", new String[] {}), MessageType.WARNING, player);
            return;
        }

        UUID familyId = FamilySymbolManager.getFamilyIdBySymbol(symbol);
        if (familyId == null) {
            sendMessage(new MessageForFormatting("family_err_symbol_not_found", new String[] {symbol}), MessageType.WARNING, player);
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(familyId), details -> {
            PlayerFamily playerFamily = FamilyUtils.getFamily(player);
            if (details.hasAccessChest(playerFamily)) {
                FamilyChestOpenManager.getInstance().openFamilyChest(player, details);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_access_chest", new String[] {symbol}), MessageType.WARNING, player);
            }
        });
    }
    public void setChestAccess() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fchest access <NickName> <allow|deny|default>"}), MessageType.WARNING, player);
            return;
        }
        String nickname = args[1];
        String accessArg = args[2].toLowerCase();
        Access access;
        switch (accessArg) {
            case "allow":
                access = Access.TRUE;
                break;
            case "deny":
                access = Access.FALSE;
                break;
            case "default":
                access = Access.DEFAULT;
                break;
            default:
                sendMessage(new MessageForFormatting("family_err_invalid_access", new String[] {accessArg}), MessageType.WARNING, player);
                return;
        }

        PlayerFamily targetFamily = FamilyUtils.getFamily(nickname);
        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_nickname_not_found", new String[] {nickname}), MessageType.WARNING, player);
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            UUID targetUUID = targetFamily.getRoot();
            Map<UUID, AccessControl> childrenAccessMap = details.getChildrenAccessMap();
            Map<UUID, AccessControl> ancestorsAccessMap = details.getAncestorsAccessMap();
            AccessControl accessControl = null;

            if (childrenAccessMap.containsKey(targetUUID)) {
                accessControl = childrenAccessMap.get(targetUUID);
            } else if (ancestorsAccessMap.containsKey(targetUUID)) {
                accessControl = ancestorsAccessMap.get(targetUUID);
            }

            if (accessControl != null) {
                accessControl.setChestAccess(access);
                if (childrenAccessMap.containsKey(targetUUID)) {
                    childrenAccessMap.put(targetUUID, accessControl);
                    FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.CHILDREN_ACCESS_MAP);
                } else {
                    ancestorsAccessMap.put(targetUUID, accessControl);
                    FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.ANCESTORS_ACCESS_MAP);
                }
                sendMessage(new MessageForFormatting("family_chest_access_set", new String[] {nickname, accessArg}), MessageType.NORMAL, player);
            } else {
                sendMessage(new MessageForFormatting("family_err_nickname_not_found_in_access_maps", new String[] {nickname}), MessageType.WARNING, player);
            }
        });
    }

    public void setChestAccessDefault() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fchest default <children|parents> <allow|deny>"}), MessageType.WARNING, player);
            return;
        }

        String targetGroup = args[1].toLowerCase();
        String accessArg = args[2].toLowerCase();
        Access access;
        switch (accessArg) {
            case "allow":
                access = Access.TRUE;
                break;
            case "deny":
                access = Access.FALSE;
                break;
            default:
                sendMessage(new MessageForFormatting("family_err_invalid_access", new String[] {accessArg}), MessageType.WARNING, player);
                return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            if ("children".equals(targetGroup)) {
                details.getChildrenAccess().setChestAccess(access);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.CHILDREN_ACCESS);
                sendMessage(new MessageForFormatting("family_default_chest_access_set", new String[] {"children", accessArg}), MessageType.NORMAL, player);
            } else if ("parents".equals(targetGroup)) {
                details.getAncestorsAccess().setChestAccess(access);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.ANCESTORS_ACCESS);
                sendMessage(new MessageForFormatting("family_default_chest_access_set", new String[] {"parents", accessArg}), MessageType.NORMAL, player);
            } else {
                sendMessage(new MessageForFormatting("family_err_invalid_group", new String[] {targetGroup}), MessageType.WARNING, player);
            }
        });
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

    private void executeWithFamilyDetails(FamilyDetails details, FamilyDetailsActionInterface action) {
        if (details != null) {
            action.execute(details);
        } else {
            sendMessage(new MessageForFormatting("family_err_details_not_found", new String[] {}), MessageType.WARNING, player);
        }
    }

    public static int getLocationHash(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        int worldHash = location.getWorld().hashCode();
        return Objects.hash(worldHash, x, y, z);
    }

    public static UUID getUUIDFromLocation(Location location) {
        int locationHash = getLocationHash(location);
        return locationToUUIDMap.get(locationHash);
    }

    public static void setLocationToUUIDMap(Map<Integer, UUID> newMap) {
        locationToUUIDMap.clear();
        locationToUUIDMap.putAll(newMap);
    }
}

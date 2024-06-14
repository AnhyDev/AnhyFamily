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
import ink.anh.api.messages.Logger;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Messenger;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.FamilyConfig;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsActionInterface;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.MessageComponentBuilder;
import ink.anh.family.fdetails.symbol.FamilySymbolManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.TypeTargetComponent;
import ink.anh.family.util.FamilyUtils;

public class FamilyChestManager extends Sender {

    private static Map<UUID, ChestRequest> chestRequests = new ConcurrentHashMap<>();
    private static final Map<Integer, UUID> locationToUUIDMap = new ConcurrentHashMap<>();

    private AnhyFamily familyPlugin;
    private Player player;
    private String command;
    private String[] args;

    public FamilyChestManager(AnhyFamily familyPlugin, Player player, Command cmd, String[] args) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
        this.player = player;
        this.command = cmd != null ? cmd.getName() : "fchest";
        this.args = args;
    }

    // Встановлення локації скрині
    public void setChestLocation() {
        Block targetBlock = getTargetBlock(player, 5);

        if (targetBlock == null || !isAllowedChestBlock(targetBlock)) {
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

    private boolean isAllowedChestBlock(Block block) {
        FamilyConfig config = GlobalManager.getInstance().getFamilyConfig();
        Material type = block.getType();
        return config.getChestBlocks().contains(type);
    }

    // Підтвердження встановлення локації скрині
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

                Player[] players = new Player[]{player, Bukkit.getPlayer(request.getRequesterUUID())};
                sendMessage(new MessageForFormatting("family_chest_set", new String[]{}), MessageType.NORMAL, players);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_pending_request", new String[]{}), MessageType.WARNING, player);
            }
        });
    }

    public void openChestWithConditions() {
        if (args.length == 0) {
            handleOpenChest(null, 0);
            return;
        }

        String firstArg = args[0];

        if (firstArg.startsWith("#")) {
            handleOpenChest(firstArg.substring(1).toUpperCase(), 1);
        } else if (firstArg.startsWith("@")) {
            handleOpenChest(firstArg.substring(1), 2);
        } else {
            handleOpenChest(null, 0);
        }
    }

    private void handleOpenChest(String key, int typeKey) {
        try {
            FamilyDetails familyDetails = null;
            String lowerCaseKey = key != null ? key.toLowerCase() : "null";

            switch (typeKey) {
                case 0:
                    familyDetails = FamilyDetailsGet.getRootFamilyDetails(player);
                    break;
                case 1:
                    if (key.length() < 3 || key.length() > 6 || !key.matches("[A-Z]+")) {
                        sendMessage(new MessageForFormatting("family_err_prefix_not_found", new String[]{key}), MessageType.WARNING, player);
                        return;
                    }

                    UUID familyId = FamilySymbolManager.getFamilyIdBySymbol(key);
                    if (familyId == null) {
                        sendMessage(new MessageForFormatting("family_err_symbol_not_found", new String[]{key}), MessageType.WARNING, player);
                        return;
                    }

                    familyDetails = FamilyDetailsGet.getRootFamilyDetails(familyId);
                    break;
                case 2:
                    Player targetPlayer = Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.getName().toLowerCase().equals(lowerCaseKey) || p.getDisplayName().toLowerCase().equals(lowerCaseKey))
                        .findFirst()
                        .orElse(null);

                    if (targetPlayer == null) {
                        sendMessage(new MessageForFormatting("family_hover_player_offline", new String[]{key}), MessageType.WARNING, player);
                        return;
                    }
                    familyDetails = FamilyDetailsGet.getRootFamilyDetails(targetPlayer);
                    break;
                default:
                    sendMessage(new MessageForFormatting("family_err_invalid_typekey", new String[]{String.valueOf(typeKey)}), MessageType.WARNING, player);
                    return;
            }

            if (familyDetails != null) {
                processOpenChest(familyDetails, key);
            }
        } catch (Exception e) {
            Logger.error(AnhyFamily.getInstance(), "Exception in handleOpenChest: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processOpenChest(FamilyDetails details, String identifier) {
        if (canOpenChest(details)) {
            PlayerFamily playerFamily = FamilyUtils.getFamily(player);
            if (details.hasAccess(playerFamily, TypeTargetComponent.CHEST)) {
                FamilyChestOpenManager.getInstance().openFamilyChest(player, details);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_access_chest", identifier != null ? new String[]{identifier} : new String[]{}), MessageType.WARNING, player);
            }
        }
    }

    // Відкрити власну сімейну скриню
    public void openChest() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> openChestIfPossible(details, null));
    }

    private void openChestIfPossible(FamilyDetails details, String identifier) {
        if (canOpenChest(details)) {
            PlayerFamily playerFamily = FamilyUtils.getFamily(player);
            if (details.hasAccess(playerFamily, TypeTargetComponent.CHEST)) {
                FamilyChestOpenManager.getInstance().openFamilyChest(player, details);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_access_chest", identifier != null ? new String[]{identifier} : new String[]{}), MessageType.WARNING, player);
            }
        }
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

    // Метод для відкриття скрині кліком
    public void attemptOpenFamilyChest(Location location) {
        UUID familyId = getUUIDFromLocation(location);
        if (familyId == null) {
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(familyId), details -> {
            if (canOpenChest(details)) {
                PlayerFamily playerFamily = FamilyUtils.getFamily(player);
                if (details.hasAccess(playerFamily, TypeTargetComponent.CHEST) || player.hasPermission(Permissions.FAMILY_CHEST_CLICK)) {
                    FamilyChestOpenManager.getInstance().openFamilyChest(player, details);
                } else {
                    sendMessage(new MessageForFormatting("family_err_no_access_chest", new String[]{}), MessageType.WARNING, player);
                }
            }
        });
    }

    // Встановлення доступу до сімейної скрині іншого гравця (родича)
    public void setChestAccess() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fchest access <NickName> <allow|deny|default>"}), MessageType.WARNING, player);
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
                sendMessage(new MessageForFormatting("family_err_invalid_access", new String[]{accessArg}), MessageType.WARNING, player);
                return;
        }

        PlayerFamily targetFamily = FamilyUtils.getFamily(nickname);
        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_nickname_not_found", new String[]{nickname}), MessageType.WARNING, player);
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
        	if (targetFamily.getFamilyId() != null && targetFamily.getFamilyId().equals(details.getFamilyId())) {
                sendMessage(new MessageForFormatting("family_access_root", new String[] {nickname, details.getFamilySymbol()}), MessageType.NORMAL, player);
                return;
        	}
        	
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
                sendMessage(new MessageForFormatting("family_chest_access_set", new String[]{nickname, accessArg}), MessageType.NORMAL, player);
            } else {
                sendMessage(new MessageForFormatting("family_err_nickname_not_found_in_access_maps", new String[]{nickname}), MessageType.WARNING, player);
            }
        });
    }

    // Метод для встановлення доступів за змовчуванням до скрині групам родичів, батькам та дітям
    public void setChestAccessDefault() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fchest default <children|parents> <allow|deny>"}), MessageType.WARNING, player);
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
                sendMessage(new MessageForFormatting("family_err_invalid_access", new String[]{accessArg}), MessageType.WARNING, player);
                return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            if ("children".equals(targetGroup)) {
                details.getChildrenAccess().setChestAccess(access);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.CHILDREN_ACCESS);
                sendMessage(new MessageForFormatting("family_default_chest_access_set", new String[]{"children", accessArg}), MessageType.NORMAL, player);
            } else if ("parents".equals(targetGroup)) {
                details.getAncestorsAccess().setChestAccess(access);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.ANCESTORS_ACCESS);
                sendMessage(new MessageForFormatting("family_default_chest_access_set", new String[]{"parents", accessArg}), MessageType.NORMAL, player);
            } else {
                sendMessage(new MessageForFormatting("family_err_invalid_group", new String[]{targetGroup}), MessageType.WARNING, player);
            }
        });
    }

    public void checkAccess() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fchest check <NickName>"}), MessageType.WARNING, player);
            return;
        }
        String nickname = args[1];

        PlayerFamily targetFamily = FamilyUtils.getFamily(nickname);
        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_nickname_not_found", new String[]{nickname}), MessageType.WARNING, player);
            return;
        }

        PlayerFamily senderFamily = FamilyUtils.getFamily(player);
        if (senderFamily != null) {
            executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(senderFamily), details -> {
                Access currentAccess = details.getAccess(targetFamily, TypeTargetComponent.CHEST);
                MessageComponents messageComponents = MessageComponentBuilder.buildCheckAccessMessageComponent(player, nickname, currentAccess, command);
                Messenger.sendMessage(familyPlugin, player, messageComponents, "family_access_get");
            });
        }
    }

    public void checkDefaultAccess() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fchest defaultcheck <children|parents>"}), MessageType.WARNING, player);
            return;
        }

        String targetGroup = args[1].toLowerCase();

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            AccessControl accessControl;

            switch (targetGroup) {
                case "children":
                    accessControl = details.getChildrenAccess();
                    break;
                case "parents":
                    accessControl = details.getAncestorsAccess();
                    break;
                default:
                    sendMessage(new MessageForFormatting("family_err_invalid_group", new String[]{targetGroup}), MessageType.WARNING, player);
                    return;
            }

            Access currentAccess = accessControl.getChestAccess();

            MessageComponents messageComponents = MessageComponentBuilder.buildDefaultAccessMessageComponent(player, targetGroup, currentAccess, command);

            Messenger.sendMessage(familyPlugin, player, messageComponents, "family_default_chest_access_check");
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
            sendMessage(new MessageForFormatting("family_err_details_not_found", new String[]{}), MessageType.WARNING, player);
        }
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
}

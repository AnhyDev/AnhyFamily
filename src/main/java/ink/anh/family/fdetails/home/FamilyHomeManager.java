package ink.anh.family.fdetails.home;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import ink.anh.api.enums.Access;
import ink.anh.api.messages.Logger;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsActionInterface;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.symbol.FamilySymbolManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;

public class FamilyHomeManager extends Sender {

    private static Map<UUID, HomeRequest> homeRequests = new ConcurrentHashMap<>();

    private AnhyFamily familyPlugin;
    private Player player;
    private String[] args;

    public FamilyHomeManager(AnhyFamily familyPlugin, Player player, String[] args) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
        this.player = player;
        this.args = args;
    }

    public void setHome() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            GlobalManager manager = (GlobalManager) libraryManager;
            if (details.getHomeLocation() == null || details.canChangeHome(manager.getFamilyConfig().getHomeChangeTimeoutMinutes())) {
                PlayerFamily playerFamily = FamilyUtils.getFamily(player);
                UUID spouseUUID = playerFamily.getSpouse();
                if (spouseUUID == null) {
                    sendMessage(new MessageForFormatting("family_err_no_spouse", new String[] {}), MessageType.WARNING, player);
                    return;
                }
                Location currentLocation = player.getLocation();
                homeRequests.put(details.getFamilyId(), new HomeRequest(currentLocation, player.getUniqueId()));
                sendMessage(new MessageForFormatting("family_home_request_sent", new String[] {}), MessageType.NORMAL, player);

                // Запуск таймера на 60 секунд
                Bukkit.getScheduler().runTaskLater(familyPlugin, () -> {
                    if (homeRequests.containsKey(details.getFamilyId())) {
                        homeRequests.remove(details.getFamilyId());
                        sendMessage(new MessageForFormatting("family_err_request_home_not_confirmed", new String[] {}), MessageType.WARNING, player);
                    }
                }, 1200L); 
            } else {
                sendMessage(new MessageForFormatting("family_err_home_already_set", new String[] {}), MessageType.WARNING, player);
            }
        });
    }

    public void setAccept() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            UUID familyId = details.getFamilyId();
            HomeRequest request = homeRequests.get(familyId);
            if (request != null && !request.getRequesterUUID().equals(player.getUniqueId())) {
                details.setHomeLocation(request.getLocation());
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.HOME_LOCATION);
                homeRequests.remove(familyId);
                Player[] players = new Player[] {player, Bukkit.getPlayer(request.getRequesterUUID())};
                sendMessage(new MessageForFormatting("family_home_set", new String[] {}), MessageType.NORMAL, players);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_pending_request", new String[] {}), MessageType.WARNING, player);
            }
        });
    }

    public void tpHomeWithConditions() {
        if (args.length == 0) {
            handleTpHome(null, 0);
            return;
        }

        String firstArg = args[0];

        if (firstArg.startsWith("#")) {
            handleTpHome(firstArg.substring(1).toUpperCase(), 1);
        } else if (firstArg.startsWith("@")) {
            handleTpHome(firstArg.substring(1), 2);
        } else {
            handleTpHome(null, 0);
        }
    }

    private void handleTpHome(String key, int typeKey) {
        try {
            FamilyDetails familyDetails = null;
            String lowerCaseKey = key != null ? key.toLowerCase() : "null";

            switch (typeKey) {
                case 0:
                    familyDetails = FamilyDetailsGet.getRootFamilyDetails(player);
                    break;
                case 1:
                    if (key.length() < 3 || key.length() > 6 || !key.matches("[A-Z]+")) {
                        sendInvalidSymbolError(key);
                        return;
                    }

                    UUID familyId = FamilySymbolManager.getFamilyIdBySymbol(key);
                    if (familyId == null) {
                        sendInvalidSymbolError(key);
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
                processTpHome(familyDetails, key);
            }
        } catch (Exception e) {
            Logger.error(AnhyFamily.getInstance(), "Exception in handleTpHome: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processTpHome(FamilyDetails details, String key) {
        PlayerFamily playerFamily = FamilyUtils.getFamily(player);

        if (!details.hasAccessHome(playerFamily)) {
            sendMessage(new MessageForFormatting("family_err_no_access_home", new String[]{key}), MessageType.WARNING, player);
            return;
        }

        Location homeLocation = details.getHomeLocation();
        if (homeLocation != null) {
            if (canTeleportToHome(homeLocation)) {
                SyncExecutor.runSync(() -> player.teleport(homeLocation));
            }
        } else {
            String messageKey = key != null ? "family_err_home_other_not_set" : "family_err_home_not_set";
            sendMessage(new MessageForFormatting(messageKey, new String[]{key}), MessageType.WARNING, player);
        }
    }

    private boolean canTeleportToHome(Location homeLocation) {
        if (!player.hasPermission(Permissions.FAMILY_TPHOME) && GlobalManager.getInstance().getFamilyConfig().isHomeWorld() && 
            !player.getWorld().equals(homeLocation.getWorld())) {
            sendMessage(new MessageForFormatting("family_err_home_world_restriction", new String[] {}), MessageType.WARNING, player);
            return false;
        }
        return true;
    }

    private void sendInvalidSymbolError(String symbol) {
        sendMessage(new MessageForFormatting("family_err_symbol_not_found", new String[] {symbol}), MessageType.WARNING, player);
        sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fhome [set|accept|access|default|#<prefix>|@<nickname>]"}), MessageType.WARNING, player);
    }

    public void setHomeAccess() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fhome access <NickName> <allow|deny|default>"}), MessageType.WARNING, player);
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
                accessControl.setHomeAccess(access);
                if (childrenAccessMap.containsKey(targetUUID)) {
                    childrenAccessMap.put(targetUUID, accessControl);
                    FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.CHILDREN_ACCESS_MAP);
                } else {
                    ancestorsAccessMap.put(targetUUID, accessControl);
                    FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.ANCESTORS_ACCESS_MAP);
                }
                sendMessage(new MessageForFormatting("family_home_access_set", new String[] {nickname, accessArg}), MessageType.NORMAL, player);
            } else {
                sendMessage(new MessageForFormatting("family_err_nickname_not_found_in_access_maps", new String[] {nickname}), MessageType.WARNING, player);
            }
        });
    }

    public void setDefaultHomeAccess() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fhome default <children|parents> <allow|deny>"}), MessageType.WARNING, player);
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
                details.getChildrenAccess().setHomeAccess(access);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.CHILDREN_ACCESS);
                sendMessage(new MessageForFormatting("family_default_home_access_set", new String[] {"children", accessArg}), MessageType.NORMAL, player);
            } else if ("parents".equals(targetGroup)) {
                details.getAncestorsAccess().setHomeAccess(access);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.ANCESTORS_ACCESS);
                sendMessage(new MessageForFormatting("family_default_home_access_set", new String[] {"parents", accessArg}), MessageType.NORMAL, player);
            } else {
                sendMessage(new MessageForFormatting("family_err_invalid_group", new String[] {targetGroup}), MessageType.WARNING, player);
            }
        });
    }

    public void checkAccess() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fhome check <NickName>"}), MessageType.WARNING, player);
            return;
        }
        String nickname = args[1];

        PlayerFamily targetFamily = FamilyUtils.getFamily(nickname);
        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_nickname_not_found", new String[] {nickname}), MessageType.WARNING, player);
            return;
        }


        PlayerFamily senderFamily = FamilyUtils.getFamily(player);
        if (senderFamily != null) {
            executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(senderFamily), details -> {
                boolean accessControl = details.hasAccessHome(targetFamily);
                sendMessage(new MessageForFormatting("family_access_get", new String[] {nickname, String.valueOf(accessControl)}), MessageType.WARNING, player);
            });
        }
    }

    private void executeWithFamilyDetails(FamilyDetails details, FamilyDetailsActionInterface action) {
        if (details != null) {
            action.execute(details);
        } else {
            sendMessage(new MessageForFormatting("family_err_details_not_found", new String[] {}), MessageType.WARNING, player);
        }
    }

    private static class HomeRequest {
        private final Location location;
        private final UUID requesterUUID;

        public HomeRequest(Location location, UUID requesterUUID) {
            this.location = location;
            this.requesterUUID = requesterUUID;
        }

        public Location getLocation() {
            return location;
        }

        public UUID getRequesterUUID() {
            return requesterUUID;
        }
    }
}

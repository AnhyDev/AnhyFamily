package ink.anh.family.fdetails.home;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import ink.anh.api.enums.Access;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.SyncExecutor;
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

public class FamilyHomeManager extends Sender {

    private static Map<UUID, HomeRequest> homeRequests = new HashMap<>();

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

    public void tpHome() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            if (details.getHomeLocation() != null) {
                SyncExecutor.runSync(() -> player.teleport(details.getHomeLocation()));
            } else {
                sendMessage(new MessageForFormatting("family_err_home_not_set", new String[] {}), MessageType.WARNING, player);
            }
        });
    }

    public void childHome() {
        PlayerFamily childFamily = getPlayerFamily(new MessageForFormatting("family_err_command_format", new String[] {"/fhome child <NickName>"}),
        		new MessageForFormatting("family_err_nickname_not_found", new String[] {args[1]}));
        if (childFamily == null) {
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(childFamily), details -> {
            if (details.hasAccessHome(childFamily)) {
                if (details.getHomeLocation() != null) {
                    SyncExecutor.runSync(() -> player.teleport(details.getHomeLocation()));
                } else {
                    sendMessage(new MessageForFormatting("family_err_home_other_not_set", new String[] {args[1]}), MessageType.WARNING, player);
                }
            } else {
                sendMessage(new MessageForFormatting("family_err_no_access_home", new String[] {args[1]}), MessageType.WARNING, player);
            }
        });
    }

    public void parentHome() {
        PlayerFamily parentFamily = getPlayerFamily(new MessageForFormatting("family_err_command_format", new String[] {"/fhome parent <NickName>"}),
        		new MessageForFormatting("family_err_nickname_not_found", new String[] {args[1]}));
        if (parentFamily == null) {
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(parentFamily), details -> {
            if (details.hasAccessHome(parentFamily)) {
                if (details.getHomeLocation() != null) {
                    SyncExecutor.runSync(() -> player.teleport(details.getHomeLocation()));
                } else {
                    sendMessage(new MessageForFormatting("family_err_home_other_not_set", new String[] {args[1]}), MessageType.WARNING, player);
                }
            } else {
                sendMessage(new MessageForFormatting("family_err_no_access_home", new String[] {args[1]}), MessageType.WARNING, player);
            }
        });
    }

    public void tpHomeBySymbol() {

        String symbol = args[0].toUpperCase();
        if (symbol.length() < 3 || symbol.length() > 6 || !symbol.matches("[A-Z]+")) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fhome [set|tp|child|parent|access|default|<symbol>]"}), MessageType.WARNING, player);
            return;
        }

        UUID familyId = FamilySymbolManager.getFamilyIdBySymbol(symbol);
        if (familyId == null) {
            sendMessage(new MessageForFormatting("family_err_symbol_not_found", new String[] {symbol}), MessageType.WARNING, player);
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fhome [set|tp|child|parent|access|default|<symbol>]"}), MessageType.WARNING, player);
            return;
        }

        FamilyDetails details = FamilyDetailsGet.getRootFamilyDetails(familyId);
        if (details == null) {
            sendMessage(new MessageForFormatting("family_err_details_not_found", new String[] {}), MessageType.WARNING, player);
            return;
        }

        PlayerFamily playerFamily = FamilyUtils.getFamily(player);
        if (!details.hasAccessHome(playerFamily)) {
            sendMessage(new MessageForFormatting("family_err_no_access_home", new String[] {symbol}), MessageType.WARNING, player);
            return;
        }

        Location homeLocation = details.getHomeLocation();
        if (homeLocation != null) {
            SyncExecutor.runSync(() -> player.teleport(homeLocation));
        } else {
            sendMessage(new MessageForFormatting("family_err_home_not_set", new String[] {}), MessageType.WARNING, player);
        }
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
                AccessControl childrenAccess = details.getChildrenAccess();
                childrenAccess.setHomeAccess(access);
                details.setChildrenAccess(childrenAccess);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.CHILDREN_ACCESS);
                sendMessage(new MessageForFormatting("family_default_home_access_set", new String[] {"children", accessArg}), MessageType.NORMAL, player);
            } else if ("parents".equals(targetGroup)) {
                AccessControl ancestorsAccess = details.getAncestorsAccess();
                ancestorsAccess.setHomeAccess(access);
                details.setAncestorsAccess(ancestorsAccess);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.ANCESTORS_ACCESS);
                sendMessage(new MessageForFormatting("family_default_home_access_set", new String[] {"parents", accessArg}), MessageType.NORMAL, player);
            } else {
                sendMessage(new MessageForFormatting("family_err_invalid_group", new String[] {targetGroup}), MessageType.WARNING, player);
            }
        });
    }

    private PlayerFamily getPlayerFamily(MessageForFormatting noArgError, MessageForFormatting notFoundError) {
        if (args.length < 2) {
            sendMessage(noArgError, MessageType.WARNING, player);
            return null;
        }
        String name = args[1];
        PlayerFamily family = FamilyUtils.getFamily(name);
        if (family == null) {
            sendMessage(notFoundError, MessageType.WARNING, player);
        }
        return family;
    }

    public void executeWithFamilyDetails(FamilyDetails details, FamilyDetailsActionInterface action) {
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

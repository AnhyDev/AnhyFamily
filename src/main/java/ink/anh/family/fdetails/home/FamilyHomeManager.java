package ink.anh.family.fdetails.home;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import ink.anh.api.enums.Access;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.MessageComponentBuilder;
import ink.anh.family.fdetails.AbstractDetailsManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.TypeTargetComponent;
import ink.anh.family.util.FamilyUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FamilyHomeManager extends AbstractDetailsManager {
    private static final Map<UUID, HomeRequest> homeRequests = new ConcurrentHashMap<>();

    public FamilyHomeManager(AnhyFamily familyPlugin, Player player, Command cmd, String[] args) {
        super(familyPlugin, player, cmd, args);
    }

    @Override
    protected String getDefaultCommand() {
        return "fhome";
    }

    @Override
    protected String getInvalidAccessMessage() {
        return "family_err_no_access_home";
    }

    @Override
    protected String getComponentAccessSetMessageKey(TypeTargetComponent component) {
        return "family_home_access_set";
    }

    @Override
    protected String getDefaultAccessSetMessageKey(TypeTargetComponent component) {
        return "family_default_home_access_set";
    }

    @Override
    protected String getDefaultAccessCheckMessageKey(TypeTargetComponent component) {
        return "family_default_home_access_check";
    }

    @Override
    protected boolean canPerformAction(FamilyDetails details, Object additionalParameter) {
        return true;
    }

    @Override
    protected TypeTargetComponent getTypeTargetComponent() {
        return TypeTargetComponent.HOME;
    }

    @Override
    protected void setComponentAccess(AccessControl accessControl, Access access, TypeTargetComponent component) {
        accessControl.setHomeAccess(access);
    }

    @Override
    protected void performAction(FamilyDetails details) {
        Location homeLocation = details.getHomeLocation();
        if (homeLocation != null) {
            if (canTeleportToHome(homeLocation)) {
                SyncExecutor.runSync(() -> teleportPlayer(player, homeLocation));
            }
        } else {
            String messageKey = "family_err_home_not_set";
            sendMessage(new MessageForFormatting(messageKey, new String[]{}), MessageType.WARNING, player);
        }
    }
    
    private void teleportPlayer(Player player, Location location) {
        // Відтворення звуку телепортації перед телепортацією
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        // Телепортація гравця
        Bukkit.getScheduler().runTaskLater(GlobalManager.getInstance().getPlugin(), () -> {
            player.teleport(location);
        }, 10L);

        // Відтворення звуку телепортації після телепортації
        Bukkit.getScheduler().runTaskLater(GlobalManager.getInstance().getPlugin(), () -> {
            player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }, 20L);
    }

    private boolean canTeleportToHome(Location homeLocation) {
        if (!player.hasPermission(Permissions.FAMILY_TPHOME) && GlobalManager.getInstance().getFamilyConfig().isHomeWorld() &&
                !player.getWorld().equals(homeLocation.getWorld())) {
            sendMessage(new MessageForFormatting("family_err_home_world_restriction", new String[]{}), MessageType.WARNING, player);
            return false;
        }
        return true;
    }

    public void setHome() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            GlobalManager manager = (GlobalManager) libraryManager;
            if (details.getHomeLocation() == null || details.canChangeHome(manager.getFamilyConfig().getHomeChangeTimeoutMinutes())) {
                PlayerFamily playerFamily = FamilyUtils.getFamily(player);
                UUID spouseUUID = playerFamily.getSpouse();
                if (spouseUUID == null) {
                    sendMessage(new MessageForFormatting("family_err_no_spouse", new String[]{}), MessageType.WARNING, player);
                    return;
                }
                Location currentLocation = player.getLocation();
                homeRequests.put(details.getFamilyId(), new HomeRequest(currentLocation, player.getUniqueId()));
                sendMessage(new MessageForFormatting("family_home_request_sent", new String[]{}), MessageType.NORMAL, player);

                Player spouse = Bukkit.getPlayer(spouseUUID);
                if (spouse != null && spouse.isOnline()) {
                    MessageComponents messageComponents = MessageComponentBuilder.acceptMessageComponent("family_home_accept_sent", command, "accept", "refuse", spouse);
                    sendMessageComponent(player, messageComponents);
                }

                Bukkit.getScheduler().runTaskLater(familyPlugin, () -> {
                    if (homeRequests.containsKey(details.getFamilyId())) {
                        homeRequests.remove(details.getFamilyId());
                        sendMessage(new MessageForFormatting("family_err_request_home_not_confirmed", new String[]{}), MessageType.WARNING, player);
                    }
                }, 1200L);
            } else {
                sendMessage(new MessageForFormatting("family_err_home_already_set", new String[]{}), MessageType.WARNING, player);
            }
        });
    }

    public void requestAccept() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            UUID familyId = details.getFamilyId();
            HomeRequest request = homeRequests.get(familyId);
            if (request != null && !request.getRequesterUUID().equals(player.getUniqueId())) {
                details.setHomeLocation(request.getLocation());
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.HOME_LOCATION);
                homeRequests.remove(familyId);
                Player[] players = new Player[]{player, Bukkit.getPlayer(request.getRequesterUUID())};
                sendMessage(new MessageForFormatting("family_home_set", new String[]{}), MessageType.NORMAL, players);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_pending_request", new String[]{}), MessageType.WARNING, player);
            }
        });
    }
    
    public void requestRejected() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            UUID familyId = details.getFamilyId();
            HomeRequest request = homeRequests.get(familyId);
            if (request != null && !request.getRequesterUUID().equals(player.getUniqueId())) {
                homeRequests.remove(familyId);
                Player[] players = new Player[]{player, Bukkit.getPlayer(request.getRequesterUUID())};
                sendMessage(new MessageForFormatting("family_request_rejected", new String[]{}), MessageType.NORMAL, players);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_pending_request", new String[]{}), MessageType.WARNING, player);
            }
        });
    }

    public void tpHomeWithConditions() {
        handleActionWithConditions();
    }

    public void setHomeAccess() {
        setAccess(args[1], args[2], TypeTargetComponent.HOME);
    }

    public void setDefaultHomeAccess() {
        setDefaultAccess(args[1], args[2], TypeTargetComponent.HOME);
    }

    public void checkAccess() {
        checkAccess(args[1], TypeTargetComponent.HOME);
    }

    public void checkDefaultAccess() {
        checkDefaultAccess(args[1], TypeTargetComponent.HOME);
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

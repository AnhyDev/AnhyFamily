package ink.anh.family.fdetails.hugs;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.api.enums.Access;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.PlayerFamilyDBService;
import ink.anh.family.fplayer.permissions.AbstractPermission;
import ink.anh.family.fplayer.permissions.ActionsPermissions;
import ink.anh.family.fplayer.permissions.PermissionModifier;
import ink.anh.family.fplayer.permissions.HugsPermission;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.TypeTargetComponent;

public class FamilyHugsSubCommand extends Sender {

    private AnhyFamily familyPlugin;
    
    private static String commandUsage = "\n| /fhugs access <args> \n| /fhugs default <args> \n| /fhugs check <NickName> \n| /fhugs defaultcheck <children|parents> "
    		+ "\n| /fhugs allow <NickName> \n| /fhugs deny <NickName> \n| /fhugs allowall <true|false> \n| /fhugs denyall <true|false> \n| /fhugs remove <NickName> "
    		+ "\n| /fhugs list \n| /fhugs globalstatus <allowall|denyall>";

    public FamilyHugsSubCommand(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
    }

    public boolean onCommand(Player player, Command cmd, String[] args) {
        CompletableFuture.runAsync(() -> {
            try {
                FamilyHugsManager hugsManager = new FamilyHugsManager(familyPlugin, player, cmd, args);
                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                        case "access":
                            hugsManager.setHugsAccess();
                            break;
                        case "default":
                            hugsManager.setDefaultHugsAccess();
                            break;
                        case "check":
                            hugsManager.checkHugsAccess();
                            break;
                        case "defaultcheck":
                            hugsManager.checkDefaultHugsAccess();
                            break;
                        case "allow":
                            handleAllowHugs(player, args);
                            break;
                        case "deny":
                            handleDenyHugs(player, args);
                            break;
                        case "allowall":
                            handleAllowAll(player, args);
                            break;
                        case "denyall":
                            handleDenyAll(player, args);
                            break;
                        case "remove":
                            handleRemoveHugs(player, args);
                            break;
                        case "list":
                            handleListPermissions(player, args);
                            break;
                        case "globalstatus":
                            handleCheckGlobalHugsStatus(player, args);
                            break;
                        default:
                            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{commandUsage}), MessageType.WARNING, player);
                    }
                } else {
                    handleCheckHugsPermission(player);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return true;
    }

    // Новий метод для перевірки глобального статусу доступу
    private void handleCheckGlobalHugsStatus(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs globalstatus <allowall|denyall>"}), MessageType.WARNING, player);
            return;
        }

        PlayerFamily playerFamily = FamilyUtils.getFamily(player);
        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, player);
            return;
        }

        HugsPermission hugsPermission = (HugsPermission) playerFamily.getPermission(ActionsPermissions.HUGS_TO_ALL_PLAYERS);
        boolean status;

        switch (args[1].toLowerCase()) {
            case "allowall":
                status = hugsPermission.isAllowAll();
                sendMessage(new MessageForFormatting("family_hugs_allow_all_status", new String[]{Boolean.toString(status)}), MessageType.NORMAL, player);
                break;
            case "denyall":
                status = hugsPermission.isDenyAllExceptFamily();
                sendMessage(new MessageForFormatting("family_hugs_deny_all_status", new String[]{Boolean.toString(status)}), MessageType.NORMAL, player);
                break;
            default:
                sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs globalstatus <allowall|denyall>"}), MessageType.WARNING, player);
                break;
        }
    }

    private void handleListPermissions(Player player, String[] args) {
        PlayerFamily playerFamily = FamilyUtils.getFamily(player);

        AbstractPermission permission = playerFamily.getPermissionsMap().get(ActionsPermissions.HUGS_TO_ALL_PLAYERS);

        if (permission != null && permission.getPermissionsMap() != null && !permission.getPermissionsMap().isEmpty()) {
            for (Map.Entry<UUID, Access> entry : permission.getPermissionsMap().entrySet()) {
                if (entry.getKey() != null) {
                    String targetName = FamilyUtils.getFamily(entry.getKey()).getRootrNickName();
                    sendMessage(new MessageForFormatting("family_hugs_access_list", new String[]{targetName, entry.getValue().name()}), MessageType.NORMAL, player);
                }
            }
        } else {
            sendMessage(new MessageForFormatting("family_hugs_access_list_empty", new String[]{}), MessageType.NORMAL, player);
        }
    }

    private void handleRemoveHugs(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs remove <NickName>"}), MessageType.WARNING, player);
            return;
        }

        String targetName = args[1];

        PlayerFamily targetFamily = FamilyUtils.getFamily(targetName);
        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, player);
            return;
        }

        PlayerFamily playerFamily = FamilyUtils.getFamily(player);

        PermissionModifier.removePermission(playerFamily, targetFamily.getRoot(), ActionsPermissions.HUGS_TO_ALL_PLAYERS);
        sendMessage(new MessageForFormatting("family_hugs_access_removed", new String[]{targetName}), MessageType.NORMAL, player);
    }

    private void handleAllowHugs(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs allow <NickName>"}), MessageType.WARNING, player);
            return;
        }

        String targetName = args[1];

        PlayerFamily targetFamily = FamilyUtils.getFamily(targetName);
        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, player);
            return;
        }

        PlayerFamily playerFamily = FamilyUtils.getFamily(player);

        PermissionModifier.setPermission(playerFamily, targetFamily.getRoot(), ActionsPermissions.HUGS_TO_ALL_PLAYERS, Access.TRUE);
        sendMessage(new MessageForFormatting("family_hugs_access_allowed", new String[]{targetName}), MessageType.NORMAL, player);
    }

    private void handleDenyHugs(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs deny <NickName>"}), MessageType.WARNING, player);
            return;
        }

        String targetName = args[1];

        PlayerFamily targetFamily = FamilyUtils.getFamily(targetName);
        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, player);
            return;
        }

        PlayerFamily playerFamily = FamilyUtils.getFamily(player);

        PermissionModifier.setPermission(playerFamily, targetFamily.getRoot(), ActionsPermissions.HUGS_TO_ALL_PLAYERS, Access.FALSE);
        sendMessage(new MessageForFormatting("family_hugs_access_denied", new String[]{targetName}), MessageType.NORMAL, player);
    }

    private void handleAllowAll(Player player, String[] args) {
        boolean allowAll = false;

        if (args.length < 2) {
            allowAll = true;
        } else {
            try {
                allowAll = Boolean.parseBoolean(args[1]);
            } catch (IllegalArgumentException e) {
                sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs allowall <true|false>"}), MessageType.WARNING, player);
                return;
            }
        }

        PlayerFamily playerFamily = FamilyUtils.getFamily(player);
        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, player);
            return;
        }

        HugsPermission hugsPermission = (HugsPermission) playerFamily.getPermission(ActionsPermissions.HUGS_TO_ALL_PLAYERS);
        hugsPermission.setAllowAll(allowAll);
        if (allowAll) {
            hugsPermission.setDenyAllExceptFamily(false);
        }

        // Збереження змін у базі даних
        PlayerFamilyDBService.savePlayerFamily(playerFamily, FamilyPlayerField.PERMISSIONS_MAP);
        sendMessage(new MessageForFormatting("family_hugs_allow_all_enabled", new String[]{Boolean.toString(allowAll)}), MessageType.NORMAL, player);
    }

    private void handleDenyAll(Player player, String[] args) {
        boolean denyAll = false;

        if (args.length < 2) {
            denyAll = true;
        } else {
            try {
                denyAll = Boolean.parseBoolean(args[1]);
            } catch (IllegalArgumentException e) {
                sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs denyall <true|false>"}), MessageType.WARNING, player);
                return;
            }
        }

        PlayerFamily playerFamily = FamilyUtils.getFamily(player);
        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, player);
            return;
        }

        HugsPermission hugsPermission = (HugsPermission) playerFamily.getPermission(ActionsPermissions.HUGS_TO_ALL_PLAYERS);
        hugsPermission.setDenyAllExceptFamily(denyAll);
        if (denyAll) {
            hugsPermission.setAllowAll(false);
        }

        // Збереження змін у базі даних
        PlayerFamilyDBService.savePlayerFamily(playerFamily, FamilyPlayerField.PERMISSIONS_MAP);
        sendMessage(new MessageForFormatting("family_hugs_deny_all_enabled", new String[]{Boolean.toString(denyAll)}), MessageType.NORMAL, player);
    }

    private void handleCheckHugsPermission(Player player) {
        SyncExecutor.runSync(() -> {
            // Використовуємо метод rayTraceEntities з фільтром для виключення самого гравця
            RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                5.0,
                entity -> entity instanceof Player && entity != player
            );

            if (rayTraceResult != null && rayTraceResult.getHitEntity() instanceof Player) {
                Player targetPlayer = (Player) rayTraceResult.getHitEntity();

                SyncExecutor.runAsync(() -> {
                    PlayerFamily targetFamily = FamilyUtils.getFamily(targetPlayer);
                    PlayerFamily playerFamily = FamilyUtils.getFamily(player);

                    HugsPermission permission = (HugsPermission) targetFamily.getPermission(ActionsPermissions.HUGS_TO_ALL_PLAYERS);
                    FamilyDetails details = FamilyDetailsGet.getRootFamilyDetails(targetFamily);

                    boolean canHug = permission.checkPermission(playerFamily, details, TypeTargetComponent.HUGS);

                    sendMessage(new MessageForFormatting(canHug ? "family_hugs_permission_allowed" : "family_hugs_permission_denied", new String[]{targetPlayer.getName(), player.getName()}), canHug ? MessageType.NORMAL : MessageType.WARNING, player);
                });
            } else {
                sendMessage(new MessageForFormatting("family_err_command_format", new String[]{commandUsage}), MessageType.WARNING, player);
            }
        });
    }
}

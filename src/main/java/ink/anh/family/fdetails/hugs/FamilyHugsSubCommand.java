package ink.anh.family.fdetails.hugs;

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
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.api.enums.Access;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.permissions.ActionsPermissions;
import ink.anh.family.fplayer.permissions.PermissionModifier;
import ink.anh.family.fplayer.permissions.HugsPermission;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.TypeTargetComponent;

public class FamilyHugsSubCommand extends Sender {

    private AnhyFamily familyPlugin;

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
                        default:
                            hugsManager.sendMessageWithConditions();
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

        PermissionModifier.setPermission(targetFamily, targetFamily.getRoot(), ActionsPermissions.HUGS_TO_ALL_PLAYERS, Access.TRUE);
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

        PermissionModifier.setPermission(targetFamily, targetFamily.getRoot(), ActionsPermissions.HUGS_TO_ALL_PLAYERS, Access.FALSE);
        sendMessage(new MessageForFormatting("family_hugs_access_denied", new String[]{targetName}), MessageType.NORMAL, player);
    }

    private void handleAllowAll(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs allowall <true|false>"}), MessageType.WARNING, player);
            return;
        }

        boolean allowAll = Boolean.parseBoolean(args[1]);

        PlayerFamily playerFamily = FamilyUtils.getFamily(player.getName());
        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, player);
            return;
        }

        HugsPermission hugsPermission = (HugsPermission) playerFamily.getPermission(ActionsPermissions.HUGS_TO_ALL_PLAYERS);
        hugsPermission.setAllowAll(allowAll);
        if (allowAll) {
            hugsPermission.setDenyAllExceptFamily(false);
        }

        sendMessage(new MessageForFormatting(allowAll ? "family_hugs_allow_all_enabled" : "family_hugs_allow_all_disabled", new String[]{}), MessageType.NORMAL, player);
    }

    private void handleDenyAll(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs denyall <true|false>"}), MessageType.WARNING, player);
            return;
        }

        boolean denyAll = Boolean.parseBoolean(args[1]);

        PlayerFamily playerFamily = FamilyUtils.getFamily(player.getName());
        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, player);
            return;
        }

        HugsPermission hugsPermission = (HugsPermission) playerFamily.getPermission(ActionsPermissions.HUGS_TO_ALL_PLAYERS);
        hugsPermission.setDenyAllExceptFamily(denyAll);
        if (denyAll) {
            hugsPermission.setAllowAll(false);
        }

        sendMessage(new MessageForFormatting(denyAll ? "family_hugs_deny_all_enabled" : "family_hugs_deny_all_disabled", new String[]{}), MessageType.NORMAL, player);
    }

    private void handleCheckHugsPermission(Player player) {
        SyncExecutor.runSync(() -> {
            RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5.0);

            if (rayTraceResult != null && rayTraceResult.getHitEntity() instanceof Player) {
                SyncExecutor.runAsync(() -> {
                    Player targetPlayer = (Player) rayTraceResult.getHitEntity();
                    PlayerFamily targetFamily = FamilyUtils.getFamily(targetPlayer);
                    PlayerFamily playerFamily = FamilyUtils.getFamily(player);

                    HugsPermission permission = (HugsPermission) targetFamily.getPermission(ActionsPermissions.HUGS_TO_ALL_PLAYERS);
                    FamilyDetails details = FamilyDetailsGet.getRootFamilyDetails(targetFamily);

                    boolean canHug = permission.checkPermission(playerFamily, details, TypeTargetComponent.HUGS);

                    sendMessage(new MessageForFormatting(canHug ? "family_hugs_permission_allowed" : "family_hugs_permission_denied", new String[]{targetPlayer.getName()}), canHug ? MessageType.NORMAL : MessageType.WARNING, player);
                });
            } else {
                String commandUsage = "\n| /fhugs access <args> \n| /fhugs default <args> \n| /fhugs check <NickName> \n| /fhugs defaultcheck <children|parents> \n| /fhugs allow <NickName> \n| /fhugs deny <NickName> \n| /fhugs allowall <true|false> \n| /fhugs denyall <true|false>";
                sendMessage(new MessageForFormatting("family_err_command_format", new String[]{commandUsage}), MessageType.WARNING, player);
            }
        });
    }

}

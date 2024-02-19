package ink.anh.family.parents;

import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.Sender;
import ink.anh.family.common.Family;
import ink.anh.family.common.FamilySeparation;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ParentSeparation extends Sender {

    public ParentSeparation(AnhyFamily familyPlugin) {
        super(familyPlugin);
    }

    public boolean separate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", null), MessageType.WARNING, sender);
            return false;
        }

        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format /family separate parent <player>", null), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Family playerFamily = FamilyUtils.getFamily(playerUUID);

        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_info_family_not_found", null), MessageType.WARNING, sender);
            return false;
        }

        String targetPlayerName = args[2];
        Family targetFamily = FamilyUtils.getFamily(targetPlayerName);

        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_no_family_found_for_target", null), MessageType.WARNING, sender);
            return false;
        }

        UUID targetUUID = targetFamily.getRoot();
        Player targetPlayer = Bukkit.getPlayer(targetUUID);

        FamilySeparation familySeparation = new FamilySeparation(familyPlugin);

        boolean success;
        if (playerUUID.equals(targetFamily.getFather()) || playerUUID.equals(targetFamily.getMother())) {
            // Якщо виконавець команди є одним із батьків цільового гравця
            success = familySeparation.separateChildFromParent(targetUUID, playerUUID);
        } else {
            sendMessage(new MessageForFormatting("family_err_no_parent_child_relationship", null), MessageType.WARNING, sender);
            return false;
        }

        if (success) {
            sendMessage(new MessageForFormatting("family_success_separation_completed", null), MessageType.IMPORTANT, player, targetPlayer);
        } else {
            sendMessage(new MessageForFormatting("family_err_separation_failed", null), MessageType.WARNING, sender);
        }

        return success;
    }
}

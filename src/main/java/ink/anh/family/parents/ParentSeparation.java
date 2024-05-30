package ink.anh.family.parents;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.FamilySeparation;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ParentSeparation extends Sender {

	private AnhyFamily familyPlugin;

    public ParentSeparation(AnhyFamily familyPlugin) {
    	super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
    }

    public boolean separate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format /family separate parent <player>", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        PlayerFamily playerFamily = FamilyUtils.getFamily(playerUUID);

        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_info_family_not_found", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        String targetPlayerName = args[2];
        PlayerFamily targetFamily = FamilyUtils.getFamily(targetPlayerName);

        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_no_family_found_for_target", new String[] {}), MessageType.WARNING, sender);
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
            sendMessage(new MessageForFormatting("family_err_no_parent_child_relationship", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        if (success) {
            sendMessage(new MessageForFormatting("family_success_separation_completed", new String[] {}), MessageType.IMPORTANT, player, targetPlayer);
        } else {
            sendMessage(new MessageForFormatting("family_err_separation_failed", new String[] {}), MessageType.WARNING, sender);
        }

        return success;
    }
}

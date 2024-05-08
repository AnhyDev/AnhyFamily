package ink.anh.family.parents;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.common.Family;
import ink.anh.family.common.FamilySeparation;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChildSeparation extends Sender {

	private AnhyFamily familyPlugin;

    public ChildSeparation(AnhyFamily familyPlugin) {
    	super(familyPlugin.getGlobalManager());
		this.familyPlugin = familyPlugin;
    }

    public boolean separate(CommandSender sender, String[] args) {
        // Перевірка правил використання команди
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format /family separate child <player>", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        UUID senderUUID = player.getUniqueId();
        Family senderFamily = FamilyUtils.getFamily(senderUUID);

        if (senderFamily == null) {
            sendMessage(new MessageForFormatting("family_info_family_not_found", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        String targetPlayerName = args[2];
        Family targetFamily = FamilyUtils.getFamily(targetPlayerName);

        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_no_family_found_for_target", new String[] {}), MessageType.WARNING, sender);
            return false;
        }
        
        UUID targetUUID = targetFamily.getRoot();
        Player targetPlayer = Bukkit.getPlayer(targetUUID);

        FamilySeparation familySeparation = new FamilySeparation(familyPlugin);

        // Виклик методу відокремлення
        boolean success;
        if (senderUUID.equals(targetFamily.getFather()) || senderUUID.equals(targetFamily.getMother())) {
            // Якщо виконавець команди є одним із батьків
            success = familySeparation.separateParentFromChild(senderUUID, targetUUID);
        } else if (targetUUID.equals(senderFamily.getFather()) || targetUUID.equals(senderFamily.getMother())) {
            // Якщо ціль команди є одним із батьків виконавця
            success = familySeparation.separateChildFromParent(senderUUID, targetUUID);
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

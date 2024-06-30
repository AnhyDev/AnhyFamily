package ink.anh.family.fplayer.info;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;

public class FamilyProfileHandler extends Sender {

    public FamilyProfileHandler() {
    	super(GlobalManager.getInstance());
    }

    public boolean handleCommand(CommandSender sender, String[] args) {
    	PlayerFamily playerFamily = getTargetFamily(sender, args);
    	
        if (playerFamily == null) return false;

        String familyInfo = translate(sender, new InfoGenerator().generateFamilyInfo(playerFamily));

        sendMessage(new MessageForFormatting(familyInfo, new String[] {}), MessageType.NORMAL, false, sender);

        return true;
    }

    private PlayerFamily getTargetFamily(CommandSender sender, String[] args) {
        if (args.length > 1) {
            PlayerFamily playerFamily = FamilyUtils.getFamily(args[1]);
            if (playerFamily == null) {
                sendMessage(new MessageForFormatting("family_player_not_found_db", new String[] {}), MessageType.WARNING, sender);
                return null;
            }
            return playerFamily;
        } else if (sender instanceof Player) {
            return FamilyUtils.getFamily((Player) sender);
        } else {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return null;
        }
    }
}

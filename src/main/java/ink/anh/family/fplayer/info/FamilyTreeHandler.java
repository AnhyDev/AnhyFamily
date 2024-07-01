package ink.anh.family.fplayer.info;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Messenger;

public class FamilyTreeHandler extends FamilyCommandHandler {

    @Override
    protected boolean executeCommand(CommandSender sender, PlayerFamily playerFamily) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            MessageComponents messageComponents = new TreeComponentGenerator(playerFamily).buildFamilyTreeComponent(player);
            Messenger.sendMessage(libraryManager.getPlugin(), player, messageComponents, "MessageComponents");
            return true;
        }

        String treeInfo = new TreeStringGenerator(playerFamily).buildFamilyTreeString();
        sendMessage(new MessageForFormatting(treeInfo, new String[] {}), MessageType.NORMAL, false, sender);
        return true;
    }
}

package ink.anh.family.fplayer.info;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.FamilyUtils;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.OtherComponentBuilder;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Messenger;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.MessageComponents;

public class FamilyInfo extends Sender {

    public FamilyInfo() {
        super(GlobalManager.getInstance());
    }

    public boolean handleInfoCommand(CommandSender sender, String[] args, boolean isInteractive) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        PlayerFamily playerFamily = null;
        String targetName = null;
        String[] langs = getLangs(player);

        if (args.length > 1) {
            targetName = args[1];
            playerFamily = FamilyUtils.getFamily(targetName);
        } else {
            playerFamily = FamilyUtils.getFamily(player);
        }

        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        String playerName = playerFamily.getRootrNickName();

        String messageBase = formatAndColorize("family_print_info", new String[]{playerName}, langs);
        String message1 = formatAndColorize("family_profile_component", new String[]{playerName}, langs);
        String message2 = formatAndColorize("family_tree_component", new String[]{playerName}, langs);

        String familyInfo = formatAndColorize(new ProfileStringGenerator().generateFamilyInfo(playerFamily) + "\n&6 family_print_component", new String[]{playerName}, langs);
        String treeInfo = formatAndColorize(new TreeStringGenerator(playerFamily).buildFamilyTreeString() + "\n&6 family_print_component", new String[]{playerName}, langs);

        String cmdProfile = "/family profile " + playerName;
        String smdTree = "/family tree " + playerName;

        MessageComponents messageComponents = OtherComponentBuilder.infoDoubleComponentHoverString(
                messageBase, cmdProfile, smdTree, message1, message2, familyInfo, treeInfo, player
        );

        Messenger.sendMessage(libraryManager.getPlugin(), player, messageComponents, "MessageComponents");

        return true;
    }

    private String formatAndColorize(String messageKey, String[] replace, String[] langs) {
        String message = Translator.translateKyeWorld(libraryManager, messageKey, langs);
        message = StringUtils.formatString(message, replace);
        return StringUtils.colorize(message);
    }

    private String[] getLangs(Player recipient) {
        return recipient != null ? LangUtils.getPlayerLanguage(recipient) : new String[]{libraryManager.getDefaultLang()};
    }
}

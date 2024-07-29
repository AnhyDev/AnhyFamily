package ink.anh.family.fplayer;

import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Messenger;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.FamilyConfig;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;

public class FirstName extends Sender {

    public FirstName() {
        super(GlobalManager.getInstance());
    }

    public boolean setFirstName(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        if (!sender.hasPermission(Permissions.FAMILY_USER)) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;

        if (args.length <= 1) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/family firstname <FirstName>"}), MessageType.WARNING, sender);
            return false;
        }

        String firstName = args[1];

        if (!checkMaxLengthFirstName(firstName)) {
            sendMessage(new MessageForFormatting("family_firstname_too_long", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        PlayerFamily playerFamily = FamilyUtils.getFamily(player);

        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        SyncExecutor.runSync(() -> handleFirstNameChange(player, playerFamily, firstName, sender));
        return true;
    }

    public boolean suggesFirstName(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        String firstName = args[3];

        PlayerFamily playerFamily = FamilyUtils.getFamily(player);

        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        SyncExecutor.runSync(() -> handleFirstNameChange(player, playerFamily, firstName, sender));
        return true;
    }

    protected void sendMessageComponent(Player recipient, MessageComponents messageComponents) {
        Messenger.sendMessage(libraryManager.getPlugin(), recipient, messageComponents, "MessageComponents");
    }

    public boolean setFirstNameFromConsole(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/family forcefirstname <PlayerName> <FirstName>"}), MessageType.WARNING, sender);
            return false;
        }

        String playerName = args[1];
        String firstName = args[2];

        if (!checkMaxLengthFirstName(firstName)) {
            sendMessage(new MessageForFormatting("family_firstname_too_long", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        PlayerFamily playerFamily = FamilyUtils.getFamily(playerName);
        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        SyncExecutor.runSync(() -> handleFirstNameChange(null, playerFamily, firstName, sender));
        return true;
    }

    private void handleFirstNameChange(Player player, PlayerFamily playerFamily, String firstName, CommandSender sender) {
        SyncExecutor.runAsync(() -> {
            playerFamily.setFirstName(firstName);
            PlayerFamilyDBService.savePlayerFamily(playerFamily, FamilyPlayerField.FIRST_NAME);

            sendMessage(new MessageForFormatting("family_firstname_selected", new String[]{firstName}), MessageType.IMPORTANT, sender);
        });
    }

    public static boolean checkMaxLengthFirstName(String firstName) {
        final int MAX_LENGTH = 12;

        // Перевірка на максимальну довжину
        if (firstName.length() > MAX_LENGTH) {
            return false;
        }

        // Отримання конфігурації
        FamilyConfig config = GlobalManager.getInstance().getFamilyConfig();

        // Перевірка на відповідність регулярному виразу
        String regex = config.getLanguagesLimitation();
        if (!Pattern.matches(regex, firstName)) {
            return false;
        }

        return true;
    }
}

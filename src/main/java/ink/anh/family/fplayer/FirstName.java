package ink.anh.family.fplayer;

import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.family.util.FamilyUtils;
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
            sendMessage(new MessageForFormatting("family_err_command_format_firstname", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        String firstName = args[1];

        if (!checkMaxLengthFirstName(firstName)) {
            sendMessage(new MessageForFormatting("family_firstname_too_long", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        UUID uuid = player.getUniqueId();
        PlayerFamily playerFamily = FamilyCacheManager.getInstance().getFamilyData(uuid);

        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        SyncExecutor.runSync(() -> handleFirstNameChange(player, playerFamily, firstName, sender));
        return true;
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
            PlayerFamilyDBServsce.savePlayerFamily(playerFamily, FamilyPlayerField.FIRST_NAME);

            sendMessage(new MessageForFormatting("family_firstname_selected", new String[]{firstName}), MessageType.IMPORTANT, sender);
        });
    }

    private boolean checkMaxLengthFirstName(String firstName) {
        final int MAX_LENGTH = 10;

        // Перевірка на максимальну довжину
        if (firstName.length() > MAX_LENGTH) {
            return false;
        }

        // Перевірка на пробіли
        if (firstName.contains(" ")) {
            return false;
        }

        // Перевірка на числа
        for (char c : firstName.toCharArray()) {
            if (Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }
}

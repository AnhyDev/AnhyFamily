package ink.anh.family.fplayer;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.FamilyConfig;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.SurnameSelectEvent;
import ink.anh.api.messages.MessageForFormatting;

public class Surname extends Sender {

    public Surname() {
        super(GlobalManager.getInstance());
    }

    public boolean setSurname(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        if (!sender.hasPermission(Permissions.FAMILY_USER)) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;

        if (args.length <= 1) {
            sendMessage(new MessageForFormatting("family_err_command_format /family surname <Surname male version[/Surname female version]>", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        String[] newFamily = processInput(args, 1);
        if (newFamily == null) {
            sendMessage(new MessageForFormatting("family_surname_build_failed", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        PlayerFamily playerFamily = FamilyUtils.getFamily(player);

        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        // Перевірка наявності firstName
        if (playerFamily.getFirstName() == null || playerFamily.getFirstName().isEmpty()) {
            sendMessage(new MessageForFormatting("family_err_firstname_not_set", new String[]{"/family firstname <FirstName>"}), MessageType.WARNING, sender);
            return false;
        }

        SyncExecutor.runSync(() -> handleSurnameChange(player, playerFamily, newFamily, ActionInitiator.PLAYER_SELF, sender));
        return true;
    }

	public boolean suggesSurname(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        String[] newFamily = processInput(args, 3);

        PlayerFamily playerFamily = FamilyUtils.getFamily(player);

        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        SyncExecutor.runSync(() -> handleSurnameChange(player, playerFamily, newFamily, ActionInitiator.PLAYER_SELF, sender));
        return true;
    }

    public boolean setSurnameFromConsole(CommandSender sender, String[] args) {

        if (sender instanceof Player) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[]{}), MessageType.WARNING, sender);
            return false;
        }
        
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format /family setsurname <PlayerName> <Surname male version[/Surname female version]>", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        String playerName = args[1];

        PlayerFamily playerFamily = FamilyUtils.getFamily(playerName);
        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        String[] newFamily = processInput(args, 2);
        if (newFamily == null) {
            sendMessage(new MessageForFormatting("family_surname_build_failed", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        SyncExecutor.runSync(() -> handleSurnameChange(null, playerFamily, newFamily, ActionInitiator.CONSOLE, sender));
        return true;
    }

    private void handleSurnameChange(Player player, PlayerFamily playerFamily, String[] newSurname, ActionInitiator initiator, CommandSender sender) {
        SurnameSelectEvent event = new SurnameSelectEvent(player, playerFamily, initiator);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            sendMessage(new MessageForFormatting("family_err_event_is_canceled", new String[]{}), MessageType.WARNING, sender);
            return;
        }

        SyncExecutor.runAsync(() -> {
            if (playerFamily.getLastName() == null || playerFamily.getLastName()[0] == null || player == null) {
                playerFamily.setLastName(newSurname);
                PlayerFamilyDBService.savePlayerFamily(playerFamily, FamilyPlayerField.LAST_NAME);

                String myfam = String.join(" / ", newSurname);
                sendMessage(new MessageForFormatting("family_surname_selected", new String[]{myfam}), MessageType.IMPORTANT, sender);
            } else {
                sendMessage(new MessageForFormatting("family_surname_already_exists", new String[]{String.join(" / ", playerFamily.getLastName())}), MessageType.WARNING, sender);
            }
        });
    }

    public static String[] processInput(String[] args, int index) {
        StringBuilder inputBuilder = new StringBuilder();
        for (int i = index; i < args.length; i++) {
            inputBuilder.append(args[i]);
            if (i < args.length - 1) {
                inputBuilder.append(" ");
            }
        }
        String input = inputBuilder.toString();
        String[] newFamily = buildSurname(input);
        return newFamily;
    }

    public static String[] buildSurname(String input) {
        String processedInput = input.replaceAll("\\s+", " ");
        String[] newFamily;
        int slashIndex = processedInput.indexOf("/");

        if (slashIndex != -1) {
            newFamily = new String[2];
            newFamily[0] = processedInput.substring(0, slashIndex).trim();
            newFamily[1] = processedInput.substring(slashIndex + 1).trim();
            if (!checkMaxLengthSurname(newFamily)) {
                return null;
            }
        } else {
            newFamily = new String[1];
            newFamily[0] = processedInput.trim();
            if (!checkMaxLengthSurname(newFamily)) {
                return null;
            }
        }

        return newFamily;
    }

    public static boolean checkMaxLengthSurname(String[] newFamily) {
        if (newFamily == null || newFamily.length == 0) {
            return false;
        }
        
        final int MAX_LENGTH = 15;

        // Отримання конфігурації
        FamilyConfig config = GlobalManager.getInstance().getFamilyConfig();
        String regex = config.getLanguagesLimitation();
        
        for (String familyString : newFamily) {
            // Перевірка на максимальну довжину
            if (familyString.length() > MAX_LENGTH) {
                return false;
            }
            
            // Перевірка на відповідність регулярному виразу
            if (!Pattern.matches(regex, familyString)) {
                return false;
            }
        }
        return true;
    }
}

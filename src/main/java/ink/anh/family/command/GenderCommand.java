package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.GlobalManager;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.GenderSelectEvent;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.fplayer.gender.GenderManager;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.Logger;
import ink.anh.api.messages.MessageForFormatting;

public class GenderCommand extends Sender implements CommandExecutor {
    public GenderCommand() {
        super(GlobalManager.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CompletableFuture.runAsync(() -> {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    handleGenderInfo(sender, null);
                }
                return; // Early return for async context
            }
            
            switch (args[0].toLowerCase()) {
                case "set":
                    if (sender instanceof Player && args.length >= 2) {
                        handleSetGender(sender, args[1]);
                    }
                    break;
                case "info":
                    if (args.length <= 1) {
                        if (sender instanceof Player) {
                            handleGenderInfo(sender, null);
                        }
                    } else if (args.length >= 2) {
                        handleGenderInfo(sender, args[1]);
                    }
                    break;
                case "reset":
                    if (args.length >= 2) {
                        handleResetGender(sender, args[1]);
                    }
                    break;
                case "forceset":
                    if (args.length >= 3) {
                        handleForceSetGender(sender, args[1], args[2]);
                    }
                    break;
                default:
                    sendMessage(new MessageForFormatting("family_err_command_format /gender [set|info|reset|forceset]", new String[]{}), MessageType.WARNING, sender);
            }
        });
        return true;
    }

    private void handleSetGender(CommandSender sender, String genderStr) {
        Player player = (Player) sender;

        // Перевіряємо, чи дозволено встановлювати небінарну стать
        if (genderStr.equalsIgnoreCase("NON_BINARY") && !((GlobalManager) libraryManager).getFamilyConfig().isNonBinary()) {
            sendMessage(new MessageForFormatting("family_non_binary_not_allowed", new String[]{}), MessageType.WARNING, player);
            return;
        }

        genderStr = genderStr.equalsIgnoreCase("boy") || genderStr.equalsIgnoreCase("man") ? "MALE"
                : genderStr.equalsIgnoreCase("girl") || genderStr.equalsIgnoreCase("woman") ? "FEMALE" 
                : genderStr.toUpperCase();

        Gender gender = Gender.fromString(genderStr);

        if (gender != null && gender != Gender.UNDECIDED) {
            if (GenderManager.getGender(player) == Gender.UNDECIDED) {
                MessageForFormatting message = new MessageForFormatting("family_set_gender_force " + Gender.getKey(gender), new String[]{});
                SyncExecutor.runSync(() -> setPlayerGender(player, FamilyUtils.getFamily(player), gender, ActionInitiator.CONSOLE, sender, message));
            } else {
                sendMessage(new MessageForFormatting("family_gender_already_selected", new String[]{}), MessageType.WARNING, player);
            }
        } else {
            sendMessage(new MessageForFormatting("family_gender_incorrectly_specified", new String[]{}), MessageType.WARNING, player);
        }
    }

    private void handleGenderInfo(CommandSender sender, String playerName) {
        Gender gender = playerName != null ? GenderManager.getGender(playerName) : sender instanceof Player ? GenderManager.getGender((Player) sender) : null;
        playerName = playerName != null ? playerName : sender.getName();

        if (gender != null) {
            sendMessage(new MessageForFormatting("family_gender_player_info " + Gender.getKey(gender), new String[]{playerName}), MessageType.NORMAL, sender);
        } else {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[]{playerName}), MessageType.WARNING, sender);
        }
    }

    private void handleResetGender(CommandSender sender, String playerName) {
        if (sender instanceof Player) {
            return;
        }

        PlayerFamily playerFamily = FamilyUtils.getFamily(playerName);
        if (playerFamily != null) {
            MessageForFormatting message = new MessageForFormatting("family_gender_player_reset", new String[]{playerName});
            SyncExecutor.runSync(() -> setPlayerGender(null, playerFamily, Gender.UNDECIDED, ActionInitiator.CONSOLE, sender, message));
        } else {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[]{playerName}), MessageType.WARNING, sender);
        }
    }

    private void handleForceSetGender(CommandSender sender, String playerName, String genderStr) {
        if (sender instanceof Player) {
            return;
        }

        genderStr = genderStr.equalsIgnoreCase("MAN") ? "MALE" : genderStr.equalsIgnoreCase("WOMAN") ? "FEMALE" : genderStr.toUpperCase();
        PlayerFamily playerFamily = FamilyUtils.getFamily(playerName);

        if (playerFamily != null) {
            Gender gender = Gender.fromString(genderStr);

            if (gender != null && gender != Gender.UNDECIDED) {
                MessageForFormatting message = new MessageForFormatting("family_gender_player_set_to " + Gender.getKey(gender), new String[]{playerName});
                SyncExecutor.runSync(() -> setPlayerGender(null, playerFamily, gender, ActionInitiator.CONSOLE, sender, message));
            } else {
                sendMessage(new MessageForFormatting("family_gender_incorrectly_specified", new String[]{}), MessageType.WARNING, sender);
            }
        } else {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[]{playerName}), MessageType.WARNING, sender);
        }
    }

    private void setPlayerGender(Player player, PlayerFamily playerFamily, Gender gender, ActionInitiator initiator, CommandSender sender, MessageForFormatting message) {

        try {
            GenderSelectEvent event = new GenderSelectEvent(playerFamily, Gender.getKey(gender), initiator);

            if (!event.isCancelled()) {
            	SyncExecutor.runAsync(() -> {
                    if (player != null) {
                        GenderManager.setGender(player, gender);
                    } else {
                        GenderManager.setGender(playerFamily.getRoot(), gender);
                    }
                    Logger.info(libraryManager.getPlugin(), "Gender set " + gender + " for player: " + playerFamily.getCurrentSurname());
                    sendMessage(message, MessageType.IMPORTANT, sender);
            	});
            } else {
                sendMessage(new MessageForFormatting("family_err_event_is_canceled", new String[]{}), MessageType.WARNING, sender);
            }
        } catch (Exception e) {
        	Logger.info(libraryManager.getPlugin(), "Exception in setPlayerGender: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

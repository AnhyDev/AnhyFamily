package ink.anh.family.gender;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.Sender;
import ink.anh.api.messages.MessageForFormatting;

import org.bukkit.Bukkit;

public class GenderCommand extends Sender implements CommandExecutor {

    public GenderCommand(AnhyFamily familyPlugin) {
    	super(familyPlugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "set":
                if (sender instanceof Player && args.length == 2) {
                    return handleSetGender(sender, args[1]);
                }
                break;
            case "info":
                if (args.length == 1) {
                    if (sender instanceof Player) {
                        return handleGenderInfo((Player) sender, sender.getName());
                    }
                } else if (args.length == 2) {
                    return handleGenderInfo(sender, args[1]);
                }
                break;
            case "reset":
                if (args.length == 2) {
                    return handleResetGender(sender, args[1]);
                }
                break;
            case "forceset":
                if (args.length == 3) {
                    return handleForceSetGender(sender, args[1], args[2]);
                }
                break;
            default:
                return false;
        }

        return true;
    }

    private boolean handleSetGender(CommandSender sender, String genderStr) {
    	Player player = (Player) sender;
        Gender gender = Gender.fromString(genderStr);
        if (gender != null && gender != Gender.UNDECIDED) {
            if (GenderUtils.getGender(player) == Gender.UNDECIDED) {
            	GenderUtils.setGender(player, gender);
                sendMessage(new MessageForFormatting("family_set_gender", null),  MessageType.NORMAL, sender);
                return true;
            } else {
                sendMessage(new MessageForFormatting("family_gender_already_selected", null), MessageType.WARNING, player);
                return true;
            }
        } else {
            sendMessage(new MessageForFormatting("family_gender_incorrectly_specified", null), MessageType.WARNING, player);
            return true;
        }
    }

    private boolean handleGenderInfo(CommandSender sender, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            Gender gender = GenderUtils.getGender(targetPlayer);
            sendMessage(new MessageForFormatting("family_gender_player" + targetPlayer.getName() + ": " + gender.name(), null),  MessageType.NORMAL, sender);
            return true;
        } else {
            sendMessage(new MessageForFormatting("family_player" + targetPlayerName + "family_player_not_found", null), MessageType.WARNING, sender);
            return true;
        }
    }

    private boolean handleResetGender(CommandSender sender, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null) {
        	GenderUtils.setGender(targetPlayer, Gender.UNDECIDED);
            sendMessage(new MessageForFormatting("family_gender_player" + targetPlayer.getName() + "family_gender_player_reset", null),  MessageType.NORMAL, sender);
            return true;
        } else {
            sendMessage(new MessageForFormatting("family_player" + playerName + "family_player_not_found", null), MessageType.WARNING, sender);
            return true;
        }
    }

    private boolean handleForceSetGender(CommandSender sender, String playerName, String genderStr) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null) {
            Gender gender = Gender.fromString(genderStr);
            if (gender != null && gender != Gender.UNDECIDED) {
            	GenderUtils.setGender(targetPlayer, gender);
                sendMessage(new MessageForFormatting("family_player" + targetPlayer.getName() + "family_gender_player_set_to" + gender.name(), null),  MessageType.NORMAL, sender);
                return true;
            } else {
                sendMessage(new MessageForFormatting("family_gender_incorrectly_specified", null), MessageType.WARNING, sender);
                return true;
            }
        } else {
            sendMessage(new MessageForFormatting("family_player" + playerName + "family_player_not_found", null), MessageType.WARNING, sender);
            return true;
        }
    }
}


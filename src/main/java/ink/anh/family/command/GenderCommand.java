package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.Sender;
import ink.anh.family.common.Family;
import ink.anh.family.gender.Gender;
import ink.anh.family.gender.GenderManager;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

public class GenderCommand extends Sender implements CommandExecutor {

    public GenderCommand(AnhyFamily familyPlugin) {
        super(familyPlugin);
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
                    sendMessage(new MessageForFormatting("family_err_command_format /gender [set|info|reset|forceset]", null), MessageType.WARNING, sender);
            }
        });
        return true;
    }

    private boolean handleSetGender(CommandSender sender, String genderStr) {
    	Player player = (Player) sender;
    	
    	genderStr = (genderStr.equalsIgnoreCase("boy") || genderStr.equalsIgnoreCase("man")) ? "MALE" 
    			: (genderStr.equalsIgnoreCase("girl") || genderStr.equalsIgnoreCase("woman")) ? "FEMALE" : genderStr.toUpperCase();
        Gender gender = Gender.fromString(genderStr);
        if (gender != null && gender != Gender.UNDECIDED) {
            if (GenderManager.getGender(player) == Gender.UNDECIDED) {
            	GenderManager.setGender(player, gender);
                sendMessage(new MessageForFormatting("family_set_gender_force " + Gender.getKey(gender), null),  MessageType.NORMAL, sender);
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

    private boolean handleGenderInfo(CommandSender sender, String playerName) {
    	Gender gender = (playerName != null) ? GenderManager.getGender(playerName) : (sender instanceof Player) ? GenderManager.getGender((Player) sender) : null;
        
        if (gender != null) {
            sendMessage(new MessageForFormatting("family_gender_player_info " + Gender.getKey(gender), new String[] {playerName}),  MessageType.NORMAL, sender);
            return true;
        } else {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[] {playerName}), MessageType.WARNING, sender);
            return true;
        }
    }

    private boolean handleResetGender(CommandSender sender, String playerName) {
    	if (sender instanceof Player) {
    		return false;
    	}

    	Family family = FamilyUtils.getFamily(playerName);
        if (family != null) {
        	GenderManager.setGender(family, Gender.UNDECIDED);
            sendMessage(new MessageForFormatting("family_gender_player_reset", new String[] {playerName}),  MessageType.NORMAL, sender);
            return true;
        } else {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[] {playerName}), MessageType.WARNING, sender);
            return true;
        }
    }

    private boolean handleForceSetGender(CommandSender sender, String playerName, String genderStr) {
    	if (sender instanceof Player) {
    		return false;
    	}
    	
    	genderStr = genderStr.equalsIgnoreCase("MAN") ? "MALE" : genderStr.equalsIgnoreCase("WOMAN") ? "FEMALE" : genderStr.toUpperCase();
    	Family family = FamilyUtils.getFamily(playerName);
        if (family != null) {
            Gender gender = Gender.fromString(genderStr);
            if (gender != null && gender != Gender.UNDECIDED) {
            	GenderManager.setGender(family, gender);
                sendMessage(new MessageForFormatting("family_gender_player_set_to " + Gender.getKey(gender), new String[] {playerName}),  MessageType.NORMAL, sender);
                return true;
            } else {
                sendMessage(new MessageForFormatting("family_gender_incorrectly_specified", null), MessageType.WARNING, sender);
                return true;
            }
        } else {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[] {playerName}), MessageType.WARNING, sender);
            return true;
        }
    }
}


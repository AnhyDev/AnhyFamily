package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.Logger;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.db.TableRegistry;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.GenderSelectEvent;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.Surname;
import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.fplayer.gender.GenderManager;
import ink.anh.family.marriage.MarriageManager;
import ink.anh.family.parents.Adopt;
import ink.anh.family.parents.ParentManager;
import ink.anh.family.separate.ClearAllRelatives;
import ink.anh.family.util.FamilyUtils;

public class AnhyFamilyCommand extends Sender implements CommandExecutor {

	private AnhyFamily familyPlugin;
	
	public AnhyFamilyCommand(AnhyFamily familyPlugin) {
		super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    CompletableFuture.runAsync(() -> {
            try {
    	        if (args.length > 0) {
    	            switch (args[0].toLowerCase()) {
    	                case "parentelement":
    	                    infoParentElement(sender);
    	                    break;
    	                case "marryelement":
    	                    infoMarryElement(sender);
    	                    break;
    	                case "setsurname":
    	                    new Surname().setSurnameFromConsole(sender, args);
    	                    break;
    	                case "clear":
    	                    new ClearAllRelatives(familyPlugin).exeClearFamily(sender, args);
    	                    break;
    	                case "reload":
    	                    reload(sender);
    	                    break;
    	                case "forceadopt":
    	                    new Adopt(familyPlugin).forceAdopt(sender, args);
    	                    break;
    	                case "forcegender":
    	                    if (args.length >= 3) {
    	                        handleForceSetGender(sender, args[1], args[2]);
    	                    }
    	                    break;
    	                default:
    	                    sendMessage(new MessageForFormatting("family_err_command_format /family <param>", new String[] {}), MessageType.WARNING, sender);
    	            }
    	        }
            } catch (Exception e) {
                e.printStackTrace(); // Вивід виключення в лог
            }
	    });
	    return true;
	}


	private boolean reload(CommandSender sender) {
		if (!(sender instanceof Player) && sender.getName().equalsIgnoreCase("CONSOLE")) {
			GlobalManager manager = GlobalManager.getInstance();
			manager.reload();

			manager.getDatabaseManager().reload(manager, new TableRegistry(familyPlugin));
	        MarriageManager.getInstance(familyPlugin).reload();
	        ParentManager.getInstance(familyPlugin).reload();

	        Logger.info(familyPlugin, Translator.translateKyeWorld(manager, "family_plugin_reloaded" , null));
			return true;
		}
		return false;
	}

	private boolean infoMarryElement(CommandSender sender) {
		if (sender.getName().equalsIgnoreCase("CONSOLE")) {
			return GlobalManager.getInstance().getMarriageManager().infoMarryElement();
		}
		return false;
	}
	
	private boolean infoParentElement(CommandSender sender) {
		if (sender.getName().equalsIgnoreCase("CONSOLE")) {
			return GlobalManager.getInstance().getParentManager().infoParentElement();
		}
		return false;
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

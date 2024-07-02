package ink.anh.family.marriage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import ink.anh.family.AnhyFamily;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.OtherUtils;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;

public class ActionsPriest extends AbstractMarriageSender {
    

    public ActionsPriest(AnhyFamily familyPlugin) {
        super(familyPlugin, true);
    }
    
    public boolean marry(CommandSender sender, String[] args) {
		
		
		if (!validator.validateCommandInput(sender, args)) {
            return false;
		}
		
		Player priest = (Player) sender;
		validator.setPriest(priest);

        Player bride1 = Bukkit.getPlayerExact(args[1]);
        Player bride2 = Bukkit.getPlayerExact(args[2]);

        Player[] initialRecipients = OtherUtils.getPlayersWithinRadius(priest.getLocation(), familyConfig.getCeremonyHearingRadius());
        Set<Player> recipientSet = new HashSet<>(Arrays.asList(initialRecipients));

        recipientSet.add(bride1);
        recipientSet.add(bride2);

        Player[] recipients = recipientSet.toArray(new Player[0]);
		
		if (!validator.validateMembers(recipients, bride1, bride2, priest)) {
			return false;
		}
        
        final String priestName = priest.getDisplayName() != null ? priest.getDisplayName() : priest.getName();
		final String bride1Name = bride1.getDisplayName() != null ? bride1.getDisplayName() : bride1.getName();
		final String bride2Name = bride2.getDisplayName() != null ? bride2.getDisplayName() : bride2.getName();

        PlayerFamily priestFamily = FamilyUtils.getFamily(priest);
        priestPrefixType = MarryPrefixType.getMarryPrefixType(priestFamily != null ? priestFamily.getGender() : Gender.UNDECIDED, 0);

        String[] validatePerm = validator.validatePermissions(bride1, bride2, recipients);
        if (validatePerm != null) {
            if (validatePerm[0] == null) {
                return false;
            } else {
                String members = String.join(", ", Arrays.copyOfRange(validatePerm, 1, validatePerm.length));
                sendMAnnouncement(priestPrefixType, priestName, validatePerm[0], MessageType.WARNING.getColor(true), new String[] {members}, recipients);
                return false;
            }
        }
		
		if (!validator.validateCeremonyParticipants(bride1, bride2, recipients)) {
			return false;
		}

		String[] validateCeremonyConditionsResult = validator.validateCeremonyConditions(bride1, bride2);
		if (validateCeremonyConditionsResult != null) {
			 String members = String.join(", ", Arrays.stream(validateCeremonyConditionsResult).skip(1).filter(Objects::nonNull).toArray(String[]::new));
		        sendMAnnouncement(priestPrefixType, priestName, validateCeremonyConditionsResult[0], MessageType.WARNING.getColor(true), new String[] {members}, recipients);
		        return false;
		}

        PlayerFamily bride1Family = FamilyUtils.getFamily(bride1.getUniqueId());
        PlayerFamily bride2Family = FamilyUtils.getFamily(bride2.getUniqueId());
        
        String[] validateCompatibilityResult = validator.validateMarriageCompatibility(bride1Family, bride2Family, recipients);
        if (validateCompatibilityResult != null) {
        	String members = String.join(", ", Arrays.stream(Arrays.copyOfRange(validateCompatibilityResult, 1, validateCompatibilityResult.length))
                    .filter(Objects::nonNull)
                    .toArray(String[]::new));
            sendMAnnouncement(priestPrefixType, priestName, validateCompatibilityResult[0], MessageType.WARNING.getColor(true), new String[] {members}, recipients);
            return false;
        }
        
        ProcessLastName processLastName = validator.processLastNameArgs(args);
        String lastName[] = processLastName.getLastName();
        int surnameChoice = processLastName.getNumberLastName();
        
    	if (lastName == null) {
            sendMessage(new MessageForFormatting("family_marry_failed_last_name", new String[] {}), MessageType.WARNING, false, recipients);
            return false;
    	}

		String[] validatePaymentResult = validator.validatePayment(bride1, bride2, recipients, bride1Name, bride2Name);
		if (validatePaymentResult != null) {
		    String members = String.join(", ", Arrays.copyOfRange(validatePaymentResult, 1, validatePaymentResult.length));
		    sendMAnnouncement(priestPrefixType, priestName, validatePaymentResult[0], MessageType.WARNING.getColor(true), new String[] {members}, recipients);
		    return false;
		}

    	if (!marriageManager.add(bride1, bride2, priest, surnameChoice, lastName)) {
            sendMessage(new MessageForFormatting("family_marry_already_started", new String[] {bride1Name, bride2Name}), MessageType.WARNING, true, priest);
            return false;
    	}

    	sendMessage(new MessageForFormatting("family_marry_start_priest", new String[] {}), MessageType.NORMAL, priest);
    	
		Bukkit.getServer().getScheduler().runTaskLater(familyPlugin, () -> {
		    sendMAnnouncement(priestPrefixType, priestName, "family_marry_start_success", MessageType.IMPORTANT.getColor(true), new String[] {bride1Name, bride2Name}, recipients);
		    sendPriestAcceptMessage(priestPrefixType, priestName, new Player[] {bride1, bride2});
		    
		}, 10L);

        return true;
	}
}

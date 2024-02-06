package ink.anh.family.marry;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.Sender;
import ink.anh.family.common.Family;
import ink.anh.family.common.FamilyConfig;
import ink.anh.family.common.FamilyService;
import ink.anh.family.gender.Gender;
import ink.anh.family.util.OtherUtils;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.PaymentManager;

public class ActionsBrides extends Sender {
	
	private GlobalManager manager;
	private MarriageManager marriageManager;
	private FamilyConfig familyConfig;
	private String priestTitle = "";
	private String bride1Title = "";
	
	public ActionsBrides (AnhyFamily familiPlugin){
		super(familiPlugin);
		this.manager = familiPlugin.getGlobalManager();
		this.marriageManager = familiPlugin.getMarriageManager();
		this.familyConfig = manager.getFamilyConfig();
	}
	
	public void accept(AsyncPlayerChatEvent event) {
		String message = event.getMessage();
		
		if (familyConfig.checkAnswer(message) == 0) {
			return;
		}
		
		event.setCancelled(true);
		
		Player bride1 = event.getPlayer();

		UUID uuidBride1 = (bride1 != null) ? bride1.getUniqueId() : null;
		
        if (uuidBride1 == null || !bride1.isOnline()) {
        	if (uuidBride1 != null) {
        		marriageManager.remove(uuidBride1);
        	}
		    event.setCancelled(false);
            return;
        }
		
		Player[] recipients = OtherUtils.getPlayersWithinRadius(bride1.getLocation(), familyConfig.getCeremonyHearingRadius());
		
		Family bride1family = FamilyUtils.getFamily(uuidBride1);

		Marry marry = marriageManager.getMarryElement(uuidBride1);
		
		if (areAllParticipantsPresent(marry)) {
			marriageManager.remove(bride1);
		    event.setCancelled(false);
		    return;
		}

		int one = 0;

		if (marry.getBride2() != null && marry.getBride2().getUniqueId().equals(uuidBride1)) {
		    one = 1;
		}

		Player bride2 = (one == 0) ? marry.getBride2() : marry.getBride1();
		UUID uuidBride2 = bride2.getUniqueId();
		Family bride2family = FamilyUtils.getFamily(uuidBride2);
		
		Player priest = marry.getPriest();
		
		if (!validateMembers(recipients, new Player[] {bride1, priest, bride2})) {
			marriageManager.remove(bride1);
			return;
		}

        setTitles(priest, bride1, bride2);
		
		String marry1 = ": family_marry_vows_man";
		String marry2 = ": family_marry_vows_woman";
		String marry0 = ": family_marry_vows_nonbinary";
		
		String stringMarry = "family_marry_success";

		String bride1Name = bride1family.getDisplayName();
		String bride2Name = bride2family.getDisplayName();
		
        int gender1Starus = bride2family.getGender() == Gender.MALE ? 1 : bride2family.getGender() == Gender.FEMALE ? 2 : 0;

		String message1 = gender1Starus == 1 ? marry1 : gender1Starus == 2 ? marry2 : marry0;;
		
		MessageForFormatting messageForFormatting = new MessageForFormatting(bride1Title + message1, new String[] {bride2Name, bride1Name});
		
		if (familyConfig.checkAnswer(message) == 2) {
			
			marriageManager.remove(uuidBride1);
			
			sendMessage(new MessageForFormatting(bride1Title + ": family_marry_refuse", new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients);
			Bukkit.getServer().getScheduler().runTaskLater(familiPlugin, () -> 
				sendMessage(new MessageForFormatting(priestTitle + ": family_marry_failed", new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients), 10L);
			
			return;
			
		} else if (familyConfig.checkAnswer(message) == 1) {
			
			setMarriageConsent(marry, one);
			
			if (!marry.areBothConsentsGiven()) {
				sendMessage(messageForFormatting, MessageType.WARNING, false, recipients);
				sendMessage(new MessageForFormatting(priestTitle + ": family_marry_waiting_for_consent", new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients);
				return;
			}
				
			if (paymentFailed(uuidBride1, bride1, bride2, recipients, bride1Name, bride2Name)) {
				return;
			}
			
			updateFamilyData(bride1family, bride2family, marry, one);
			
			sendMessage(messageForFormatting, MessageType.WARNING, false, recipients);

			Bukkit.getServer().getScheduler().runTaskLater(familiPlugin, () -> 
				sendMessage(new MessageForFormatting(priestTitle + stringMarry, new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients), 10L);
			
			marriageManager.remove(uuidBride1);
			return;
			
		} else {
			marriageManager.remove(uuidBride1);
			event.setCancelled(false);
		}
	}

	private void updateFamilyData(Family familyBride1, Family familyBride2, Marry marry, int one) {
		int surnameChoice = marry.getSurnameChoice();
		String[] chosenSurname = marry.getChosenSurname();
		
	    Family familyOfBrideChoosingSurname = (one == 0) ? familyBride1 : familyBride2;
	    Family familyOfOtherBride = (one == 0) ? familyBride2 : familyBride1;

	    switch (surnameChoice) {
	        case 1:
	            // Встановити прізвище 1 нареченого
	            familyOfOtherBride.setOldLastName(familyOfOtherBride.getLastName());
	            familyOfOtherBride.setLastName(chosenSurname);
	            break;
	        case 2:
	            // Встановити прізвище 2 нареченого
	            familyOfBrideChoosingSurname.setOldLastName(familyOfBrideChoosingSurname.getLastName());
	            familyOfBrideChoosingSurname.setLastName(chosenSurname);
	            break;
	        case 0:
	        default:
	            // Зберегти поточні прізвища (нічого не робити)
	            break;
	    }

	    // Оновити статус спільника
	    familyBride1.setSpouse(familyBride2.getRoot());
	    familyBride2.setSpouse(familyBride1.getRoot());

	    // Зберегти змінені дані сімей
	    FamilyUtils.saveFamily(familyBride1);
	    FamilyUtils.saveFamily(familyBride2);
	}

    private boolean paymentFailed(UUID uuidBride1, Player bride1, Player bride2, Player[] recipients, String bride1Name, String bride2Name) {
        PaymentManager pay = new PaymentManager(familiPlugin);

        if (!pay.makePayment(bride1, FamilyService.MARRIAGE) || !pay.makePayment(bride2, FamilyService.MARRIAGE)) {
        	marriageManager.remove(uuidBride1);
			sendMessage(new MessageForFormatting(priestTitle + ": family_marry_payment_failed", new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients);
            return true;
        }
        return false;
    }

    private void setMarriageConsent(Marry marry, int one) {
		if (one == 0) {
			marry.setConsent1(true);
		} else {
			marry.setConsent2(true);
		}
    }
    
    private void setTitles(Player priest, Player bride1, Player bride2) {
        priestTitle = FamilyUtils.getPriestTitle(priest);
        bride1Title = FamilyUtils.getBrideTitle(bride1);
    }

    private boolean validateMembers(Player[] recipients, Player... members) {
    	for (Player player : members) {
            if (player == null || !player.isOnline()) {
            	sendMessage(new MessageForFormatting("family_member_missing", null), MessageType.WARNING, false, recipients);
                return false;
            }
    	}
        return true;
    }

    private boolean areAllParticipantsPresent(Marry marry) {
        return marry != null &&
        	   marry.getBride1() != null &&
               marry.getBride2() != null &&
               marry.getPriest() != null;
    }
}

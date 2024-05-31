package ink.anh.family.marriage;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.FamilyConfig;
import ink.anh.family.GlobalManager;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.MarriageEvent;
import ink.anh.family.fplayer.FamilyService;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.gender.Gender;
import ink.anh.family.util.OtherUtils;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.PaymentManager;

public class ActionsBrides extends Sender {

	private AnhyFamily familyPlugin;
	private GlobalManager manager;
	private MarriageManager marriageManager;
	private FamilyConfig familyConfig;
	private String priestTitle = "";
	private String bride1Title = "";
	
	public ActionsBrides (AnhyFamily familyPlugin){
		super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
		this.manager = GlobalManager.getInstance();
		this.marriageManager = GlobalManager.getInstance().getMarriageManager();
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
		
		PlayerFamily bride1family = FamilyUtils.getFamily(uuidBride1);

		MarryPublic marryPublic = marriageManager.getMarryElement(uuidBride1);
		
		if (areAllParticipantsPresent(marryPublic)) {
			marriageManager.remove(bride1);
		    event.setCancelled(false);
		    return;
		}

		int one = 0;

		if (marryPublic.getBride2() != null && marryPublic.getBride2().getUniqueId().equals(uuidBride1)) {
		    one = 1;
		}

		Player bride2 = (one == 0) ? marryPublic.getBride2() : marryPublic.getBride1();
		UUID uuidBride2 = bride2.getUniqueId();
		PlayerFamily bride2family = FamilyUtils.getFamily(uuidBride2);
		
		Player priest = marryPublic.getPriest();
		
		if (!validateMembers(recipients, new Player[] {bride1, priest, bride2})) {
			marriageManager.remove(bride1);
			return;
		}

        setTitles(priest, bride1, bride2);
		
		String marry1 = ": family_marry_vows_man";
		String marry2 = ": family_marry_vows_woman";
		String marry0 = ": family_marry_vows_nonbinary";
		
		String stringMarry = "family_marry_success";

		String bride1Name = bride1family.getLoverCaseName();
		String bride2Name = bride2family.getLoverCaseName();
		
        int gender1Starus = bride2family.getGender() == Gender.MALE ? 1 : bride2family.getGender() == Gender.FEMALE ? 2 : 0;

		String message1 = gender1Starus == 1 ? marry1 : gender1Starus == 2 ? marry2 : marry0;;
		
		MessageForFormatting messageForFormatting = new MessageForFormatting(bride1Title + message1, new String[] {bride2Name, bride1Name});
		
		if (familyConfig.checkAnswer(message) == 2) {
			
			marriageManager.remove(uuidBride1);
			
			sendMessage(new MessageForFormatting(bride1Title + ": family_marry_refuse", new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients);
			Bukkit.getServer().getScheduler().runTaskLater(familyPlugin, () -> 
				sendMessage(new MessageForFormatting(priestTitle + ": family_marry_failed", new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients), 10L);
			
			return;
			
		} else if (familyConfig.checkAnswer(message) == 1) {
			
			setMarriageConsent(marryPublic, one);
			
			if (!marryPublic.areBothConsentsGiven()) {
				sendMessage(messageForFormatting, MessageType.WARNING, false, recipients);
				sendMessage(new MessageForFormatting(priestTitle + ": family_marry_waiting_for_consent", new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients);
				return;
			}
				
			if (paymentFailed(uuidBride1, bride1, bride2, recipients, bride1Name, bride2Name)) {
				return;
			}

			SyncExecutor.runSync(() -> {
				handleMarriage(priest, bride1family, bride2family, recipients, messageForFormatting, marryPublic, gender1Starus, new String[] {bride1Name, bride2Name}, stringMarry);
			});
			
			return;
			
		} else {
			marriageManager.remove(uuidBride1);
			event.setCancelled(false);
		}
	}

	private void updateFamilyData(PlayerFamily familyBride1, PlayerFamily familyBride2, MarryPublic marryPublic, int one) {
		int surnameChoice = marryPublic.getSurnameChoice();
		String[] chosenSurname = marryPublic.getChosenSurname();
		
	    PlayerFamily familyOfBrideChoosingSurname = (one == 0) ? familyBride1 : familyBride2;
	    PlayerFamily familyOfOtherBride = (one == 0) ? familyBride2 : familyBride1;

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
        PaymentManager pay = new PaymentManager(familyPlugin);

        if (!pay.makePayment(bride1, FamilyService.MARRIAGE) || !pay.makePayment(bride2, FamilyService.MARRIAGE)) {
        	marriageManager.remove(uuidBride1);
			sendMessage(new MessageForFormatting(priestTitle + ": family_marry_payment_failed", new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients);
            return true;
        }
        return false;
    }

    private void setMarriageConsent(MarryPublic marryPublic, int one) {
		if (one == 0) {
			marryPublic.setConsent1(true);
		} else {
			marryPublic.setConsent2(true);
		}
    }
    
    private void setTitles(Player priest, Player bride1, Player bride2) {
        priestTitle = FamilyUtils.getPriestTitle(priest);
        bride1Title = FamilyUtils.getBrideTitle(bride1);
    }

    private boolean validateMembers(Player[] recipients, Player... members) {
    	for (Player player : members) {
            if (player == null || !player.isOnline()) {
            	sendMessage(new MessageForFormatting("family_member_missing", new String[] {}), MessageType.WARNING, false, recipients);
                return false;
            }
    	}
        return true;
    }

    private boolean areAllParticipantsPresent(MarryPublic marryPublic) {
        return marryPublic != null &&
        	   marryPublic.getBride1() != null &&
               marryPublic.getBride2() != null &&
               marryPublic.getPriest() != null;
    }

    private void handleMarriage(Player priest, PlayerFamily proposerFamily, PlayerFamily receiverFamily, Player[] recipients,
    		MessageForFormatting messageForFormatting, MarryPublic marryPublic, int one, String[] brides, String stringMarry) {
        final MessageType[] messageType = {MessageType.WARNING, MessageType.ESPECIALLY};
        try {
            MarriageEvent event = new MarriageEvent(priest, proposerFamily, receiverFamily, ActionInitiator.PLAYER_SELF);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                SyncExecutor.runAsync(() -> {
                	updateFamilyData(proposerFamily, receiverFamily, marryPublic, one);
        			
        			sendMessage(messageForFormatting, messageType[1], false, recipients);

        			Bukkit.getServer().getScheduler().runTaskLater(familyPlugin, () -> 
        				sendMessage(new MessageForFormatting(priestTitle + stringMarry, brides), messageType[1], false, recipients), 10L);
        			
        			marriageManager.remove(proposerFamily.getRoot());
                });
            } else {
                sendMessage(new MessageForFormatting("family_err_event_is_canceled", new String[]{}), messageType[0], recipients);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Exception in handleMarriage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

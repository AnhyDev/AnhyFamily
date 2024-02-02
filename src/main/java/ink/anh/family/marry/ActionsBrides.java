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
		Player[] recipients = OtherUtils.getPlayersWithinRadius(bride1.getLocation(), familyConfig.getCeremonyHearingRadius());
		
		UUID uuid1 = bride1.getUniqueId();
		Family family1 = FamilyUtils.getFamily(uuid1);

		Marry marry = marriageManager.getMarryElement(uuid1);
		if (marry == null) {
		    event.setCancelled(false);
		    return;
		}

		int one = 0;

		if (marry.getBride2() != null && marry.getBride2().getUniqueId().equals(uuid1)) {
		    one = 1;
		}

		Player bride2 = (one == 0) ? marry.getBride2() : marry.getBride1();
		UUID uuid2 = bride2.getUniqueId();
		Family family2 = FamilyUtils.getFamily(uuid2);
		
		Player priest = marry.getPriest();
		
		if (!validateMembers(recipients, new Player[] {bride1, priest, bride2})) {
			return;
		}

        setTitles(priest, bride1, bride2);
		
		String marry1 = ": family_marry_vows_man";
		String marry2 = ": family_marry_vows_woman";
		String marry0 = ": family_marry_vows_nonbinary";
		
		String stringMarry = "family_marry_success";

		String bride1Name = family1.getDisplayName();
		String bride2Name = family2.getDisplayName();
		
        int gender1Starus = family2.getGender() == Gender.MALE ? 1 : family2.getGender() == Gender.FEMALE ? 2 : 0;

		String message1 = gender1Starus == 1 ? marry1 : gender1Starus == 2 ? marry2 : marry0;;
		
		MessageForFormatting messageForFormatting = new MessageForFormatting(bride1Title + message1, new String[] {bride2Name, bride1Name});
		
		if (familyConfig.checkAnswer(message) == 2) {
			
			marriageManager.remove(uuid1);
			
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
				
			if (paymentFailed(uuid1, bride1, bride2, recipients, bride1Name, bride2Name)) {
				return;
			}
			
			updateFamilyData(one, family1, family2, uuid1, uuid2);
			
			sendMessage(messageForFormatting, MessageType.WARNING, false, recipients);

			Bukkit.getServer().getScheduler().runTaskLater(familiPlugin, () -> 
				sendMessage(new MessageForFormatting(priestTitle + stringMarry, new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients), 10L);
			
			return;
			
		} else {

			event.setCancelled(false);
		}
	}

    private void updateFamilyData(int one, Family family1, Family family2, UUID uuid1, UUID uuid2) {
        String[] lastName1 = one == 0 ? family1.getLastName() : family2.getLastName();
        String[] lastName2 = one == 0 ? family2.getLastName() : family1.getLastName();

        family1.setSpouse(uuid2);
        family2.setSpouse(uuid1);

        if (one == 0) {
            if (family2 != null) {
                family2.setOldLastName(lastName2);
            }
            family2.setLastName(lastName1);
        } else {
            if (family1 != null) {
                family1.setOldLastName(lastName1);
            }
            family1.setLastName(lastName2);
        }

        FamilyUtils.saveFamily(family1);
        FamilyUtils.saveFamily(family2);
        
        marriageManager.remove(uuid1);
    }

    private boolean paymentFailed(UUID uuid1, Player bride1, Player bride2, Player[] recipients, String bride1Name, String bride2Name) {
        PaymentManager pay = new PaymentManager(familiPlugin);

        if (!pay.makePayment(bride1, FamilyService.MARRIAGE) || !pay.makePayment(bride2, FamilyService.MARRIAGE)) {
        	marriageManager.remove(uuid1);
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
}

package ink.anh.family.marriage;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.MarriageEvent;
import ink.anh.family.fdetails.FamilyDetailsService;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.PlayerFamilyDBService;
import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.util.OtherUtils;
import ink.anh.family.util.FamilyUtils;

public class ActionsBridesPublic extends AbstractMarriageSender {

    public ActionsBridesPublic(AnhyFamily familyPlugin) {
        super(familyPlugin, true);
    }

    public boolean handleMarriage(Player sender, boolean consentGiven) {
        UUID uuidBride1 = sender != null ? sender.getUniqueId() : null;

        if (uuidBride1 == null || !sender.isOnline()) {
            marriageManager.remove(uuidBride1);
            return false;
        }
        
        MarryPublic marryPublic = marriageManager.getMarryElement(uuidBride1);

        if (!areAllParticipantsPresent(marryPublic)) {
            marriageManager.remove(uuidBride1);
            return false;
        }

        int one = 0;

        if (marryPublic.getReceiver() != null && marryPublic.getReceiver().getUniqueId().equals(uuidBride1)) {
            one = 1;
        }

        Player bride = marryPublic.getOtherParticipant(sender);
        Player priest = marryPublic.getPriest();

        Player[] recipients = OtherUtils.getPlayersWithinRadius(sender.getLocation(), familyConfig.getCeremonyHearingRadius());

        if (!validateMembers(recipients, sender, priest, bride)) {
            marriageManager.remove(sender);
            return false;
        }

        String senderName = sender.getDisplayName() != null ? sender.getDisplayName() : sender.getName();
        String brideName = bride.getDisplayName() != null ? bride.getDisplayName() : bride.getName();
        String priestName = priest.getDisplayName() != null ? priest.getDisplayName() : priest.getName();
        
        priestPrefixType = MarryPrefixType.getMarryPrefixType(FamilyUtils.getFamily(priest).getGender(), 0);
        
        PlayerFamily senderfamily = FamilyUtils.getFamily(sender);
        bridePrefixType = MarryPrefixType.getMarryPrefixType(senderfamily.getGender(), 1);

        if (!consentGiven) {
            marriageManager.remove(uuidBride1);
            
        	sendMAnnouncement(bridePrefixType, senderName, "family_marry_refuse", MessageType.WARNING.getColor(true), new String[]{senderName, brideName}, recipients);
            
            Bukkit.getServer().getScheduler().runTaskLater(familyPlugin, () -> {

            	sendMAnnouncement(priestPrefixType, priestName, "family_marry_failed", MessageType.WARNING.getColor(true), new String[]{senderName, brideName}, recipients);
            }, 10L);
            
            return true;
        }

        String family_marry_vows_man = "family_marry_vows_man";
        String family_marry_vows_woman = "family_marry_vows_woman";
        String family_marry_vows_nonbinary = "family_marry_vows_nonbinary";
        String family_marry_success = "family_marry_success";

        int genderStatus = senderfamily.getGender() == Gender.MALE ? 1 : senderfamily.getGender() == Gender.FEMALE ? 2 : 0;

        String vowOfNewlyweds = genderStatus == 1 ? family_marry_vows_man : genderStatus == 2 ? family_marry_vows_woman : family_marry_vows_nonbinary;

        setMarriageConsent(marryPublic, one);
        
    	sendMAnnouncement(bridePrefixType, senderName, vowOfNewlyweds, MessageType.IMPORTANT.getColor(true), new String[]{brideName}, recipients);

        if (!marryPublic.areBothConsentsGiven()) {
        	sendMAnnouncement(priestPrefixType, priestName, "family_marry_waiting_for_consent", MessageType.IMPORTANT.getColor(true), new String[]{senderName, brideName}, recipients);

            return true;
        }

        PlayerFamily bridefamily = FamilyUtils.getFamily(bride);

        final int num = one;

        SyncExecutor.runSync(() -> {
            processMarriage(priest, senderfamily, bridefamily, recipients, vowOfNewlyweds, marryPublic, num, new String[]{senderName, brideName}, family_marry_success);
        });
        return true;
    }

    private void updateFamilyData(PlayerFamily familyBride1, PlayerFamily familyBride2, MarryPublic marryPublic, int one) {
        int surnameChoice = marryPublic.getSurnameChoice();
        String[] chosenSurname = marryPublic.getChosenSurname();

        PlayerFamily familyOfBrideChoosingSurname = one == 0 ? familyBride1 : familyBride2;
        PlayerFamily familyOfOtherBride = one == 0 ? familyBride2 : familyBride1;

        switch (surnameChoice) {
            case 1:
                familyOfOtherBride.setOldLastName(familyOfOtherBride.getLastName());
                familyOfOtherBride.setLastName(chosenSurname);
                break;
            case 2:
                familyOfBrideChoosingSurname.setOldLastName(familyOfBrideChoosingSurname.getLastName());
                familyOfBrideChoosingSurname.setLastName(chosenSurname);
                break;
            case 0:
            default:
                break;
        }

        familyBride1.setSpouse(familyBride2.getRoot());
        familyBride2.setSpouse(familyBride1.getRoot());

        PlayerFamilyDBService.savePlayerFamily(familyBride1, null);
        PlayerFamilyDBService.savePlayerFamily(familyBride2, null);
    }

    private void setMarriageConsent(MarryPublic marryPublic, int one) {
        if (one == 0) {
            marryPublic.setConsent1(true);
        } else {
            marryPublic.setConsent2(true);
        }
    }

    private boolean validateMembers(Player[] recipients, Player... members) {
        for (Player player : members) {
            if (player == null || !player.isOnline()) {
                sendMessage(new MessageForFormatting("family_member_missing", new String[]{}), MessageType.WARNING, false, recipients);
                return false;
            }
        }
        return true;
    }

    private boolean areAllParticipantsPresent(MarryPublic marryPublic) {
        return marryPublic != null &&
                marryPublic.getProposer() != null &&
                marryPublic.getReceiver() != null &&
                marryPublic.getPriest() != null;
    }

    private void processMarriage(Player priest, PlayerFamily proposerFamily, PlayerFamily receiverFamily, Player[] recipients,
    		String vowOfNewlyweds, MarryPublic marryPublic, int one, String[] brides, String stringMarry) {
        final MessageType[] messageType = {MessageType.WARNING, MessageType.IMPORTANT};
        final String priestName = priest.getDisplayName() != null ? priest.getDisplayName() : priest.getName();
        try {
            MarriageEvent event = new MarriageEvent(priest, proposerFamily, receiverFamily, ActionInitiator.PLAYER_SELF);
            Bukkit.getPluginManager().callEvent(event);

        	String[] paymentFailedResult = validator.paymentFailed(marryPublic, marriageManager);
        	if (paymentFailedResult != null) {
        	    sendMAnnouncement(priestPrefixType, priestName, paymentFailedResult[0], MessageType.WARNING.getColor(true), Arrays.copyOfRange(paymentFailedResult, 1, paymentFailedResult.length), recipients);
        	    return;
        	}

            if (!event.isCancelled()) {
                SyncExecutor.runAsync(() -> {
                    updateFamilyData(proposerFamily, receiverFamily, marryPublic, one);

                    FamilyDetailsService.createFamilyOnMarriage(proposerFamily, receiverFamily);

                    Bukkit.getServer().getScheduler().runTaskLater(familyPlugin, () ->
                    sendMAnnouncement(priestPrefixType, priestName, stringMarry, messageType[1].getColor(true), brides, recipients), 10L);

                    marriageManager.remove(proposerFamily.getRoot());
                });
            } else {
            	String reason = event.getCancellationReason();
            	reason = (reason != null && !reason.isEmpty()) ? ": " + reason : "";
            	
                sendMessage(new MessageForFormatting("family_err_event_is_canceled" + reason, new String[]{}), messageType[0], recipients);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Exception in handleMarriage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

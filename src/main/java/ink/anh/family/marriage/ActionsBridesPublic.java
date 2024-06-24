package ink.anh.family.marriage;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.MarriageEvent;
import ink.anh.family.fdetails.FamilyDetailsService;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.PlayerFamilyDBServsce;
import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.util.OtherUtils;
import ink.anh.family.util.FamilyUtils;

public class ActionsBridesPublic extends AbstractMarriageSender {

    public ActionsBridesPublic(AnhyFamily familyPlugin) {
        super(familyPlugin);
    }

    public void accept(AsyncPlayerChatEvent event) {
        String message = event.getMessage();

        if (familyConfig.checkAnswer(message) == 0) {
            return;
        }

        event.setCancelled(true);

        Player bride1 = event.getPlayer();
        boolean consentGiven = familyConfig.checkAnswer(message) == 1;
        handleMarriage(bride1, consentGiven);
    }

    public boolean handleMarriage(Player bride1, boolean consentGiven) {
        UUID uuidBride1 = bride1 != null ? bride1.getUniqueId() : null;

        if (uuidBride1 == null || !bride1.isOnline()) {
            marriageManager.remove(uuidBride1);
            return false;
        }
        
        MarryPublic marryPublic = marriageManager.getMarryElement(uuidBride1);

        if (!areAllParticipantsPresent(marryPublic)) {
            marriageManager.remove(uuidBride1);
            return false;
        }

        int one = 0;

        if (marryPublic.getProposer() != null && marryPublic.getReceiver().getUniqueId().equals(uuidBride1)) {
            one = 1;
        }

        Player bride2 = one == 0 ? marryPublic.getProposer() : marryPublic.getReceiver();
        Player priest = marryPublic.getPriest();

        Player[] recipients = OtherUtils.getPlayersWithinRadius(bride1.getLocation(), familyConfig.getCeremonyHearingRadius());

        if (!validateMembers(recipients, bride1, priest, bride2)) {
            marriageManager.remove(bride1);
            return false;
        }

        String bride1Name = bride1.getDisplayName() != null ? bride1.getDisplayName() : bride1.getName();
        String bride2Name = bride2.getDisplayName() != null ? bride2.getDisplayName() : bride2.getName();
        String priestName = priest.getDisplayName() != null ? priest.getDisplayName() : priest.getName();
        
        priestPrefixType = MarryPrefixType.getMarryPrefixType(FamilyUtils.getFamily(priest).getGender(), 0);
        
        PlayerFamily bride1family = FamilyUtils.getFamily(bride1);
        bridePrefixType = MarryPrefixType.getMarryPrefixType(bride1family.getGender(), 1);

        if (!consentGiven) {
            marriageManager.remove(uuidBride1);
            
        	sendMAnnouncement(bridePrefixType, bride1Name, "family_marry_refuse", MessageType.WARNING.getColor(true), new String[]{bride1Name, bride2Name}, recipients);
            
            Bukkit.getServer().getScheduler().runTaskLater(familyPlugin, () -> {

            	sendMAnnouncement(priestPrefixType, priestName, "family_marry_failed", MessageType.WARNING.getColor(true), new String[]{bride1Name, bride2Name}, recipients);
            }, 10L);
            
            return true;
        }

        String family_marry_vows_man = ":§b family_marry_vows_man";
        String family_marry_vows_woman = ":§d family_marry_vows_woman";
        String family_marry_vows_nonbinary = ":§f family_marry_vows_nonbinary";
        String family_marry_success = "family_marry_success";

        PlayerFamily bride2family = FamilyUtils.getFamily(bride2);

        int genderStatus = bride2family.getGender() == Gender.MALE ? 1 : bride2family.getGender() == Gender.FEMALE ? 2 : 0;

        String vowOfNewlyweds = genderStatus == 1 ? family_marry_vows_man : genderStatus == 2 ? family_marry_vows_woman : family_marry_vows_nonbinary;

        setMarriageConsent(marryPublic, one);
        
    	sendMAnnouncement(bridePrefixType, bride1Name, vowOfNewlyweds, MessageType.ESPECIALLY.getColor(true), new String[]{bride1Name, bride2Name}, recipients);

        if (!marryPublic.areBothConsentsGiven()) {
        	sendMAnnouncement(priestPrefixType, priestName, "family_marry_waiting_for_consent", MessageType.ESPECIALLY.getColor(true), new String[]{bride1Name, bride2Name}, recipients);

            return true;
        }

        final int num = one;

        SyncExecutor.runSync(() -> {
            processMarriage(priest, bride1family, bride2family, recipients, vowOfNewlyweds, marryPublic, num, new String[]{bride1Name, bride2Name}, family_marry_success);
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

        PlayerFamilyDBServsce.savePlayerFamily(familyBride1, null);
        PlayerFamilyDBServsce.savePlayerFamily(familyBride2, null);
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
        final MessageType[] messageType = {MessageType.WARNING, MessageType.ESPECIALLY};
        final String priestName = priest.getDisplayName() != null ? priest.getDisplayName() : priest.getName();
        try {
            MarriageEvent event = new MarriageEvent(priest, proposerFamily, receiverFamily, ActionInitiator.PLAYER_SELF);
            Bukkit.getPluginManager().callEvent(event);

        	String[] paymentFailedResult = new MarriageValidator(familyPlugin, true).paymentFailed(marryPublic, marriageManager);
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

package ink.anh.family.marriage;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.PlayerFamilyDBService;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.OtherUtils;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.MarriageEvent;
import ink.anh.family.fdetails.FamilyDetailsService;
import ink.anh.api.utils.SyncExecutor;

public class ActionsBridesPrivate extends AbstractMarriageSender {

	private static final String priestName = "family_marry_private_priest";

    public ActionsBridesPrivate(AnhyFamily familyPlugin) {
        super(familyPlugin, false);
        this.priestPrefixType = MarryPrefixType.getMarryPrefixType(null, 2);
    }

    public void proposePrivateMarriage(CommandSender sender, String[] args) {

        if (!validator.validateCommandInput(sender, args)) {
            return;
        }

        Player proposer = (Player)sender;
        Player receiver = Bukkit.getPlayerExact(args[1]);

        if (receiver == null || !receiver.isOnline()) {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[]{args[1]}), MessageType.WARNING, sender); // +
            return;
        }

        Player[] recipients = OtherUtils.getPlayersWithinRadius(proposer.getLocation(), manager.getFamilyConfig().getCeremonyRadius());

		String[] validateCeremonyConditionsResult = validator.validateCeremonyConditions(proposer, receiver);
		if (validateCeremonyConditionsResult != null) {
			 String members = String.join(", ", Arrays.stream(validateCeremonyConditionsResult).skip(1).filter(Objects::nonNull).toArray(String[]::new));
		        sendMAnnouncement(priestPrefixType, priestName, validateCeremonyConditionsResult[0], MessageType.WARNING.getColor(true), new String[] {members}, recipients);
		        return ;
		}
        
        String[] validatePerm = validator.validatePermissions(proposer, receiver, recipients);
        if (validatePerm != null) {
            if (validatePerm[0] == null) {
                // Переривання роботи без відправлення складного повідомлення
                return;
            } else {
                String members = String.join(", ", Arrays.copyOfRange(validatePerm, 1, validatePerm.length));
                sendMAnnouncement(priestPrefixType, priestName, validatePerm[0], MessageType.ESPECIALLY.getColor(true), new String[] {members}, recipients);
                return;
            }
        }

        PlayerFamily proposerFamily = FamilyUtils.getFamily(proposer);
        PlayerFamily receiverFamily = FamilyUtils.getFamily(receiver);
        
        String[] validateCompatibilityResult = validator.validateMarriageCompatibility(proposerFamily, receiverFamily, recipients);
        if (validateCompatibilityResult != null) {
        	String members = String.join(", ", Arrays.stream(Arrays.copyOfRange(validateCompatibilityResult, 1, validateCompatibilityResult.length))
                    .filter(Objects::nonNull)
                    .toArray(String[]::new));

            sendMAnnouncement(priestPrefixType, priestName, validateCompatibilityResult[0], MessageType.WARNING.getColor(true), new String[] {members}, recipients);
            return;
        }

        String[] chosenSurname = proposerFamily.getLastName();

        MarryPrivate proposal = new MarryPrivate(proposer, receiver, chosenSurname);

        if (marriageManager.addProposal(proposal)) {
            String proposerName = proposer.getDisplayName() != null ? proposer.getDisplayName() : proposer.getName();
            String receiverName = receiver.getDisplayName() != null ? receiver.getDisplayName() : receiver.getName();
            
        	sendMAnnouncement(priestPrefixType, priestName, "family_proposal_sent", MessageType.ESPECIALLY.getColor(true), new String[]{receiverName}, new Player[] {proposer});
        	sendMAnnouncement(priestPrefixType, priestName, "family_proposal_received", MessageType.ESPECIALLY.getColor(true), new String[]{proposerName}, new Player[] {receiver});
        	sendPriestAcceptMessage(priestPrefixType, priestName, new Player[] {receiver});
        } else {
            sendMessage(new MessageForFormatting("family_proposal_failed", new String[]{receiver.getName()}), MessageType.WARNING, sender); // +
        }
    }

    public boolean acceptPrivateMarriage(Player receiver) {
        MarryPrivate proposal = marriageManager.getProposal(receiver);

        if (proposal == null) {
            return false;
        }

        Player proposer = proposal.getProposer();
        PlayerFamily proposerFamily = FamilyUtils.getFamily(proposer);
        PlayerFamily receiverFamily = FamilyUtils.getFamily(receiver);
        
        bridePrefixType = MarryPrefixType.getMarryPrefixType(receiverFamily.getGender(), 1);

        Player[] players = getPlayersWithRadius(new Player[] {receiver, proposer}, 10);

        SyncExecutor.runSync(() -> handleMarriage(proposerFamily, receiverFamily, ActionInitiator.PLAYER_SELF, players, proposal));
		return true;
    }

    public boolean refusePrivateMarriage(Player receiver) {
        MarryPrivate proposal = marriageManager.getProposal(receiver);

        if (proposal == null) {
            return false;
        }

        Player proposer = proposal.getProposer();

        marriageManager.removeProposal(proposal);
        
    	sendMAnnouncement(priestPrefixType, priestName, "family_proposal_refused", MessageType.NORMAL.getColor(true),
    			new String[]{proposer.getDisplayName() != null ? proposer.getDisplayName() : proposer.getName()}, new Player[] {receiver});
        
    	sendMAnnouncement(priestPrefixType, priestName, "family_proposal_refused_sender", MessageType.NORMAL.getColor(true),
    			new String[]{receiver.getDisplayName() != null ? receiver.getDisplayName() : receiver.getName()}, new Player[] {proposer});
		return true;
    }

	private void updateFamilyData(PlayerFamily familyOfBrideChoosingSurname, PlayerFamily familyOfOtherBride, MarryBase marryBase) {
		String[] chosenSurname = marryBase.getChosenSurname();

        familyOfOtherBride.setOldLastName(familyOfOtherBride.getLastName());
        familyOfOtherBride.setLastName(chosenSurname);
        
	    // Оновити статус спільника
	    familyOfBrideChoosingSurname.setSpouse(familyOfOtherBride.getRoot());
	    familyOfOtherBride.setSpouse(familyOfBrideChoosingSurname.getRoot());

	    // Зберегти змінені дані сімей
        PlayerFamilyDBService.savePlayerFamily(familyOfBrideChoosingSurname, null);
        PlayerFamilyDBService.savePlayerFamily(familyOfOtherBride, null);
	}

    private void handleMarriage(PlayerFamily proposerFamily, PlayerFamily receiverFamily, ActionInitiator initiator, Player[] players,  MarryBase marryBase) {
        try {
            MarriageEvent event = new MarriageEvent(null, proposerFamily, receiverFamily, initiator);
            Bukkit.getPluginManager().callEvent(event);

        	String[] paymentFailedResult = validator.paymentFailed(marryBase, marriageManager);
        	if (paymentFailedResult != null) {
        	    sendMAnnouncement(priestPrefixType, priestName, paymentFailedResult[0], MessageType.WARNING.getColor(true), Arrays.copyOfRange(paymentFailedResult, 1, paymentFailedResult.length), players);
        	    return;
        	}
        	
            if (!event.isCancelled()) {
                SyncExecutor.runAsync(() -> {
                	
                	updateFamilyData(proposerFamily, receiverFamily, marryBase);
                	
                	FamilyDetailsService.createFamilyOnMarriage(proposerFamily, receiverFamily);

                    String proposerName = players[0].getDisplayName() != null ? players[0].getDisplayName() : players[0].getName();
                    String receiverName = players[1].getDisplayName() != null ? players[1].getDisplayName() : players[1].getName();
                    
                	sendMAnnouncement(priestPrefixType, priestName, "family_proposal_accepted_sender", MessageType.IMPORTANT.getColor(true),
                			new String[]{proposerName}, new Player[] {players[1]});
                    
                	sendMAnnouncement(priestPrefixType, priestName, "family_proposal_accepted", MessageType.NORMAL.getColor(true),
                			new String[]{receiverName}, new Player[] {players[0]});
                    
                	sendMAnnouncement(priestPrefixType, priestName, "family_marriage_successful", MessageType.NORMAL.getColor(true),
                			new String[]{proposerName, receiverName}, players);
                });
            } else {
                sendMessage(new MessageForFormatting("family_err_event_is_canceled", new String[]{}), MessageType.WARNING, players); // +
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Exception in handleMarriage: " + e.getMessage());
            e.printStackTrace();
        }

        marriageManager.removeProposal((MarryPrivate) marryBase);
    }
    
    private Player[] getPlayersWithRadius(Player[] players, double radius) {
        Player proposer = players[1];
        Location center = proposer.getLocation();
        
        List<Player> nearbyPlayers = Bukkit.getOnlinePlayers().stream()
            .filter(player -> !player.equals(players[0]) && !player.equals(players[1])
                    && player.getWorld().equals(center.getWorld())
                    && player.getLocation().distance(center) <= radius)
            .collect(Collectors.toList());

        return Stream.concat(Stream.of(players), nearbyPlayers.stream())
            .toArray(Player[]::new);
    }
}

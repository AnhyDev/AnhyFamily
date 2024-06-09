package ink.anh.family.marriage;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.OtherUtils;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.MarriageEvent;
import ink.anh.api.utils.SyncExecutor;

public class ActionsBridesPrivate extends Sender {

    private GlobalManager manager;
    private MarriageManager marriageManager;
    private MarriageValidator validator;

    public ActionsBridesPrivate(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.manager = GlobalManager.getInstance();
        this.marriageManager = GlobalManager.getInstance().getMarriageManager();
        this.validator = new MarriageValidator(familyPlugin, false);
    }

    public void proposePrivateMarriage(CommandSender sender, String[] args) {

        if (!validator.validateCommandInput(sender, args)) {
            return;
        }

        Player proposer = (Player)sender;
        Player receiver = Bukkit.getPlayerExact(args[1]);

        if (receiver == null || !receiver.isOnline()) {
            sendMessage(new MessageForFormatting("family_err_player_not_found", new String[]{args[1]}), MessageType.WARNING, sender);
            return;
        }

        Player[] recipients = OtherUtils.getPlayersWithinRadius(proposer.getLocation(), manager.getFamilyConfig().getCeremonyRadius());

        if (!validator.validateCeremonyConditions(proposer, receiver, recipients)) {
            return;
        }

        if (!validator.validateCeremonyParticipants(proposer, receiver, recipients)) {
            return;
        }

        if (!validator.validatePermissions(proposer, receiver, recipients)) {
            return;
        }

        String[] chosenSurname = FamilyUtils.getFamily(proposer).getLastName();

        MarryPrivate proposal = new MarryPrivate(proposer, receiver, chosenSurname);

        if (marriageManager.addProposal(proposal)) {
            sendMessage(new MessageForFormatting("family_proposal_sent", new String[]{receiver.getName()}), MessageType.NORMAL, sender);
            sendMessage(new MessageForFormatting("family_proposal_received", new String[]{proposer.getName()}), MessageType.NORMAL, receiver);
        } else {
            sendMessage(new MessageForFormatting("family_proposal_failed", new String[]{receiver.getName()}), MessageType.WARNING, sender);
        }
    }

    public void acceptPrivateMarriage(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
            return;
        }

        Player receiver = (Player)sender;
        MarryPrivate proposal = marriageManager.getProposal(receiver);

        if (proposal == null) {
            sendMessage(new MessageForFormatting("family_err_no_proposal", new String[]{}), MessageType.WARNING, sender);
            return;
        }

        Player proposer = proposal.getProposer();
        PlayerFamily proposerFamily = FamilyUtils.getFamily(proposer.getUniqueId());
        PlayerFamily receiverFamily = FamilyUtils.getFamily(receiver.getUniqueId());

        MessageForFormatting messageTrue = new MessageForFormatting("family_proposal_accepted", new String[]{proposer.getName()});
        MessageForFormatting messageFalse = new MessageForFormatting("family_proposal_failed", new String[]{proposer.getName()});
        CommandSender[] senders = {sender, proposer};

        SyncExecutor.runSync(() -> handleMarriage(proposerFamily, receiverFamily, ActionInitiator.PLAYER_SELF, senders, messageTrue, messageFalse, proposal));
    }

    public void refusePrivateMarriage(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
            return;
        }

        Player receiver = (Player)sender;
        MarryPrivate proposal = marriageManager.getProposal(receiver);

        if (proposal == null) {
            sendMessage(new MessageForFormatting("family_err_no_proposal", new String[]{}), MessageType.WARNING, sender);
            return;
        }

        Player proposer = proposal.getProposer();
        sendMessage(new MessageForFormatting("family_proposal_refused", new String[]{proposer.getName()}), MessageType.NORMAL, sender);
        sendMessage(new MessageForFormatting("family_proposal_refused_sender", new String[]{receiver.getName()}), MessageType.NORMAL, proposer);
    }

	private void updateFamilyData(PlayerFamily familyOfBrideChoosingSurname, PlayerFamily familyOfOtherBride, MarryBase marryBase) {
		String[] chosenSurname = marryBase.getChosenSurname();

        familyOfOtherBride.setOldLastName(familyOfOtherBride.getLastName());
        familyOfOtherBride.setLastName(chosenSurname);
        
	    // Оновити статус спільника
	    familyOfBrideChoosingSurname.setSpouse(familyOfOtherBride.getRoot());
	    familyOfOtherBride.setSpouse(familyOfBrideChoosingSurname.getRoot());

	    // Зберегти змінені дані сімей
	    FamilyUtils.saveFamily(familyOfBrideChoosingSurname);
	    FamilyUtils.saveFamily(familyOfOtherBride);
	}

    private void handleMarriage(PlayerFamily proposerFamily, PlayerFamily receiverFamily, ActionInitiator initiator, CommandSender[] senders,
    		MessageForFormatting messageTrue, MessageForFormatting messageFalse, MarryBase marryBase) {
        final MessageType[] messageType = {MessageType.WARNING};
        try {
            MarriageEvent event = new MarriageEvent(null, proposerFamily, receiverFamily, initiator);
            Bukkit.getPluginManager().callEvent(event);
        	
        	if (validator.paymentFailed(marryBase, (Player[]) senders, marriageManager)) {
        		event.cancellEvent("Error in payment of peace enterprise");
        		return;
        	}

            if (!event.isCancelled()) {
                SyncExecutor.runAsync(() -> {
                	
                	updateFamilyData(proposerFamily, receiverFamily, marryBase);
                	
                	FamilyHandler.createFamilyOnMarriage(proposerFamily, receiverFamily);

                    messageType[0] = MessageType.IMPORTANT;
                    sendMessage(messageTrue, messageType[0], senders);
                });
            } else {
                sendMessage(new MessageForFormatting("family_err_event_is_canceled", new String[]{}), MessageType.WARNING, senders);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Exception in handleMarriage: " + e.getMessage());
            e.printStackTrace();
        }

        marriageManager.removeProposal((MarryPrivate) marryBase);
    }
}

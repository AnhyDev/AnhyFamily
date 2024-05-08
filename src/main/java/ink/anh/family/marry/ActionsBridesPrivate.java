package ink.anh.family.marry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.common.Family;
import ink.anh.family.util.FamilyUtils;

public class ActionsBridesPrivate extends Sender {

    private MarriageManager marriageManager;

    public ActionsBridesPrivate(AnhyFamily familyPlugin) {
        super(familyPlugin.getGlobalManager());
        this.marriageManager = familyPlugin.getMarriageManager();
    }

    public void proposePrivateMarriage(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
            return;
        }

        Player proposer = (Player)sender;
        Player receiver = Bukkit.getPlayerExact(args[1]);

        if (receiver == null || !receiver.isOnline()) {
            sendMessage(new MessageForFormatting("family_err_player_not_found", new String[]{args[1]}), MessageType.WARNING, sender);
            return;
        }

        String[] chosenSurname = {""};
        if (args.length > 2) {
            chosenSurname = new String[]{args[2]};
        }

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
        Family proposerFamily = FamilyUtils.getFamily(proposer.getUniqueId());
        Family receiverFamily = FamilyUtils.getFamily(receiver.getUniqueId());

        // Оновити інформацію про сім'ю
        proposerFamily.setSpouse(receiverFamily.getRoot());
        receiverFamily.setSpouse(proposerFamily.getRoot());

        if (proposal.getChosenSurname().length > 0 && !proposal.getChosenSurname()[0].isEmpty()) {
            receiverFamily.setOldLastName(receiverFamily.getLastName());
            receiverFamily.setLastName(proposal.getChosenSurname());
        }

        FamilyUtils.saveFamily(proposerFamily);
        FamilyUtils.saveFamily(receiverFamily);

        sendMessage(new MessageForFormatting("family_proposal_accepted", new String[]{proposer.getName()}), MessageType.NORMAL, sender);
        sendMessage(new MessageForFormatting("family_proposal_accepted_sender", new String[]{receiver.getName()}), MessageType.NORMAL, proposer);

        marriageManager.removeProposal(proposal);
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

        marriageManager.removeProposal(proposal);
    }
}

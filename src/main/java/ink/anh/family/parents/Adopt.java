package ink.anh.family.parents;

import java.util.UUID;  
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.Permissions;
import ink.anh.family.Sender;
import ink.anh.family.common.Family;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

public class Adopt extends Sender {

    public Adopt(AnhyFamily familiPlugin) {
        super(familiPlugin);
    }

    public boolean execAdopt(CommandSender sender, String[] args) {
        String sendername = sender.getName();
        Player player = null;
        ParentManager manager = familiPlugin.getParentManager();
        
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format  /family adopt <player1>", null), MessageType.WARNING, sender);
            return false;
        }
        
        if (sender instanceof Player) {
            player = (Player) sender;
            if (!player.hasPermission(Permissions.FAMILY_USER)) {
                sendMessage(new MessageForFormatting("family_err_not_have_permission", null), MessageType.WARNING, sender);
                return false;
            }
        } else if (sendername.equalsIgnoreCase("CONSOLE")) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", null), MessageType.WARNING, sender);
            return false;
        }
        
        UUID uuid = player.getUniqueId();
        String adopter1Name = args[1];
        Player player1 = Bukkit.getPlayerExact(adopter1Name);
        
        if (player1 == null) {
            sendMessage(new MessageForFormatting("family_player" + adopter1Name + "family_player_not_found", null), MessageType.WARNING, sender);
            return false;
        }
        
        UUID uuid1 = player1.getUniqueId();
        Family family1 = FamilyUtils.getFamily(uuid1);
        
        if (family1 == null) {
            sendMessage(new MessageForFormatting("family_player" + adopter1Name + "family_player_not_found", null), MessageType.WARNING, sender);
            return false;
        }
        
        if (family1.getFather() != null || family1.getMother() != null) {
            sendMessage(new MessageForFormatting("family_player" + adopter1Name + "family_adopt_error_already_has_parents", null), MessageType.WARNING, sender);
            return false;
        }
        
        int result = manager.addOrUpdateParent(uuid1, uuid);
        
        switch (result) {
            case -2:
                sendMessage(new MessageForFormatting("family_adopt_error_unknown", null), MessageType.WARNING, sender);
                return false;
            case -1:
                sendMessage(new MessageForFormatting("family_adopt_error_self_adoption", null), MessageType.WARNING, sender);
                return false;
            case 0:
                sendMessage(new MessageForFormatting("family_adopt_error_duplicate_requests", null), MessageType.WARNING, sender);
                return false;
            case 1:
                sendMessage(new MessageForFormatting("family_adopt_request_sent_for" + adopter1Name + "family_adopt_waiting_for_second_parent", null), MessageType.IMPORTANT, sender);
                sendMessage(new MessageForFormatting("family_adopt_parent_offer_received", null), MessageType.IMPORTANT, player1);
                return true;
            case 2:
                sendMessage(new MessageForFormatting("family_adopt_request_sent_for" + adopter1Name + "family_adopt_waiting_for_child_decision", null), MessageType.IMPORTANT, sender);
                sendMessage(new MessageForFormatting("family_adopt_parent_offer_received", null), MessageType.IMPORTANT, player1);
                return true;
            case 3:
                sendMessage(new MessageForFormatting("family_adopt_request_already_sent", null), MessageType.WARNING, sender);
                return false;
        }
        sendMessage(new MessageForFormatting("family_error_generic", null), MessageType.WARNING, sender);
        return false;
    }

    public boolean exeAddParents(CommandSender sender) {
        Player player = null;
        ParentManager manager = familiPlugin.getParentManager();
        
        if (sender instanceof Player) {
            player = (Player) sender;
            if (!player.hasPermission(Permissions.FAMILY_USER)) {
                sendMessage(new MessageForFormatting("family_err_not_have_permission", null), MessageType.WARNING, sender);
                return false;
            }
        } else {
            sendMessage(new MessageForFormatting("family_err_command_only_player", null), MessageType.WARNING, sender);
            return false;
        }
        
        String adoptedName = player.getName();
        UUID uuid = player.getUniqueId();
        Family family = FamilyUtils.getFamily(uuid);
        
        if (family == null) {
            sendMessage(new MessageForFormatting("family_player" + adoptedName + "family_player_not_found", null), MessageType.WARNING, sender);
            return false;
        }
        
        UUID[] parents = manager.getParentElement(uuid);
        
        if (parents == null || parents[0] == null || parents[1] == null || parents[2] == null) {
            sendMessage(new MessageForFormatting("family_accept_error_no_parents", null), MessageType.WARNING, sender);
            return false;
        }

        UUID uuid1 = parents[1];
        UUID uuid2 = parents[2];

        Family family1 = FamilyUtils.getFamily(uuid1);
        
        if (family1 == null) {
            sendMessage(new MessageForFormatting("family_accept_error_parent1_missing", null), MessageType.WARNING, sender);
            return false;
        }
        
        Family family2 = FamilyUtils.getFamily(uuid2);
        
        if (family2 == null) {
            sendMessage(new MessageForFormatting("family_accept_error_parent2_missing", null), MessageType.WARNING, sender);
            return false;
        }

        String adopter1Name = family1.getDisplayName();
        String adopter2Name = family2.getDisplayName();
        
        FamilyAdoption utilsAdopt = new FamilyAdoption(familiPlugin);
        
        if (!utilsAdopt.adoption(family, family1, family2)) {
            sendMessage(new MessageForFormatting(adopter1Name + ", " + adopter2Name + "family_accept_error_cannot_adopt" + adoptedName, null), MessageType.WARNING, sender);
            return false;
        }


        Player player1 = Bukkit.getPlayer(uuid1);
        Player player2 = Bukkit.getPlayer(uuid2);
        
        sendMessage(new MessageForFormatting(adopter1Name + ", " + adopter2Name + "family_accept_success" + adoptedName, null), MessageType.WARNING, player, player1, player2);
        return true;
    }

    public boolean forceAdopt(CommandSender sender, String[] args) {
        if (!(sender instanceof Player && sender.hasPermission(Permissions.FAMILY_ADMIN)) && !sender.getName().equalsIgnoreCase("CONSOLE")) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", null), MessageType.WARNING, sender);
            return false;
        }

        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format  /family forceadopt <adoptedPlayer> <adopterPlayer>", null), MessageType.WARNING, sender);
            return false;
        }

        String adoptedPlayerName = args[1];
        String adopterPlayerName = args[2];

        Family adoptedFamily = FamilyUtils.getFamily(adoptedPlayerName);
        Family adopterFamily = FamilyUtils.getFamily(adopterPlayerName);

        if (adoptedFamily == null || adopterFamily == null) {
            sendMessage(new MessageForFormatting("family_err_family_not_found", null), MessageType.WARNING, sender);
            return false;
        }

        FamilyAdoption adoption = new FamilyAdoption(familiPlugin);
        if (!adoption.adoption(adoptedFamily, adopterFamily)) {
            sendMessage(new MessageForFormatting("family_err_adoption_failed", null), MessageType.WARNING, sender);
            return false;
        }

        Player player1 = Bukkit.getPlayer(adoptedFamily.getRoot());
        Player player2 = Bukkit.getPlayer(adopterFamily.getRoot());
        
        sendMessage(new MessageForFormatting(adopterFamily + " family_accept_success_adoption " + adopterFamily, null), MessageType.IMPORTANT, sender, player1, player2);
        return true;
    }
}
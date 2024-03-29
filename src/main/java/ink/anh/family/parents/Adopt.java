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

    public Adopt(AnhyFamily familyPlugin) {
        super(familyPlugin);
    }

    public boolean adoption(CommandSender sender, String[] args) {
        String sendername = sender.getName();
        Player player = null;
        ParentManager manager = familyPlugin.getParentManager();
        
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
        String adoptedName = args[1];
        Player player1 = Bukkit.getPlayerExact(adoptedName);
        
        if (player1 == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[] {adoptedName}), MessageType.WARNING, sender);
            return false;
        }
        
        UUID uuid1 = player1.getUniqueId();
        Family family1 = FamilyUtils.getFamily(player1);
        
        if (family1 == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[] {adoptedName}), MessageType.WARNING, sender);
            return false;
        }
        
        if (family1.getFather() != null || family1.getMother() != null) {
            sendMessage(new MessageForFormatting("family_adopt_error_already_has_parents", new String[] {adoptedName}), MessageType.WARNING, sender);
            return false;
        }
        
        int result = manager.addOrUpdateParent(uuid1, uuid);
        
        switch (result) {
            case -2:
                sendMessage(new MessageForFormatting("family_error_generic", null), MessageType.WARNING, sender);
                return false;
            case -1:
                sendMessage(new MessageForFormatting("family_adopt_error_self_adoption", null), MessageType.WARNING, sender);
                return false;
            case 0:
                sendMessage(new MessageForFormatting("family_adopt_error_duplicate_requests", null), MessageType.WARNING, sender);
                return false;
            case 1:
                sendMessage(new MessageForFormatting("family_adopt_waiting_for_second_parent", new String[] {adoptedName}), MessageType.IMPORTANT, sender);
                sendMessage(new MessageForFormatting("family_adopt_parent_offer_received", null), MessageType.IMPORTANT, player1);
                adoptionSheduler(player, manager);
                return true;
            case 2:
                sendMessage(new MessageForFormatting("family_adopt_waiting_for_child_decision", new String[] {adoptedName}), MessageType.IMPORTANT, sender);
                sendMessage(new MessageForFormatting("family_adopt_parent_offer_received", null), MessageType.IMPORTANT, player1);
                adoptionSheduler(player, manager);
                return true;
            case 3:
                sendMessage(new MessageForFormatting("family_adopt_request_already_sent", null), MessageType.WARNING, sender);
                return false;
        }
        sendMessage(new MessageForFormatting("family_error_generic", null), MessageType.WARNING, sender);
        return false;
    }

    public boolean cancelAdoption(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", null), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        ParentManager manager = familyPlugin.getParentManager();
        UUID[] parentRequest = manager.getParentElementByParent(playerUUID);

        // Перевірка, чи існує заявка на усиновлення
        if (parentRequest == null || parentRequest[0] == null) {
            sendMessage(new MessageForFormatting("family_cancel_adoption_error_no_request", null), MessageType.WARNING, sender);
            return false;
        }

        Player adopted = Bukkit.getPlayer(parentRequest[0]);
        Player otherAdopter = null;
        boolean isInitiator = parentRequest[1].equals(playerUUID);
        if (!isInitiator && parentRequest.length > 2) {
            isInitiator = parentRequest[2].equals(playerUUID);
            otherAdopter = Bukkit.getPlayer(parentRequest[1]);
        } else if (isInitiator) {
            otherAdopter = Bukkit.getPlayer(parentRequest[2]);
        }

        if (isInitiator) {
            // Якщо існує інший усиновлювач і він онлайн
            if (otherAdopter != null && otherAdopter.isOnline()) {
                parentRequest[isInitiator ? 1 : 2] = null; // Видаляємо ініціатора з пропозиції
                sendMessage(new MessageForFormatting("family_cancel_adoption_partial_success", 
                		new String[] {sender.getName(), adopted.getName(), otherAdopter.getName()}), MessageType.IMPORTANT, sender, otherAdopter, adopted);
            } else {
                // Видаляємо пропозицію повністю
                manager.removeParent(parentRequest[0]);
                sendMessage(new MessageForFormatting("family_cancel_adoption_full_success", 
                		new String[] {sender.getName(), adopted.getName()}), MessageType.IMPORTANT, otherAdopter, adopted);
            }
        } else {
            // Якщо гравець не є ініціатором
            sendMessage(new MessageForFormatting("family_cancel_adoption_error_no_request", null), MessageType.WARNING, sender);
            return false;
        }

        return true;
    }
    
    private boolean adoptionSheduler(Player player, ParentManager manager) {
        
        // UUID гравця, який подає заявку
        UUID uuid = (player).getUniqueId();

        // Планування задачі для перевірки статусу усиновлення через 5 хвилин
        Bukkit.getScheduler().runTaskLater(familyPlugin, () -> {
            // Перевірка, чи існує заявка на усиновлення
            if (manager.getParentElement(uuid) != null) {
                manager.removeParent(uuid);
                sendMessage(new MessageForFormatting("family_accept_adoption_close", null), MessageType.WARNING, player);
            }
        }, 5 * 60 * 20);

        return true;
    }
    
    public boolean accept(CommandSender sender) {
        Player player = null;
        ParentManager manager = familyPlugin.getParentManager();
        
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
        
        String adoptedName = player.getDisplayName();
        UUID uuid = player.getUniqueId();
        Family family = FamilyUtils.getFamily(player);
        
        if (family == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[] {adoptedName}), MessageType.WARNING, sender);
            return false;
        }
        
        UUID[] parents = manager.getParentElement(uuid);
        
        if (parents == null || parents[0] == null || parents[1] == null || parents[2] == null) {
            sendMessage(new MessageForFormatting("family_accept_error_no_parents", null), MessageType.WARNING, sender);
            manager.removeParent(uuid);
            return false;
        }

        UUID uuid1 = parents[1];
        UUID uuid2 = parents[2];

        Player player1 = Bukkit.getPlayer(uuid1);
        Player player2 = Bukkit.getPlayer(uuid2);
        
        if (player1 == null || !player1.isOnline() || player2 == null || !player2.isOnline()) {
            sendMessage(new MessageForFormatting("family_accept_error_parrent_not_online", null), MessageType.WARNING, sender);
            manager.removeParent(uuid);
            return false;
        	
        }

        Family family1 = FamilyUtils.getFamily(player1);
        
        if (family1 == null) {
            sendMessage(new MessageForFormatting("family_accept_error_parent1_missing", null), MessageType.WARNING, sender);
            manager.removeParent(uuid);
            return false;
        }
        
        Family family2 = FamilyUtils.getFamily(player2);
        
        if (family2 == null) {
            sendMessage(new MessageForFormatting("family_accept_error_parent2_missing", null), MessageType.WARNING, sender);
            manager.removeParent(uuid);
            return false;
        }

        String adopter1Name = player1.getDisplayName();
        String adopter2Name = player2.getDisplayName();
        
        FamilyAdoption utilsAdopt = new FamilyAdoption(familyPlugin);
        
        if (!utilsAdopt.adoption(family, family1, family2)) {
            sendMessage(new MessageForFormatting("family_accept_error_cannot_adopt" + adoptedName, new String[] {adopter1Name, adopter2Name, adoptedName}), MessageType.WARNING, sender);
            manager.removeParent(uuid);
            return false;
        }
        
        sendMessage(new MessageForFormatting("family_accept_success" + adoptedName, new String[] {adopter1Name, adopter2Name, adoptedName}), MessageType.WARNING, player, player1, player2);
        manager.removeParent(uuid);
        return true;
    }

    public boolean declineAdoption(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", null), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        ParentManager manager = familyPlugin.getParentManager();
        UUID[] parents = manager.getParentElement(playerUUID);

        // Перевірка, чи існує заявка на усиновлення
        if (parents == null || parents[0] == null) {
            sendMessage(new MessageForFormatting("family_cancel_adoption_error_no_request", null), MessageType.WARNING, sender);
            manager.removeParent(playerUUID);
            return false;
        }

        // Видалення заявки на усиновлення
        manager.removeParent(playerUUID);

        // Отримання гравців-батьків
        Player parent1 = parents[1] != null ? Bukkit.getPlayer(parents[1]) : null;
        Player parent2 = parents[2] != null ? Bukkit.getPlayer(parents[2]) : null;

        // Повідомлення батьків про відмову
        sendMessage(new MessageForFormatting("family_decline_notify_parent", new String[] {player.getDisplayName()}), MessageType.WARNING, parent1, parent2);

        // Повідомлення гравцеві про успішну відмову
        sendMessage(new MessageForFormatting("family_decline_success", null), MessageType.IMPORTANT, sender);
        manager.removeParent(playerUUID);

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

        FamilyAdoption adoption = new FamilyAdoption(familyPlugin);
        if (!adoption.adoption(adoptedFamily, adopterFamily)) {
            sendMessage(new MessageForFormatting("family_err_adoption_failed", null), MessageType.WARNING, sender);
            return false;
        }

        Player player1 = Bukkit.getPlayer(adoptedFamily.getRoot());
        Player player2 = Bukkit.getPlayer(adopterFamily.getRoot());
        
        sendMessage(new MessageForFormatting("family_accept_success_adoption", 
        		new String[] {player1.getDisplayName(), player2.getDisplayName()}), MessageType.IMPORTANT, sender, player1, player2);
        return true;
    }
}
package ink.anh.family.parents;

import java.util.UUID;  
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.FamilyConfig;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.AdoptionEvent;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsHandler;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

public class Adopt extends Sender {

    private AnhyFamily familyPlugin;
    private GlobalManager globalManager;

    public Adopt(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
        this.globalManager = GlobalManager.getInstance();
    }

    public boolean adoption(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format /adopt invite <player1>", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (sender instanceof Player) ? (Player) sender : null;

        if (player == null) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        if (!player.hasPermission(Permissions.FAMILY_USER)) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        String adoptedName = args[1];
        Player player1 = Bukkit.getPlayerExact(adoptedName);

        if (player1 == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[]{adoptedName}), MessageType.WARNING, sender);
            return false;
        }

        UUID uuid1 = player1.getUniqueId();
        PlayerFamily family1 = FamilyUtils.getFamily(player1);

        if (family1 == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[]{adoptedName}), MessageType.WARNING, sender);
            return false;
        }

        if (family1.getFather() != null || family1.getMother() != null) {
            sendMessage(new MessageForFormatting("family_adopt_error_already_has_parents", new String[]{adoptedName}), MessageType.WARNING, sender);
            return false;
        }

        ParentManager manager = globalManager.getParentManager();
        int result = manager.addOrUpdateParent(uuid1, player.getUniqueId());

        switch (result) {
            case -2:
                sendMessage(new MessageForFormatting("family_error_generic", new String[]{}), MessageType.WARNING, sender);
                return false;
            case -1:
                sendMessage(new MessageForFormatting("family_adopt_error_self_adoption", new String[]{}), MessageType.WARNING, sender);
                return false;
            case 0:
                sendMessage(new MessageForFormatting("family_adopt_error_duplicate_requests", new String[]{}), MessageType.WARNING, sender);
                return false;
            case 1:
                sendMessage(new MessageForFormatting("family_adopt_waiting_for_second_parent", new String[]{adoptedName}), MessageType.IMPORTANT, sender);
                sendMessage(new MessageForFormatting("family_adopt_parent_offer_received", new String[]{}), MessageType.IMPORTANT, player1);
                return adoptionScheduler(player, manager);
            case 2:
                sendMessage(new MessageForFormatting("family_adopt_waiting_for_child_decision", new String[]{adoptedName}), MessageType.IMPORTANT, sender);
                sendMessage(new MessageForFormatting("family_adopt_parent_offer_received", new String[]{}), MessageType.IMPORTANT, player1);
                return adoptionScheduler(player, manager);
            case 3:
                sendMessage(new MessageForFormatting("family_adopt_request_already_sent", new String[]{}), MessageType.WARNING, sender);
                return false;
        }
        sendMessage(new MessageForFormatting("family_error_generic", new String[]{}), MessageType.WARNING, sender);
        return false;
    }

    private boolean validateAdoptionConfig(PlayerFamily family1, PlayerFamily family2, CommandSender... sender) {
        FamilyConfig familyConfig = globalManager.getFamilyConfig();
        boolean nonTraditionalAllowed = familyConfig.isNonBinaryAdopt();
        
        if (!nonTraditionalAllowed && !FamilyUtils.areGendersCompatibleForTraditional(family1, family2)) {
            sendMessage(new MessageForFormatting("family_adopt_failed_traditional", new String[]{}), MessageType.WARNING, sender);
            return false;
        }
        return true;
    }

    public boolean cancelAdoption(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        ParentManager manager = globalManager.getParentManager();
        UUID[] parentRequest = manager.getParentElementByParent(playerUUID);

        if (parentRequest == null || parentRequest[0] == null) {
            sendMessage(new MessageForFormatting("family_cancel_adoption_error_no_request", new String[]{}), MessageType.WARNING, sender);
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
            if (otherAdopter != null && otherAdopter.isOnline()) {
                parentRequest[isInitiator ? 1 : 2] = null;
                sendMessage(new MessageForFormatting("family_cancel_adoption_partial_success", new String[]{sender.getName(), adopted.getName(), otherAdopter.getName()}), MessageType.IMPORTANT, sender, otherAdopter, adopted);
            } else {
                manager.removeParent(parentRequest[0]);
                sendMessage(new MessageForFormatting("family_cancel_adoption_full_success", new String[]{sender.getName(), adopted.getName()}), MessageType.IMPORTANT, sender, adopted);
            }
        } else {
            sendMessage(new MessageForFormatting("family_cancel_adoption_error_no_request", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        return true;
    }

    private boolean adoptionScheduler(Player player, ParentManager manager) {
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskLater(familyPlugin, () -> {
            if (manager.getParentElement(uuid) != null) {
                manager.removeParent(uuid);
                sendMessage(new MessageForFormatting("family_accept_adoption_close", new String[]{}), MessageType.WARNING, player);
            }
        }, 5 * 60 * 20);

        return true;
    }

    public boolean accept(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.FAMILY_USER)) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        String adoptedName = player.getDisplayName();
        UUID uuid = player.getUniqueId();
        PlayerFamily adoptedFamily = FamilyUtils.getFamily(player);

        if (adoptedFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[]{adoptedName}), MessageType.WARNING, sender);
            return false;
        }

        ParentManager manager = globalManager.getParentManager();
        UUID[] parents = manager.getParentElement(uuid);

        if (parents == null || parents[1] == null || parents[2] == null) {
            sendMessage(new MessageForFormatting("family_accept_error_no_parents", new String[]{}), MessageType.WARNING, sender);
            manager.removeParent(uuid);
            return false;
        }

        Player player1 = Bukkit.getPlayer(parents[1]);
        Player player2 = Bukkit.getPlayer(parents[2]);

        if (player1 == null || !player1.isOnline() || player2 == null || !player2.isOnline()) {
            sendMessage(new MessageForFormatting("family_accept_error_parrent_not_online", new String[]{}), MessageType.WARNING, sender);
            manager.removeParent(uuid);
            return false;
        }

        PlayerFamily family1 = FamilyUtils.getFamily(player1);
        PlayerFamily family2 = FamilyUtils.getFamily(player2);

        if (family1 == null) {
            sendMessage(new MessageForFormatting("family_accept_error_parent1_missing", new String[]{}), MessageType.WARNING, sender);
            manager.removeParent(uuid);
            return false;
        }

        if (family2 == null) {
            sendMessage(new MessageForFormatting("family_accept_error_parent2_missing", new String[]{}), MessageType.WARNING, sender);
            manager.removeParent(uuid);
            return false;
        }
        validateAdoptionConfig(family1, family2, sender, player1, player2);

        String adopter1Name = player1.getDisplayName();
        String adopter2Name = player2.getDisplayName();

        PlayerFamily[] adoptersFamily = {family1, family2};
        FamilyDetails adoptedDetails = FamilyDetailsGet.getRootFamilyDetails(adoptedFamily);
        FamilyDetails[] adoptersDetails = {FamilyDetailsGet.getRootFamilyDetails(family1), FamilyDetailsGet.getRootFamilyDetails(family2)};

        ActionInitiator initiator = ActionInitiator.PLAYER_SELF;

        MessageForFormatting messageTrue = new MessageForFormatting("family_accept_success", new String[]{adopter1Name, adopter2Name, adoptedName});
        MessageForFormatting messageFalse = new MessageForFormatting("family_accept_error_cannot_adopt", new String[]{adopter1Name, adopter2Name, adoptedName});
        CommandSender[] senders = {sender, player1, player2};

        SyncExecutor.runSync(() -> handleAdoption(manager, adoptersFamily, adoptedFamily, adoptersDetails, adoptedDetails, initiator, senders, messageTrue, messageFalse));
        return true;
    }

    public boolean declineAdoption(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        ParentManager manager = globalManager.getParentManager();
        UUID[] parents = manager.getParentElement(playerUUID);

        if (parents == null || parents[0] == null) {
            sendMessage(new MessageForFormatting("family_cancel_adoption_error_no_request", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        manager.removeParent(playerUUID);

        Player parent1 = (parents[1] != null) ? Bukkit.getPlayer(parents[1]) : null;
        Player parent2 = (parents[2] != null) ? Bukkit.getPlayer(parents[2]) : null;

        sendMessage(new MessageForFormatting("family_decline_notify_parent", new String[]{player.getDisplayName()}), MessageType.WARNING, parent1, parent2);
        sendMessage(new MessageForFormatting("family_decline_success", new String[]{}), MessageType.IMPORTANT, sender);
        return true;
    }

    public boolean forceAdopt(CommandSender sender, String[] args) {
        boolean isPlayer = sender instanceof Player;

        if (!(isPlayer && sender.hasPermission(Permissions.FAMILY_ADMIN)) && !sender.getName().equalsIgnoreCase("CONSOLE")) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format /family forceadopt <adoptedPlayer> <adopterPlayer>", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        String adoptedPlayerName = args[1];
        String adopterPlayerName = args[2];

        PlayerFamily adoptedFamily = FamilyUtils.getFamily(adoptedPlayerName);
        PlayerFamily adopterFamily = FamilyUtils.getFamily(adopterPlayerName);

        if (adoptedFamily == null || adopterFamily == null) {
            sendMessage(new MessageForFormatting("family_err_family_not_found", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        Player player1 = Bukkit.getPlayer(adoptedFamily.getRoot());
        Player player2 = Bukkit.getPlayer(adopterFamily.getRoot());

        PlayerFamily[] adoptersFamily = {adopterFamily};
        FamilyDetails adoptedDetails = FamilyDetailsGet.getRootFamilyDetails(adoptedFamily);
        FamilyDetails[] adoptersDetails = {FamilyDetailsGet.getRootFamilyDetails(adopterFamily)};

        ActionInitiator initiator = isPlayer ? ActionInitiator.PLAYER_WITH_PERMISSION : ActionInitiator.CONSOLE;

        MessageForFormatting messageTrue = new MessageForFormatting("family_accept_success_adoption", new String[]{player1.getDisplayName(), player2.getDisplayName()});
        MessageForFormatting messageFalse = new MessageForFormatting("family_err_adoption_failed", new String[]{});
        CommandSender[] senders = {sender, player1, player2};

        SyncExecutor.runSync(() -> handleAdoption(null, adoptersFamily, adoptedFamily, adoptersDetails, adoptedDetails, initiator, senders, messageTrue, messageFalse));
        return true;
    }

    private void handleAdoption(ParentManager manager, PlayerFamily[] adoptersFamily, PlayerFamily adoptedFamily,
                                FamilyDetails[] adoptersDetails, FamilyDetails adoptedDetails, ActionInitiator initiator, CommandSender[] senders,
                                MessageForFormatting messageTrue, MessageForFormatting messageFalse) {

        try {
            AdoptionEvent event = new AdoptionEvent(adoptersFamily, adoptedFamily, adoptersDetails, adoptedDetails, initiator);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                SyncExecutor.runAsync(() -> {
                	
                    FamilyAdoption familyAdoption = new FamilyAdoption(familyPlugin);
                    
                    if (familyAdoption.adoption(adoptedFamily, adoptersFamily)) {
                    	FamilyDetailsHandler.handleAdoption(adoptersFamily, adoptedFamily);
                        sendMessage(messageTrue, MessageType.IMPORTANT, senders);
                    } else {
                        sendMessage(messageFalse, MessageType.WARNING, senders);
                    }
                    if (manager != null) manager.removeParent(adoptedFamily.getRoot());
                });
            } else {
                sendMessage(new MessageForFormatting("family_err_event_is_canceled", new String[]{}), MessageType.WARNING, senders);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Exception in setPlayerGender: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

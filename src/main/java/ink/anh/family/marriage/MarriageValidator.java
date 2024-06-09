package ink.anh.family.marriage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.FamilyConfig;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.fplayer.FamilyService;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.OtherUtils;
import ink.anh.family.util.PaymentManager;

public class MarriageValidator extends Sender {

	private AnhyFamily familyPlugin;
	private Player priest = null;
	private String priestTitle = "";
    private boolean isPublic;

    private PlayerFamily familyBride1;
    private PlayerFamily familyBride2;

    public MarriageValidator(AnhyFamily familyPlugin, boolean isPublic) {
		super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
		this.isPublic = isPublic;
	}
    
    public void setPriest(Player priest) {
		this.priest = priest;
		this.priestTitle = FamilyUtils.getPriestTitle(priest);
	}

    public boolean validateCommandInput(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        if (args.length <= 2 && isPublic) {
            sendMessage(new MessageForFormatting("family_err_command_format  /mary public <bride1> <bride2> [number]", new String[] {}), MessageType.WARNING, sender);
            return false;
        } else if (args.length <= 1 && !isPublic) {
            sendMessage(new MessageForFormatting("family_err_command_format  /mary private <bride>", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        return true;
    }

    public boolean validateCeremonyConditions(Player bride1, Player bride2, Player[] recipients) {
        Player[] players = isPublic ? new Player[] {bride1, bride2, priest} : new Player[] {bride1, bride2};

        if (!validateMembers(recipients, players)) {
            return false;
        }

        int radius = isPublic ? ((GlobalManager) libraryManager).getFamilyConfig().getCeremonyRadius() : 5;
        Location location = isPublic && priest != null ? priest.getLocation() : bride1.getLocation();
        if (!OtherUtils.isPlayerWithinRadius(bride1, location, radius) || !OtherUtils.isPlayerWithinRadius(bride2, location, radius)) {
            sendMessage(new MessageForFormatting(priestTitle + ": family_marry_failed_distance", new String[] {}), MessageType.WARNING, false, recipients);
            return false;
        }

        return true;
    }

    public boolean validatePermissions(Player bride1, Player bride2, Player[] recipients) {
        boolean perm = true; // призначено для того щоб вивести всі повідомлення про дозволи
        List<String> members = new ArrayList<>();

        if (isPublic && priest != null && !priest.hasPermission(Permissions.FAMILY_PASTOR)) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[] {}), MessageType.WARNING, priest);
            return false;
        }

        if (!bride1.hasPermission(Permissions.FAMILY_USER)) {
            sendMessage(new MessageForFormatting("family_mary_not_have_permission", new String[] {}), MessageType.WARNING, bride1);
            members.add(bride1.getDisplayName());
            perm = false;
        }

        if (!bride2.hasPermission(Permissions.FAMILY_USER)) {
            sendMessage(new MessageForFormatting("family_mary_not_have_permission", new String[] {}), MessageType.WARNING, bride2);
            if (!members.isEmpty()) members.add(", ");
            members.add(bride2.getDisplayName());
            perm = false;
        }

        if (!members.isEmpty()) {
            String[] membersArray = members.toArray(new String[0]);
            sendMessage(new MessageForFormatting(priestTitle + ": family_mary_not_have_permission_members", membersArray), MessageType.WARNING, recipients);
        }

        return perm;
    }

    public boolean validatePayment(Player bride1, Player bride2, Player[] recipients, String bride1Name, String bride2Name) {
        PaymentManager pay = new PaymentManager(familyPlugin);

        if (pay.canAfford(bride1, FamilyService.MARRIAGE) && pay.canAfford(bride2, FamilyService.MARRIAGE)) {
            return true;
        }
		sendMessage(new MessageForFormatting(priestTitle + ": family_marry_payment_failed_check", new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients);
        return false;
    }

    public boolean paymentFailed(MarryBase marryBase, Player[] recipients, MarriageManager marriageManager) {
    	Player bride1 = marryBase.getProposer();
    	Player bride2 = marryBase.getReceiver();
    	
        PaymentManager pay = new PaymentManager(familyPlugin);

        if (!pay.makePayment(bride1, FamilyService.MARRIAGE) || !pay.makePayment(bride2, FamilyService.MARRIAGE)) {
        	marriageManager.remove(bride1.getUniqueId());
			sendMessage(new MessageForFormatting(priestTitle + ": family_marry_payment_failed", new String[] {bride1.getDisplayName(), bride2.getDisplayName()}),
					MessageType.WARNING, false, recipients);
            return true;
        }
        return false;
    }

    public boolean validateCeremonyParticipants(Player bride1, Player bride2, Player[] recipients) {
        if (isPublic && priest != null) {
            UUID uuidPriest = priest.getUniqueId();
            if (uuidPriest.equals(bride1.getUniqueId()) || uuidPriest.equals(bride2.getUniqueId())) {
                sendMessage(new MessageForFormatting("family_marry_failed_myself", new String[] {}), MessageType.WARNING, new Player[] {priest, bride1, bride2});
                return false;
            }
        }

        familyBride1 = FamilyUtils.getFamily(bride1);
        familyBride2 = FamilyUtils.getFamily(bride2);
        
        if (!validateMarriageConfig(familyBride1, familyBride2, recipients)) {
            return false;
        }

        return true;
    }

    public boolean validateMarriageConfig(PlayerFamily family1, PlayerFamily family2, Player[] recipients) {
        FamilyConfig familyConfig = ((GlobalManager) libraryManager).getFamilyConfig();
        boolean nonTraditionalAllowed = familyConfig.isNonBinaryMarry();
        
        if (!nonTraditionalAllowed && !FamilyUtils.areGendersCompatibleForTraditional(family1, family2)) {
            sendMessage(new MessageForFormatting(priestTitle + ": family_marry_failed_traditional", new String[] {}), MessageType.WARNING, false, recipients);
            return false;
        }
        return true;
    }

    public boolean validateMembers(Player[] recipients, Player... members) {
    	for (Player member : members) {
            if (member == null || !member.isOnline()) {
            	sendMessage(new MessageForFormatting("family_member_missing", new String[] {}), MessageType.WARNING, false, recipients);
                return false;
            }
    	}
        return true;
    }

    public ProcessLastName processLastNameArgs(String[] args) {
        ProcessLastName result = new ProcessLastName();

        if (args.length > 2) {
            try {
                int numberLastName = Integer.parseInt(args[2]);
                if (numberLastName != 0 && numberLastName != 1 && numberLastName != 2) {
                    numberLastName = 0;
                }
                result.setNumberLastName(numberLastName);
            } catch (NumberFormatException e) {
                result.setNumberLastName(0);
            }
        }

        int numberLastName = result.getNumberLastName();

        if (numberLastName == 0) {
            result.setLastName(new String[] {null});
        } else if (numberLastName == 1) {
            if (familyBride1 == null || familyBride1.getLastName() == null || familyBride1.getLastName().length == 0) {
                result.setLastName(new String[] {null});
            } else {
                result.setLastName(new String[] {familyBride1.getLastName()[0]});
            }
        } else if (numberLastName == 2) {
            if (familyBride2 == null || familyBride2.getLastName() == null || familyBride2.getLastName().length == 0) {
                result.setLastName(new String[] {null});
            } else {
                result.setLastName(new String[] {familyBride2.getLastName()[0]});
            }
        }

        return result;
    }

}

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
import ink.anh.family.payment.PaymentManager;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.OtherUtils;

public class MarriageValidator extends Sender {

	private AnhyFamily familyPlugin;
	private Player priest = null;
    private boolean isPublic;

    public MarriageValidator(AnhyFamily familyPlugin, boolean isPublic) {
		super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
		this.isPublic = isPublic;
	}
    
    public void setPriest(Player priest) {
		this.priest = priest;
	}

    public boolean validateCommandInput(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        if (args.length < 3 && isPublic) {
            sendMessage(new MessageForFormatting("family_err_command_format  /mary public <bride1> <bride2> [number]", new String[] {}), MessageType.WARNING, sender);
            return false;
        } else if (args.length < 2 && !isPublic) {
            sendMessage(new MessageForFormatting("family_err_command_format  /mary private <bride>", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        return true;
    }

    public String[] validateCeremonyConditions(Player bride1, Player bride2) {
        FamilyConfig config = ((GlobalManager) libraryManager).getFamilyConfig();
        
        int radius = isPublic ? config.getCeremonyRadius() : 5;
        Location location = isPublic && priest != null ? priest.getLocation() : config.getPrivateCeremonyLocation();
        
        boolean isBride1WithinRadius = OtherUtils.isPlayerWithinRadius(bride1, location, radius);
        boolean isBride2WithinRadius = OtherUtils.isPlayerWithinRadius(bride2, location, radius);

        if (!isBride1WithinRadius || !isBride2WithinRadius) {
            return new String[] {"family_marry_failed_distance %s", !isBride1WithinRadius ? bride1.getDisplayName() : null, !isBride2WithinRadius ? bride2.getDisplayName() : null};
        }

        return null;
    }

    public String[] validatePermissions(Player bride1, Player bride2, Player[] recipients) {
        String[] result = null;

        if (isPublic && priest != null && !priest.hasPermission(Permissions.FAMILY_PASTOR)) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[] {}), MessageType.WARNING, priest);
            return new String[] {null};
        }

        if (!bride1.hasPermission(Permissions.FAMILY_USER)) {
            sendMessage(new MessageForFormatting("family_mary_not_have_permission", new String[] {}), MessageType.WARNING, bride1);
            result = new String[] {"family_mary_not_have_permission_members", bride1.getDisplayName(), null};
        }

        if (!bride2.hasPermission(Permissions.FAMILY_USER)) {
            sendMessage(new MessageForFormatting("family_mary_not_have_permission", new String[] {}), MessageType.WARNING, bride2);
            if (result == null) {
                result = new String[] {"family_mary_not_have_permission_members", bride2.getDisplayName()};
            } else {
                result[2] = bride2.getDisplayName();
            }
        }

        return result;
    }

    public String[] validatePayment(Player bride1, Player bride2, Player[] recipients, String bride1Name, String bride2Name) {
        PaymentManager pay = new PaymentManager(familyPlugin);
        List<String> issues = new ArrayList<>();
        issues.add("family_marry_payment_failed_check %s");

        boolean canAffordBride1 = pay.canAfford(bride1, FamilyService.MARRIAGE);
        boolean canAffordBride2 = pay.canAfford(bride2, FamilyService.MARRIAGE);

        if (canAffordBride1 && canAffordBride2) {
            return null;
        }

        if (!canAffordBride1) {
            issues.add(bride1Name);
        }

        if (!canAffordBride2) {
            issues.add(bride2Name);
        }

        return issues.toArray(new String[0]);
    }

    public String[] paymentFailed(MarryBase marryBase, MarriageManager marriageManager) {
        Player bride1 = marryBase.getProposer();
        Player bride2 = marryBase.getReceiver();
        
        PaymentManager pay = new PaymentManager(familyPlugin);

        if (!pay.makePayment(bride1, FamilyService.MARRIAGE) || !pay.makePayment(bride2, FamilyService.MARRIAGE)) {
            marriageManager.remove(bride1.getUniqueId());
            return new String[] {"family_marry_payment_failed", bride1.getDisplayName(), bride2.getDisplayName()};
        }
        return null;
    }

    public boolean validateCeremonyParticipants(Player bride1, Player bride2, Player[] recipients) {
        if (isPublic && priest != null) {
            UUID uuidPriest = priest.getUniqueId();
            if (uuidPriest.equals(bride1.getUniqueId()) || uuidPriest.equals(bride2.getUniqueId())) {
                sendMessage(new MessageForFormatting("family_marry_failed_myself", new String[] {}), MessageType.WARNING, new Player[] {priest, bride1, bride2});
                return false;
            }
        }
        return true;
    }

    public String[] validateMarriageCompatibility(PlayerFamily family1, PlayerFamily family2, Player[] recipients) {
        FamilyConfig familyConfig = ((GlobalManager) libraryManager).getFamilyConfig();
        boolean nonTraditionalAllowed = familyConfig.isNonBinaryMarry();
        
        if (!nonTraditionalAllowed && !FamilyUtils.areGendersCompatibleForTraditional(family1, family2)) {
            return new String[] {"family_marry_failed_traditional", ""};
        }

        String[] result = null;

        if (family1.getLastName() == null || family1.getLastName()[0] == null || family1.getLastName()[0].isEmpty()) {
            result = new String[] {"family_marry_last_name_not_found", family1.getRootrNickName(), null};
        }

        if (family2.getLastName() == null || family2.getLastName()[0] == null || family2.getLastName()[0].isEmpty()) {
            if (result == null) {
                result = new String[] {"family_marry_last_name_not_found", family2.getRootrNickName()};
            } else {
                result[2] = family2.getRootrNickName();
            }
        }

        return result;
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

    public ProcessLastName processLastNameArgs(String[] args, PlayerFamily familyBride1, PlayerFamily familyBride2) {
        ProcessLastName result = new ProcessLastName();

        if (args.length > 3) {
            try {
                int numberLastName = Integer.parseInt(args[3]);
                if (numberLastName < 0 || numberLastName > 2) {
                    numberLastName = 0;
                }
                result.setNumberLastName(numberLastName);
            } catch (NumberFormatException e) {
                result.setNumberLastName(0);
            }
        } else {
            result.setNumberLastName(1);
        }

        int numberLastName = result.getNumberLastName();

        if (numberLastName == 0) {
            result.setLastName(new String[] {null});
        } else if (numberLastName == 1) {
            result.setLastName(familyBride1.getLastName());
        } else if (numberLastName == 2) {
            result.setLastName(familyBride2.getLastName());
        }

        return result;
    }

}

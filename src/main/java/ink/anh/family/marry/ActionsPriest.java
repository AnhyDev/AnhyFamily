package ink.anh.family.marry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.common.Family;
import ink.anh.family.common.FamilyConfig;
import ink.anh.family.common.FamilyService;
import ink.anh.family.util.OtherUtils;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.PaymentManager;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;

public class ActionsPriest extends Sender {

	private AnhyFamily familyPlugin;
    private GlobalManager manager;
    private MarriageManager marriageManager;
    private FamilyConfig familyConfig;
    private String priestTitle = "";
    
    public ActionsPriest(AnhyFamily familyPlugin) {
        super(familyPlugin.getGlobalManager());
		this.familyPlugin = familyPlugin;
        this.manager = familyPlugin.getGlobalManager();
        this.marriageManager = familyPlugin.getMarriageManager();
        this.familyConfig = manager.getFamilyConfig();
    }
    
    public boolean marry(CommandSender sender, String[] args) {
		
		if (!validateCommandInput(sender, args)) {
            return false;
		}
		
		Player priest = (Player) sender;

        String bride1Name = args[1];
        String bride2Name = args[2];

        Player bride1 = Bukkit.getPlayerExact(bride1Name);
        Player bride2 = Bukkit.getPlayerExact(bride2Name);

    	priestTitle = FamilyUtils.getPriestTitle(priest);
        
		Player[] recipients = OtherUtils.getPlayersWithinRadius(priest.getLocation(), familyConfig.getCeremonyHearingRadius());
		
		if (!validateCeremonyConditions(priest, bride1, bride2, recipients)) {
			return false;
		}
		
		if (!validatePermissions(priest, bride1, bride2, recipients)) {
			return false;
		}

		UUID uuidPriest = priest.getUniqueId();
        UUID uuidBride1 = bride1.getUniqueId();
        UUID uuidBride2 = bride2.getUniqueId();
        
        Family familyBride1 = FamilyUtils.getFamily(uuidBride1);
        Family familyBride2 = FamilyUtils.getFamily(uuidBride2);
		
		if (!validateCeremonyParticipants(priest, bride1, bride2, familyBride1, familyBride2, uuidPriest, uuidBride1, uuidBride2, recipients)) {
			return false;
		}
        
        ProcessLastName processLastName = processLastNameArgs(args, familyBride1, familyBride2);
        String lastName[] = processLastName.getLastName();
        int surnameChoice = processLastName.getNumberLastName();
        
    	if (lastName == null) {
            sendMessage(new MessageForFormatting(priestTitle + ": family_marry_failed_last_name", new String[] {}), MessageType.WARNING, false, recipients);
            return false;
    	}
		
		if (!validatePayment(bride1, bride2, recipients, bride1Name, bride2Name)) {
			return false;
		}
        
    	if (marriageManager.add(bride1, bride2, priest, surnameChoice, lastName)) {
            sendMessage(new MessageForFormatting("family_marry_already_started", new String[] {bride1Name, bride2Name}), MessageType.WARNING, true, priest);
            return false;
    	}

    	sendMessage(new MessageForFormatting("family_marry_start_priest", new String[] {}), MessageType.WARNING, priest);
    	
		Bukkit.getServer().getScheduler().runTaskLater(familyPlugin, () -> 
			sendMessage(new MessageForFormatting(priestTitle + ": family_marry_start_success", new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients), 10L);

        return true;
	}

    private boolean validatePayment(Player bride1, Player bride2, Player[] recipients, String bride1Name, String bride2Name) {
        PaymentManager pay = new PaymentManager(familyPlugin);

        if (pay.canAfford(bride1, FamilyService.MARRIAGE) && pay.canAfford(bride2, FamilyService.MARRIAGE)) {
            return true;
        }
		sendMessage(new MessageForFormatting(priestTitle + ": family_marry_payment_failed_check", new String[] {bride1Name, bride2Name}), MessageType.WARNING, false, recipients);
        return false;
    }

    private boolean validateCeremonyParticipants(Player priest, Player bride1, Player bride2, Family familyBride1, Family familyBride2,
    		UUID uuidPriest, UUID uuidBride1, UUID uuidBride2, Player[] recipients) {

        if (uuidPriest.equals(uuidBride1) || uuidPriest.equals(uuidBride2)) {
            sendMessage(new MessageForFormatting("family_marry_failed_myself", new String[] {}), MessageType.WARNING, new Player[] {priest, bride1, bride2});
            return false;
        }
        
        if (!FamilyUtils.areGendersCompatibleForTraditional(familyBride1, familyBride2)) {
            sendMessage(new MessageForFormatting(priestTitle + ": family_marry_failed_traditional", new String[] {}), MessageType.WARNING, false, recipients);
            return false;
        }

        return true;
    }

    private boolean validatePermissions(Player priest, Player bride1, Player bride2, Player[] recipients) {
        boolean perm = true;
        List<String> members = new ArrayList<>();

        if (!priest.hasPermission(Permissions.FAMILY_PASTOR)) {
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

    private boolean validateCeremonyConditions(Player priest, Player bride1, Player bride2, Player[] recipients) {
        if (!validateMembers(recipients, new Player[] {bride1, priest, bride2})) {
            return false;
        }
        
        int radius = familyConfig.getCeremonyRadius();
        if (!OtherUtils.isPlayerWithinRadius(bride1, priest.getLocation(), radius) || !OtherUtils.isPlayerWithinRadius(bride2, priest.getLocation(), radius)) {
            sendMessage(new MessageForFormatting(priestTitle + ": family_marry_failed_distance", new String[] {}), MessageType.WARNING, false, recipients);
            return false;
        }

        return true;
    }

    private boolean validateMembers(Player[] recipients, Player... members) {
    	for (Player priest : members) {
            if (priest == null || !priest.isOnline()) {
            	sendMessage(new MessageForFormatting("family_member_missing", new String[] {}), MessageType.WARNING, false, recipients);
                return false;
            }
    	}
        return true;
    }

    private boolean validateCommandInput(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        if (args.length <= 2) {
            sendMessage(new MessageForFormatting("family_err_command_format  /family mary <bride1> <bride2> [number]", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        return true;
    }

    private ProcessLastName processLastNameArgs(String[] args, Family familyBride1, Family familyBride2) {
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
    
    class ProcessLastName {
    	
    	private int numberLastName = 1;
    	private String lastName[] = null;
    	
		public int getNumberLastName() {
			return numberLastName;
		}
		
		public String[] getLastName() {
			return lastName;
		}
		
		public void setNumberLastName(int numberLastName) {
			this.numberLastName = numberLastName;
		}
		
		public void setLastName(String[] lastName) {
			this.lastName = lastName;
		}
    }
}

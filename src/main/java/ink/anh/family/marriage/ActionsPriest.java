package ink.anh.family.marriage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import ink.anh.family.AnhyFamily;
import ink.anh.family.FamilyConfig;
import ink.anh.family.GlobalManager;
import ink.anh.family.util.OtherUtils;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;

public class ActionsPriest extends Sender {

	private AnhyFamily familyPlugin;
    private GlobalManager manager;
    private MarriageManager marriageManager;
    private MarriageValidator validator;
    private FamilyConfig familyConfig;
    private String priestTitle = "";
    
    public ActionsPriest(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
        this.manager = GlobalManager.getInstance();
        this.marriageManager = GlobalManager.getInstance().getMarriageManager();
        this.validator = new MarriageValidator(familyPlugin, true);
        this.familyConfig = manager.getFamilyConfig();
    }
    
    public boolean marry(CommandSender sender, String[] args) {
		
		if (!validator.validateCommandInput(sender, args)) {
            return false;
		}
		
		Player priest = (Player) sender;
		validator.setPriest(priest);

        String bride1Name = args[1];
        String bride2Name = args[2];

        Player bride1 = Bukkit.getPlayerExact(bride1Name);
        Player bride2 = Bukkit.getPlayerExact(bride2Name);

    	priestTitle = FamilyUtils.getPriestTitle(priest);
        
		Player[] recipients = OtherUtils.getPlayersWithinRadius(priest.getLocation(), familyConfig.getCeremonyHearingRadius());
		
		if (!validator.validateCeremonyConditions(bride1, bride2, recipients)) {
			return false;
		}
		
		if (!validator.validatePermissions(bride1, bride2, recipients)) {
			return false;
		}
       
		
		if (!validator.validateCeremonyParticipants(bride1, bride2, recipients)) {
			return false;
		}
        
        ProcessLastName processLastName = validator.processLastNameArgs(args);
        String lastName[] = processLastName.getLastName();
        int surnameChoice = processLastName.getNumberLastName();
        
    	if (lastName == null) {
            sendMessage(new MessageForFormatting(priestTitle + ": family_marry_failed_last_name", new String[] {}), MessageType.WARNING, false, recipients);
            return false;
    	}
		
		if (!validator.validatePayment(bride1, bride2, recipients, bride1Name, bride2Name)) {
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

}

package ink.anh.family.separate;

import org.bukkit.command.CommandSender;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.marriage.Divorce;
import ink.anh.family.parents.ChildSeparation;
import ink.anh.family.parents.ParentSeparation;
import ink.anh.api.messages.MessageForFormatting;

public class Separation extends Sender {

	private AnhyFamily familyPlugin;

    public Separation(AnhyFamily familyPlugin) {
    	super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
    }

	public boolean separate(CommandSender sender, String[] args) {
        
        if (args.length < 2 || args[1].equalsIgnoreCase("spouse")) {
            return new Divorce(familyPlugin).separate(sender);
        } else if (args.length > 2 && args[1].equalsIgnoreCase("child")) {
        	return new ChildSeparation(familyPlugin).separate(sender, args);
        } else if (args.length > 2 && args[1].equalsIgnoreCase("parent")) {
        	return new ParentSeparation(familyPlugin).separate(sender, args);
        }
        
        sendMessage(new MessageForFormatting("family_error_command_line", new String[] {}), MessageType.WARNING, sender);
        return false;
	}
}

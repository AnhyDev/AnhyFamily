package ink.anh.family.command.sub;

import org.bukkit.command.CommandSender;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.Sender;
import ink.anh.family.marry.Divorce;
import ink.anh.family.parents.ChildSeparation;
import ink.anh.family.parents.ParentSeparation;
import ink.anh.api.messages.MessageForFormatting;

public class Separation extends Sender {

    public Separation(AnhyFamily familiPlugin) {
        super(familiPlugin);
    }

	public boolean separate(CommandSender sender, String[] args) {
        
        if (args.length < 2 || args[1].equalsIgnoreCase("spouse")) {
            return new Divorce(familiPlugin).separate(sender, args);
        } else if (args.length > 2 && args[1].equalsIgnoreCase("child")) {
        	return new ChildSeparation(familiPlugin).separate(sender, args);
        } else if (args.length > 2 && args[1].equalsIgnoreCase("parent")) {
        	return new ParentSeparation(familiPlugin).separate(sender, args);
        }
        
        sendMessage(new MessageForFormatting("family_error_command_line", null), MessageType.WARNING, sender);
        return false;
	}
}

package ink.anh.family.fplayer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Messenger;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fdetails.FDetailsComponentBuilder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NamesPastorManager extends Sender {

    private static NamesPastorManager instance;
    private final AnhyFamily familyPlugin;
    private final Map<UUID, NameRequest> nameRequests;

    private NamesPastorManager(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
        this.nameRequests = new ConcurrentHashMap<>();
    }

    public static NamesPastorManager getInstance(AnhyFamily familyPlugin) {
        if (instance == null) {
            instance = new NamesPastorManager(familyPlugin);
        }
        return instance;
    }

    public boolean sugges(CommandSender sender, String[] args) {
        switch (args[1].toLowerCase()) {
            case "firstname":
            case "surname":
                Player player = Bukkit.getPlayerExact(args[2]);
                if (player != null) {
                    return startTimedAction(sender, player, args);
                }
                break;
            case "accept":
                return accept(sender);
            case "refuse":
                return refuse(sender);
            default:
                break;
        }
        return false;
    }

    private boolean startTimedAction(CommandSender sender, Player player, String[] args) {
        UUID playerId = player.getUniqueId();
        NameRequest nameRequest = new NameRequest(sender, args);
        if (nameRequests.putIfAbsent(playerId, nameRequest) == null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    nameRequests.remove(playerId);
                }
            }.runTaskLater(familyPlugin, 20 * 60);

            if (sendRequest(sender, player, args)) {
                sendMessage(new MessageForFormatting("family_name_replacement_sent ", new String[]{player.getName()}), MessageType.NORMAL, player);
                return true;
            }
        }
        sendMessage(new MessageForFormatting("family_err_there_already_offer ", new String[]{player.getName()}), MessageType.WARNING, player);

        return false;
    }

    private boolean sendRequest(CommandSender sender, Player player, String[] args) {
        MessageComponents messageComponents;
        switch (args[1].toLowerCase()) {
            case "firstname":
                if (!FirstName.checkMaxLengthFirstName(args[3])) {
                    sendMessage(new MessageForFormatting("family_firstname_too_long", new String[]{}), MessageType.WARNING, sender);
                    return false;
                }

                messageComponents = FDetailsComponentBuilder.acceptMessageComponent("family_name_change_proposal", "fam sugges", "accept", "refuse", player);
                sendMessageComponent(player, messageComponents);
                return true;
            case "surname":
                if (Surname.processInput(args, 3) == null) {
                    sendMessage(new MessageForFormatting("family_surname_build_failed", new String[]{}), MessageType.WARNING, sender);
                    return false;
                }

                messageComponents = FDetailsComponentBuilder.acceptMessageComponent("family_lastname_change_proposal", "fam sugges", "accept", "refuse", player);
                sendMessageComponent(player, messageComponents);
                return true;
            default:
                return false;
        }
    }

    public boolean accept(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();
            NameRequest nameRequest = nameRequests.remove(playerId);
            if (nameRequest != null) {
                return processRequest(sender, nameRequest.getArgs());
            }
        }
        return false;
    }

    public boolean refuse(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();
            NameRequest request = nameRequests.get(playerId);
            
            if (request == null) return false;
            
            sendMessage(new MessageForFormatting("family_name_change_proposal_refuse", new String[]{player.getName()}), MessageType.WARNING, player, request.getSender());
            return nameRequests.remove(playerId) != null;
        }
        return false;
    }

    private boolean processRequest(CommandSender sender, String[] args) {
        switch (args[1].toLowerCase()) {
            case "firstname":
                new FirstName().suggesFirstName(sender, args);
                return true;
            case "surname":
                new Surname().suggesSurname(sender, args);
                return true;
            default:
                return false;
        }
    }

    protected void sendMessageComponent(Player recipient, MessageComponents messageComponents) {
        Messenger.sendMessage(familyPlugin, recipient, messageComponents, "MessageComponents");
    }
    

    public class NameRequest {
    	private final CommandSender sender;
    	private final String[] args;

    	public NameRequest(CommandSender sender, String[] args) {
    		this.sender = sender;
    		this.args = args;
    	}

    	public CommandSender getSender() {
    		return sender;
    	}

    	public String[] getArgs() {
    		return args;
    	}
    }
}

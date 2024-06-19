package ink.anh.family.fdetails;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import ink.anh.api.enums.Access;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.Logger;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Messenger;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.symbol.FamilySymbolManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.StringColorUtils;
import ink.anh.family.util.TypeTargetComponent;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractDetailsManager extends Sender {
    protected AnhyFamily familyPlugin;
    protected Player player;
    protected String command;
    protected String[] args;

    public AbstractDetailsManager(AnhyFamily familyPlugin, Player player, Command cmd, String[] args) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
        this.player = player;
        this.command = cmd != null ? cmd.getName() : getDefaultCommand();
        this.args = args;
    }

    protected abstract String getDefaultCommand();

    protected abstract String getInvalidAccessMessage();

    protected abstract TypeTargetComponent getTypeTargetComponent();

    protected abstract void performAction(FamilyDetails details);

    protected void handleActionWithConditions() {
    	int defaultTypeKey = 0;
    	
        if (args.length == 0) {
            handleWithTypeKey(null, defaultTypeKey);
            return;
        }

        String firstArg = args[0];

        if (firstArg.startsWith("#")) {
            handleWithTypeKey(firstArg.substring(1).toUpperCase(), 1);
        } else if (firstArg.startsWith("@")) {
            handleWithTypeKey(firstArg.substring(1), 2);
        } else {
            handleWithTypeKey(null, defaultTypeKey);
        }
    }
    
    protected void handleWithTypeKey(String key, int typeKey) {
        try {
            FamilyDetails familyDetails = null;

            switch (typeKey) {
                case 0:
                    familyDetails = FamilyDetailsGet.getRootFamilyDetails(player);
                    if (familyDetails == null) {
                        sendMessage(new MessageForFormatting("family_err_no_family", new String[]{}), MessageType.WARNING, player);
                        return;
                    }
                    break;
                case 1:
                    UUID familyId = FamilySymbolManager.getFamilyIdBySymbol(key);
                    if (familyId != null) {
                        familyDetails = FamilyDetailsGet.getFamilyDetails(familyId);
                    } else {
                        sendMessage(new MessageForFormatting("family_err_prefix_not_found", new String[]{key}), MessageType.WARNING, player);
                        return;
                    }
                    break;
                case 2:
                    Player targetPlayer = Bukkit.getPlayerExact(key);

                    if (targetPlayer == null) {
                        sendMessage(new MessageForFormatting("family_err_player_offline", new String[]{key}), MessageType.WARNING, player);
                        return;
                    }
                    familyDetails = FamilyDetailsGet.getRootFamilyDetails(targetPlayer);
                    if (familyDetails == null) {
                        sendMessage(new MessageForFormatting("family_err_player_no_family", new String[]{key}), MessageType.WARNING, player);
                        return;
                    }
                    break;
                default:
                    sendMessage(new MessageForFormatting("family_error_generic", new String[]{String.valueOf(typeKey)}), MessageType.WARNING, player);
                    return;
            }

            if (familyDetails != null) {
                processAction(familyDetails, key);
            }
        } catch (Exception e) {
            Logger.error(AnhyFamily.getInstance(), "Exception in handleWithTypeKey: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void processAction(FamilyDetails details, String identifier) {
        PlayerFamily playerFamily = FamilyUtils.getFamily(player);
        if (canPerformAction(details, null)) {
            if (details.hasAccess(playerFamily, getTypeTargetComponent())) {
                performAction(details);
            } else {
                sendMessage(new MessageForFormatting(getInvalidAccessMessage(), identifier != null ? new String[]{identifier} : new String[]{}), MessageType.WARNING, player);
            }
        }
    }

    protected boolean canPerformAction(FamilyDetails details, Object additionalParameter) {
        return true;
    }

    protected void executeWithFamilyDetails(FamilyDetails details, FamilyDetailsActionInterface action) {
        if (details != null) {
            action.execute(details);
        } else {
            sendMessage(new MessageForFormatting("family_err_details_not_found", new String[]{}), MessageType.WARNING, player);
        }
    }

    protected void sendMessageToPlayer(Player recipient, String messageKey, String[] messageArgs, MessageType messageType) {
        sendMessage(new MessageForFormatting(messageKey, messageArgs), messageType, recipient);
    }

    protected void setAccess(String nickname, String accessArg, TypeTargetComponent component) {
        PlayerFamily targetFamily = FamilyUtils.getFamily(nickname);
        
        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_nickname_not_found", new String[]{nickname}), MessageType.WARNING, player);
            return;
        }
        
        String colorStart = "e";
        Access access;
        switch (accessArg.toLowerCase()) {
            case "allow":
                access = Access.TRUE;
                colorStart = "a";
                break;
            case "deny":
                access = Access.FALSE;
                colorStart = "4";
                break;
            case "default":
                access = Access.DEFAULT;
                colorStart = "e";
                break;
            default:
                sendMessage(new MessageForFormatting("family_err_invalid_access", new String[]{accessArg.toUpperCase()}), MessageType.WARNING, player);
                return;
        }

        String colorFinish = MessageType.NORMAL.getColor(true);
        String accessUpColor = StringUtils.colorize(StringColorUtils.colorSet(colorStart, accessArg.toUpperCase(), colorFinish));
        String nicknameColor = StringUtils.colorize(StringColorUtils.colorSet("2", nickname, colorFinish));

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            if (targetFamily.getFamilyId() != null && targetFamily.getFamilyId().equals(details.getFamilyId())) {
                sendMessage(new MessageForFormatting("family_access_root", new String[]{nicknameColor, details.getFamilySymbol()}), MessageType.NORMAL, player);
                return;
            }

            UUID targetUUID = targetFamily.getRoot();
            Map<UUID, AccessControl> childrenAccessMap = details.getChildrenAccessMap();
            Map<UUID, AccessControl> ancestorsAccessMap = details.getAncestorsAccessMap();
            AccessControl accessControl = null;

            if (childrenAccessMap.containsKey(targetUUID)) {
                accessControl = childrenAccessMap.get(targetUUID);
            } else if (ancestorsAccessMap.containsKey(targetUUID)) {
                accessControl = ancestorsAccessMap.get(targetUUID);
            }

            if (accessControl != null) {
                setComponentAccess(accessControl, access, component);
                if (childrenAccessMap.containsKey(targetUUID)) {
                    childrenAccessMap.put(targetUUID, accessControl);
                    FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.CHILDREN_ACCESS_MAP);
                } else {
                    ancestorsAccessMap.put(targetUUID, accessControl);
                    FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.ANCESTORS_ACCESS_MAP);
                }
                sendMessage(new MessageForFormatting(getComponentAccessSetMessageKey(component), new String[]{nicknameColor, accessUpColor}), MessageType.NORMAL, player);
            } else {
                sendMessage(new MessageForFormatting("family_err_nickname_not_found_in_access_maps", new String[]{nicknameColor}), MessageType.WARNING, player);
            }
        });
    }

    protected abstract void setComponentAccess(AccessControl accessControl, Access access, TypeTargetComponent component);

    protected abstract String getComponentAccessSetMessageKey(TypeTargetComponent component);

    protected void setDefaultAccess(String targetGroup, String accessArg, TypeTargetComponent component) {
        String colorStart = "e";
        Access access;
        switch (accessArg.toLowerCase()) {
            case "allow":
                access = Access.TRUE;
                colorStart = "a";
                break;
            case "deny":
                access = Access.FALSE;
                colorStart = "4";
                break;
            default:
                sendMessage(new MessageForFormatting("family_err_invalid_access", new String[]{accessArg}), MessageType.WARNING, player);
                return;
        }

        String colorFinish = MessageType.NORMAL.getColor(true);
        String accessUpColor = StringUtils.colorize(StringColorUtils.colorSet(colorStart, accessArg.toUpperCase(), colorFinish));
        String groupsUpColor = StringUtils.colorize(StringColorUtils.colorSet("2", targetGroup.toUpperCase(), colorFinish));

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            if ("children".equalsIgnoreCase(targetGroup)) {
                details.getChildrenAccess().setChatAccess(access);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.CHILDREN_ACCESS);
            } else if ("parents".equalsIgnoreCase(targetGroup)) {
                details.getAncestorsAccess().setChatAccess(access);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.ANCESTORS_ACCESS);
            } else {
                sendMessage(new MessageForFormatting("family_err_invalid_group", new String[]{targetGroup}), MessageType.WARNING, player);
                return;
            }
            sendMessage(new MessageForFormatting(getDefaultAccessSetMessageKey(component), new String[]{groupsUpColor, accessUpColor}), MessageType.NORMAL, player);
        });
    }

    protected abstract String getDefaultAccessSetMessageKey(TypeTargetComponent component);

    protected void checkAccess(String nickname, TypeTargetComponent component) {
        PlayerFamily targetFamily = FamilyUtils.getFamily(nickname);
        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_nickname_not_found", new String[]{nickname}), MessageType.WARNING, player);
            return;
        }

        PlayerFamily senderFamily = FamilyUtils.getFamily(player);
        if (senderFamily != null) {
            executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(senderFamily), details -> {
                Access currentAccess = details.getAccess(targetFamily, component);
                MessageComponents messageComponents = MessageComponentBuilder.buildCheckAccessMessageComponent(player, nickname, currentAccess, command);
                Messenger.sendMessage(familyPlugin, player, messageComponents, "family_access_get");
            });
        }
    }

    protected void checkDefaultAccess(String targetGroup, TypeTargetComponent component) {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            AccessControl accessControl;

            switch (targetGroup.toLowerCase()) {
                case "children":
                    accessControl = details.getChildrenAccess();
                    break;
                case "parents":
                    accessControl = details.getAncestorsAccess();
                    break;
                default:
                    sendMessage(new MessageForFormatting("family_err_invalid_group", new String[]{targetGroup}), MessageType.WARNING, player);
                    return;
            }

            Access currentAccess = accessControl.getChatAccess();
            MessageComponents messageComponents = MessageComponentBuilder.buildDefaultAccessMessageComponent(player, targetGroup, currentAccess, command);
            Messenger.sendMessage(familyPlugin, player, messageComponents, getDefaultAccessCheckMessageKey(component));
        });
    }

    protected abstract String getDefaultAccessCheckMessageKey(TypeTargetComponent component);

    protected void sendActionBarMessage(Player player, MessageForFormatting textForFormatting, String hexColor) {
    	String[] langs = LangUtils.getPlayerLanguage(player);
    	
    	String message = StringUtils.formatString(Translator.translateKyeWorld(libraryManager, textForFormatting.getTemplate(), langs), textForFormatting.getReplacements());
    	
        MessageComponents messageComponents = MessageComponents.builder()
            .content(message)
            .hexColor(hexColor)
            .build();
        
        Messenger.sendActionBar(familyPlugin, player, messageComponents, message);
    }
}

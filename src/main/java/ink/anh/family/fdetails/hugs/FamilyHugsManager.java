package ink.anh.family.fdetails.hugs;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ink.anh.api.enums.Access;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fdetails.AbstractDetailsManager;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.TypeTargetComponent;

public class FamilyHugsManager extends AbstractDetailsManager {

    private Set<Player> cooldownPlayers = new HashSet<>();
    
    public FamilyHugsManager(AnhyFamily familyPlugin, Player player, Command cmd, String[] args) {
        super(familyPlugin, player, cmd, args);
    }

    @Override
    protected String getDefaultCommand() {
        return "fhugs";
    }

    @Override
    protected String getInvalidAccessMessage() {
        return "family_err_no_access_hugs";
    }

    @Override
    protected String getComponentAccessSetMessageKey(TypeTargetComponent component) {
        return "family_hugs_access_set";
    }

    @Override
    protected String getDefaultAccessSetMessageKey(TypeTargetComponent component) {
        return "family_default_hugs_access_set";
    }

    @Override
    protected String getDefaultAccessCheckMessageKey(TypeTargetComponent component) {
        return "family_default_hugs_access_check";
    }

    @Override
    protected boolean canPerformAction(FamilyDetails details, Object additionalParameter) {
        return true;
    }

    @Override
    protected TypeTargetComponent getTypeTargetComponent() {
        return TypeTargetComponent.HUGS;
    }

    @Override
    protected void setComponentAccess(AccessControl accessControl, Access access, TypeTargetComponent component) {
        accessControl.setHugsAccess(access);
    }

    @Override
    protected void performAction(FamilyDetails details) {
    	hugsToFamilyDetails(details, "Hugs performed! (Заглушка для обіймів)");
    }

    public void sendMessageWithConditions() {
        handleActionWithConditions();
    }

    private void hugsToFamilyDetails(FamilyDetails details, String message) {
        //
    }

    public void tryHug(Player target) {
        if (player.getLocation().getPitch() > 0 && player.getLocation().getPitch() < 70 &&
            player.isSneaking() &&
            player.getLocation().distance(target.getLocation()) < 2 &&
            cooldownPlayers.contains(player)) {

            // Встановити КД
            cooldownPlayers.add(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    cooldownPlayers.remove(player);
                }
            }.runTaskLater(familyPlugin, 60L);
            
            PlayerFamily targetFamily = FamilyUtils.getFamily(target);
            FamilyDetails details = FamilyDetailsGet.getRootFamilyDetails(targetFamily);
            
            boolean access = false;

            if (details != null && details.hasAccess(targetFamily, getTypeTargetComponent())) {
            	access = true;
            } else /*if (   )*/ {
            	access = true;
            }
            
            if (access) {
                // Повідомлення в actionbar
                sendActionBarMessage(player, "Ви обняли гравця " + target.getName(), "#fc4e03");
                sendActionBarMessage(target, "Вас обійняв гравець " + player.getName(), "#fc4e03");

                // Частинки сердець, якщо стать різна
                if (true) { // Передбачається, що є метод getGender()
                    target.getWorld().spawnParticle(Particle.HEART, target.getLocation(), 10);
                    player.getWorld().spawnParticle(Particle.HEART, player.getLocation(), 10);
                }
            } else {
                sendMessage(new MessageForFormatting(getInvalidAccessMessage(), new String[]{target.getName()}), MessageType.WARNING, player);
            }
        }
    }
    
    public void setHugsAccess() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs access <NickName> <allow|deny|default>"}), MessageType.WARNING, player);
            return;
        }
        setAccess(args[1], args[2], TypeTargetComponent.HUGS);
    }

    public void setDefaultHugsAccess() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs default <children|parents> <allow|deny>"}), MessageType.WARNING, player);
            return;
        }
        setDefaultAccess(args[1], args[2], TypeTargetComponent.HUGS);
    }

    public void checkHugsAccess() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs check <NickName>"}), MessageType.WARNING, player);
            return;
        }
        checkAccess(args[1], TypeTargetComponent.HUGS);
    }

    public void checkDefaultHugsAccess() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fhugs defaultcheck <children|parents>"}), MessageType.WARNING, player);
            return;
        }
        checkDefaultAccess(args[1], TypeTargetComponent.HUGS);
    }

     MessageComponents buildInteractiveMessage(FamilyDetails details, String message, Player recipient) {
        // Заглушка для побудови повідомлення для обіймів
        return null;
    }

     void sendInteractiveMessageToPlayer(Player recipient, FamilyDetails details, String message) {
        // Заглушка для відправки інтерактивного повідомлення гравцю
    }
}

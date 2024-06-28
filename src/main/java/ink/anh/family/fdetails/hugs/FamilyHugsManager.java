package ink.anh.family.fdetails.hugs;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import ink.anh.api.enums.Access;
import ink.anh.api.messages.Logger;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.permissions.HugsPermission;
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

    public boolean tryHug(Player target) {

        if (player.getLocation().getPitch() > 0 && player.getLocation().getPitch() < 70 && player.isSneaking() && player.getLocation().distance(target.getLocation()) < 2) {

            if (!cooldownPlayers.contains(player)) {
                
                // Встановити КД
                cooldownPlayers.add(player);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        cooldownPlayers.remove(player);
                    }
                }.runTaskLater(familyPlugin, 60L);

                PlayerFamily targetFamily = FamilyUtils.getFamily(target);
                PlayerFamily playerFamily = FamilyUtils.getFamily(player);

                HugsPermission permission = new HugsPermission();
                FamilyDetails details = FamilyDetailsGet.getRootFamilyDetails(targetFamily);

                if (!permission.checkPermission(playerFamily, details, getTypeTargetComponent())) {
                    
                    // Відштовхнути гравця
                    Vector direction = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
                    direction.setY(0.5);
                    player.setVelocity(direction.multiply(1.5));

                    DamageSource slapDamageSource = DamageSource.builder(DamageType.PLAYER_ATTACK)
                            .withCausingEntity(target)
                            .withDirectEntity(target)
                            .build();

                    player.damage(1.0, slapDamageSource);

                    // Ефект VILLAGER_ANGRY перед очима гравця
                    player.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, player.getLocation().add(0, 1.5, 0), 1, 0.2, 0.2, 0.2, 0.05);

                    sendActionBarMessage(player, new MessageForFormatting(getInvalidAccessMessage(), new String[]{target.getName()}), "#820419");

                    return true;
                }

                Logger.info(familyPlugin, "Permission check failed");

                Particle particle = Particle.SPELL_WITCH;
                String hexColor = "#a40cf0";
                
                if (playerFamily.getSpouse() != null && playerFamily.getSpouse() == targetFamily.getRoot()) {
                    particle = Particle.HEART;
                    hexColor = "#f24607";
                    Logger.info(familyPlugin, "Spouse relationship detected");
                } else if (playerFamily.isFamilyMember(targetFamily.getRoot())) {
                    particle = Particle.VILLAGER_HAPPY;
                    hexColor = "#62fc03";
                    Logger.info(familyPlugin, "Family member relationship detected");
                }

                target.getWorld().spawnParticle(particle, target.getLocation(), 10);
                player.getWorld().spawnParticle(particle, player.getLocation(), 10);
                Logger.info(familyPlugin, "Particle effects spawned");

                // Повідомлення в actionbar
                sendActionBarMessage(player, new MessageForFormatting("family_hugs_access", new String[]{target.getName()}), hexColor);
                sendActionBarMessage(target, new MessageForFormatting("family_hugs_access_you", new String[]{player.getName()}), hexColor);
                Logger.info(familyPlugin, "Action bar messages sent to player and target");

                return true;
            } else {
                Logger.info(familyPlugin, "Player is in cooldown");
            }
        } else {
            Logger.info(familyPlugin, "Position check false");
        }

        Logger.info(familyPlugin, "tryHug end");
        return false;
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

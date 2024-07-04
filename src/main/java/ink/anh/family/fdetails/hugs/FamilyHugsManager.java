package ink.anh.family.fdetails.hugs;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import ink.anh.api.enums.Access;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.fplayer.permissions.HugsPermission;
import ink.anh.family.fdetails.AbstractDetailsManager;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.RelationshipDegree;
import ink.anh.family.util.StringColorUtils;
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
                    player.setVelocity(direction.multiply(0.5));

                    DamageSource slapDamageSource = DamageSource.builder(DamageType.PLAYER_ATTACK)
                            .withCausingEntity(target)
                            .withDirectEntity(target)
                            .build();

                    player.damage(1.0, slapDamageSource);

                    // Ефект VILLAGER_ANGRY перед очима гравця
                    player.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, player.getLocation().add(0, 1.5, 0), 1, 0.2, 0.2, 0.2, 0.05);

                    // Спавнування частинок вздовж вектора руху
                    spawnParticlesAlongVector(player, Particle.VILLAGER_ANGRY, direction, 5.0, 10, 2);

                    sendActionBarMessage(player, new MessageForFormatting(getInvalidAccessMessage(), new String[]{target.getName()}), StringColorUtils.HUGS_DENY);

                    return true;
                }

                Particle particle = Particle.SPELL_WITCH;
                String hexColor = StringColorUtils.HUGS_OTHER;
                
                if (FamilyUtils.getRelationshipDegree(playerFamily, target.getUniqueId()) == RelationshipDegree.SPOUSE) {
                    particle = Particle.HEART;
                    hexColor = StringColorUtils.HUGS_SPOUSE;
                } else if (playerFamily.isFamilyMember(targetFamily.getRoot())) {
                    particle = Particle.VILLAGER_HAPPY;
                    hexColor = StringColorUtils.HUGS_RELATIVE;
                }
                
                // Спавнити частинки на рівні плечей
                target.getWorld().spawnParticle(particle, target.getLocation().add(0, 1.5, 0), 10);

                player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1.5, 0), 10);

                // Повідомлення в actionbar
                String huggedMesage = playerFamily.getGender() == Gender.MALE ? "family_you_hugged_player1" : 
                	playerFamily.getGender() == Gender.FEMALE ? "family_you_hugged_player2" : "family_you_hugged_player3";
                
                String huggedMesageYou = targetFamily.getGender() == Gender.MALE ? "family_hugged_player_you1" : 
                	targetFamily.getGender() == Gender.FEMALE ? "family_hugged_player_you2" : "family_hugged_player_you3";
                
                sendActionBarMessage(player, new MessageForFormatting(huggedMesage, new String[]{target.getName()}), hexColor);
                sendActionBarMessage(target, new MessageForFormatting(huggedMesageYou, new String[]{player.getName()}), hexColor);

                if (AnhyFamily.isProtocolLibEnabled()) {
                	// SyncExecutor.runSync(() -> ZombieArmAnimator.toggleZombieArms(player, 20L, familyPlugin));
                }
                return true;
            }
        }
        return false;
    }

    private void spawnParticlesAlongVector(Player player, Particle particle, Vector direction, double distance, int count, int delay) {
        new BukkitRunnable() {
            double coveredDistance = 0;

            @Override
            public void run() {
                if (coveredDistance >= distance) {
                    this.cancel();
                    return;
                }
                coveredDistance += direction.length();
                Location location = player.getLocation().add(direction.clone().multiply(coveredDistance));
                player.getWorld().spawnParticle(particle, location.add(0, 1.5, 0), 1, 0.2, 0.2, 0.2, 0.05);
            }
        }.runTaskTimer(familyPlugin, 0L, delay);
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

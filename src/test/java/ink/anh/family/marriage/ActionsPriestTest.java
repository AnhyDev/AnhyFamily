package ink.anh.family.marriage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import ink.anh.family.AnhyFamily;
import ink.anh.family.FamilyConfig;
import ink.anh.family.GlobalManager;
import ink.anh.family.util.OtherUtils;
import ink.anh.family.util.FamilyUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ActionsPriestTest {

    private ActionsPriest actionsPriest;
    private AnhyFamily familyPlugin;
    private GlobalManager manager;
    private MarriageManager marriageManager;
    private MarriageValidator validator;
    private FamilyConfig familyConfig;

    @BeforeEach
    public void setUp() {
        familyPlugin = mock(AnhyFamily.class);
        manager = mock(GlobalManager.class);
        marriageManager = mock(MarriageManager.class);
        validator = mock(MarriageValidator.class);
        familyConfig = mock(FamilyConfig.class);

        when(manager.getMarriageManager()).thenReturn(marriageManager);
        when(manager.getFamilyConfig()).thenReturn(familyConfig);
        when(GlobalManager.getInstance()).thenReturn(manager);

        actionsPriest = new ActionsPriest(familyPlugin);
        actionsPriest.setValidator(validator); // Override the validator with the mocked one
    }

    @Test
    public void testMarry_ValidConditions() {
        CommandSender sender = mock(CommandSender.class);
        Player priest = mock(Player.class);
        Player bride1 = mock(Player.class);
        Player bride2 = mock(Player.class);

        String[] args = {"marry", "bride1", "bride2"};

        when(validator.validateCommandInput(sender, args)).thenReturn(true);
        when(sender instanceof Player).thenReturn(true);
        when((Player) sender).thenReturn(priest);
        when(Bukkit.getPlayerExact("bride1")).thenReturn(bride1);
        when(Bukkit.getPlayerExact("bride2")).thenReturn(bride2);
        when(FamilyUtils.getPriestTitle(priest)).thenReturn("Priest");
        when(familyConfig.getCeremonyHearingRadius()).thenReturn(10);
        Player[] recipients = new Player[]{};
        try (MockedStatic<OtherUtils> otherUtilsMockedStatic = Mockito.mockStatic(OtherUtils.class)) {
            otherUtilsMockedStatic.when(() -> OtherUtils.getPlayersWithinRadius(any(), anyInt())).thenReturn(recipients);
            when(validator.validateCeremonyConditions(bride1, bride2, recipients)).thenReturn(true);
            when(validator.validatePermissions(bride1, bride2, recipients)).thenReturn(true);
            when(validator.validateCeremonyParticipants(bride1, bride2, recipients)).thenReturn(true);

            ProcessLastName processLastName = mock(ProcessLastName.class);
            String[] lastName = {"LastName1, LastName1"};
            when(processLastName.getLastName()).thenReturn(lastName);
            when(processLastName.getNumberLastName()).thenReturn(1);
            when(validator.processLastNameArgs(args)).thenReturn(processLastName);
            when(validator.validatePayment(bride1, bride2, recipients, "bride1", "bride2")).thenReturn(true);
            when(marriageManager.add(bride1, bride2, priest, 1, lastName)).thenReturn(true);

            BukkitScheduler scheduler = mock(BukkitScheduler.class);
            try (MockedStatic<Bukkit> bukkitMockedStatic = Mockito.mockStatic(Bukkit.class)) {
                bukkitMockedStatic.when(Bukkit::getScheduler).thenReturn(scheduler);

                boolean result = actionsPriest.marry(sender, args);

                assertTrue(result);

                ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
                verify(scheduler).runTaskLater(eq(familyPlugin), captor.capture(), eq(10L));

                // Run the captured runnable
                captor.getValue().run();

                verify(validator).validateCommandInput(sender, args);
                verify(validator).validateCeremonyConditions(bride1, bride2, recipients);
                verify(validator).validatePermissions(bride1, bride2, recipients);
                verify(validator).validateCeremonyParticipants(bride1, bride2, recipients);
                verify(validator).validatePayment(bride1, bride2, recipients, "bride1", "bride2");
                verify(marriageManager).add(bride1, bride2, priest, 1, lastName);
            }
        }
    }

    // Додати додаткові тести для інших умов та негативних сценаріїв

}

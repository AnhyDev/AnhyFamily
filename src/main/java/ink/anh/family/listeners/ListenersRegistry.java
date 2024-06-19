package ink.anh.family.listeners;

import org.bukkit.event.Listener;
import ink.anh.family.AnhyFamily;

public class ListenersRegistry {

    private final AnhyFamily familyPlugin;
    
    private AnswerBridesChatListener bridesChatListener;
    private FamilyChestListener familyChestListener;
    private PlayerInteractionListener playerInteractionListener;

    public ListenersRegistry(AnhyFamily familyPlugin) {
        this.familyPlugin = familyPlugin;
    }
    
    public void register() {
    	bridesChatListener = new AnswerBridesChatListener(familyPlugin);
        familyChestListener = new FamilyChestListener(familyPlugin);
        playerInteractionListener = new PlayerInteractionListener(familyPlugin);
    	
        Listener[] listeners = new Listener[] {bridesChatListener, familyChestListener, playerInteractionListener};
        registerListeners(listeners);
    }

    /**
     * Registers the specified listeners with the Bukkit event manager.
     *
     * @param listeners The listeners to register.
     */
    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            familyPlugin.getServer().getPluginManager().registerEvents(listener, familyPlugin);
        }
    }
}

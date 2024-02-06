package ink.anh.family.listeners;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import ink.anh.family.AnhyFamily;

public class ListenersRegistratar {

    private final AnhyFamily familiPlugin;
    
    private AnswerBridesChatListener bridesChat;

    public ListenersRegistratar(AnhyFamily familiPlugin) {
        this.familiPlugin = familiPlugin;
    }
    
    public void register() {
    	bridesChat = new AnswerBridesChatListener(familiPlugin);
    	
    	Listener[] listeners = new Listener[] {bridesChat};
    	registerListeners(listeners);
    }

    /**
     * Registers the specified listeners with the Bukkit event manager.
     *
     * @param listeners The listeners to register.
     */
    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
        	familiPlugin.getServer().getPluginManager().registerEvents(listener, familiPlugin);
        }
    }

    /**
     * Unregisters the specified listeners from the Bukkit event manager.
     * <p>
     * Note: This method is currently not in use.
     *
     * @param listeners The listeners to unregister.
     */
    @SuppressWarnings("unused")
	private void unregisterListeners(Listener... listeners) {
        for (Listener listener : listeners) {
        	HandlerList.unregisterAll(listener);
        }
    }
}

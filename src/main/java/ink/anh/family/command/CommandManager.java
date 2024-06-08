package ink.anh.family.command;

import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.chat.FamilyChatTabCompleter;
import ink.anh.family.fdetails.chest.FamilyChestTabCompleter;
import ink.anh.family.fdetails.home.FamilyHomeTabCompleter;

public class CommandManager {

    private final AnhyFamily familiPlugin;

    public CommandManager(AnhyFamily familiPlugin) {
        this.familiPlugin = familiPlugin;
    }

    public void registerCommands() {
    	familiPlugin.getCommand("gender").setExecutor(new GenderCommand());
    	familiPlugin.getCommand("gender").setTabCompleter(new GenderTabCompleter());
    	
    	familiPlugin.getCommand("family").setExecutor(new FamilyCommand(familiPlugin));
    	
    	familiPlugin.getCommand("marry").setExecutor(new MarryCommand(familiPlugin));
    	familiPlugin.getCommand("marry").setTabCompleter(new MarryTabCompleter());
    	
    	familiPlugin.getCommand("adoption").setExecutor(new AdoptionCommand(familiPlugin));
    	familiPlugin.getCommand("adoption").setTabCompleter(new AdoptionTabCompleter());
    	
    	familiPlugin.getCommand("fhome").setExecutor(new FamilyHomeCommand(familiPlugin));
    	familiPlugin.getCommand("fhome").setTabCompleter(new FamilyHomeTabCompleter());
    	
    	familiPlugin.getCommand("fchat").setExecutor(new FamilyChatCommand(familiPlugin));
    	familiPlugin.getCommand("fchat").setTabCompleter(new FamilyChatTabCompleter());
    	
    	familiPlugin.getCommand("fchest").setExecutor(new FamilyChestCommand(familiPlugin));
    	familiPlugin.getCommand("fchest").setTabCompleter(new FamilyChestTabCompleter());
    }
}
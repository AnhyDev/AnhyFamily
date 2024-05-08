package ink.anh.family.command;

import ink.anh.family.AnhyFamily;

public class CommandManager {

    private final AnhyFamily familiPlugin;

    public CommandManager(AnhyFamily familiPlugin) {
        this.familiPlugin = familiPlugin;
    }

    public void registerCommands() {
    	familiPlugin.getCommand("gender").setExecutor(new GenderCommand(familiPlugin));
    	familiPlugin.getCommand("family").setExecutor(new FamilyCommand(familiPlugin));
    	familiPlugin.getCommand("marry").setExecutor(new MarryCommand(familiPlugin));
    	familiPlugin.getCommand("adoption").setExecutor(new AdoptionCommand(familiPlugin));
    }
}
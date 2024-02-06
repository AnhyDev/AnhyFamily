package ink.anh.family;

import ink.anh.family.command.FamilyCommand;
import ink.anh.family.gender.GenderCommand;

public class CommandManager {

    private final AnhyFamily familiPlugin;

    public CommandManager(AnhyFamily familiPlugin) {
        this.familiPlugin = familiPlugin;
    }

    public void registerCommands() {
    	familiPlugin.getCommand("gender").setExecutor(new GenderCommand(familiPlugin));
    	familiPlugin.getCommand("family").setExecutor(new FamilyCommand(familiPlugin));
    }
}
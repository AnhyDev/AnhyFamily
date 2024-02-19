package ink.anh.family.papi;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import ink.anh.api.LibraryManager;
import ink.anh.api.lingo.Translator;
import ink.anh.api.utils.LangUtils;
import ink.anh.family.AnhyFamily;
import ink.anh.family.common.Family;
import ink.anh.family.gender.Gender;

public class GenderPlaceholders {

    private final LibraryManager libraryManager;
    
    public GenderPlaceholders(AnhyFamily familyPlugin) {
		this.libraryManager = familyPlugin.getGlobalManager();
    }

    public String getGenderType(Family family) {
    	Gender gender = getGender(family);
    	if(gender == null) {
    		return null;
    	}
    	
		return gender.name();
    }

    public String getGenderLangKey(Family family) {
    	Gender gender = getGender(family);
    	if(gender == null) {
    		return null;
    	}
    	
		return Gender.getKey(gender);
    }

    public String getGenderHEXColor(Family family) {
    	Gender gender = getGender(family);
    	if(gender == null) {
    		return null;
    	}
    	
		return Gender.getColor(gender);
    }

    public String getGenderMCColor(Family family) {
    	Gender gender = getGender(family);
    	if(gender == null) {
    		return null;
    	}
    	
		return Gender.getMinecraftColor(gender);
    }

    public String getGenderSymbol(Family family) {
    	Gender gender = getGender(family);
    	if(gender == null) {
    		return null;
    	}
    	
		return Gender.getSymbol(gender);
    }

    public String getGenderLangName(OfflinePlayer player, Family family) {
    	Gender gender = getGender(family);
    	if(gender == null) {
    		return null;
    	}
		return Translator.translateKyeWorld(libraryManager, Gender.getKey(gender) , langs(player));
    }

    private Gender getGender(Family family) {
    	if(family == null) {
    		return null;
    	}
		return family.getGender();
    }
    
    private String[] langs(OfflinePlayer player) {
    	return player.isOnline() ? LangUtils.getPlayerLanguage((Player) player) : new String[] {libraryManager.getDefaultLang()};
    }
}

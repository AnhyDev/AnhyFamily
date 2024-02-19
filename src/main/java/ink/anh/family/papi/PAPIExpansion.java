package ink.anh.family.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import ink.anh.family.AnhyFamily;
import ink.anh.family.common.Family;
import ink.anh.family.util.FamilyUtils;

public class PAPIExpansion extends PlaceholderExpansion {

    private final AnhyFamily familyPlugin;

    public PAPIExpansion(AnhyFamily familyPlugin) {
        this.familyPlugin = familyPlugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", familyPlugin.getDescription().getAuthors());
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "anhy";
    }

    @Override
    @NotNull
    public String getVersion() {
        return familyPlugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        GenderPlaceholders genderPlaceholders = new GenderPlaceholders(familyPlugin);
        Family family = getFamily(player);
        
        switch (params.toLowerCase()) {
            case "gender":
                return genderPlaceholders.getGenderType(family);
            case "gender_key":
                return genderPlaceholders.getGenderLangKey(family);
            case "gender_lang":
                return genderPlaceholders.getGenderLangName(player, family);
            case "gender_symbol":
                return genderPlaceholders.getGenderSymbol(family);
            case "gender_mccolor":
                return genderPlaceholders.getGenderMCColor(family);
            case "gender_hexcolor":
                return genderPlaceholders.getGenderHEXColor(family);
            default:
                return null;
        }
    }

    private Family getFamily(OfflinePlayer player) {
    	Family family = null;
    	if (player.isOnline()) {
    		family = FamilyUtils.getFamily((Player) player);
    	}
    	
    	if (family == null) {
    		family = FamilyUtils.getFamily(player.getUniqueId());
    	}
		return family;
    }
}
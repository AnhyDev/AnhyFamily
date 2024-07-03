package ink.anh.family.util;

import ink.anh.api.enums.Access;
import ink.anh.api.messages.Logger;
import ink.anh.family.AnhyFamily;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class StringColorUtils {

    private static File configFile;
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    
    public static String PRIEST_PREFIX = "#C78470";
    public static String PRIEST_NAME = "#D7ADA3";
    public static String PRIESTESS_PREFIX = "#C78470";
    public static String PRIESTESS_NAME = "#D7ADA3";
    public static String PRIEST_NONBINARY_PREFIX = "#808080";
    public static String PRIEST_NONBINARY_NAME = "#32CD32";
    public static String PRIEST_PRIVATE_PREFIX = "#8B4513";
    public static String PRIEST_PRIVATE_NAME = "#F4A460";    
    public static String GROOM_PREFIX = "#4184BD";
    public static String GROOM_NAME = "#8AC2F0";
    public static String BRIDE_PREFIX = "#db7bc8";
    public static String BRIDE_NAME = "#FFB6F5";
    public static String BRIDE_NONBINARY_PREFIX = "#40E0D0";
    public static String BRIDE_NONBINARY_NAME = "#20B2AA";  
    public static String MESSAGE_COLOR = "#0bdebb";
    public static String GROUP_COLOR = "#0bdebb";
    public static String ACCESS_COLOR_TRUE = "#00FF00";
    public static String ACCESS_COLOR_FALSE = "#FF0000";
    public static String ACCESS_COLOR_DEFAULT = "#FFFF00";
    public static String SEPARATOR_COLOR = "#cedcf2";
    public static String PREFIX_CHAT_COLOR = "#6ea4fa";
    public static String PREFIX_CHEST_COLOR = "#03f0fc";
    public static String PREFIX_HOME_COLOR = "#03fc94";
    public static String PLUGIN_COLOR = "#1D87E4";
    public static String SYMBOL_COLOR = "#0bdebb";
    public static String ARROW_COLOR = "#8a690f";
    public static String TREE_COLOR = "#228B22";
    public static String PLAYER_NAME_COLOR = "#fac32a";
    public static String FAMILY_CHAT_COLOR = "#2ab1fa";
    public static String HUGS_OTHER = "#a40cf0";
    public static String HUGS_RELATIVE = "#62fc03";
    public static String HUGS_SPOUSE = "#f24607";
    public static String HUGS_DENY = "#820419";
    public static String GENDER_MALE_COLOR = "#1E90FF";
    public static String GENDER_FEMALE_COLOR = "#FF69B4";
    public static String GENDER_NON_BINARY_COLOR = "#8B4513";
    public static String GENDER_UNDECIDED_COLOR = "#808080";
    public static String PROFILE_COMPONENT_COLOR1 = "#00FF00";
    public static String PROFILE_COMPONENT_COLOR2 = "#FFFFFF";
    public static String PROFILE_COMPONENT_COLOR3 = "#FFFF00";
    public static String PROFILE_COMPONENT_COLOR4 = "#FF0000";
    public static String PROFILE_COMPONENT_COLOR5 = "#12ccad";
    public static String TREE_TITLE_COLOR = "#FFD700";
    public static String TREE_MEMBER_NAME_COLOR = "#029952";
    public static String TREE_REPEATED_COLOR = "#8B0000";
    public static String TREE_LEVEL0_COLOR = "#00FF00";
    public static String TREE_LEVEL1_COLOR = "#FFD700";
    public static String TREE_LEVEL2_COLOR = "#1E90FF";
    public static String TREE_LEVEL3_COLOR = "#00FFFF";
    public static String TREE_LEVEL4_COLOR = "#FF69B4";
    public static String TREE_LEVEL5_COLOR = "#FF4500";
    public static String TREE_LEVEL6_COLOR = "#ADFF2F";
    public static String TREE_DEFAULT_COLOR = "#808080";
    

    public static String DEFAULT_PREFIX_COLOR = "#FFA500";
    public static String DEFAULT_NICKNAME_COLOR = "#FFD700";
    public static String OTHER_SUB_PREFIX_COLOR = "#0bdebb";

    
    public static void initialize(File dataFolder) {
        configFile = new File(dataFolder, "colors.yml");
        if (!configFile.exists()) {
            copyDefaultConfig();
        }
        reloadColors();
    }

    private static void copyDefaultConfig() {
        try (InputStream in = StringColorUtils.class.getResourceAsStream("/colors.yml")) {
            if (in != null) {
                Files.copy(in, configFile.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadColors(FileConfiguration config) {
        PRIEST_PREFIX = getColorFromConfig(config, "priest_prefix", PRIEST_PREFIX);
        PRIEST_NAME = getColorFromConfig(config, "priest_name", PRIEST_NAME);
        PRIESTESS_PREFIX = getColorFromConfig(config, "priestess_prefix", PRIESTESS_PREFIX);
        PRIESTESS_NAME = getColorFromConfig(config, "priestess_name", PRIESTESS_NAME);
        PRIEST_NONBINARY_PREFIX = getColorFromConfig(config, "priest_nonbinary_prefix", PRIEST_NONBINARY_PREFIX);
        PRIEST_NONBINARY_NAME = getColorFromConfig(config, "priest_nonbinary_name", PRIEST_NONBINARY_NAME);
        PRIEST_PRIVATE_PREFIX = getColorFromConfig(config, "priest_private_prefix", PRIEST_PRIVATE_PREFIX);
        PRIEST_PRIVATE_NAME = getColorFromConfig(config, "priest_private_name", PRIEST_PRIVATE_NAME);
        GROOM_PREFIX = getColorFromConfig(config, "groom_prefix", GROOM_PREFIX);
        GROOM_NAME = getColorFromConfig(config, "groom_name", GROOM_NAME);
        BRIDE_PREFIX = getColorFromConfig(config, "bride_prefix", BRIDE_PREFIX);
        BRIDE_NAME = getColorFromConfig(config, "bride_name", BRIDE_NAME);
        BRIDE_NONBINARY_PREFIX = getColorFromConfig(config, "bride_nonbinary_prefix", BRIDE_NONBINARY_PREFIX);
        BRIDE_NONBINARY_NAME = getColorFromConfig(config, "bride_nonbinary_name", BRIDE_NONBINARY_NAME);
        MESSAGE_COLOR = getColorFromConfig(config, "message_color", MESSAGE_COLOR);
        GROUP_COLOR = getColorFromConfig(config, "group_color", GROUP_COLOR);
        ACCESS_COLOR_TRUE = getColorFromConfig(config, "access_color_true", ACCESS_COLOR_TRUE);
        ACCESS_COLOR_FALSE = getColorFromConfig(config, "access_color_false", ACCESS_COLOR_FALSE);
        ACCESS_COLOR_DEFAULT = getColorFromConfig(config, "access_color_default", ACCESS_COLOR_DEFAULT);
        SEPARATOR_COLOR = getColorFromConfig(config, "separator_color", SEPARATOR_COLOR);
        PREFIX_CHAT_COLOR = getColorFromConfig(config, "prefix_chat_color", PREFIX_CHAT_COLOR);
        PREFIX_CHEST_COLOR = getColorFromConfig(config, "prefix_chest_color", PREFIX_CHEST_COLOR);
        PREFIX_HOME_COLOR = getColorFromConfig(config, "prefix_home_color", PREFIX_HOME_COLOR);
        PLUGIN_COLOR = getColorFromConfig(config, "plugin_color", PLUGIN_COLOR);
        SYMBOL_COLOR = getColorFromConfig(config, "symbol_color", SYMBOL_COLOR);
        ARROW_COLOR = getColorFromConfig(config, "arrow_color", ARROW_COLOR);
        TREE_COLOR = getColorFromConfig(config, "tree_color", TREE_COLOR);
        PLAYER_NAME_COLOR = getColorFromConfig(config, "player_name_color", PLAYER_NAME_COLOR);
        FAMILY_CHAT_COLOR = getColorFromConfig(config, "family_chat_color", FAMILY_CHAT_COLOR);
        HUGS_OTHER = getColorFromConfig(config, "hugs_other", HUGS_OTHER);
        HUGS_RELATIVE = getColorFromConfig(config, "hugs_relative", HUGS_RELATIVE);
        HUGS_SPOUSE = getColorFromConfig(config, "hugs_spouse", HUGS_SPOUSE);
        HUGS_DENY = getColorFromConfig(config, "hugs_deny", HUGS_DENY);
        GENDER_MALE_COLOR = getColorFromConfig(config, "gender_male_color", GENDER_MALE_COLOR);
        GENDER_FEMALE_COLOR = getColorFromConfig(config, "gender_female_color", GENDER_FEMALE_COLOR);
        GENDER_NON_BINARY_COLOR = getColorFromConfig(config, "gender_non_binary_color", GENDER_NON_BINARY_COLOR);
        GENDER_UNDECIDED_COLOR = getColorFromConfig(config, "gender_undecided_color", GENDER_UNDECIDED_COLOR);
        PROFILE_COMPONENT_COLOR1 = getColorFromConfig(config, "profile_component_color1", PROFILE_COMPONENT_COLOR1);
        PROFILE_COMPONENT_COLOR2 = getColorFromConfig(config, "profile_component_color2", PROFILE_COMPONENT_COLOR2);
        PROFILE_COMPONENT_COLOR3 = getColorFromConfig(config, "profile_component_color3", PROFILE_COMPONENT_COLOR3);
        PROFILE_COMPONENT_COLOR4 = getColorFromConfig(config, "profile_component_color4", PROFILE_COMPONENT_COLOR4);
        PROFILE_COMPONENT_COLOR5 = getColorFromConfig(config, "profile_component_color5", PROFILE_COMPONENT_COLOR5);
        TREE_TITLE_COLOR = getColorFromConfig(config, "tree_title_color", TREE_TITLE_COLOR);
        TREE_MEMBER_NAME_COLOR = getColorFromConfig(config, "tree_member_name_color", TREE_MEMBER_NAME_COLOR);
        TREE_REPEATED_COLOR = getColorFromConfig(config, "tree_repeated_color", TREE_REPEATED_COLOR);
        TREE_LEVEL0_COLOR = getColorFromConfig(config, "tree_level0_color", TREE_LEVEL0_COLOR);
        TREE_LEVEL1_COLOR = getColorFromConfig(config, "tree_level1_color", TREE_LEVEL1_COLOR);
        TREE_LEVEL2_COLOR = getColorFromConfig(config, "tree_level2_color", TREE_LEVEL2_COLOR);
        TREE_LEVEL3_COLOR = getColorFromConfig(config, "tree_level3_color", TREE_LEVEL3_COLOR);
        TREE_LEVEL4_COLOR = getColorFromConfig(config, "tree_level4_color", TREE_LEVEL4_COLOR);
        TREE_LEVEL5_COLOR = getColorFromConfig(config, "tree_level5_color", TREE_LEVEL5_COLOR);
        TREE_LEVEL6_COLOR = getColorFromConfig(config, "tree_level6_color", TREE_LEVEL6_COLOR);
        TREE_DEFAULT_COLOR = getColorFromConfig(config, "tree_default_color", TREE_DEFAULT_COLOR);
        DEFAULT_PREFIX_COLOR = getColorFromConfig(config, "default_prefix_color", DEFAULT_PREFIX_COLOR);
        DEFAULT_NICKNAME_COLOR = getColorFromConfig(config, "default_nickname_color", DEFAULT_NICKNAME_COLOR);
        OTHER_SUB_PREFIX_COLOR = getColorFromConfig(config, "other_sub_prefix_color", OTHER_SUB_PREFIX_COLOR);

    }

    private static String getColorFromConfig(FileConfiguration config, String key, String defaultColor) {
        String color = config.getString(key);
        if (validateColor(color)) {
            return color;
        } else {
            Logger.info(AnhyFamily.getInstance(), "Invalid color format for key: " + key + " with value: " + color);
            return defaultColor;
        }
    }

    private static boolean validateColor(String color) {
        return color != null && HEX_PATTERN.matcher(color).matches();
    }

    public static void reloadColors() {
        if (!configFile.exists()) {
            copyDefaultConfig();
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        loadColors(config);
    }

    public static String getAccessColor(Access access) {
        return access == Access.TRUE ? ACCESS_COLOR_TRUE : 
               access == Access.FALSE ? ACCESS_COLOR_FALSE : 
               ACCESS_COLOR_DEFAULT;
    }

    public static String colorSet(String startColor, String element) {
        return "&" + validateColor(startColor) + element + "&" + MESSAGE_COLOR;
    }

    public static String colorSet(String startColor, String element, String finishColor) {
        return "&" + validateColor(startColor) + element + "&" + validateColor(finishColor);
    }
}

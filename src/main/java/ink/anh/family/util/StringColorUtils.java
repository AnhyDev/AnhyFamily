package ink.anh.family.util;

import ink.anh.api.enums.Access;

public class StringColorUtils {

    public static final String MESSAGE_COLOR = "#0bdebb";
    public static final String GROUP_COLOR = "#0bdebb";
    public static final String ACCESS_COLOR_TRUE = "#00FF00";
    public static final String ACCESS_COLOR_FALSE = "#FF0000";
    public static final String ACCESS_COLOR_DEFAULT = "#FFFF00";
    public static final String SEPARATOR_COLOR = "#cedcf2";
    public static final String PREFIX_COLOR = "#6ea4fa";
    public static final String PLUGIN_COLOR = "#1D87E4";

    public static String getAccessColor(Access access) {
        return access == Access.TRUE ? ACCESS_COLOR_TRUE : access == Access.FALSE ? ACCESS_COLOR_FALSE : ACCESS_COLOR_DEFAULT;
    }

    public static String colorSet(String startColor, String element) {
        return "&" + startColor + element + "&" + MESSAGE_COLOR;
    }

    public static String colorSet(String startColor, String element, String finishColor) {
        return "&" + startColor + element + "&" + finishColor;
    }
}

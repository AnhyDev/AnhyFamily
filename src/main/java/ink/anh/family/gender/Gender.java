package ink.anh.family.gender;

/**
 * Enum representing different genders with associated metadata.
 * Each gender has a unique key, symbol, and color code.
 */
public enum Gender {
    MALE("family_gender_male", "♂", "#1E90FF"),
    FEMALE("family_gender_female", "♀", "#FF69B4"),
    NON_BINARY("family_gender_non-binary", "⚧", "#8B4513"),
    UNDECIDED("family_gender_undecided", "?", "#808080");

    private final String key;     // The key representing the gender (used for localization or references)
    private final String symbol;  // The symbol representing the gender
    private final String color;   // The color code associated with the gender

    /**
     * Constructor for the Gender enum.
     *
     * @param key    The key representing the gender.
     * @param symbol The symbol representing the gender.
     * @param color  The color code associated with the gender.
     */
    Gender(String key, String symbol, String color) {
        this.key = key;
        this.symbol = symbol;
        this.color = color;
    }

    /**
     * Safely gets the key of the specified gender. If the gender is null, returns the key of UNDECIDED.
     *
     * @param gender The gender to get the key for.
     * @return The key of the specified gender, or the key of UNDECIDED if gender is null.
     */
    public static String getKey(Gender gender) {
        return (gender != null) ? gender.key : UNDECIDED.key;
    }

    /**
     * Safely gets the symbol of the specified gender. If the gender is null, returns the symbol of UNDECIDED.
     *
     * @param gender The gender to get the symbol for.
     * @return The symbol of the specified gender, or the symbol of UNDECIDED if gender is null.
     */
    public static String getSymbol(Gender gender) {
        return (gender != null) ? gender.symbol : UNDECIDED.symbol;
    }

    /**
     * Safely gets the color of the specified gender. If the gender is null, returns the color of UNDECIDED.
     *
     * @param gender The gender to get the color for.
     * @return The color of the specified gender, or the color of UNDECIDED if gender is null.
     */
    public static String getColor(Gender gender) {
        return (gender != null) ? gender.color : UNDECIDED.color;
    }

    /**
     * Safely returns the string representation of the specified gender. If the gender is null, returns the string
     * representation of UNDECIDED.
     *
     * @param gender The gender to get the string representation for.
     * @return The string representation of the specified gender, or UNDECIDED if gender is null.
     */
    public static String toStringSafe(Gender gender) {
        return (gender != null) ? gender.toString() : UNDECIDED.toString();
    }

    /**
     * Safely converts a string to its corresponding Gender enum value. 
     * Returns null if the string does not match any Gender enum values.
     *
     * @param str The string to convert to a Gender enum value.
     * @return The corresponding Gender enum value, or null if the string does not match any Gender values.
     */
    public static Gender fromString(String str) {
        if (str != null) {
            for (Gender gender : Gender.values()) {
                if (gender.name().equalsIgnoreCase(str)) {
                    return gender;
                }
            }
        }
        return UNDECIDED;
    }
}

package ink.anh.family.marriage;

public enum MarryPrefixType {
    PRIEST_MALE("family_marry_priest_male", "#000080", "#ADD8E6"), // Navy for prefix, Light Blue for nickname
    PRIEST_FEMALE("family_marry_priest_female", "#800080", "#FF69B4"), // Purple for prefix, Hot Pink for nickname
    PRIEST_NON_BINARY("family_marry_priest_nonbinary", "#808080", "#32CD32"), // Gray for prefix, Lime Green for nickname

    PRIVATE_MARRY_PREFIX("family_marry_private_prefix", "#8B4513", "#F4A460"), // SaddleBrown for prefix, SandyBrown for nickname
	
    BRIDE_MALE("family_marry_groom_male", "#006400", "#00FF00"), // Dark Green for prefix, Green for nickname
    BRIDE_FEMALE("family_marry_groom_female", "#FFC0CB", "#FF1493"), // Pink for prefix, Deep Pink for nickname
    BRIDE_NON_BINARY("family_marry_groom_nonbinary", "#40E0D0", "#20B2AA"), // Turquoise for prefix, Light Sea Green for nickname
    
	DEFAULT("", "#FFA500", "#FFD700"); // Orange for prefix, Gold for nickname

    private final String key;
    private final String prefixColor;
    private final String nicknameColor;

    MarryPrefixType(String key, String prefixColor, String nicknameColor) {
        this.key = key;
        this.prefixColor = prefixColor;
        this.nicknameColor = nicknameColor;
    }

    public String getKey() {
        return key;
    }

    public String getPrefixColor() {
        return prefixColor;
    }

    public String getNicknameColor() {
        return nicknameColor;
    }
}

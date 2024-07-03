package ink.anh.family.marriage;

import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.util.StringColorUtils;

public enum MarryPrefixType {
    PRIEST_MALE("family_marry_priest_male", StringColorUtils.PRIEST_PREFIX, StringColorUtils.PRIEST_NAME),
    PRIEST_FEMALE("family_marry_priest_female", StringColorUtils.PRIESTESS_PREFIX, StringColorUtils.PRIESTESS_NAME),
    PRIEST_NON_BINARY("family_marry_priest_nonbinary", StringColorUtils.PRIEST_NONBINARY_PREFIX, StringColorUtils.PRIEST_NONBINARY_NAME),

    PRIVATE_MARRY_PREFIX("family_marry_private_prefix", StringColorUtils.PRIEST_PRIVATE_PREFIX, StringColorUtils.PRIEST_PRIVATE_NAME),
    
    BRIDE_MALE("family_marry_groom_male", StringColorUtils.GROOM_PREFIX, StringColorUtils.GROOM_NAME),
    BRIDE_FEMALE("family_marry_groom_female", StringColorUtils.BRIDE_PREFIX, StringColorUtils.BRIDE_NAME),
    BRIDE_NON_BINARY("family_marry_groom_nonbinary", StringColorUtils.BRIDE_NONBINARY_PREFIX, StringColorUtils.BRIDE_NONBINARY_NAME),
    
    DEFAULT("", StringColorUtils.DEFAULT_PREFIX_COLOR, StringColorUtils.DEFAULT_NICKNAME_COLOR);

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

    public static MarryPrefixType getMarryPrefixType(Gender gender, int roleIndex) {
        switch (roleIndex) {
            case 0:
                switch (gender) {
                    case MALE:
                        return MarryPrefixType.PRIEST_MALE;
                    case FEMALE:
                        return MarryPrefixType.PRIEST_FEMALE;
                    default:
                        return MarryPrefixType.PRIEST_NON_BINARY;
                }
            case 1:
                switch (gender) {
                    case MALE:
                        return MarryPrefixType.BRIDE_MALE;
                    case FEMALE:
                        return MarryPrefixType.BRIDE_FEMALE;
                    default:
                        return MarryPrefixType.BRIDE_NON_BINARY;
                }
            case 2:
                return MarryPrefixType.PRIVATE_MARRY_PREFIX;
            default:
                return MarryPrefixType.DEFAULT;
        }
    }
}

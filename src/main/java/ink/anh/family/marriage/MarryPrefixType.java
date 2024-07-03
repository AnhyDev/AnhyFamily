package ink.anh.family.marriage;

import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.util.StringColorUtils;

public enum MarryPrefixType {
    PRIEST_MALE("family_marry_priest_male", StringColorUtils.MARRY_PRIEST_MALE_PREFIX_COLOR, StringColorUtils.MARRY_PRIEST_MALE_NICKNAME_COLOR),
    PRIEST_FEMALE("family_marry_priest_female", StringColorUtils.MARRY_PRIEST_FEMALE_PREFIX_COLOR, StringColorUtils.MARRY_PRIEST_FEMALE_NICKNAME_COLOR),
    PRIEST_NON_BINARY("family_marry_priest_nonbinary", StringColorUtils.MARRY_PRIEST_NONBINARY_PREFIX_COLOR, StringColorUtils.MARRY_PRIEST_NONBINARY_NICKNAME_COLOR),

    PRIVATE_MARRY_PREFIX("family_marry_private_prefix", StringColorUtils.PRIVATE_MARRY_PREFIX_COLOR, StringColorUtils.PRIVATE_MARRY_NICKNAME_COLOR),
    
    BRIDE_MALE("family_marry_groom_male", StringColorUtils.BRIDE_MALE_PREFIX_COLOR, StringColorUtils.BRIDE_MALE_NICKNAME_COLOR),
    BRIDE_FEMALE("family_marry_groom_female", StringColorUtils.BRIDE_FEMALE_PREFIX_COLOR, StringColorUtils.BRIDE_FEMALE_NICKNAME_COLOR),
    BRIDE_NON_BINARY("family_marry_groom_nonbinary", StringColorUtils.BRIDE_NONBINARY_PREFIX_COLOR, StringColorUtils.BRIDE_NONBINARY_NICKNAME_COLOR),
    
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

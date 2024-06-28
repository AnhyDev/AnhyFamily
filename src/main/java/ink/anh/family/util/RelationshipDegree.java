package ink.anh.family.util;

public enum RelationshipDegree {
    SPOUSE,
    FATHER,
    MOTHER,
    GRANDPARENT,  // Includes both grandfathers and grandmothers
    GRANDCHILD,   // Includes both grandsons and granddaughters
    GREAT_GRANDPARENT,  // Includes both great-grandfathers and great-grandmothers
    GREAT_GRANDCHILD,   // Includes both great-grandsons and great-granddaughters
    UNKNOWN
}

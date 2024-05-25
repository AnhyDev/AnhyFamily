package ink.anh.family.fdetails;

import ink.anh.api.DataHandler;
import java.util.UUID;

public class FamilyDetailsDataHandler extends DataHandler {

    private static final String FAMILY_DETAILS_DATA_KEY = "fdetails";

    /**
     * Saves the family details for a specific family.
     * 
     * @param familyId   The UUID of the family.
     * @param familyDetails The family details to save.
     */
    public void addFamilyDetails(UUID familyId, FamilyDetails familyDetails) {
        addData(familyId, FAMILY_DETAILS_DATA_KEY, familyDetails);
    }

    /**
     * Retrieves the family details for a specific family.
     * 
     * @param familyId The UUID of the family.
     * @return The FamilyDetails object, or null if no data is found.
     */
    public FamilyDetails getFamilyDetails(UUID familyId) {
        Object data = getData(familyId, FAMILY_DETAILS_DATA_KEY);
        if (data instanceof FamilyDetails) {
            return (FamilyDetails) data;
        }
        removeFamilyDetails(familyId);
        return null;
    }

    /**
     * Removes the family details for a specific family.
     * 
     * @param familyId The UUID of the family.
     */
    public void removeFamilyDetails(UUID familyId) {
        removeData(familyId, FAMILY_DETAILS_DATA_KEY);
    }

    /**
     * Checks if the family details exist for a specific family.
     * 
     * @param familyId The UUID of the family.
     * @return true if family details exist, false otherwise.
     */
    public boolean hasFamilyDetails(UUID familyId) {
        return getData(familyId, FAMILY_DETAILS_DATA_KEY) instanceof FamilyDetails;
    }
}

package ink.anh.family.parents;

import ink.anh.api.enums.Access;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fplayer.PlayerFamily;

import java.util.UUID;

public class ParentHandler {

    public static void handleAdoption(PlayerFamily[] adoptersFamily, PlayerFamily adoptedFamily) {
        if (adoptersFamily == null || adoptersFamily.length == 0 || adoptedFamily == null) {
            return; // Якщо немає батьків або дитини, виходимо
        }

        UUID familyId1 = adoptersFamily[0].getFamilyId();
        UUID familyId2 = (adoptersFamily.length > 1) ? adoptersFamily[1].getFamilyId() : null;

        if (familyId1 != null) {
            addAdoptedChildToFamilyDetails(familyId1, adoptedFamily.getRoot());
        }

        if (familyId2 != null && !familyId2.equals(familyId1)) {
            addAdoptedChildToFamilyDetails(familyId2, adoptedFamily.getRoot());
        }
    }

    private static void addAdoptedChildToFamilyDetails(UUID familyId, UUID childUuid) {
        FamilyDetails familyDetails = FamilyDetailsGet.getFamilyDetails(familyId);
        if (familyDetails != null) {
            familyDetails.getChildrenAccessMap().put(childUuid, new AccessControl(Access.DEFAULT, Access.DEFAULT, Access.DEFAULT));
            FamilyDetailsSave.saveFamilyDetails(familyDetails, null);
        }
    }
}

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

    public static void handleParentSeparation(PlayerFamily parentFamily, PlayerFamily childFamily) {
        UUID familyId = parentFamily.getFamilyId();
        if (familyId != null) {
            removeFromFamilyDetails(familyId, childFamily);
        }
    }

    public static void handleChildSeparation(PlayerFamily childFamily, PlayerFamily parentFamily) {
        UUID familyId = childFamily.getFamilyId();
        if (familyId != null) {
            removeFromFamilyDetails(familyId, parentFamily);
        }
    }

    private static void removeFromFamilyDetails(UUID familyId, PlayerFamily relatedFamily) {
    	if (relatedFamily == null) {
    		return;
    	}

    	UUID relatedUuid = relatedFamily.getRoot();
        FamilyDetails familyDetails = FamilyDetailsGet.getFamilyDetails(familyId);
        if (familyDetails != null) {
            familyDetails.getChildrenAccessMap().remove(relatedUuid);
            familyDetails.getAncestorsAccessMap().remove(relatedUuid);
            FamilyDetailsSave.saveFamilyDetails(familyDetails, null);
        }

        FamilyDetails relatedDetails = FamilyDetailsGet.getFamilyDetails(relatedUuid);
        if (relatedDetails != null) {
            relatedDetails.getChildrenAccessMap().remove(familyId);
            relatedDetails.getAncestorsAccessMap().remove(familyId);
            FamilyDetailsSave.saveFamilyDetails(relatedDetails, null);
        }
    }
}

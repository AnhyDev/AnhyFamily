package ink.anh.family.util;

import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.marriage.FamilyHandler;
import ink.anh.family.parents.ParentHandler;
import java.util.Set;
import java.util.UUID;

public class FullFamilySeparation {

    public static void separateFamilyCompletely(PlayerFamily playerFamily) {
        if (playerFamily == null) {
            return;
        }
        FamilyDetails familyDetails = FamilyDetailsGet.getRootFamilyDetails(playerFamily);
        
        // Розірвання зв'язків з усіма родичами
        separateAllRelations(playerFamily, familyDetails);

        FamilyHandler.handleDivorce(playerFamily);
    }

    private static void separateAllRelations(PlayerFamily playerFamily, FamilyDetails familyDetails) {

        // Розірвання зв'язків з дітьми
        Set<UUID> children = playerFamily.getChildren();
        if (familyDetails != null && familyDetails.getChildrenAccessMap() != null) {
        	children.addAll(familyDetails.getChildrenAccessMap().keySet());
        }
        if (children != null) {
            for (UUID childId : children) {
                PlayerFamily childFamily = FamilyUtils.getFamily(childId);
                if (childFamily != null) {
                    ParentHandler.handleParentSeparation(playerFamily, childFamily);
                }
            }
        }

        // Розірвання зв'язків з батьками
        UUID fatherId = playerFamily.getFather();
        if (fatherId != null) {
            PlayerFamily fatherFamily = FamilyUtils.getFamily(fatherId);
            if (fatherFamily != null) {
                ParentHandler.handleChildSeparation(playerFamily, fatherFamily);
            }
        }

        UUID motherId = playerFamily.getMother();
        if (motherId != null) {
            PlayerFamily motherFamily = FamilyUtils.getFamily(motherId);
            if (motherFamily != null) {
                ParentHandler.handleChildSeparation(playerFamily, motherFamily);
            }
        }
    }
}

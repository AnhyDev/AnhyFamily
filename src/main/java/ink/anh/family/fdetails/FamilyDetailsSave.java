package ink.anh.family.fdetails;

import ink.anh.family.GlobalManager;
import ink.anh.family.db.fdetails.FamilyDetailsTable;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.api.database.TableField;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

public class FamilyDetailsSave {

    private static FamilyDetailsTable familyDetailsTable = (FamilyDetailsTable) GlobalManager.getInstance().getDatabaseManager().getTable(FamilyDetails.class);
    private static FamilyDetailsDataHandler dataHandler = FamilyDetailsDataHandler.getInstance();

    public static void saveFamilyDetails(FamilyDetails familyDetails, FamilyDetailsField fieldToUpdate) {
        dataHandler.addFamilyDetails(familyDetails);
        
        if (fieldToUpdate == null) {
            // Додаємо нові дані
            familyDetailsTable.insert(familyDetails);
        } else {
            // Оновлюємо тільки вказане поле
            updateSingleField(familyDetails, fieldToUpdate);
        }
    }

    private static void updateSingleField(FamilyDetails familyDetails, FamilyDetailsField fieldToUpdate) {
        UUID familyId = familyDetails.getFamilyId();

        if (familyDetailsTable.getFamilyDetails(familyId) == null) {
            // Додаємо нові дані
            familyDetailsTable.insert(familyDetails);
            return;
        }

        String fieldValue;
        switch (fieldToUpdate) {
            case FAMILY_SYMBOL:
                fieldValue = familyDetails.getFamilySymbol();
                break;
            case HOME_LOCATION:
                fieldValue = FamilyDetailsSerializer.serializeLocation(familyDetails.getHomeLocation());
                break;
            case FAMILY_CHEST:
                fieldValue = FamilyDetailsSerializer.serializeChest(familyDetails.getFamilyChest());
                break;
            case CHILDREN_ACCESS:
                fieldValue = FamilyDetailsSerializer.serializeAccessControl(familyDetails.getChildrenAccess());
                break;
            case ANCESTORS_ACCESS:
                fieldValue = FamilyDetailsSerializer.serializeAccessControl(familyDetails.getAncestorsAccess());
                break;
            case CHILDREN_ACCESS_MAP:
                fieldValue = FamilyDetailsSerializer.serializeAccessControlMap(familyDetails.getChildrenAccessMap());
                break;
            case ANCESTORS_ACCESS_MAP:
                fieldValue = FamilyDetailsSerializer.serializeAccessControlMap(familyDetails.getAncestorsAccessMap());
                break;
            case HOME_SET_DATE:
                fieldValue = familyDetails.getHomeSetDate().toString();
                break;
            default:
                // Якщо поле не існує в `enum`, оновлюємо весь об'єкт
                familyDetailsTable.update(familyDetails);
                return;
        }

        familyDetailsTable.updateField(new TableField<>(familyId, fieldToUpdate.getFieldName(), fieldValue));
    }

    public static void addRootFamilyDetails(UUID rootId, FamilyDetails familyDetails, FamilyDetailsField fieldToUpdate) {
        dataHandler.addRootDetails(rootId, familyDetails);
        saveFamilyDetails(familyDetails, fieldToUpdate);
    }

    public static void addRootFamilyDetails(Player player, FamilyDetails familyDetails, FamilyDetailsField fieldToUpdate) {
        UUID rootId = player.getUniqueId();
        addRootFamilyDetails(rootId, familyDetails, fieldToUpdate);
    }

    public static void addRootFamilyDetails(PlayerFamily playerFamily, FamilyDetails familyDetails, FamilyDetailsField fieldToUpdate) {
        UUID rootId = playerFamily.getRoot();
        addRootFamilyDetails(rootId, familyDetails, fieldToUpdate);
    }

    public static void addFatherFamilyDetails(UUID rootId, FamilyDetails fatherDetails, FamilyDetailsField fieldToUpdate) {
        dataHandler.addFatherDetails(rootId, fatherDetails);
        saveFamilyDetails(fatherDetails, fieldToUpdate);
    }

    public static void addFatherFamilyDetails(Player player, FamilyDetails fatherDetails, FamilyDetailsField fieldToUpdate) {
        UUID rootId = player.getUniqueId();
        addFatherFamilyDetails(rootId, fatherDetails, fieldToUpdate);
    }

    public static void addFatherFamilyDetails(PlayerFamily playerFamily, FamilyDetails fatherDetails, FamilyDetailsField fieldToUpdate) {
        UUID rootId = playerFamily.getRoot();
        addFatherFamilyDetails(rootId, fatherDetails, fieldToUpdate);
    }

    public static void addMotherFamilyDetails(UUID rootId, FamilyDetails motherDetails, FamilyDetailsField fieldToUpdate) {
        dataHandler.addMotherDetails(rootId, motherDetails);
        saveFamilyDetails(motherDetails, fieldToUpdate);
    }

    public static void addMotherFamilyDetails(Player player, FamilyDetails motherDetails, FamilyDetailsField fieldToUpdate) {
        UUID rootId = player.getUniqueId();
        addMotherFamilyDetails(rootId, motherDetails, fieldToUpdate);
    }

    public static void addMotherFamilyDetails(PlayerFamily playerFamily, FamilyDetails motherDetails, FamilyDetailsField fieldToUpdate) {
        UUID rootId = playerFamily.getRoot();
        addMotherFamilyDetails(rootId, motherDetails, fieldToUpdate);
    }

    public static void addChildrenFamilyDetails(UUID rootId, List<FamilyDetails> childrenDetails, FamilyDetailsField fieldToUpdate) {
        dataHandler.addChildrenDetails(rootId, childrenDetails);
        for (FamilyDetails childDetails : childrenDetails) {
            saveFamilyDetails(childDetails, fieldToUpdate);
        }
    }

    public static void addChildrenFamilyDetails(Player player, List<FamilyDetails> childrenDetails, FamilyDetailsField fieldToUpdate) {
        UUID rootId = player.getUniqueId();
        addChildrenFamilyDetails(rootId, childrenDetails, fieldToUpdate);
    }

    public static void addChildrenFamilyDetails(PlayerFamily playerFamily, List<FamilyDetails> childrenDetails, FamilyDetailsField fieldToUpdate) {
        UUID rootId = playerFamily.getRoot();
        addChildrenFamilyDetails(rootId, childrenDetails, fieldToUpdate);
    }
}

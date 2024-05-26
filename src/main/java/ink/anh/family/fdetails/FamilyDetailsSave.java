package ink.anh.family.fdetails;

import ink.anh.family.AnhyFamily;
import ink.anh.family.db.fdetails.AbstractFamilyDetailsTable;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.api.database.TableField;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

public class FamilyDetailsSave {

    private static AbstractFamilyDetailsTable familyDetailsTable = (AbstractFamilyDetailsTable) AnhyFamily.getInstance().getGlobalManager().getDatabaseManager().getTable(FamilyDetails.class);
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
        
        switch (fieldToUpdate) {
            case HOME_LOCATION:
                familyDetailsTable.updateField(new TableField<>(familyId, "home_location", FDetailsSerializator.serializeLocation(familyDetails.getHomeLocation())));
                break;
            case FAMILY_CHEST:
                familyDetailsTable.updateField(new TableField<>(familyId, "family_chest", FDetailsSerializator.serializeFamilyChest(familyDetails.getFamilyChest())));
                break;
            case CHILDREN_ACCESS_HOME:
                familyDetailsTable.updateField(new TableField<>(familyId, "children_access_home", Boolean.toString(familyDetails.isChildrenAccessHome())));
                break;
            case CHILDREN_ACCESS_CHEST:
                familyDetailsTable.updateField(new TableField<>(familyId, "children_access_chest", Boolean.toString(familyDetails.isChildrenAccessChest())));
                break;
            case ANCESTORS_ACCESS_HOME:
                familyDetailsTable.updateField(new TableField<>(familyId, "ancestors_access_home", Boolean.toString(familyDetails.isAncestorsAccessHome())));
                break;
            case ANCESTORS_ACCESS_CHEST:
                familyDetailsTable.updateField(new TableField<>(familyId, "ancestors_access_chest", Boolean.toString(familyDetails.isAncestorsAccessChest())));
                break;
            case SPECIFIC_ACCESS_MAP:
                familyDetailsTable.updateField(new TableField<>(familyId, "specific_access_map", FDetailsSerializator.serializeSpecificAccessMap(familyDetails.getSpecificAccessMap())));
                break;
            case HOME_SET_DATE:
                familyDetailsTable.updateField(new TableField<>(familyId, "home_set_date", familyDetails.getHomeSetDate().toString()));
                break;
            default:
                // Якщо поле не існує в `enum`, оновлюємо весь об'єкт
                familyDetailsTable.update(familyDetails);
                break;
        }
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

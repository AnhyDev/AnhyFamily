package ink.anh.family.db.fplayer;

import java.util.UUID;

import ink.anh.family.AnhyFamily;
import ink.anh.family.db.AbstractTable;
import ink.anh.family.fplayer.PlayerFamily;

public abstract class AbstractFamilyTable extends AbstractTable<PlayerFamily> {

    public AbstractFamilyTable(AnhyFamily familyPlugin) {
        super(familyPlugin, "PlayerFamily");
        initialize();
    }

    public abstract void deleteFamily(UUID playerUUID);

    public abstract PlayerFamily getFamily(UUID playerUUID);

    public abstract PlayerFamily getFamilyByDisplayName(String displayName);

    public PlayerFamily getEntity(UUID uuid, String displayName) {
        PlayerFamily entity = null;

        if (uuid != null) {
            entity = getFamily(uuid);
        }

        if (entity == null && displayName != null) {
            entity = getFamilyByDisplayName(displayName);
        }

        return entity;
    }
    
    public PlayerFamily getFamily(UUID playerUUID, String displayName) {

        PlayerFamily playerFamily = null;
        
        if (playerUUID != null) {
        	playerFamily = getFamily(playerUUID);
        }
        
        if (playerFamily == null && displayName != null) {
        	playerFamily = getFamilyByDisplayName(displayName);
        }
        
        if (playerFamily != null) {
        	if (!playerFamily.getRoot().equals(playerUUID)) {
        		playerFamily.setRoot(playerUUID);
        	}
        }
        
        return playerFamily;
    }

    @Override
    public void update(PlayerFamily entity) {
        insert(entity);
    }

    @Override
    public void delete(PlayerFamily entity) {
        deleteFamily(entity.getRoot());
    }
}
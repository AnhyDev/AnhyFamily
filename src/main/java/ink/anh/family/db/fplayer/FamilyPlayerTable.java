package ink.anh.family.db.fplayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import ink.anh.api.database.AbstractTable;
import ink.anh.api.database.ErrorLogger;
import ink.anh.api.database.TableField;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.gender.Gender;

public abstract class FamilyPlayerTable extends AbstractTable<PlayerFamily> {

    protected AnhyFamily familyPlugin;
    protected static final String tableCreate = FamilyPlayerField.getTableCreate();
    protected static final String tableInsert = FamilyPlayerField.getTableInsert();

    public FamilyPlayerTable(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance(), "PlayerFamily");
        this.familyPlugin = familyPlugin;
        initialize();
    }

    protected void initialize() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + dbName + tableCreate;
        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(createTableSQL)) {
                ps.executeUpdate();
            }
        }, "Failed to create player family table");

        String createIndexSQL = getCreateIndexSQL();
        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(createIndexSQL)) {
                ps.executeUpdate();
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate key name") && !e.getMessage().contains("already exists")) {
                    throw e; // Кидаємо виняток, щоб ErrorLogger міг обробити помилку
                }
            }
        }, "Failed to create index on player family table");
    }

    protected abstract String getCreateIndexSQL();

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
        if (playerFamily != null && !playerFamily.getRoot().equals(playerUUID)) {
            playerFamily.setRoot(playerUUID);
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

    public void deleteFamily(UUID playerUUID) {
        String deleteSQL = "DELETE FROM " + dbName + " WHERE player_uuid = ?;";
        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                ps.setString(1, playerUUID.toString());
                ps.executeUpdate();
            }
        }, "Failed to delete player family data for UUID: " + playerUUID);
    }

    public <K> void updateField(TableField<K> tableField) {
        String fieldName = tableField.getFieldName();
        if (!FamilyPlayerField.contains(fieldName)) {
            throw new IllegalArgumentException("Invalid field name");
        }

        String updateSQL = "UPDATE " + dbName + " SET " + fieldName + " = ? WHERE player_uuid = ?;";
        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(updateSQL)) {
                ps.setString(1, tableField.getFieldValue());
                ps.setString(2, tableField.getKey().toString());
                ps.executeUpdate();
            }
        }, "Failed to update player family field: " + tableField.getFieldValue());
    }

    protected void insertPlayerFamily(PlayerFamily playerFamily, String insertSQL) {
        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setString(1, playerFamily.getRoot().toString());
                ps.setString(2, Gender.toStringSafe(playerFamily.getGender()));
                ps.setString(3, playerFamily.getLoverCaseName().toLowerCase());
                ps.setString(4, joinOrReturnNull(playerFamily.getLastName()));
                ps.setString(5, joinOrReturnNull(playerFamily.getOldLastName()));
                ps.setString(6, playerFamily.getFather() != null ? playerFamily.getFather().toString() : null);
                ps.setString(7, playerFamily.getMother() != null ? playerFamily.getMother().toString() : null);
                ps.setString(8, playerFamily.getSpouse() != null ? playerFamily.getSpouse().toString() : null);
                ps.setString(9, PlayerFamily.uuidSetToString(playerFamily.getChildren()));
                ps.setString(10, playerFamily.getFamilyId() != null ? playerFamily.getFamilyId().toString() : null);
                ps.setString(11, playerFamily.getDynastyId() != null ? playerFamily.getDynastyId().toString() : null);
                ps.executeUpdate();
            }
        }, "Failed to insert or replace player family data: " + playerFamily);
    }

    protected PlayerFamily getPlayerFamilyFromResultSet(ResultSet rs) throws Exception {
        return new PlayerFamily(
                UUID.fromString(rs.getString("player_uuid")),
                Gender.fromString(rs.getString("gender")),
                rs.getString("displayName"),
                splitStringAndNullify(rs.getString("last_name"), ","),
                splitStringAndNullify(rs.getString("old_last_name"), ","),
                rs.getString("father") != null ? UUID.fromString(rs.getString("father")) : null,
                rs.getString("mother") != null ? UUID.fromString(rs.getString("mother")) : null,
                rs.getString("spouse") != null ? UUID.fromString(rs.getString("spouse")) : null,
                PlayerFamily.stringToUuidSet(rs.getString("children")),
                rs.getString("family_id") != null ? UUID.fromString(rs.getString("family_id")) : null,
                rs.getString("dynasty_id") != null ? UUID.fromString(rs.getString("dynasty_id")) : null
        );
    }

    protected PlayerFamily fetchPlayerFamily(String sql, String param) {
        final PlayerFamily[] playerFamily = {null};

        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, param);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        playerFamily[0] = getPlayerFamilyFromResultSet(rs);
                    }
                } catch (Exception e) {
                    ErrorLogger.log(familyPlugin, e, "Failed to create index on family tableInsert");
				}
            }
        }, "Failed to get player family data for parameter: " + param);

        return playerFamily[0];
    }
}

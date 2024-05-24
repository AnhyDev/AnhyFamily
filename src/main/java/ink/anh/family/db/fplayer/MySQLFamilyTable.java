package ink.anh.family.db.fplayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ink.anh.family.db.ErrorLogger;
import ink.anh.family.db.MySQLDatabaseManager;
import ink.anh.family.db.TableField;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.gender.Gender;

public class MySQLFamilyTable extends AbstractFamilyTable {

    private final MySQLDatabaseManager dbManager;
    private final String tablePrefix;
    private static final Map<String, String> allowedFields = new HashMap<>();

    static {
        allowedFields.put("displayName", "displayName");
        allowedFields.put("last_name", "last_name");
        allowedFields.put("old_last_name", "old_last_name");
        allowedFields.put("father", "father");
        allowedFields.put("mother", "mother");
        allowedFields.put("spouse", "spouse");
        allowedFields.put("children", "children");
        allowedFields.put("family_id", "family_id");
        allowedFields.put("parent_family_id", "parent_family_id");
        allowedFields.put("child_family_ids", "child_family_ids");
        allowedFields.put("dynasty_id", "dynasty_id");
    }

    public MySQLFamilyTable(MySQLDatabaseManager dbManager) {
        super(dbManager.plugin);
        this.dbManager = dbManager;
        this.tablePrefix = dbManager.getTablePrefix();
    }

    @Override
    protected void initialize() {
        String createTableSQL =
                "CREATE TABLE IF NOT EXISTS " + tablePrefix + dbName + " (" +
                        "player_uuid VARCHAR(36) PRIMARY KEY," +
                        "gender VARCHAR(255)," +
                        "displayName VARCHAR(255) NOT NULL UNIQUE," +
                        "last_name TEXT," +
                        "old_last_name TEXT," +
                        "father VARCHAR(36)," +
                        "mother VARCHAR(36)," +
                        "spouse VARCHAR(36)," +
                        "children TEXT," +
                        "family_id VARCHAR(36)," +
                        "parent_family_id VARCHAR(36)," +
                        "child_family_ids TEXT," +
                        "dynasty_id VARCHAR(36)" +
                        ");" +
                        "CREATE INDEX IF NOT EXISTS idx_displayName ON " + tablePrefix + dbName + " (displayName);";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(createTableSQL)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to create family table");
        }
    }

    @Override
    public void insert(PlayerFamily playerFamily) {
        String insertSQL =
                "INSERT INTO " + tablePrefix + dbName + 
                " (player_uuid, gender, displayName, last_name, old_last_name, father, mother, spouse, children, family_id, parent_family_id, child_family_ids, dynasty_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE gender = VALUES(gender), displayName = VALUES(displayName), last_name = VALUES(last_name), old_last_name = VALUES(old_last_name), father = VALUES(father), mother = VALUES(mother), spouse = VALUES(spouse), children = VALUES(children), family_id = VALUES(family_id), parent_family_id = VALUES(parent_family_id), child_family_ids = VALUES(child_family_ids), dynasty_id = VALUES(dynasty_id);";

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false); // Початок транзакції

            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                // Заповнення параметрів запиту
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
                ps.setString(11, playerFamily.getParentFamilyId() != null ? playerFamily.getParentFamilyId().toString() : null);
                ps.setString(12, PlayerFamily.uuidSetToString(playerFamily.getChildFamilyIds()));
                ps.setString(13, playerFamily.getDynastyId() != null ? playerFamily.getDynastyId().toString() : null);

                ps.executeUpdate();
            }

            conn.commit(); // Завершення транзакції
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to insert or replace family data");
            try (Connection conn = dbManager.getConnection()) {
                conn.rollback(); // Відкат транзакції у випадку помилки
            } catch (SQLException rollbackEx) {
                ErrorLogger.log(dbManager.plugin, rollbackEx, "Failed to rollback transaction");
            }
        } finally {
            try (Connection conn = dbManager.getConnection()) {
                conn.setAutoCommit(true); // Відновлення автоматичного режиму комітів
            } catch (SQLException finalEx) {
                ErrorLogger.log(dbManager.plugin, finalEx, "Failed to reset auto-commit mode");
            }
        }
    }

    @Override
    public PlayerFamily getFamily(UUID playerUUID) {
        String selectSQL = "SELECT * FROM " + tablePrefix + dbName + " WHERE player_uuid = ?;";
        return getPlayerFamily(selectSQL, playerUUID.toString());
    }

    @Override
    public PlayerFamily getFamilyByDisplayName(String displayName) {
        String selectSQL = "SELECT * FROM " + tablePrefix + dbName + " WHERE displayName = ?;";
        return getPlayerFamily(selectSQL, displayName.toLowerCase());
    }

    private PlayerFamily getPlayerFamily(String sql, String param) {
        PlayerFamily playerFamily = null;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    playerFamily = new PlayerFamily(
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
                            rs.getString("parent_family_id") != null ? UUID.fromString(rs.getString("parent_family_id")) : null,
                            PlayerFamily.stringToUuidSet(rs.getString("child_family_ids")),
                            rs.getString("dynasty_id") != null ? UUID.fromString(rs.getString("dynasty_id")) : null
                    );
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to get family data");
        }
        return playerFamily;
    }

    @Override
    public void deleteFamily(UUID playerUUID) {
        String deleteSQL = "DELETE FROM " + tablePrefix + dbName + " WHERE player_uuid = ?;";
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false); // Початок транзакції

            try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                ps.setString(1, playerUUID.toString());
                ps.executeUpdate();
            }

            conn.commit(); // Завершення транзакції
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to delete family data");
            try (Connection conn = dbManager.getConnection()) {
                conn.rollback(); // Відкат транзакції у випадку помилки
            } catch (SQLException rollbackEx) {
                ErrorLogger.log(dbManager.plugin, rollbackEx, "Failed to rollback transaction");
            }
        } finally {
            try (Connection conn = dbManager.getConnection()) {
                conn.setAutoCommit(true); // Відновлення автоматичного режиму комітів
            } catch (SQLException finalEx) {
                ErrorLogger.log(dbManager.plugin, finalEx, "Failed to reset auto-commit mode");
            }
        }
    }

    @Override
    public <K> void updateField(TableField<K> tableField) {
        if (!allowedFields.containsKey(tableField.getFieldName())) {
            throw new IllegalArgumentException("Invalid field name");
        }

        String updateSQL = "UPDATE " + tablePrefix + dbName + " SET " + allowedFields.get(tableField.getFieldName()) + " = ? WHERE player_uuid = ?;";
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false); // Початок транзакції

            try (PreparedStatement ps = conn.prepareStatement(updateSQL)) {
                ps.setString(1, tableField.getFieldValue());
                ps.setString(2, tableField.getKey().toString());
                ps.executeUpdate();
            }

            conn.commit(); // Завершення транзакції
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to update family field: " + tableField.getFieldValue());
            try (Connection conn = dbManager.getConnection()) {
                conn.rollback(); // Відкат транзакції у випадку помилки
            } catch (SQLException rollbackEx) {
                ErrorLogger.log(dbManager.plugin, rollbackEx, "Failed to rollback transaction");
            }
        } finally {
            try (Connection conn = dbManager.getConnection()) {
                conn.setAutoCommit(true); // Відновлення автоматичного режиму комітів
            } catch (SQLException finalEx) {
                ErrorLogger.log(dbManager.plugin, finalEx, "Failed to reset auto-commit mode");
            }
        }
    }
}

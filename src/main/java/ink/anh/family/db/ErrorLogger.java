package ink.anh.family.db;

import java.util.logging.Level;

import ink.anh.family.AnhyFamily;

public class ErrorLogger {

    public static void log(AnhyFamily plugin, Exception ex, String message) {
        plugin.getLogger().log(Level.SEVERE, message, ex);
    }
}

package org.moss.discord.util;

import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import org.javacord.api.DiscordApi;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseUtil {

    DiscordApi api;

    public DatabaseUtil(DiscordApi discordApi) {
        this.api = discordApi;
    }

    public void initSqlite(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Database db = PooledDatabaseOptions.builder().options(DatabaseOptions.builder().sqlite(file.getPath()).logger(Logger.getLogger("com.zaxxer.hikari")).build()).createHikariDatabase();
        DB.setGlobalDatabase(db);
        firstRun();
    }

    private void firstRun() {
        try {
            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY, `snowflake` varchar(36) NOT NULL UNIQUE, `spigot_id` int(8) NOT NULL DEFAULT 0)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

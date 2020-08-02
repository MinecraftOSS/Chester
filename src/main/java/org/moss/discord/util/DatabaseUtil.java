package org.moss.discord.util;

import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseUtil {

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
            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `servers` (`server_id` INTEGER PRIMARY KEY, `server_snow` VARCHAR(36) NOT NULL UNIQUE, `server_settings` VARCHAR, `joined` TIMESTAMP)");
            //serverid, name, content, owner, modified
            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `factoids` (`id` INTEGER PRIMARY KEY, `name` VARCHAR, `content` VARCHAR, `owner` VARCHAR(36), `modified` VARCHAR, `server_id` INT(16), FOREIGN KEY (server_id) REFERENCES servers(server_id))");
            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `user_factoids` (`id` INTEGER PRIMARY KEY, `name` VARCHAR, `content` VARCHAR, `owner` VARCHAR(36), `modified` VARCHAR, `server_id` INT(16), FOREIGN KEY (server_id) REFERENCES servers(server_id))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

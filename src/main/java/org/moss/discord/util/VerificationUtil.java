package org.moss.discord.util;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.javacord.api.entity.user.User;

import java.sql.SQLException;
import java.util.Objects;

public class VerificationUtil {

    private ObjectMapper mapper = new ObjectMapper();
    private OkHttpClient client = new OkHttpClient.Builder().build();
    private static final String USER_ENDPOINT = "https://api.spigotmc.org/simple/0.1/index.php?action=getAuthor&id=";

    public boolean verify(User user, int spigotID) {
        String userDiscord = getUserDiscord(spigotID);
        if (userDiscord != null && user.getDiscriminatedName().equalsIgnoreCase(userDiscord)) {
            setSpigotID(user.getIdAsString(), spigotID);
            return true;
        }
        return false;
    }

    /*
    Force links a user to their spigot id in-case of broken API
    This assumes that the staff has verified the user themselves
     */
    public boolean verify(User user, int spigotID, boolean force) {
        if (!isVerified(user.getIdAsString()) && getSnowflakeFromID(spigotID) == 0) {
            setSpigotID(user.getIdAsString(), spigotID);
            return true;
        }
        return false;
    }

    public boolean isVerified(String snowflake) {
        int spigotID = getSpigotID(snowflake);
        return  (spigotID != 0);
    }

    public int getSpigotID(String snowflake) {
        try {
            DbRow dbRow = DB.getFirstRow("SELECT spigot_id FROM users WHERE snowflake = ?", snowflake);
            return (dbRow == null || dbRow.isEmpty()) ? 0 : dbRow.get("spigot_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSnowflakeFromID(int spigotID) {
        try {
            DbRow dbRow = DB.getFirstRow("SELECT snowflake FROM users WHERE spigot_id = ?", spigotID);
            return (dbRow == null || dbRow.isEmpty()) ? 0 : dbRow.get("snowflake");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setSpigotID(String snowflake, int spigotID) {
        try {
            DB.executeInsert("INSERT INTO users (snowflake, spigot_id) VALUES (?, ?)", snowflake, spigotID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getUserDiscord(int spigotID) {
        try {
            JsonNode jsonNode = makeRequest(USER_ENDPOINT+spigotID);
            //System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
            if (jsonNode.get("identities").hasNonNull("discord")) {
                return jsonNode.get("identities").get("discord").asText();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private JsonNode makeRequest(String url) {
        try {
            Request request = new Request.Builder()
                    .cacheControl(new CacheControl.Builder().noCache().build())
                    .url(url)
                    .build();
            return mapper.readTree(Objects.requireNonNull(client.newCall(request).execute().body()).string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
package org.moss.discord.storage;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import org.javacord.api.entity.server.Server;
import org.moss.chesterapi.Factoids.Factoid;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserFactoidStorage  {

    private Map<Server, HashMap<String, Factoid>> tagMap = new HashMap<>();

    public HashMap<String, Factoid> getFactoidsFromServer(Server server) {
        return tagMap.computeIfAbsent(server, k -> getFactoidsFromDB(server));
    }

    private HashMap<String, Factoid> getFactoidsFromDB(Server server) {
        HashMap<String, Factoid> tagMap = new HashMap<>();
        try {
            List<DbRow> results = DB.getResults("select name, content, owner, modified from user_factoids where server_id in (select server_id from servers where server_snow = ?)", server.getIdAsString());
            results.forEach(dbRow -> {
                Factoid toid = new Factoid()
                        .setName(dbRow.getString("name"))
                        .setContent(dbRow.getString("content"))
                        .setOwner(dbRow.getString("owner"))
                        .setModified(dbRow.getString("modified"));
                tagMap.put(toid.getName(), toid);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tagMap;
    }

    public void setFactoid(Server server, Factoid factoid) {
        try {
            getFactoidsFromServer(server).put(factoid.getName(), factoid);
            DB.executeInsert("insert or replace into user_factoids (name, content, owner, modified, server_id) values(?, ?, ?, ?,(select server_id from servers where server_snow = ?))", factoid.getName(), factoid.getContent(), factoid.getOwner(), factoid.getModified(), server.getIdAsString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteFactoid(Server server, Factoid factoid) {
        return deleteFactoid(server, factoid.getName());
    }

    public boolean deleteFactoid(Server server, String tag) {
        try {
            if (getFactoid(server, tag) != null) {
                getFactoidsFromServer(server).remove(tag);
                DB.executeUpdate("delete from user_factoids where name = ? and server_id = (select server_id from servers where server_snow = ?)", tag, server.getIdAsString());
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Factoid getFactoid(Server server, String key) {
        return getFactoidsFromServer(server).getOrDefault(key.toLowerCase(), null);
    }


}
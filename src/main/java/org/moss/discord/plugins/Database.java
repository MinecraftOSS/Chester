package org.moss.discord.plugins;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import de.btobastian.sdcf4j.Command;
import org.javacord.api.entity.channel.TextChannel;
import org.moss.discord.Chester;

import java.sql.SQLException;
import java.util.List;

public class Database extends Chester {

    public Database() {
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!database"}, usage = "!database <query>", description = "Executes DB query", requiredPermissions = "chester.admin")
    public void onCommand(TextChannel channel, String[] args) {
        try {
            List<DbRow> results = DB.getResults(String.join(" ", args));
            StringBuilder builder = new StringBuilder();
            results.forEach(dbRow -> builder.append(dbRow.toString()+"\n"));
            channel.sendMessage(String.format("```%s```", builder.toString()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
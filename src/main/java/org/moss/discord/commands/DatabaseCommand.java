package org.moss.discord.commands;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;

import java.sql.SQLException;
import java.util.List;

public class DatabaseCommand implements CommandExecutor {

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
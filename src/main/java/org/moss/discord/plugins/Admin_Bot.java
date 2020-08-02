package org.moss.discord.plugins;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import de.btobastian.sdcf4j.Command;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.server.ServerJoinListener;
import org.moss.chesterapi.ChesterPlugin;
import org.moss.discord.Chester;

import java.awt.*;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Admin_Bot extends Chester implements ChesterPlugin, ServerJoinListener {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
        getDiscordApi().addListener(this);
    }

    @Command(aliases = {"!database"}, usage = "!database <query>", description = "Executes DB query")
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

    @Command(aliases = {"!setavatar"}, usage = "!setavatar <img>", description = "Sets the bot's avatar", requiredPermissions = "chester.admin")
    public void onSetAvatar(DiscordApi api, String[] args, TextChannel channel, Message message) {
        if (args.length >= 1) {
            try {
                URL url = new URL(args[0]); //pray to god its a URL
                message.delete();
                api.createAccountUpdater().setAvatar(url).update();
                channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle("Avatar set!").setImage(args[0]));
            } catch (Exception e) {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Unable to set avatar").setDescription("`!setavatar <url>`"));
            }
        }
    }

    @Command(aliases = {"!presence"}, usage = "!presence <Sstatus>", description = "Sets the status presence of the bot", requiredPermissions = "chester.admin")
    public void onCommand(DiscordApi api, String[] args, TextChannel channel) {
        if (args.length >= 2) {
            String type = args[0].toUpperCase();
            String url = args[args.length-1];
            LinkedList<String> status = new LinkedList(Arrays.asList(args));
            status.removeFirst();
            try {
                if (type.equalsIgnoreCase("streaming")) {
                    status.removeLast();
                    api.updateActivity(String.join(" ", status), url);
                } else {
                    api.updateActivity(ActivityType.valueOf(type), String.join(" ", status));
                }
            } catch (Exception e) {
                channel.sendMessage("Invalid activity use: " + Arrays.toString(ActivityType.values()));
            }
        }
    }

    @Override
    public void onServerJoin(ServerJoinEvent serverJoinEvent) {
        try {
            DB.executeInsert("INSERT OR IGNORE into servers (server_snow, joined) VALUES (?, ?)", serverJoinEvent.getServer().getIdAsString(), Timestamp.from(Instant.now()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

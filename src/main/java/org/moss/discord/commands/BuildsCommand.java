package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;
import org.moss.discord.util.BStatsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


public class BuildsCommand implements CommandExecutor {

    /**
     * The logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(BuildsCommand.class);


    private String stableBuild = "https://ci.ender.zone/job/%s/lastBuild/api/json";

    private Map<String, String> shortcuts = new HashMap<>();

    public BuildsCommand() {
        shortcuts.put("essx", "EssentialsX"); //TODO: Make configurable
        shortcuts.put("ess", "EssentialsX");
        shortcuts.put("essentialsx", "EssentialsX");
        shortcuts.put("essentials", "EssentialsX");
        shortcuts.put("factions", "FactionsUUID");
        shortcuts.put("fuuid", "FactionsUUID");
        shortcuts.put("factionsuuid", "FactionsUUID");
        shortcuts.put("playervaults", "PlayerVaultsX");
        shortcuts.put("pvx", "PlayerVaultsX");
    }

    @Command(aliases = {"!builds"}, usage = "!builds <pluginName>", description = "Get latest information about the build for this plugin")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {
        if (args.length == 1) {
            // Lets use shortcuts and if they do not work, fall back to string provided
            String provided = args[0];
            if (shortcuts.containsKey(provided)) {
                provided = shortcuts.get(provided);
            }

            EmbedBuilder embed = new EmbedBuilder();
            try {
                JsonNode build = new BStatsUtil(api).makeRequest(String.format(stableBuild, provided));

                embed.setTitle(build.get("fullDisplayName").asText());
                embed.setUrl(build.get("url").asText());
                embed.setColor(build.get("result").asText().equals("FAILURE") ? Color.RED : Color.GREEN);
                embed.addField("Commit", "```" + build.get("changeSet").get("items").get(0).get("msg").asText() + "```");

                embed.setTimestamp(Instant.ofEpochMilli(build.get("timestamp").asLong()));
            } catch (IOException e) {
                e.printStackTrace();
                embed.setTitle("Unknown plugin!").setColor(Color.RED);
            }
            channel.sendMessage(embed);

        }

        // No plugin name provided. Send help message.
        if (args.length == 0) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setDescription("**Usage**: !builds <pluginName>")
                    .setColor(Color.RED);
            channel.sendMessage(embed)
                    .exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
        }
    }


}

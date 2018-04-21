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

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GithubCommand implements CommandExecutor {

    Map<String, String> shortcuts = new HashMap<>();

    String repos = "https://api.github.com/repos/%s";

    public GithubCommand() {
        shortcuts.put("essx", "EssentialsX/Essentials");
        shortcuts.put("ess", "EssentialsX/Essentials");
        shortcuts.put("essentialsx", "EssentialsX/Essentials");
        shortcuts.put("essentials", "EssentialsX/Essentials");
        shortcuts.put("paper", "PaperMC/Paper");
        shortcuts.put("papermc", "PaperMC/Paper");
    }

    @Command(aliases = {"!github", "!gh"}, usage = "!github <username|repo>", description = "Shows some stats about the given plugin.")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {

        if (args.length >= 1) { //TODO: Fancier embed
            if (shortcuts.containsKey(args[0])) {
                channel.sendMessage(makeEmbed(api, shortcuts.get(args[0])));
            } else {
                channel.sendMessage(makeEmbed(api, args[0]));
            }
        }

        if (args.length == 0) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setDescription("**Usage**: !github <username|repo>")
                    .setColor(Color.RED);
            channel.sendMessage(embed)
                    .exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
        }
    }

    public EmbedBuilder makeEmbed(DiscordApi api, String repository) {
        BStatsUtil bStatsUtil = new BStatsUtil(api);
        EmbedBuilder embed = new EmbedBuilder();
        try {
            JsonNode repo = bStatsUtil.makeRequest(String.format(repos, repository));

            embed.setTitle(repo.get("name").asText());
            embed.setUrl(repo.get("url").asText());
            embed.setThumbnail(repo.get("owner").get("avatar_url").asText());
            embed.setColor(Color.YELLOW);

            embed.addField("Open Issues", repo.get("open_issues_count").asText());

            embed.setTimestamp();

            return embed;
        } catch (Exception e) {
            embed.setTitle("Unknown repo!").setColor(Color.RED);
        }
        return embed;
    }

}

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
    String issuerepos = "https://api.github.com/repos/%s/issues";

    public GithubCommand() {
        shortcuts.put("essx", "EssentialsX/Essentials"); //TODO: Make configurable
        shortcuts.put("ess", "EssentialsX/Essentials");
        shortcuts.put("essentialsx", "EssentialsX/Essentials");
        shortcuts.put("essentials", "EssentialsX/Essentials");
        shortcuts.put("paper", "PaperMC/Paper");
        shortcuts.put("papermc", "PaperMC/Paper");
        shortcuts.put("factions", "drtshock/Factions");
        shortcuts.put("fuuid", "drtshock/Factions");
        shortcuts.put("factionsuuid", "drtshock/Factions");
        shortcuts.put("playervaults", "drtshock/PlayerVaults");
        shortcuts.put("pvx", "drtshock/PlayerVaults");
    }

    @Command(aliases = {"!github", "!gh"}, usage = "!github <username|repo> <issue #>", description = "Shows some stats about the given repository.")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {

        if (args.length == 1) { //TODO: Fancier embed
            if (shortcuts.containsKey(args[0])) {
                channel.sendMessage(makeInfoEmbed(api, shortcuts.get(args[0])));
            } else {
                channel.sendMessage(makeInfoEmbed(api, args[0]));
            }
        }

        if (args.length == 2) {
            if (shortcuts.containsKey(args[0])) {
                channel.sendMessage(makeIssueEmbed(api, shortcuts.get(args[0]), args[1]));
            } else {
                channel.sendMessage(makeIssueEmbed(api, args[0], args[1]));
            }
        }

        if (args.length == 0) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setDescription("**Usage**: !github <username|repo> <issue #>")
                    .setColor(Color.RED);
            channel.sendMessage(embed)
                    .exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
        }
    }

    @Command(aliases = {".supaham"}, usage = ".supaham", description = "Shows some stats about the given repository.")
    public void onSupaham(DiscordApi api, TextChannel channel, String[] args) {
        try {
            JsonNode repo = new BStatsUtil(api).makeRequest(String.format(repos, shortcuts.get("essx")));
            channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(String.format("EssentialsX has %s open issues", repo.get("open_issues_count").asText())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Command(aliases = {".md678685", ".md"}, usage = ".md", description = "Shows some stats about the given repository.")
    public void onmd678685(DiscordApi api, TextChannel channel, String[] args) {
        try {
            JsonNode repo = new BStatsUtil(api).makeRequest("https://api.github.com/search/issues?q=repo:EssentialsX/Essentials/+type:issue+state:closed");
            channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle(String.format("EssentialsX has %s closed issues", repo.get("total_count").asText())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public EmbedBuilder makeInfoEmbed(DiscordApi api, String repository) {
        BStatsUtil bStatsUtil = new BStatsUtil(api);
        EmbedBuilder embed = new EmbedBuilder();
        try {
            JsonNode repo = bStatsUtil.makeRequest(String.format(repos, repository));
            JsonNode issues = bStatsUtil.makeRequest(String.format(issuerepos, repository));

            embed.setTitle(repo.get("name").asText());
            embed.setUrl(repo.get("url").asText());
            embed.setColor(Color.YELLOW);
            embed.setDescription(repo.get("description").asText());
            embed.setThumbnail(repo.get("owner").get("avatar_url").asText());

            embed.addInlineField("\uD83C\uDF1F Stars", String.format("```%s```", repo.get("stargazers_count").asText()));
            embed.addInlineField("\u203C Issues", String.format("```%s```", repo.get("open_issues_count").asText()));

            StringBuilder issuenames = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                if (issues.has(i)) {
                    issuenames.append("[#"+issues.get(i).get("number").asText()+"]").append("("+issues.get(i).get("html_url").asText()+") ")
                            .append("```" + issues.get(i).get("title").asText() + "```");
                }
            }

            embed.addField("Current issues", issuenames.toString().isEmpty() ? "None!" : issuenames.toString());

            embed.setTimestamp();

            return embed;
        } catch (Exception e) {
            e.printStackTrace();
            embed.setTitle("Unknown repo!").setColor(Color.RED);
        }
        return embed;
    }

    public EmbedBuilder makeIssueEmbed(DiscordApi api, String repository, String issuenum) {
        BStatsUtil bStatsUtil = new BStatsUtil(api);
        EmbedBuilder embed = new EmbedBuilder();
        try {
            JsonNode issue = bStatsUtil.makeRequest(String.format(issuerepos, repository)+"/"+issuenum);

            embed.setAuthor(issue.get("user").get("login").asText());
            embed.setTitle(issue.get("title").asText());
            embed.setUrl(issue.get("html_url").asText());
            embed.setColor(issue.get("state").asText().equals("closed") ? Color.RED : Color.YELLOW);
            embed.setThumbnail(issue.get("user").get("avatar_url").asText());

            embed.addInlineField("Status", String.format("```%s```", issue.get("state").asText()));
            embed.addInlineField("Comments", String.format("```%s```", issue.get("comments").asText()));

            String body = issue.get("body").asText();
            int maxLength = (body.length() < 1020) ? body.length() : 1020;

            embed.addField("Issue", issue.get("body").asText().substring(0, maxLength) + " ...");

            embed.setTimestamp();

            return embed;
        } catch (Exception e) {
            e.printStackTrace();
            embed.setTitle("Issue not found!").setColor(Color.RED);
        }
        return embed;
    }

}

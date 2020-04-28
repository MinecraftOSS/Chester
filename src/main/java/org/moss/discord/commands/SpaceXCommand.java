package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang.WordUtils;
import org.moss.discord.util.EmbedPaged;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SpaceXCommand implements CommandExecutor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient.Builder().build();
    String[] links = {"video_link","reddit_campaign", "reddit_launch", "reddit_recovery"};
    String API_NEXT = "https://api.spacexdata.com/v3/launches/next";
    String API_FUTURE = "https://api.spacexdata.com/v3/launches/upcoming";
    String API_PAST = "https://api.spacexdata.com/v3/launches/past";

    @Command(aliases = {"!spacex", ".spacex"}, usage = "!spacex", description = "Spacex Command")
    public void spacex(TextChannel channel, String[] args, Member user) {
        if (args.length == 1) {
            switch (args[0].toUpperCase()) {
                case "FUTURE":
                    List<EmbedBuilder> futurelaunches = new ArrayList<>();
                    int i = 0;
                    for (Iterator<JsonNode> it = makeRequest(API_FUTURE).elements(); it.hasNext();) {
                        if (i >= 5) {
                            break;
                        }
                        i++;
                        futurelaunches.add(createLaunchEmbed(it.next()));
                    }
                    new EmbedPaged(channel, futurelaunches, user.getUser()).build();
                    break;
                case "PAST":
                    List<EmbedBuilder> pastLaunches = new ArrayList<>();
                    JsonNode pLaunches = makeRequest(API_PAST);
                    int ii = 1;
                    for (int ils = 0; ils <= 4; ils++) {
                        JsonNode leNode = pLaunches.get(pLaunches.size()-ii);
                        ii++;
                        pastLaunches.add(createLaunchEmbed(leNode));
                    }
                    new EmbedPaged(channel, pastLaunches, user.getUser()).setPagedButtons(true).build();
                    break;
                default:
                    channel.sendMessage(new MessageBuilder().setContent(user.getAsMention()).setEmbed(createLaunchEmbed(makeRequest(API_NEXT)).build()).build()).queue();
                    break;
            }
        } else {
            channel.sendMessage(new MessageBuilder().setContent(user.getAsMention()).setEmbed(createLaunchEmbed(makeRequest(API_NEXT)).build()).build()).queue();
        }
    }

    public EmbedBuilder createLaunchEmbed(JsonNode node) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(String.format("Launch #%d - %s", node.get("flight_number").asInt(), node.get("mission_name").asText()));
        builder.setThumbnail("https://i.imgur.com/mN9KsTW.png");
        builder.setDescription(node.get("details").asText().equalsIgnoreCase("null") ? node.get("mission_name").asText() : node.get("details").asText());
        builder.addField("Rocket Type", node.get("rocket").get("rocket_name").asText(), true);
        builder.addField("Launching in", String.format("%s\n%s", getTimeLeft(node.get("launch_date_unix").asLong()), node.get("launch_date_utc").asText().split("\\.")[0].replace("T", " ")+ " UTC"), true);
        builder.addField("Launch Site", node.get("launch_site").get("site_name_long").asText(), false);
        builder.setColor(Color.decode("#005288"));
        StringBuilder linkText = new StringBuilder();
        for (String s : links) {
            if (!node.get("links").get(s).asText().equalsIgnoreCase("null")) {
                linkText.append(String.format("[%s](%s)", WordUtils.capitalize(s.replace("_", " ")), node.get("links").get(s).asText())).append("\n");
            }
        }
        if (!linkText.toString().equalsIgnoreCase("")) {
            builder.addField("Links", linkText.toString(), false);
        }
        return builder;
    }

    public String getTimeLeft(long timestamp) {
        long delta = timestamp - System.currentTimeMillis()/1000;
        return String.format("%d days %d hours %d minutes", delta /86400, delta % 86400 / 3600 % 24, delta % 3600 / 60);
    }

    public JsonNode makeRequest(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            return mapper.readTree(Objects.requireNonNull(client.newCall(request).execute().body()).string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

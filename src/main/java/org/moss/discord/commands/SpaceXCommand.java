package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang.WordUtils;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class SpaceXCommand implements CommandExecutor {

    private ObjectMapper mapper = new ObjectMapper();
    private OkHttpClient client = new OkHttpClient.Builder().build();
    String[] links = {"video_link","reddit_campaign", "reddit_launch", "reddit_recovery"};

    @Command(aliases = {"!spacex", ".spacex"}, usage = "!spacex", description = "Spacex Command")
    public void spacex(TextChannel channel, String[] args, User user) {
        try {
            JsonNode node = makeRequest("https://api.spacexdata.com/v3/launches/next");
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(String.format("Launch #%d - %s", node.get("flight_number").asInt(), node.get("mission_name").asText()));
            builder.setThumbnail("https://i.imgur.com/mN9KsTW.png");
            builder.setDescription(node.get("details").asText());
            builder.addInlineField("Rocket Type", node.get("rocket").get("rocket_name").asText());
            builder.addInlineField("Launching in", String.format("%s\n%s", getTimeLeft(node.get("launch_date_unix").asLong()), node.get("launch_date_utc").asText().split("\\.")[0].replace("T", " ")+ " UTC"));
            builder.addField("Launch Site", node.get("launch_site").get("site_name").asText());
            builder.setColor(Color.decode("#005288"));
            String linkText = "";
            for (String s : links) {
                if (!node.get("links").get(s).asText().equalsIgnoreCase("null")) {
                    linkText += (String.format("[%s](%s)", WordUtils.capitalize(s.replace("_", " ")), node.get("links").get(s).asText()))+"\n";
                }
            }
            builder.addField("Links", linkText);
            channel.sendMessage(user.getMentionTag(), builder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTimeLeft(long timestamp) {
        long delta = timestamp - System.currentTimeMillis()/1000;
        return String.format("%d days %d hours %d minutes", delta /86400, delta % 86400 / 3600 % 24, delta % 3600 / 60);
    }

    public JsonNode makeRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return mapper.readTree(Objects.requireNonNull(client.newCall(request).execute().body()).string());
    }
}

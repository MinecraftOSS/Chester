package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.Objects;

public class XkcdCommand implements CommandExecutor {

    private String searchURL = "https://relevantxkcd.appspot.com/process?action=xkcd&query=%s";
    private ObjectMapper mapper = new ObjectMapper();
    private OkHttpClient client = new OkHttpClient.Builder().build();

    @Command(aliases = {"!xkcd", "!.xkcd"}, usage = "!xkcd <Query>", description = "Search xkcd")
    public void onCommand(DiscordApi api, User user, TextChannel channel, String[] args) {
        if (args.length >= 1) {
            String id = search(String.join(" ", args));
            if (id != null) {
                JsonNode node = xkcd_info(id);
                EmbedBuilder embed = new EmbedBuilder().setColor(Color.YELLOW);
                embed.setTitle("xkcd: " + node.get("safe_title").asText());
                embed.setUrl(String.format("https://xkcd.com/%s/", id));
                embed.setImage(node.get("img").asText());
                embed.setFooter(node.get("alt").asText());
                channel.sendMessage(user.getMentionTag(), embed);
            }
        }
    }

    private String search(String query) {
        try {
            Request request = new Request.Builder().url(String.format(searchURL, query)).build();
            String response = client.newCall(request).execute().body().string().split(" ")[2].replaceAll("\n","");//.replaceAll("\\D+"," ")
            return StringUtils.isNumeric(response) ? response : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private JsonNode xkcd_info(String comicNum)  {
        try {
            Request request = new Request.Builder()
                    .url(String.format("https://xkcd.com/%s/info.0.json", comicNum))
                    .build();
            return mapper.readTree(Objects.requireNonNull(client.newCall(request).execute().body()).string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
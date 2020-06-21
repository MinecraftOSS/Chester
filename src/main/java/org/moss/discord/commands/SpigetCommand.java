package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.moss.discord.util.BStatsUtil;

import java.awt.*;
import java.text.MessageFormat;
import java.util.Iterator;

public class SpigetCommand implements CommandExecutor {

    String queryurl = "https://api.spiget.org/v2/search/resources/{0}?field=name&size=5&page={1}&fields=id,name,tag,rating";

    @Command(aliases = {"!spiget", "!plsearch"}, usage = "!spiget <Query>", description = "Search spigots resources")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {
        String query = String.join(" ", args);
        String lastIndex = query.substring(query.lastIndexOf(" ")+1);
        int page = 1;
        if (StringUtils.isNumeric(lastIndex) && !lastIndex.equals("1")) {
            page = Integer.valueOf(lastIndex);
            query = String.join(" ", (String[]) ArrayUtils.remove(args, args.length-1));
        }
        BStatsUtil bStatsUtil = new BStatsUtil(api);
        EmbedBuilder embed = new EmbedBuilder();
        if (args.length >= 1) {
            try {
                JsonNode search = bStatsUtil.makeRequest(MessageFormat.format(queryurl, query, page));
                StringBuilder result = new StringBuilder();
                for (Iterator<JsonNode> i = search.elements(); i.hasNext();) {
                    JsonNode resource = i.next();
                    DecimalFormat df = new DecimalFormat("#.##");
                    double input = resource.get("rating").get("average").asDouble();
                    String message = df.format(input);
                    String name = String.format("[%s](https://www.spigotmc.org/resources/%s/) | %s \u2605", resource.get("name").asText(), resource.get("id").asText(), message);
                    String tag = String.format("```%s```", resource.get("tag").asText());
                    result.append(name).append(tag).append("\n");
                }

                embed.setAuthor("Spiget Search");
                embed.setColor(Color.GREEN);
                embed.addField("Results", result.toString());

                embed.setTimestampToNow();

            } catch (Exception e) {
                e.printStackTrace();
                embed.setTitle("No resources found!").setColor(Color.RED);
            }
        }
        channel.sendMessage(embed);
    }
}

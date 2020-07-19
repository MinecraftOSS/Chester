package org.moss.discord.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.moss.discord.Chester;
import org.moss.discord.ChesterPlugin;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BukkitJavadoc extends Chester implements ChesterPlugin {

    private Map<String, String> types = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();
    private String out = "https://hub.spigotmc.org/javadocs/spigot/";


    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
        try {
            JsonNode classes = mapper.readTree(new File("./bukjd.json")).get(0);
            for (Iterator<String> it = classes.fieldNames(); it.hasNext(); ) {
                String s = it.next();
                types.put(s, classes.get(s).asText());
                //System.out.println(s + " | " + String.format("%s%s%s.html", out,classes.get(s).asText(),s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Command(aliases = {"!bukjd"}, usage = "!bukjd <param>", description = "Bukkit javadoc search")
    public void onCommand(TextChannel channel, String[] args, User user) {
        if (args.length >= 1) {
            List<String> result = search(args[0].toLowerCase());
            if (result.size() != 0) {
                String field = result.stream().map(s -> String.format("[%s](%s)",s,String.format("%s%s%s.html", out,types.get(s),s))).collect(Collectors.joining("\n"));
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("Results", field);
                channel.sendMessage(user.getMentionTag(),embed.setColor(Color.GREEN)).exceptionally(e -> {
                    channel.sendMessage(user.getMentionTag(), new EmbedBuilder().setTitle("Please narrow your search").setColor(Color.RED));
                    return null;
                });
            } else {
                channel.sendMessage(new EmbedBuilder().setTitle("No results").setColor(Color.RED));
            }
        }
    }

    private List<String> search(String param) {
        List<String> result = new ArrayList<>();
        types.keySet().forEach(s -> {
            if (s.toLowerCase().contains(param)){
                result.add(s);
            }
        });
        return result;
    }

}

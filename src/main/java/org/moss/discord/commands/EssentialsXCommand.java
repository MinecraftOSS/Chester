package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EssentialsXCommand implements CommandExecutor {

    private static JsonNode essxCommands;
    private static JsonNode essxPermissions;
    private static Map<String, List<String>> itemDb = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();

    public EssentialsXCommand() {
        try {
            essxCommands = mapper.readTree(new File("./essx_commands.json")).get(0).get("data");
            essxPermissions = mapper.readTree(new File("./essx_perms.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            JsonNode essxItemDB = mapper.readTree(new File("./essx_items.json"));

            Request request = new Request.Builder().url("https://raw.githubusercontent.com/EssentialsX/Essentials/2.x/Essentials/src/items.json").build();
            JsonNode essxItemDB2 =  mapper.readTree(Objects.requireNonNull(new OkHttpClient.Builder().build().newCall(request).execute().body()).string().replace("#version: ${full.version}", ""));

            if (essxItemDB2.size() > essxItemDB.size()) {
                System.out.println("Updating Essentials ItemDB.");
                updateItemDB(essxItemDB2);
            } else {
                parseItemDB(essxItemDB);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Acquiring Essentials ItemDB");
            updateItemDB(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Command(aliases = {"!essc", ".essc"}, usage = "!essc <command>", description = "Show info about a EssentialsX command")
    public void onCommand(String[] args, User user, TextChannel textChannel) {
        if (args.length >= 1) {
            JsonNode command = searchCommands(args[0]);
            if (command != null) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(new Color(11825408));

                embed.setTitle("Command Information").setUrl("https://essinfo.xeya.me/index.php?page=commands");
                embed.addInlineField("Command", command.get("Command").asText());
                embed.addInlineField("Usage", command.get("Syntax").asText());
                embed.addInlineField("Module", command.get("Module").asText());
                embed.addInlineField("Description", command.get("Description").asText());
                embed.addField("Aliases", String.format("```%s```", command.get("Aliases").asText()));
                embed.setFooter("Requested By | " + user.getName());

                textChannel.sendMessage(user.getMentionTag(), embed);
            } else {
                textChannel.sendMessage(new EmbedBuilder().setTitle("Command not found").setColor(Color.RED));
            }
        }
    }

    @Command(aliases = {"!essp", ".essp"}, usage = "!essp <permission>", description = "Show info about a EssentialsX permission")
    public void onPermCommand(String[] args, User user, TextChannel textChannel) {
        if (args.length >= 1) {
            List<JsonNode> perm = searchPermissions(args[0]);
            if (perm.size() != 0) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(new Color(11825408));
                embed.setTitle("Permission Information").setUrl("https://essinfo.xeya.me/index.php?page=permissions");

                for (JsonNode node : perm) {
                    embed.addField(node.get("Permission").asText(), String.format("```%s```", node.get("Description").asText()));
                }

                embed.setFooter("Requested By | " + user.getName());
                textChannel.sendMessage(user.getMentionTag(), embed);
            } else {
                textChannel.sendMessage(new EmbedBuilder().setTitle("Permission not found").setColor(Color.RED));
            }
        }
    }

    @Command(aliases = {"!itemdb", ".itemdb"}, usage = "!itemdb <item>", description = "Show info about a EssentialsX Item")
    public void onItemDbCommand(String[] args, User user, TextChannel textChannel) {
        if (args.length >= 1) {
            List<String> items = searchItems(args[0]);
            if (items.size() != 0) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(new Color(11825408));
                embed.setTitle("Essentials ItemDB").setUrl("https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/items.json");

                for (String item : items) {
                    List<String> db = itemDb.get(item);
                    embed.addField(item.toUpperCase(), db.isEmpty() ? item : String.format("```%s```", String.join(" ", itemDb.get(item))));
                }
                embed.setFooter("Requested By | " + user.getName());
                textChannel.sendMessage(user.getMentionTag(), embed)
                        .exceptionally(e -> {
                    textChannel.sendMessage(user.getMentionTag(), new EmbedBuilder().setColor(new Color(11825408)).setTitle("Essentials ItemDB").addField("Results", String.format("```%s```", String.join(" ", items))))
                            .exceptionally(ee -> {
                        textChannel.sendMessage(user.getMentionTag(), new EmbedBuilder().setTitle("Please narrow your search").setColor(Color.RED));
                        return null;
                    });
                    return null;
                });
            } else {
                textChannel.sendMessage(new EmbedBuilder().setTitle("Item not found").setColor(Color.RED));
            }
        }
    }

    private JsonNode searchCommands(String param) {
        for (int i = 0; i <= essxCommands.size() -1; i++) {
            JsonNode command = essxCommands.get(i);
            if (param.equalsIgnoreCase(command.get("Command").asText())) {
                return command;
            }
        }
        for (int i = 0; i < essxCommands.size(); i++) {
            JsonNode command = essxCommands.get(i);
            if (command.get("Aliases").asText().contains(param)) {
                return command;
            }
        }
        return null;
    }

    private List<JsonNode> searchPermissions(String param) {
        java.util.List<JsonNode> nodes = new ArrayList<>();
        for (Iterator<String> it = essxPermissions.fieldNames(); it.hasNext();) {
            JsonNode permNode = essxPermissions.get(it.next());
            if (permNode.get("Command").asText().contains(param) || permNode.get("Permission").asText().contains(param)) {
                nodes.add(permNode);
            }
        }
        return nodes;
    }

    private List<String> searchItems(String param) {
        List<String> result = new ArrayList<>();
        for (String s : itemDb.keySet()) {
            if (s.contains(param) || itemDb.get(s).stream().anyMatch(str -> str.contains(param))) {
                result.add(s);
            }
        }
        return result;
    }

    private void updateItemDB(JsonNode database) {
        try {
            JsonNode essxItemDB2;
            if (database == null) {
                Request request = new Request.Builder().url("https://raw.githubusercontent.com/EssentialsX/Essentials/2.x/Essentials/src/items.json").build();
                essxItemDB2 =  mapper.readTree(Objects.requireNonNull(new OkHttpClient.Builder().build().newCall(request).execute().body()).string().replace("#version: ${full.version}", ""));
            } else {
                essxItemDB2 = database;
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("./essx_items.json"), essxItemDB2);
            parseItemDB(essxItemDB2);
        } catch (Exception ignored) {}
    }

    private void parseItemDB(JsonNode essxItemDB) {
        for (Iterator<String> it = essxItemDB.fieldNames(); it.hasNext();) {
            String s = it.next();
            if (itemDb.get(essxItemDB.get(s).asText()) == null) {
                itemDb.put(s, new ArrayList<>());
            } else {
                itemDb.get(essxItemDB.get(s).asText()).add(s);
            }
        }
    }
}

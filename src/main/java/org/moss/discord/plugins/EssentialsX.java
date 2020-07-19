package org.moss.discord.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.moss.discord.Chester;
import org.moss.discord.ChesterPlugin;
import org.moss.discord.util.PagedEmbed;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EssentialsX extends Chester implements ChesterPlugin {

    private static String itemdbURL = "https://raw.githubusercontent.com/EssentialsX/Essentials/2.x/Essentials/src/items.json";
    private static String commanddbURL = "https://raw.githubusercontent.com/Xeyame/essinfo.xeya.me/master/data/commands.json";
    private static String permissionsdbURL = "https://raw.githubusercontent.com/Xeyame/essinfo.xeya.me/master/data/permissions.json";

    private JsonNode essxCommands;
    private JsonNode essxPermissions;
    private Map<String, List<String>> itemDb = new HashMap<>();

    private ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient.Builder().build();

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
        try {
            essxCommands = mapper.readTree(new File("./essx_commands.json"));
            JsonNode cmdb2 =  mapper.readTree(Objects.requireNonNull(client.newCall(new Request.Builder().url(commanddbURL).build()).execute().body()).string());
            if (cmdb2.size() > essxCommands.size()) {
                System.out.println("Updating Essentials CommandsDB.");
                updateCommandsDB();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Acquiring Essentials CommandsDB");
            updateCommandsDB();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            essxPermissions = mapper.readTree(new File("./essx_perms.json"));
            JsonNode permdb2 =  mapper.readTree(Objects.requireNonNull(client.newCall(new Request.Builder().url(permissionsdbURL).build()).execute().body()).string());
            if (permdb2.size() > essxPermissions.size()) {
                System.out.println("Updating Essentials PermsDB.");
                updatePermissionsDB();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Acquiring Essentials PermissionsDB");
            updatePermissionsDB();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            JsonNode essxItemDB = mapper.readTree(new File("./essx_items.json"));

            Request request = new Request.Builder().url(itemdbURL).build();
            JsonNode essxItemDB2 =  mapper.readTree(Objects.requireNonNull(client.newCall(request).execute().body()).string()
                    .replace("#version: ${full.version}", "")
                    .replace("# This file is for internal EssentialsX usage.","")
                    .replace("# We recommend using custom_items.yml to add custom aliases.",""));

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

    @Command(aliases = {"!essc", ".essc"}, usage = "!essc <command>", description = "Show info about an EssentialsX command")
    public void onCommand(String[] args, User user, TextChannel textChannel) {
        if (args.length >= 1) {
            List<JsonNode> commands = searchCommands(args[0]);
            if (commands.size() >= 1) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(new Color(11825408))
                        .setTitle("Command Information")
                        .setUrl("https://essinfo.xeya.me/index.php?page=commands")
                        .setAuthor(user);
                PagedEmbed pagedEmbed = new PagedEmbed(textChannel, embed, user);
                pagedEmbed.setMaxFieldsPerPage(5);
                for (JsonNode command : commands) {
                    pagedEmbed.addField(command.get(1).asText(), String.format("```Usage: %s \n\nDescripton: %s \n\nAliases: %s```", command.get(4).asText(), command.get(3).asText(), command.get(2).asText()));
                }
                pagedEmbed.build().join();
            } else {
                textChannel.sendMessage(new EmbedBuilder().setTitle("Command not found").setColor(Color.RED));
            }
        }
    }

    @Command(aliases = {"!essp", ".essp"}, usage = "!essp <permission>", description = "Show info about an EssentialsX permission")
    public void onPermCommand(String[] args, User user, TextChannel textChannel) {
        if (args.length >= 1) {
            List<JsonNode> perm = searchPermissions(args[0]);
            if (perm.size() != 0) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(new Color(11825408))
                        .setTitle("Permission Information")
                        .setUrl("https://essinfo.xeya.me/index.php?page=permissions")
                        .setAuthor(user);
                PagedEmbed pagedEmbed = new PagedEmbed(textChannel, embed, user);
                for (JsonNode node : perm) {
                    pagedEmbed.addField(node.get(2).asText(), String.format("```%s```", node.get(3).asText()));
                }
                pagedEmbed.build().join();
            } else {
                textChannel.sendMessage(new EmbedBuilder().setTitle("Permission not found").setColor(Color.RED));
            }
        }
    }

    @Command(aliases = {"!itemdb", ".itemdb"}, usage = "!itemdb <item>", description = "Show info about an EssentialsX Item")
    public void onItemDbCommand(String[] args, User user, TextChannel textChannel) {
        if (args.length >= 1) {
            List<String> items = searchItems(args[0]);
            if (items.size() != 0) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(new Color(11825408))
                        .setTitle("Essentials ItemDB")
                        .setUrl("https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/items.json")
                        .setAuthor(user);
                PagedEmbed pagedEmbed = new PagedEmbed(textChannel, embed, user);
                for (String item : items) {
                    List<String> db = itemDb.get(item);
                    pagedEmbed.addField(item.toUpperCase(), db.isEmpty() ? String.format("```%s```", item) : String.format("```%s```", String.join(" ", itemDb.get(item))));
                }
                pagedEmbed.build().join();
            } else {
                textChannel.sendMessage(new EmbedBuilder().setTitle("Item not found").setColor(Color.RED));
            }
        }
    }

    private List<JsonNode> searchCommands(String param) {
        List<JsonNode> nodes = new ArrayList<>();
        for (JsonNode command : essxCommands.get("data")) {
            if (param.equalsIgnoreCase(command.get(1).asText()) || command.get(2).asText().contains(param)) {
                nodes.add(command);
            }
        }
        return nodes;
    }

    private List<JsonNode> searchPermissions(String param) {
        List<JsonNode> nodes = new ArrayList<>();
        for (JsonNode permNode : essxPermissions.get("data")) {
            if (permNode.get(1).asText().contains(param) || permNode.get(2).asText().contains(param)) {
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

    private void updateCommandsDB() {
        try {
            essxCommands = mapper.readTree(cleanUp(Objects.requireNonNull(client.newCall(new Request.Builder().url(commanddbURL).build()).execute().body()).string()));
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("./essx_commands.json"), essxCommands);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePermissionsDB() {
        try {
            essxPermissions = mapper.readTree(cleanUp(Objects.requireNonNull(client.newCall(new Request.Builder().url(permissionsdbURL).build()).execute().body()).string()));
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("./essx_perms.json"), essxPermissions);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private String cleanUp(String xeyamesMess) { //https://i.imgur.com/InxkMUu.png
        return xeyamesMess.replace("&lt;", "<").replace("&gt;", ">");
    }
}

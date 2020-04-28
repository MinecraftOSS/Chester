package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.moss.discord.util.PagedEmbed;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.*;

public class EssentialsXCommand implements CommandExecutor {


    private static final String itemdbURL = "https://raw.githubusercontent.com/EssentialsX/Essentials/2.x/Essentials/src/items.json";
    private static final String commanddbURL = "https://essinfo.xeya.me/index.php?page=commands&raw-data=true";
    private static final String permissionsdbURL = "https://essinfo.xeya.me/index.php?page=permissions&raw-data=true";

    private JsonNode essxCommands;
    private JsonNode essxPermissions;
    private final Map<String, List<String>> itemDb = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient.Builder().build();

    public EssentialsXCommand() {
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
            JsonNode essxItemDB2 =  mapper.readTree(Objects.requireNonNull(client.newCall(request).execute().body()).string().replace("#version: ${full.version}", ""));

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
    public void onCommand(String[] args, Member user, TextChannel textChannel) {
        if (args.length >= 1) {
            List<JsonNode> commands = searchCommands(args[0]);
            if (commands.size() >= 1) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(new Color(11825408))
                        .setTitle("Command Information", "https://essinfo.xeya.me/index.php?page=commands")
                        .setAuthor(user.getUser().getName());
                PagedEmbed pagedEmbed = new PagedEmbed(textChannel, embed, user.getUser());
                pagedEmbed.setMaxFieldsPerPage(5);
                for (JsonNode command : commands) {
                    pagedEmbed.addField(command.get("Command").asText(), String.format("```Usage: %s \n\nDescripton: %s \n\nAliases: %s```", command.get("Syntax").asText(), command.get("Description").asText(), command.get("Aliases").asText()));
                }
                pagedEmbed.build().join();
            } else {
                textChannel.sendMessage(new EmbedBuilder().setTitle("Command not found").setColor(Color.RED).build()).queue();
            }
        }
    }

    @Command(aliases = {"!essp", ".essp"}, usage = "!essp <permission>", description = "Show info about an EssentialsX permission")
    public void onPermCommand(String[] args, Member user, TextChannel textChannel) {
        if (args.length >= 1) {
            List<JsonNode> perm = searchPermissions(args[0]);
            if (perm.size() != 0) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(new Color(11825408))
                        .setTitle("Permission Information", "https://essinfo.xeya.me/index.php?page=permissions")
                        .setAuthor(user.getUser().getName());
                PagedEmbed pagedEmbed = new PagedEmbed(textChannel, embed, user.getUser());
                for (JsonNode node : perm) {
                    pagedEmbed.addField(node.get("Permission").asText(), String.format("```%s```", node.get("Description").asText()));
                }
                pagedEmbed.build().join();
            } else {
                textChannel.sendMessage(new EmbedBuilder().setTitle("Permission not found").setColor(Color.RED).build()).queue();
            }
        }
    }

    @Command(aliases = {"!itemdb", ".itemdb"}, usage = "!itemdb <item>", description = "Show info about an EssentialsX Item")
    public void onItemDbCommand(String[] args, Member user, TextChannel textChannel) {
        if (args.length >= 1) {
            List<String> items = searchItems(args[0]);
            if (items.size() != 0) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(new Color(11825408))
                        .setTitle("Essentials ItemDB", "https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/items.json")
                        .setAuthor(user.getUser().getName());
                PagedEmbed pagedEmbed = new PagedEmbed(textChannel, embed, user.getUser());
                for (String item : items) {
                    List<String> db = itemDb.get(item);
                    pagedEmbed.addField(item.toUpperCase(), db.isEmpty() ? String.format("```%s```", item) : String.format("```%s```", String.join(" ", itemDb.get(item))));
                }
                pagedEmbed.build().join();
            } else {
                textChannel.sendMessage(new EmbedBuilder().setTitle("Item not found").setColor(Color.RED).build()).queue();
            }
        }
    }

    private List<JsonNode> searchCommands(String param) {
        List<JsonNode> nodes = new ArrayList<>();
        for (Iterator<String> it = essxCommands.fieldNames(); it.hasNext();) {
            JsonNode command = essxCommands.get(it.next());
            if (param.equalsIgnoreCase(command.get("Command").asText()) || command.get("Aliases").asText().contains(param)) {
                nodes.add(command);
            }
        }
        return nodes;
    }

    private List<JsonNode> searchPermissions(String param) {
        List<JsonNode> nodes = new ArrayList<>();
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

package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EssentialsXCommand implements CommandExecutor {

    private static JsonNode essxCommands;
    private static JsonNode essxPermissions;

    public EssentialsXCommand() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            essxCommands = mapper.readTree(new File("./essx_commands.json")).get(0).get("data");
            essxPermissions = mapper.readTree(new File("./essx_perms.json")).get(0).get("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Command(aliases = {"!essc", ".essc"}, usage = "!essc <command>", description = "Show info about a EssentialsX command")
    public void onCommand(DiscordApi api, String[] args, User user, TextChannel textChannel) {
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
    public void onPermCommand(DiscordApi api, String[] args, User user, TextChannel textChannel) {
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

    private JsonNode searchCommands(String param) {
        for (int i = 0; i <= essxCommands.size(); i++) {
            JsonNode command = essxCommands.get(i);
            if (param.equalsIgnoreCase(command.get("Command").asText()) ) {
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
        for (int i = 0; i <= essxPermissions.size(); i++) {
            JsonNode permNode = essxPermissions.get(i);
            if (permNode.get("Command").asText().contains(param) || permNode.get("Permission").asText().contains(param)) {
                nodes.add(permNode);
            }
        }
        return nodes;
    }
}

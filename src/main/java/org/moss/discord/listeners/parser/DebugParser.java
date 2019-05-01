package org.moss.discord.listeners.parser;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.Scanner;

public class DebugParser implements LogParser {

    private String[] mossPlugins = {"Essentials", "Factions", "NuVotifier", "PlayerVaultsX", "LWC", "ObsidianDestroyer", "Guilds", "eZProtector"};

    @Override
    public void evaluate(Message message, Scanner log) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("**Log Info**");
        embed.setColor(Color.GREEN);

        while(log.hasNextLine()) {
            String line = log.nextLine();
            if (line.contains("This server is running")) {
                embed.addInlineField("Minecraft", "`" + line.substring(line.indexOf("MC:") + 3, line.indexOf(")")) + "`" );
                embed.addInlineField("Server Software", "`" + line.substring(line.indexOf("git-"), line.indexOf("(") - 1) + "`");
            }

            for (String plugin : mossPlugins) {
                if (line.contains("Loading " + plugin)) {
                    embed.addField(plugin, "`" + line.substring(line.lastIndexOf(" ")) + "`");
                }
            }
        }

        message.getChannel().sendMessage(embed);
    }

}

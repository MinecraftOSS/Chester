package org.moss.discord.listeners.log.parsers;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.moss.discord.listeners.log.LogData;
import org.moss.discord.listeners.log.LogParser;

import java.awt.*;
import java.util.Scanner;

public class DebugParser implements LogParser {

    private String[] mossPlugins = {"Essentials", "Factions", "NuVotifier", "PlayerVaultsX", "LWC", "ObsidianDestroyer", "Guilds", "eZProtector"};

    @Override
    public void evaluate(LogData log) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("**Log Info**");
        embed.setColor(Color.GREEN);

        Scanner scanner = log.getLog();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("This server is running")) {
                log.setVersion(line.substring(line.indexOf("MC:") + 3, line.indexOf(")")));
                log.setServerSoftware(line.substring(line.indexOf("git-"), line.indexOf("(") - 1));
            }

            if (line.contains("Loading ")) {
                for (String plugin : mossPlugins) {
                    if (line.contains(plugin)) {
                        log.addPlugin(new LogData.Plugin(plugin, line.substring(line.lastIndexOf(" "))));
                    }
                }
            }
        }

        embed.addInlineField("Minecraft", "`" + log.getVersion() + "`" );
        embed.addInlineField("Server Software", "`" + log.getServerSoftware() + "`");
        for (LogData.Plugin plugin : log.getPlugins()) {
            embed.addField(plugin.getName(), "`" + plugin.getVersion() + "`");
        }

        log.getMessage().getChannel().sendMessage(embed);
    }

}

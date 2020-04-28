package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.moss.discord.util.BStatsUtil;

import java.time.Instant;

public class MojangCommand implements CommandExecutor {

    @Command(aliases = {"!mojang", "!mcstatus"}, usage = "!mojang", description = "Shows mojang servers")
    public void onCommand(JDA api, TextChannel channel, String[] args) {
        BStatsUtil bStatsUtil = new BStatsUtil(api);
        EmbedBuilder embed = new EmbedBuilder();
        try {

            JsonNode status = bStatsUtil.makeRequest("http://status.mojang.com/check");
            embed.setAuthor("Mojang Status");
            embed.setThumbnail("https://i.imgur.com/uPuh3cT.png");

            embed.addField("Minecraft | Website", parseStatus(status.get(0).get("minecraft.net").asText()), true);
            embed.addField("Minecraft | Session", parseStatus(status.get(1).get("session.minecraft.net").asText()) +"\n", true);

            embed.addField("Minecraft | Textures", parseStatus(status.get(6).get("textures.minecraft.net").asText()), true);
            embed.addField("Mojang | Accounts", parseStatus(status.get(2).get("account.mojang.com").asText()) +"\n", true);

            embed.addField("Mojang | AuthServer", parseStatus(status.get(3).get("authserver.mojang.com").asText()), true);
            embed.addField("Mojang | Session", parseStatus(status.get(4).get("sessionserver.mojang.com").asText()) +"\n", true);

            embed.addField("Mojang | API", parseStatus(status.get(5).get("api.mojang.com").asText()), true);
            embed.addField("Mojang | Website", parseStatus(status.get(7).get("mojang.com").asText()), true);

            embed.setTimestamp(Instant.now());

        } catch (Exception e) {
            embed.addField("Error", "```"+e.toString() + " @ " +e.getStackTrace()[0]+"```", false);
        }

        channel.sendMessage(embed.build()).queue();
    }

    public String parseStatus(String status) {
        if (status.equalsIgnoreCase("green")) {
            return "\u2705";
        }
        return "\u274C";
    }
}

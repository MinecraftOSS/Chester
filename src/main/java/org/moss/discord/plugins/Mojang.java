package org.moss.discord.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import de.btobastian.sdcf4j.Command;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.moss.discord.Chester;
import org.moss.discord.util.BStatsUtil;

public class Mojang extends Chester {

    public Mojang() {
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!mojang", "!mcstatus"}, usage = "!mojang", description = "Shows mojang servers")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {
        BStatsUtil bStatsUtil = new BStatsUtil(api);
        EmbedBuilder embed = new EmbedBuilder();
        try {

            JsonNode status = bStatsUtil.makeRequest("http://status.mojang.com/check");
            embed.setAuthor("Mojang Status");
            embed.setThumbnail("https://i.imgur.com/uPuh3cT.png");

            embed.addInlineField("Minecraft | Website", parseStatus(status.get(0).get("minecraft.net").asText()));
            embed.addInlineField("Minecraft | Session", parseStatus(status.get(1).get("session.minecraft.net").asText()) +"\n");

            embed.addInlineField("Minecraft | Textures", parseStatus(status.get(6).get("textures.minecraft.net").asText()));
            embed.addInlineField("Mojang | Accounts", parseStatus(status.get(2).get("account.mojang.com").asText()) +"\n");

            embed.addInlineField("Mojang | AuthServer", parseStatus(status.get(3).get("authserver.mojang.com").asText()));
            embed.addInlineField("Mojang | Session", parseStatus(status.get(4).get("sessionserver.mojang.com").asText()) +"\n");

            embed.addInlineField("Mojang | API", parseStatus(status.get(5).get("api.mojang.com").asText()));
            embed.addInlineField("Mojang | Website", parseStatus(status.get(7).get("mojang.com").asText()));

            embed.setTimestampToNow();

        } catch (Exception e) {
            embed.addField("Error", "```"+e.toString() + " @ " +e.getStackTrace()[0]+"```");
        }

        channel.sendMessage(embed);
    }

    public String parseStatus(String status) {
        if (status.equalsIgnoreCase("green")) {
            return "\u2705";
        }
        return "\u274C";
    }
}

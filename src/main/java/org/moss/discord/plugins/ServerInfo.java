package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.moss.discord.Chester;
import org.moss.discord.ChesterPlugin;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ServerInfo extends Chester implements ChesterPlugin {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!serverinfo"}, usage = "!serverinfo", description = "Shows server info")
    public void onCommand(TextChannel textChannel, Server server) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(server.getName());
        embed.setThumbnail(server.getIcon().get());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy").withLocale(Locale.US).withZone(ZoneId.systemDefault());

        embed.addInlineField("Created", formatter.format(server.getCreationTimestamp()));
        embed.addInlineField("Region", server.getRegion().getName());

        embed.addInlineField("Users", String.valueOf(server.getMemberCount()));
        embed.addInlineField("Roles", String.valueOf(server.getRoles().size()));

        embed.addInlineField("Text Channels", String.valueOf(server.getTextChannels().size()));
        embed.addInlineField("Voice Channels", String.valueOf(server.getVoiceChannels().size()));


        embed.addInlineField("Owner", server.getOwner().getMentionTag());
        embed.addInlineField("Verification", server.getVerificationLevel().name());

        embed.setFooter("ServerID: " + server.getIdAsString(), server.getIcon().get());

        textChannel.sendMessage(embed);
    }

}

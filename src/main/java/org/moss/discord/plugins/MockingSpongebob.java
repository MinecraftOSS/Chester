package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.moss.discord.Chester;
import org.moss.discord.ChesterPlugin;

import java.awt.*;
import java.net.URL;

public class MockingSpongebob extends Chester implements ChesterPlugin {

    private static final String msb = "https://mockingspongebob.org/%s.jpg";

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!msb", ".msb"}, usage = "!msb text", description = "Mocking spongebob")
    public void onBob(TextChannel channel, String[] args, MessageAuthor author) {
        if (!author.canKickUsersFromServer()) {
            return;
        }
        if (args.length >= 1) {
            try {
                channel.sendMessage(new EmbedBuilder().setImage(new URL(String.format(msb, String.join("_", args))).openStream()).setColor(Color.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

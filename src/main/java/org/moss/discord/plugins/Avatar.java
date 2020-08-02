package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.moss.chesterapi.ChesterPlugin;
import org.moss.discord.Chester;

public class Avatar extends Chester implements ChesterPlugin {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }


    @Command(aliases = {"!avatar", ".avatar"}, usage = "!avatar <User>", description = "Shows the users' avatar")
    public void onCommand(String[] args, TextChannel channel, Message message) {
        if (args.length >= 1 && message.getMentionedUsers().size() >= 1) {
            channel.sendMessage(new EmbedBuilder().setImage(message.getMentionedUsers().get(0).getAvatar().getUrl().toString()+"?size=1024"));
        }
    }

}

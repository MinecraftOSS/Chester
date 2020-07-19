package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.moss.discord.Chester;
import org.moss.discord.ChesterPlugin;

import java.awt.*;
import java.net.URL;

public class Avatar extends Chester implements ChesterPlugin {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }


    @Command(aliases = {"!avatar", ".avatar"}, usage = "!avatar <User>", description = "Shows the users' avatar")
    public void onCommand(DiscordApi api, String[] args, TextChannel channel, Message message, Server server) {
        if (args.length >= 1) {
            if (message.getMentionedUsers().size() >= 1) {
                channel.sendMessage(new EmbedBuilder().setImage(message.getMentionedUsers().get(0).getAvatar()));
                return;
            }
            for (User user : server.getMembers()) {
                if (user.getName().equalsIgnoreCase(args[0])) {
                    channel.sendMessage(new EmbedBuilder().setImage(user.getAvatar())); //TODO LAAAARGER
                    return;
                }
            }
        }
    }

    @Command(aliases = {"!setavatar"}, usage = "!setavatar <img>", description = "Sets the bot's avatar")
    public void onSetAvatar(DiscordApi api, String[] args, TextChannel channel, Message message, MessageAuthor messageAuthor) {
        if (args.length >= 1 && messageAuthor.canManageServer()) {
            try {
                URL url = new URL(args[0]); //pray to god its a URL
                message.delete();
                api.createAccountUpdater().setAvatar(url).update();
                channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle("Avatar set!").setImage(args[0]));
            } catch (Exception e) {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Unable to set avatar").setDescription("`!setavatar <url>`"));
            }
        }
    }
}

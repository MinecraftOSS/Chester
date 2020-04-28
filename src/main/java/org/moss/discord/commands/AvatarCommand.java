package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.net.URL;

public class AvatarCommand implements CommandExecutor {

    @Command(aliases = {"!avatar", ".avatar"}, usage = "!avatar <User>", description = "Shows the users' avatar")
    public void onCommand(JDA api, String[] args, TextChannel channel, Message message, Guild server) {
        if (args.length >= 1) {
            if (message.getMentionedUsers().size() >= 1) {
                channel.sendMessage(new EmbedBuilder().setImage(message.getMentionedUsers().get(0).getAvatarUrl()).build()).queue();
                return;
            }
            for (Member user : server.getMembers()) {
                if (user.getUser().getName().equalsIgnoreCase(args[0])) {
                    channel.sendMessage(new EmbedBuilder().setImage(user.getUser().getAvatarUrl()).build()).queue(); //TODO LAAAARGER
                    return;
                }
            }
        }
    }

    @Command(aliases = {"!setavatar"}, usage = "!setavatar <img>", description = "Sets the bot's avatar")
    public void onSetAvatar(JDA api, String[] args, TextChannel channel, Message message, Member messageAuthor) {
        if (args.length >= 1 && messageAuthor.hasPermission(Permission.MANAGE_SERVER)) {
            try {
                URL url = new URL(args[0]); //pray to god its a URL
                message.delete().queue();
                api.getSelfUser().getManager().setAvatar(Icon.from(url.openStream())).queue();
                channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle("Avatar set!").setImage(args[0]).build()).queue();
            } catch (Exception e) {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Unable to set avatar").setDescription("`!setavatar <url>`").build()).queue();
            }
        }
    }
}

package org.moss.discord.commands.moderation;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class KickCommand implements CommandExecutor {

    @Command(aliases = {"!kick"}, usage = "!kick <username> <reason>", description = "Kicks a chosen user.")
    public void onCommand(TextChannel channel, String[] args, Message message, Member author) {
        if (author.hasPermission(Permission.KICK_MEMBERS)) {
            if (args.length >= 2) {
                User user = message.getMentionedUsers().get(0);
                String reason = String.join(" ", args).substring(args[0].length());

                message.getGuild().kick(user.getId(), reason).queue();

                channel.sendMessage("Succesfully kicked " + user.getAsMention() + " for " + reason).queue();
            } else {
                channel.sendMessage("You need to specify a user and reason to kick!").queue();
            }
        } else message.addReaction("\uD83D\uDC4E").queue();
    }

}

package org.moss.discord.commands.moderation;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang.StringUtils;

public class BanCommand implements CommandExecutor {

    @Command(aliases = {"!ban"}, usage = "!ban <username> <reason>", description = "Bans a chosen user.")
    public void onCommand(TextChannel channel, String[] args, Message message, Member author) {
        if (author.hasPermission(Permission.BAN_MEMBERS)) {
            if (args.length >= 2) {
                User user = message.getMentionedUsers().get(0);
                String reason = String.join(" ", args).substring(args[0].length());

                message.getGuild().ban(user, 1, reason).queue();

                channel.sendMessage("Succesfully banned " + user.getAsMention() + " for " + reason).queue();
            } else {
                channel.sendMessage("You need to specify a user and reason to ban!").queue();
            }
        } else message.addReaction("\uD83D\uDC4E").queue();
    }

    @Command(aliases = {"!softban"}, usage = "!ban <username> <days>", description = "Ban then unbans a user and deleting <days> of messages.")
    public void onSoftBan(TextChannel channel, String[] args, Message message, Member author) {
        if (author.hasPermission(Permission.BAN_MEMBERS)) {
            if (args.length == 2 && StringUtils.isNumeric(args[1])) {
                User user = message.getMentionedUsers().get(0);
                message.getGuild().ban(user, Integer.parseInt(args[1]), "Softban").queue();

                channel.sendMessage("Soft-banned " + user.getAsMention() + " successfully!").queue();

                message.getGuild().unban(user).queue();
            } else if (args.length == 1) {
                channel.sendMessage("You need an integer 1-7").queue();
            } else if (args.length == 0) {
                channel.sendMessage("You need to mention a user and specify a reason to ban!").queue();
            }
        } else message.addReaction("\uD83D\uDC4E").queue();
    }

}
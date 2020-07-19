package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.apache.commons.lang.StringUtils;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.moss.discord.Chester;
import org.moss.discord.util.VerificationUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class Profile extends Chester {

    VerificationUtil veriUtil = new VerificationUtil();

    public Profile() {
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!profile"}, usage = "!profile <User>", description = "View user profiles")
    public void onCommand(User user, Server server, TextChannel channel, Message message, String[] args) {
        if (args.length >= 1) {
            User target = message.getMentionedUsers().size() >= 1 ? message.getMentionedUsers().get(0) : findUser(args[0], server);
            if (target != null) {
                channel.sendMessage(user.getMentionTag(), buildProfile(target, server));
            }
        } else {
            channel.sendMessage(buildProfile(user, server));
        }
    }

    private EmbedBuilder buildProfile(User user, Server server) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Profile")
                .setAuthor(user)
                .setThumbnail(user.getAvatar().getUrl().toString())
                .addInlineField("User", user.getDiscriminatedName())
                .addInlineField("Status", getFancyStatus(user))
                //TODO Add status here <https://github.com/Javacord/Javacord/issues/583>
                .addField(":medal: Rating", server.isAdmin(user) ? ":star2: :star2: :star2: :star2: :star2:" : getRating())
                .addInlineField(":calendar: Account Created", getFormattedTime(user.getCreationTimestamp()))
                .addInlineField(":calendar_spiral: Server Joined", getFormattedTime(user.getJoinedAtTimestamp(server).get()))
                .setFooter("ID: " + user.getIdAsString());
        if (veriUtil.isVerified(user.getIdAsString())) {
            embed.addField(":jigsaw: Connections", String.format("[[SpigotMC]](https://www.spigotmc.org/members/%s/)", veriUtil.getSpigotID(user.getIdAsString())));
        }
        return embed;
    }

    private String getFancyStatus(User user) {
        switch (user.getStatus()) {
            case ONLINE:
                return ":green_circle: Online";
            case IDLE:
                return ":yellow_circle: Idle";
            case DO_NOT_DISTURB:
                return ":no_entry: Do Not Disturb";
        }
        return ":black_circle: Offline";
    }

    private String getRating() {
        return StringUtils.repeat(":star:", ThreadLocalRandom.current().nextInt(1, 6));
    }

    private String getFormattedTime(Instant instant) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd MMMM yyyy")
                .withLocale(Locale.US)
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    private User findUser(String name, Server server) {
        List<User> users = new ArrayList<>(server.getMembersByNameIgnoreCase(name));
        return users.isEmpty() ? null : users.get(0);
    }
}

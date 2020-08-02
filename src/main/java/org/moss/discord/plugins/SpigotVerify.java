package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.apache.commons.lang.StringUtils;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.moss.discord.Chester;
import org.moss.chesterapi.ChesterPlugin;
import org.moss.discord.Constants;
import org.moss.discord.util.VerificationUtil;

import java.awt.*;

public class SpigotVerify extends Chester implements ChesterPlugin {

    private VerificationUtil verificationUtil = new VerificationUtil();

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }


    @Command(aliases = {"!spigot"}, usage = "!spigot <Spigot ID>", description = "Spigot linking")
    public void onCommand(User user, Server server, TextChannel channel, String[] args) {
        if (args.length == 1) {
            if (!StringUtils.isNumeric(args[0])) {
                channel.sendMessage(user.getMentionTag(), new EmbedBuilder().setColor(Color.RED).setDescription("Please make sure to use your numeric ID").setImage("https://i.imgur.com/smkf1Td.png"));
            }
            if (verificationUtil.isVerified(user.getIdAsString())) {
                channel.sendMessage(user.getNicknameMentionTag(), new EmbedBuilder().setTitle("Your account is already linked!").setColor(Color.RED));
                return;
            }
            if (verificationUtil.verify(user, Integer.valueOf(args[0]))) {
                channel.sendMessage(user.getMentionTag(),
                        new EmbedBuilder().setTitle("Success!")
                                .setDescription("Your spigot and discord account is now linked!")
                                .setFooter("Thanks!").setColor(Color.decode("#884ea0")));
                user.addRole(server.getRoleById(Constants.ROLE_LINKED).get(), "Spigot link");
            } else {
                channel.sendMessage(user.getMentionTag(), getHelpEmbed(user));
            }
        } else {
            channel.sendMessage(getHelpEmbed(user));
        }

    }

    @Command(aliases = {"!fspigot"}, usage = "!fspigot <user> <Spigot ID>", description = "Spigot linking", requiredPermissions = "chester.admin")
    public void onForceLink(User user, Server server, TextChannel channel, String[] args, Message message) {
        if (args.length >= 2 && message.getMentionedUsers().size() >= 1 && StringUtils.isNumeric(args[1])) {
            User target = message.getMentionedUsers().get(0);
            if (verificationUtil.verify(target, Integer.valueOf(args[1]), true)) {
                target.addRole(server.getRoleById(Constants.ROLE_LINKED).get(), "Spigot link");
                channel.sendMessage(user.getMentionTag(),
                        new EmbedBuilder()
                                .setTitle("Success!")
                                .setDescription(String.format("Force linked [SpigotMC account](https://www.spigotmc.org/members/%s/) to `%s`", args[1], target.getDiscriminatedName()))
                                .setFooter("Thanks!")
                                .setColor(Color.decode("#884ea0")));
            } else {
                channel.sendMessage(new EmbedBuilder().setTitle("Unable to force link user, spigot id already liked?").setColor(Color.RED));
            }
        }
    }

    private EmbedBuilder getHelpEmbed(User user) {
        return new EmbedBuilder().setTitle("Unable to verify account!")
                .setDescription(String.format("Please make sure you have your Discord correctly set to `%s` in your [contact details](https://www.spigotmc.org/account/contact-details) section on Spigot. \nSee ?linking for more details", user.getDiscriminatedName())).addField("Privacy", "Make sure to also set your contact details public on the [privacy page](https://www.spigotmc.org/account/privacy)").setColor(Color.RED);
    }

}

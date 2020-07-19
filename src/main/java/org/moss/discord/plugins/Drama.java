package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.moss.discord.Chester;
import org.moss.discord.ChesterPlugin;

import java.awt.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Drama extends Chester implements ChesterPlugin {

    private static final String url = "https://drama.essentialsx.net/";
    private static final String regex = "(?s)(?<=<h1>)(.+?)(?=<\\/h1>)";
    private static final String permalink = "href=\"https://drama.essentialsx.net/([0-9A-Za-z/+=]+)\"";
    private static final OkHttpClient client = new OkHttpClient.Builder().build();

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!drama"}, usage = "!drama", description = "Creates a spigot drama")
    public void onCommand(TextChannel channel, User user, MessageAuthor author) {
        if (!author.canKickUsersFromServer()) {
            return;
        }
        try {
            String body = client.newCall(new Request.Builder().url(url).build()).execute().body().string();
            Matcher matcher = Pattern.compile(regex).matcher(body);
            if (matcher.find()) {
                String drama = matcher.group(1);
                matcher = Pattern.compile(permalink).matcher(body);
                matcher.find();
                channel.sendMessage(user.getMentionTag(), new EmbedBuilder().setTitle("**Drama Alert**!").setDescription(drama).setColor(Color.RED).setUrl(url+matcher.group(1)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

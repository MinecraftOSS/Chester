package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

public class WolframAlphaCommand implements CommandExecutor {

    private String query = "https://api.wolframalpha.com/v1/result?i=<QUERY>&appid=";
    private String queryLink = "https://www.wolframalpha.com/input/?i=<QUERY>";
    private String key = "DEMO";

    private Message msg;

    @Command(aliases = {"!wolfram", "!wa"}, usage = "!wolfram <Query>", description = "Search wolframalpha")
    public void onCommand(JDA api, Member user, TextChannel channel, String[] args) {
        long epoch = System.currentTimeMillis();
        String url = query.replace("<QUERY>", String.join("%20", args))+key;
        String link = queryLink.replace("<QUERY>", String.join("%20", args));
        channel.sendMessage(new EmbedBuilder().setTitle("Querying WolframAlpha").build()).queue(message -> msg = message);
        try {
            String reply = "Yes";
            URLConnection connection = new URL(url).openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            reply = reader.lines().collect(Collectors.joining("\n"));
            msg.editMessage(new MessageBuilder().setContent(user.getAsMention()).setEmbed(new EmbedBuilder()
                .setTitle("Answer", link)
                .setDescription(String.format("```%s```", reply))
                .setColor(Color.GREEN)
                .setFooter("Took " +(System.currentTimeMillis() - epoch) +"ms").build()).build()).queue();
        } catch (IOException e) {
            msg.editMessage(new MessageBuilder().setContent(user.getAsMention()).setEmbed(new EmbedBuilder().setColor(Color.RED).setTitle("Unable to query WolframAlpha").build()).build()).queue();
            e.printStackTrace();
        }
    }
}
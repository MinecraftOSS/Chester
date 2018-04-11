package org.moss.discord.listeners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.DiscordRegexPattern;
import org.moss.discord.Constants;
import org.moss.discord.fun.Nonsense;

public class NonsenseListener implements MessageCreateListener {
    private static File corpusFile = new File("./corpus.txt");

    private DiscordApi api;
    private Nonsense nonsense;

    private boolean ready = false;

    public NonsenseListener(DiscordApi discordApi) {
        api = discordApi;

        String corpus = "";

        try {
            Scanner scanner = new Scanner(corpusFile);
            scanner.useDelimiter("\\Z");
            corpus = scanner.next()
                .replaceAll("[“”]", "\"")
                .replaceAll("[‘’]", "\'");
            scanner.close();
        } catch (FileNotFoundException ignored) {}

        nonsense = new Nonsense(corpus);

        nonsense.getReady().whenCompleteAsync((chain, throwable) -> {
            if (throwable == null) ready = true;
        });
    }

	@Override
	public void onMessageCreate(MessageCreateEvent event) {
        api.getMessageById(event.getMessageId(), event.getChannel()).thenAccept(this::handleMessage);
    }
    
    private void handleMessage(Message message) {
        TextChannel channel = message.getChannel();
        if (!ready
            || !channel.getIdAsString().equals(Constants.CHANNEL_NONSENSE)
            || message.getAuthor().isYourself()) return;

        if (message.getMentionedUsers().contains(api.getYourself())) {
            String response = message.getAuthor().asUser().get().getMentionTag() + " " + nonsense.generateNonsense();
            channel.sendMessage(response);
        } else {
            storeMessage(message.getContent());
        }
    }

    private void storeMessage(String message) {
        String filtered = message.replaceAll(DiscordRegexPattern.CHANNEL_MENTION.pattern(), "")
            .replaceAll(DiscordRegexPattern.CUSTOM_EMOJI.pattern(), "")
            .replaceAll(DiscordRegexPattern.ROLE_MENTION.pattern(), "")
            .replaceAll(DiscordRegexPattern.USER_MENTION.pattern(), "");

        try {
            FileWriter fw = new FileWriter(corpusFile, true);
            fw.append("\n" + filtered);
            fw.close();
        } catch (IOException ignored) {}
        
        nonsense.addToChain(filtered);
    }
}
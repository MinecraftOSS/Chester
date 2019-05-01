package org.moss.discord.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.moss.discord.listeners.parser.DebugParser;
import org.moss.discord.listeners.parser.TruAntiLagParser;
import org.moss.discord.listeners.parser.provider.FileProvider;
import org.moss.discord.listeners.parser.provider.PasteProvider;
import org.moss.discord.listeners.parser.LogParser;
import org.moss.discord.listeners.parser.provider.LogProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MrParser implements MessageCreateListener {

    private static final Logger logger = LoggerFactory.getLogger(MrParser.class);
    private DiscordApi api;

    /* Providers */
    private PasteProvider hastebin = new PasteProvider();
    private FileProvider file = new FileProvider();

    /* Parsers */
    private DebugParser debug = new DebugParser();
    private TruAntiLagParser truAntiLag = new TruAntiLagParser();

    private List<LogProvider> providers = new ArrayList<>();
    private List<LogParser> parsers = new ArrayList<>();

    public MrParser(DiscordApi dApi) {
        api = dApi;

        providers.add(hastebin);
        providers.add(file);

        parsers.add(debug);
        parsers.add(truAntiLag);
    }

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        for (LogProvider provider : providers) {
            CompletableFuture<String> futureLog = provider.provide(messageCreateEvent.getMessage());
            futureLog.thenAcceptAsync(log -> {
                for (LogParser parser : parsers) {
                    parser.evaluate(messageCreateEvent.getMessage(), new Scanner(log));
                }
            });
        }
    }
}

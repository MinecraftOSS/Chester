package org.moss.discord.listeners.parser.provider;

import org.javacord.api.entity.message.Message;

import java.util.concurrent.CompletableFuture;

public interface LogProvider {

    CompletableFuture<String> provide(Message message);

}

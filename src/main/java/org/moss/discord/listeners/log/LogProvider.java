package org.moss.discord.listeners.log;

import org.javacord.api.entity.message.Message;

import java.util.concurrent.CompletableFuture;

public interface LogProvider {

    CompletableFuture<String> provide(Message message);

}

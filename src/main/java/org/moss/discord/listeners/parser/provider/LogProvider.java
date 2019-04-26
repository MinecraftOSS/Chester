package org.moss.discord.listeners.parser.provider;

import org.javacord.api.entity.message.Message;

public interface LogProvider {

    String provide(Message message);

}

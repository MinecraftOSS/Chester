package org.moss.discord.listeners.parser.provider;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;

import java.util.concurrent.ExecutionException;

public class FileProvider implements LogProvider {

    @Override
    public String provide(Message message) {
        for (MessageAttachment attachment : message.getAttachments()) {
            if (attachment.getFileName().contains("log")) {
                try {
                    byte[] log = attachment.downloadAsByteArray().get();
                    return new String(log);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}

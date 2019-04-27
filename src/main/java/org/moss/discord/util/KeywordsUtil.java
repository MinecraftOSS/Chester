package org.moss.discord.util;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.time.Instant;

public class KeywordsUtil {

    private String template;
    private User user;
    private Server server;
    private String[] args;

    public KeywordsUtil(String template, User user, Server server, String... args) {
        this.template = template;
        this.user = user;
        this.server = server;
        this.args = args;
    }

    public String replace() {
        return template
                .replace("{USER}", user.getDisplayName(server))
                .replace("{USER_TAG}", user.getMentionTag())
                .replace("{TIMESTAMP_NOW}", Instant.now().toString())
                .replace("{ARGS}", String.join(" ", args));
    }
}

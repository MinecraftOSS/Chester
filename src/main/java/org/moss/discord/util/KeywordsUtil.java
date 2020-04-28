package org.moss.discord.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;

public class KeywordsUtil {

    private String template;
    private Member user;
    private Guild server;
    private String[] args;

    public KeywordsUtil(String template, Member user, Guild server, String... args) {
        this.template = template;
        this.user = user;
        this.server = server;
        this.args = args;
    }

    public String replace() {
        return template
                .replace("{USER}", user.getEffectiveName())
                .replace("{USER_TAG}", user.getAsMention())
                .replace("{TIMESTAMP_NOW}", Instant.now().toString())
                .replace("{ARGS}", String.join(" ", args))
                .replace("{USER_COUNT}", String.valueOf(server.getMemberCount()));
    }
}

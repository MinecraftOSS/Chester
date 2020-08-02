package org.moss.discord.storage;

@Deprecated
public class StarboardStorage extends KeyValStorage {

    public StarboardStorage() {
        super("./starboard.yml");
    }

    public boolean set(String key, String value) {
        boolean replaced = super.set(key, value);
        this.saveYaml();
        return replaced;
    }

    public boolean isStarred(long messageId) {
        return exists(Long.toString(messageId));
    }

    public boolean isStarboardMessage(long messageId) {
        return existsValue(Long.toString(messageId));
    }

}

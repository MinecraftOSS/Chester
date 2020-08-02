package org.moss.discord.storage;

@Deprecated
public class RolePollStorage extends KeyValStorage {

    public RolePollStorage() {
        super("./rolepoll.yml");
    }

    public boolean set(String key, String value) {
        boolean replaced = super.set(key, value);
        this.saveYaml();
        return replaced;
    }

    public boolean ispoll(long messageId) {
        return exists(Long.toString(messageId));
    }

    public String getChannel(String messageId) {
        return get(messageId).toString();
    }
}

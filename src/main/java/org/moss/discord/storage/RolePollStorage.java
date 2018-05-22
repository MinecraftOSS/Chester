package org.moss.discord.storage;

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

    public String getRole(long messageId) {
        return get(Long.toString(messageId)).toString();
    }
}

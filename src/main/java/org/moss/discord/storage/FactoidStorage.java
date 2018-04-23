package org.moss.discord.storage;

public class FactoidStorage extends KeyValStorage {

    public FactoidStorage() {
        super("./factoids.yml");
    }

    public boolean set(String key, String value) {
        boolean replaced = super.set(key, value);
        this.saveYaml();
        return replaced;
    }

    public boolean isFactoid(String args) {
        return exists(args);
    }

    public String getTag(String key) {
        return get(key).toString();
    }
}
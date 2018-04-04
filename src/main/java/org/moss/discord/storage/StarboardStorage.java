package org.moss.discord.storage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class StarboardStorage extends KeyValStorage {

    public StarboardStorage() {
        super("./starboard.yml");
    }

    @Override
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

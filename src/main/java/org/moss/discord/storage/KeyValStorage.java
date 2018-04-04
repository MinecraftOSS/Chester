package org.moss.discord.storage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class KeyValStorage {
    
    private Yaml yaml = new Yaml();
    private Logger logger = LoggerFactory.getLogger(KeyValStorage.class);

    private Map<String, String> kvMap;
    private File kvFile;

    public KeyValStorage(String path) {
        kvFile = new File(path);
        loadYaml();
    }

    @SuppressWarnings("unchecked")
    protected synchronized void loadYaml() {
        try {
            kvFile.createNewFile();
            kvMap = (Map<String, String>) yaml.load(new FileReader(kvFile));
        } catch (IOException e) {
            logger.error("Could not load key-value file!", e);
        }

        if (kvMap == null) kvMap = new HashMap<String, String>();

        logger.debug("Loaded from {}: {}", kvFile.getPath(), kvMap);
    }

    protected synchronized void saveYaml() {
        try {
            yaml.dump(kvMap, new FileWriter(kvFile));
        } catch (IOException e) {
            logger.error("Could not save key-value file!", e);
        }
    }

    public boolean exists(String key) {
        return kvMap.containsKey(key);
    }

    public boolean existsValue(String value) {
        return kvMap.containsValue(value);
    }

    public boolean set(String key, String value) {
        boolean hasKey = exists(key);
        kvMap.put(key, value);
        return hasKey;
    }

    public String get(String key) {
        return kvMap.get(key);
    }

    public Map<String, String> getMap() {
        return Collections.unmodifiableMap(kvMap);
    }

}

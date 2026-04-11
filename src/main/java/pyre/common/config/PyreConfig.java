package pyre.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pyre.common.platform.PyrePlatformHolder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PyreConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("Pyre/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static volatile PyreConfig cached;

    public PyreServerConfig server = new PyreServerConfig();
    public PyreClientConfig client = new PyreClientConfig();

    public static PyreConfig load() {
        PyreConfig existing = cached;
        if (existing != null) {
            return existing;
        }

        PyreConfig loaded = loadFromDisk();
        cached = loaded;
        return loaded;
    }

    private static PyreConfig loadFromDisk() {
        PyreConfig defaults = new PyreConfig();
        Path configPath = configPath();

        if (Files.notExists(configPath)) {
            defaults.save();
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            PyreConfig loaded = GSON.fromJson(reader, PyreConfig.class);
            if (loaded == null) {
                defaults.save();
                return defaults;
            }

            if (loaded.server == null) {
                loaded.server = new PyreServerConfig();
            }
            if (loaded.client == null) {
                loaded.client = new PyreClientConfig();
            }

            loaded.server.maxClusterRadius = Math.max(0.0D, loaded.server.maxClusterRadius);
            return loaded;
        } catch (IOException | JsonParseException exception) {
            LOGGER.warn("Failed to load {}, falling back to conservative defaults", configPath, exception);
            defaults.save();
            return defaults;
        }
    }

    public void save() {
        Path configPath = configPath();
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            LOGGER.warn("Failed to write {}", configPath, exception);
        }
    }

    private static Path configPath() {
        return PyrePlatformHolder.getPlatform().getConfigDir().resolve("pyre.json");
    }
}

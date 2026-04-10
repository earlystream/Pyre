package pyre.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PyreConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("Pyre/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("pyre.json");

    public boolean enabled = true;
    public boolean strictCompatibilityMode = true;
    public boolean enableExplosionQueryCache = true;
    public boolean enableClusterReuse = true;
    public double maxClusterRadius = 4.0D;
    public boolean debugLogging = false;
    public boolean profilerMarkers = false;
    public boolean autoDisableRiskyPathsWithKnownMods = true;

    public static PyreConfig load() {
        PyreConfig defaults = new PyreConfig();

        if (Files.notExists(CONFIG_PATH)) {
            defaults.save();
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            PyreConfig loaded = GSON.fromJson(reader, PyreConfig.class);
            if (loaded == null) {
                defaults.save();
                return defaults;
            }

            loaded.maxClusterRadius = Math.max(0.0D, loaded.maxClusterRadius);
            return loaded;
        } catch (IOException | JsonParseException exception) {
            LOGGER.warn("Failed to load {}, falling back to conservative defaults", CONFIG_PATH, exception);
            defaults.save();
            return defaults;
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            LOGGER.warn("Failed to write {}", CONFIG_PATH, exception);
        }
    }
}

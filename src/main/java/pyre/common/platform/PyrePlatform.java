package pyre.common.platform;

import java.nio.file.Path;

/**
 * Loader bridge for the small number of services Pyre needs outside the game runtime itself.
 * Shared explosion logic stays loader-agnostic and fails safe if a platform implementation is
 * missing.
 */
public interface PyrePlatform {
    Path getConfigDir();

    boolean isModLoaded(String modId);

    String getLoaderName();
}

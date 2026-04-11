package pyre.common.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public record CompatFlags(
        boolean sodium,
        boolean lithium,
        boolean krypton,
        boolean badOptimizations,
        boolean ferriteCore,
        boolean immediatelyFast,
        boolean modernFix,
        boolean noisium,
        boolean entityCulling,
        boolean moreCulling
) {
    public boolean hasAnyKnownOptimizationMod() {
        return sodium || lithium || krypton || badOptimizations || ferriteCore || immediatelyFast || modernFix || noisium || entityCulling || moreCulling;
    }

    public boolean hasKnownRenderOptimizationMod() {
        return sodium || immediatelyFast || entityCulling || moreCulling;
    }

    public List<String> loadedModIds() {
        List<String> loaded = new ArrayList<>();
        addIfLoaded(loaded, sodium, "sodium");
        addIfLoaded(loaded, lithium, "lithium");
        addIfLoaded(loaded, krypton, "krypton");
        addIfLoaded(loaded, badOptimizations, "badoptimizations");
        addIfLoaded(loaded, ferriteCore, "ferritecore");
        addIfLoaded(loaded, immediatelyFast, "immediatelyfast");
        addIfLoaded(loaded, modernFix, "modernfix");
        addIfLoaded(loaded, noisium, "noisium");
        addIfLoaded(loaded, entityCulling, "entityculling");
        addIfLoaded(loaded, moreCulling, "moreculling");
        return loaded;
    }

    public String summary() {
        if (!hasAnyKnownOptimizationMod()) {
            return "no known optimization mods detected";
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (String modId : loadedModIds()) {
            joiner.add(modId);
        }
        return joiner.toString();
    }

    private static void addIfLoaded(List<String> loaded, boolean present, String modId) {
        if (present) {
            loaded.add(modId);
        }
    }
}

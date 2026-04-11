package pyre.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class PyreVersionedMixinPlugin implements IMixinConfigPlugin {
    private static final VersionFamily ACTIVE_FAMILY = detectFamily();

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return mixinClassName.contains("." + ACTIVE_FAMILY.packageToken() + ".");
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private static VersionFamily detectFamily() {
        String version = FabricLoader.getInstance()
                .getModContainer("minecraft")
                .orElseThrow(() -> new IllegalStateException("Pyre could not resolve the Minecraft version"))
                .getMetadata()
                .getVersion()
                .getFriendlyString();

        return switch (version) {
            case "1.21", "1.21.1" -> VersionFamily.V1210_1211;
            case "1.21.2", "1.21.3", "1.21.4" -> VersionFamily.V1212_1214;
            case "1.21.5", "1.21.6", "1.21.7", "1.21.8" -> VersionFamily.V1217;
            case "1.21.9", "1.21.10", "1.21.11" -> VersionFamily.V12111;
            default -> throw new IllegalStateException("Pyre 1.21.x build does not support Minecraft " + version);
        };
    }

    private enum VersionFamily {
        V1210_1211("v1210_1211"),
        V1212_1214("v1212_1214"),
        V1217("v1217"),
        V12111("v12111");

        private final String packageToken;

        VersionFamily(String packageToken) {
            this.packageToken = packageToken;
        }

        public String packageToken() {
            return this.packageToken;
        }
    }
}

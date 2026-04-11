package pyre.mixin.v1210_1211;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyre.common.compat.ExplosionCompat;
import pyre.common.compat.LegacyWorldCompat;
import pyre.common.manager.PyreExplosionManager;

import java.util.List;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    /**
     * 1.21 and 1.21.1 still run the vanilla server explosion pipeline through
     * Explosion.collectBlocksAndDamageEntities(). Pyre only brackets that vanilla work for
     * bookkeeping and never replaces the actual blast simulation.
     */
    @Inject(method = "method_8348", at = @At("HEAD"), remap = false)
    private void pyre$onExplosionStart(CallbackInfo ci) {
        PyreExplosionManager.INSTANCE.onExplosionStart((Explosion) (Object) this);
    }

    @Inject(method = "method_8348", at = @At("RETURN"), remap = false)
    private void pyre$onExplosionEnd(CallbackInfo ci) {
        PyreExplosionManager.INSTANCE.onExplosionEnd((Explosion) (Object) this);
    }

    /**
     * The nearby-entity query in the old explosion pipeline still routes through
     * World.getOtherEntities(Entity, Box). Pyre only reuses exact-box support data when the
     * same-tick safety rules pass; otherwise this falls straight back to vanilla.
     */
    @Redirect(
            method = "method_8348",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_1937;method_8335(Lnet/minecraft/class_1297;Lnet/minecraft/class_238;)Ljava/util/List;",
                    remap = false
            ),
            remap = false
    )
    private List<Entity> pyre$reuseNearbyEntityQuery(World world, Entity except, Box box) {
        Explosion explosion = (Explosion) (Object) this;
        return PyreExplosionManager.INSTANCE.queryNearbyEntities(ExplosionCompat.world(explosion), explosion, except, box, () -> LegacyWorldCompat.getOtherEntities(world, except, box));
    }
}

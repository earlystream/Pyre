package pyre.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyre.manager.PyreExplosionManager;

import java.util.List;

@Mixin(ExplosionImpl.class)
public abstract class ExplosionImplMixin {
    /**
     * 1.21.11-specific hook: ExplosionImpl.explode() is the narrowest stable lifecycle point
     * that surrounds the full vanilla explosion pipeline without replacing any vanilla logic.
     */
    @Inject(method = "explode", at = @At("HEAD"))
    private void pyre$onExplosionStart(CallbackInfoReturnable<Integer> cir) {
        PyreExplosionManager.INSTANCE.onExplosionStart((Explosion) (Object) this);
    }

    /**
     * Matching end hook for the same 1.21.11 method. Pyre only closes bookkeeping here and
     * never changes the returned explosion result.
     */
    @Inject(method = "explode", at = @At("RETURN"))
    private void pyre$onExplosionEnd(CallbackInfoReturnable<Integer> cir) {
        PyreExplosionManager.INSTANCE.onExplosionEnd((Explosion) (Object) this, cir.getReturnValue());
    }

    /**
     * 1.21.11-specific call site inside ExplosionImpl.damageEntities():
     * ServerWorld.getOtherEntities(Entity, Box). Pyre may reuse only exact-box support data
     * when its safety rules allow it, otherwise this falls straight back to vanilla.
     */
    @Redirect(
            method = "damageEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"
            )
    )
    private List<Entity> pyre$reuseNearbyEntityQuery(ServerWorld world, Entity except, Box box) {
        Explosion explosion = (Explosion) (Object) this;
        return PyreExplosionManager.INSTANCE.queryNearbyEntities(world, explosion, except, box, () -> world.getOtherEntities(except, box));
    }
}

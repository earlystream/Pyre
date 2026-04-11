package pyre.mixin.v1217;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyre.common.manager.PyreExplosionManager;

import java.util.List;

@Mixin(ExplosionImpl.class)
public abstract class ExplosionImplMixin {
    /**
     * 1.21.5 through 1.21.8 keep ExplosionImpl, but explode() returns void rather than the later
     * integer result. Pyre only brackets vanilla execution for bookkeeping here.
     */
    @Inject(method = "explode", at = @At("HEAD"))
    private void pyre$onExplosionStart(CallbackInfo ci) {
        PyreExplosionManager.INSTANCE.onExplosionStart((Explosion) (Object) this);
    }

    @Inject(method = "explode", at = @At("RETURN"))
    private void pyre$onExplosionEnd(CallbackInfo ci) {
        PyreExplosionManager.INSTANCE.onExplosionEnd((Explosion) (Object) this);
    }

    /**
     * 1.21.5 through 1.21.8 route the nearby-entity lookup through
     * ServerWorld#getOtherEntities. The cache still only reuses exact-box support data and falls
     * straight back to vanilla if safety checks fail.
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

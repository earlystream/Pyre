package pyre.client.mixin.v1210_1211;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pyre.client.manager.ClientExplosionEffectManager;
import pyre.client.compat.LegacyClientEffectCompat;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    /**
     * 1.21 and 1.21.1 emit local explosion sounds inside Explosion.affectWorld(true) after the
     * packet has already been accepted. Pyre only suppresses duplicate same-tick sounds.
     */
    @Redirect(
            method = "method_8350",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_1937;method_8486(DDDLnet/minecraft/class_3414;Lnet/minecraft/class_3419;FFZ)V",
                    remap = false
            ),
            remap = false
    )
    private void pyre$coalesceExplosionSound(World world, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
        if (ClientExplosionEffectManager.INSTANCE.shouldPlaySound()) {
            LegacyClientEffectCompat.playExplosionSound(world, x, y, z, sound, category, volume, pitch, useDistance);
        }
    }

    /**
     * The old client path emits the primary explosion particle inside Explosion.affectWorld(true).
     * Pyre only budgets that local effect after the packet result is already known.
     */
    @Redirect(
            method = "method_8350",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_1937;method_8406(Lnet/minecraft/class_2394;DDDDDD)V",
                    remap = false
            ),
            remap = false
    )
    private void pyre$budgetExplosionPrimaryParticle(World world, ParticleEffect effect, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if (ClientExplosionEffectManager.INSTANCE.shouldSpawnPrimaryParticle()) {
            LegacyClientEffectCompat.addExplosionParticle(world, effect, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

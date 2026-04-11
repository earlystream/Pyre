package pyre.client.mixin.v12111;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyre.client.manager.ClientExplosionEffectManager;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    /**
     * 1.21.9 through 1.21.11 use the later client packet hook family: explosion packets are
     * already authoritative by the time they reach ClientPlayNetworkHandler. Pyre only derives a
     * local effect-emission plan here.
     */
    @Inject(method = "onExplosion", at = @At("HEAD"))
    private void pyre$beginExplosionPacket(ExplosionS2CPacket packet, CallbackInfo ci) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null) {
            ClientExplosionEffectManager.INSTANCE.beginExplosionPacket(world, packet);
        }
    }

    @Inject(method = "onExplosion", at = @At("RETURN"))
    private void pyre$endExplosionPacket(ExplosionS2CPacket packet, CallbackInfo ci) {
        ClientExplosionEffectManager.INSTANCE.endExplosionPacket();
    }

    /**
     * Preserves packet semantics and server authority. Pyre only suppresses duplicate local
     * explosion sounds in tiny same-tick windows when the client effect manager says it is safe.
     */
    @Redirect(
            method = "onExplosion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/world/ClientWorld;playSoundClient(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"
            )
    )
    private void pyre$coalesceExplosionSound(ClientWorld world, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
        if (ClientExplosionEffectManager.INSTANCE.shouldPlaySound()) {
            world.playSoundClient(x, y, z, sound, category, volume, pitch, useDistance);
        }
    }

    /**
     * Preserves vanilla outcomes because this only affects the local primary explosion particle
     * after the server result is already known.
     */
    @Redirect(
            method = "onExplosion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/world/ClientWorld;addParticleClient(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"
            )
    )
    private void pyre$budgetExplosionPrimaryParticle(ClientWorld world, ParticleEffect effect, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if (ClientExplosionEffectManager.INSTANCE.shouldSpawnPrimaryParticle()) {
            world.addParticleClient(effect, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    /**
     * The block-particle burst is the heaviest local explosion effect path in this later family
     * because it feeds the client block-particle effects manager. Pyre only suppresses duplicate
     * bursts for clustered packets and otherwise delegates to vanilla unchanged.
     */
    @Redirect(
            method = "onExplosion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/world/ClientWorld;addBlockParticleEffects(Lnet/minecraft/util/math/Vec3d;FILnet/minecraft/util/collection/Pool;)V"
            )
    )
    private void pyre$dedupeTrackedExplosionEffects(ClientWorld world, Vec3d center, float radius, int blockCount, Pool blockParticles) {
        if (ClientExplosionEffectManager.INSTANCE.shouldTrackBlockEffects()) {
            world.addBlockParticleEffects(center, radius, blockCount, blockParticles);
        }
    }
}

package pyre.client.mixin.v1212_1214;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyre.client.compat.LegacyClientEffectCompat;
import pyre.client.manager.ClientExplosionEffectManager;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    /**
     * 1.21.2 through 1.21.4 already use the packet-local client effect path in
     * ClientPlayNetworkHandler.onExplosion. Pyre only derives a local effect plan here.
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
     * This early packet-local path still calls ClientWorld.playSound(...) rather than
     * playSoundClient(...). Pyre only suppresses duplicate local sounds in tiny same-tick windows.
     */
    @Redirect(
            method = "onExplosion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/world/ClientWorld;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"
            )
    )
    private void pyre$coalesceExplosionSound(ClientWorld world, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
        if (ClientExplosionEffectManager.INSTANCE.shouldPlaySound()) {
            LegacyClientEffectCompat.playExplosionSound(world, x, y, z, sound, category, volume, pitch, useDistance);
        }
    }

    /**
     * 1.21.2 through 1.21.4 also emit only the primary packet particle directly here, with no
     * later tracked block-particle helper.
     */
    @Redirect(
            method = "onExplosion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/world/ClientWorld;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"
            )
    )
    private void pyre$budgetExplosionPrimaryParticle(ClientWorld world, ParticleEffect effect, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if (ClientExplosionEffectManager.INSTANCE.shouldSpawnPrimaryParticle()) {
            LegacyClientEffectCompat.addExplosionParticle(world, effect, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

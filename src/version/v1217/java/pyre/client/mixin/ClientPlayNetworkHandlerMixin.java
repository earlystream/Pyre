package pyre.client.mixin.v1217;

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
import pyre.client.manager.ClientExplosionEffectManager;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    /**
     * 1.21.5 through 1.21.8 still receive authoritative explosion packets through
     * ClientPlayNetworkHandler. Pyre only derives a local effect-emission plan and never changes
     * packet semantics.
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
     * 1.21.5 through 1.21.8 use ClientWorld.playSoundClient(...) on the packet effect path. Pyre
     * only suppresses duplicate local explosion sounds in tiny same-tick windows.
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
     * 1.21.5 through 1.21.8 do not expose the later block-particle helper method from the newer
     * family, so this group only budgets the primary particle and otherwise leaves local effects
     * vanilla.
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
}

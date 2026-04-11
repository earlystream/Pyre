package pyre.client.mixin.v1210_1211;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyre.client.manager.ClientExplosionEffectManager;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    /**
     * 1.21 and 1.21.1 still deliver authoritative explosion packets through ClientPlayNetworkHandler,
     * but the local effects are emitted later through Explosion.affectWorld(true). Pyre only opens
     * and closes a local effect plan here; packet semantics stay untouched.
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
}

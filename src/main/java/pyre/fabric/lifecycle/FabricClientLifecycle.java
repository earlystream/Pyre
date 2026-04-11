package pyre.fabric.lifecycle;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import pyre.client.manager.ClientExplosionEffectManager;

/**
 * Fabric client lifecycle adapter for purely local explosion effect smoothing. This remains
 * strictly cosmetic and is isolated from dedicated-server classloading.
 */
public final class FabricClientLifecycle {
    private FabricClientLifecycle() {
    }

    public static void register() {
        ClientTickEvents.START_WORLD_TICK.register(ClientExplosionEffectManager.INSTANCE::onWorldTickStart);
        ClientTickEvents.END_WORLD_TICK.register(ClientExplosionEffectManager.INSTANCE::onWorldTickEnd);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientExplosionEffectManager.INSTANCE.onDisconnect());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ClientExplosionEffectManager.INSTANCE.onDisconnect());
    }
}

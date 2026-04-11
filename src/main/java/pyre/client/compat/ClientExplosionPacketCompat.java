package pyre.client.compat;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Method;

public final class ClientExplosionPacketCompat {
    private static final Method CENTER_METHOD = firstPresent(findMethod("center"), findMethod("comp_2883"));
    private static final Method RADIUS_METHOD = firstPresent(
            findMethod("radius"),
            findMethod("comp_4594"),
            findMethod("getRadius"),
            findMethod("method_11476")
    );
    private static final Method GET_X_METHOD = firstPresent(findMethod("getX"), findMethod("method_11475"));
    private static final Method GET_Y_METHOD = firstPresent(findMethod("getY"), findMethod("method_11477"));
    private static final Method GET_Z_METHOD = firstPresent(findMethod("getZ"), findMethod("method_11478"));

    private ClientExplosionPacketCompat() {
    }

    public static Vec3d center(ExplosionS2CPacket packet) {
        if (CENTER_METHOD != null) {
            return (Vec3d) invoke(CENTER_METHOD, packet);
        }

        if (GET_X_METHOD != null && GET_Y_METHOD != null && GET_Z_METHOD != null) {
            return new Vec3d(
                    ((Double) invoke(GET_X_METHOD, packet)).doubleValue(),
                    ((Double) invoke(GET_Y_METHOD, packet)).doubleValue(),
                    ((Double) invoke(GET_Z_METHOD, packet)).doubleValue()
            );
        }

        throw new IllegalStateException("Pyre could not resolve an explosion packet center for the active 1.21.x runtime");
    }

    public static float radius(ExplosionS2CPacket packet) {
        if (RADIUS_METHOD == null) {
            return 0.0F;
        }

        return ((Float) invoke(RADIUS_METHOD, packet)).floatValue();
    }

    private static Object invoke(Method method, ExplosionS2CPacket packet) {
        try {
            return method.invoke(packet);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Pyre failed to invoke an explosion packet compatibility accessor", exception);
        }
    }

    private static Method firstPresent(Method... methods) {
        for (Method method : methods) {
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    private static Method findMethod(String name) {
        try {
            Method method = ExplosionS2CPacket.class.getDeclaredMethod(name);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}

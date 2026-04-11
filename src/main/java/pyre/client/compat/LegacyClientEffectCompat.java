package pyre.client.compat;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import java.lang.reflect.Method;

public final class LegacyClientEffectCompat {
    private static final String[] SOUND_METHOD_NAMES = new String[]{"playSound", "method_8486"};
    private static final String[] PARTICLE_METHOD_NAMES = new String[]{"addParticle", "method_8406"};
    private static final Class<?>[] SOUND_SIGNATURE = new Class<?>[]{
            double.class, double.class, double.class, SoundEvent.class, SoundCategory.class, float.class, float.class, boolean.class
    };
    private static final Class<?>[] PARTICLE_SIGNATURE = new Class<?>[]{
            ParticleEffect.class, double.class, double.class, double.class, double.class, double.class, double.class
    };

    private LegacyClientEffectCompat() {
    }

    public static void playExplosionSound(Object world, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
        Method method = findMethod(world.getClass(), SOUND_SIGNATURE, SOUND_METHOD_NAMES);
        if (method == null) {
            throw new IllegalStateException("Pyre could not find the legacy explosion sound method for the active 1.21.x runtime");
        }

        invoke(method, world, x, y, z, sound, category, volume, pitch, useDistance);
    }

    public static void addExplosionParticle(Object world, ParticleEffect effect, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Method method = findMethod(world.getClass(), PARTICLE_SIGNATURE, PARTICLE_METHOD_NAMES);
        if (method == null) {
            throw new IllegalStateException("Pyre could not find the legacy explosion particle method for the active 1.21.x runtime");
        }

        invoke(method, world, effect, x, y, z, velocityX, velocityY, velocityZ);
    }

    private static void invoke(Method method, Object owner, Object... arguments) {
        try {
            method.invoke(owner, arguments);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Pyre failed to invoke a legacy client explosion effect method", exception);
        }
    }

    private static Method findMethod(Class<?> type, Class<?>[] parameterTypes, String... names) {
        Class<?> current = type;
        while (current != null) {
            Method method = findDeclaredMethod(current, parameterTypes, names);
            if (method != null) {
                return method;
            }

            for (Class<?> iface : current.getInterfaces()) {
                method = findMethodInInterface(iface, parameterTypes, names);
                if (method != null) {
                    return method;
                }
            }

            current = current.getSuperclass();
        }
        return null;
    }

    private static Method findDeclaredMethod(Class<?> owner, Class<?>[] parameterTypes, String... names) {
        for (String name : names) {
            try {
                Method method = owner.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private static Method findMethodInInterface(Class<?> iface, Class<?>[] parameterTypes, String... names) {
        Method method = findDeclaredMethod(iface, parameterTypes, names);
        if (method != null) {
            return method;
        }

        for (Class<?> parent : iface.getInterfaces()) {
            method = findMethodInInterface(parent, parameterTypes, names);
            if (method != null) {
                return method;
            }
        }

        return null;
    }
}

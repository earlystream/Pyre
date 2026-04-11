package pyre.common.compat;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ExplosionCompat {
    private static final String[] WORLD_METHOD_NAMES = {"getWorld", "method_64504"};
    private static final String[] WORLD_FIELD_NAMES = {"world", "field_9187"};
    private static final String[] POSITION_METHOD_NAMES = {"getPosition", "method_55109"};
    private static final String[] POWER_METHOD_NAMES = {"getPower", "method_55107"};

    private static final Map<Class<?>, Method> WORLD_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method> POSITION_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method> POWER_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> WORLD_FIELD_CACHE = new ConcurrentHashMap<>();

    private ExplosionCompat() {
    }

    public static ServerWorld world(Object explosion) {
        Method worldMethod = WORLD_METHOD_CACHE.computeIfAbsent(explosion.getClass(), type -> findMethod(type, WORLD_METHOD_NAMES));
        Object result = invoke(worldMethod, explosion);
        if (result instanceof ServerWorld serverWorld) {
            return serverWorld;
        }

        Field worldField = WORLD_FIELD_CACHE.computeIfAbsent(explosion.getClass(), type -> findField(type, WORLD_FIELD_NAMES));
        if (worldField != null) {
            try {
                Object fieldValue = worldField.get(explosion);
                if (fieldValue instanceof ServerWorld serverWorld) {
                    return serverWorld;
                }
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Pyre could not read the legacy explosion world field", exception);
            }
        }

        throw new IllegalStateException("Pyre could not resolve an explosion world for the current 1.21.x runtime");
    }

    public static Vec3d position(Object explosion) {
        Method positionMethod = POSITION_METHOD_CACHE.computeIfAbsent(explosion.getClass(), type -> findMethod(type, POSITION_METHOD_NAMES));
        Object result = invokeRequired(positionMethod, explosion, "explosion position");
        return (Vec3d) result;
    }

    public static float power(Object explosion) {
        Method powerMethod = POWER_METHOD_CACHE.computeIfAbsent(explosion.getClass(), type -> findMethod(type, POWER_METHOD_NAMES));
        Object result = invokeRequired(powerMethod, explosion, "explosion power");
        return ((Float) result).floatValue();
    }

    private static Object invokeRequired(Method method, Object explosion, String label) {
        if (method == null) {
            throw new IllegalStateException("Pyre is missing the reflected " + label + " accessor for this 1.21.x runtime");
        }

        return invoke(method, explosion);
    }

    private static Object invoke(Method method, Object explosion) {
        if (method == null) {
            return null;
        }

        try {
            return method.invoke(explosion);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Pyre failed to invoke an explosion compatibility accessor", exception);
        }
    }

    private static Method findMethod(Class<?> owner, String... names) {
        Class<?> current = owner;
        while (current != null) {
            Method method = findDeclaredMethod(current, names);
            if (method != null) {
                return method;
            }

            for (Class<?> iface : current.getInterfaces()) {
                method = findMethodInInterface(iface, names);
                if (method != null) {
                    return method;
                }
            }

            current = current.getSuperclass();
        }
        return null;
    }

    private static Method findDeclaredMethod(Class<?> owner, String... names) {
        for (String name : names) {
            try {
                Method method = owner.getDeclaredMethod(name);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private static Method findMethodInInterface(Class<?> iface, String... names) {
        Method method = findDeclaredMethod(iface, names);
        if (method != null) {
            return method;
        }

        for (Class<?> parent : iface.getInterfaces()) {
            method = findMethodInInterface(parent, names);
            if (method != null) {
                return method;
            }
        }

        return null;
    }

    private static Field findField(Class<?> owner, String... names) {
        for (String name : names) {
            Class<?> current = owner;
            while (current != null) {
                try {
                    Field field = current.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                    current = current.getSuperclass();
                }
            }
        }
        return null;
    }
}

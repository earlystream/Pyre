package pyre.common.compat;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

import java.lang.reflect.Method;
import java.util.List;

public final class LegacyWorldCompat {
    private static final Class<?>[] ENTITY_QUERY_SIGNATURE = new Class<?>[]{Entity.class, Box.class};
    private static final String[] ENTITY_QUERY_METHOD_NAMES = new String[]{"getOtherEntities", "method_8335", "a_"};

    private LegacyWorldCompat() {
    }

    @SuppressWarnings("unchecked")
    public static List<Entity> getOtherEntities(Object world, Entity except, Box box) {
        Method method = findMethod(world.getClass(), ENTITY_QUERY_SIGNATURE, ENTITY_QUERY_METHOD_NAMES);
        if (method == null) {
            throw new IllegalStateException("Pyre could not find legacy World#getOtherEntities(Entity, Box) for the active 1.21.x runtime");
        }

        try {
            return (List<Entity>) method.invoke(world, except, box);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Pyre failed to invoke legacy World#getOtherEntities(Entity, Box)", exception);
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

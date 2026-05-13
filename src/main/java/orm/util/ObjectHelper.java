package orm.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ObjectHelper {

    public static String[] getFields(Object entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        List<String> names = new ArrayList<>();
        for (Field field : fields) {
            if (!Modifier.isTransient(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                names.add(field.getName());
            }
        }
        return names.toArray(new String[0]);
    }

    public static Object getter(Object instance, String propertyName) {
        String suffix = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        try {
            try {
                return instance.getClass().getMethod("get" + suffix).invoke(instance);
            } catch (NoSuchMethodException ignored) {
                return instance.getClass().getMethod("is" + suffix).invoke(instance);
            }
        } catch (Exception e) {
            throw new RuntimeException("No se ha podido leer la propiedad " + propertyName, e);
        }
    }

    public static void setter(Object instance, String propertyName, Object value) {
        String setterName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        try {
            for (Method method : instance.getClass().getMethods()) {
                if (method.getName().equals(setterName) && method.getParameterTypes().length == 1) {
                    method.invoke(instance, convertValue(value, method.getParameterTypes()[0]));
                    return;
                }
            }
            throw new NoSuchMethodException(setterName);
        } catch (Exception e) {
            throw new RuntimeException("No se ha podido escribir la propiedad " + propertyName, e);
        }
    }

    private static Object convertValue(Object value, Class targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return ((Number) value).intValue();
        }
        if (targetType == double.class || targetType == Double.class) {
            return ((Number) value).doubleValue();
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            return ((Number) value).intValue() != 0;
        }
        if (targetType == String.class) {
            return value.toString();
        }
        return value;
    }
}

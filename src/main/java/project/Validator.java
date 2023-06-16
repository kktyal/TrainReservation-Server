package project;

import org.springframework.lang.Nullable;

import java.lang.reflect.Field;

public class Validator {
    public static <T> boolean isValid(T obj, String... names) {
        Class<T> cls = (Class<T>) obj.getClass();
        Class<?> superCls = cls.getSuperclass();
        while(superCls != null) {
            Boolean result = isValid(obj, superCls, names);
            if(result != null) {
                return result;
            }
            superCls = superCls.getSuperclass();
        }
        return Boolean.TRUE.equals(isValid(obj, cls, names));
    }

    public static <T> Boolean isValid(T obj, Class<?> cls, String[] names) {
        for(String name : names) {
            Field f = null;
            try {
                f = cls.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                System.out.println("NO_SUCHED_FIELD : "+e.getMessage());
            }
            if(f == null) {
                return null;
            }
            Class<?> t = f.getType();
            boolean canAccess = f.canAccess(obj);
            f.setAccessible(true);

            Boolean tmpResult = null;
            if(t == Integer.class) {
                Integer value = null;
                try {
                    value = (Integer) f.get(obj);
                } catch (IllegalAccessException e) {
                    System.out.println("ILLEGAL_ACCESS : "+e.getMessage());
                }
                tmpResult = isValidInteger(value);
            } else if(t == String.class) {
                String value = null;
                try {
                    value = (String) f.get(obj);
                } catch (IllegalAccessException e) {
                    System.out.println("ILLEGAL_ACCESS : "+e.getMessage());
                }
                tmpResult = isValidString(value);
            }
            f.setAccessible(canAccess);

            if(tmpResult == null) {
                continue;
            }
            return tmpResult;
        }
        return null;
    }

    public static boolean isValidInteger(@Nullable Integer integer) {
        return isValidInteger(integer, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static boolean isValidInteger(@Nullable Integer integer, @Nullable Integer min, @Nullable Integer max) {
        if (integer == null) {
            return false;
        }
        min = min != null ? min : Integer.MIN_VALUE;
        max = max != null ? max : Integer.MAX_VALUE;
        return integer > min && integer < max;
    }

    public static boolean isValidString(@Nullable String string) {
        return string != null && string.length() != 0;
    }
}
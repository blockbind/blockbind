package dev.cerus.unref;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

/**
 * Retrieves the 'Unsafe' instance
 */
public class UnsafeLocator {

    private static Unsafe unsafe;

    static {
        try {
            final Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            unsafe = (Unsafe) theUnsafeField.get(null);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Locate the 'Unsafe' instance
     *
     * @return The 'Unsafe' instance
     */
    public static Unsafe locateUnsafe() {
        return unsafe;
    }

}

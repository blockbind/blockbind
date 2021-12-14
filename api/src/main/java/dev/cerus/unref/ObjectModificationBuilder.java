package dev.cerus.unref;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import sun.misc.Unsafe;

/**
 * Object modification builder using reflection
 */
public class ObjectModificationBuilder {

    private final Object obj;
    private final Class<?> clazz;
    private Consumer<Throwable> throwableDelegate;

    public ObjectModificationBuilder(final Object obj) {
        this.obj = obj;
        this.clazz = obj.getClass();
    }

    public ObjectModificationBuilder(final Class<?> clazz) {
        this.obj = null;
        this.clazz = clazz;
    }

    /**
     * Delegate all throwables to the provided callback
     *
     * @param delegate The callback
     *
     * @return The builder
     */
    public ObjectModificationBuilder delegateThrowables(final Consumer<Throwable> delegate) {
        this.throwableDelegate = delegate;
        return this;
    }

    /**
     * Set a value to a field
     *
     * @param name The name of the field
     * @param val  The new value
     *
     * @return The builder
     */
    public ObjectModificationBuilder field(final String name, final Object val) {
        try {
            final Field field = this.clazz.getDeclaredField(name);
            field.setAccessible(true);
            if (Modifier.isFinal(field.getModifiers())) {
                this.setFinal(field, val);
            } else {
                field.set(this.obj, val);
            }
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            this.throwableDelegate.accept(e);
        }
        return this;
    }

    /**
     * Set a value to a field that's part of the object's superclass
     *
     * @param name The name of the field
     * @param val  The new value
     *
     * @return The builder
     */
    public ObjectModificationBuilder superField(final String name, final Object val) {
        try {
            final Field field = this.clazz.getSuperclass().getDeclaredField(name);
            field.setAccessible(true);
            if (Modifier.isFinal(field.getModifiers())) {
                this.setFinal(field, val);
            } else {
                field.set(this.obj, val);
            }
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            this.throwableDelegate.accept(e);
        }
        return this;
    }

    /**
     * Set a final field using 'Unsafe'
     * This is a mess, I don't really like this solution
     *
     * @param field The field to overwrite
     * @param val   The value to set
     */
    private void setFinal(final Field field, final Object val) {
        final boolean isStatic = this.obj == null;
        final Unsafe unsafe = UnsafeLocator.locateUnsafe();
        final long off = isStatic ? unsafe.staticFieldOffset(field) : unsafe.objectFieldOffset(field);
        if (val instanceof Integer) {
            if (isStatic) {
                unsafe.putInt(off, (int) val);
            } else {
                unsafe.putInt(this.obj, off, (int) val);
            }
        } else if (val instanceof Byte) {
            if (isStatic) {
                unsafe.putByte(off, (byte) val);
            } else {
                unsafe.putByte(this.obj, off, (byte) val);
            }
        } else if (val instanceof Long) {
            if (isStatic) {
                unsafe.putLong(off, (long) val);
            } else {
                unsafe.putLong(this.obj, off, (long) val);
            }
        } else if (val instanceof Short) {
            if (isStatic) {
                unsafe.putShort(off, (short) val);
            } else {
                unsafe.putShort(this.obj, off, (short) val);
            }
        } else if (val instanceof Double) {
            if (isStatic) {
                unsafe.putDouble(off, (double) val);
            } else {
                unsafe.putDouble(this.obj, off, (double) val);
            }
        } else if (val instanceof Float) {
            if (isStatic) {
                unsafe.putFloat(off, (float) val);
            } else {
                unsafe.putFloat(this.obj, off, (float) val);
            }
        } else if (val instanceof Character) {
            if (isStatic) {
                unsafe.putChar(off, (char) val);
            } else {
                unsafe.putChar(this.obj, off, (char) val);
            }
        } else if (val instanceof Boolean) {
            if (isStatic) {
                // ??
            } else {
                unsafe.putBoolean(this.obj, off, (boolean) val);
            }
        } else {
            if (isStatic) {
                // ??
            } else {
                unsafe.putObject(this.obj, off, val);
            }
        }
    }

    /**
     * Return the modified object
     *
     * @return The modified object
     */
    public Object finish() {
        return this.obj;
    }

}

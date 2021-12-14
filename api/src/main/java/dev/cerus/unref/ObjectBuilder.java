package dev.cerus.unref;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Consumer;
import sun.misc.Unsafe;

/**
 * Builder for instantiating objects through reflection
 */
public class ObjectBuilder {

    private final Class<?> clazz;
    private Consumer<Throwable> throwableDelegate;
    private Object obj;

    public ObjectBuilder(final Class<?> clazz) {
        this.clazz = clazz;
        this.throwableDelegate = Throwable::printStackTrace;
    }

    /**
     * Delegate all throwables to the provided callback
     *
     * @param delegate The callback
     *
     * @return The builder
     */
    public ObjectBuilder delegateThrowables(final Consumer<Throwable> delegate) {
        this.throwableDelegate = delegate;
        return this;
    }

    /**
     * Instantiate an object without calling any constructor using 'Unsafe'
     *
     * @return The builder
     */
    public ObjectBuilder unsafeInstantiate() {
        final Unsafe unsafe = UnsafeLocator.locateUnsafe();
        try {
            this.obj = unsafe.allocateInstance(this.clazz);
        } catch (final InstantiationException e) {
            this.throwableDelegate.accept(e);
        }
        return this;
    }

    /**
     * Instantiate an object with the provided constructor params
     *
     * @param params The constructor params
     *
     * @return The builder
     */
    public ObjectBuilder instantiate(final Object... params) {
        return this.instantiate(Arrays.stream(params)
                .map(Object::getClass)
                .toArray(Class[]::new), params);
    }

    /**
     * Instantiate an object with the provided constructor params
     *
     * @param paramTypes Param types
     * @param params     Param values
     *
     * @return The builder
     */
    public ObjectBuilder instantiate(final Class<?>[] paramTypes, final Object[] params) {
        try {
            final Constructor<?> constructor = this.clazz.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            this.obj = constructor.newInstance(params);
        } catch (final InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            this.throwableDelegate.accept(e);
        }
        return this;
    }

    /**
     * Pipe the created object to a new modification builder
     *
     * @return A new modification builder
     */
    public ObjectModificationBuilder modify() {
        return new ObjectModificationBuilder(this.obj);
    }

    /**
     * Get the created object
     *
     * @param <T> Type to cast to
     *
     * @return The created object
     */
    public <T> T get() {
        return (T) this.obj;
    }

}

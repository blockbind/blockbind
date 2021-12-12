package dev.cerus.unref;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Consumer;
import sun.misc.Unsafe;

public class ObjectBuilder {

    private final Class<?> clazz;
    private Consumer<Throwable> throwableDelegate;
    private Object obj;

    public ObjectBuilder(final Class<?> clazz) {
        this.clazz = clazz;
        this.throwableDelegate = Throwable::printStackTrace;
    }

    public ObjectBuilder delegateThrowables(final Consumer<Throwable> delegate) {
        this.throwableDelegate = delegate;
        return this;
    }

    public ObjectBuilder unsafeInstantiate() {
        final Unsafe unsafe = UnsafeLocator.locateUnsafe();
        try {
            this.obj = unsafe.allocateInstance(this.clazz);
        } catch (final InstantiationException e) {
            this.throwableDelegate.accept(e);
        }
        return this;
    }

    public ObjectBuilder instantiate(final Object... params) {
        return this.instantiate(Arrays.stream(params)
                .map(Object::getClass)
                .toArray(Class[]::new), params);
    }

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

    public ObjectModificationBuilder modify() {
        return new ObjectModificationBuilder(this.obj);
    }

    public <T> T get() {
        return (T) this.obj;
    }

}

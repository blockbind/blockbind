package dev.cerus.blockbind.api.identity;

import org.jetbrains.annotations.NotNull;

public abstract class Identity {

    private static Identity instance = null;

    public static Identity getIdentity() {
        return instance;
    }

    public static void setIdentity(@NotNull final Identity identity) {
        if (identity == null) {
            throw new IllegalStateException("Can't unset identity");
        }
        Identity.instance = identity;
    }

    public abstract String getName();

}

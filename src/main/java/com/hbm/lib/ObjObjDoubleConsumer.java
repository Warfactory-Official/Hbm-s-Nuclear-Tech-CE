package com.hbm.lib;

import java.util.Objects;

@FunctionalInterface
public interface ObjObjDoubleConsumer<T, U> {
    void accept(T t, U u, double value);

    default ObjObjDoubleConsumer<T, U> andThen(ObjObjDoubleConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (t, u, v) -> {
            accept(t, u, v);
            after.accept(t, u, v);
        };
    }
}

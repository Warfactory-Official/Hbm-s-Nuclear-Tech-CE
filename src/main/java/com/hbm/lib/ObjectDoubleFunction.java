package com.hbm.lib;

import java.util.function.Function;

public interface ObjectDoubleFunction<T> extends Function<T, Double> {
    double applyDouble(T t);

    @Override
    @Deprecated
    default Double apply(T t) {
        return applyDouble(t);
    }
}

package io.github.mjtb49.strongholdtrainer.util;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface TriFunction<Q,R,S,T> {

    T apply(Q q, R r, S s);

    default <V> TriFunction<Q,R,S,V> andThen(Function<? super T, ? extends V> after) {
        Objects.requireNonNull(after);
        return (Q q, R r, S s) -> after.apply(apply(q, r, s));
    }

}

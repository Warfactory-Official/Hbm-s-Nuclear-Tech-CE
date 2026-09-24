package com.hbm.util;

public final class ReferenceIntTuple<T> {
    public final T reference;
    public final int value;

    public ReferenceIntTuple(T reference, int value) {
        this.reference = reference;
        this.value = value;
    }

    public T getReference() {
        return reference;
    }

    public int getInt() {
        return value;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(reference) ^ value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ReferenceIntTuple<?> other)) return false;
        return other.reference == reference && other.value == value;
    }

    @Override
    public String toString() {
        return "(" + reference + ", " + value + ")";
    }
}

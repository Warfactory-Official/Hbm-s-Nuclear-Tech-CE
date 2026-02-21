package com.hbm.util;

public class ObjectIntPair<T> {
    public final T object;
    public final int i;

    public ObjectIntPair(T obj, int val) {
        this.object = obj;
        this.i = val;
    }

    @Override
    public int hashCode() {
        return object.hashCode() ^ i;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ObjectIntPair<?> p)) return false;
        return p.object.equals(object) && p.i == i;
    }

    @Override
    public String toString() {
        return "(" + object + ", " + i + ")";
    }
}

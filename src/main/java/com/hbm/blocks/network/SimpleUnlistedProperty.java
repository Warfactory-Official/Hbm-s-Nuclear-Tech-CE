package com.hbm.blocks.network;

import net.minecraftforge.common.property.IUnlistedProperty;
// I do not appreciate llm making dozens of this fucking class so I'll put it here
public class SimpleUnlistedProperty<T> implements IUnlistedProperty<T> {
    private final String name;
    private final Class<T> type;

    public SimpleUnlistedProperty(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(T value) {
        return true;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public String valueToString(T value) {
        return String.valueOf(value);
    }
}

package com.zergatul.freecam;

import net.minecraft.resources.ResourceLocation;

public interface WrappedRegistry<T> {
    ResourceLocation getKey(T value);
    T getValue(ResourceLocation id);
}
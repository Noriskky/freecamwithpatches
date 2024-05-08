package com.zergatul.freecam;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class ModApiWrapper {

    public static final ModApiWrapper instance = new ModApiWrapper();

    public final WrappedRegistry<Block> BLOCKS = new VanillaWrapperRegistry<>(Registry.BLOCK);

    private ModApiWrapper() {

    }

    public void setup() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> FreeCam.instance.onClientTickStart());
        ClientTickEvents.END_CLIENT_TICK.register(client -> ChatCommandManager.instance.onTickEnd());
    }

    private record VanillaWrapperRegistry<T>(Registry<T> registry) implements WrappedRegistry<T> {

        @Override
        public ResourceLocation getKey(T value) {
            return registry.getKey(value);
        }

        @Override
        public T getValue(ResourceLocation id) {
            return registry.get(id);
        }
    }
}
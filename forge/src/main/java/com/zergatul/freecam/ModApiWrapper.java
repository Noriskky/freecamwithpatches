package com.zergatul.freecam;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class ModApiWrapper {

    public static final ModApiWrapper instance = new ModApiWrapper();

    public final WrappedRegistry<Block> BLOCKS = new ForgeWrappedRegistry<>(ForgeRegistries.BLOCKS);

    private ModApiWrapper() {

    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            FreeCam.instance.onClientTickStart();
        }
        if (event.phase == TickEvent.Phase.END) {
            ChatCommandManager.instance.onTickEnd();
        }
    }

    private record ForgeWrappedRegistry<T>(IForgeRegistry<T> registry) implements WrappedRegistry<T> {

        @Override
        public ResourceLocation getKey(T value) {
            return registry.getKey(value);
        }

        @Override
        public T getValue(ResourceLocation id) {
            return registry.getValue(id);
        }
    }
}
package com.zergatul.freecam.mixins;

import com.zergatul.freecam.helpers.MixinMouseHandlerHelper;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/MouseHandler;turnPlayer()V")
    private void onBeforeTurnPlayer(CallbackInfo info) {
        MixinMouseHandlerHelper.insideTurnPlayer = true;
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/MouseHandler;turnPlayer()V")
    private void onAfterTurnPlayer(CallbackInfo info) {
        MixinMouseHandlerHelper.insideTurnPlayer = false;
    }
}
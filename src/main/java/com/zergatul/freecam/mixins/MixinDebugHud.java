package com.zergatul.freecam.mixins;

import com.zergatul.freecam.FreeCamController;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class MixinDebugHud {

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/gui/hud/DebugHud;getLeftText()Ljava/util/List;")
    private void onAfterGetLeftText(CallbackInfoReturnable<List<String>> info) {
        FreeCamController.instance.onRenderDebugScreenLeft(info.getReturnValue());
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/gui/hud/DebugHud;getRightText()Ljava/util/List;")
    private void onAfterGetRightText(CallbackInfoReturnable<List<String>> info) {
        FreeCamController.instance.onRenderDebugScreenRight(info.getReturnValue());
    }
}
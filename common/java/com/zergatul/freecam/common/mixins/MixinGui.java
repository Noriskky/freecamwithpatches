package com.zergatul.freecam.common.mixins;

import com.zergatul.freecam.common.FreeCam;
import com.zergatul.freecam.common.plugin.ModifyMethodReturnValue;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Gui.class, priority = 2000)
public abstract class MixinGui {

    @SuppressWarnings("unused")
    @ModifyMethodReturnValue(
            method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean onRenderCrosshairModifyIsFirstPerson(boolean value) {
        return FreeCam.instance.onRenderCrosshairModifyIsFirstPerson(value);
    }
}
package com.zergatul.freecam.mixins;

import com.zergatul.freecam.FreeCam;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public abstract class MixinOptions {

    @Inject(at = @At("HEAD"), method = "bobView()Lnet/minecraft/client/OptionInstance;", cancellable = true)
    private void onBobView(CallbackInfoReturnable<OptionInstance<Boolean>> info) {
        if (FreeCam.instance.isActive()) {
            info.setReturnValue(OptionInstance.createBoolean("", false));
        }
    }
}
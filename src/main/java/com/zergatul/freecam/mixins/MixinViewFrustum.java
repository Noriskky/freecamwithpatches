package com.zergatul.freecam.mixins;

import com.zergatul.freecam.FreeCamController;
import net.minecraft.client.renderer.ViewFrustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ViewFrustum.class)
public abstract class MixinViewFrustum {

    @ModifyVariable(
            method = "updateChunkPositions(DD)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private double onUpdateChunkPositionsViewEntityX(double viewEntityX) {
        return FreeCamController.instance.getViewFrustumEntityPosX(viewEntityX);
    }

    @ModifyVariable(
            method = "updateChunkPositions(DD)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true
    )
    private double onUpdateChunkPositionsViewEntityZ(double viewEntityZ) {
        return FreeCamController.instance.getViewFrustumEntityPosZ(viewEntityZ);
    }
}
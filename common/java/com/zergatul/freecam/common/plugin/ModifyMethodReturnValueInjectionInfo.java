package com.zergatul.freecam.common.plugin;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@InjectionInfo.AnnotationType(ModifyMethodReturnValue.class)
@InjectionInfo.HandlerPrefix("return")
public class ModifyMethodReturnValueInjectionInfo extends InjectionInfo {

    public ModifyMethodReturnValueInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
        super(mixin, method, annotation);
    }

    @Override
    protected Injector parseInjector(AnnotationNode annotationNode) {
        return new ModifyMethodReturnValueInjector(this);
    }
}
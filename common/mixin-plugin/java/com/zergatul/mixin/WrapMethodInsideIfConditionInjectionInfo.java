package com.zergatul.mixin;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@InjectionInfo.AnnotationType(WrapMethodInsideIfCondition.class)
@InjectionInfo.HandlerPrefix("wrap-method")
public class WrapMethodInsideIfConditionInjectionInfo extends InjectionInfo {

    public WrapMethodInsideIfConditionInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
        super(mixin, method, annotation);
    }

    @Override
    protected Injector parseInjector(AnnotationNode annotationNode) {
        return new WrapMethodInsideIfConditionInjector(this);
    }
}
package com.zergatul.mixin;

import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.util.logging.MessageRouter;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes({
        "com.zergatul.mixin.ModifyMethodReturnValue",
        "com.zergatul.mixin.WrapMethodInsideIfCondition"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AnnotationProcessor extends AbstractProcessor {

    public AnnotationProcessor() {
        // fix crash with missing IMixinService at this stage
        MessageRouter.setMessager(new NullMessager());
        InjectionInfo.register(ModifyMethodReturnValueInjectionInfo.class);
        InjectionInfo.register(WrapMethodInsideIfConditionInjectionInfo.class);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // return false so original spongepowered annotation processor can process registered annotation
        return false;
    }

    private static class NullMessager implements Messager {

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg) {}

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {}

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {}

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {}
    }
}
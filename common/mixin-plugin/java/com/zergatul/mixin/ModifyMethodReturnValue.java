package com.zergatul.mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface ModifyMethodReturnValue {
    String[] method();
    At[] at();
    Slice[] slice() default {};
    boolean remap() default true;
    int require() default -1;
    int expect() default 1;
    int allow() default -1;
}
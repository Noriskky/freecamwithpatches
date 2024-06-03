package com.zergatul.mixin;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionPointException;

public class WrapMethodInsideIfConditionInjector extends Injector {

    public WrapMethodInsideIfConditionInjector(InjectionInfo info) {
        super(info, "@WrapMethodInsideIfCondition");
    }

    @Override
    protected void inject(Target target, InjectionNodes.InjectionNode node) {
        checkTargetModifiers(target, false);

        AbstractInsnNode instructionNode = node.getCurrentTarget();
        if (instructionNode instanceof MethodInsnNode methodInsnNode) {
            Type returnType = Type.getReturnType(methodInsnNode.desc);
            if (returnType != Type.VOID_TYPE) {
                throw new InvalidInjectionException(this.info, "@WrapMethodInsideIfCondition should point to void methods.");
            }

            InsnList instructions = new InsnList();
            // TODO: validate parameters
            if (!this.isStatic) {
                throw new InvalidInjectionException(this.info, "@WrapMethodInsideIfCondition - only static supported.");
            }

            Type[] arguments = Type.getArgumentTypes(methodInsnNode.desc);
            for (int i = 0; i < arguments.length; i++) {
                instructions.add(new VarInsnNode());
            }

            //Type methodType = Type.getMethodType(methodInsnNode.desc);
            //Type.getMethodDescriptor() methodInsnNode.desc

            invokeHandler(instructions);
            //target.insns.insert(instructionNode, instructions);
        } /*else if (instructionNode instanceof VarInsnNode varInsnNode) {
            if (varInsnNode.getOpcode() == Opcodes.ALOAD) {
                if (this.isStatic) {
                    InsnList instructions = new InsnList();
                    invokeHandler(instructions);
                    target.insns.insert(instructionNode, instructions);
                } else {
                    throw new InvalidInjectionPointException(this.info, "Can only inject static method.");
                }
            } else {
                throw new InvalidInjectionPointException(this.info, "Can only inject into ALOAD.");
            }
        }*/ else {
            throw new InvalidInjectionPointException(this.info, "@ModifyMethodReturnValue should point to method instruction.");
        }
    }
}
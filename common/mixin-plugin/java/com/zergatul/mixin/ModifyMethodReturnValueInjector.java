package com.zergatul.mixin;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionPointException;

public class ModifyMethodReturnValueInjector extends Injector {

    public ModifyMethodReturnValueInjector(InjectionInfo info) {
        super(info, "@ModifyMethodReturnValue");
    }

    @Override
    protected void inject(Target target, InjectionNodes.InjectionNode node) {
        checkTargetModifiers(target, false);

        AbstractInsnNode instructionNode = node.getCurrentTarget();
        if (instructionNode instanceof MethodInsnNode methodInsnNode) {
            Type returnType = Type.getReturnType(methodInsnNode.desc);
            if (returnType == Type.VOID_TYPE) {
                throw new InvalidInjectionException(this.info, "@ModifyMethodReturnValue should not point to void methods.");
            }

            InsnList instructions = new InsnList();
            // TODO: validate parameters
            if (!this.isStatic) {
                target.method.maxStack++;
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                if (returnType.getSize() == 2) {
                    target.method.maxStack++;
                    instructions.add(new InsnNode(Opcodes.DUP_X2));
                    instructions.add(new InsnNode(Opcodes.POP));
                } else {
                    instructions.add(new InsnNode(Opcodes.SWAP));
                }
            }

            invokeHandler(instructions);
            target.insns.insert(instructionNode, instructions);
        } else {
            throw new InvalidInjectionPointException(this.info, "@ModifyMethodReturnValue should point to method instruction.");
        }
    }
}
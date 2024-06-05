package com.zergatul.mixin.helpers;

import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class LocalVarHelper {

    public static int getStoreInst(Type type) {
        return switch (type.getSort()) {
            case Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> ISTORE;
            case Type.LONG -> LSTORE;
            case Type.FLOAT -> FSTORE;
            case Type.DOUBLE -> DSTORE;
            case Type.ARRAY, Type.OBJECT -> ASTORE;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    public static int getLoadInst(Type type) {
        return switch (type.getSort()) {
            case Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> ILOAD;
            case Type.LONG -> LLOAD;
            case Type.FLOAT -> FLOAD;
            case Type.DOUBLE -> DLOAD;
            case Type.ARRAY, Type.OBJECT -> ALOAD;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }
}
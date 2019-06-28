function initializeCoreMod() {
    print("Dynamic Lights cminit");

    var/*Class*/ ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

    var/*Class*/ VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var/*Class*/ MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var/*Class*/ InsnList = Java.type('org.objectweb.asm.tree.InsnList');

    var/*Class/Interface*/ Opcodes = Java.type('org.objectweb.asm.Opcodes');

    // var methodName = "getLightFor";
    var methodName = ASMAPI.mapMethod("func_175642_b");
    print("func_175642_b was mapped to: ", methodName);

    var className = 'net.minecraft.world.World';
    var methodDescriptorToModify = "(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)I";
    var goalInvokeDesc = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;I)I";

    if (!methodName.equals("getLightFor")) {
        print("detecting obfuscated environment, method name is ", methodName);
    }

    /*
          public getLightFor(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)I
           L0
            LINENUMBER 416 L0
            ALOAD 0
            INVOKEVIRTUAL net/minecraft/world/World.getChunkProvider ()Lnet/minecraft/world/chunk/AbstractChunkProvider;
            INVOKEVIRTUAL net/minecraft/world/chunk/AbstractChunkProvider.getLightManager ()Lnet/minecraft/world/lighting/WorldLightManager;
            ALOAD 1
            INVOKEVIRTUAL net/minecraft/world/lighting/WorldLightManager.getLightEngine (Lnet/minecraft/world/LightType;)Lnet/minecraft/world/lighting/IWorldLightListener;
            ALOAD 2
            INVOKEINTERFACE net/minecraft/world/lighting/IWorldLightListener.getLightFor (Lnet/minecraft/util/math/BlockPos;)I (itf)
            IRETURN
           L1
            LOCALVARIABLE this Lnet/minecraft/world/World; L0 L1 0
            LOCALVARIABLE type Lnet/minecraft/world/LightType; L0 L1 1
            LOCALVARIABLE pos Lnet/minecraft/util/math/BlockPos; L0 L1 2
            MAXSTACK = 2
            MAXLOCALS = 3

            transform to:

          public getLightFor(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)I
           L0
            LINENUMBER 112 L0
            ALOAD 0
            INVOKEVIRTUAL atomicstryker/dynamiclights/client/WorldTest.getChunkProvider ()Lnet/minecraft/world/chunk/AbstractChunkProvider;
            INVOKEVIRTUAL net/minecraft/world/chunk/AbstractChunkProvider.getLightManager ()Lnet/minecraft/world/lighting/WorldLightManager;
            ALOAD 1
            INVOKEVIRTUAL net/minecraft/world/lighting/WorldLightManager.getLightEngine (Lnet/minecraft/world/LightType;)Lnet/minecraft/world/lighting/IWorldLightListener;
            ALOAD 2
            INVOKEINTERFACE net/minecraft/world/lighting/IWorldLightListener.getLightFor (Lnet/minecraft/util/math/BlockPos;)I (itf)
            ISTORE 3
           L1
            LINENUMBER 113 L1
            ALOAD 0
            ALOAD 2
            ILOAD 3
            INVOKESTATIC atomicstryker/dynamiclights/client/DynamicLights.getDynamicLightValue (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;I)I
            IRETURN
           L2
            LOCALVARIABLE this Latomicstryker/dynamiclights/client/WorldTest; L0 L2 0
            LOCALVARIABLE type Lnet/minecraft/world/LightType; L0 L2 1
            LOCALVARIABLE pos Lnet/minecraft/util/math/BlockPos; L0 L2 2
            LOCALVARIABLE i I L1 L2 3
            MAXSTACK = 3
            MAXLOCALS = 4
        }

     */

    return {
        'Dynamic Lights Hook': {
            'target': {
                'type': 'CLASS',
                'name': className
            },
            'transformer': function (classNode) {

                print("dynamic lights coremod visiting class ", classNode.name);

                var methods = classNode.methods;
                for (i = 0; i < methods.length; i++) {
                    print("dynamic lights coremod looking at method ", methods[i].name, "desc: ", methods[i].desc);
                    if (methods[i].name.equals(methodName) && methods[i].desc.equals(methodDescriptorToModify)) {
                        print("this is our target method!");

                        /* inject this before IRETURN:

                            ISTORE 3
                            ALOAD 0
                            ALOAD 2
                            ILOAD 3
                            INVOKESTATIC atomicstryker/dynamiclights/client/DynamicLights.getDynamicLightValue (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;I)I

                         */

                        var targetNode;
                        var iter = methods[i].instructions.iterator();
                        var index = 0;
                        var found = false;
                        while (iter.hasNext()) {
                            // check all nodes
                            targetNode = iter.next();

                            // find the IRETURN node
                            if (targetNode.getOpcode() === Opcodes.IRETURN) {
                                print("Found IRETURN Node at index ", index);
                                found = true;
                                break;
                            }
                            index++;
                        }
                        if (found) {

                            var toInject = new InsnList();
                            toInject.add(new VarInsnNode(Opcodes.ISTORE, 3));
                            toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            toInject.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            toInject.add(new VarInsnNode(Opcodes.ILOAD, 3));
                            toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "atomicstryker/dynamiclights/client/DynamicLights", "getDynamicLightValue", goalInvokeDesc, false));

                            // now write our instructions before the target node
                            methods[i].instructions.insertBefore(targetNode, toInject);
                            print("Dynamic Lights injected code!");
                        }
                        break;
                    }
                }
                print("Dynamic Lights coremod exiting");
                return classNode;
            }
        }
    }
}
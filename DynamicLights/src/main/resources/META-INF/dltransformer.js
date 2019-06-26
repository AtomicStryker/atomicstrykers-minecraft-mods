function initializeCoreMod() {
    print("Dynamic Lights cminit");

    var/*Class*/ ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

    var/*Class*/ MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

    var/*Class/Interface*/ Opcodes = Java.type('org.objectweb.asm.Opcodes');

    // var methodName = "getRawLight";
    var methodName = ASMAPI.mapMethod("func_175638_a");
    print("func_175638_a was mapped to: ", methodName);

    var className = 'net.minecraft.world.World';
    var methodDescriptorToModify = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/EnumLightType;)I";
    var goalInvokeDesc = "(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;)I";

    if (!methodName.equals("getRawLight")) {
        print("detecting obfuscated environment, method name is ", methodName);
    }

    /*
          private getRawLight(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/EnumLightType;)I
           L16
            ALOAD 2
            GETSTATIC net/minecraft/world/EnumLightType.SKY : Lnet/minecraft/world/EnumLightType;
            IF_ACMPNE L17
            ALOAD 0
            ALOAD 1
            INVOKEVIRTUAL net/minecraft/world/World.canSeeSky (Lnet/minecraft/util/math/BlockPos;)Z
            IFEQ L17
           L18
            BIPUSH 15
            IRETURN
           L17
            ALOAD 0
            ALOAD 1
            INVOKEVIRTUAL net/minecraft/world/World.getBlockState (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
            ASTORE 3
           L19
            ALOAD 2
            GETSTATIC net/minecraft/world/EnumLightType.SKY : Lnet/minecraft/world/EnumLightType;
            IF_ACMPNE L20
            ICONST_0
            GOTO L21
           L20
            ALOAD 3
            ALOAD 0
            ALOAD 1
            INVOKEINTERFACE net/minecraft/block/state/IBlockState.getLightValue (Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;)I (itf)
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

                        /* BEFORE ASM TRANSFORM:

                            ICONST_0
                            GOTO L21
                           L20
                           * FRAME NODE *
                            ALOAD 3
                            ALOAD 0
                            ALOAD 1
                            INVOKEINTERFACE net/minecraft/block/state/IBlockState.getLightValue (Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;)I (itf)
                         */

                        /* AFTER ASM TRANSFORM:

                            ICONST_0
                            GOTO L21
                           L20
                           * FRAME NODE *
                            ALOAD 0
                            ALOAD 1
                            INVOKEVIRTUAL atomicstryker/dynamiclights/client/modules/PlayerSelfLightSource.getBlockState (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
                         */

                        var targetNode;
                        var iter = methods[i].instructions.iterator();
                        var index = 0;
                        var found = false;
                        while (iter.hasNext()) {
                            // check all nodes
                            targetNode = iter.next();

                            // find the ICONST_0 node
                            if (targetNode.getOpcode() === Opcodes.ICONST_0) {
                                print("Found ICONST_0 Node at index ", index);
                                targetNode = iter.next(); // L20
                                print("next node ", targetNode, " opcode: ", targetNode.getOpcode());
                                targetNode = iter.next(); // FRAME NODE
                                print("next node ", targetNode, " opcode: ", targetNode.getOpcode());
                                targetNode = iter.next(); // GOTO
                                print("next node ", targetNode, " opcode: ", targetNode.getOpcode());
                                var aload3 = iter.next();
                                print("aload3 node", aload3, " opcode: ", aload3.getOpcode());
                                if (aload3.getOpcode() === Opcodes.ALOAD && aload3.var === 3) {
                                    print("found ALOAD 3 as expected. deleting it!");
                                    iter.remove();
                                    iter.next(); // ALOAD 0
                                    iter.next(); // ALOAD 1
                                    targetNode = iter.next();
                                    print("found INVOKESTATIC: ", targetNode.name, " ", targetNode.desc);
                                    found = true;
                                    iter.remove();
                                    // select followup node just so we can inject our code before it
                                    targetNode = iter.next();
                                    break;
                                }
                            }
                            index++;
                        }
                        if (found) {
                            // now write our replacement before the target node
                            methods[i].instructions.insertBefore(targetNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "atomicstryker/dynamiclights/client/DynamicLights", "getDynamicLightValue", goalInvokeDesc, false));
                            print("Dynamic Lights injected code!");
                        }
                        break;
                    }
                }
                print("Dynamic Lights coremode exiting");
                return classNode;
            }
        }
    }
}
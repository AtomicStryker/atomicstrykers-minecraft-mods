function initializeCoreMod() {
    print("Multi Mine cminit");

    var/*Class*/ ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

    var/*Class*/ VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var/*Class*/ MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var/*Class*/ FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
    var/*Class*/ InsnList = Java.type('org.objectweb.asm.tree.InsnList');

    var/*Class/Interface*/ Opcodes = Java.type('org.objectweb.asm.Opcodes');

    // var methodName = "onPlayerDamageBlock";
    var methodName = ASMAPI.mapMethod("func_180512_c");
    print("func_180512_c was mapped to: ", methodName);

    var className = 'net.minecraft.client.multiplayer.PlayerControllerMP';
    var methodDescriptorToModify = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z";

    if (!methodName.equals("onPlayerDamageBlock")) {
        print("detecting obfuscated environment");
    }

    return {
        'Multi Mine Hook': {
            'target': {
                'type': 'CLASS',
                'name': className
            },
            'transformer': function (classNode) {

                print("Multi Mine coremod visiting class ", classNode.name);

                var methods = classNode.methods;
                for (i = 0; i < methods.length; i++) {
                    print("Multi Mine coremod looking at method ", methods[i].name, "desc: ", methods[i].desc);
                    if (methods[i].name.equals(methodName) && methods[i].desc.equals(methodDescriptorToModify)) {
                        print("this is our target method!");
                        var targetNode;
                        var index = -1;
                        var iter = methods[i].instructions.iterator();
                        while (iter.hasNext()) {
                            // check all nodes
                            index++;
                            targetNode = iter.next();

                            if (targetNode.getOpcode() === Opcodes.IFLT) {
                                print("Multi Mine coremod found IFLT at index ", index);

                                // from there, step backwards to ALOAD node to get infront of the curBlockDamageMP field load
                                var offset = 1;
                                while (methods[i].instructions.get(index - offset).getOpcode() !== Opcodes.ALOAD)
                                {
                                    offset++;
                                }
                                print("Found ALOAD Node at offset -", offset, " from IFLT Node");

                                /* this is the code we want to patch in, in bytecode
                                    0: aload_0
                                    1: invokestatic  #38                 // Method atomicstryker/multimine/client/MultiMineClient.instance:()Latomicstryker/multimine/client/MultiMineClient;
                                    4: aload_1
                                    5: aload_0
                                    6: getfield      #44                 // Field curBlockDamageMP:F which is e:F
                                    9: invokevirtual #46                 // Method atomicstryker/multimine/client/MultiMineClient.eventPlayerDamageBlock:(Lnet/minecraft/util/BlockPos;F)F
                                   12: putfield      #44                 // Field curBlockDamageMP:F which is e:F
                                 */

                                // construct it using asm, insert it before 'if (this.curBlockDamageMP >= 1.0F)'
                                var toInject = new InsnList();
                                toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                toInject.add(
                                    new MethodInsnNode(Opcodes.INVOKESTATIC, "atomicstryker/multimine/client/MultiMineClient", "instance", "()Latomicstryker/multimine/client/MultiMineClient;", false));
                                toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/multiplayer/PlayerControllerMP", "curBlockDamageMP", "F"));
                                toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "atomicstryker/multimine/client/MultiMineClient", "eventPlayerDamageBlock", "(Lnet/minecraft/util/math/BlockPos;F)F", false));
                                toInject.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/multiplayer/PlayerControllerMP", "curBlockDamageMP", "F"));
                                methods[i].instructions.insertBefore(methods[i].instructions.get(index - offset), toInject);
                                print("Multi Mine injection success!");
                                // in effect, we added this line of code: 'this.curBlockDamageMP = atomicstryker.multimine.client.MultiMineClient.instance().eventPlayerDamageBlock(blockPos, curBlockDamageMP);'
                            }
                        }
                        break;
                    }
                }
                print("Multi Mine coremod exiting");
                return classNode;
            }
        }
    }
}
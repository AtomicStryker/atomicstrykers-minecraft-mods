function initializeCoreMod() {
    print("Ruins cminit");

    var/*Class*/ ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

    var/*Class*/ VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var/*Class*/ MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var/*Class*/ InsnList = Java.type('org.objectweb.asm.tree.InsnList');

    var/*Class/Interface*/ Opcodes = Java.type('org.objectweb.asm.Opcodes');

    // var methodName = "decorate";
    var methodName = ASMAPI.mapMethod("func_202092_b");
    print("func_202092_b was mapped to: ", methodName);

    var className = 'net.minecraft.world.gen.ChunkGenerator';
    var methodDescriptorToModify = "(Lnet/minecraft/world/gen/WorldGenRegion;)V";

    if (!methodName.equals("decorate")) {
        print("detecting obfuscated environment");
    }

    return {
        'Ruins chunk decoration Hook': {
            'target': {
                'type': 'CLASS',
                'name': className
            },
            'transformer': function (classNode) {

                print("ruins coremod visiting class ", classNode.name);

                var methods = classNode.methods;
                for (i = 0; i < methods.length; i++) {
                    print("ruins coremod looking at method ", methods[i].name, "desc: ", methods[i].desc);
                    if (methods[i].name.equals(methodName) && methods[i].desc.equals(methodDescriptorToModify)) {
                        print("this is our target method!");

                        var targetNode;
                        var iter = methods[i].instructions.iterator();
                        while (iter.hasNext()) {
                            // check all nodes
                            targetNode = iter.next();

                            if (targetNode.getOpcode() === Opcodes.RETURN) {

                                print("ruins coremod found target return node, opcode: ", targetNode.getOpcode(), " ,", targetNode.owner, " ,", targetNode.name, " ,", targetNode.desc);

                                /* to inject before the RETURN:
                                    ALOAD 1
                                    INVOKESTATIC atomicstryker/ruins/common/RuinsMod.decorateChunkHook (Lnet/minecraft/world/gen/WorldGenRegion;)V
                                 */

                                // prepare code to inject
                                var toInject = new InsnList();
                                toInject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // push WorldGenRegion argument from calling method
                                toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "atomicstryker/ruins/common/RuinsMod", "decorateChunkHook", "(Lnet/minecraft/world/gen/WorldGenRegion;)V", false));
                                // this bytecode is equivalent to this line: RuinsMod.decorateChunkHook(param1);

                                // now write our hook in, before the target node
                                methods[i].instructions.insertBefore(targetNode, toInject);
                                print("ruins coremod injection success!");
                                break;
                            }
                        }
                        break;
                    }
                }
                print("ruins coremod exiting");
                return classNode;
            }
        }
    }
}
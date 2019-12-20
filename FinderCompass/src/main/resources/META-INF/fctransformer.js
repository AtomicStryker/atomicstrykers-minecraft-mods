function initializeCoreMod() {
    print("Finder Compass cminit");

    var/*Class*/ ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

    var/*Class*/ VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var/*Class*/ MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var/*Class*/ InsnList = Java.type('org.objectweb.asm.tree.InsnList');

    var/*Class/Interface*/ Opcodes = Java.type('org.objectweb.asm.Opcodes');

    // method which calls another method with ForgeHooksClient.renderLitItem inside it
    var methodName = ASMAPI.mapMethod("func_229111_a_");
    print("func_229111_a_ was mapped to: ", methodName);

    var className = 'net.minecraft.client.renderer.ItemRenderer';
    var methodDescriptorToModify = "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/model/ItemCameraTransforms$TransformType;ZLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;IILnet/minecraft/client/renderer/model/IBakedModel;)V";
    var targetNodeDescriptor = "(Lnet/minecraft/client/renderer/model/IBakedModel;Lnet/minecraft/item/ItemStack;IILcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;)V";
    var itemStackVoidDescriptor = "(Lnet/minecraft/item/ItemStack;)V";
    // if (!methodName.equals("renderItemModelIntoGUI")) {
    //     print("detecting obfuscated environment");
    // }

    return {
        'Finder Compass Render Hook': {
            'target': {
                'type': 'CLASS',
                'name': className
            },
            'transformer': function (classNode) {

                print("finder compass coremod visiting class ", classNode.name);

                var methods = classNode.methods;
                for (i = 0; i < methods.length; i++) {
                    print("finder compass coremod looking at method ", methods[i].name, "desc: ", methods[i].desc);
                    if (methods[i].name.equals(methodName) && methods[i].desc.equals(methodDescriptorToModify)) {
                        print("this is our target method!");
                        var targetNode;
                        var iter = methods[i].instructions.iterator();
                        var found = false;
                        while (iter.hasNext()) {
                            // check all nodes
                            targetNode = iter.next();

                            if (targetNode instanceof MethodInsnNode) {
                                var candidate = targetNode;
                                if (candidate.desc.equals(targetNodeDescriptor)) {
                                    found = true;
                                    print("finder compass coremod found target node, opcode: ", candidate.getOpcode(), " ,", candidate.owner, " ,", candidate.name, " ,", candidate.desc);
                                    break;
                                }
                            }
                        }
                        if (found) {
                            // prepare code to inject
                            var toInject = new InsnList();
                            toInject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // push itemstack argument from calling method
                            toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "atomicstryker/findercompass/client/CompassRenderHook", "renderItemHook", itemStackVoidDescriptor, false));
                            // this bytecode is equivalent to this line: CompassRenderHook.renderItemHook(itemStack);

                            // now write our hook in, after the target node
                            // it needs to be rendered afterwards for the semi-transparency of the needles to not perforate the compass
                            methods[i].instructions.insert(targetNode, toInject);
                            print("Finder Compass coremode injection success!");
                        }
                        break;
                    }
                }
                print("Finder Compass coremode exiting");
                return classNode;
            }
        }
    }
}
package atomicstryker.findercompass.client.coremod;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * 
 * @author AtomicStryker
 * 
 * Use bytecode injection to place an item render hook for the compass needle renderer.
 * Obfuscated names will have to be updated with each Obfuscation change.
 *
 */
public class FCTransformer implements IClassTransformer
{
    
    /*  net.minecraft.client.renderer.entity.RenderItem */
    private String classNameToModify = "cqh";
      
    /* (Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V */
    private String methodDescriptorToModify = "(Lamj;Lcxe;)V";
    
    /* net.minecraft.client.renderer.entity.RenderItem.renderItem(ItemStack stack, IBakedModel model) / func_180454_a */
    private String methodNameToModify = "a"; // this is actually unneeded because a is too generic
    
    /* (Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/item/ItemStack;)V */
    private String targetNodeDescriptor = "(Lcxe;Lamj;)V";
    
    /* (Lnet/minecraft/item/ItemStack;)V */
    private String itemStackVoidDescriptor = "(Lamj;)V";
    
    @Override
    public byte[] transform(String name, String newName, byte[] bytes)
    {
        //System.out.println("transforming: "+name);
        if (name.equals(classNameToModify))
        {
            return handleWorldTransform(bytes, true);
        }
        else if (name.equals("net.minecraft.client.renderer.entity.RenderItem")) // MCP testing
        {
            methodNameToModify = "renderItem";
            methodDescriptorToModify = "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V";
            targetNodeDescriptor = "(Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/item/ItemStack;)V";
            itemStackVoidDescriptor = "(Lnet/minecraft/item/ItemStack;)V";
            return handleWorldTransform(bytes, false);
        }
        
        return bytes;
    }
    
    private byte[] handleWorldTransform(byte[] bytes, boolean obf)
    {
        System.out.println("**************** Finder Compass transform running on RenderItem, obf: "+obf+" *********************** ");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.name.equals(methodNameToModify)
            && m.desc.equals(methodDescriptorToModify))
            {
                System.out.println("In target method "+methodNameToModify+", Patching!");
                
                AbstractInsnNode targetNode = null;
                Iterator<AbstractInsnNode> iter = m.instructions.iterator();
                boolean found = false;
                while (iter.hasNext())
                {
                	// check all nodes
                    targetNode = (AbstractInsnNode) iter.next();
                    
                    if (targetNode instanceof MethodInsnNode)
                    {
                        MethodInsnNode candidate = (MethodInsnNode) targetNode;
                        if (candidate.desc.equals(targetNodeDescriptor))
                        {
                            found = true;
                            System.out.printf("found target node, opcode: %d, %s %s %s\n", candidate.getOpcode(), candidate.owner, candidate.name, candidate.desc);
                            break;
                        }
                    }
                }
                
                if (found)
                {
                    // prepare code to inject
                    InsnList toInject = new InsnList();
                    toInject.add(new VarInsnNode(ALOAD, 1)); // push itemstack argument from calling method
                    toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/findercompass/client/CompassRenderHook", "renderItemHook", itemStackVoidDescriptor, false));
                    // this bytecode is equivalent to this line: CompassRenderHook.renderItemHook(itemStack);
                    
                	// now write our hook in, after the target node
                	m.instructions.insertBefore(targetNode, toInject);
                }
                break;
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}

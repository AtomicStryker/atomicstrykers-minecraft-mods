package atomicstryker.dynamiclights.common;

import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.ISTORE;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * 
 * @author AtomicStryker
 * 
 * Magic happens in here. MAGIC.
 * Obfuscated names will have to be updated with each Obfuscation change.
 *
 */
public class DLTransformer implements IClassTransformer
{
    
    /* net/minecraft/World */
    private String classNameWorld = "aqu";
      
    /* (Lnet/minecraft/util/BlockPos;Lnet/minecraft/world/EnumSkyBlock;)I */
    private String targetMethodDesc = "(Ldt;Larf;)I";
    
    /* net/minecraft/World.getRawLight / func_175638_a */
    private String computeLightValueMethodName = "a";
    
    /* (Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;)I */
    private String goalInvokeDesc = "(Latr;Lard;Ldt;)I";
    
    @Override
    public byte[] transform(String name, String newName, byte[] bytes)
    {
        //System.out.println("transforming: "+name);
        if (name.equals(classNameWorld))
        {
            return handleWorldTransform(bytes, true);
        }
        else if (name.equals("net.minecraft.world.World")) // MCP testing
        {
            computeLightValueMethodName = "getRawLight";
            targetMethodDesc = "(Lnet/minecraft/util/BlockPos;Lnet/minecraft/world/EnumSkyBlock;)I";
            goalInvokeDesc = "(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;)I";
            return handleWorldTransform(bytes, false);
        }
        
        return bytes;
    }
    
    private byte[] handleWorldTransform(byte[] bytes, boolean obf)
    {
        System.out.println("**************** Dynamic Lights transform running on World, obf: "+obf+" *********************** ");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, ClassReader.SKIP_FRAMES); // SKIP_FRAMES to avoid ASM bug present here
        
        // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.name.equals(computeLightValueMethodName)
            && m.desc.equals(targetMethodDesc))
            {
                System.out.println("In target method "+computeLightValueMethodName+", Patching!");
                
                /* before patch:
			       0: aload_2       
			       1: getstatic     #136                // Field net/minecraft/world/EnumSkyBlock.SKY:Lnet/minecraft/world/EnumSkyBlock;
			       4: if_acmpne     18
			       7: aload_0       
			       8: aload_1       
			       9: invokevirtual #160                // Method isAgainstSky:(Lnet/minecraft/util/BlockPos;)Z
			      12: ifeq          18
			      15: bipush        15
			      17: ireturn       
			      18: aload_0       
			      19: aload_1       
			      20: invokevirtual #80                 // Method getBlockState:(Lnet/minecraft/util/BlockPos;)Lnet/minecraft/block/state/IBlockState;
			      23: invokeinterface #81,  1           // InterfaceMethod net/minecraft/block/state/IBlockState.getBlock:()Lnet/minecraft/block/Block;
			      28: astore_3      
			      29: aload_3       
			      30: aload_0       
			      31: aload_1       
			      32: invokevirtual #486                // Method net/minecraft/block/Block.getLightValue:(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;)I
			      35: istore        4
			      [... many more...]
                 */
                
                AbstractInsnNode targetNode = null;
                Iterator<AbstractInsnNode> iter = m.instructions.iterator();
                boolean found = false;
                while (iter.hasNext())
                {
                	// check all nodes
                    targetNode = (AbstractInsnNode) iter.next();
                    
                    // find the first ASTORE node, it stores the Block reference for the Block.getLightValue call
                    if (targetNode.getOpcode() == ASTORE)
                    {
                    	VarInsnNode astore = (VarInsnNode) targetNode;
                    	System.out.println("Found ASTORE Node, is writing variable number: "+astore.var);
                    	
                    	// go further until ISTORE is hit
                    	while (targetNode.getOpcode() != ISTORE)
                    	{
                    		if (targetNode instanceof MethodInsnNode)
                    		{
                    			MethodInsnNode mNode = (MethodInsnNode) targetNode;
                    			System.out.printf("found target node, opcode: %d, %s %s %s\n", mNode.getOpcode(), mNode.owner, mNode.name, mNode.desc);
                    			found = true;
                    			iter.remove();
                    			targetNode = iter.next(); // select next node as target
                    			break;
                    		}
                    		targetNode = iter.next();
                    		System.out.printf("Node %s, opcode %d\n", targetNode, targetNode.getOpcode());
                    	}
                    	break;
                    }
                }
                
                if (found)
                {
                	// now write our replacement before the target node
                	m.instructions.insertBefore(targetNode, new MethodInsnNode(INVOKESTATIC, "atomicstryker/dynamiclights/client/DynamicLights", "getLightValue", goalInvokeDesc, false));
                }
                break;
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}

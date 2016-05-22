package atomicstryker.dynamiclights.common;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

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
    
    /* net/minecraft/world/World */
    private String classNameWorld = "aht";
      
    /* (Lnet/minecraft/util/BlockPos;Lnet/minecraft/world/EnumSkyBlock;)I */
    private String targetMethodDesc = "(Lcl;Lahz;)I";
    
    /* net/minecraft/world/World.getRawLight / func_175638_a */
    private String computeLightValueMethodName = "a";
    
    /* (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;)I */
    private String goalInvokeDesc = "(Lard;Lahx;Lcl;)I";
    
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
            goalInvokeDesc = "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)I";
            return handleWorldTransform(bytes, false);
        }
        
        return bytes;
    }
    
    private String insnToString(AbstractInsnNode insn)
    {
        insn.accept(mp);
        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();
        return sw.toString();
    }

    private Printer printer = new Textifier();
    private TraceMethodVisitor mp = new TraceMethodVisitor(printer); 
    
    private byte[] handleWorldTransform(byte[] bytes, boolean obf)
    {
        System.out.println("**************** Dynamic Lights transform running on World, obf: "+obf+" *********************** ");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        // find method to inject into
        for (MethodNode m : classNode.methods)
        {
            if (m.name.equals(computeLightValueMethodName)
                    && (!obf || m.desc.equals(targetMethodDesc)))
            {
                System.out.println("In target method " + computeLightValueMethodName +":"+m.desc+ ", Patching!");
                
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
                
                /*
                System.out.println("=== PRE PRINT===");
                Iterator<AbstractInsnNode> printIter = m.instructions.iterator();
                while (printIter.hasNext())
                {
                    System.out.print(insnToString(printIter.next()));
                }
                System.out.println();
                System.out.println("=== PRE PRINT===");
                */

                AbstractInsnNode targetNode = null;
                Iterator<AbstractInsnNode> iter = m.instructions.iterator();
                boolean found = false;
                int index = 0;
                while (iter.hasNext())
                {
                    // check all nodes
                    targetNode = iter.next();

                    // find the first ASTORE node, it stores the Block reference for the Block.getLightValue call
                    if (targetNode.getOpcode() == Opcodes.ASTORE)
                    {
                        VarInsnNode astore = (VarInsnNode) targetNode;
                        System.out.println("Found ASTORE Node at index "+index+", is writing variable number: " + astore.var);

                        // go further until ISTORE is hit
                        while (targetNode.getOpcode() != Opcodes.ISTORE)
                        {
                            if (targetNode instanceof MethodInsnNode && targetNode.getOpcode() != Opcodes.INVOKEINTERFACE)
                            {
                                MethodInsnNode mNode = (MethodInsnNode) targetNode;
                                System.out.printf("found deletion target at index %d: %s\n", index, insnToString(mNode));
                                found = true;
                                iter.remove();
                                targetNode = iter.next(); // select next node as target
                                break;
                            }
                            targetNode = iter.next();
                            System.out.print("Reading node: "+insnToString(targetNode));
                        }
                        break;
                    }
                    index++;
                }

                if (found)
                {
                    // now write our replacement before the target node
                    m.instructions.insertBefore(targetNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "atomicstryker/dynamiclights/client/DynamicLights", "getLightValue", goalInvokeDesc, false));
                }
                break;
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}

package atomicstryker.dynamiclights.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import net.minecraft.launchwrapper.IClassTransformer;

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
    private String classNameWorld = "amu";

    /*
     * (Lnet/minecraft/util/BlockPos;Lnet/minecraft/world/EnumSkyBlock;)I / func_175638_a
     */
    private String targetMethodDesc = "(Let;Lana;)I";

    /* net/minecraft/world/World.getRawLight / func_175638_a */
    private String computeLightValueMethodName = "a";

    /*
     * (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;)I
     */
    private String goalInvokeDesc = "(Laow;Lawt;Lamy;Let;)I";
    
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
            goalInvokeDesc = "(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)I";
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
                
                /* before patch, forge 2698
                   
               aload_2
               getstatic     #136               // Field net/minecraft/world/EnumSkyBlock.SKY:Lnet/minecraft/world/EnumSkyBlock;
               if_acmpne     18
               aload_0       
               aload_1       
               invokevirtual #160               // Method isAgainstSky:(Lnet/minecraft/util/BlockPos;)Z
               ifeq          18
               bipush        15
               ireturn       
               aload_0       
               aload_1       
               invokevirtual #80                // Method getBlockState:(Lnet/minecraft/util/BlockPos;)Lnet/minecraft/block/state/IBlockState;
               astore_3       
               getstatic                        // Field net/minecraft/world/EnumSkyBlock.SKY:Lnet/minecraft/world/EnumSkyBlock;
               if_acmpne
               iconst_0
               goto [control jumps somewhere else]
               aload_3
               invokeinterface                  // InterfaceMethod Lnet/minecraft/block/state/IBlockState.getBlock:()Lnet/minecraft/block/Block;
               aload_3
               aload_0       
               aload_1       
               stack is block, iblockstate, iblockaccess, blockpos
               invokevirtual                    // Method net/minecraft/block/Block.getLightValue (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;)I
               istore        4
              
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
                            // we want to delete the INVOKEVIRTUAL aka the call to Block.getLightValue
                            if (targetNode instanceof MethodInsnNode && targetNode.getOpcode() == Opcodes.INVOKEVIRTUAL)
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

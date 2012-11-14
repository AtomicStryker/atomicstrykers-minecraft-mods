package atomicstryker.dynamiclights.common;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.ISTORE;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.relauncher.IClassTransformer;

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
    /* class net.minecraft.src.World */
    private final String classNameWorldObfusc = "xv"; // 1.4.4 obfuscation
    //private final String classNameWorldObfusc = "xe"; // 1.4.2 obfuscation
    
    /* class net.minecraft.src.IBlockAccess */
    private final String classNameBlockAccessObfusc = "yf"; // 1.4.4 obfuscation
    //private final String classNameBlockAccessObfusc = "xo"; // 1.4.2 obfuscation
    
    /* method World.computeBlockLightValue(IIIIII)I */
    private final String computeBlockLightMethodNameO = "g"; // both 1.4.2 and 1.4.4 obfuscation
    
    
    private final String classNameWorld = "net.minecraft.src.World";
    private final String classNameWorldJava = "net/minecraft/src/World";
    private final String blockAccessJava = "net/minecraft/src/IBlockAccess";
    private final String computeBlockLightMethodName = "computeBlockLightValue";
    
    @Override
    public byte[] transform(String name, byte[] bytes)
    {
        //System.out.println("transforming: "+name);
        if (name.equals(classNameWorldObfusc))
        {
            return handleWorldTransform(bytes, true);
        }
        else if (name.equals(classNameWorld))
        {
            return handleWorldTransform(bytes, false);
        }
        
        return bytes;
    }
    
    private byte[] handleWorldTransform(byte[] bytes, boolean obfuscated)
    {
        System.out.println("**************** Dynamic Lights transform running on World *********************** ");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.name.equals( obfuscated ? computeBlockLightMethodNameO : computeBlockLightMethodName) && m.desc.equals("(IIIIII)I"))
            {
                System.out.println("In target method! Patching!");
                
                AbstractInsnNode targetNode = null;
                Iterator iter = m.instructions.iterator();
                while (iter.hasNext())
                {
                    targetNode = (AbstractInsnNode) iter.next();
                    if (targetNode.getOpcode() != ISTORE)
                    {
                        iter.remove();
                    }
                    else
                    {
                        // leave the ISTORE node, we'll inject our code infront of it instead
                        break;
                    }
                }
                
                if (targetNode == null)
                {
                    System.err.println("Dynamic Lights transformer did not run into ISTORE node! ABANDON CLASS!");
                    return bytes;
                }
                
                // make new instruction list
                InsnList toInject = new InsnList();
                
                // argument mapping! 0 is World, 5 is blockID, 234 are xyz
                toInject.add(new VarInsnNode(ALOAD, 0));
                toInject.add(new VarInsnNode(ILOAD, 5));
                toInject.add(new VarInsnNode(ILOAD, 2));
                toInject.add(new VarInsnNode(ILOAD, 3));
                toInject.add(new VarInsnNode(ILOAD, 4));
                toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/dynamiclights/client/DynamicLights", "getLightValue", "(L"+(obfuscated ? classNameBlockAccessObfusc : blockAccessJava)+";IIII)I"));
                
                // inject new instruction list into method instruction list
                m.instructions.insertBefore(targetNode, toInject);
                
                System.out.println("Patching Complete!");
                break;
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}

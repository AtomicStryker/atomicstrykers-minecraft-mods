package atomicstryker.simplyhax;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

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
 * Magic happens in here. MAGIC.
 * Obfuscated names will have to be updated with each Obfuscation change.
 *
 */
public class SHTransformer implements IClassTransformer
{
    /* class net.minecraft.src.EntityPlayerSP */
    private final String classNamePlayerObfusc = "blk";
    
    private final String classNamePlayer = "net.minecraft.client.entity.EntityPlayerSP";
    
    @Override
    public byte[] transform(String name, String newName, byte[] bytes)
    {
        //System.out.println("transforming: "+name);
        if (name.equals(classNamePlayerObfusc))
        {
            return handleTransform(bytes, true);
        }
        else if (name.equals(classNamePlayer))
        {
            return handleTransform(bytes, false);
        }
        
        return bytes;
    }
    
    private byte[] handleTransform(byte[] bytes, boolean obfuscated)
    {
        System.out.println("**************** Simply Hax transform running on EntityPlayerSP *********************** ");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.desc.equals("(DDD)V")) // public void moveEntity(double x, double y, double z)
            {
                System.out.println("In target method! Patching!");
                
                AbstractInsnNode targetNode = null;
                Iterator<AbstractInsnNode> iter = m.instructions.iterator();
                int index = 0;
                while (iter.hasNext())
                {
                    index++;
                    targetNode = iter.next();
                    if (targetNode.getOpcode() == RETURN)
                    {
                        break;
                    }
                }
                                
                // make new instruction list
                InsnList toInject = new InsnList();
                
                toInject.add(new VarInsnNode(ALOAD, 0));
                toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/simplyhax/SimplyHaxFlying", "preMoveEntityPlayerSP", "()V"));
                
                // inject new instruction list into method instruction list
                m.instructions.insertBefore(targetNode, toInject);
                
                System.out.println("Patching Complete, target Node was at index: "+index);
                break;
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}

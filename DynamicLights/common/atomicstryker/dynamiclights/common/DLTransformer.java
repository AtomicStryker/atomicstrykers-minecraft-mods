package atomicstryker.dynamiclights.common;

import static org.objectweb.asm.Opcodes.*;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

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
    private final String classNameWorldObfusc = "aab";
    
    /* class net.minecraft.src.IBlockAccess */
    private final String classNameBlockAccessObfusc = "aak";
    
    /* method World.computeBlockLightValue(IIILnet/minecraft/world/EnumSkyBlock;)I aka func_98179_a*/
    private final String computeBlockLightMethodNameO = "a";
    
    /* class net.minecraft.world.EnumSkyBlock */
    private final String enumSkyBlockObfusc = "aam";
    
    
    private final String classNameWorld = "net.minecraft.world.World";
    private final String blockAccessJava = "net/minecraft/world/IBlockAccess";
    private final String computeBlockLightMethodName = "computeLightValue";
    
    
    @Override
    public byte[] transform(String name, String newName, byte[] bytes)
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
            if (m.name.equals( obfuscated ? computeBlockLightMethodNameO : computeBlockLightMethodName)
            && m.desc.equals( obfuscated ? "(IIIL"+enumSkyBlockObfusc+";)I" : "(IIILnet/minecraft/world/EnumSkyBlock;)I"))
            {
                System.out.println("In target method! Patching!");
                
                AbstractInsnNode targetNode = null;
                Iterator<AbstractInsnNode> iter = m.instructions.iterator();
                boolean deleting = false;
                boolean replacing = false;
                while (iter.hasNext())
                {
                    targetNode = (AbstractInsnNode) iter.next();
                    
                    if (targetNode instanceof VarInsnNode)
                    {
                        VarInsnNode vNode = (VarInsnNode) targetNode;
                        if (vNode.var == 6)
                        {
                            if (vNode.getOpcode() == ASTORE)
                            {
                                System.out.println("Bytecode ASTORE 6 case!");
                                deleting = true;
                                continue;
                            }
                            else if (vNode.getOpcode() == ISTORE)
                            {
                                System.out.println("Bytecode ISTORE 6 case!");
                                replacing = true;
                                targetNode = (AbstractInsnNode) iter.next();
                                break;
                            }
                        }
                        
                        if (vNode.var == 7 && deleting)
                        {
                            break;
                        }
                    }
                    
                    if (deleting)
                    {
                        System.out.println("Removing "+targetNode);
                        iter.remove();
                    }
                }
                
                // make new instruction list
                InsnList toInject = new InsnList();
                
                // argument mapping! 0 is World, 5 is blockID, 123 are xyz
                toInject.add(new VarInsnNode(ALOAD, 0));
                toInject.add(new VarInsnNode(ILOAD, 5));
                toInject.add(new VarInsnNode(ILOAD, 1));
                toInject.add(new VarInsnNode(ILOAD, 2));
                toInject.add(new VarInsnNode(ILOAD, 3));
                toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/dynamiclights/client/DynamicLights", "getLightValue", "(L"+(obfuscated ? classNameBlockAccessObfusc : blockAccessJava)+";IIII)I"));
                if (replacing)
                {
                    toInject.add(new VarInsnNode(ISTORE, 6));
                }
                
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

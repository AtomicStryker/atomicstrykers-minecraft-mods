package atomicstryker.stalkercreepers.common;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;



public class SCTransformer implements IClassTransformer
{
    /* Obfuscated Names for EntityAICreeperSwell Transformation */
    
    /* class net.minecraft.src.EntityAICreeperSwell */
    private final String entityAICreeperSwellClassNameO = "aae";
    private final String entityAICreeperSwellJavaClassNameO = "aae";
    /* class net.minecraft.src.EntityLiving */
    private final String entityLivingJavaClassNameO = "xn";
    /* class net.minecraft.src.EntityCreeper */
    private final String entityCreeperJavaClassNameO = "aep";
    /*shouldExecute() / func_75250_a */
    private final String shouldExecuteMethodNameO = "a";
    /*swellingCreeper / field_75269_a */
    private final String swellingCreeperFieldNameO = "a";
    
    /* MCP Names for EntityAICreeperSwell Transformation */
    
    private final String entityAICreeperSwellClassName = "net.minecraft.entity.ai.EntityAICreeperSwell";
    private final String entityAICreeperSwellJavaClassName = "net/minecraft/entity/ai/EntityAICreeperSwell";
    private final String shouldExecuteMethodName = "shouldExecute";
    private final String swellingCreeperFieldName = "swellingCreeper";
    private final String entityLivingJavaClassName = "net/minecraft/entity/EntityLiving";
    private final String entityCreeperJavaClassName = "net/minecraft/entity/monster/EntityCreeper";
    
    @Override
    public byte[] transform(String name, String newName, byte[] bytes)
    {
        //System.out.println("transforming: "+name);
        if (name.equals(entityAICreeperSwellClassNameO))
        {
            return handleEntityAICreeperSwellObfuscated(bytes);
        }
        else if (name.equals(entityAICreeperSwellClassName))
        {
            return handleEntityAICreeperSwell(bytes);
        }
        
        return bytes;
    }
    
    private byte[] handleEntityAICreeperSwellObfuscated(byte[] bytes)
    {
        System.out.println("**************** Stalker Creepers transform running on EntityAICreeperSwell *********************** ");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.name.equals(shouldExecuteMethodNameO) && m.desc.equals("()Z"))
            {
                System.out.println("In target method! Patching!");
                
                // find interesting instructions in method, there is a single ICONST_1 instruction we use as target
                AbstractInsnNode nodeTarget = null;
                for (int index = 0; index < m.instructions.size(); index++)
                {
                    AbstractInsnNode curNode = m.instructions.get(index);
                    if (curNode.getOpcode() == ICONST_1)
                    {
                        nodeTarget = curNode;
                    }
                }
                
                if (nodeTarget == null)
                {
                    System.out.println("Did not find all necessary target nodes! ABANDON CLASS!");
                    return bytes;
                }
                
                // make new instruction list
                InsnList toInject = new InsnList();
                
                // make an exit label node
                LabelNode exitLabelNode = new LabelNode(new Label());
                               
                toInject.add(new VarInsnNode(ALOAD, 0));
                toInject.add(new FieldInsnNode(GETFIELD, entityAICreeperSwellJavaClassNameO, swellingCreeperFieldNameO, "L"+entityCreeperJavaClassNameO+";"));
                
                try
                {
                    try
                    {
                        AbstractInsnNode node = MethodInsnNode.class.getConstructor(int.class, String.class, String.class, String.class).newInstance(
                                INVOKESTATIC, "atomicstryker/stalkercreepers/common/EntityAIHelperStalker", "isSeenByTarget", "(L"+entityLivingJavaClassNameO+";)Z");
                        toInject.add(node);
                    }
                    catch (NoSuchMethodException e)
                    {
                        AbstractInsnNode node = MethodInsnNode.class.getConstructor(int.class, String.class, String.class, String.class, boolean.class).newInstance(
                                INVOKESTATIC, "atomicstryker/stalkercreepers/common/EntityAIHelperStalker", "isSeenByTarget", "(L"+entityLivingJavaClassNameO+";)Z", false);
                        toInject.add(node);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    System.out.println("Stalker Creepers ASM transform failed T_T");
                    return bytes;
                }
                
                toInject.add(new JumpInsnNode(IFNE, exitLabelNode));
                toInject.add(new InsnNode(ICONST_0));
                toInject.add(new InsnNode(IRETURN));
                toInject.add(exitLabelNode);
                
                // inject new instruction list into method instruction list
                m.instructions.insertBefore(nodeTarget, toInject);
                
                System.out.println("Patching Complete!");
                break;
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
    
    private byte[] handleEntityAICreeperSwell(byte[] bytes)
    {
        System.out.println("**************** Stalker Creepers transform running on EntityAICreeperSwell *********************** ");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.name.equals(shouldExecuteMethodName) && m.desc.equals("()Z"))
            {
                System.out.println("In target method! Patching!");
                
                // find interesting instructions in method, there is a single ICONST_1 instruction we use as target
                AbstractInsnNode nodeTarget = null;
                for (int index = 0; index < m.instructions.size(); index++)
                {
                    AbstractInsnNode curNode = m.instructions.get(index);
                    if (curNode.getOpcode() == ICONST_1)
                    {
                        nodeTarget = curNode;
                    }
                }
                
                if (nodeTarget == null)
                {
                    System.out.println("Did not find all necessary target nodes! ABANDON CLASS!");
                    return bytes;
                }
                
                // make new instruction list
                InsnList toInject = new InsnList();
                
                // make an exit label node
                LabelNode exitLabelNode = new LabelNode(new Label());
                                
                toInject.add(new VarInsnNode(ALOAD, 0));
                toInject.add(new FieldInsnNode(GETFIELD, entityAICreeperSwellJavaClassName, swellingCreeperFieldName, "L"+entityCreeperJavaClassName+";"));
                
                try
                {
                    try
                    {
                        AbstractInsnNode node = MethodInsnNode.class.getConstructor(int.class, String.class, String.class, String.class).newInstance(
                                INVOKESTATIC, "atomicstryker/stalkercreepers/common/EntityAIHelperStalker", "isSeenByTarget", "(L"+entityLivingJavaClassName+";)Z");
                        toInject.add(node);
                    }
                    catch (NoSuchMethodException e)
                    {
                        AbstractInsnNode node = MethodInsnNode.class.getConstructor(int.class, String.class, String.class, String.class, boolean.class).newInstance(
                                INVOKESTATIC, "atomicstryker/stalkercreepers/common/EntityAIHelperStalker", "isSeenByTarget", "(L"+entityLivingJavaClassName+";)Z", false);
                        toInject.add(node);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    System.out.println("Stalker Creepers ASM transform failed T_T");
                    return bytes;
                }
                
                toInject.add(new JumpInsnNode(IFNE, exitLabelNode));
                toInject.add(new InsnNode(ICONST_0));
                toInject.add(new InsnNode(IRETURN));
                toInject.add(exitLabelNode);
                
                // inject new instruction list into method instruction list
                m.instructions.insertBefore(nodeTarget, toInject);
                
                System.out.println("Patching Complete!");
                break;
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}

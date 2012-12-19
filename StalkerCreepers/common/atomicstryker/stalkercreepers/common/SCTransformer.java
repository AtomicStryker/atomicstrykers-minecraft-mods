package atomicstryker.stalkercreepers.common;

import static org.objectweb.asm.Opcodes.*;

import java.util.Iterator;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import cpw.mods.fml.relauncher.IClassTransformer;

public class SCTransformer implements IClassTransformer
{
    /* Obfuscated Names for EntityAICreeperSwell Transformation */
    
    /* class net.minecraft.src.EntityAICreeperSwell */
    private final String entityAICreeperSwellClassNameO = "ob";
    private final String entityAICreeperSwellJavaClassNameO = "ob";
    /* class net.minecraft.src.EntityLiving */
    private final String entityLivingJavaClassNameO = "md";
    /* class net.minecraft.src.EntityCreeper */
    private final String entityCreeperJavaClassNameO = "qc";
    /* method shouldExecute(), unlikely to change */
    private final String shouldExecuteMethodNameO = "a";
    /* field swellingCreeper, unlikely to change */
    private final String swellingCreeperFieldNameO = "a";
    
    /* MCP Names for EntityAICreeperSwell Transformation */
    
    private final String entityAICreeperSwellClassName = "net.minecraft.entity.ai.EntityAICreeperSwell";
    private final String entityAICreeperSwellJavaClassName = "net/minecraft/entity/ai/EntityAICreeperSwell";
    private final String shouldExecuteMethodName = "shouldExecute";
    private final String swellingCreeperFieldName = "swellingCreeper";
    private final String entityLivingJavaClassName = "net/minecraft/entity/EntityLiving";
    private final String entityCreeperJavaClassName = "net/minecraft/entity/monster/EntityCreeper";
    
    @Override
    public byte[] transform(String name, byte[] bytes)
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
                toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/stalkercreepers/common/EntityAIHelperStalker", "isSeenByTarget", "(L"+entityLivingJavaClassNameO+";)Z"));
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
                toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/stalkercreepers/common/EntityAIHelperStalker", "isSeenByTarget", "(L"+entityLivingJavaClassName+";)Z"));
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

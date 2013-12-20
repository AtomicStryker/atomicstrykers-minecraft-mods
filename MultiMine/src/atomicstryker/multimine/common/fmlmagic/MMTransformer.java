package atomicstryker.multimine.common.fmlmagic;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.RETURN;

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

public class MMTransformer implements IClassTransformer
{
    /* Obfuscated Names for NetServerHandler Transformation */
    
    /* net/minecraft/network/packet/Packet14BlockDig */
    private final String packet14BlockDigNameO = "fb";
    /* net.minecraft.network.NetServerHandler */
    private final String netServerHandlerClassNameO = "ka";
    private final String netServerHandlerJavaClassNameO = "ka";
    /* handleBlockDig / func_72510_a */
    private final String netServerHandlertargetMethodNameO = "a";
    /* playerEntity / field_72574_e */
    private final String netServerHandlerEntIDFieldNameO = "c";
    /* net/minecraft/src/EntityPlayerMP */
    private final String entityPlayerMPJavaClassNameO = "jv";
    /* entityId / field_70157_k */
    private final String entityPlayerMPEntIDFieldNameO = "k";
    
    
    /* Obfuscated Names for PlayerControllerMP Transformation */
    
    /* net.minecraft.client.multiplayer.PlayerControllerMP */
    private final String playerControllerMPClassNameO = "bdc";
    private final String playerControllerMPJavaClassNameO = "bdc";
    /* onPlayerDamageBlock / func_78759_c */
    private final String playerControllerMPtargetMethodNameO = "c";
    /* currentBlockX / field_78775_c */
    private final String playerControllerMPcurrentBlockXFieldNameO = "c";
    /* currentBlockY / field_78772_d */
    private final String playerControllerMPcurrentBlockYFieldNameO = "d";
    /* currentBlockZ / field_78773_e */
    private final String playerControllerMPcurrentBlockZFieldNameO = "e";
    /* curBlockDamageMP / field_78770_f */
    private final String playerControllerMPcurrentBlockDamageFieldNameO = "g";
    
    
    /* MCP Names for PlayerControllerMP Transformation */
    private final String playerControllerMPClassName = "net.minecraft.client.multiplayer.PlayerControllerMP";
    private final String playerControllerMPJavaClassName = "net/minecraft/client/multiplayer/PlayerControllerMP";
    private final String playerControllerMPtargetMethodName = "onPlayerDamageBlock";
    private final String playerControllerMPcurrentBlockXFieldName = "currentBlockX";
    private final String playerControllerMPcurrentBlockYFieldName = "currentBlockY";
    private final String playerControllerMPcurrentBlockZFieldName = "currentblockZ";
    private final String playerControllerMPcurrentBlockDamageFieldName = "curBlockDamageMP";
    
    /* MCP Names for NetServerHandler Transformation */
    private final String packet14BlockDigName = "net/minecraft/network/packet/Packet14BlockDig";
    private final String netServerHandlerClassName = "net.minecraft.network.NetServerHandler";
    private final String netServerHandlerJavaClassName = "net/minecraft/network/NetServerHandler";
    private final String netServerHandlertargetMethodName = "handleBlockDig";
    private final String netServerHandlerEntIDFieldName = "playerEntity";
    private final String entityPlayerMPJavaClassName = "net/minecraft/entity/player/EntityPlayerMP";
    private final String entityPlayerMPEntIDFieldName = "entityId";
    
    @Override
    public byte[] transform(String name, String newName, byte[] bytes)
    {
        //System.out.println("transforming: "+name);
        if (name.equals(netServerHandlerClassNameO))
        {
            return handleNetServerHandlerObfuscated(bytes);
        }
        else if (name.equals(playerControllerMPClassNameO))
        {
            return handlePlayerControllerMPObfuscated(bytes);
        }
        else if (name.equals(netServerHandlerClassName))
        {
            return handleNetServerHandler(bytes);
        }
        else if (name.equals(playerControllerMPClassName))
        {
            return handlePlayerControllerMP(bytes);
        }
        
        return bytes;
    }
    
    private byte[] handlePlayerControllerMPObfuscated(byte[] bytes)
    {
        System.out.println("**************** Multi Mine transform running on PlayerControllerMP *********************** ");
        
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.name.equals(playerControllerMPtargetMethodNameO) && m.desc.equals("(IIII)V"))
            {
                System.out.println("In target method! Patching!");
                
                // find injection point in method, there is a single IFLT instruction we use as target
                for (int index = 0; index < m.instructions.size(); index++)
                {
                    // find block ID local variable node and from that, local variable index
                    int blockIDvar = 5;
                    if (m.instructions.get(index).getType() == AbstractInsnNode.VAR_INSN
                    && m.instructions.get(index).getOpcode() == ISTORE)
                    {
                        System.out.println("Found local variable ISTORE Node at "+index);
                        VarInsnNode blockIDNode = (VarInsnNode)m.instructions.get(index);
                        blockIDvar = blockIDNode.var;
                        System.out.println("Block ID is in local variable "+blockIDvar);
                    }
                    
                    if (m.instructions.get(index).getOpcode() == IFLT)
                    {
                        System.out.println("Found IFLT Node at "+index);
                        
                        int offset = 1;
                        while (m.instructions.get(index-offset).getOpcode() != ALOAD)
                        {
                            offset++;
                        }
                        
                        System.out.println("Found ALOAD Node at offset -"+offset+" from IFLT Node");
                        
                        // make an exit label node
                        LabelNode elseJumpNode = new LabelNode(new Label());
                        
                        // make new instruction list
                        InsnList toInject = new InsnList();
                        
                        // construct instruction nodes for list                        
                        toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/multimine/client/MultiMineClient", "instance", "()Latomicstryker/multimine/client/MultiMineClient;"));
                        toInject.add(new VarInsnNode(ILOAD, blockIDvar));
                        toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "atomicstryker/multimine/client/MultiMineClient", "getIsEnabledForServerAndBlockId", "(I)Z"));
                        toInject.add(new JumpInsnNode(IFEQ, elseJumpNode));
                        
                        toInject.add(new VarInsnNode(ALOAD, 0)); // stash an object ref for the final putfield on the stack
                        toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/multimine/client/MultiMineClient", "instance", "()Latomicstryker/multimine/client/MultiMineClient;"));                     
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, playerControllerMPJavaClassNameO, playerControllerMPcurrentBlockXFieldNameO, "I"));
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, playerControllerMPJavaClassNameO, playerControllerMPcurrentBlockYFieldNameO, "I"));
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, playerControllerMPJavaClassNameO, playerControllerMPcurrentBlockZFieldNameO, "I"));
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, playerControllerMPJavaClassNameO, playerControllerMPcurrentBlockDamageFieldNameO, "F"));
                        toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "atomicstryker/multimine/client/MultiMineClient", "eventPlayerDamageBlock", "(IIIF)F"));
                        toInject.add(new FieldInsnNode(PUTFIELD, playerControllerMPJavaClassNameO, playerControllerMPcurrentBlockDamageFieldNameO, "F"));
                        
                        toInject.add(elseJumpNode);
                        
                        m.instructions.insertBefore(m.instructions.get(index-offset), toInject);
                        System.out.println("Patching Complete!");
                        break;
                    }
                }
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
    
    private byte[] handleNetServerHandlerObfuscated(byte[] bytes)
    {
        System.out.println("**************** Multi Mine transform running on NetServerHandler *********************** ");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.name.equals(netServerHandlertargetMethodNameO) && m.desc.equals("(L"+packet14BlockDigNameO+";)V"))
            {
                System.out.println("In target method! Patching!");
                
                // make an exit label node
                LabelNode lmm2Node = new LabelNode(new Label());
                
                // make new instruction list
                InsnList toInject = new InsnList();
                
                // construct instruction nodes for list
                toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/multimine/common/MultiMineServer", "instance", "()Latomicstryker/multimine/common/MultiMineServer;"));
                toInject.add(new VarInsnNode(ALOAD, 0));
                toInject.add(new FieldInsnNode(GETFIELD, netServerHandlerJavaClassNameO, netServerHandlerEntIDFieldNameO, "L"+entityPlayerMPJavaClassNameO+";"));
                toInject.add(new FieldInsnNode(GETFIELD, entityPlayerMPJavaClassNameO, entityPlayerMPEntIDFieldNameO, "I"));
                toInject.add(new VarInsnNode(ALOAD, 1));
                toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "atomicstryker/multimine/common/MultiMineServer", "getShouldIgnoreBlockDigPacket", "(IL"+packet14BlockDigNameO+";)Z"));
                toInject.add(new JumpInsnNode(IFEQ, lmm2Node));
                toInject.add(new InsnNode(RETURN));
                toInject.add(lmm2Node);
                
                // inject new instruction list into method instruction list
                m.instructions.insert(toInject);
                
                System.out.println("Patching Complete!");
                break;
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
    
    private byte[] handlePlayerControllerMP(byte[] bytes)
    {
        System.out.println("**************** Multi Mine transform running on PlayerControllerMP *********************** ");
        
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.name.equals(playerControllerMPtargetMethodName) && m.desc.equals("(IIII)V"))
            {
                System.out.println("In target method! Patching!");
                
                // find injection point in method, there is a single IFLT instruction we use as target
                for (int index = 0; index < m.instructions.size(); index++)
                {
                    // find block ID local variable node and from that, local variable index
                    int blockIDvar = 5;
                    if (m.instructions.get(index).getType() == AbstractInsnNode.VAR_INSN
                    && m.instructions.get(index).getOpcode() == ISTORE)
                    {
                        System.out.println("Found local variable ISTORE Node at "+index);
                        VarInsnNode blockIDNode = (VarInsnNode)m.instructions.get(index);
                        blockIDvar = blockIDNode.var;
                        System.out.println("Block ID is in local variable "+blockIDvar);
                    }
                    
                    if (m.instructions.get(index).getOpcode() == IFLT)
                    {
                        System.out.println("Found IFLT Node at "+index);
                        
                        int offset = 1;
                        while (m.instructions.get(index-offset).getOpcode() != ALOAD)
                        {
                            offset++;
                        }
                        
                        System.out.println("Found ALOAD Node at offset -"+offset+" from IFLT Node");
                        
                        // make an exit label node
                        LabelNode lmm1Node = new LabelNode(new Label());
                        
                        // make new instruction list
                        InsnList toInject = new InsnList();
                        
                        // construct instruction nodes for list
                        toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/multimine/client/MultiMineClient", "instance", "()Latomicstryker/multimine/client/MultiMineClient;"));
                        toInject.add(new VarInsnNode(ILOAD, blockIDvar));
                        toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "atomicstryker/multimine/client/MultiMineClient", "getIsEnabledForServerAndBlockId", "(I)Z"));
                        toInject.add(new JumpInsnNode(IFEQ, lmm1Node));
                        toInject.add(new VarInsnNode(ALOAD, 0)); // stash an object ref for the final putfield on the stack
                        toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/multimine/client/MultiMineClient", "instance", "()Latomicstryker/multimine/client/MultiMineClient;"));
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, playerControllerMPJavaClassName, playerControllerMPcurrentBlockXFieldName, "I"));
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, playerControllerMPJavaClassName, playerControllerMPcurrentBlockYFieldName, "I"));
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, playerControllerMPJavaClassName, playerControllerMPcurrentBlockZFieldName, "I"));
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, playerControllerMPJavaClassName, playerControllerMPcurrentBlockDamageFieldName, "F"));
                        toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "atomicstryker/multimine/client/MultiMineClient", "eventPlayerDamageBlock", "(IIIF)F"));
                        toInject.add(new FieldInsnNode(PUTFIELD, playerControllerMPJavaClassName, playerControllerMPcurrentBlockDamageFieldName, "F"));
                        
                        toInject.add(lmm1Node);
                        
                        m.instructions.insertBefore(m.instructions.get(index-offset), toInject);
                        System.out.println("Patching Complete!");
                        break;
                    }
                }
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
    
    private byte[] handleNetServerHandler(byte[] bytes)
    {
        System.out.println("**************** Multi Mine transform running on NetServerHandler *********************** ");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.name.equals(netServerHandlertargetMethodName) && m.desc.equals("(L"+packet14BlockDigName+";)V"))
            {
                System.out.println("In target method! Patching!");
                
                // make an exit label node
                LabelNode lmm2Node = new LabelNode(new Label());
                
                // make new instruction list
                InsnList toInject = new InsnList();
                
                // construct instruction nodes for list
                toInject.add(new MethodInsnNode(INVOKESTATIC, "atomicstryker/multimine/common/MultiMineServer", "instance", "()Latomicstryker/multimine/common/MultiMineServer;"));
                toInject.add(new VarInsnNode(ALOAD, 0));
                toInject.add(new FieldInsnNode(GETFIELD, netServerHandlerJavaClassName, netServerHandlerEntIDFieldName, "L"+entityPlayerMPJavaClassName+";"));
                toInject.add(new FieldInsnNode(GETFIELD, entityPlayerMPJavaClassName, entityPlayerMPEntIDFieldName, "I"));
                toInject.add(new VarInsnNode(ALOAD, 1));
                toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "atomicstryker/multimine/common/MultiMineServer", "getShouldIgnoreBlockDigPacket", "(IL"+packet14BlockDigName+";)Z"));
                toInject.add(new JumpInsnNode(IFEQ, lmm2Node));
                toInject.add(new InsnNode(RETURN));
                toInject.add(lmm2Node);
                
                // inject new instruction list into method instruction list
                m.instructions.insert(toInject);
                
                System.out.println("Patching Complete!");
                break;
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}

package atomicstryker.multimine.common.fmlmagic;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.PUTFIELD;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class MMTransformer implements IClassTransformer
{
    /* Obfuscated Names for PlayerControllerMP Transformation */

    /* net.minecraft.client.multiplayer.PlayerControllerMP */
    private final String playerControllerMPClassNameO = "bje";
    private final String playerControllerMPJavaClassNameO = "bje";
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

    private boolean obfuscation;

    @Override
    public byte[] transform(String name, String newName, byte[] bytes)
    {
        // System.out.println("transforming: "+name);
        if (name.equals(playerControllerMPClassNameO))
        {
            obfuscation = true;
            return handlePlayerControllerMP(bytes);
        }
        else if (name.equals(playerControllerMPClassName))
        {
            obfuscation = false;
            return handlePlayerControllerMP(bytes);
        }

        return bytes;
    }

    private byte[] handlePlayerControllerMP(byte[] bytes)
    {
        System.out.println("**************** Multi Mine transform running on PlayerControllerMP, obfuscated: " + obfuscation + " *********************** ");

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while (methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.name.equals(getTargetMethodName()) && m.desc.equals("(IIII)V"))
            {
                System.out.println("In target method! Patching!");

                // find injection point in method, there is a single IFLT instruction we use as target
                for (int index = 0; index < m.instructions.size(); index++)
                {
                    // find block ID local variable node and from that, local variable index
                    int blockIDvar = 5;
                    if (m.instructions.get(index).getType() == AbstractInsnNode.VAR_INSN && m.instructions.get(index).getOpcode() == ISTORE)
                    {
                        System.out.println("Found local variable ISTORE Node at " + index);
                        VarInsnNode blockIDNode = (VarInsnNode) m.instructions.get(index);
                        blockIDvar = blockIDNode.var;
                        System.out.println("Block ID is in local variable " + blockIDvar);
                    }

                    if (m.instructions.get(index).getOpcode() == IFLT)
                    {
                        System.out.println("Found IFLT Node at " + index);

                        int offset = 1;
                        while (m.instructions.get(index - offset).getOpcode() != ALOAD)
                        {
                            offset++;
                        }

                        System.out.println("Found ALOAD Node at offset -" + offset + " from IFLT Node");

                        // make an exit label node
                        LabelNode lmm1Node = new LabelNode(new Label());

                        // make new instruction list
                        InsnList toInject = new InsnList();

                        // construct instruction nodes for list
                        
                        // stash an object ref for the final putfield on the stack
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        
                        try
                        {
                            try
                            {
                                AbstractInsnNode node = MethodInsnNode.class.getConstructor(int.class, String.class, String.class, String.class).newInstance(
                                        INVOKESTATIC, "atomicstryker/multimine/client/MultiMineClient", "instance", "()Latomicstryker/multimine/client/MultiMineClient;");
                                toInject.add(node);
                            }
                            catch (NoSuchMethodException e)
                            {
                                AbstractInsnNode node = MethodInsnNode.class.getConstructor(int.class, String.class, String.class, String.class, boolean.class).newInstance(
                                        INVOKESTATIC, "atomicstryker/multimine/client/MultiMineClient", "instance", "()Latomicstryker/multimine/client/MultiMineClient;", false);
                                toInject.add(node);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            System.out.println("Multi Mine ASM transform failed T_T");
                            return bytes;
                        }
                        
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, getPlayerControllerClassName(), getCurBlockXName(), "I"));
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, getPlayerControllerClassName(), getCurBlockYName(), "I"));
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, getPlayerControllerClassName(), getCurBlockZName(), "I"));
                        toInject.add(new VarInsnNode(ALOAD, 0));
                        toInject.add(new FieldInsnNode(GETFIELD, getPlayerControllerClassName(), getCurBlockDamageName(), "F"));
                        
                        try
                        {
                            try
                            {
                                AbstractInsnNode node = MethodInsnNode.class.getConstructor(int.class, String.class, String.class, String.class).newInstance(
                                        INVOKEVIRTUAL, "atomicstryker/multimine/client/MultiMineClient", "eventPlayerDamageBlock", "(IIIF)F");
                                toInject.add(node);
                            }
                            catch (NoSuchMethodException e)
                            {
                                AbstractInsnNode node = MethodInsnNode.class.getConstructor(int.class, String.class, String.class, String.class, boolean.class).newInstance(
                                        INVOKEVIRTUAL, "atomicstryker/multimine/client/MultiMineClient", "eventPlayerDamageBlock", "(IIIF)F", false);
                                toInject.add(node);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            System.out.println("Multi Mine ASM transform failed T_T");
                            return bytes;
                        }
                        
                        toInject.add(new FieldInsnNode(PUTFIELD, getPlayerControllerClassName(), getCurBlockDamageName(), "F"));
                        toInject.add(lmm1Node);

                        m.instructions.insertBefore(m.instructions.get(index - offset), toInject);
                        break;
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        System.out.println("Patching Complete! Writing class bytes now.");
        return writer.toByteArray();
    }

    private String getTargetMethodName()
    {
        return obfuscation ? playerControllerMPtargetMethodNameO : playerControllerMPtargetMethodName;
    }

    private String getPlayerControllerClassName()
    {
        return obfuscation ? playerControllerMPJavaClassNameO : playerControllerMPJavaClassName;
    }

    private String getCurBlockXName()
    {
        return obfuscation ? playerControllerMPcurrentBlockXFieldNameO : playerControllerMPcurrentBlockXFieldName;
    }

    private String getCurBlockYName()
    {
        return obfuscation ? playerControllerMPcurrentBlockYFieldNameO : playerControllerMPcurrentBlockYFieldName;
    }

    private String getCurBlockZName()
    {
        return obfuscation ? playerControllerMPcurrentBlockZFieldNameO : playerControllerMPcurrentBlockZFieldName;
    }

    private String getCurBlockDamageName()
    {
        return obfuscation ? playerControllerMPcurrentBlockDamageFieldNameO : playerControllerMPcurrentBlockDamageFieldName;
    }
}

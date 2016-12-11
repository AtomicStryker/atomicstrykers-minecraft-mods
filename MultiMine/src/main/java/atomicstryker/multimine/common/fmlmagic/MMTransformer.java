package atomicstryker.multimine.common.fmlmagic;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class MMTransformer implements IClassTransformer
{
    /* Obfuscated Names for PlayerControllerMP Transformation */

    /* net.minecraft.client.multiplayer.PlayerControllerMP */
    private final String playerControllerMPClassNameO = "bnn";
    private final String playerControllerMPJavaClassNameO = "bnn";

    /* onPlayerDamageBlock / func_180512_c */
    private final String playerControllerMPtargetMethodNameO = "b";

    /* method desc of onPlayerDamageBlock / func_180512_c */
    private final String methodDescO = "(Lco;Lcv;)Z";
    private final String methodDesc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z";

    /* method desc of call to client hook */
    private final String methodDescCallO = "(Lco;F)F";
    private final String methodDescCall = "(Lnet/minecraft/util/math/BlockPos;F)F";

    /* curBlockDamageMP / field_78770_f */
    private final String playerControllerMPcurrentBlockDamageFieldNameO = "e";

    /* MCP Names for PlayerControllerMP Transformation */
    private final String playerControllerMPClassName = "net.minecraft.client.multiplayer.PlayerControllerMP";
    private final String playerControllerMPJavaClassName = "net/minecraft/client/multiplayer/PlayerControllerMP";

    private final String playerControllerMPtargetMethodName = "onPlayerDamageBlock";

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
        for (MethodNode m : classNode.methods)
        {
            if (m.name.equals(getTargetMethodName()) && m.desc.equals(getTargetMethodDesc()))
            {
                System.out.println("In target method " + getTargetMethodName() + "! Patching!");
                
                /*  pre patch, java source
                 
	                if (this.curBlockDamageMP >= 1.0F)
	                {
	                    this.isHittingBlock = false;
	                    this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, p_180512_1_, p_180512_2_));
	                    this.onPlayerDestroyBlock(p_180512_1_, p_180512_2_);
	                    this.curBlockDamageMP = 0.0F;
	                    this.stepSoundTickCounter = 0.0F;
	                    this.blockHitDelay = 5;
	                }
                 */
                
                /* pre patch, obfuscated bytecode

				     258: aload_0       
				     259: getfield      #67                 // Field e:F
				     262: fconst_1      
				     263: fcmpl   
				     258: aload_0       
				     259: getfield      #67                 // Field e:F
				     262: fconst_1      
				     263: fcmpl         
				     264: iflt          313
				     267: aload_0       
				     268: iconst_0      
				     269: putfield      #70                 // Field h:Z
				     272: aload_0       
				     273: getfield      #64                 // Field b:Lcee;
				     276: new           #39                 // class ml
				     279: dup           
				     280: getstatic     #83                 // Field mm.c:Lmm;
				     283: aload_1       
				     284: aload_2       
				     285: invokespecial #158                // Method ml."<init>":(Lmm;Ldt;Lej;)V
				     288: invokevirtual #124                // Method cee.a:(Lid;)V
				     291: aload_0       
				     292: aload_1       
				     293: aload_2       
				     294: invokevirtual #127                // Method a:(Ldt;Lej;)Z
				     297: pop           
				     298: aload_0       
				     299: fconst_0      
				     300: putfield      #67                 // Field e:F
				     303: aload_0       
				     304: fconst_0      
				     305: putfield      #68                 // Field f:F
				     308: aload_0       
				     309: iconst_5      
				     310: putfield      #69                 // Field g:I
                 */

                for (int index = 0; index < m.instructions.size(); index++)
                {
                    // find injection point in method, there is a single IFLT instruction we use as target
                    if (m.instructions.get(index).getOpcode() == Opcodes.IFLT)
                    {
                        System.out.println("Found IFLT Node at " + index);

                        // from there, step backwards to ALOAD node to get infront of the curBlockDamageMP field load
                        int offset = 1;
                        while (m.instructions.get(index - offset).getOpcode() != Opcodes.ALOAD)
                        {
                            offset++;
                        }

                        System.out.println("Found ALOAD Node at offset -" + offset + " from IFLT Node");
                        
                        /* this is the code we want to patch in, in bytecode
                            0: aload_0
	 				       1: invokestatic  #38                 // Method atomicstryker/multimine/client/MultiMineClient.instance:()Latomicstryker/multimine/client/MultiMineClient;
	 				       4: aload_1       
	 				       5: aload_0       
	 				       6: getfield      #44                 // Field curBlockDamageMP:F which is e:F
	 				       9: invokevirtual #46                 // Method atomicstryker/multimine/client/MultiMineClient.eventPlayerDamageBlock:(Lnet/minecraft/util/BlockPos;F)F
	 				      12: putfield      #44                 // Field curBlockDamageMP:F which is e:F
                         */

                        // construct it using asm, insert it before 'if (this.curBlockDamageMP >= 1.0F)'
                        InsnList toInject = new InsnList();
                        toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        toInject.add(
                                new MethodInsnNode(Opcodes.INVOKESTATIC, "atomicstryker/multimine/client/MultiMineClient", "instance", "()Latomicstryker/multimine/client/MultiMineClient;", false));
                        toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        toInject.add(new FieldInsnNode(Opcodes.GETFIELD, getPlayerControllerClassName(), getCurBlockDamageName(), "F"));
                        toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "atomicstryker/multimine/client/MultiMineClient", "eventPlayerDamageBlock", getTargetMethodCallDesc(), false));
                        toInject.add(new FieldInsnNode(Opcodes.PUTFIELD, getPlayerControllerClassName(), getCurBlockDamageName(), "F"));
                        m.instructions.insertBefore(m.instructions.get(index - offset), toInject);

                        // in effect, we added this line of code: 'this.curBlockDamageMP = atomicstryker.multimine.client.MultiMineClient.instance().eventPlayerDamageBlock(blockPos, curBlockDamageMP);'
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

    private String getTargetMethodDesc()
    {
        return obfuscation ? methodDescO : this.methodDesc;
    }

    private String getTargetMethodCallDesc()
    {
        return obfuscation ? methodDescCallO : methodDescCall;
    }

    private String getPlayerControllerClassName()
    {
        return obfuscation ? playerControllerMPJavaClassNameO : playerControllerMPJavaClassName;
    }

    private String getCurBlockDamageName()
    {
        return obfuscation ? playerControllerMPcurrentBlockDamageFieldNameO : playerControllerMPcurrentBlockDamageFieldName;
    }
}

package alexiil.mods.load.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.client.FMLClientHandler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import alexiil.mods.load.ProgressDisplayer;

public class BetterLoadingScreenTransformer implements IClassTransformer, Opcodes {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.Minecraft"))
            return transformMinecraft(basicClass, transformedName == name);
        if (name.equals("com.mumfrey.liteloader.client.api.ObjectFactoryClient"))
            return transformObjectFactoryClient(basicClass);
        return basicClass;
    }

    private byte[] transformObjectFactoryClient(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        for (MethodNode m : classNode.methods) {
            if (m.name.equals("preBeginGame")) {
                m.instructions.clear();
                m.instructions.add(new TypeInsnNode(NEW, "alexiil/mods/load/LiteLoaderProgress"));
                m.instructions.add(new MethodInsnNode(INVOKESPECIAL, "alexiil/mods/load/LiteLoaderProgress", "<init>", "()V", false));
                m.instructions.add(new InsnNode(RETURN));
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private byte[] transformMinecraft(byte[] before, boolean dev) {
        boolean hasFoundStartGame = false;
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        boolean found = false;
        String tryingToFind = dev ? "drawSplashScreen" : "func_180510_a";
        String minecraftStartGame = dev ? "startGame" : "func_71384_a";

        for (MethodNode m : classNode.methods) {
            if (m.name.equals(tryingToFind)) {
                found = true;
                m.instructions.insertBefore(m.instructions.getFirst(), new InsnNode(Opcodes.RETURN));
                // just return from the method, as if nothing happened
                break;
            }
            if (m.name.equals(minecraftStartGame)) {
                for (int i = 0; i < m.instructions.size(); i++) {
                    /* LiteLoader disabling -NOTE TO ANYONE FROM LITELOADER OR ANYONE ELSE: I am disabling liteloader's
                     * overlay simply because otherwise it switches between liteloader's bar and mine. I can safely
                     * assume that people won't wont LiteLoader's as they are using my mod, which is just a progress
                     * bar, they can disable this behaviour by removing my mod (as all my mod does is just add a loading
                     * bar) */
                    AbstractInsnNode node = m.instructions.get(i);
                    if (node instanceof MethodInsnNode) {
                        MethodInsnNode method = (MethodInsnNode) node;
                        if (method.owner.equals("com/mumfrey/liteloader/client/gui/startup/LoadingBar")) {
                            m.instructions.remove(method);
                            continue;
                        }
                        else if (method.owner.startsWith("com/mumfrey")) {
                            System.out.println("Started with \"com/mumfrey\", was actually \"" + method.owner + "\"");
                        }
                    }

                    // LiteLoader removing end
                    if (node instanceof MethodInsnNode) {
                        MethodInsnNode method = (MethodInsnNode) node;
                        if (method.owner.equals(Type.getInternalName(FMLClientHandler.class)) && method.name.equals("instance")) {
                            MethodInsnNode newOne =
                                    new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(ProgressDisplayer.class),
                                            "minecraftDisplayFirstProgress", "()V", false);
                            m.instructions.insertBefore(method, newOne);
                            hasFoundStartGame = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!found) {
            System.out.println("Did not find " + tryingToFind + "! Could it have been any of these?");
            for (MethodNode m : classNode.methods) {
                System.out.println("  -" + m.name);
            }
        }

        if (!hasFoundStartGame) {
            System.out.println("Did not find " + minecraftStartGame + "! Could it have been any of these?");
            for (MethodNode m : classNode.methods) {
                System.out.println("  -" + m.name);
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        System.out.println("Transformed Minecraft");
        return cw.toByteArray();
    }
}

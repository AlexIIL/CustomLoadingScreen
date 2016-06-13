package alexiil.mc.mod.load.coremod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import net.minecraft.launchwrapper.IClassTransformer;

import net.minecraftforge.fml.client.FMLClientHandler;

import alexiil.mc.mod.load.BLSLog;
import alexiil.mc.mod.load.ProgressDisplayer;

public class BetterLoadingScreenTransformer implements IClassTransformer, Opcodes {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("alexiil.mc.mod.load.BLSLog")) return basicClass;
        if (transformedName.equals("net.minecraft.client.Minecraft")) return transformMinecraft(basicClass, transformedName.equals(name));
        if (name.equals("com.mumfrey.liteloader.client.api.ObjectFactoryClient")) return transformObjectFactoryClient(basicClass);
        if (name.equals("lumien.resourceloader.ResourceLoader")) {
            return transformResourceLoader(basicClass);
        }

        // I COULD submit a pull request with these features, but it just doesn't make sense with it being a non stable
        // API. Also, this is just a hook! I'm sorry! I'll put a big warning in the log file? is that ok?
        if (name.equals("net.minecraftforge.fml.common.ProgressManager")) {
            return transformProgressManager(basicClass);
        }

        // I'm SO SORRY FORGE PEOPLE!
        // Actually I'm not. But I hope you can understand why this is needed.
        if (name.equals("net.minecraftforge.fml.common.ProgressManager$ProgressBar")) {
            return transformProgressBar(basicClass);
        }
        out(basicClass, transformedName);
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
            boolean hasFoundOnce = false;
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
                        } else if (method.owner.startsWith("com/mumfrey")) {
                            BLSLog.info("Started with \"com/mumfrey\", was actually \"" + method.owner + "\"");
                        }
                    }
                    // LiteLoader removing end

                    String progressDisplayer = Type.getInternalName(ProgressDisplayer.class);

                    if (node instanceof MethodInsnNode) {
                        MethodInsnNode method = (MethodInsnNode) node;
                        if (method.owner.equals(Type.getInternalName(FMLClientHandler.class)) && method.name.equals("instance")) {
                            if (!hasFoundOnce) {
                                MethodInsnNode newOne = new MethodInsnNode(Opcodes.INVOKESTATIC, progressDisplayer, "minecraftDisplayFirstProgress",
                                        "()V", false);
                                m.instructions.insertBefore(method, newOne);
                                i++;

                                MethodInsnNode newTwo = new MethodInsnNode(Opcodes.INVOKESTATIC, progressDisplayer, "minecraftDisplayAfterForge",
                                        "()V", false);
                                m.instructions.insert(method.getNext().getNext().getNext().getNext().getNext().getNext(), newTwo);
                                hasFoundStartGame = true;
                                hasFoundOnce = true;
                            } else {
                                // Pause when minecraft inits its render global
                                MethodInsnNode pause = new MethodInsnNode(Opcodes.INVOKESTATIC, progressDisplayer, "pause", "()V", false);
                                m.instructions.insertBefore(getPrevious(method, 39), pause);

                                MethodInsnNode resume = new MethodInsnNode(Opcodes.INVOKESTATIC, progressDisplayer, "resume", "()V", false);
                                m.instructions.insertBefore(getPrevious(method, 38), resume);
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (!found) {
            BLSLog.info("Did not find " + tryingToFind + "! Could it have been any of these?");
            for (MethodNode m : classNode.methods) {
                BLSLog.info("  -" + m.name);
            }
        }

        if (!hasFoundStartGame) {
            System.out.println("Did not find " + minecraftStartGame + "! Could it have been any of these?");
            for (MethodNode m : classNode.methods) {
                BLSLog.info("  -" + m.name);
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        BLSLog.info("Transformed Minecraft");
        byte[] bytes = cw.toByteArray();

        // Export it :)

        File folder = new File("./asm");
        folder.mkdir();
        folder = new File(folder, "BetterLoadingScreen");
        folder.mkdir();
        File fle = new File(folder, "Minecraft.class");
        try {
            FileOutputStream stream = new FileOutputStream(fle);
            stream.write(bytes);
            stream.flush();
            stream.close();
        } catch (IOException io) {
            io.printStackTrace();
        }

        return bytes;
    }

    private byte[] transformResourceLoader(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        for (MethodNode m : classNode.methods) {
            if (m.name.equals("preInit")) {
                m.visibleAnnotations.remove(0);// Remove @Mod.EventHandler
            }
        }

        for (FieldNode f : classNode.fields) {
            if (f.name.equals("INSTANCE")) f.visibleAnnotations.remove(0);// Remove @Mod.Instance("ResourceLoader")
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        byte[] arr = cw.toByteArray();
        BLSLog.info("Transformed ResourceLoader!");
        return arr;
    }

    private byte[] transformProgressManager(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        for (MethodNode m : classNode.methods) {
            if (m.name.equals("push") && m.desc.contains("Z")) {
                // ADD:
                // Load 0
                // Load 1
                // Load 2
                // INVOKE_STATIC alexiil.mods.load.ProgressDisplayer.forgeHook_ProgressManager_Push();
                m.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(ProgressDisplayer.class),
                        "forgeHook_ProgressManager_Push", "(Ljava/lang/String;IZ)V", false));
                m.instructions.insert(new VarInsnNode(Opcodes.ILOAD, 2));
                m.instructions.insert(new VarInsnNode(Opcodes.ILOAD, 1));
                m.instructions.insert(new VarInsnNode(Opcodes.ALOAD, 0));

            }

            if (m.name.equals("pop")) {
                // ADD:
                // INVOKE_STATIC alexiil.mods.load.ProgressDisplayer.forgeHook_ProgressManager_Pop();
                m.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(ProgressDisplayer.class),
                        "forgeHook_ProgressManager_Pop", "()V", false));
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        byte[] arr = cw.toByteArray();
        BLSLog.info("Transformed ProgressManager!");
        return arr;
    }

    private byte[] transformProgressBar(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        for (MethodNode m : classNode.methods) {
            if (m.name.equals("step") && !m.desc.contains("Class")) {
                // ADD
                // LOAD 0
                // INVOKE_STATIC alexiil.mods.load.ProgressDisplayer.forgeHook_ProgressManager_ProgressBar_Step();
                m.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(ProgressDisplayer.class),
                        "forgeHook_ProgressManager_ProgressBar_Step", "(Ljava/lang/String;)V", false));
                m.instructions.insert(new VarInsnNode(Opcodes.ALOAD, 1));
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        byte[] arr = cw.toByteArray();
        BLSLog.info("Transformed ProgressBar!");
        return arr;
    }

    private void out(byte[] data, String clazzName) {

    }

    private AbstractInsnNode getPrevious(AbstractInsnNode node, int num) {
        while (num > 0) {
            node = node.getPrevious();
            num--;
        }
        return node;
    }
}

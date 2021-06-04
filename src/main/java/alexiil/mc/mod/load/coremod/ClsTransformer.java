package alexiil.mc.mod.load.coremod;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.ResourceLocation;

public class ClsTransformer implements IClassTransformer, Opcodes {

    public static final Logger LOG = LogManager.getLogger("cls.transform");

    private static final String OWNER_MAIN_SPLASH_RENDERER;
    private static final String OWNER_ASM_CALLBACKS;

    private static final String MC_FONT_RENDERER = "net.minecraft.client.gui.FontRenderer";

    private static String fontRendererUnderlineName = "ClsWentWrong_Underline";
    private static String fontRendererStrikethroughName = "ClsWentWrong_Strikethrough";

    static {
        OWNER_MAIN_SPLASH_RENDERER = "alexiil/mc/mod/load/render/MainSplashRenderer";
        OWNER_ASM_CALLBACKS = "alexiil/mc/mod/load/coremod/Callbacks";
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (name.equals("net.minecraftforge.fml.client.SplashProgress")) {
            return transformSplashProgress(basicClass);
        }
        if (name.equals("net.minecraftforge.fml.client.SplashProgress$Texture")) {
            return transformSplashProgress_Texture(basicClass);
        }
        if (name.startsWith("net.minecraftforge.fml.client.SplashProgress$")) {
            return transformSplashProgress_3(basicClass);
        }
        if (name.equals("alexiil.mc.mod.load.render.MainSplashRenderer")) {
            return transformMainSplashRenderer(basicClass);
        }
        if (name.equals("alexiil.mc.mod.load.render.FontRendererSeparate")) {
            return transformFontRendererSeparate(basicClass);
        }
        if (transformedName.equals(MC_FONT_RENDERER)) {
            return transformFontRenderer(basicClass);
        }
        return basicClass;
    }

    private static byte[] transformSplashProgress(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        for (FieldNode f : classNode.fields) {
            if (f.name.equals("logoTexture")) {
                f.access = ACC_PUBLIC | ACC_STATIC;
            }
        }

        for (MethodNode m : classNode.methods) {
            if (m.name.equals("finish")) {
                InsnList list = new InsnList();

                String owner = OWNER_MAIN_SPLASH_RENDERER;
                String name = "finish";
                String desc = "()V";
                list.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc, false));

                AbstractInsnNode from = null;

                ListIterator<AbstractInsnNode> iter = m.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode node = iter.next();
                    if (node instanceof MethodInsnNode) {
                        MethodInsnNode method = (MethodInsnNode) node;
                        if (method.name.equals("checkThreadState")) {
                            from = node;
                            break;
                        }
                    }
                }

                m.instructions.insert(from, list);

            } else if (m.name.equals("pause")) {
                InsnList list = new InsnList();

                list.add(new LdcInsnNode(Integer.valueOf(1)));

                String owner = OWNER_MAIN_SPLASH_RENDERER;
                String name = "pause";
                String desc = "Z";
                list.add(new FieldInsnNode(PUTSTATIC, owner, name, desc));

                m.instructions.insert(list);

            } else if (m.name.equals("resume")) {
                InsnList list = new InsnList();

                list.add(new LdcInsnNode(Integer.valueOf(0)));

                String owner = OWNER_MAIN_SPLASH_RENDERER;
                String name = "pause";
                String desc = "Z";
                list.add(new FieldInsnNode(PUTSTATIC, owner, name, desc));

                m.instructions.insert(list);
            }
        }

        // Ensure our changes are obvious
        changeLineNumbers(classNode);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private static byte[] transformSplashProgress_3(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        String desc_resource_location = Type.getDescriptor(ResourceLocation.class);

        for (MethodNode m : classNode.methods) {
            if (m.name.equals("run")) {

                ListIterator<AbstractInsnNode> iter = m.instructions.iterator();
                boolean found = false;
                while (iter.hasNext()) {
                    AbstractInsnNode node = iter.next();
                    if (found) {
                        iter.remove();
                        continue;
                    }

                    if (node instanceof MethodInsnNode) {
                        MethodInsnNode method = (MethodInsnNode) node;
                        if (method.name.equals("glDisable")) {
                            found = true;
                        }
                    }
                }

                String owner = "net/minecraftforge/fml/client/SplashProgress";
                String name = "logoTexture";
                String desc = "Lnet/minecraftforge/fml/client/SplashProgress$Texture;";

                m.instructions.add(new FieldInsnNode(GETSTATIC, owner, name, desc));

                owner = OWNER_MAIN_SPLASH_RENDERER;
                name = "mojangLogoTex";

                m.instructions.add(new FieldInsnNode(PUTSTATIC, owner, name, desc));

                m.instructions.add(new VarInsnNode(ALOAD, 0));

                owner = classNode.name;
                name = "val$fontLoc";
                desc = desc_resource_location;
                m.instructions.add(new FieldInsnNode(GETFIELD, owner, name, desc));

                owner = OWNER_MAIN_SPLASH_RENDERER;
                name = "fontLoc";
                desc = desc_resource_location;
                m.instructions.add(new FieldInsnNode(PUTSTATIC, owner, name, desc));

                owner = OWNER_MAIN_SPLASH_RENDERER;
                name = "run";
                desc = "()V";
                m.instructions.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc, false));

                m.instructions.add(new InsnNode(RETURN));
                m.tryCatchBlocks.clear();
            }
        }

        // Ensure our changes are obvious
        changeLineNumbers(classNode);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private static byte[] transformSplashProgress_Texture(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        classNode.access = Opcodes.ACC_PUBLIC;

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private static byte[] transformMainSplashRenderer(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        String nameTexture = "net/minecraftforge/fml/client/SplashProgress$Texture";
        String descTexture = "L" + nameTexture + ";";

        for (FieldNode field : classNode.fields) {
            if (field.desc.contains("DummyTexture")) {
                field.desc = descTexture;
            }
        }

        for (MethodNode method : classNode.methods) {
            ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode node = iter.next();
                if (node instanceof FieldInsnNode) {
                    FieldInsnNode fld = (FieldInsnNode) node;
                    if (fld.desc.contains("DummyTexture")) {
                        fld.desc = descTexture;
                    }
                } else if (node instanceof MethodInsnNode) {
                    MethodInsnNode m = (MethodInsnNode) node;
                    if (m.owner.contains("DummyTexture")) {
                        m.owner = nameTexture;
                    }
                }
            }
        }

        // Ensure our changes are obvious
        changeLineNumbers(classNode);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private static byte[] transformFontRendererSeparate(byte[] before) {
        LOG.info("Transforming CLS.FontRendererSeparate");
        if (fontRendererStrikethroughName.contains("ClsWentWrong")) {
            LOG.warn("Transformed before minecraft's FontRenderer!");
            LOG.warn(" (force-loading FontRenderer)");
            try {
                Class<?> cls = Class.forName(MC_FONT_RENDERER);
                if (cls == null) {
                    throw new IllegalStateException(MC_FONT_RENDERER);
                } else {
                    LOG.info("Loaded " + cls);
                }
            } catch (ClassNotFoundException e) {
                throw new Error(e);
            }
        }

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        for (MethodNode m : classNode.methods) {
            ListIterator<AbstractInsnNode> iter = m.instructions.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode i = iter.next();
                if (i instanceof FieldInsnNode) {
                    FieldInsnNode fn = (FieldInsnNode) i;
                    if ("__cls__replaced__underline".equals(fn.name)) {
                        fn.name = fontRendererUnderlineName;
                    } else if ("__cls__replaced__strikethrough".equals(fn.name)) {
                        fn.name = fontRendererStrikethroughName;
                    }
                }
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private static byte[] transformFontRenderer(byte[] before) {
        LOG.info("Transforming Minecraft.FontRenderer");
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        List<FieldNode> fields = classNode.fields;
        List<FieldNode> boolFields = new ArrayList<>();
        for (FieldNode n : fields) {
            if ("Z".equals(n.desc) && (n.access & Opcodes.ACC_STATIC) == 0) {
                boolFields.add(n);
            }
        }

        FieldNode fldUnderline = boolFields.get(5);
        FieldNode fldStrike = boolFields.get(6);

        fontRendererUnderlineName = fldUnderline.name;
        fontRendererStrikethroughName = fldStrike.name;

        if ((fldUnderline.access & ACC_PUBLIC) == 0) {
            fldUnderline.access &= ~ACC_PRIVATE;
            fldUnderline.access |= ACC_PROTECTED;
        }

        if ((fldStrike.access & ACC_PUBLIC) == 0) {
            fldStrike.access &= ~ACC_PRIVATE;
            fldStrike.access |= ACC_PROTECTED;
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private static void changeLineNumbers(ClassNode node) {
        // for (MethodNode m : node.methods) {
        // changeLineNumbers(m);
        // }
    }

    private static void changeLineNumbers(MethodNode m) {
        ListIterator<AbstractInsnNode> iter = m.instructions.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode insn = iter.next();
            if (insn instanceof LineNumberNode) {
                LineNumberNode ln = (LineNumberNode) insn;
                ln.line += 10_000;
            }
        }
    }

    protected static void showMethod(MethodNode m, int from) {
        LOG.info("Showing Method...");
        for (int i = from; i < m.instructions.size(); i++) {
            LOG.info("  -" + getInsn(m.instructions.get(i)));
        }
    }

    protected static MethodNode getMethod(ClassNode node, String name) {
        for (MethodNode m : node.methods)
            if (m.name.equals(name)) return m;
        return null;
    }

    protected static String getInsn(AbstractInsnNode ins) {
        if (ins instanceof MethodInsnNode) {
            MethodInsnNode n = (MethodInsnNode) ins;
            return (n.owner + "," + n.name + "," + n.desc);
        }
        if (ins instanceof FieldInsnNode) {
            FieldInsnNode n = (FieldInsnNode) ins;
            return (n.owner + "," + n.name + "," + n.desc);
        }
        if (ins instanceof VarInsnNode) {
            VarInsnNode n = (VarInsnNode) ins;
            return n.getOpcode() + " " + n.var;
        }
        return (ins.getOpcode() + ":" + ins.getClass().getSimpleName());
    }
}

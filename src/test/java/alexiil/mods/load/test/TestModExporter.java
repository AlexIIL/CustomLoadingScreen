package alexiil.mods.load.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import com.google.common.base.Throwables;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class TestModExporter implements Opcodes {
    public static void dumpMods() {
        File folder = new File("./mods/test/");
        folder.mkdirs();
        for (int i = 0; i < 0x500; i++) {
            String name = StringUtils.leftPad(Integer.toHexString(i), 3, "0");
            name = name.toUpperCase();
            System.out.println(name);
            byte[] classBytes = dump(name);
            File fle = new File(folder, "BasicTestMod_" + name + ".jar");
            try {
                FileOutputStream fos = new FileOutputStream(fle);
                ZipOutputStream zos = new ZipOutputStream(fos);

                // Class
                ZipEntry ze = new ZipEntry("alexiil/mods/test/BasicTestMod_" + name + ".class");
                zos.putNextEntry(ze);
                zos.write(classBytes);
                zos.closeEntry();

                zos.finish();

                // Random Texture
                ze = new ZipEntry("assets/test/textures/blocks/Block_" + name + ".png");
                zos.putNextEntry(ze);
                zos.write(texture(name));
                zos.closeEntry();

                zos.finish();

                zos.flush();
                zos.close();

                fos.flush();
                fos.close();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }

    private static byte[] texture(String num) {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        genRandomGraphics(g2d, num);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException shouldnthappen) {
            throw Throwables.propagate(shouldnthappen);
        }
        return out.toByteArray();
    }

    private static void genRandomGraphics(Graphics2D g2d, String num) {
        g2d.setColor(Color.WHITE);
        g2d.drawRect(0, 0, 64, 64);

        g2d.setColor(new Color(0xAFAFAF));
        for (int i = 0; i < 1024; i++) {
            int x = (int) (Math.random() * 64);
            int y = (int) (Math.random() * 64);
            g2d.fillRect(x, y, 1, 1);
        }

        g2d.setColor(Color.BLACK);
        g2d.setFont(g2d.getFont().deriveFont(32));
        g2d.drawString(num, 0, 0);
    }

    public static byte[] dump(String num) {
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        AnnotationVisitor av0;
        MethodNode mn;

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER + ACC_SYNTHETIC, "alexiil/mods/test/BasicTestMod_" + num, null, Type.getInternalName(Object.class),
                null);

        cw.visitSource("SYNTHETIC[TestModExporter]", null);

        {
            av0 = cw.visitAnnotation("Lnet/minecraftforge/fml/common/Mod;", true);
            av0.visit("modid", "empty_test_mod_" + num);
            av0.visit("name", "Empty Test Mod " + num);
            av0.visit("version", "0.1");
            av0.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            String preInit = Type.getDescriptor(FMLPreInitializationEvent.class);
            mn = new MethodNode(ACC_PUBLIC, "preInit", "(" + preInit + ")V", null, null);

            mn.visitAnnotation(Type.getDescriptor(EventHandler.class), true);

            InsnList list = mn.instructions;
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(new InsnNode(DUP));
            list.add(new VarInsnNode(ALOAD, 1));
            list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(TestModHelper.class), "preInit", "(Ljava.lang.Object;" + preInit + ")V",
                    false));
            // list.add(new MethodInsnNode(INVOKESTATIC));
            mn.accept(cw);
        }
        {
            String postInit = Type.getDescriptor(FMLPostInitializationEvent.class);
            mn = new MethodNode(ACC_PUBLIC, "postInit", "(" + postInit + ")V", null, null);

            mn.visitAnnotation(Type.getDescriptor(EventHandler.class), true);

            InsnList list = mn.instructions;
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(new VarInsnNode(ALOAD, 1));
            list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(TestModHelper.class), "postInit", "(Ljava.lang.Object;" + postInit + ")V",
                    false));
            mn.accept(cw);
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}

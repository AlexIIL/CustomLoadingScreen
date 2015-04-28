package alexiil.mods.load.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TestModExporter implements Opcodes {
    public static void dumpMods() {
        File folder = new File("./mods/test/");
        folder.mkdirs();
        for (int i = 0; i < 0x500; i++) {
            String name = StringUtils.leftPad(Integer.toHexString(i), 3, "0");
            name = name.toUpperCase();
            System.out.println(name);
            byte[] bytes = dump(name);
            File fle = new File(folder, "BasicTestMod_" + name + ".class");
            try {
                FileOutputStream fos = new FileOutputStream(fle);
                fos.write(bytes);
                fos.flush();
                fos.close();
            }
            catch (IOException io) {
                io.printStackTrace();
            }
        }
    }

    public static byte[] dump(String num) {
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "BasicTestMod_" + num, null, "java/lang/Object", null);

        cw.visitSource("BasicTestMod_" + num + ".java", null);

        {
            av0 = cw.visitAnnotation("Lnet/minecraftforge/fml/common/Mod;", true);
            av0.visit("modid", "emptyTestMod_" + num);
            av0.visit("name", "Empty Test Mod number " + num);
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
        cw.visitEnd();

        return cw.toByteArray();
    }
}

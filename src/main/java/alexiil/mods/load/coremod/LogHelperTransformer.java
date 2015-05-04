package alexiil.mods.load.coremod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import alexiil.mods.load.BLSLog;

public class LogHelperTransformer implements Opcodes {
    private static final Multimap<String, LoggingPositions> annotationTypeMap = ArrayListMultimap.create();
    private static final List<String> whitelist = Lists.newArrayList();
    private static final Map<Integer, Integer> ifBytecodes = Maps.newHashMap();
    private static final Map<Integer, Boolean> ifByteType = Maps.newHashMap();

    static {
        annotationTypeMap.put(Type.getDescriptor(DebugLog.class), LoggingPositions.INVOKE);
        annotationTypeMap.put(Type.getDescriptor(DebugLog.class), LoggingPositions.LOGIC_SPLIT);
        annotationTypeMap.put(Type.getDescriptor(DebugLog.class), LoggingPositions.RETURN);

        annotationTypeMap.put(Type.getDescriptor(DebugLog.Invoke.class), LoggingPositions.INVOKE);
        annotationTypeMap.put(Type.getDescriptor(DebugLog.LogicSplit.class), LoggingPositions.LOGIC_SPLIT);
        annotationTypeMap.put(Type.getDescriptor(DebugLog.Return.class), LoggingPositions.RETURN);

        annotationTypeMap.put(Type.getDescriptor(DebugLog.IgnoreClass.class), LoggingPositions.IGNORE_PARENT);

        addToWhitelist("alexiil");

        addType(IF_ACMPEQ, 2, true);
        addType(IF_ACMPNE, 2, true);
        addType(IF_ICMPEQ, 2, false);
        addType(IF_ICMPGE, 2, false);
        addType(IF_ICMPGT, 2, false);
        addType(IF_ICMPLE, 2, false);
        addType(IF_ICMPLT, 2, false);
        addType(IF_ICMPNE, 2, false);

        addType(IFEQ, 1, false);
        addType(IFGE, 1, false);
        addType(IFGT, 1, false);
        addType(IFLE, 1, false);
        addType(IFLT, 1, false);
        addType(IFNE, 1, false);

        addType(IFNONNULL, 1, true);
        addType(IFNULL, 1, true);
    }

    public static void addToWhitelist(String packageBase) {
        whitelist.add(packageBase);
    }

    public static void addType(int opcode, int stackSize, boolean isObject) {
        ifBytecodes.put(opcode, stackSize);
        ifByteType.put(opcode, isObject);
    }

    public static boolean isIfObject(int opcode) {
        return ifByteType.get(opcode);
    }

    public static byte[] transform(String name, byte[] data) {
        if (name == null)
            return data;
        boolean can = false;
        for (String white : whitelist) {
            if (name.startsWith(white))
                can = true;
        }
        if (!can)
            return data;

        try {
            ClassNode classNode = new ClassNode();
            ClassReader reader = new ClassReader(data);
            reader.accept(classNode, 0);

            List<LoggingPositions> shouldDebug = shouldDebug(classNode.visibleAnnotations);

            boolean hasChanged = false;

            for (MethodNode node : classNode.methods) {
                String blsLogName = Type.getInternalName(BLSLog.class);

                List<LoggingPositions> actual = shouldDebug(node.visibleAnnotations);
                actual = combine(shouldDebug, actual);

                if (actual.contains(LoggingPositions.INVOKE)) {
                    insertInvoke(node);
                    hasChanged = true;
                }
                else if (!actual.isEmpty()) {
                    node.instructions.insert(new MethodInsnNode(INVOKESTATIC, blsLogName, "justInvoke", "()V", false));
                    hasChanged = true;
                }

                if (actual.contains(LoggingPositions.RETURN)) {
                    for (AbstractInsnNode ret : getReturnNodes(node)) {
                        insertReturn(node, ret);
                    }
                    hasChanged = true;
                }
                else if (!actual.isEmpty()) {
                    for (AbstractInsnNode ret : getReturnNodes(node)) {
                        MethodInsnNode justReturn = new MethodInsnNode(INVOKESTATIC, blsLogName, "justReturn", "()V", false);
                        node.instructions.insertBefore(ret, justReturn);
                    }
                    hasChanged = true;
                }

                if (actual.contains(LoggingPositions.LOGIC_SPLIT)) {
                    for (AbstractInsnNode ifNode : getIfNodes(node)) {
                        insertIf(node, ifNode);
                    }
                    hasChanged = true;
                }
            }

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(cw);
            byte[] bytes = cw.toByteArray();

            if (hasChanged) {
                File folder = new File("./bls-asm");
                folder.mkdir();
                folder = new File(folder, "/logging");
                folder.mkdir();
                File fle = new File(folder, name + ".class");
                try {
                    FileOutputStream stream = new FileOutputStream(fle);
                    stream.write(bytes);
                    stream.flush();
                    stream.close();
                }
                catch (IOException io) {
                    io.printStackTrace();
                }
            }

            return bytes;
        }
        catch (Throwable t) {
            BLSLog.warn("An exception occoured while transforming " + name + "!", t);
            return data;
        }
    }

    private static List<LoggingPositions> shouldDebug(List<AnnotationNode> annotations) {
        List<LoggingPositions> list = Lists.newArrayList();
        if (annotations != null)
            for (AnnotationNode annotation : annotations) {
                for (Entry<String, Collection<LoggingPositions>> entry : annotationTypeMap.asMap().entrySet()) {
                    if (entry.getKey().equals(annotation.desc)) {
                        list.addAll(entry.getValue());
                    }
                }
            }
        return list;
    }

    private static List<LoggingPositions> combine(List<LoggingPositions> clazz, List<LoggingPositions> method) {
        if (method.contains(LoggingPositions.IGNORE_PARENT))
            return method;
        for (LoggingPositions entry : clazz) {
            if (!method.contains(entry)) {
                method.add(entry);
            }
        }
        return method;
    }

    private static void insertInvoke(MethodNode node) {
        Type[] args = Type.getArgumentTypes(node.desc);
        int length = (Type.getArgumentsAndReturnSizes(node.desc) >> 2) - 1;
        InsnList list = new InsnList();
        list.add(new LdcInsnNode(length));
        list.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
        // Use 'j' to track the array index (double_2nd counts as 2 long in the locals stack, but only one in our 'args'
        // array, so we must negate it separately from 'i'
        int arrayIndex = args.length - 1;
        for (int localsIndex = length - 1; localsIndex >= 0;) {
            list.add(new InsnNode(DUP));// push Array reference (duplicate the array thats on there)
            list.add(new LdcInsnNode(localsIndex));// push Index
            localsIndex -= insertCastToObject(list, localsIndex, args[arrayIndex]);// push Value
            list.add(new InsnNode(AASTORE));// pop 3
            arrayIndex--;
        }
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(BLSLog.class), "logInvoke", "([Ljava/lang/Object;)V", false));
        node.instructions.insert(list);
    }

    private static List<AbstractInsnNode> getReturnNodes(MethodNode node) {
        List<AbstractInsnNode> nodes = Lists.newArrayList();
        for (int index = 0; index < node.instructions.size(); index++) {
            AbstractInsnNode potential = node.instructions.get(index);
            if (potential.getOpcode() >= IRETURN && potential.getOpcode() <= RETURN) {
                nodes.add(potential);
            }
        }
        return nodes;
    }

    private static void insertReturn(MethodNode meth, AbstractInsnNode node) {
        Type ret = Type.getReturnType(meth.desc);
        InsnList list = new InsnList();
        String text = "()V";
        if (node.getOpcode() != RETURN) {
            list.add(new InsnNode(DUP));
            insertCastToObject(list, -1, ret);
            text = "(Ljava/lang/Object;)V";
        }
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(BLSLog.class), "logReturn", text, false));
        meth.instructions.insertBefore(node, list);
    }

    private static List<AbstractInsnNode> getIfNodes(MethodNode node) {
        List<AbstractInsnNode> nodes = Lists.newArrayList();
        for (int index = 0; index < node.instructions.size(); index++) {
            AbstractInsnNode potential = node.instructions.get(index);
            if (ifBytecodes.containsKey(potential.getOpcode())) {
                nodes.add(potential);
            }
        }
        return nodes;
    }

    private static void insertIf(MethodNode meth, AbstractInsnNode node) {
        InsnList list = new InsnList();
        int opcode = node.getOpcode();
        int arguments = ifBytecodes.get(opcode);
        boolean isObject = ifByteType.get(opcode);
        if (arguments == 1) {
            list.add(new InsnNode(DUP));
            if (isObject)
                list.add(new InsnNode(ACONST_NULL));
            else {
                list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Integer.class), "valueOf", "(I)Ljava/lang/Integer;", false));
                list.add(new LdcInsnNode(0));
                list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Integer.class), "valueOf", "(I)Ljava/lang/Integer;", false));
            }
        }
        else if (isObject) {
            list.add(new InsnNode(DUP2));
        }
        else {
            list.add(new InsnNode(DUP2));
            list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Integer.class), "valueOf", "(I)Ljava/lang/Integer;", false));
            list.add(new InsnNode(SWAP));
            list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Integer.class), "valueOf", "(I)Ljava/lang/Integer;", false));
            list.add(new InsnNode(SWAP));
        }
        list.add(new LdcInsnNode(opcode));
        String text = "(Ljava/lang/Object;Ljava/lang/Object;I)V";
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(BLSLog.class), "logIf", text, false));
        meth.instructions.insertBefore(node, list);
    }

    private static int insertCastToObject(InsnList list, int index, Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN: {
                if (index >= 0)
                    list.add(new VarInsnNode(ILOAD, index));
                list.add(new InsnNode(I2B));
                list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Boolean.class), "valueOf", "(Z)Ljava/lang/Boolean;", false));
                break;
            }
            case Type.BYTE: {
                if (index >= 0)
                    list.add(new VarInsnNode(ILOAD, index));
                list.add(new InsnNode(I2B));
                list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Byte.class), "valueOf", "(B)Ljava/lang/Byte;", false));
                break;
            }
            case Type.CHAR: {
                if (index >= 0)
                    list.add(new VarInsnNode(ILOAD, index));
                list.add(new InsnNode(I2C));
                list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Character.class), "valueOf", "(C)Ljava/lang/Character;", false));
                break;
            }
            case Type.DOUBLE: {
                if (index >= 0)
                    list.add(new VarInsnNode(DLOAD, index - 1));
                list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Double.class), "valueOf", "(D)Ljava/lang/Double;", false));
                return 2;
            }
            case Type.FLOAT: {
                if (index >= 0)
                    list.add(new VarInsnNode(FLOAD, index));
                list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Float.class), "valueOf", "(F)Ljava/lang/Float;", false));
                break;
            }
            case Type.INT: {
                if (index >= 0)
                    list.add(new VarInsnNode(ILOAD, index));
                list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Integer.class), "valueOf", "(I)Ljava/lang/Integer;", false));
                break;
            }
            case Type.LONG: {
                if (index >= 0)
                    list.add(new VarInsnNode(LLOAD, index - 1));
                list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Long.class), "valueOf", "(J)Ljava/lang/Long;", false));
                return 2;
            }
            case Type.SHORT: {
                if (index >= 0)
                    list.add(new VarInsnNode(ILOAD, index));
                list.add(new InsnNode(I2S));
                list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Short.class), "valueOf", "(S)Ljava/lang/Short;", false));
                break;
            }
            default: {
                if (index >= 0)
                    list.add(new VarInsnNode(ALOAD, index));
            }
        }
        return 1;
    }
}

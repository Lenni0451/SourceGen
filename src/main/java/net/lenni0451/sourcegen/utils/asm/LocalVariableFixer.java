package net.lenni0451.sourcegen.utils.asm;

import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.classtransform.utils.Types;
import net.lenni0451.sourcegen.utils.JarUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;

public class LocalVariableFixer {

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "continue",
            "const", "default", "do", "double", "else",
            "enum", "exports", "extends", "final", "finally",
            "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long",
            "module", "native", "new", "package", "private",
            "protected", "public", "requires", "return", "short",
            "static", "strictfp", "super", "switch", "synchronized",
            "this", "throw", "throws", "transient", "try",
            "var", "void", "volatile", "while"
    ));

    public static void run(final File input, final File output) throws Exception {
        Map<String, byte[]> entries = JarUtils.read(input);
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            if (entry.getKey().toLowerCase(Locale.ROOT).endsWith(".class")) {
                ClassNode node = ASMUtils.fromBytes(entry.getValue());
                for (MethodNode method : node.methods) {
                    fixParameters(method);
                    fixLocalVariableTable(method);
                    Set<String> names = new HashSet<>();
                    if (method.localVariables != null) {
                        int[] parameterIndices = ASMUtils.getParameterIndices(method);
                        for (int i = 0; i < method.localVariables.size(); i++) {
                            LocalVariableNode localVariable = method.localVariables.get(i);
                            if (!Modifier.isStatic(method.access) && i == 0) {
                                localVariable.name = "this";
                            } else {
                                localVariable.name = generateNewName(Type.getType(localVariable.desc), names);
                                for (int j = 0; j < parameterIndices.length; j++) {
                                    if (parameterIndices[j] == localVariable.index) {
                                        method.parameters.get(j).name = localVariable.name;
                                    }
                                }
                            }
                        }
                    } else {
                        Type[] parameterTypes = Types.argumentTypes(method);
                        for (int i = 0; i < method.parameters.size(); i++) {
                            ParameterNode parameter = method.parameters.get(i);
                            parameter.name = generateNewName(parameterTypes[i], names);
                        }
                    }
                }
                fixRecordComponents(node);
                entry.setValue(ASMUtils.toStacklessBytes(node));
            }
        }
        JarUtils.write(output, entries);
    }

    private static void fixParameters(final MethodNode methodNode) {
        Type[] parameterTypes = Types.argumentTypes(methodNode);
        String[] names = new String[parameterTypes.length];
        int[] accesses = new int[parameterTypes.length];
        if (methodNode.parameters != null && methodNode.parameters.size() == parameterTypes.length) {
            for (int i = 0; i < parameterTypes.length; i++) {
                ParameterNode parameter = methodNode.parameters.get(i);
                names[i] = parameter.name;
                accesses[i] = parameter.access;
            }
        }
        methodNode.parameters = new ArrayList<>();
        for (int i = 0; i < parameterTypes.length; i++) {
            methodNode.parameters.add(new ParameterNode(names[i], accesses[i]));
        }
    }

    private static void fixLocalVariableTable(final MethodNode methodNode) {
        if (Modifier.isAbstract(methodNode.access) || Modifier.isNative(methodNode.access)) {
            methodNode.localVariables = null;
            return;
        }

        if (methodNode.localVariables == null) methodNode.localVariables = new ArrayList<>();
        Type[] parameterTypes = Types.argumentTypes(methodNode);
        int[] parameterIndices = ASMUtils.getParameterIndices(methodNode);
        List<Integer> missingVariables = new ArrayList<>();
        if (!Modifier.isStatic(methodNode.access)) {
            boolean found = false;
            for (LocalVariableNode localVariable : methodNode.localVariables) {
                if (localVariable.index == 0) {
                    found = true;
                    break;
                }
            }
            if (!found) missingVariables.add(0);
        }
        for (int parameterIndex : parameterIndices) {
            boolean found = false;
            for (LocalVariableNode localVariable : methodNode.localVariables) {
                if (localVariable.index == parameterIndex) {
                    found = true;
                    break;
                }
            }
            if (!found) missingVariables.add(parameterIndex);
        }

        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();
        methodNode.instructions.insert(start);
        methodNode.instructions.add(end);
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!missingVariables.contains(parameterIndices[i])) continue;
            methodNode.localVariables.add(new LocalVariableNode("arg" + i, parameterTypes[i].getDescriptor(), null, start, end, parameterIndices[i]));
        }
    }

    private static String generateNewName(final Type type, final Set<String> alreadyGeneratedNames) {
        String newName = type.getClassName();
        if (type.getSort() == Type.ARRAY) {
            newName = type.getElementType().getClassName();
            for (int j = 0; j < type.getDimensions(); j++) newName += "Array";
        }
        if (type.getDescriptor().length() == 1) {
            newName = type.getDescriptor().toLowerCase(Locale.ROOT);
        } else {
            newName = newName.substring(newName.lastIndexOf('.') + 1);
            newName = newName.substring(newName.lastIndexOf('$') + 1);
            newName = newName.substring(0, 1).toLowerCase(Locale.ROOT) + newName.substring(1);
        }
        if (KEYWORDS.contains(newName)) newName = "_" + newName;

        int index = 2;
        String name = newName;
        while (!alreadyGeneratedNames.add(name)) name = newName + index++;
        return name;
    }

    private static void fixRecordComponents(final ClassNode node) {
        if ((node.access & Opcodes.ACC_RECORD) == 0) return;
        List<RecordComponentNode> recordComponents = node.recordComponents;
        if (recordComponents == null) return;
        List<FieldNode> fields = node.fields;
        for (int i = 0; i < Math.min(fields.size(), recordComponents.size()); i++) {
            RecordComponentNode recordComponent = recordComponents.get(i);
            FieldNode field = fields.get(i);
            if (recordComponent.descriptor.equals(field.desc)) recordComponent.name = field.name;
            else break;
        }

        String constructorDescriptor = "(";
        for (RecordComponentNode recordComponent : recordComponents) constructorDescriptor += recordComponent.descriptor;
        constructorDescriptor += ")V";
        MethodNode constructor = ASMUtils.getMethod(node, "<init>", constructorDescriptor);
        if (constructor != null && constructor.localVariables != null) {
            for (int i = 0; i < Math.min(recordComponents.size(), constructor.localVariables.size() - 1); i++) {
                RecordComponentNode recordComponent = recordComponents.get(i);
                LocalVariableNode localVariable = constructor.localVariables.get(i + 1);
                if (recordComponent.descriptor.equals(localVariable.desc)) localVariable.name = recordComponent.name;
                else break;
            }
        }
    }

}

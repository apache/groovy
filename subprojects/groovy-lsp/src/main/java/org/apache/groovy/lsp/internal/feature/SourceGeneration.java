/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.lsp.internal.feature;

import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Source-generation edits (getters, constructors, toString, equals/hashCode,
 * {@code @Override}), designed after JDT LS source actions but written for
 * the Groovy AST.
 */
public final class SourceGeneration {

    static final String GENERATE_ACCESSORS = "source.generate.accessors";
    static final String GENERATE_TO_STRING = "source.generate.toString";
    static final String GENERATE_EQUALS = "source.generate.hashCodeEquals";
    static final String GENERATE_CONSTRUCTORS = "source.generate.constructors";
    private static final String METHOD_CLOSE = "\n    }\n";

    private SourceGeneration() {
    }

    static List<FieldNode> instanceFields(final ClassNode classNode) {
        List<FieldNode> fields = new ArrayList<>();
        if (classNode == null) {
            return fields;
        }
        for (FieldNode field : classNode.getFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())
                    || field.getLineNumber() <= 0 || "metaClass".equals(field.getName())) {
                continue;
            }
            fields.add(field);
        }
        for (PropertyNode property : classNode.getProperties()) {
            if (property.isSynthetic() || Modifier.isStatic(property.getModifiers())
                    || property.getField() == null || "metaClass".equals(property.getName())) {
                continue;
            }
            FieldNode field = property.getField();
            boolean known = fields.stream().anyMatch(existing -> existing.getName().equals(field.getName()));
            if (!known) {
                fields.add(field);
            }
        }
        return fields;
    }

    static List<FieldNode> privateFieldsWithoutAccessors(final ClassNode classNode) {
        List<FieldNode> fields = new ArrayList<>();
        for (FieldNode field : instanceFields(classNode)) {
            if (needsGeneratedAccessor(classNode, field)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private static boolean needsGeneratedAccessor(final ClassNode classNode, final FieldNode field) {
        return Modifier.isPrivate(field.getModifiers())
                && classNode.getProperty(field.getName()) == null
                && !hasAccessor(classNode, field);
    }

    static boolean hasAccessor(final ClassNode classNode, final FieldNode field) {
        String cap = capitalize(field.getName());
        return classNode.getDeclaredMethod("get" + cap, Parameter.EMPTY_ARRAY) != null
                || classNode.getDeclaredMethod("set" + cap, new Parameter[]{new Parameter(field.getType(), field.getName())}) != null
                || classNode.getGetterMethod("get" + cap) != null;
    }

    static boolean hasDeclared(final ClassNode classNode, final String name, final Parameter[] parameters) {
        MethodNode method = classNode.getDeclaredMethod(name, parameters);
        return method != null && !method.isSynthetic();
    }

    static List<TextEdit> accessors(final ClassNode classNode, final CompiledDocument compiled,
                                    final PositionEncoding encoding) {
        List<FieldNode> fields = privateFieldsWithoutAccessors(classNode);
        if (fields.isEmpty()) {
            return List.of();
        }
        StringBuilder stub = new StringBuilder();
        for (FieldNode field : fields) {
            String cap = capitalize(field.getName());
            String type = typeName(field);
            stub.append("    ").append(type).append(" get").append(cap).append("() {\n        ")
                    .append(field.getName()).append(METHOD_CLOSE)
                    .append("    void set").append(cap).append('(').append(type).append(' ').append(field.getName())
                    .append(") {\n        this.").append(field.getName()).append(" = ").append(field.getName())
                    .append(METHOD_CLOSE);
        }
        return insert(classNode, compiled, encoding, stub.toString());
    }

    static List<TextEdit> toStringMethod(final ClassNode classNode, final CompiledDocument compiled,
                                         final PositionEncoding encoding) {
        if (hasDeclared(classNode, "toString", Parameter.EMPTY_ARRAY)) {
            return List.of();
        }
        StringBuilder body = new StringBuilder("    @Override\n    String toString() {\n        '")
                .append(classNode.getNameWithoutPackage()).append("(");
        List<FieldNode> fields = instanceFields(classNode);
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                body.append(", ");
            }
            body.append(fields.get(i).getName()).append(": ${").append(fields.get(i).getName()).append('}');
        }
        body.append(")'\n    }\n");
        return insert(classNode, compiled, encoding, body.toString());
    }

    static List<TextEdit> equalsAndHashCode(final ClassNode classNode, final CompiledDocument compiled,
                                            final PositionEncoding encoding) {
        Parameter[] equalsParams = {new Parameter(ClassHelper.OBJECT_TYPE, "o")};
        if (hasDeclared(classNode, "equals", equalsParams) || hasDeclared(classNode, "hashCode", Parameter.EMPTY_ARRAY)) {
            return List.of();
        }
        String simple = classNode.getNameWithoutPackage();
        StringBuilder body = new StringBuilder();
        body.append("    @Override\n    boolean equals(Object o) {\n        if (this.is(o)) {\n            return true\n        }\n")
                .append("        if (!(o instanceof ").append(simple).append(")) {\n            return false\n        }\n")
                .append("        ").append(simple).append(" other = (").append(simple).append(") o\n        ");
        List<FieldNode> fields = instanceFields(classNode);
        if (fields.isEmpty()) {
            body.append("true").append(METHOD_CLOSE);
        } else {
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) {
                    body.append(" && ");
                }
                String name = fields.get(i).getName();
                body.append(name).append(" == other.").append(name);
            }
            body.append(METHOD_CLOSE);
        }
        body.append("    @Override\n    int hashCode() {\n        Objects.hash(");
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                body.append(", ");
            }
            body.append(fields.get(i).getName());
        }
        body.append(")\n    }\n");
        return insert(classNode, compiled, encoding, body.toString());
    }

    static List<TextEdit> constructor(final ClassNode classNode, final CompiledDocument compiled,
                                      final PositionEncoding encoding) {
        for (ConstructorNode constructor : classNode.getDeclaredConstructors()) {
            if (!constructor.isSynthetic()) {
                return List.of();
            }
        }
        List<FieldNode> fields = instanceFields(classNode);
        if (fields.isEmpty()) {
            return List.of();
        }
        StringBuilder body = new StringBuilder("    ").append(classNode.getNameWithoutPackage()).append('(');
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                body.append(", ");
            }
            body.append(typeName(fields.get(i))).append(' ').append(fields.get(i).getName());
        }
        body.append(") {\n");
        for (FieldNode field : fields) {
            body.append("        this.").append(field.getName()).append(" = ").append(field.getName()).append('\n');
        }
        body.append("    }\n");
        return insert(classNode, compiled, encoding, body.toString());
    }

    static List<TextEdit> overrideAnnotations(final ClassNode classNode, final CompiledDocument compiled,
                                              final PositionEncoding encoding) {
        List<TextEdit> edits = new ArrayList<>();
        if (classNode == null) {
            return edits;
        }
        ClassNode superClass = classNode.getSuperClass();
        for (MethodNode method : classNode.getMethods()) {
            TextEdit edit = overrideEdit(method, superClass, classNode, compiled, encoding);
            if (edit != null) {
                edits.add(edit);
            }
        }
        return edits;
    }

    static Position beforeClose(final ClassNode classNode, final CompiledDocument compiled,
                                final PositionEncoding encoding) {
        Range classRange = Positions.toRange(classNode, compiled.getText(), encoding);
        if (classRange == null) {
            return null;
        }
        Position insert = classRange.getEnd();
        String line = Positions.lineText(compiled.getText(), insert.getLine() + 1);
        int brace = line.lastIndexOf('}');
        if (brace >= 0) {
            return Positions.toLsp(insert.getLine() + 1, brace + 1, line, encoding);
        }
        return insert;
    }

    static String capitalize(final String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        int first = name.codePointAt(0);
        return new String(Character.toChars(Character.toUpperCase(first))) + name.substring(Character.charCount(first));
    }

    private static TextEdit overrideEdit(final MethodNode method, final ClassNode superClass,
                                         final ClassNode classNode, final CompiledDocument compiled,
                                         final PositionEncoding encoding) {
        if (method.isSynthetic() || method.getLineNumber() <= 0 || hasOverride(method)) {
            return null;
        }
        if (!overrides(method, superClass) && !overridesInterface(method, classNode)) {
            return null;
        }
        Range name = Positions.toNameRange(method, compiled.getText(), encoding);
        if (name == null) {
            return null;
        }
        Position start = new Position(name.getStart().getLine(), 0);
        String line = Positions.lineText(compiled.getText(), name.getStart().getLine() + 1);
        int indent = TextDocument.leadingWhitespace(line);
        return new TextEdit(new Range(start, start), line.substring(0, indent) + "@Override\n");
    }

    private static List<TextEdit> insert(final ClassNode classNode, final CompiledDocument compiled,
                                         final PositionEncoding encoding, final String stub) {
        Position insert = beforeClose(classNode, compiled, encoding);
        if (insert == null || stub.isEmpty()) {
            return List.of();
        }
        return List.of(new TextEdit(new Range(insert, insert), stub));
    }

    private static String typeName(final FieldNode field) {
        return field.getType() == null ? "def" : field.getType().getNameWithoutPackage();
    }

    private static boolean hasOverride(final MethodNode method) {
        for (AnnotationNode annotation : method.getAnnotations()) {
            if (annotation.getClassNode() != null
                    && "Override".equals(annotation.getClassNode().getNameWithoutPackage())) {
                return true;
            }
        }
        return false;
    }

    private static boolean overrides(final MethodNode method, final ClassNode superClass) {
        if (superClass == null || "java.lang.Object".equals(superClass.getName())) {
            return false;
        }
        MethodNode inherited = superClass.getMethod(method.getName(), method.getParameters());
        return inherited != null && !inherited.isPrivate();
    }

    private static boolean overridesInterface(final MethodNode method, final ClassNode classNode) {
        if (classNode.getInterfaces() == null) {
            return false;
        }
        for (ClassNode iface : classNode.getInterfaces()) {
            if (iface.getMethod(method.getName(), method.getParameters()) != null) {
                return true;
            }
        }
        return false;
    }

}

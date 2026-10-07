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
 * Source actions that insert {@code groovy.transform} annotations, drop
 * {@code private} so a field becomes a property, or add {@code @Override}.
 * Method bodies are not pasted: {@code @ToString}, {@code @EqualsAndHashCode}
 * and {@code @TupleConstructor} already generate them.
 */
public final class SourceGeneration {

    static final String GENERATE_ACCESSORS = "source.generate.accessors";
    static final String GENERATE_TO_STRING = "source.generate.toString";
    static final String GENERATE_EQUALS = "source.generate.hashCodeEquals";
    static final String GENERATE_CONSTRUCTORS = "source.generate.constructors";
    private static final String CANONICAL = "Canonical";

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
        return instanceFields(classNode).stream()
                .filter(field -> needsGeneratedAccessor(classNode, field))
                .toList();
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

    /**
     * Drops {@code private} on instance fields that have no accessor, so
     * Groovy treats them as properties and generates the getter and setter.
     */
    static List<TextEdit> accessors(final ClassNode classNode, final CompiledDocument compiled,
                                    final PositionEncoding encoding) {
        List<TextEdit> edits = new ArrayList<>();
        for (FieldNode field : privateFieldsWithoutAccessors(classNode)) {
            TextEdit edit = dropPrivate(field, compiled, encoding);
            if (edit != null) {
                edits.add(edit);
            }
        }
        return edits;
    }

    static List<TextEdit> toStringMethod(final ClassNode classNode, final CompiledDocument compiled,
                                         final PositionEncoding encoding) {
        if (hasAst(classNode, "ToString") || hasAst(classNode, CANONICAL)
                || hasDeclared(classNode, "toString", Parameter.EMPTY_ARRAY)) {
            return List.of();
        }
        return annotate(classNode, compiled, encoding, "@groovy.transform.ToString");
    }

    static List<TextEdit> equalsAndHashCode(final ClassNode classNode, final CompiledDocument compiled,
                                            final PositionEncoding encoding) {
        Parameter[] equalsParams = {new Parameter(ClassHelper.OBJECT_TYPE, "o")};
        if (hasAst(classNode, "EqualsAndHashCode") || hasAst(classNode, CANONICAL)
                || hasDeclared(classNode, "equals", equalsParams)
                || hasDeclared(classNode, "hashCode", Parameter.EMPTY_ARRAY)) {
            return List.of();
        }
        return annotate(classNode, compiled, encoding, "@groovy.transform.EqualsAndHashCode");
    }

    static List<TextEdit> constructor(final ClassNode classNode, final CompiledDocument compiled,
                                      final PositionEncoding encoding) {
        if (hasAst(classNode, "TupleConstructor") || hasAst(classNode, CANONICAL)
                || hasAst(classNode, "MapConstructor")) {
            return List.of();
        }
        for (ConstructorNode constructor : classNode.getDeclaredConstructors()) {
            if (!constructor.isSynthetic()) {
                return List.of();
            }
        }
        if (instanceFields(classNode).isEmpty()) {
            return List.of();
        }
        return annotate(classNode, compiled, encoding, "@groovy.transform.TupleConstructor");
    }

    private static boolean hasAst(final ClassNode classNode, final String simpleName) {
        return classNode.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getClassNode() != null
                        && simpleName.equals(annotation.getClassNode().getNameWithoutPackage()));
    }

    private static List<TextEdit> annotate(final ClassNode classNode, final CompiledDocument compiled,
                                           final PositionEncoding encoding, final String annotation) {
        Range name = Positions.toNameRange(classNode, compiled.getText(), encoding);
        if (name == null) {
            return List.of();
        }
        int line = name.getStart().getLine();
        String row = Positions.lineText(compiled.getText(), line + 1);
        int indent = TextDocument.leadingWhitespace(row);
        String pad = indent <= 0 ? "" : row.substring(0, indent);
        return List.of(new TextEdit(new Range(new Position(line, 0), new Position(line, 0)),
                pad + annotation + "\n"));
    }

    private static TextEdit dropPrivate(final FieldNode field, final CompiledDocument compiled,
                                        final PositionEncoding encoding) {
        Range name = Positions.toNameRange(field, compiled.getText(), encoding);
        if (name == null) {
            return null;
        }
        String row = Positions.lineText(compiled.getText(), name.getStart().getLine() + 1);
        int nameAt = Math.min(Math.max(name.getStart().getCharacter(), 0), row.length());
        int at = lastWordIndex(row.substring(0, nameAt), "private");
        if (at < 0) {
            return null;
        }
        int end = at + "private".length();
        if (end < row.length() && row.charAt(end) == ' ') {
            end += 1;
        }
        int groovyLine = name.getStart().getLine() + 1;
        Position start = Positions.toLsp(groovyLine, at + 1, row, encoding);
        Position stop = Positions.toLsp(groovyLine, end + 1, row, encoding);
        return new TextEdit(new Range(start, stop), "");
    }

    static int wordIndex(final String line, final String word) {
        int from = 0;
        while (from < line.length()) {
            int at = line.indexOf(word, from);
            if (at < 0) {
                return -1;
            }
            boolean startOk = at == 0 || !Character.isJavaIdentifierPart(line.charAt(at - 1));
            int end = at + word.length();
            boolean endOk = end >= line.length() || !Character.isJavaIdentifierPart(line.charAt(end));
            if (startOk && endOk) {
                return at;
            }
            from = end;
        }
        return -1;
    }

    private static int lastWordIndex(final String line, final String word) {
        int from = 0;
        int found = -1;
        while (from < line.length()) {
            int at = wordIndex(line.substring(from), word);
            if (at < 0) {
                return found;
            }
            found = from + at;
            from = found + word.length();
        }
        return found;
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

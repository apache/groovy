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

import groovy.lang.groovydoc.Groovydoc;
import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.TypeIndex;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;

/**
 * Markdown hover for types, methods, fields and documentation comments.
 */
public final class HoverService {

    /**
     * @param document open document
     * @param snapshot latest compile
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return hover, or {@code null}
     */
    public Hover hover(final TextDocument document, final CompilationSnapshot snapshot,
                       final Position position, final PositionEncoding encoding) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        ModuleNode module = compiled == null ? null : compiled.getModule();
        ASTNode node = module == null ? null : AstQuery.nodeAt(module, document, position, encoding);
        ASTNode resolved = node == null ? null : resolveForHover(node, snapshot);
        String markdown = resolved == null ? null : render(resolved, module);
        if (markdown != null && (inferredCall(node, resolved)
                || (resolved instanceof VariableExpression variable
                && TypeInference.usedInitializer(variable, module)))) {
            markdown += "\n\n_inferred_";
        }
        if (markdown == null || markdown.isBlank()) {
            markdown = identifierHover(document, snapshot, position, encoding);
        }
        if (markdown == null || markdown.isBlank()) {
            return null;
        }
        Hover hover = new Hover(new MarkupContent(MarkupKind.MARKDOWN, markdown));
        if (node != null) {
            hover.setRange(Positions.toIdentifierRange(node, document.getText(), encoding));
        }
        return hover;
    }

    private static ASTNode resolveForHover(final ASTNode node, final CompilationSnapshot snapshot) {
        if (node instanceof MethodCallExpression call) {
            MethodNode target = NavigationService.resolveMethod(call, snapshot);
            if (target != null) {
                return target;
            }
        }
        return node;
    }

    private static String identifierHover(final TextDocument document, final CompilationSnapshot snapshot,
                                          final Position position, final PositionEncoding encoding) {
        String prefix = CompletionService.prefixAt(document, position, encoding);
        if (prefix == null || prefix.isEmpty()) {
            return null;
        }
        if (snapshot != null) {
            TypeIndex.TypeHit hit = snapshot.types().uniqueBySimpleName(prefix);
            if (hit != null) {
                return codeFence(hit.name());
            }
        }
        return codeFence(prefix);
    }

    static String render(final ASTNode node) {
        return render(node, null);
    }

    static String render(final ASTNode node, final ModuleNode module) {
        if (node instanceof MethodNode method) {
            return codeFence(signature(method)) + docs(method);
        }
        if (node instanceof ClassNode classNode) {
            return codeFence(kind(classNode) + " " + classNode.getName()) + docs(classNode);
        }
        if (node instanceof FieldNode field) {
            return renderField(field);
        }
        if (node instanceof PropertyNode property) {
            return codeFence(property.getType().getNameWithoutPackage() + " " + property.getName()) + docs(property);
        }
        if (node instanceof Parameter parameter) {
            return codeFence(parameter.getType().getNameWithoutPackage() + " " + parameter.getName());
        }
        if (node instanceof VariableExpression variable) {
            return renderVariable(variable, module);
        }
        if (node instanceof ClassExpression classExpression) {
            return codeFence(classExpression.getType().getName());
        }
        if (node instanceof MethodCallExpression call) {
            return renderCall(call);
        }
        if (node instanceof PropertyExpression property) {
            String name = property.getPropertyAsString();
            return codeFence(property.getType().getNameWithoutPackage() + " " + (name == null ? "?" : name));
        }
        if (node instanceof ImportNode imp) {
            return codeFence("import " + imp.getText());
        }
        if (node instanceof ConstantExpression constant) {
            return codeFence(String.valueOf(constant.getValue()));
        }
        return null;
    }

    private static String renderField(final FieldNode field) {
        return typedFence(TypeInference.of(field), field.getType(), field.getName()) + docs(field);
    }

    private static String renderVariable(final VariableExpression variable, final ModuleNode module) {
        return typedFence(TypeInference.of(variable, module), variable.getType(), variable.getName());
    }

    private static String typedFence(final ClassNode inferred, final ClassNode declared, final String name) {
        ClassNode type = inferred == null ? declared : inferred;
        String typeName = type == null ? "?" : type.getNameWithoutPackage();
        return codeFence(typeName + " " + name);
    }

    private static String renderCall(final MethodCallExpression call) {
        MethodNode target = call.getMethodTarget();
        if (target != null) {
            return codeFence(signature(target)) + docs(target);
        }
        return codeFence(call.getMethodAsString() + "(...)");
    }

    private static boolean inferredCall(final ASTNode node, final ASTNode resolved) {
        return node instanceof MethodCallExpression call && call.getMethodTarget() == null
                && resolved instanceof MethodNode;
    }

    private static String kind(final ClassNode classNode) {
        if (classNode.isInterface()) {
            return "interface";
        }
        if (classNode.isEnum()) {
            return "enum";
        }
        if (classNode.isRecord()) {
            return "record";
        }
        return "class";
    }

    private static String signature(final MethodNode method) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getReturnType().getNameWithoutPackage()).append(' ')
                .append(method.getDeclaringClass() == null ? "" : method.getDeclaringClass().getNameWithoutPackage() + ".")
                .append(method.getName()).append('(');
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameters[i].getType().getNameWithoutPackage()).append(' ').append(parameters[i].getName());
        }
        builder.append(')');
        return builder.toString();
    }

    private static String docs(final AnnotatedNode node) {
        Groovydoc groovydoc = node.getGroovydoc();
        if (groovydoc == null || !groovydoc.isPresent()) {
            return "";
        }
        String content = groovydoc.getContent();
        if (content == null || content.isBlank()) {
            return "";
        }
        return "\n\n" + content.replace("/**", "").replace("*/", "").strip();
    }

    private static String codeFence(final String groovy) {
        return "```groovy\n" + groovy + "\n```";
    }
}

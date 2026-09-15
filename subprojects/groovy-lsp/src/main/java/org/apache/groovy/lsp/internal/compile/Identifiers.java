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
package org.apache.groovy.lsp.internal.compile;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

/**
 * Word-boundary identifier scans, used to skip files that cannot contain a
 * reference and to detect unused imports. Also maps an AST node to the
 * identifier language servers should select, without extra fields on
 * {@code AnnotatedNode}.
 */
public final class Identifiers {

    private Identifiers() {
    }

    /**
     * @param text source
     * @param ident identifier
     * @return {@code true} when {@code ident} occurs as a Java identifier
     */
    public static boolean containsWord(final String text, final String ident) {
        return count(text, ident) > 0;
    }

    /**
     * Counts identifier occurrences with Java identifier boundaries.
     *
     * @param text source, possibly {@code null}
     * @param ident identifier, possibly {@code null}
     * @return the count
     */
    public static int count(final String text, final String ident) {
        if (text == null || ident == null || ident.isEmpty()) {
            return 0;
        }
        int count = 0;
        int from = 0;
        while (from < text.length()) {
            int at = text.indexOf(ident, from);
            if (at < 0) {
                return count;
            }
            boolean startOk = at == 0 || !Character.isJavaIdentifierPart(text.codePointBefore(at));
            int end = at + ident.length();
            boolean endOk = end >= text.length() || !Character.isJavaIdentifierPart(text.codePointAt(end));
            if (startOk && endOk) {
                count += 1;
            }
            from = at + ident.length();
        }
        return count;
    }

    /**
     * The identifier a language server should highlight for {@code node}.
     *
     * @param node any AST node
     * @return the name, or {@code null}
     */
    public static String nameOf(final ASTNode node) {
        if (node instanceof VariableExpression variable) {
            return variable.getName();
        }
        if (node instanceof Parameter parameter) {
            return parameter.getName();
        }
        if (node instanceof MethodNode method) {
            return method.getName();
        }
        if (node instanceof FieldNode field) {
            return field.getName();
        }
        if (node instanceof PropertyNode property) {
            return property.getName();
        }
        if (node instanceof ClassNode classNode) {
            return classNode.getNameWithoutPackage();
        }
        if (node instanceof MethodCallExpression call) {
            return call.getMethodAsString();
        }
        if (node instanceof StaticMethodCallExpression call) {
            return call.getMethodAsString();
        }
        if (node instanceof PropertyExpression property) {
            return property.getPropertyAsString();
        }
        if (node instanceof ClassExpression classExpression && classExpression.getType() != null) {
            return classExpression.getType().getNameWithoutPackage();
        }
        if (node instanceof ConstructorCallExpression ctor && ctor.getType() != null) {
            return ctor.getType().getNameWithoutPackage();
        }
        return null;
    }
}

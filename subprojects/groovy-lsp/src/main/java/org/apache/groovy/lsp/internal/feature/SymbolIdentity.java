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

import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.query.AstContext;

/**
 * Declaration identity for rename, references and highlight. Matching uses
 * the resolved binding (or receiver type + name), not the unqualified name
 * alone, so two methods both called {@code foo} stay distinct.
 */
final class SymbolIdentity {

    enum Kind {
        TYPE, METHOD, FIELD, VARIABLE
    }

    private final Kind kind;
    private final String key;
    private final ASTNode declaration;
    private final String name;

    private SymbolIdentity(final Kind kind, final String key, final ASTNode declaration, final String name) {
        this.kind = kind;
        this.key = key;
        this.declaration = declaration;
        this.name = name;
    }

    static SymbolIdentity of(final ASTNode node, final CompilationSnapshot snapshot) {
        ASTNode declaration = declarationOf(node, snapshot);
        if (declaration == null) {
            return null;
        }
        String key = keyOf(declaration);
        if (key == null) {
            return null;
        }
        return new SymbolIdentity(kindOf(declaration), key, declaration, NavigationService.nameOf(declaration));
    }

    ASTNode declaration() {
        return declaration;
    }

    String name() {
        return name;
    }

    Kind kind() {
        return kind;
    }

    boolean isDeclaration(final ASTNode node) {
        return node == declaration || (node != null && key.equals(keyOf(node)));
    }

    boolean refersTo(final ASTNode node, final AstContext context) {
        if (node == null) {
            return false;
        }
        if (node == declaration) {
            return true;
        }
        return switch (kind) {
            case TYPE -> refersToType(node);
            case METHOD -> refersToMethod(node, context);
            case FIELD -> refersToField(node);
            case VARIABLE -> refersToVariable(node);
        };
    }

    static ASTNode declarationOf(final ASTNode node, final CompilationSnapshot snapshot) {
        if (node instanceof VariableExpression variable) {
            return declarationOfVariable(variable);
        }
        if (node instanceof MethodCallExpression call) {
            return NavigationService.resolveMethod(call, snapshot);
        }
        if (node instanceof StaticMethodCallExpression call) {
            return NavigationService.resolveStatic(call, snapshot);
        }
        if (node instanceof PropertyExpression property) {
            return NavigationService.resolveProperty(property);
        }
        if (node instanceof ClassExpression classExpression && classExpression.getType() != null) {
            ClassNode type = classExpression.getType();
            return type.getLineNumber() > 0 ? type : classExpression;
        }
        if (node instanceof ConstructorCallExpression ctor && ctor.getType() != null) {
            return ctor.getType();
        }
        return node;
    }

    private static ASTNode declarationOfVariable(final VariableExpression variable) {
        Variable accessed = variable.getAccessedVariable();
        if (accessed instanceof ASTNode ast && ast.getLineNumber() > 0) {
            return ast;
        }
        return variable;
    }

    static String keyOf(final ASTNode node) {
        if (node instanceof MethodNode method) {
            String owner = ownerName(method.getDeclaringClass());
            String descriptor = method.getTypeDescriptor() == null ? "" : method.getTypeDescriptor();
            return "M:" + owner + "#" + method.getName() + descriptor;
        }
        if (node instanceof ClassNode classNode) {
            return "C:" + classNode.getName();
        }
        if (node instanceof FieldNode field) {
            return "F:" + ownerName(field.getDeclaringClass()) + "#" + field.getName();
        }
        if (node instanceof PropertyNode property) {
            return "P:" + ownerName(property.getDeclaringClass()) + "#" + property.getName();
        }
        if (node instanceof Parameter parameter) {
            return "V:" + parameter.getName() + "@" + parameter.getLineNumber() + ":" + parameter.getColumnNumber();
        }
        if (node instanceof VariableExpression variable) {
            return "V:" + variable.getName() + "@" + variable.getLineNumber() + ":" + variable.getColumnNumber();
        }
        if (node instanceof MethodCallExpression call) {
            return "M:?#" + call.getMethodAsString();
        }
        if (node instanceof StaticMethodCallExpression call) {
            return "M:" + ownerName(call.getOwnerType()) + "#" + call.getMethodAsString();
        }
        if (node instanceof PropertyExpression property) {
            return "F:?#" + property.getPropertyAsString();
        }
        if (node instanceof ClassExpression classExpression && classExpression.getType() != null) {
            return "C:" + classExpression.getType().getName();
        }
        return null;
    }

    private static Kind kindOf(final ASTNode node) {
        if (node instanceof MethodNode || node instanceof MethodCallExpression
                || node instanceof StaticMethodCallExpression) {
            return Kind.METHOD;
        }
        if (node instanceof ClassNode || node instanceof ClassExpression
                || node instanceof ConstructorCallExpression) {
            return Kind.TYPE;
        }
        if (node instanceof FieldNode || node instanceof PropertyNode || node instanceof PropertyExpression) {
            return Kind.FIELD;
        }
        return Kind.VARIABLE;
    }

    private boolean refersToType(final ASTNode node) {
        if (node instanceof ClassNode || node instanceof ClassExpression || node instanceof ConstructorCallExpression) {
            return key.equals(keyOf(node));
        }
        return false;
    }

    private boolean refersToMethod(final ASTNode node, final AstContext context) {
        if (node instanceof MethodNode) {
            return key.equals(keyOf(node));
        }
        if (node instanceof MethodCallExpression call) {
            if (declaration instanceof MethodNode method) {
                return NavigationService.callMatches(call, method, context);
            }
            return name != null && name.equals(call.getMethodAsString()) && key.equals(keyOf(call));
        }
        if (node instanceof StaticMethodCallExpression call) {
            if (declaration instanceof MethodNode method) {
                return NavigationService.staticCallMatches(call, method);
            }
            return key.equals(keyOf(call));
        }
        return false;
    }

    private boolean refersToField(final ASTNode node) {
        if (node instanceof FieldNode || node instanceof PropertyNode) {
            return ownerAndName(key).equals(ownerAndName(keyOf(node)));
        }
        if (node instanceof PropertyExpression property) {
            ASTNode target = NavigationService.resolveProperty(property);
            if (target != null) {
                return isDeclaration(target) || ownerAndName(key).equals(ownerAndName(keyOf(target)));
            }
            String propertyName = property.getPropertyAsString();
            return name != null && name.equals(propertyName)
                    && NavigationService.receiverMatchesOwner(property.getObjectExpression(), ownerFromKey(key));
        }
        if (node instanceof VariableExpression variable) {
            Variable accessed = variable.getAccessedVariable();
            return accessed == declaration || (accessed instanceof ASTNode ast && isDeclaration(ast));
        }
        return false;
    }

    private boolean refersToVariable(final ASTNode node) {
        if (node == declaration) {
            return true;
        }
        if (node instanceof VariableExpression variable) {
            Variable accessed = variable.getAccessedVariable();
            if (accessed == declaration) {
                return true;
            }
            return accessed instanceof ASTNode ast && key.equals(keyOf(ast));
        }
        return key.equals(keyOf(node));
    }

    private static String ownerName(final ClassNode type) {
        return type == null ? "" : type.getName();
    }

    private static String ownerAndName(final String key) {
        if (key == null) {
            return "";
        }
        int colon = key.indexOf(':');
        return colon < 0 ? key : key.substring(colon + 1);
    }

    private static String ownerFromKey(final String key) {
        if (key == null) {
            return "";
        }
        int colon = key.indexOf(':');
        int hash = key.indexOf('#');
        if (colon < 0 || hash < 0 || hash <= colon) {
            return "";
        }
        return key.substring(colon + 1, hash);
    }

    static boolean isResolvedType(final ClassNode type) {
        if (type == null || ClassHelper.isDynamicTyped(type) || ClassHelper.isObjectType(type)) {
            return false;
        }
        String name = type.getName();
        return name != null && !name.isEmpty();
    }
}

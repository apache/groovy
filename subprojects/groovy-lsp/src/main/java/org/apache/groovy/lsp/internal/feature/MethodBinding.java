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

import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.query.AstContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves calls, properties and overrides against a compilation snapshot.
 * Definition and rename share this, so a heuristic cannot diverge from navigation.
 */
public final class MethodBinding {

    private MethodBinding() {
    }

    static MethodNode resolveMethod(final MethodCallExpression call, final CompilationSnapshot snapshot) {
        List<MethodNode> methods = resolveMethodCandidates(call, snapshot);
        return methods.size() == 1 ? methods.get(0) : null;
    }

    static MethodNode resolveStatic(final StaticMethodCallExpression call, final CompilationSnapshot snapshot) {
        List<MethodNode> methods = resolveMethodCandidates(call, snapshot);
        return methods.size() == 1 ? methods.get(0) : null;
    }

    /**
     * All arity-matching methods for definition. When {@code methodTarget}
     * is set, that is the only candidate.
     *
     * @param node a method or static call
     * @param snapshot latest compile
     * @return candidates, never {@code null}
     */
    static List<MethodNode> resolveMethodCandidates(final ASTNode node, final CompilationSnapshot snapshot) {
        if (node instanceof MethodCallExpression call) {
            if (call.getMethodTarget() != null) {
                return List.of(call.getMethodTarget());
            }
            String name = call.getMethodAsString();
            int argc = argumentCount(call.getArguments());
            List<MethodNode> found = findMethods(typeOf(call.getObjectExpression()), name, argc);
            if (found.isEmpty() && call.isImplicitThis()) {
                found = findMethods(enclosingClass(call, snapshot), name, argc);
            }
            return found;
        }
        if (node instanceof StaticMethodCallExpression call) {
            return findMethods(call.getOwnerType(), call.getMethodAsString(), argumentCount(call.getArguments()));
        }
        return List.of();
    }

    /**
     * Unique-arity constructors for {@code new Type(...)}. Empty when
     * anonymous or the type is unresolved. Exact arity only.
     *
     * @param ctor a constructor call
     * @return candidates, never {@code null}
     */
    static List<ConstructorNode> resolveConstructorCandidates(final ConstructorCallExpression ctor) {
        if (ctor == null || ctor.isUsingAnonymousInnerClass()) {
            return List.of();
        }
        ClassNode type = ctor.getType();
        if (!SymbolIdentity.isResolvedType(type)) {
            return List.of();
        }
        return matchingArity(type.getDeclaredConstructors(), argumentCount(ctor.getArguments()));
    }

    static ConstructorNode resolveConstructor(final ConstructorCallExpression ctor) {
        List<ConstructorNode> found = resolveConstructorCandidates(ctor);
        return found.size() == 1 ? found.get(0) : null;
    }

    /**
     * Whether rename/prepareRename may edit this symbol. Declarations are
     * safe. Calls and properties are safe only when the compiler bound a
     * unique target ({@code methodTarget} or a uniquely resolved field).
     * A name-and-arity heuristic is not enough: it can rewrite the wrong
     * overload in dynamically typed Groovy.
     *
     * @param node node under the caret
     * @param snapshot latest compile
     * @return {@code true} when a workspace edit is justified
     */
    static boolean isRenameSafe(final ASTNode node, final CompilationSnapshot snapshot) {
        if (node instanceof MethodCallExpression call) {
            return call.getMethodTarget() != null;
        }
        if (node instanceof StaticMethodCallExpression call) {
            return resolveStatic(call, snapshot) != null;
        }
        if (node instanceof PropertyExpression property) {
            return resolveProperty(property) != null;
        }
        if (node instanceof VariableExpression variable) {
            Variable accessed = variable.getAccessedVariable();
            return accessed instanceof ASTNode ast && ast.getLineNumber() > 0;
        }
        return node instanceof ClassNode || node instanceof MethodNode || node instanceof FieldNode
                || node instanceof PropertyNode || node instanceof Parameter;
    }

    static ASTNode resolveProperty(final PropertyExpression property) {
        if (property == null) {
            return null;
        }
        String name = property.getPropertyAsString();
        ClassNode type = typeOf(property.getObjectExpression());
        if (name == null || !SymbolIdentity.isResolvedType(type)) {
            return null;
        }
        FieldNode field = type.getField(name);
        if (field != null && !field.isSynthetic()) {
            return field;
        }
        PropertyNode prop = type.getProperty(name);
        if (prop != null && !prop.isSynthetic()) {
            return prop;
        }
        return null;
    }

    static boolean callMatches(final MethodCallExpression call, final MethodNode method,
                               final AstContext context) {
        if (call == null || method == null || !method.getName().equals(call.getMethodAsString())) {
            return false;
        }
        MethodNode target = call.getMethodTarget();
        if (target != null) {
            return SymbolIdentity.keyOf(target).equals(SymbolIdentity.keyOf(method));
        }
        ClassNode receiver = typeOf(call.getObjectExpression());
        ClassNode owner = method.getDeclaringClass();
        if (SymbolIdentity.isResolvedType(receiver) && owner != null) {
            return sameOrDerived(receiver, owner);
        }
        if (call.isImplicitThis() && context != null && context.enclosingClass() != null && owner != null) {
            return sameOrDerived(context.enclosingClass(), owner);
        }
        return false;
    }

    static boolean staticCallMatches(final StaticMethodCallExpression call, final MethodNode method) {
        if (call == null || method == null || !method.getName().equals(call.getMethodAsString())) {
            return false;
        }
        ClassNode owner = method.getDeclaringClass();
        return owner != null && sameType(owner, call.getOwnerType());
    }

    static boolean receiverMatchesOwner(final Expression receiver, final String ownerName) {
        if (ownerName == null || ownerName.isEmpty()) {
            return false;
        }
        ClassNode type = typeOf(receiver);
        return SymbolIdentity.isResolvedType(type) && ownerName.equals(type.getName());
    }

    static List<ClassNode> implementations(final ClassNode type, final CompilationSnapshot snapshot) {
        List<ClassNode> result = new ArrayList<>();
        if (snapshot == null || type == null) {
            return result;
        }
        for (CompiledDocument document : snapshot.documents()) {
            if (document.getModule() == null) {
                continue;
            }
            for (ClassNode classNode : document.getModule().getClasses()) {
                if (classNode == type || sameType(classNode, type) || classNode.getLineNumber() <= 0) {
                    continue;
                }
                if (implementsOrExtends(classNode, type)) {
                    result.add(classNode);
                }
            }
        }
        return result;
    }

    static List<MethodNode> methodImplementations(final MethodNode method, final CompilationSnapshot snapshot) {
        List<MethodNode> result = new ArrayList<>();
        if (method == null || method.getName() == null || method.getDeclaringClass() == null) {
            return result;
        }
        int arity = method.getParameters() == null ? 0 : method.getParameters().length;
        for (ClassNode classNode : implementations(method.getDeclaringClass(), snapshot)) {
            for (MethodNode candidate : classNode.getMethods(method.getName())) {
                if (candidate == null || candidate == method || candidate.getLineNumber() <= 0) {
                    continue;
                }
                Parameter[] parameters = candidate.getParameters();
                int count = parameters == null ? 0 : parameters.length;
                if (count == arity) {
                    result.add(candidate);
                }
            }
        }
        return result;
    }
    static MethodNode methodAt(final ASTNode node, final CompilationSnapshot snapshot) {
        if (node instanceof MethodNode method) {
            return method;
        }
        if (node instanceof MethodCallExpression call) {
            return resolveMethod(call, snapshot);
        }
        if (node instanceof StaticMethodCallExpression call) {
            return resolveStatic(call, snapshot);
        }
        return null;
    }

    static MethodNode superOf(final MethodNode method) {
        if (method == null || method.getDeclaringClass() == null) {
            return null;
        }
        ClassNode type = method.getDeclaringClass();
        MethodNode found = matchSuper(type.getSuperClass(), method);
        if (found != null) {
            return found;
        }
        if (type.getInterfaces() != null) {
            for (ClassNode iface : type.getInterfaces()) {
                found = matchSuper(iface, method);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static MethodNode matchSuper(final ClassNode type, final MethodNode method) {
        if (type == null || skipSuperOwner(type)) {
            return null;
        }
        MethodNode declared = type.getDeclaredMethod(method.getName(), method.getParameters());
        if (declared == null) {
            declared = type.getMethod(method.getName(), method.getParameters());
        }
        if (declared != null && declared != method) {
            return declared;
        }
        MethodNode further = matchSuper(type.getSuperClass(), method);
        if (further != null) {
            return further;
        }
        if (type.getInterfaces() != null) {
            for (ClassNode iface : type.getInterfaces()) {
                further = matchSuper(iface, method);
                if (further != null) {
                    return further;
                }
            }
        }
        return null;
    }

    private static boolean skipSuperOwner(final ClassNode type) {
        String name = type.getName();
        return "java.lang.Object".equals(name) || "groovy.lang.GroovyObject".equals(name)
                || "groovy.lang.GroovyObjectSupport".equals(name);
    }

    static MethodNode findMethod(final CompilationSnapshot snapshot, final String owner, final String name,
                                 final String descriptor) {
        if (snapshot == null || name == null) {
            return null;
        }
        MethodNode fallback = null;
        for (CompiledDocument document : snapshot.documents()) {
            MethodNode match = findMethodIn(document, owner, name, descriptor);
            if (match == null) {
                continue;
            }
            if (descriptor != null && !descriptor.isEmpty() && descriptor.equals(match.getTypeDescriptor())) {
                return match;
            }
            if (fallback == null) {
                fallback = match;
            }
        }
        return fallback;
    }

    private static MethodNode findMethodIn(final CompiledDocument document, final String owner, final String name,
                                           final String descriptor) {
        if (document.getModule() == null) {
            return null;
        }
        MethodNode fallback = null;
        for (ClassNode classNode : document.getModule().getClasses()) {
            if (ownerMatches(owner, classNode)) {
                MethodNode hit = methodOn(classNode, name, descriptor);
                if (hit != null && descriptor != null && !descriptor.isEmpty()
                        && descriptor.equals(hit.getTypeDescriptor())) {
                    return hit;
                }
                if (fallback == null) {
                    fallback = hit;
                }
            }
        }
        return fallback;
    }

    private static boolean ownerMatches(final String owner, final ClassNode classNode) {
        return owner == null || owner.isEmpty() || owner.equals(classNode.getName());
    }

    private static MethodNode methodOn(final ClassNode classNode, final String name, final String descriptor) {
        MethodNode fallback = null;
        for (MethodNode method : classNode.getMethods()) {
            if (name.equals(method.getName()) && method.getLineNumber() > 0) {
                if (descriptor != null && !descriptor.isEmpty()) {
                    if (descriptor.equals(method.getTypeDescriptor())) {
                        return method;
                    }
                } else if (fallback == null) {
                    fallback = method;
                }
            }
        }
        return fallback;
    }

    private static List<MethodNode> findMethods(final ClassNode type, final String name, final int argc) {
        if (!SymbolIdentity.isResolvedType(type) || name == null) {
            return List.of();
        }
        return matchingArity(type.getMethods(name), argc);
    }

    private static <T extends MethodNode> List<T> matchingArity(final Iterable<T> methods, final int argc) {
        List<T> matches = new ArrayList<>();
        if (methods == null) {
            return matches;
        }
        for (T method : methods) {
            if (method == null || method.isSynthetic()) {
                continue;
            }
            int count = method.getParameters() == null ? 0 : method.getParameters().length;
            if (count == argc) {
                matches.add(method);
            }
        }
        return matches;
    }

    private static ClassNode enclosingClass(final MethodCallExpression call, final CompilationSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        ClassNode[] found = new ClassNode[1];
        for (CompiledDocument document : snapshot.documents()) {
            if (document.getModule() == null) {
                continue;
            }
            AstQuery.walk(document.getModule(), (node, ctx) -> {
                if (node == call && ctx.enclosingClass() != null) {
                    found[0] = ctx.enclosingClass();
                }
            });
            if (found[0] != null) {
                return found[0];
            }
        }
        return null;
    }

    static List<MethodNode> overloads(final ClassNode type, final String name) {
        List<MethodNode> result = new ArrayList<>();
        if (type == null || name == null) {
            return result;
        }
        for (MethodNode method : type.getMethods(name)) {
            if (!method.isSynthetic()) {
                result.add(method);
            }
        }
        return result;
    }

    /**
     * All visible overloads of a call, including implicit {@code this}.
     * Signature help lists every overload; it must not require a unique match.
     *
     * @param call a method call
     * @param snapshot latest compile
     * @return overloads, possibly empty
     */
    static List<MethodNode> overloads(final MethodCallExpression call, final CompilationSnapshot snapshot) {
        if (call == null) {
            return List.of();
        }
        List<MethodNode> found = overloads(typeOf(call.getObjectExpression()), call.getMethodAsString());
        if (found.isEmpty() && call.isImplicitThis()) {
            found = overloads(enclosingClass(call, snapshot), call.getMethodAsString());
        }
        return found;
    }

    static int argumentCount(final Expression arguments) {
        if (arguments instanceof TupleExpression tuple) {
            return tuple.getExpressions().size();
        }
        return arguments == null ? 0 : 1;
    }

    private static ClassNode typeOf(final Expression expression) {
        return expression == null ? null : expression.getType();
    }

    static boolean sameType(final ClassNode left, final ClassNode right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.equals(right)) {
            return true;
        }
        return left.getName() != null && left.getName().equals(right.getName());
    }

    static boolean sameOrDerived(final ClassNode receiver, final ClassNode owner) {
        if (sameType(receiver, owner)) {
            return true;
        }
        return implementsOrExtends(receiver, owner);
    }

    private static boolean implementsOrExtends(final ClassNode candidate, final ClassNode type) {
        if (candidate == null || type == null || sameType(candidate, type)) {
            return false;
        }
        if (sameType(candidate.getSuperClass(), type)) {
            return true;
        }
        if (candidate.isDerivedFrom(type)) {
            return true;
        }
        return candidate.implementsInterface(type);
    }
}

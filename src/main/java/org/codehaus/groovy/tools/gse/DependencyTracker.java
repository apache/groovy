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
package org.codehaus.groovy.tools.gse;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.control.SourceUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tracks type dependencies referenced from a source unit and stores them in a
 * {@link StringSetMap}.
 */
public class DependencyTracker extends ClassCodeVisitorSupport {
    private Set<String> current;
    private final Map<String, ?> precompiledDependencies;
    private final SourceUnit source;
    private final StringSetMap cache;

    /**
     * Creates a dependency tracker for a source unit.
     *
     * @param source the source unit being analyzed
     * @param cache the dependency cache to populate
     */
    public DependencyTracker(SourceUnit source, StringSetMap cache) {
        this(source, cache, new HashMap());
    }

    /**
     * Creates a dependency tracker for a source unit with an explicit set of
     * precompiled dependency entries.
     *
     * @param source the source unit being analyzed
     * @param cache the dependency cache to populate
     * @param precompiledEntries precompiled classes considered valid
     * dependencies
     */
    public DependencyTracker(SourceUnit source, StringSetMap cache, Map<String, ?> precompiledEntries) {
        this.source = source;
        this.cache = cache;
        this.precompiledDependencies = precompiledEntries;
    }

    private void addToCache(ClassNode node){
        if (node == null) return;
        String name = node.getName();
        if (!precompiledDependencies.containsKey(name)  &&
            !node.isPrimaryClassNode())
        {
            return;
        }
        current.add(node.getName());
        addToCache(node.getSuperClass());
        addToCache(node.getInterfaces());
    }

    private void addToCache(ClassNode[] nodes){
        if (nodes==null) return;
        for (ClassNode node : nodes) addToCache(node);
    }

    /**
     * Visits a class and records its direct type dependencies.
     *
     * @param node the class being visited
     */
    @Override
    public void visitClass(ClassNode node) {
        Set<String> old = current;
        current = cache.get(node.getName());
        addToCache(node);
        super.visitClass(node);
        current =  old;
    }

    /**
     * Returns the source unit whose dependencies are being tracked.
     *
     * @return the current source unit
     */
    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    /**
     * Records the type referenced by a class literal expression.
     *
     * @param expression the class expression being visited
     */
    @Override
    public void visitClassExpression(ClassExpression expression) {
        super.visitClassExpression(expression);
        addToCache(expression.getType());
    }

    /**
     * Records the declared type of a visited field.
     *
     * @param node the field being visited
     */
    @Override
    public void visitField(FieldNode node) {
        super.visitField(node);
        addToCache(node.getType());
    }

    /**
     * Records parameter, return, and exception types referenced by a method.
     *
     * @param node the method being visited
     */
    @Override
    public void visitMethod(MethodNode node) {
        for (Parameter p : node.getParameters()) {
            addToCache(p.getType());
        }
        addToCache(node.getReturnType());
        addToCache(node.getExceptions());
        super.visitMethod(node);
    }

    /**
     * Records the array type referenced by an array expression.
     *
     * @param expression the array expression being visited
     */
    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        super.visitArrayExpression(expression);
        addToCache(expression.getType());
    }

    /**
     * Records the target type referenced by a cast expression.
     *
     * @param expression the cast expression being visited
     */
    @Override
    public void visitCastExpression(CastExpression expression) {
        super.visitCastExpression(expression);
        addToCache(expression.getType());
    }

    /**
     * Records the type referenced by a variable expression.
     *
     * @param expression the variable expression being visited
     */
    @Override
    public void visitVariableExpression(VariableExpression expression) {
        super.visitVariableExpression(expression);
        addToCache(expression.getType());
    }

    /**
     * Records the exception-variable type referenced by a catch statement.
     *
     * @param statement the catch statement being visited
     */
    @Override
    public void visitCatchStatement(CatchStatement statement) {
        super.visitCatchStatement(statement);
        addToCache(statement.getVariable().getType());
    }

    /**
     * Records the types of annotations attached to the supplied node.
     *
     * @param node the annotated node being visited
     */
    @Override
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode an : node.getAnnotations()) {
            addToCache(an.getClassNode());
        }
    }

    /**
     * Records the constructed type referenced by a constructor call.
     *
     * @param call the constructor call expression being visited
     */
    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);
        addToCache(call.getType());
    }
}

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

public class DependencyTracker extends ClassCodeVisitorSupport {
    private Set<String> current;
    private final Map<String, ?> precompiledDependencies;
    private final SourceUnit source;
    private final StringSetMap cache;

    public DependencyTracker(SourceUnit source, StringSetMap cache) {
        this(source, cache, new HashMap());
    }
    
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

    @Override
    public void visitClass(ClassNode node) {
        Set<String> old = current;
        current = cache.get(node.getName());
        addToCache(node);
        super.visitClass(node);
        current =  old;
    }
    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }
    @Override
    public void visitClassExpression(ClassExpression expression) {
        super.visitClassExpression(expression);
        addToCache(expression.getType());
    }
    @Override
    public void visitField(FieldNode node) {
        super.visitField(node);
        addToCache(node.getType());
    }
    @Override
    public void visitMethod(MethodNode node) {
        for (Parameter p : node.getParameters()) {
            addToCache(p.getType());
        }
        addToCache(node.getReturnType());
        addToCache(node.getExceptions());
        super.visitMethod(node);
    }
    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        super.visitArrayExpression(expression);
        addToCache(expression.getType());
    }
    @Override
    public void visitCastExpression(CastExpression expression) {
        super.visitCastExpression(expression);
        addToCache(expression.getType());
    }
    @Override
    public void visitVariableExpression(VariableExpression expression) {
        super.visitVariableExpression(expression);
        addToCache(expression.getType());
    }
    @Override
    public void visitCatchStatement(CatchStatement statement) {
        super.visitCatchStatement(statement);
        addToCache(statement.getVariable().getType());
    }
    @Override
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode an : node.getAnnotations()) {
            addToCache(an.getClassNode());
        }
    }
    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);
        addToCache(call.getType());
    }
}

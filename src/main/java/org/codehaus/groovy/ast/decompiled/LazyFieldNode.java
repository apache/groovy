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
package org.codehaus.groovy.ast.decompiled;

import groovy.lang.groovydoc.Groovydoc;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.NodeMetaDataHandler;
import org.codehaus.groovy.ast.expr.Expression;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents lazy field node, which will be initialized only when accessed
 *
 * @since 3.0.0
 */
public class LazyFieldNode extends FieldNode {
    private final Supplier<FieldNode> fieldNodeSupplier;
    private FieldNode delegate;
    private boolean initialized;

    private String name;

    public LazyFieldNode(Supplier<FieldNode> fieldNodeSupplier, String name) {
        this.fieldNodeSupplier = fieldNodeSupplier;
        this.name = name;
    }

    private void init() {
        if (initialized) return;
        delegate = fieldNodeSupplier.get();

        ClassNode declaringClass = super.getDeclaringClass();
        if (null != declaringClass) delegate.setDeclaringClass(declaringClass);

        ClassNode owner = super.getOwner();
        if (null != owner) delegate.setOwner(owner);

        initialized = true;
    }

    @Override
    public Expression getInitialExpression() {
        init();
        return delegate.getInitialExpression();
    }

    @Override
    public int getModifiers() {
        init();
        return delegate.getModifiers();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ClassNode getType() {
        init();
        return delegate.getType();
    }

    @Override
    public void setType(ClassNode type) {
        init();
        delegate.setType(type);
    }

    @Override
    public ClassNode getOwner() {
        init();
        return delegate.getOwner();
    }

    @Override
    public boolean isHolder() {
        init();
        return delegate.isHolder();
    }

    @Override
    public void setHolder(boolean holder) {
        init();
        delegate.setHolder(holder);
    }

    @Override
    public boolean isDynamicTyped() {
        init();
        return delegate.isDynamicTyped();
    }

    @Override
    public void setModifiers(int modifiers) {
        init();
        delegate.setModifiers(modifiers);
    }

    @Override
    public boolean isStatic() {
        init();
        return delegate.isStatic();
    }

    @Override
    public boolean isEnum() {
        init();
        return delegate.isEnum();
    }

    @Override
    public boolean isFinal() {
        init();
        return delegate.isFinal();
    }

    @Override
    public boolean isVolatile() {
        init();
        return delegate.isVolatile();
    }

    @Override
    public boolean isPublic() {
        init();
        return delegate.isPublic();
    }

    @Override
    public boolean isProtected() {
        init();
        return delegate.isProtected();
    }

    @Override
    public boolean isPrivate() {
        init();
        return delegate.isPrivate();
    }

    @Override
    public void setOwner(ClassNode owner) {
        super.setOwner(owner);
    }

    @Override
    public boolean hasInitialExpression() {
        init();
        return delegate.hasInitialExpression();
    }

    @Override
    public boolean isInStaticContext() {
        init();
        return delegate.isInStaticContext();
    }

    @Override
    public Expression getInitialValueExpression() {
        init();
        return delegate.getInitialValueExpression();
    }

    @Override
    public void setInitialValueExpression(Expression initialValueExpression) {
        init();
        delegate.setInitialValueExpression(initialValueExpression);
    }

    @Override
    @Deprecated
    public boolean isClosureSharedVariable() {
        init();
        return delegate.isClosureSharedVariable();
    }

    @Override
    @Deprecated
    public void setClosureSharedVariable(boolean inClosure) {
        init();
        delegate.setClosureSharedVariable(inClosure);
    }

    @Override
    public ClassNode getOriginType() {
        init();
        return delegate.getOriginType();
    }

    @Override
    public void setOriginType(ClassNode cn) {
        init();
        delegate.setOriginType(cn);
    }

    @Override
    public void rename(String name) {
        init();
        delegate.rename(name);
    }

    @Override
    public List<AnnotationNode> getAnnotations() {
        init();
        return delegate.getAnnotations();
    }

    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        init();
        return delegate.getAnnotations(type);
    }

    @Override
    public void addAnnotation(AnnotationNode annotation) {
        init();
        delegate.addAnnotation(annotation);
    }

    @Override
    public void addAnnotations(List<AnnotationNode> annotations) {
        init();
        delegate.addAnnotations(annotations);
    }

    @Override
    public ClassNode getDeclaringClass() {
        init();
        return delegate.getDeclaringClass();
    }

    @Override
    public void setDeclaringClass(ClassNode declaringClass) {
        super.setDeclaringClass(declaringClass);
    }

    @Override
    public Groovydoc getGroovydoc() {
        init();
        return delegate.getGroovydoc();
    }

    @Override
    public AnnotatedNode getInstance() {
        init();
        return delegate.getInstance();
    }

    @Override
    public boolean hasNoRealSourcePosition() {
        init();
        return delegate.hasNoRealSourcePosition();
    }

    @Override
    public void setHasNoRealSourcePosition(boolean hasNoRealSourcePosition) {
        init();
        delegate.setHasNoRealSourcePosition(hasNoRealSourcePosition);
    }

    @Override
    public boolean isSynthetic() {
        init();
        return delegate.isSynthetic();
    }

    @Override
    public void setSynthetic(boolean synthetic) {
        init();
        delegate.setSynthetic(synthetic);
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        init();
        delegate.visit(visitor);
    }

    @Override
    public String getText() {
        init();
        return delegate.getText();
    }

    @Override
    public int getLineNumber() {
        init();
        return delegate.getLineNumber();
    }

    @Override
    public void setLineNumber(int lineNumber) {
        init();
        delegate.setLineNumber(lineNumber);
    }

    @Override
    public int getColumnNumber() {
        init();
        return delegate.getColumnNumber();
    }

    @Override
    public void setColumnNumber(int columnNumber) {
        init();
        delegate.setColumnNumber(columnNumber);
    }

    @Override
    public int getLastLineNumber() {
        init();
        return delegate.getLastLineNumber();
    }

    @Override
    public void setLastLineNumber(int lastLineNumber) {
        init();
        delegate.setLastLineNumber(lastLineNumber);
    }

    @Override
    public int getLastColumnNumber() {
        init();
        return delegate.getLastColumnNumber();
    }

    @Override
    public void setLastColumnNumber(int lastColumnNumber) {
        init();
        delegate.setLastColumnNumber(lastColumnNumber);
    }

    @Override
    public void setSourcePosition(ASTNode node) {
        init();
        delegate.setSourcePosition(node);
    }

    @Override
    public void copyNodeMetaData(ASTNode other) {
        init();
        delegate.copyNodeMetaData(other);
    }

    @Override
    public Map<?, ?> getMetaDataMap() {
        init();
        return delegate.getMetaDataMap();
    }

    @Override
    public void setMetaDataMap(Map<?, ?> metaDataMap) {
        init();
        delegate.setMetaDataMap(metaDataMap);
    }

    @Override
    public int hashCode() {
        init();
        return delegate.hashCode();
    }

    @Override
    public <T> T getNodeMetaData(Object key) {
        init();
        return delegate.getNodeMetaData(key);
    }

    @Override
    public <T> T getNodeMetaData(Object key, Function<?, ? extends T> valFn) {
        init();
        return delegate.getNodeMetaData(key, valFn);
    }

    @Override
    public void copyNodeMetaData(NodeMetaDataHandler other) {
        init();
        delegate.copyNodeMetaData(other);
    }

    @Override
    public void setNodeMetaData(Object key, Object value) {
        init();
        delegate.setNodeMetaData(key, value);
    }

    @Override
    public Object putNodeMetaData(Object key, Object value) {
        init();
        return delegate.putNodeMetaData(key, value);
    }

    @Override
    public void removeNodeMetaData(Object key) {
        init();
        delegate.removeNodeMetaData(key);
    }

    @Override
    public Map<?, ?> getNodeMetaData() {
        init();
        return delegate.getNodeMetaData();
    }
}

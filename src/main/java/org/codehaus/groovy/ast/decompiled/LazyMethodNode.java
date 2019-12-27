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
import org.apache.groovy.util.concurrent.LazyInitializable;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.NodeMetaDataHandler;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents lazy method node, which will be initialized only when accessed
 *
 * @since 2.5.9
 */
class LazyMethodNode extends MethodNode implements LazyInitializable {
    private final Supplier<MethodNode> methodNodeSupplier;
    private MethodNode delegate;

    private final String name;

    private volatile boolean initialized;

    public LazyMethodNode(Supplier<MethodNode> methodNodeSupplier, String name) {
        this.methodNodeSupplier = methodNodeSupplier;
        this.name = name;
    }

    @Override
    public void doInit() {
        delegate = methodNodeSupplier.get();

        ClassNode declaringClass = super.getDeclaringClass();
        if (null != declaringClass) delegate.setDeclaringClass(declaringClass);
    }
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    @Override
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public String getTypeDescriptor() {
        lazyInit();
        return delegate.getTypeDescriptor();
    }

    @Override
    public Statement getCode() {
        lazyInit();
        return delegate.getCode();
    }

    @Override
    public void setCode(Statement code) {
        lazyInit();
        delegate.setCode(code);
    }

    @Override
    public int getModifiers() {
        lazyInit();
        return delegate.getModifiers();
    }

    @Override
    public void setModifiers(int modifiers) {
        lazyInit();
        delegate.setModifiers(modifiers);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Parameter[] getParameters() {
        lazyInit();
        return delegate.getParameters();
    }

    @Override
    public void setParameters(Parameter[] parameters) {
        lazyInit();
        delegate.setParameters(parameters);
    }

    @Override
    public boolean hasDefaultValue() {
        lazyInit();
        return delegate.hasDefaultValue();
    }

    @Override
    public ClassNode getReturnType() {
        lazyInit();
        return delegate.getReturnType();
    }

    @Override
    public void setReturnType(ClassNode returnType) {
        lazyInit();
        delegate.setReturnType(returnType);
    }

    @Override
    public boolean isDynamicReturnType() {
        lazyInit();
        return delegate.isDynamicReturnType();
    }

    @Override
    public boolean isVoidMethod() {
        lazyInit();
        return delegate.isVoidMethod();
    }

    @Override
    public VariableScope getVariableScope() {
        lazyInit();
        return delegate.getVariableScope();
    }

    @Override
    public void setVariableScope(VariableScope variableScope) {
        lazyInit();
        delegate.setVariableScope(variableScope);
    }

    @Override
    public boolean isAbstract() {
        lazyInit();
        return delegate.isAbstract();
    }

    @Override
    public boolean isDefault() {
        lazyInit();
        return delegate.isDefault();
    }

    @Override
    public boolean isFinal() {
        lazyInit();
        return delegate.isFinal();
    }

    @Override
    public boolean isStatic() {
        lazyInit();
        return delegate.isStatic();
    }

    @Override
    public boolean isPublic() {
        lazyInit();
        return delegate.isPublic();
    }

    @Override
    public boolean isPrivate() {
        lazyInit();
        return delegate.isPrivate();
    }

    @Override
    public boolean isProtected() {
        lazyInit();
        return delegate.isProtected();
    }

    @Override
    public boolean isPackageScope() {
        lazyInit();
        return delegate.isPackageScope();
    }

    @Override
    public ClassNode[] getExceptions() {
        lazyInit();
        return delegate.getExceptions();
    }

    @Override
    public Statement getFirstStatement() {
        lazyInit();
        return delegate.getFirstStatement();
    }

    @Override
    public GenericsType[] getGenericsTypes() {
        lazyInit();
        return delegate.getGenericsTypes();
    }

    @Override
    public void setGenericsTypes(GenericsType[] genericsTypes) {
        lazyInit();
        delegate.setGenericsTypes(genericsTypes);
    }

    @Override
    public boolean hasAnnotationDefault() {
        lazyInit();
        return delegate.hasAnnotationDefault();
    }

    @Override
    public void setAnnotationDefault(boolean hasDefaultValue) {
        lazyInit();
        delegate.setAnnotationDefault(hasDefaultValue);
    }

    @Override
    public boolean isScriptBody() {
        lazyInit();
        return delegate.isScriptBody();
    }

    @Override
    public void setIsScriptBody() {
        lazyInit();
        delegate.setIsScriptBody();
    }

    @Override
    public boolean isStaticConstructor() {
        lazyInit();
        return delegate.isStaticConstructor();
    }

    @Override
    public boolean isSyntheticPublic() {
        lazyInit();
        return delegate.isSyntheticPublic();
    }

    @Override
    public void setSyntheticPublic(boolean syntheticPublic) {
        lazyInit();
        delegate.setSyntheticPublic(syntheticPublic);
    }

    @Override
    public String getText() {
        lazyInit();
        return delegate.getText();
    }

    @Override
    public List<AnnotationNode> getAnnotations() {
        lazyInit();
        return delegate.getAnnotations();
    }

    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        lazyInit();
        return delegate.getAnnotations(type);
    }

    @Override
    public void addAnnotation(AnnotationNode annotation) {
        lazyInit();
        delegate.addAnnotation(annotation);
    }

    @Override
    public void addAnnotations(List<AnnotationNode> annotations) {
        lazyInit();
        delegate.addAnnotations(annotations);
    }

    @Override
    public ClassNode getDeclaringClass() {
        lazyInit();
        return delegate.getDeclaringClass();
    }

    @Override
    public void setDeclaringClass(ClassNode declaringClass) {
        super.setDeclaringClass(declaringClass);
    }

    @Override
    public Groovydoc getGroovydoc() {
        lazyInit();
        return delegate.getGroovydoc();
    }

    @Override
    public AnnotatedNode getInstance() {
        lazyInit();
        return delegate.getInstance();
    }

    @Override
    public boolean hasNoRealSourcePosition() {
        lazyInit();
        return delegate.hasNoRealSourcePosition();
    }

    @Override
    public void setHasNoRealSourcePosition(boolean hasNoRealSourcePosition) {
        lazyInit();
        delegate.setHasNoRealSourcePosition(hasNoRealSourcePosition);
    }

    @Override
    public boolean isSynthetic() {
        lazyInit();
        return delegate.isSynthetic();
    }

    @Override
    public void setSynthetic(boolean synthetic) {
        lazyInit();
        delegate.setSynthetic(synthetic);
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        lazyInit();
        delegate.visit(visitor);
    }

    @Override
    public int getLineNumber() {
        lazyInit();
        return delegate.getLineNumber();
    }

    @Override
    public void setLineNumber(int lineNumber) {
        lazyInit();
        delegate.setLineNumber(lineNumber);
    }

    @Override
    public int getColumnNumber() {
        lazyInit();
        return delegate.getColumnNumber();
    }

    @Override
    public void setColumnNumber(int columnNumber) {
        lazyInit();
        delegate.setColumnNumber(columnNumber);
    }

    @Override
    public int getLastLineNumber() {
        lazyInit();
        return delegate.getLastLineNumber();
    }

    @Override
    public void setLastLineNumber(int lastLineNumber) {
        lazyInit();
        delegate.setLastLineNumber(lastLineNumber);
    }

    @Override
    public int getLastColumnNumber() {
        lazyInit();
        return delegate.getLastColumnNumber();
    }

    @Override
    public void setLastColumnNumber(int lastColumnNumber) {
        lazyInit();
        delegate.setLastColumnNumber(lastColumnNumber);
    }

    @Override
    public void setSourcePosition(ASTNode node) {
        lazyInit();
        delegate.setSourcePosition(node);
    }

    @Override
    public void copyNodeMetaData(ASTNode other) {
        lazyInit();
        delegate.copyNodeMetaData(other);
    }

    @Override
    public Map<?, ?> getMetaDataMap() {
        lazyInit();
        return delegate.getMetaDataMap();
    }

    @Override
    public void setMetaDataMap(Map<?, ?> metaDataMap) {
        lazyInit();
        delegate.setMetaDataMap(metaDataMap);
    }

    @Override
    public int hashCode() {
        lazyInit();
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        lazyInit();
        return delegate.equals(obj);
    }

    @Override
    public String toString() {
        lazyInit();
        return delegate.toString();
    }

    @Override
    public <T> T getNodeMetaData(Object key) {
        lazyInit();
        return delegate.getNodeMetaData(key);
    }

    @Override
    public <T> T getNodeMetaData(Object key, Function<?, ? extends T> valFn) {
        lazyInit();
        return delegate.getNodeMetaData(key, valFn);
    }

    @Override
    public void copyNodeMetaData(NodeMetaDataHandler other) {
        lazyInit();
        delegate.copyNodeMetaData(other);
    }

    @Override
    public void setNodeMetaData(Object key, Object value) {
        lazyInit();
        delegate.setNodeMetaData(key, value);
    }

    @Override
    public Object putNodeMetaData(Object key, Object value) {
        lazyInit();
        return delegate.putNodeMetaData(key, value);
    }

    @Override
    public void removeNodeMetaData(Object key) {
        lazyInit();
        delegate.removeNodeMetaData(key);
    }

    @Override
    public Map<?, ?> getNodeMetaData() {
        lazyInit();
        return delegate.getNodeMetaData();
    }
}

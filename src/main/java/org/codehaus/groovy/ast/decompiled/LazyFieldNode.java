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
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.NodeMetaDataHandler;
import org.codehaus.groovy.ast.expr.Expression;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A lazy proxy for {@link FieldNode} that defers initialization until first access.
 *
 * <p>This class implements the lazy initialization pattern to optimize AST node creation when decompiling
 * bytecode. Rather than eagerly initializing all field metadata, this proxy defers instantiation of the
 * underlying delegate {@code FieldNode} until the first method invocation on the proxy. This is particularly
 * useful when processing large class files where not all fields may be accessed during compilation.
 *
 * <p><b>Thread Safety:</b> Initialization is protected by double-checked locking via the
 * {@link LazyInitializable#lazyInit()} method. The {@link #initialized} field is declared {@code volatile}
 * to prevent JVM instruction reordering during the check-then-act sequence.
 *
 * <p><b>Delegation Strategy:</b> Most method calls are forwarded to the delegate after lazy initialization.
 * The {@link #getName()} method short-circuits initialization by returning the cached field name, since this
 * value is available immediately without requiring full node initialization.
 *
 * @see LazyInitializable
 * @see DecompiledClassNode
 * @see org.codehaus.groovy.ast.FieldNode
 * @since 2.5.9
 */
class LazyFieldNode extends FieldNode implements LazyInitializable {
    /**
     * Supplier that produces the underlying {@code FieldNode} on first access.
     * This supplier is invoked exactly once during the {@link #doInit()} phase.
     */
    private final Supplier<FieldNode> fieldNodeSupplier;

    /**
     * The initialized delegate {@code FieldNode}. Initially {@code null} until
     * {@link #lazyInit()} triggers {@link #doInit()}.
     */
    private FieldNode delegate;

    /**
     * The field name, cached at construction time to allow {@link #getName()} to
     * avoid lazy initialization since field names are available without full decompilation.
     */
    private final String name;

    /**
     * Marks whether the delegate has been initialized. Declared {@code volatile}
     * to coordinate visibility across threads and prevent JVM reordering during
     * the double-checked locking pattern in {@link #lazyInit()}.
     */
    private volatile boolean initialized;

    /**
     * Constructs a lazy proxy for a field node.
     *
     * @param fieldNodeSupplier a supplier that produces the underlying {@code FieldNode}
     *                          on first access. Must not be {@code null}.
     * @param name              the field name, used for quick retrieval without initialization.
     *                          Must not be {@code null}.
     */
    public LazyFieldNode(Supplier<FieldNode> fieldNodeSupplier, String name) {
        this.fieldNodeSupplier = fieldNodeSupplier;
        this.name = name;
    }

    /**
     * Initializes the delegate field node on first access.
     *
     * <p>This method is called exactly once via the double-checked locking pattern in
     * {@link #lazyInit()}. It retrieves the delegate from the supplier and propagates
     * any metadata that was set on the proxy before initialization.
     *
     * @see LazyInitializable#doInit()
     */
    @Override
    public void doInit() {
        delegate = fieldNodeSupplier.get();

        ClassNode declaringClass = super.getDeclaringClass();
        if (null != declaringClass) delegate.setDeclaringClass(declaringClass);

        ClassNode owner = super.getOwner();
        if (null != owner) delegate.setOwner(owner);
    }

    /**
     * Returns whether this field node has been initialized.
     *
     * @return {@code true} if the delegate has been instantiated; {@code false} otherwise
     * @see LazyInitializable#isInitialized()
     */
    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Sets the initialization state of this field node.
     *
     * <p>This method is invoked by {@link #lazyInit()} after {@link #doInit()} completes.
     * Should not be called directly by client code.
     *
     * @param initialized {@code true} to mark the delegate as initialized
     * @see LazyInitializable#setInitialized(boolean)
     */
    @Override
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * Gets the initial expression for this field. Triggers lazy initialization.
     *
     * @return the initial expression, or {@code null} if not defined
     * @see FieldNode#getInitialExpression()
     */
    @Override
    public Expression getInitialExpression() {
        lazyInit();
        return delegate.getInitialExpression();
    }

    /**
     * Gets the access modifiers for this field. Triggers lazy initialization.
     *
     * @return a bitmask of the field's modifiers (e.g., {@code Modifier.PUBLIC})
     * @see FieldNode#getModifiers()
     */
    @Override
    public int getModifiers() {
        lazyInit();
        return delegate.getModifiers();
    }

    /**
     * Gets the field name without triggering lazy initialization.
     *
     * <p>This method returns the field name that was cached at construction time,
     * avoiding the overhead of full node initialization for a frequently-accessed property.
     *
     * @return the field name
     * @see FieldNode#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the declared type of this field. Triggers lazy initialization.
     *
     * @return the field's type as a {@code ClassNode}
     * @see FieldNode#getType()
     */
    @Override
    public ClassNode getType() {
        lazyInit();
        return delegate.getType();
    }

    /**
     * Sets the type of this field. Triggers lazy initialization.
     *
     * @param type the new type for this field
     * @see FieldNode#setType(ClassNode)
     */
    @Override
    public void setType(ClassNode type) {
        lazyInit();
        delegate.setType(type);
    }

    /**
     * Gets the owning class for this field. Triggers lazy initialization.
     *
     * @return the class that declares this field
     * @see FieldNode#getOwner()
     */
    @Override
    public ClassNode getOwner() {
        lazyInit();
        return delegate.getOwner();
    }

    /**
     * Checks if this field is a holder. Triggers lazy initialization.
     *
     * @return {@code true} if this field holds a value
     * @see FieldNode#isHolder()
     */
    @Override
    public boolean isHolder() {
        lazyInit();
        return delegate.isHolder();
    }

    /**
     * Sets whether this field is a holder. Triggers lazy initialization.
     *
     * @param holder {@code true} if this field should hold a value
     * @see FieldNode#setHolder(boolean)
     */
    @Override
    public void setHolder(boolean holder) {
        lazyInit();
        delegate.setHolder(holder);
    }

    /**
     * Checks if this field has a dynamic type. Triggers lazy initialization.
     *
     * @return {@code true} if the field type is determined at runtime
     * @see FieldNode#isDynamicTyped()
     */
    @Override
    public boolean isDynamicTyped() {
        lazyInit();
        return delegate.isDynamicTyped();
    }

    /**
     * Sets the access modifiers for this field. Triggers lazy initialization.
     *
     * @param modifiers the new modifier bitmask
     * @see FieldNode#setModifiers(int)
     */
    @Override
    public void setModifiers(int modifiers) {
        lazyInit();
        delegate.setModifiers(modifiers);
    }

    /**
     * Sets the owner of this field without triggering full initialization.
     *
     * <p>This setter is called before initialization to establish the declaring context.
     * The owner is propagated to the delegate during {@link #doInit()}.
     *
     * @param owner the new owner class
     * @see FieldNode#setOwner(ClassNode)
     */
    @Override
    public void setOwner(ClassNode owner) {
        super.setOwner(owner);
    }

    /**
     * Checks if this field has an initial value expression. Triggers lazy initialization.
     *
     * @return {@code true} if an initial expression is defined
     * @see FieldNode#hasInitialExpression()
     */
    @Override
    public boolean hasInitialExpression() {
        lazyInit();
        return delegate.hasInitialExpression();
    }

    /**
     * Checks if this field is in a static context. Triggers lazy initialization.
     *
     * @return {@code true} if this field is statically scoped
     * @see FieldNode#isInStaticContext()
     */
    @Override
    public boolean isInStaticContext() {
        lazyInit();
        return delegate.isInStaticContext();
    }

    /**
     * Gets the initial value expression for this field. Triggers lazy initialization.
     *
     * @return the initial value expression, or {@code null} if not defined
     * @see FieldNode#getInitialValueExpression()
     */
    @Override
    public Expression getInitialValueExpression() {
        lazyInit();
        return delegate.getInitialValueExpression();
    }

    /**
     * Sets the initial value expression for this field. Triggers lazy initialization.
     *
     * @param initialValueExpression the expression to initialize this field with
     * @see FieldNode#setInitialValueExpression(Expression)
     */
    @Override
    public void setInitialValueExpression(Expression initialValueExpression) {
        lazyInit();
        delegate.setInitialValueExpression(initialValueExpression);
    }

    /**
     * Gets the original type of this field before any transformations. Triggers lazy initialization.
     *
     * @return the original, untransformed field type
     * @see FieldNode#getOriginType()
     */
    @Override
    public ClassNode getOriginType() {
        lazyInit();
        return delegate.getOriginType();
    }

    /**
     * Sets the original type of this field. Triggers lazy initialization.
     *
     * @param cn the original type before transformation
     * @see FieldNode#setOriginType(ClassNode)
     */
    @Override
    public void setOriginType(ClassNode cn) {
        lazyInit();
        delegate.setOriginType(cn);
    }

    /**
     * Renames this field. Triggers lazy initialization.
     *
     * @param name the new field name
     * @see FieldNode#rename(String)
     */
    @Override
    public void rename(String name) {
        lazyInit();
        delegate.rename(name);
    }

    /**
     * Gets all annotations on this field. Triggers lazy initialization.
     *
     * @return a list of {@code AnnotationNode} objects; never {@code null}
     * @see FieldNode#getAnnotations()
     */
    @Override
    public List<AnnotationNode> getAnnotations() {
        lazyInit();
        return delegate.getAnnotations();
    }

    /**
     * Gets all annotations of a specific type on this field. Triggers lazy initialization.
     *
     * @param type the annotation type to query
     * @return a list of matching {@code AnnotationNode} objects
     * @see FieldNode#getAnnotations(ClassNode)
     */
    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        lazyInit();
        return delegate.getAnnotations(type);
    }

    /**
     * Adds an annotation to this field. Triggers lazy initialization.
     *
     * @param annotation the annotation to add
     * @see FieldNode#addAnnotation(AnnotationNode)
     */
    @Override
    public void addAnnotation(AnnotationNode annotation) {
        lazyInit();
        delegate.addAnnotation(annotation);
    }

    /**
     * Adds multiple annotations to this field. Triggers lazy initialization.
     *
     * @param annotations the annotations to add
     * @see FieldNode#addAnnotations(List)
     */
    @Override
    public void addAnnotations(List<AnnotationNode> annotations) {
        lazyInit();
        delegate.addAnnotations(annotations);
    }

    /**
     * Gets the class that declares this field. Triggers lazy initialization.
     *
     * @return the declaring class
     * @see FieldNode#getDeclaringClass()
     */
    @Override
    public ClassNode getDeclaringClass() {
        lazyInit();
        return delegate.getDeclaringClass();
    }

    /**
     * Sets the class that declares this field without triggering full initialization.
     *
     * <p>This setter is called before initialization to establish the declaring context.
     * The declaring class is propagated to the delegate during {@link #doInit()}.
     *
     * @param declaringClass the class declaring this field
     * @see FieldNode#setDeclaringClass(ClassNode)
     */
    @Override
    public void setDeclaringClass(ClassNode declaringClass) {
        super.setDeclaringClass(declaringClass);
    }

    /**
     * Gets the Groovydoc comment for this field. Triggers lazy initialization.
     *
     * @return the Groovydoc, or {@code null} if not available
     * @see FieldNode#getGroovydoc()
     */
    @Override
    public Groovydoc getGroovydoc() {
        lazyInit();
        return delegate.getGroovydoc();
    }

    /**
     * Gets the source instance for this annotated node. Triggers lazy initialization.
     *
     * @return the source instance
     * @see FieldNode#getInstance()
     */
    @Override
    public AnnotatedNode getInstance() {
        lazyInit();
        return delegate.getInstance();
    }

    /**
     * Checks if this field has no real source position. Triggers lazy initialization.
     *
     * @return {@code true} if source position information is unavailable
     * @see FieldNode#hasNoRealSourcePosition()
     */
    @Override
    public boolean hasNoRealSourcePosition() {
        lazyInit();
        return delegate.hasNoRealSourcePosition();
    }

    /**
     * Sets whether this field has no real source position. Triggers lazy initialization.
     *
     * @param hasNoRealSourcePosition {@code true} if source position is unavailable
     * @see FieldNode#setHasNoRealSourcePosition(boolean)
     */
    @Override
    public void setHasNoRealSourcePosition(boolean hasNoRealSourcePosition) {
        lazyInit();
        delegate.setHasNoRealSourcePosition(hasNoRealSourcePosition);
    }

    /**
     * Checks if this field is synthetic. Triggers lazy initialization.
     *
     * @return {@code true} if this field was generated by the compiler
     * @see FieldNode#isSynthetic()
     */
    @Override
    public boolean isSynthetic() {
        lazyInit();
        return delegate.isSynthetic();
    }

    /**
     * Sets whether this field is synthetic. Triggers lazy initialization.
     *
     * @param synthetic {@code true} if this field is compiler-generated
     * @see FieldNode#setSynthetic(boolean)
     */
    @Override
    public void setSynthetic(boolean synthetic) {
        lazyInit();
        delegate.setSynthetic(synthetic);
    }

    /**
     * Accepts a visitor for tree traversal. Triggers lazy initialization.
     *
     * @param visitor the code visitor to dispatch to
     * @see FieldNode#visit(GroovyCodeVisitor)
     */
    @Override
    public void visit(GroovyCodeVisitor visitor) {
        lazyInit();
        delegate.visit(visitor);
    }

    /**
     * Gets the source code text for this field. Triggers lazy initialization.
     *
     * @return the source text representation
     * @see FieldNode#getText()
     */
    @Override
    public String getText() {
        lazyInit();
        return delegate.getText();
    }

    /**
     * Gets the line number where this field is defined in source. Triggers lazy initialization.
     *
     * @return the source line number
     * @see FieldNode#getLineNumber()
     */
    @Override
    public int getLineNumber() {
        lazyInit();
        return delegate.getLineNumber();
    }

    /**
     * Sets the line number for this field. Triggers lazy initialization.
     *
     * @param lineNumber the source line number
     * @see FieldNode#setLineNumber(int)
     */
    @Override
    public void setLineNumber(int lineNumber) {
        lazyInit();
        delegate.setLineNumber(lineNumber);
    }

    /**
     * Gets the column number where this field is defined in source. Triggers lazy initialization.
     *
     * @return the source column number
     * @see FieldNode#getColumnNumber()
     */
    @Override
    public int getColumnNumber() {
        lazyInit();
        return delegate.getColumnNumber();
    }

    /**
     * Sets the column number for this field. Triggers lazy initialization.
     *
     * @param columnNumber the source column number
     * @see FieldNode#setColumnNumber(int)
     */
    @Override
    public void setColumnNumber(int columnNumber) {
        lazyInit();
        delegate.setColumnNumber(columnNumber);
    }

    /**
     * Gets the last line number for this field in source. Triggers lazy initialization.
     *
     * @return the last source line number
     * @see FieldNode#getLastLineNumber()
     */
    @Override
    public int getLastLineNumber() {
        lazyInit();
        return delegate.getLastLineNumber();
    }

    /**
     * Sets the last line number for this field. Triggers lazy initialization.
     *
     * @param lastLineNumber the last source line number
     * @see FieldNode#setLastLineNumber(int)
     */
    @Override
    public void setLastLineNumber(int lastLineNumber) {
        lazyInit();
        delegate.setLastLineNumber(lastLineNumber);
    }

    /**
     * Gets the last column number for this field in source. Triggers lazy initialization.
     *
     * @return the last source column number
     * @see FieldNode#getLastColumnNumber()
     */
    @Override
    public int getLastColumnNumber() {
        lazyInit();
        return delegate.getLastColumnNumber();
    }

    /**
     * Sets the last column number for this field. Triggers lazy initialization.
     *
     * @param lastColumnNumber the last source column number
     * @see FieldNode#setLastColumnNumber(int)
     */
    @Override
    public void setLastColumnNumber(int lastColumnNumber) {
        lazyInit();
        delegate.setLastColumnNumber(lastColumnNumber);
    }

    /**
     * Copies the source position from another AST node to this field. Triggers lazy initialization.
     *
     * @param node the source node to copy position from
     * @see FieldNode#setSourcePosition(ASTNode)
     */
    @Override
    public void setSourcePosition(ASTNode node) {
        lazyInit();
        delegate.setSourcePosition(node);
    }

    /**
     * Copies node metadata from another AST node. Triggers lazy initialization.
     *
     * @param other the node to copy metadata from
     * @see FieldNode#copyNodeMetaData(ASTNode)
     */
    @Override
    public void copyNodeMetaData(ASTNode other) {
        lazyInit();
        delegate.copyNodeMetaData(other);
    }

    /**
     * Gets the metadata map for this field. Triggers lazy initialization.
     *
     * @return a map of node metadata
     * @see FieldNode#getMetaDataMap()
     */
    @Override
    public Map<?, ?> getMetaDataMap() {
        lazyInit();
        return delegate.getMetaDataMap();
    }

    /**
     * Sets the metadata map for this field. Triggers lazy initialization.
     *
     * @param metaDataMap the new metadata map
     * @see FieldNode#setMetaDataMap(Map)
     */
    @Override
    public void setMetaDataMap(Map<?, ?> metaDataMap) {
        lazyInit();
        delegate.setMetaDataMap(metaDataMap);
    }

    /**
     * Computes the hash code for this field. Triggers lazy initialization.
     *
     * @return the hash code of the delegate
     * @see FieldNode#hashCode()
     */
    @Override
    public int hashCode() {
        lazyInit();
        return delegate.hashCode();
    }

    /**
     * Checks equality with another object with special delegation logic.
     *
     * <p>If the other object is this proxy instance, returns {@code true} immediately without
     * initialization. Otherwise, triggers lazy initialization and delegates to the underlying
     * field node for comparison.
     *
     * @param obj the object to compare with
     * @return {@code true} if equal to the delegate
     * @see FieldNode#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        lazyInit();
        return delegate.equals(obj);
    }

    /**
     * Gets the string representation of this field. Triggers lazy initialization.
     *
     * @return the string representation of the delegate
     * @see FieldNode#toString()
     */
    @Override
    public String toString() {
        lazyInit();
        return delegate.toString();
    }

    /**
     * Gets node metadata by key. Triggers lazy initialization.
     *
     * @param key the metadata key
     * @param <T> the expected value type
     * @return the metadata value, or {@code null} if not found
     * @see FieldNode#getNodeMetaData(Object)
     */
    @Override
    public <T> T getNodeMetaData(Object key) {
        lazyInit();
        return delegate.getNodeMetaData(key);
    }

    /**
     * Gets node metadata by key with a value function fallback. Triggers lazy initialization.
     *
     * @param key the metadata key
     * @param valFn the function to compute a value if the key is not found
     * @param <T> the expected value type
     * @return the metadata value or the computed value
     * @see FieldNode#getNodeMetaData(Object, Function)
     */
    @Override
    public <T> T getNodeMetaData(Object key, Function<?, ? extends T> valFn) {
        lazyInit();
        return delegate.getNodeMetaData(key, valFn);
    }

    /**
     * Copies node metadata from another node. Triggers lazy initialization.
     *
     * @param other the metadata source
     * @see FieldNode#copyNodeMetaData(NodeMetaDataHandler)
     */
    @Override
    public void copyNodeMetaData(NodeMetaDataHandler other) {
        lazyInit();
        delegate.copyNodeMetaData(other);
    }

    /**
     * Sets node metadata by key. Triggers lazy initialization.
     *
     * @param key the metadata key
     * @param value the metadata value
     * @see FieldNode#setNodeMetaData(Object, Object)
     */
    @Override
    public void setNodeMetaData(Object key, Object value) {
        lazyInit();
        delegate.setNodeMetaData(key, value);
    }

    /**
     * Puts node metadata by key, returning the previous value. Triggers lazy initialization.
     *
     * @param key the metadata key
     * @param value the new metadata value
     * @return the previous value associated with the key, or {@code null}
     * @see FieldNode#putNodeMetaData(Object, Object)
     */
    @Override
    public Object putNodeMetaData(Object key, Object value) {
        lazyInit();
        return delegate.putNodeMetaData(key, value);
    }

    /**
     * Removes node metadata by key. Triggers lazy initialization.
     *
     * @param key the metadata key to remove
     * @see FieldNode#removeNodeMetaData(Object)
     */
    @Override
    public void removeNodeMetaData(Object key) {
        lazyInit();
        delegate.removeNodeMetaData(key);
    }

    /**
     * Gets all node metadata. Triggers lazy initialization.
     *
     * @return a map of all metadata
     * @see FieldNode#getNodeMetaData()
     */
    @Override
    public Map<?, ?> getNodeMetaData() {
        lazyInit();
        return delegate.getNodeMetaData();
    }
}

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
 * A lazy proxy for {@link MethodNode} that defers initialization until first access.
 *
 * <p>This class implements the lazy initialization pattern to optimize AST node creation when decompiling
 * bytecode. Rather than eagerly initializing all method metadata, this proxy defers instantiation of the
 * underlying delegate {@code MethodNode} until the first method invocation on the proxy. This is particularly
 * useful when processing large class files where not all methods may be accessed during compilation.
 *
 * <p><b>Thread Safety:</b> Initialization is protected by double-checked locking via the
 * {@link LazyInitializable#lazyInit()} method. The {@link #initialized} field is declared {@code volatile}
 * to prevent JVM instruction reordering during the check-then-act sequence.
 *
 * <p><b>Delegation Strategy:</b> Most method calls are forwarded to the delegate after lazy initialization.
 * The {@link #getName()} method short-circuits initialization by returning the cached method name, since this
 * value is available immediately without requiring full node initialization.
 *
 * @see LazyInitializable
 * @see DecompiledClassNode
 * @see org.codehaus.groovy.ast.MethodNode
 * @since 2.5.9
 */
class LazyMethodNode extends MethodNode implements LazyInitializable {
    /**
     * Supplier that produces the underlying {@code MethodNode} on first access.
     * This supplier is invoked exactly once during the {@link #doInit()} phase.
     */
    private final Supplier<MethodNode> methodNodeSupplier;

    /**
     * The initialized delegate {@code MethodNode}. Initially {@code null} until
     * {@link #lazyInit()} triggers {@link #doInit()}.
     */
    private MethodNode delegate;

    /**
     * The method name, cached at construction time to allow {@link #getName()} to
     * avoid lazy initialization since method names are available without full decompilation.
     */
    private final String name;

    /**
     * Marks whether the delegate has been initialized. Declared {@code volatile}
     * to coordinate visibility across threads and prevent JVM reordering during
     * the double-checked locking pattern in {@link #lazyInit()}.
     */
    private volatile boolean initialized;

    /**
     * Constructs a lazy proxy for a method node.
     *
     * @param methodNodeSupplier a supplier that produces the underlying {@code MethodNode}
     *                           on first access. Must not be {@code null}.
     * @param name               the method name, used for quick retrieval without initialization.
     *                           Must not be {@code null}.
     */
    public LazyMethodNode(Supplier<MethodNode> methodNodeSupplier, String name) {
        this.methodNodeSupplier = methodNodeSupplier;
        this.name = name;
    }

    /**
     * Initializes the delegate method node on first access.
     *
     * <p>This method is called exactly once via the double-checked locking pattern in
     * {@link #lazyInit()}. It retrieves the delegate from the supplier and propagates
     * any metadata that was set on the proxy before initialization.
     *
     * @see LazyInitializable#doInit()
     */
    @Override
    public void doInit() {
        delegate = methodNodeSupplier.get();

        ClassNode declaringClass = super.getDeclaringClass();
        if (null != declaringClass) delegate.setDeclaringClass(declaringClass);
    }

    /**
     * Returns whether this method node has been initialized.
     *
     * @return {@code true} if the delegate has been instantiated; {@code false} otherwise
     * @see LazyInitializable#isInitialized()
     */
    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Sets the initialization state of this method node.
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
     * Gets the type descriptor for this method. Triggers lazy initialization.
     *
     * @return the method's type descriptor string
     * @see MethodNode#getTypeDescriptor()
     */
    @Override
    public String getTypeDescriptor() {
        lazyInit();
        return delegate.getTypeDescriptor();
    }

    /**
     * Gets the method body code. Triggers lazy initialization.
     *
     * @return the method's statement block, or {@code null} if abstract/native
     * @see MethodNode#getCode()
     */
    @Override
    public Statement getCode() {
        lazyInit();
        return delegate.getCode();
    }

    /**
     * Sets the method body code. Triggers lazy initialization.
     *
     * @param code the statement block for this method
     * @see MethodNode#setCode(Statement)
     */
    @Override
    public void setCode(Statement code) {
        lazyInit();
        delegate.setCode(code);
    }

    /**
     * Gets the access modifiers for this method. Triggers lazy initialization.
     *
     * @return a bitmask of the method's modifiers (e.g., {@code Modifier.PUBLIC})
     * @see MethodNode#getModifiers()
     */
    @Override
    public int getModifiers() {
        lazyInit();
        return delegate.getModifiers();
    }

    /**
     * Sets the access modifiers for this method. Triggers lazy initialization.
     *
     * @param modifiers the new modifier bitmask
     * @see MethodNode#setModifiers(int)
     */
    @Override
    public void setModifiers(int modifiers) {
        lazyInit();
        delegate.setModifiers(modifiers);
    }

    /**
     * Gets the method name without triggering lazy initialization.
     *
     * <p>This method returns the method name that was cached at construction time,
     * avoiding the overhead of full node initialization for a frequently-accessed property.
     *
     * @return the method name
     * @see MethodNode#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the parameters for this method. Triggers lazy initialization.
     *
     * @return an array of parameter declarations
     * @see MethodNode#getParameters()
     */
    @Override
    public Parameter[] getParameters() {
        lazyInit();
        return delegate.getParameters();
    }

    /**
     * Sets the parameters for this method. Triggers lazy initialization.
     *
     * @param parameters the new parameter declarations
     * @see MethodNode#setParameters(Parameter[])
     */
    @Override
    public void setParameters(Parameter[] parameters) {
        lazyInit();
        delegate.setParameters(parameters);
    }

    /**
     * Checks if this method has default parameter values. Triggers lazy initialization.
     *
     * @return {@code true} if at least one parameter has a default value
     * @see MethodNode#hasDefaultValue()
     */
    @Override
    public boolean hasDefaultValue() {
        lazyInit();
        return delegate.hasDefaultValue();
    }

    /**
     * Gets the return type for this method. Triggers lazy initialization.
     *
     * @return the method's return type as a {@code ClassNode}
     * @see MethodNode#getReturnType()
     */
    @Override
    public ClassNode getReturnType() {
        lazyInit();
        return delegate.getReturnType();
    }

    /**
     * Sets the return type for this method. Triggers lazy initialization.
     *
     * @param returnType the new return type
     * @see MethodNode#setReturnType(ClassNode)
     */
    @Override
    public void setReturnType(ClassNode returnType) {
        lazyInit();
        delegate.setReturnType(returnType);
    }

    /**
     * Checks if this method has a dynamic return type. Triggers lazy initialization.
     *
     * @return {@code true} if the return type is determined at runtime
     * @see MethodNode#isDynamicReturnType()
     */
    @Override
    public boolean isDynamicReturnType() {
        lazyInit();
        return delegate.isDynamicReturnType();
    }

    /**
     * Checks if this method has a void return type. Triggers lazy initialization.
     *
     * @return {@code true} if this method returns void
     * @see MethodNode#isVoidMethod()
     */
    @Override
    public boolean isVoidMethod() {
        lazyInit();
        return delegate.isVoidMethod();
    }

    /**
     * Gets the variable scope for this method. Triggers lazy initialization.
     *
     * @return the method's variable scope
     * @see MethodNode#getVariableScope()
     */
    @Override
    public VariableScope getVariableScope() {
        lazyInit();
        return delegate.getVariableScope();
    }

    /**
     * Sets the variable scope for this method. Triggers lazy initialization.
     *
     * @param variableScope the new variable scope
     * @see MethodNode#setVariableScope(VariableScope)
     */
    @Override
    public void setVariableScope(VariableScope variableScope) {
        lazyInit();
        delegate.setVariableScope(variableScope);
    }

    /**
     * Checks if this method is abstract. Triggers lazy initialization.
     *
     * @return {@code true} if this method has no implementation
     * @see MethodNode#isAbstract()
     */
    @Override
    public boolean isAbstract() {
        lazyInit();
        return delegate.isAbstract();
    }

    /**
     * Checks if this is a default interface method. Triggers lazy initialization.
     *
     * @return {@code true} if this is a default method on an interface
     * @see MethodNode#isDefault()
     */
    @Override
    public boolean isDefault() {
        lazyInit();
        return delegate.isDefault();
    }

    /**
     * Checks if this method is final. Triggers lazy initialization.
     *
     * @return {@code true} if this method cannot be overridden
     * @see MethodNode#isFinal()
     */
    @Override
    public boolean isFinal() {
        lazyInit();
        return delegate.isFinal();
    }

    /**
     * Checks if this method is static. Triggers lazy initialization.
     *
     * @return {@code true} if this is a class-level method
     * @see MethodNode#isStatic()
     */
    @Override
    public boolean isStatic() {
        lazyInit();
        return delegate.isStatic();
    }

    /**
     * Checks if this method is public. Triggers lazy initialization.
     *
     * @return {@code true} if this method is publicly accessible
     * @see MethodNode#isPublic()
     */
    @Override
    public boolean isPublic() {
        lazyInit();
        return delegate.isPublic();
    }

    /**
     * Checks if this method is private. Triggers lazy initialization.
     *
     * @return {@code true} if this method is accessible only within its class
     * @see MethodNode#isPrivate()
     */
    @Override
    public boolean isPrivate() {
        lazyInit();
        return delegate.isPrivate();
    }

    /**
     * Checks if this method is protected. Triggers lazy initialization.
     *
     * @return {@code true} if this method is protected from external access
     * @see MethodNode#isProtected()
     */
    @Override
    public boolean isProtected() {
        lazyInit();
        return delegate.isProtected();
    }

    /**
     * Checks if this method is package-scoped. Triggers lazy initialization.
     *
     * @return {@code true} if this method has package scope
     * @see MethodNode#isPackageScope()
     */
    @Override
    public boolean isPackageScope() {
        lazyInit();
        return delegate.isPackageScope();
    }

    /**
     * Gets the exception types declared by this method. Triggers lazy initialization.
     *
     * @return an array of checked exception types
     * @see MethodNode#getExceptions()
     */
    @Override
    public ClassNode[] getExceptions() {
        lazyInit();
        return delegate.getExceptions();
    }

    /**
     * Gets the first statement in this method body. Triggers lazy initialization.
     *
     * @return the first statement, or {@code null} if the method is empty or abstract
     * @see MethodNode#getFirstStatement()
     */
    @Override
    public Statement getFirstStatement() {
        lazyInit();
        return delegate.getFirstStatement();
    }

    /**
     * Gets the generic type parameters for this method. Triggers lazy initialization.
     *
     * @return an array of generic type specifications
     * @see MethodNode#getGenericsTypes()
     */
    @Override
    public GenericsType[] getGenericsTypes() {
        lazyInit();
        return delegate.getGenericsTypes();
    }

    /**
     * Sets the generic type parameters for this method. Triggers lazy initialization.
     *
     * @param genericsTypes the new generic type specifications
     * @see MethodNode#setGenericsTypes(GenericsType[])
     */
    @Override
    public void setGenericsTypes(GenericsType[] genericsTypes) {
        lazyInit();
        delegate.setGenericsTypes(genericsTypes);
    }

    /**
     * Checks if this method has an annotation default value. Triggers lazy initialization.
     *
     * @return {@code true} if this method is an annotation member with a default
     * @see MethodNode#hasAnnotationDefault()
     */
    @Override
    public boolean hasAnnotationDefault() {
        lazyInit();
        return delegate.hasAnnotationDefault();
    }

    /**
     * Sets whether this method has an annotation default value. Triggers lazy initialization.
     *
     * @param hasDefaultValue {@code true} if this annotation member has a default
     * @see MethodNode#setAnnotationDefault(boolean)
     */
    @Override
    public void setAnnotationDefault(boolean hasDefaultValue) {
        lazyInit();
        delegate.setAnnotationDefault(hasDefaultValue);
    }

    /**
     * Checks if this method is a script body method. Triggers lazy initialization.
     *
     * @return {@code true} if this method represents the body of a Groovy script
     * @see MethodNode#isScriptBody()
     */
    @Override
    public boolean isScriptBody() {
        lazyInit();
        return delegate.isScriptBody();
    }

    /**
     * Marks this method as a script body method. Triggers lazy initialization.
     *
     * @see MethodNode#setIsScriptBody()
     */
    @Override
    public void setIsScriptBody() {
        lazyInit();
        delegate.setIsScriptBody();
    }

    /**
     * Checks if this is a static constructor (&lt;clinit&gt;). Triggers lazy initialization.
     *
     * @return {@code true} if this method initializes static fields
     * @see MethodNode#isStaticConstructor()
     */
    @Override
    public boolean isStaticConstructor() {
        lazyInit();
        return delegate.isStaticConstructor();
    }

    /**
     * Checks if this method is synthetically public. Triggers lazy initialization.
     *
     * @return {@code true} if this method was made public by the compiler
     * @see MethodNode#isSyntheticPublic()
     */
    @Override
    public boolean isSyntheticPublic() {
        lazyInit();
        return delegate.isSyntheticPublic();
    }

    /**
     * Sets whether this method is synthetically public. Triggers lazy initialization.
     *
     * @param syntheticPublic {@code true} if this method is compiler-made public
     * @see MethodNode#setSyntheticPublic(boolean)
     */
    @Override
    public void setSyntheticPublic(boolean syntheticPublic) {
        lazyInit();
        delegate.setSyntheticPublic(syntheticPublic);
    }

    /**
     * Gets the source code text for this method. Triggers lazy initialization.
     *
     * @return the source text representation
     * @see MethodNode#getText()
     */
    @Override
    public String getText() {
        lazyInit();
        return delegate.getText();
    }

    /**
     * Gets all annotations on this method. Triggers lazy initialization.
     *
     * @return a list of {@code AnnotationNode} objects; never {@code null}
     * @see MethodNode#getAnnotations()
     */
    @Override
    public List<AnnotationNode> getAnnotations() {
        lazyInit();
        return delegate.getAnnotations();
    }

    /**
     * Gets all annotations of a specific type on this method. Triggers lazy initialization.
     *
     * @param type the annotation type to query
     * @return a list of matching {@code AnnotationNode} objects
     * @see MethodNode#getAnnotations(ClassNode)
     */
    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        lazyInit();
        return delegate.getAnnotations(type);
    }

    /**
     * Adds an annotation to this method. Triggers lazy initialization.
     *
     * @param annotation the annotation to add
     * @see MethodNode#addAnnotation(AnnotationNode)
     */
    @Override
    public void addAnnotation(AnnotationNode annotation) {
        lazyInit();
        delegate.addAnnotation(annotation);
    }

    /**
     * Adds multiple annotations to this method. Triggers lazy initialization.
     *
     * @param annotations the annotations to add
     * @see MethodNode#addAnnotations(List)
     */
    @Override
    public void addAnnotations(List<AnnotationNode> annotations) {
        lazyInit();
        delegate.addAnnotations(annotations);
    }

    /**
     * Gets the class that declares this method. Triggers lazy initialization.
     *
     * @return the declaring class
     * @see MethodNode#getDeclaringClass()
     */
    @Override
    public ClassNode getDeclaringClass() {
        lazyInit();
        return delegate.getDeclaringClass();
    }

    /**
     * Sets the class that declares this method without triggering full initialization.
     *
     * <p>This setter is called before initialization to establish the declaring context.
     * The declaring class is propagated to the delegate during {@link #doInit()}.
     *
     * @param declaringClass the class declaring this method
     * @see MethodNode#setDeclaringClass(ClassNode)
     */
    @Override
    public void setDeclaringClass(ClassNode declaringClass) {
        super.setDeclaringClass(declaringClass);
    }

    /**
     * Gets the Groovydoc comment for this method. Triggers lazy initialization.
     *
     * @return the Groovydoc, or {@code null} if not available
     * @see MethodNode#getGroovydoc()
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
     * @see MethodNode#getInstance()
     */
    @Override
    public AnnotatedNode getInstance() {
        lazyInit();
        return delegate.getInstance();
    }

    /**
     * Checks if this method has no real source position. Triggers lazy initialization.
     *
     * @return {@code true} if source position information is unavailable
     * @see MethodNode#hasNoRealSourcePosition()
     */
    @Override
    public boolean hasNoRealSourcePosition() {
        lazyInit();
        return delegate.hasNoRealSourcePosition();
    }

    /**
     * Sets whether this method has no real source position. Triggers lazy initialization.
     *
     * @param hasNoRealSourcePosition {@code true} if source position is unavailable
     * @see MethodNode#setHasNoRealSourcePosition(boolean)
     */
    @Override
    public void setHasNoRealSourcePosition(boolean hasNoRealSourcePosition) {
        lazyInit();
        delegate.setHasNoRealSourcePosition(hasNoRealSourcePosition);
    }

    /**
     * Checks if this method is synthetic. Triggers lazy initialization.
     *
     * @return {@code true} if this method was generated by the compiler
     * @see MethodNode#isSynthetic()
     */
    @Override
    public boolean isSynthetic() {
        lazyInit();
        return delegate.isSynthetic();
    }

    /**
     * Sets whether this method is synthetic. Triggers lazy initialization.
     *
     * @param synthetic {@code true} if this method is compiler-generated
     * @see MethodNode#setSynthetic(boolean)
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
     * @see MethodNode#visit(GroovyCodeVisitor)
     */
    @Override
    public void visit(GroovyCodeVisitor visitor) {
        lazyInit();
        delegate.visit(visitor);
    }

    /**
     * Gets the line number where this method is defined in source. Triggers lazy initialization.
     *
     * @return the source line number
     * @see MethodNode#getLineNumber()
     */
    @Override
    public int getLineNumber() {
        lazyInit();
        return delegate.getLineNumber();
    }

    /**
     * Sets the line number for this method. Triggers lazy initialization.
     *
     * @param lineNumber the source line number
     * @see MethodNode#setLineNumber(int)
     */
    @Override
    public void setLineNumber(int lineNumber) {
        lazyInit();
        delegate.setLineNumber(lineNumber);
    }

    /**
     * Gets the column number where this method is defined in source. Triggers lazy initialization.
     *
     * @return the source column number
     * @see MethodNode#getColumnNumber()
     */
    @Override
    public int getColumnNumber() {
        lazyInit();
        return delegate.getColumnNumber();
    }

    /**
     * Sets the column number for this method. Triggers lazy initialization.
     *
     * @param columnNumber the source column number
     * @see MethodNode#setColumnNumber(int)
     */
    @Override
    public void setColumnNumber(int columnNumber) {
        lazyInit();
        delegate.setColumnNumber(columnNumber);
    }

    /**
     * Gets the last line number for this method in source. Triggers lazy initialization.
     *
     * @return the last source line number
     * @see MethodNode#getLastLineNumber()
     */
    @Override
    public int getLastLineNumber() {
        lazyInit();
        return delegate.getLastLineNumber();
    }

    /**
     * Sets the last line number for this method. Triggers lazy initialization.
     *
     * @param lastLineNumber the last source line number
     * @see MethodNode#setLastLineNumber(int)
     */
    @Override
    public void setLastLineNumber(int lastLineNumber) {
        lazyInit();
        delegate.setLastLineNumber(lastLineNumber);
    }

    /**
     * Gets the last column number for this method in source. Triggers lazy initialization.
     *
     * @return the last source column number
     * @see MethodNode#getLastColumnNumber()
     */
    @Override
    public int getLastColumnNumber() {
        lazyInit();
        return delegate.getLastColumnNumber();
    }

    /**
     * Sets the last column number for this method. Triggers lazy initialization.
     *
     * @param lastColumnNumber the last source column number
     * @see MethodNode#setLastColumnNumber(int)
     */
    @Override
    public void setLastColumnNumber(int lastColumnNumber) {
        lazyInit();
        delegate.setLastColumnNumber(lastColumnNumber);
    }

    /**
     * Copies the source position from another AST node to this method. Triggers lazy initialization.
     *
     * @param node the source node to copy position from
     * @see MethodNode#setSourcePosition(ASTNode)
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
     * @see MethodNode#copyNodeMetaData(ASTNode)
     */
    @Override
    public void copyNodeMetaData(ASTNode other) {
        lazyInit();
        delegate.copyNodeMetaData(other);
    }

    /**
     * Gets the metadata map for this method. Triggers lazy initialization.
     *
     * @return a map of node metadata
     * @see MethodNode#getMetaDataMap()
     */
    @Override
    public Map<?, ?> getMetaDataMap() {
        lazyInit();
        return delegate.getMetaDataMap();
    }

    /**
     * Sets the metadata map for this method. Triggers lazy initialization.
     *
     * @param metaDataMap the new metadata map
     * @see MethodNode#setMetaDataMap(Map)
     */
    @Override
    public void setMetaDataMap(Map<?, ?> metaDataMap) {
        lazyInit();
        delegate.setMetaDataMap(metaDataMap);
    }

    /**
     * Computes the hash code for this method. Triggers lazy initialization.
     *
     * @return the hash code of the delegate
     * @see MethodNode#hashCode()
     */
    @Override
    public int hashCode() {
        lazyInit();
        return delegate.hashCode();
    }

    /**
     * Checks equality with another object, delegating to the underlying method node.
     *
     * <p>Triggers lazy initialization before delegating the comparison to the underlying method.
     *
     * @param obj the object to compare with
     * @return {@code true} if equal to the delegate
     * @see MethodNode#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        lazyInit();
        return delegate.equals(obj);
    }

    /**
     * Gets the string representation of this method. Triggers lazy initialization.
     *
     * @return the string representation of the delegate
     * @see MethodNode#toString()
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
     * @see MethodNode#getNodeMetaData(Object)
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
     * @see MethodNode#getNodeMetaData(Object, Function)
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
     * @see MethodNode#copyNodeMetaData(NodeMetaDataHandler)
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
     * @see MethodNode#setNodeMetaData(Object, Object)
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
     * @see MethodNode#putNodeMetaData(Object, Object)
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
     * @see MethodNode#removeNodeMetaData(Object)
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
     * @see MethodNode#getNodeMetaData()
     */
    @Override
    public Map<?, ?> getNodeMetaData() {
        lazyInit();
        return delegate.getNodeMetaData();
    }
}

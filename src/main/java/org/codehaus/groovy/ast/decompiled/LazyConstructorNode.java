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
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.NodeMetaDataHandler;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A lazy proxy for {@link ConstructorNode} that defers initialization until first access.
 *
 * <p>This class implements the lazy initialization pattern to optimize AST node creation when decompiling
 * bytecode. Rather than eagerly initializing all constructor metadata, this proxy defers instantiation of the
 * underlying delegate {@code ConstructorNode} until the first method invocation on the proxy. This is particularly
 * useful when processing large class files where constructors may not be accessed during compilation.
 *
 * <p><b>Thread Safety:</b> Initialization is protected by double-checked locking via the
 * {@link LazyInitializable#lazyInit()} method. The {@link #initialized} field is declared {@code volatile}
 * to prevent JVM instruction reordering during the check-then-act sequence.
 *
 * <p><b>Delegation Strategy:</b> All method calls are forwarded to the delegate after lazy initialization.
 * Unlike field and method nodes, constructor nodes do not cache a name separately since constructor names
 * are generally meaningful only after full decompilation.
 *
 * @see LazyInitializable
 * @see DecompiledClassNode
 * @see org.codehaus.groovy.ast.ConstructorNode
 * @since 2.5.9
 */
class LazyConstructorNode extends ConstructorNode implements LazyInitializable {
    /**
     * Supplier that produces the underlying {@code ConstructorNode} on first access.
     * This supplier is invoked exactly once during the {@link #doInit()} phase.
     */
    private final Supplier<ConstructorNode> constructorNodeSupplier;

    /**
     * The initialized delegate {@code ConstructorNode}. Initially {@code null} until
     * {@link #lazyInit()} triggers {@link #doInit()}.
     */
    private ConstructorNode delegate;

    /**
     * Marks whether the delegate has been initialized. Declared {@code volatile}
     * to coordinate visibility across threads and prevent JVM reordering during
     * the double-checked locking pattern in {@link #lazyInit()}.
     */
    private volatile boolean initialized;

    /**
     * Constructs a lazy proxy for a constructor node.
     *
     * @param constructorNodeSupplier a supplier that produces the underlying {@code ConstructorNode}
     *                                on first access. Must not be {@code null}.
     */
    public LazyConstructorNode(Supplier<ConstructorNode> constructorNodeSupplier) {
        this.constructorNodeSupplier = constructorNodeSupplier;
    }

    /**
     * Initializes the delegate constructor node on first access.
     *
     * <p>This method is called exactly once via the double-checked locking pattern in
     * {@link #lazyInit()}. It retrieves the delegate from the supplier and propagates
     * any metadata that was set on the proxy before initialization.
     *
     * @see LazyInitializable#doInit()
     */
    @Override
    public void doInit() {
        delegate = constructorNodeSupplier.get();

        ClassNode declaringClass = super.getDeclaringClass();
        if (null != declaringClass) delegate.setDeclaringClass(declaringClass);
    }

    /**
     * Returns whether this constructor node has been initialized.
     *
     * @return {@code true} if the delegate has been instantiated; {@code false} otherwise
     * @see LazyInitializable#isInitialized()
     */
    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Sets the initialization state of this constructor node.
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
     * Checks if the first statement in this constructor is a special call (e.g., super() or this()).
     * Triggers lazy initialization.
     *
     * @return {@code true} if the first statement is a super or this constructor call
     * @see ConstructorNode#firstStatementIsSpecialConstructorCall()
     */
    @Override
    public boolean firstStatementIsSpecialConstructorCall() {
        lazyInit();
        return delegate.firstStatementIsSpecialConstructorCall();
    }

    /**
     * Gets the type descriptor for this constructor. Triggers lazy initialization.
     *
     * @return the constructor's type descriptor string
     * @see ConstructorNode#getTypeDescriptor()
     */
    @Override
    public String getTypeDescriptor() {
        lazyInit();
        return delegate.getTypeDescriptor();
    }

    /**
     * Gets the constructor body code. Triggers lazy initialization.
     *
     * @return the constructor's statement block
     * @see ConstructorNode#getCode()
     */
    @Override
    public Statement getCode() {
        lazyInit();
        return delegate.getCode();
    }

    /**
     * Sets the constructor body code. Triggers lazy initialization.
     *
     * @param code the statement block for this constructor
     * @see ConstructorNode#setCode(Statement)
     */
    @Override
    public void setCode(Statement code) {
        lazyInit();
        delegate.setCode(code);
    }

    /**
     * Gets the access modifiers for this constructor. Triggers lazy initialization.
     *
     * @return a bitmask of the constructor's modifiers (e.g., {@code Modifier.PUBLIC})
     * @see ConstructorNode#getModifiers()
     */
    @Override
    public int getModifiers() {
        lazyInit();
        return delegate.getModifiers();
    }

    /**
     * Sets the access modifiers for this constructor. Triggers lazy initialization.
     *
     * @param modifiers the new modifier bitmask
     * @see ConstructorNode#setModifiers(int)
     */
    @Override
    public void setModifiers(int modifiers) {
        lazyInit();
        delegate.setModifiers(modifiers);
    }

    /**
     * Gets the constructor name. Triggers lazy initialization.
     *
     * <p>For constructors, this typically returns "&lt;init&gt;" or similar internal representation.
     *
     * @return the constructor name
     * @see ConstructorNode#getName()
     */
    @Override
    public String getName() {
        lazyInit();
        return delegate.getName();
    }

    /**
     * Gets the parameters for this constructor. Triggers lazy initialization.
     *
     * @return an array of parameter declarations
     * @see ConstructorNode#getParameters()
     */
    @Override
    public Parameter[] getParameters() {
        lazyInit();
        return delegate.getParameters();
    }

    /**
     * Sets the parameters for this constructor. Triggers lazy initialization.
     *
     * @param parameters the new parameter declarations
     * @see ConstructorNode#setParameters(Parameter[])
     */
    @Override
    public void setParameters(Parameter[] parameters) {
        lazyInit();
        delegate.setParameters(parameters);
    }

    /**
     * Checks if this constructor has default parameter values. Triggers lazy initialization.
     *
     * @return {@code true} if at least one parameter has a default value
     * @see ConstructorNode#hasDefaultValue()
     */
    @Override
    public boolean hasDefaultValue() {
        lazyInit();
        return delegate.hasDefaultValue();
    }

    /**
     * Gets the return type for this constructor. Triggers lazy initialization.
     *
     * @return the declaring class (constructors return an instance of their class)
     * @see ConstructorNode#getReturnType()
     */
    @Override
    public ClassNode getReturnType() {
        lazyInit();
        return delegate.getReturnType();
    }

    /**
     * Sets the return type for this constructor. Triggers lazy initialization.
     *
     * @param returnType the new return type
     * @see ConstructorNode#setReturnType(ClassNode)
     */
    @Override
    public void setReturnType(ClassNode returnType) {
        lazyInit();
        delegate.setReturnType(returnType);
    }

    /**
     * Checks if this constructor has a dynamic return type. Triggers lazy initialization.
     *
     * @return {@code true} if the return type is determined at runtime
     * @see ConstructorNode#isDynamicReturnType()
     */
    @Override
    public boolean isDynamicReturnType() {
        lazyInit();
        return delegate.isDynamicReturnType();
    }

    /**
     * Checks if this constructor has a void return type. Triggers lazy initialization.
     *
     * @return {@code true} if this constructor returns void (typically false)
     * @see ConstructorNode#isVoidMethod()
     */
    @Override
    public boolean isVoidMethod() {
        lazyInit();
        return delegate.isVoidMethod();
    }

    /**
     * Gets the variable scope for this constructor. Triggers lazy initialization.
     *
     * @return the constructor's variable scope
     * @see ConstructorNode#getVariableScope()
     */
    @Override
    public VariableScope getVariableScope() {
        lazyInit();
        return delegate.getVariableScope();
    }

    /**
     * Sets the variable scope for this constructor. Triggers lazy initialization.
     *
     * @param variableScope the new variable scope
     * @see ConstructorNode#setVariableScope(VariableScope)
     */
    @Override
    public void setVariableScope(VariableScope variableScope) {
        lazyInit();
        delegate.setVariableScope(variableScope);
    }

    /**
     * Checks if this constructor is abstract. Triggers lazy initialization.
     *
     * @return {@code true} if this constructor has no implementation
     * @see ConstructorNode#isAbstract()
     */
    @Override
    public boolean isAbstract() {
        lazyInit();
        return delegate.isAbstract();
    }

    /**
     * Checks if this is a default interface method. Triggers lazy initialization.
     *
     * @return {@code true} if this is a default constructor (rarely applicable)
     * @see ConstructorNode#isDefault()
     */
    @Override
    public boolean isDefault() {
        lazyInit();
        return delegate.isDefault();
    }

    /**
     * Checks if this constructor is final. Triggers lazy initialization.
     *
     * @return {@code true} if this constructor cannot be overridden
     * @see ConstructorNode#isFinal()
     */
    @Override
    public boolean isFinal() {
        lazyInit();
        return delegate.isFinal();
    }

    /**
     * Checks if this constructor is static. Triggers lazy initialization.
     *
     * @return {@code true} if this is a static initializer (rarely applicable to constructors)
     * @see ConstructorNode#isStatic()
     */
    @Override
    public boolean isStatic() {
        lazyInit();
        return delegate.isStatic();
    }

    /**
     * Checks if this constructor is public. Triggers lazy initialization.
     *
     * @return {@code true} if this constructor is publicly accessible
     * @see ConstructorNode#isPublic()
     */
    @Override
    public boolean isPublic() {
        lazyInit();
        return delegate.isPublic();
    }

    /**
     * Checks if this constructor is private. Triggers lazy initialization.
     *
     * @return {@code true} if this constructor is accessible only within its class
     * @see ConstructorNode#isPrivate()
     */
    @Override
    public boolean isPrivate() {
        lazyInit();
        return delegate.isPrivate();
    }

    /**
     * Checks if this constructor is protected. Triggers lazy initialization.
     *
     * @return {@code true} if this constructor is protected from external access
     * @see ConstructorNode#isProtected()
     */
    @Override
    public boolean isProtected() {
        lazyInit();
        return delegate.isProtected();
    }

    /**
     * Checks if this constructor is package-scoped. Triggers lazy initialization.
     *
     * @return {@code true} if this constructor has package scope
     * @see ConstructorNode#isPackageScope()
     */
    @Override
    public boolean isPackageScope() {
        lazyInit();
        return delegate.isPackageScope();
    }

    /**
     * Gets the exception types declared by this constructor. Triggers lazy initialization.
     *
     * @return an array of checked exception types
     * @see ConstructorNode#getExceptions()
     */
    @Override
    public ClassNode[] getExceptions() {
        lazyInit();
        return delegate.getExceptions();
    }

    /**
     * Gets the first statement in this constructor body. Triggers lazy initialization.
     *
     * @return the first statement, or {@code null} if the constructor is empty
     * @see ConstructorNode#getFirstStatement()
     */
    @Override
    public Statement getFirstStatement() {
        lazyInit();
        return delegate.getFirstStatement();
    }

    /**
     * Gets the generic type parameters for this constructor. Triggers lazy initialization.
     *
     * @return an array of generic type specifications
     * @see ConstructorNode#getGenericsTypes()
     */
    @Override
    public GenericsType[] getGenericsTypes() {
        lazyInit();
        return delegate.getGenericsTypes();
    }

    /**
     * Sets the generic type parameters for this constructor. Triggers lazy initialization.
     *
     * @param genericsTypes the new generic type specifications
     * @see ConstructorNode#setGenericsTypes(GenericsType[])
     */
    @Override
    public void setGenericsTypes(GenericsType[] genericsTypes) {
        lazyInit();
        delegate.setGenericsTypes(genericsTypes);
    }

    /**
     * Checks if this constructor has an annotation default value. Triggers lazy initialization.
     *
     * @return {@code true} if this constructor is an annotation member with a default
     * @see ConstructorNode#hasAnnotationDefault()
     */
    @Override
    public boolean hasAnnotationDefault() {
        lazyInit();
        return delegate.hasAnnotationDefault();
    }

    /**
     * Sets whether this constructor has an annotation default value. Triggers lazy initialization.
     *
     * @param hasDefaultValue {@code true} if this annotation member has a default
     * @see ConstructorNode#setAnnotationDefault(boolean)
     */
    @Override
    public void setAnnotationDefault(boolean hasDefaultValue) {
        lazyInit();
        delegate.setAnnotationDefault(hasDefaultValue);
    }

    /**
     * Checks if this constructor is a script body method. Triggers lazy initialization.
     *
     * @return {@code true} if this constructor represents the body of a Groovy script
     * @see ConstructorNode#isScriptBody()
     */
    @Override
    public boolean isScriptBody() {
        lazyInit();
        return delegate.isScriptBody();
    }

    /**
     * Marks this constructor as a script body method. Triggers lazy initialization.
     *
     * @see ConstructorNode#setIsScriptBody()
     */
    @Override
    public void setIsScriptBody() {
        lazyInit();
        delegate.setIsScriptBody();
    }

    /**
     * Checks if this is a static constructor (&lt;clinit&gt;). Triggers lazy initialization.
     *
     * @return {@code true} if this constructor initializes static fields
     * @see ConstructorNode#isStaticConstructor()
     */
    @Override
    public boolean isStaticConstructor() {
        lazyInit();
        return delegate.isStaticConstructor();
    }

    /**
     * Checks if this constructor is synthetically public. Triggers lazy initialization.
     *
     * @return {@code true} if this constructor was made public by the compiler
     * @see ConstructorNode#isSyntheticPublic()
     */
    @Override
    public boolean isSyntheticPublic() {
        lazyInit();
        return delegate.isSyntheticPublic();
    }

    /**
     * Sets whether this constructor is synthetically public. Triggers lazy initialization.
     *
     * @param syntheticPublic {@code true} if this constructor is compiler-made public
     * @see ConstructorNode#setSyntheticPublic(boolean)
     */
    @Override
    public void setSyntheticPublic(boolean syntheticPublic) {
        lazyInit();
        delegate.setSyntheticPublic(syntheticPublic);
    }

    /**
     * Gets the source code text for this constructor. Triggers lazy initialization.
     *
     * @return the source text representation
     * @see ConstructorNode#getText()
     */
    @Override
    public String getText() {
        lazyInit();
        return delegate.getText();
    }

    /**
     * Gets all annotations on this constructor. Triggers lazy initialization.
     *
     * @return a list of {@code AnnotationNode} objects; never {@code null}
     * @see ConstructorNode#getAnnotations()
     */
    @Override
    public List<AnnotationNode> getAnnotations() {
        lazyInit();
        return delegate.getAnnotations();
    }

    /**
     * Gets all annotations of a specific type on this constructor. Triggers lazy initialization.
     *
     * @param type the annotation type to query
     * @return a list of matching {@code AnnotationNode} objects
     * @see ConstructorNode#getAnnotations(ClassNode)
     */
    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        lazyInit();
        return delegate.getAnnotations(type);
    }

    /**
     * Adds an annotation to this constructor. Triggers lazy initialization.
     *
     * @param annotation the annotation to add
     * @see ConstructorNode#addAnnotation(AnnotationNode)
     */
    @Override
    public void addAnnotation(AnnotationNode annotation) {
        lazyInit();
        delegate.addAnnotation(annotation);
    }

    /**
     * Adds multiple annotations to this constructor. Triggers lazy initialization.
     *
     * @param annotations the annotations to add
     * @see ConstructorNode#addAnnotations(List)
     */
    @Override
    public void addAnnotations(List<AnnotationNode> annotations) {
        lazyInit();
        delegate.addAnnotations(annotations);
    }

    /**
     * Gets the class that declares this constructor. Triggers lazy initialization.
     *
     * @return the declaring class
     * @see ConstructorNode#getDeclaringClass()
     */
    @Override
    public ClassNode getDeclaringClass() {
        lazyInit();
        return delegate.getDeclaringClass();
    }

    /**
     * Sets the class that declares this constructor without triggering full initialization.
     *
     * <p>This setter is called before initialization to establish the declaring context.
     * The declaring class is propagated to the delegate during {@link #doInit()}.
     *
     * @param declaringClass the class declaring this constructor
     * @see ConstructorNode#setDeclaringClass(ClassNode)
     */
    @Override
    public void setDeclaringClass(ClassNode declaringClass) {
        super.setDeclaringClass(declaringClass);
    }

    /**
     * Gets the Groovydoc comment for this constructor. Triggers lazy initialization.
     *
     * @return the Groovydoc, or {@code null} if not available
     * @see ConstructorNode#getGroovydoc()
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
     * @see ConstructorNode#getInstance()
     */
    @Override
    public AnnotatedNode getInstance() {
        lazyInit();
        return delegate.getInstance();
    }

    /**
     * Checks if this constructor has no real source position. Triggers lazy initialization.
     *
     * @return {@code true} if source position information is unavailable
     * @see ConstructorNode#hasNoRealSourcePosition()
     */
    @Override
    public boolean hasNoRealSourcePosition() {
        lazyInit();
        return delegate.hasNoRealSourcePosition();
    }

    /**
     * Sets whether this constructor has no real source position. Triggers lazy initialization.
     *
     * @param hasNoRealSourcePosition {@code true} if source position is unavailable
     * @see ConstructorNode#setHasNoRealSourcePosition(boolean)
     */
    @Override
    public void setHasNoRealSourcePosition(boolean hasNoRealSourcePosition) {
        lazyInit();
        delegate.setHasNoRealSourcePosition(hasNoRealSourcePosition);
    }

    /**
     * Checks if this constructor is synthetic. Triggers lazy initialization.
     *
     * @return {@code true} if this constructor was generated by the compiler
     * @see ConstructorNode#isSynthetic()
     */
    @Override
    public boolean isSynthetic() {
        lazyInit();
        return delegate.isSynthetic();
    }

    /**
     * Sets whether this constructor is synthetic. Triggers lazy initialization.
     *
     * @param synthetic {@code true} if this constructor is compiler-generated
     * @see ConstructorNode#setSynthetic(boolean)
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
     * @see ConstructorNode#visit(GroovyCodeVisitor)
     */
    @Override
    public void visit(GroovyCodeVisitor visitor) {
        lazyInit();
        delegate.visit(visitor);
    }

    /**
     * Gets the line number where this constructor is defined in source. Triggers lazy initialization.
     *
     * @return the source line number
     * @see ConstructorNode#getLineNumber()
     */
    @Override
    public int getLineNumber() {
        lazyInit();
        return delegate.getLineNumber();
    }

    /**
     * Sets the line number for this constructor. Triggers lazy initialization.
     *
     * @param lineNumber the source line number
     * @see ConstructorNode#setLineNumber(int)
     */
    @Override
    public void setLineNumber(int lineNumber) {
        lazyInit();
        delegate.setLineNumber(lineNumber);
    }

    /**
     * Gets the column number where this constructor is defined in source. Triggers lazy initialization.
     *
     * @return the source column number
     * @see ConstructorNode#getColumnNumber()
     */
    @Override
    public int getColumnNumber() {
        lazyInit();
        return delegate.getColumnNumber();
    }

    /**
     * Sets the column number for this constructor. Triggers lazy initialization.
     *
     * @param columnNumber the source column number
     * @see ConstructorNode#setColumnNumber(int)
     */
    @Override
    public void setColumnNumber(int columnNumber) {
        lazyInit();
        delegate.setColumnNumber(columnNumber);
    }

    /**
     * Gets the last line number for this constructor in source. Triggers lazy initialization.
     *
     * @return the last source line number
     * @see ConstructorNode#getLastLineNumber()
     */
    @Override
    public int getLastLineNumber() {
        lazyInit();
        return delegate.getLastLineNumber();
    }

    /**
     * Sets the last line number for this constructor. Triggers lazy initialization.
     *
     * @param lastLineNumber the last source line number
     * @see ConstructorNode#setLastLineNumber(int)
     */
    @Override
    public void setLastLineNumber(int lastLineNumber) {
        lazyInit();
        delegate.setLastLineNumber(lastLineNumber);
    }

    /**
     * Gets the last column number for this constructor in source. Triggers lazy initialization.
     *
     * @return the last source column number
     * @see ConstructorNode#getLastColumnNumber()
     */
    @Override
    public int getLastColumnNumber() {
        lazyInit();
        return delegate.getLastColumnNumber();
    }

    /**
     * Sets the last column number for this constructor. Triggers lazy initialization.
     *
     * @param lastColumnNumber the last source column number
     * @see ConstructorNode#setLastColumnNumber(int)
     */
    @Override
    public void setLastColumnNumber(int lastColumnNumber) {
        lazyInit();
        delegate.setLastColumnNumber(lastColumnNumber);
    }

    /**
     * Copies the source position from another AST node to this constructor. Triggers lazy initialization.
     *
     * @param node the source node to copy position from
     * @see ConstructorNode#setSourcePosition(ASTNode)
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
     * @see ConstructorNode#copyNodeMetaData(ASTNode)
     */
    @Override
    public void copyNodeMetaData(ASTNode other) {
        lazyInit();
        delegate.copyNodeMetaData(other);
    }

    /**
     * Gets the metadata map for this constructor. Triggers lazy initialization.
     *
     * @return a map of node metadata
     * @see ConstructorNode#getMetaDataMap()
     */
    @Override
    public Map<?, ?> getMetaDataMap() {
        lazyInit();
        return delegate.getMetaDataMap();
    }

    /**
     * Sets the metadata map for this constructor. Triggers lazy initialization.
     *
     * @param metaDataMap the new metadata map
     * @see ConstructorNode#setMetaDataMap(Map)
     */
    @Override
    public void setMetaDataMap(Map<?, ?> metaDataMap) {
        lazyInit();
        delegate.setMetaDataMap(metaDataMap);
    }

    /**
     * Computes the hash code for this constructor. Triggers lazy initialization.
     *
     * @return the hash code of the delegate
     * @see ConstructorNode#hashCode()
     */
    @Override
    public int hashCode() {
        lazyInit();
        return delegate.hashCode();
    }

    /**
     * Checks equality with another object, delegating to the underlying constructor node.
     *
     * <p>Triggers lazy initialization before delegating the comparison to the underlying constructor.
     *
     * @param obj the object to compare with
     * @return {@code true} if equal to the delegate
     * @see ConstructorNode#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        lazyInit();
        return delegate.equals(obj);
    }

    /**
     * Gets the string representation of this constructor. Triggers lazy initialization.
     *
     * @return the string representation of the delegate
     * @see ConstructorNode#toString()
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
     * @see ConstructorNode#getNodeMetaData(Object)
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
     * @see ConstructorNode#getNodeMetaData(Object, Function)
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
     * @see ConstructorNode#copyNodeMetaData(NodeMetaDataHandler)
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
     * @see ConstructorNode#setNodeMetaData(Object, Object)
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
     * @see ConstructorNode#putNodeMetaData(Object, Object)
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
     * @see ConstructorNode#removeNodeMetaData(Object)
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
     * @see ConstructorNode#getNodeMetaData()
     */
    @Override
    public Map<?, ?> getNodeMetaData() {
        lazyInit();
        return delegate.getNodeMetaData();
    }
}

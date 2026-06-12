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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

import static org.apache.groovy.ast.tools.ClassNodeUtils.formatTypeName;
import static org.apache.groovy.ast.tools.MethodNodeUtils.methodDescriptor;
import static org.codehaus.groovy.ast.tools.GenericsUtils.toGenericTypesString;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Represents a method declaration.
 */
public class MethodNode extends AnnotatedNode {

    private final String name;
    private int modifiers;
    private boolean syntheticPublic;
    private ClassNode returnType;
    private Parameter[] parameters;
    private boolean hasDefaultValue;
    private Statement code;
    private boolean dynamicReturnType;
    private VariableScope variableScope;
    private final ClassNode[] exceptions;

    // type spec for generics
    private GenericsType[] genericsTypes;

    // cached data
    private String typeDescriptor;

    protected MethodNode() {
        this.name = null;
        this.exceptions = null;
    }

    /**
     * Creates a method node representing a method or constructor declaration.
     * The parameters array defines the method signature and may include default values.
     * The exceptions array lists checked exceptions declared in the method's throws clause.
     *
     * @param name the method name (use "&lt;init&gt;" for constructors, "&lt;clinit&gt;" for static initializers)
     * @param modifiers ASM modifier flags (public, static, abstract, final, etc.)
     * @param returnType the return type as a {@link ClassNode}
     * @param parameters the method parameters as an array of {@link Parameter}
     * @param exceptions the checked exception types thrown by this method
     * @param code the method body as a {@link Statement} (typically a {@link BlockStatement})
     */
    public MethodNode(final String name, final int modifiers, final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        this.name = name;
        this.modifiers = modifiers;
        this.exceptions = exceptions;
        this.code = code;
        setReturnType(returnType);
        setParameters(parameters);
    }

    /**
     * The type descriptor for a method node is a string containing the name of the method, its return type,
     * and its parameter types in a canonical form. For simplicity, we use the format of a Java declaration
     * without parameter names or generics.
     */
    public String getTypeDescriptor() {
        if (typeDescriptor == null) {
            typeDescriptor = methodDescriptor(this, false);
        }
        return typeDescriptor;
    }

    /**
     * @deprecated use {@link org.apache.groovy.ast.tools.MethodNodeUtils#methodDescriptor(MethodNode, boolean)}
     */
    @Deprecated
    public String getTypeDescriptor(final boolean pretty) {
        return methodDescriptor(this, pretty);
    }

    private void invalidateCachedData() {
        typeDescriptor = null;
    }

    /**
     * Returns the method body as a Statement.
     * Typically a {@link BlockStatement} containing the method's instructions.
     * Returns null for abstract methods or interface methods with no implementation.
     *
     * @return the method body statement, or null for abstract methods
     */
    public Statement getCode() {
        return code;
    }

    /**
     * Sets the method body statement.
     * Typically a {@link BlockStatement}. Setting null indicates an abstract method.
     *
     * @param code the statement representing the method body
     */
    public void setCode(Statement code) {
        this.code = code;
    }

    /**
     * Returns the ASM modifier flags for this method.
     * Flags include visibility (public/protected/private), static, abstract, final, synchronized, etc.
     *
     * @return ASM opcode flags representing this method's modifiers
     */
    public int getModifiers() {
        return modifiers;
    }

    /**
     * Sets the ASM modifier flags for this method.
     * Invalidates cached data (like type descriptor) when modifiers change.
     * Updates the variable scope's static context to match the new modifiers.
     *
     * @param modifiers ASM opcode flags to set
     */
    public void setModifiers(int modifiers) {
        invalidateCachedData();
        this.modifiers = modifiers;
        getVariableScope().setInStaticContext(isStatic());
    }

    /**
     * Returns the method name.
     * Special names: "&lt;init&gt;" for constructors, "&lt;clinit&gt;" for static initializers.
     *
     * @return the method's identifier
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parameter list for this method.
     * Each {@link Parameter} may have an initial value expression for default parameters.
     *
     * @return array of parameters, or empty array if no parameters
     */
    public Parameter[] getParameters() {
        return parameters;
    }

    /**
     * Sets the method's parameter list.
     * Updates variable scope to register all parameters and detects default values.
     * Invalidates cached data like the type descriptor.
     *
     * @param parameters array of {@link Parameter} objects
     */
    public void setParameters(Parameter[] parameters) {
        invalidateCachedData();
        VariableScope scope = new VariableScope();
        this.hasDefaultValue = false;
        this.parameters = parameters;
        if (parameters != null && parameters.length > 0) {
            for (Parameter para : parameters) {
                if (para.hasInitialExpression()) {
                    this.hasDefaultValue = true;
                }
                para.setInStaticContext(isStatic());
                scope.putDeclaredVariable(para);
            }
        }
        setVariableScope(scope);
    }

    /**
     * Checks whether any parameter of this method has a default value.
     * A default value is an initializer expression for a parameter.
     *
     * @return true if at least one parameter has an initial expression
     */
    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }

    /**
     * Returns the method's return type as a {@link ClassNode}.
     *
     * @return the return type (defaults to {@link ClassHelper#OBJECT_TYPE} if not set)
     */
    public ClassNode getReturnType() {
        return returnType;
    }

    /**
     * Sets the method's return type.
     * If the type is null, defaults to {@link ClassHelper#OBJECT_TYPE}.
     * Marks the method as having dynamic return type if appropriate.
     *
     * @param returnType the return {@link ClassNode}
     */
    public void setReturnType(ClassNode returnType) {
        invalidateCachedData();
        this.dynamicReturnType |= ClassHelper.isDynamicTyped(returnType);
        this.returnType = returnType != null ? returnType : ClassHelper.OBJECT_TYPE;
    }

    /**
     * Checks whether this method has a dynamically typed return type.
     * Dynamic typing occurs when the exact return type cannot be determined at compile time.
     *
     * @return true if this method's return type is dynamically typed
     */
    public boolean isDynamicReturnType() {
        return dynamicReturnType;
    }

    /**
     * Checks whether this method returns void.
     *
     * @return true if the return type is primitive void
     */
    public boolean isVoidMethod() {
        return ClassHelper.isPrimitiveVoid(getReturnType());
    }

    /**
     * Returns the variable scope for this method.
     * The scope tracks declared variables and parameters.
     *
     * @return the {@link VariableScope} for this method
     */
    public VariableScope getVariableScope() {
        return variableScope;
    }

    /**
     * Sets the variable scope for this method.
     * The scope is updated to match the method's static context.
     *
     * @param variableScope the {@link VariableScope} to set
     */
    public void setVariableScope(VariableScope variableScope) {
        this.variableScope = variableScope;
        variableScope.setInStaticContext(isStatic());
    }

    /**
     * Checks whether this method is declared as abstract.
     * Abstract methods have no implementation and must be overridden in subclasses.
     *
     * @return true if this method has the abstract modifier
     */
    public boolean isAbstract() {
        return (modifiers & ACC_ABSTRACT) != 0;
    }

    /**
     * Checks whether this method is a default method.
     * Default methods are public methods in interfaces with no abstract modifier.
     *
     * @return true if this method is a default interface method
     */
    public boolean isDefault() {
        return (modifiers & (ACC_ABSTRACT | ACC_PUBLIC | ACC_STATIC)) == ACC_PUBLIC &&
            Optional.ofNullable(getDeclaringClass()).filter(ClassNode::isInterface).isPresent();
    }

    /**
     * Checks whether this method is declared as final.
     * Final methods cannot be overridden.
     *
     * @return true if this method has the final modifier
     */
    public boolean isFinal() {
        return (modifiers & ACC_FINAL) != 0;
    }

    /**
     * Checks whether this method is declared as static.
     *
     * @return true if this method has the static modifier
     */
    public boolean isStatic() {
        return (modifiers & ACC_STATIC) != 0;
    }

    /**
     * Checks whether this method is declared as public.
     *
     * @return true if this method has the public modifier
     */
    public boolean isPublic() {
        return (modifiers & ACC_PUBLIC) != 0;
    }

    /**
     * Checks whether this method is declared as private.
     *
     * @return true if this method has the private modifier
     */
    public boolean isPrivate() {
        return (modifiers & ACC_PRIVATE) != 0;
    }

    /**
     * Checks whether this method is declared as protected.
     *
     * @return true if this method has the protected modifier
     */
    public boolean isProtected() {
        return (modifiers & ACC_PROTECTED) != 0;
    }

    /**
     * Checks whether this method has package scope (no explicit visibility modifier).
     *
     * @return true if no public/private/protected modifier is set
     */
    public boolean isPackageScope() {
        return (modifiers & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) == 0;
    }

    /**
     * Returns the list of checked exceptions declared in the method's throws clause.
     *
     * @return array of exception {@link ClassNode} types, or empty array if no exceptions declared
     */
    public ClassNode[] getExceptions() {
        return exceptions;
    }

    /**
     * Returns the first statement in this method's code block.
     * Recursively unwraps nested BlockStatements to find the first actual statement.
     * Returns null if the code is null or consists only of empty blocks.
     *
     * @return the first statement, or null if no statements exist
     */
    public Statement getFirstStatement() {
        if (code == null) return null;
        Statement first = code;
        while (first instanceof BlockStatement) {
            List<Statement> list = ((BlockStatement) first).getStatements();
            if (list.isEmpty()) {
                first = null;
            } else {
                first = list.get(0);
            }
        }
        return first;
    }

    /**
     * Returns the generic type parameters for this method.
     * Used for generic method declarations (e.g., &lt;T&gt; T getValue()).
     *
     * @return array of {@link GenericsType}, or null if no generics declared
     */
    public GenericsType[] getGenericsTypes() {
        return genericsTypes;
    }

    /**
     * Sets the generic type parameters for this method.
     * Invalidates cached data like the type descriptor.
     *
     * @param genericsTypes array of {@link GenericsType} for this method
     */
    public void setGenericsTypes(GenericsType[] genericsTypes) {
        invalidateCachedData();
        this.genericsTypes = genericsTypes;
    }

    /**
     * @return {@code true} if annotation method has a default value
     */
    public boolean hasAnnotationDefault() {
        return Boolean.TRUE.equals(getNodeMetaData("org.codehaus.groovy.ast.MethodNode.hasDefaultValue"));
    }

    /**
     * Sets the annotation default flag for this method.
     * Used when this method is part of an annotation interface.
     *
     * @param hasDefaultValue true if this annotation method has a default value
     */
    public void setAnnotationDefault(boolean hasDefaultValue) {
        if (hasDefaultValue) {
            putNodeMetaData("org.codehaus.groovy.ast.MethodNode.hasDefaultValue", Boolean.TRUE);
        } else {
            removeNodeMetaData("org.codehaus.groovy.ast.MethodNode.hasDefaultValue");
        }
    }

    /**
     * Checks whether this method is the run method from a script execution.
     * Scripts have their module-level statements compiled into a synthetic run() method.
     *
     * @return true if this method represents script body code
     */
    public boolean isScriptBody() {
        return Boolean.TRUE.equals(getNodeMetaData("org.codehaus.groovy.ast.MethodNode.isScriptBody"));
    }

    /**
     * Sets the flag for this method to indicate it is a script body implementation.
     *
     * @see ModuleNode#createStatementsClass()
     */
    public void setIsScriptBody() {
        setNodeMetaData("org.codehaus.groovy.ast.MethodNode.isScriptBody", Boolean.TRUE);
    }

    /**
     * Checks whether this method is a static initializer (&lt;clinit&gt;).
     *
     * @return true if this method's name is "&lt;clinit&gt;"
     */
    public boolean isStaticConstructor() {
        return "<clinit>".equals(name);
    }

    /**
     * Checks whether this method is a constructor (&lt;init&gt;).
     *
     * @return true if this method's name is "&lt;init&gt;"
     * @since 4.0.0
     */
    public boolean isConstructor() {
        return "<init>".equals(name);
    }

    /**
     * Checks whether this method was synthesized as public by Groovy's default visibility rule.
     * Groovy makes methods public by default, even if no public modifier was explicitly written.
     * This flag tracks whether the public modifier was synthetic versus explicit.
     * This information is primarily of interest to AST transform writers.
     *
     * @return true if this method is public due to Groovy's default visibility, not an explicit modifier
     */
    public boolean isSyntheticPublic() {
        return syntheticPublic;
    }

    /**
     * Marks this method as having synthetic public visibility.
     *
     * @param syntheticPublic true to mark as synthetically public
     */
    public void setSyntheticPublic(boolean syntheticPublic) {
        this.syntheticPublic = syntheticPublic;
    }

    @Override
    public String getText() {
        int mask = this instanceof ConstructorNode ? Modifier.constructorModifiers() : Modifier.methodModifiers();
        String name = getName(); if (name.indexOf(' ') != -1) name = "\"" + name + "\""; // GROOVY-10417
        return AstToTextHelper.getModifiersText(getModifiers() & mask) +
                ' ' +
                toGenericTypesString(getGenericsTypes()) +
                AstToTextHelper.getClassText(getReturnType()) +
                ' ' +
                name +
                '(' +
                AstToTextHelper.getParametersText(getParameters()) +
                ')' +
                AstToTextHelper.getThrowsClauseText(getExceptions()) +
                " { ... }";
    }

    @Override
    public String toString() {
        ClassNode declaringClass = getDeclaringClass();
        return super.toString() + "[" + methodDescriptor(this, true) + (declaringClass == null ? "" : " from " + formatTypeName(declaringClass)) + "]";
    }
}

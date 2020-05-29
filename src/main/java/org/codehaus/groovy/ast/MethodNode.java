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

import org.apache.groovy.ast.tools.MethodNodeUtils;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Optional;

/**
 * Represents a method declaration.
 */
public class MethodNode extends AnnotatedNode implements Opcodes {

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
    private final boolean staticConstructor;

    // type spec for generics
    private GenericsType[] genericsTypes;

    // cached data
    private String typeDescriptor;

    protected MethodNode() {
        this.name = null;
        this.exceptions = null;
        this.staticConstructor = false;
    }

    public MethodNode(final String name, final int modifiers, final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        this.name = name;
        this.modifiers = modifiers;
        this.exceptions = exceptions;
        this.code = code;
        setReturnType(returnType);
        setParameters(parameters);
        this.staticConstructor = "<clinit>".equals(name);
    }

    /**
     * The type descriptor for a method node is a string containing the name of the method, its return type,
     * and its parameter types in a canonical form. For simplicity, we use the format of a Java declaration
     * without parameter names or generics.
     */
    public String getTypeDescriptor() {
        if (typeDescriptor == null) {
            typeDescriptor = MethodNodeUtils.methodDescriptor(this);
        }
        return typeDescriptor;
    }

    private void invalidateCachedData() {
        typeDescriptor = null;
    }

    public Statement getCode() {
        return code;
    }

    public void setCode(Statement code) {
        this.code = code;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        invalidateCachedData();
        this.modifiers = modifiers;
    }

    public String getName() {
        return name;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

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
     * @return {@code true} if any parameter has a default value
     */
    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }

    public ClassNode getReturnType() {
        return returnType;
    }

    public void setReturnType(ClassNode returnType) {
        invalidateCachedData();
        this.dynamicReturnType |= ClassHelper.DYNAMIC_TYPE == returnType;
        this.returnType = returnType != null ? returnType : ClassHelper.OBJECT_TYPE;
    }

    public boolean isDynamicReturnType() {
        return dynamicReturnType;
    }

    public boolean isVoidMethod() {
        return ClassHelper.VOID_TYPE.equals(getReturnType());
    }

    public VariableScope getVariableScope() {
        return variableScope;
    }

    public void setVariableScope(VariableScope variableScope) {
        this.variableScope = variableScope;
        variableScope.setInStaticContext(isStatic());
    }

    public boolean isAbstract() {
        return (modifiers & ACC_ABSTRACT) != 0;
    }

    public boolean isDefault() {
        return (modifiers & (ACC_ABSTRACT | ACC_PUBLIC | ACC_STATIC)) == ACC_PUBLIC &&
            Optional.ofNullable(getDeclaringClass()).filter(ClassNode::isInterface).isPresent();
    }

    public boolean isFinal() {
        return (modifiers & ACC_FINAL) != 0;
    }

    public boolean isStatic() {
        return (modifiers & ACC_STATIC) != 0;
    }

    public boolean isPublic() {
        return (modifiers & ACC_PUBLIC) != 0;
    }

    public boolean isPrivate() {
        return (modifiers & ACC_PRIVATE) != 0;
    }

    public boolean isProtected() {
        return (modifiers & ACC_PROTECTED) != 0;
    }

    public boolean isPackageScope() {
        return (modifiers & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) == 0;
    }

    public ClassNode[] getExceptions() {
        return exceptions;
    }

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

    public GenericsType[] getGenericsTypes() {
        return genericsTypes;
    }

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

    public void setAnnotationDefault(boolean hasDefaultValue) {
        if (hasDefaultValue) {
            putNodeMetaData("org.codehaus.groovy.ast.MethodNode.hasDefaultValue", Boolean.TRUE);
        } else {
            removeNodeMetaData("org.codehaus.groovy.ast.MethodNode.hasDefaultValue");
        }
    }

    /**
     * @return {@code true} if this method is the run method from a script
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

    public boolean isStaticConstructor() {
        return staticConstructor;
    }

    /**
     * Indicates that this method has been "promoted" to public by
     * Groovy when in fact there was no public modifier explicitly
     * in the source code. I.e. it remembers that it has applied
     * Groovy's "public methods by default" rule. This property is
     * typically only of interest to AST transform writers.
     *
     * @return {@code true} if this class is public but had no explicit public modifier
     */
    public boolean isSyntheticPublic() {
        return syntheticPublic;
    }

    public void setSyntheticPublic(boolean syntheticPublic) {
        this.syntheticPublic = syntheticPublic;
    }

    /**
     * Provides a nicely formatted string of the method definition. For simplicity, generic types on some of the elements
     * are not displayed.
     *
     * @return string form of node with some generic elements suppressed
     */
    @Override
    public String getText() {
        String retType = AstToTextHelper.getClassText(returnType);
        String exceptionTypes = AstToTextHelper.getThrowsClauseText(exceptions);
        String params = AstToTextHelper.getParametersText(parameters);
        return AstToTextHelper.getModifiersText(modifiers) + " " + retType + " " + name + "(" + params + ") " + exceptionTypes + " { ... }";
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getDeclaringClass().getName() + "#" + getTypeDescriptor() + "]";
    }
}

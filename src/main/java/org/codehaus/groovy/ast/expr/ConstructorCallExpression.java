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
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a constructor call expression.
 * This includes regular object construction (e.g., {@code new MyClass(args)}),
 * as well as special constructor calls like {@code this()} and {@code super()} calls within constructors.
 * The constructor is identified by the type being constructed, and the arguments are wrapped in a {@link TupleExpression}.
 * May optionally use an anonymous inner class.
 * Explicit constructor type arguments ({@code new <T>Box(t)}, {@code <T>this(t)}, {@code <T>super(t)})
 * are stored separately from the constructed type's own type arguments.
 * 
 * @see {@link MethodCall} for the method interface this class implements
 * @see {@link TupleExpression} for argument representation
 * @see {@link ClassNode#THIS} for {@code this()} constructor calls
 * @see {@link ClassNode#SUPER} for {@code super()} constructor calls
 */
public class ConstructorCallExpression extends Expression implements MethodCall {

    /**
     * The constructor arguments, always wrapped in a {@link TupleExpression}.
     */
    private final Expression arguments;
    /**
     * Whether this constructor call uses an anonymous inner class declaration.
     */
    private boolean usesAnonymousInnerClass;
    /**
     * Explicit constructor type arguments, e.g. {@code new <String>Box("x")} or {@code <T>super(t)}.
     */
    private GenericsType[] genericsTypes;

    /**
     * Creates a constructor call expression.
     * Arguments are automatically wrapped in a {@link TupleExpression} if not already.
     * 
     * @param type the type being constructed (non-null), or {@link ClassNode#THIS}/{@link ClassNode#SUPER} for special calls
     * @param arguments the constructor arguments (may be a single expression or already a {@link TupleExpression})
     */
    public ConstructorCallExpression(ClassNode type, Expression arguments) {
        setType(type);
        if (!(arguments instanceof TupleExpression)) {
            this.arguments = new TupleExpression(arguments);
            this.arguments.setSourcePosition(arguments);
        } else {
            this.arguments = arguments;
        }
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitConstructorCallExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        ConstructorCallExpression answer = new ConstructorCallExpression(getType(), transformer.transform(arguments));
        answer.setUsingAnonymousInnerClass(isUsingAnonymousInnerClass());
        answer.setGenericsTypes(genericsTypes);
        answer.setSourcePosition(this);
        answer.copyNodeMetaData(this);
        return answer;
    }

    @Override
    public ASTNode getReceiver() {
        return null;
    }

    @Override
    public String getMethodAsString() {
        return "<init>";
    }

    /**
     * Returns the constructor arguments.
     * 
     * @return a {@link TupleExpression} containing the arguments
     */
    @Override
    public Expression getArguments() {
        return arguments;
    }

    @Override
    public String getText() {
        StringBuilder builder = new StringBuilder(64);
        if (isSuperCall()) {
            appendTypeArguments(builder);
            builder.append("super ");
        } else if (isThisCall()) {
            appendTypeArguments(builder);
            builder.append("this ");
        } else {
            builder.append("new ");
            appendTypeArguments(builder);
            builder.append(getType().toString(false));
        }
        return builder.append(getArguments().getText()).toString();
    }

    private void appendTypeArguments(final StringBuilder builder) {
        if (!isUsingGenerics()) return;
        builder.append('<');
        boolean first = true;
        for (GenericsType t : genericsTypes) {
            if (!first) builder.append(", ");
            else first = false;
            builder.append(t);
        }
        builder.append('>');
    }

    /**
     * Returns the explicit constructor type arguments, if specified.
     * Used for generic constructor invocations such as {@code new <String>Box("x")},
     * {@code <T>this(t)} and {@code <T>super(t)}.
     *
     * @return an array of {@link GenericsType} parameters, or {@code null} if none are specified
     * @since 6.0.0
     */
    public GenericsType[] getGenericsTypes() {
        return genericsTypes;
    }

    /**
     * Sets the explicit constructor type arguments.
     *
     * @param genericsTypes an array of {@link GenericsType} parameters; may be {@code null}
     * @since 6.0.0
     */
    public void setGenericsTypes(final GenericsType[] genericsTypes) {
        this.genericsTypes = genericsTypes;
    }

    /**
     * Indicates whether explicit constructor type arguments are specified.
     *
     * @return {@code true} if type arguments are present; {@code false} otherwise
     * @since 6.0.0
     */
    public boolean isUsingGenerics() {
        return genericsTypes != null && genericsTypes.length > 0;
    }

    /**
     * Indicates whether this is a special constructor call ({@code this()} or {@code super()}).
     * 
     * @return true if this is a {@code this()} or {@code super()} call, false for regular constructor calls
     */
    public boolean isSpecialCall() {
        return isThisCall() || isSuperCall();
    }

    /**
     * Indicates whether this is a {@code super()} constructor call.
     * 
     * @return true if this calls the superclass constructor, false otherwise
     */
    public boolean isSuperCall() {
        return getType() == ClassNode.SUPER;
    }

    /**
     * Indicates whether this is a {@code this()} constructor call.
     * 
     * @return true if this calls another constructor in the same class, false otherwise
     */
    public boolean isThisCall() {
        return getType() == ClassNode.THIS;
    }

    /**
     * Indicates whether this constructor call uses an anonymous inner class.
     * 
     * @return true if an anonymous inner class is being created, false for regular construction
     */
    public boolean isUsingAnonymousInnerClass() {
        return usesAnonymousInnerClass;
    }

    /**
     * Sets whether this constructor call uses an anonymous inner class.
     * 
     * @param usage true if an anonymous inner class is being used, false otherwise
     */
    public void setUsingAnonymousInnerClass(boolean usage) {
        this.usesAnonymousInnerClass = usage;
    }

    @Override
    public String toString() {
        return super.toString() + "[type: " + getType() + " arguments: " + arguments + "]";
    }
}

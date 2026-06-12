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
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.MethodNode;

import java.util.Collections;
import java.util.List;

import static org.codehaus.groovy.tools.Utilities.isJavaIdentifier;

/**
 * Represents a method call on an object or class, including receiver object, method name/expression,
 * and arguments. Supports safe navigation (null-safe calls with {@code ?}), spread-safe operations,
 * implicit this, and direct method targets for optimization. Method names can be constant strings
 * or dynamic expressions (e.g., computed method names).
 *
 * @see Expression
 * @see MethodCall
 * @see VariableExpression
 * @see ClosureExpression
 */
public class MethodCallExpression extends Expression implements MethodCall {

    private Expression objectExpression;
    private Expression method;
    private Expression arguments;

    private boolean implicitThis = true;
    private boolean spreadSafe;
    private boolean safe;

    // type spec for generics
    private GenericsType[] genericsTypes;

    private MethodNode target;

    public static final Expression NO_ARGUMENTS = new TupleExpression() {
        @Override
        public List<Expression> getExpressions() {
            return Collections.unmodifiableList(super.getExpressions());
        }
        @Override
        public TupleExpression addExpression(Expression e) {
            throw new UnsupportedOperationException();
        }
    };

    /**
     * Creates a method call with a string method name.
     *
     * @param objectExpression the receiver object on which the method is invoked; must not be null
     * @param method the name of the method as a {@link String}; must not be null
     * @param arguments the method arguments as an {@link Expression}; will be wrapped in a
     *                  {@link TupleExpression} if not already one
     */
    public MethodCallExpression(Expression objectExpression, String method, Expression arguments) {
        this(objectExpression, new ConstantExpression(method), arguments);
    }

    /**
     * Creates a method call with an expression-based method name (supports dynamic method names).
     *
     * @param objectExpression the receiver object on which the method is invoked; must not be null
     * @param method the method name as an {@link Expression}, which may be a {@link ConstantExpression}
     *               for static names or other expression types for dynamic names; must not be null
     * @param arguments the method arguments as an {@link Expression}; will be wrapped in a
     *                  {@link TupleExpression} if not already one
     */
    public MethodCallExpression(Expression objectExpression, Expression method, Expression arguments) {
        setMethod(method);
        setArguments(arguments);
        setObjectExpression(objectExpression);

        // TODO: set correct type here
        // if setting type and a MethodCall is the last expression in a method,
        // then the method will return null if the method itself is not void too!
        // (in bytecode after call: aconst_null, areturn)
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitMethodCallExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        MethodCallExpression answer =
            new MethodCallExpression(transformer.transform(objectExpression), transformer.transform(method), transformer.transform(arguments));
        answer.setSafe(safe);
        answer.setSpreadSafe(spreadSafe);
        answer.setImplicitThis(implicitThis);
        answer.setGenericsTypes(genericsTypes);
        answer.setSourcePosition(this);
        answer.setMethodTarget(target);
        answer.copyNodeMetaData(this);
        return answer;
    }

    /**
     * Returns the method arguments as a {@link TupleExpression}.
     *
     * @return the method arguments; typically a {@link TupleExpression}, never null
     */
    @Override
    public Expression getArguments() {
        return arguments;
    }

    /**
     * Sets the method arguments. If the provided expression is not a {@link TupleExpression},
     * it will be automatically wrapped in one for internal consistency.
     *
     * @param arguments the method arguments as an {@link Expression}; must not be null
     */
    public void setArguments(Expression arguments) {
        if (!(arguments instanceof TupleExpression)) {
            this.arguments = new TupleExpression(arguments);
            this.arguments.setSourcePosition(arguments);
        } else {
            this.arguments = arguments;
        }
    }

    /**
     * Returns the method name or reference expression. This may be a {@link ConstantExpression}
     * for static method names or other expression types for dynamically computed names.
     *
     * @return the method name {@link Expression}; never null
     */
    public Expression getMethod() {
        return method;
    }

    /**
     * Sets the method name or reference expression.
     *
     * @param method the method name as an {@link Expression}; must not be null
     */
    public void setMethod(Expression method) {
      this.method = method;
    }

    /**
     * Returns the method name as a string if it is a constant static name, or null if the
     * method name is computed dynamically.
     *
     * @return the method name as a {@link String} if this is a static method call, or null
     *         if the method name is a dynamic expression
     */
    @Override
    public String getMethodAsString() {
        return (method instanceof ConstantExpression ? method.getText() : null);
    }

    /**
     * Returns the object (receiver) on which this method is invoked.
     *
     * @return the receiver {@link Expression}; never null
     */
    public Expression getObjectExpression() {
        return objectExpression;
    }

    /**
     * Sets the object (receiver) on which this method is invoked.
     *
     * @param objectExpression the receiver {@link Expression}; must not be null
     */
    public void setObjectExpression(Expression objectExpression) {
      this.objectExpression = objectExpression;
    }

    /**
     * Returns the receiver as an {@link ASTNode}. This is used by the {@link MethodCall} interface.
     *
     * @return the receiver {@link ASTNode}
     */
    @Override
    public ASTNode getReceiver() {
        return getObjectExpression();
    }

    @Override
    public String getText() {
        StringBuilder builder = new StringBuilder( 64 );
        builder.append(getObjectExpression().getText());
        if (isSpreadSafe()) builder.append('*');
        if (isSafe()) builder.append('?');
        builder.append('.');

        if (isUsingGenerics()) {
            builder.append('<');
            boolean first = true;
            for (GenericsType t : getGenericsTypes()) {
                if (!first) builder.append(", ");
                else first = false;
                builder.append(t);
            }
            builder.append('>');
        }

        Expression method = getMethod();
        if (method instanceof GStringExpression) {
            builder.append('"').append(method.getText()).append('"');
        } else if (!(method instanceof ConstantExpression)) {
            builder.append('(').append(method.getText()).append(')');
        } else {
            Object value = ((ConstantExpression) method).getValue();
            if (!(value instanceof String) || !isJavaIdentifier((String) value)) {
                builder.append("'").append(value).append("'");
            } else {
                builder.append((String) value);
            }
        }

        builder.append(getArguments().getText());

        return builder.toString();
    }

    /**
     * Indicates whether this is a safe method call. In a safe call, if the receiver object is null,
     * the method call returns null instead of throwing a NullPointerException (e.g., {@code obj?.method()}).
     *
     * @return true if this is a safe method call; false otherwise
     */
    public boolean isSafe() {
        return safe;
    }

    /**
     * Sets whether this should be a safe method call.
     *
     * @param safe true for safe navigation; false otherwise
     */
    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    /**
     * Indicates whether this method call uses spread-safe navigation. Spread-safe operations
     * iterate over collection elements and apply the method call to each element safely.
     *
     * @return true if spread-safe navigation is enabled; false otherwise
     */
    public boolean isSpreadSafe() {
        return spreadSafe;
    }

    /**
     * Sets whether this method call should use spread-safe navigation.
     *
     * @param value true to enable spread-safe navigation; false otherwise
     */
    public void setSpreadSafe(boolean value) {
        spreadSafe = value;
    }

    /**
     * Indicates whether this method call implicitly refers to the current object ({@code this}).
     * If true, no explicit receiver object was specified (e.g., {@code method()} vs {@code obj.method()}).
     *
     * @return true if the method call implicitly refers to this; false if an explicit receiver
     *         object was specified
     */
    public boolean isImplicitThis() {
        return implicitThis;
    }

    /**
     * Sets whether this method call implicitly refers to the current object.
     *
     * @param implicitThis true if this is an implicit this call; false otherwise
     */
    public void setImplicitThis(boolean implicitThis) {
        this.implicitThis = implicitThis;
    }

    /**
     * Returns the generic type parameters for this method call, if specified.
     * Used for generic method invocations like {@code obj.<String>method()}.
     *
     * @return an array of {@link GenericsType} parameters, or null if no generics are specified
     */
    public GenericsType[] getGenericsTypes() {
        return genericsTypes;
    }

    /**
     * Sets the generic type parameters for this method call.
     *
     * @param genericsTypes an array of {@link GenericsType} parameters; may be null
     */
    public void setGenericsTypes(GenericsType[] genericsTypes) {
        this.genericsTypes = genericsTypes;
    }

    /**
     * Indicates whether generic type parameters are specified for this method call.
     *
     * @return true if generic type parameters are present; false otherwise
     */
    public boolean isUsingGenerics() {
        return (genericsTypes != null && genericsTypes.length > 0);
    }

    /**
     * Returns the target {@link MethodNode} if this method call has been resolved to a specific method.
     * When a target is set, the method call will execute the direct target outside of the MOP
     * (method resolution order), enabling direct method invocation for optimization.
     *
     * @return the target {@link MethodNode}, or null if no specific target is set
     */
    public MethodNode getMethodTarget() {
        return target;
    }

    /**
     * Sets a direct method target for this method call. When set, the method call will invoke
     * the specified target directly, bypassing the MOP (method resolution order) for efficiency.
     * WARNING: A method call made this way will run outside of the MOP and may not respect
     * dynamic dispatch or meta-programming extensions.
     *
     * @param mn the target {@link MethodNode}; may be null to clear the target
     */
    public void setMethodTarget(MethodNode mn) {
        this.target = mn;
        if (mn != null) {
            setType(target.getReturnType());
        } else {
            setType(ClassHelper.OBJECT_TYPE);
        }
    }

    @Override
    public void setSourcePosition(ASTNode node) {
        super.setSourcePosition(node);
        // GROOVY-8002: propagate position to (possibly new) method expression
        if (node instanceof MethodCall) {
            if (node instanceof MethodCallExpression) {
                method.setSourcePosition(((MethodCallExpression) node).getMethod());
            } else if (node.getLineNumber() > 0) {
                method.setLineNumber(node.getLineNumber());
                method.setColumnNumber(node.getColumnNumber());
                method.setLastLineNumber(node.getLineNumber());
                method.setLastColumnNumber(node.getColumnNumber() + getMethodAsString().length());
            }
            if (arguments != null) {
                arguments.setSourcePosition(((MethodCall) node).getArguments());
            }
        } else if (node instanceof PropertyExpression) {
            method.setSourcePosition(((PropertyExpression) node).getProperty());
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[object: " + objectExpression + " method: " + method + " arguments: " + arguments + "]";
    }
}

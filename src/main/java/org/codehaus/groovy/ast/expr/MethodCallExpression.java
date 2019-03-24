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

/**
 * A method call on an object or class
 */
public class MethodCallExpression extends Expression implements MethodCall {

    private Expression objectExpression;
    private Expression method;
    private Expression arguments;
    private boolean spreadSafe = false;
    private boolean safe = false;
    private boolean implicitThis;

    // type spec for generics
    private GenericsType[] genericsTypes = null;
    private boolean usesGenerics = false;

    private MethodNode target;

    public static final Expression NO_ARGUMENTS = new TupleExpression();

    public MethodCallExpression(Expression objectExpression, String method, Expression arguments) {
        this(objectExpression,new ConstantExpression(method),arguments);
    }

    public MethodCallExpression(Expression objectExpression, Expression method, Expression arguments) {
        this.objectExpression = objectExpression;
        this.method = method;
        if (!(arguments instanceof TupleExpression)){
            this.arguments = new TupleExpression(arguments);
        } else {
            this.arguments = arguments;
        }
        //TODO: set correct type here
        // if setting type and a methodcall is the last expression in a method,
        // then the method will return null if the method itself is not void too!
        // (in bytecode after call: aconst_null, areturn)
        this.setType(ClassHelper.DYNAMIC_TYPE);
        this.setImplicitThis(true);
    }

    public void accept(GroovyCodeVisitor visitor) {
        visitor.visitMethodCallExpression(this);
    }

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

    public Expression getArguments() {
        return arguments;
    }

    public void setArguments(Expression arguments) {
        if (!(arguments instanceof TupleExpression)){
            this.arguments = new TupleExpression(arguments);
        } else {
            this.arguments = arguments;
        }
    }

    public Expression getMethod() {
        return method;
    }

    public void setMethod(Expression method) {
      this.method = method;
    }

    public ASTNode getReceiver() {
        return getObjectExpression();
    }

    /**
     * This method returns the method name as String if it is no dynamic
     * calculated method name, but a constant.
     */
    public String getMethodAsString() {
        if (! (method instanceof ConstantExpression)) return null;
        ConstantExpression constant = (ConstantExpression) method;
        return constant.getText();
    }

    public void setObjectExpression(Expression objectExpression) {
      this.objectExpression = objectExpression;
    }

    public Expression getObjectExpression() {
        return objectExpression;
    }

    public String getText() {
        String object = objectExpression.getText();
        String meth = method.getText();
        String args = arguments.getText();
        String spread = spreadSafe ? "*" : "";
        String dereference = safe ? "?" : "";
        return object + spread + dereference + "." + meth + args;
    }

    /**
     * @return is this a safe method call, i.e. if true then if the source object is null
     * then this method call will return null rather than throwing a null pointer exception
     */
    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    public boolean isSpreadSafe() {
        return spreadSafe;
    }

    public void setSpreadSafe(boolean value) {
        spreadSafe = value;
    }

    /**
     * @return true if no object expression was specified otherwise if
     * some expression was specified for the object on which to evaluate
     * the method then return false
     */
    public boolean isImplicitThis() {
        return implicitThis;
    }

    public void setImplicitThis(boolean implicitThis) {
        this.implicitThis = implicitThis;
    }

    public String toString() {
        return super.toString()
            + "[object: "
            + objectExpression
            + " method: "
            + method
            + " arguments: "
            + arguments
            + "]";
    }

    public GenericsType[] getGenericsTypes() {
        return genericsTypes;
    }

    public void setGenericsTypes(GenericsType[] genericsTypes) {
        usesGenerics = usesGenerics || genericsTypes != null;
        this.genericsTypes = genericsTypes;
    }

    public boolean isUsingGenerics() {
        return usesGenerics;
    }

    /**
     * Sets a method call target for a direct method call.
     * WARNING: A method call made this way will run outside of the MOP!
     * @param mn the target as MethodNode, mn==null means no target
     */
    public void setMethodTarget(MethodNode mn) {
        this.target = mn;
        if (mn!=null) {
            setType(target.getReturnType());
        } else {
            setType(ClassHelper.OBJECT_TYPE);
        }
    }

    /**
     * @return the target as method node if set
     */
    public MethodNode getMethodTarget() {
        return target;
    }
}

/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.ast.expr;

import groovy.lang.MetaMethod;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * A method call on an object or class
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MethodCallExpression extends Expression {

    private Expression objectExpression;
    private Expression method;
    private Expression arguments;
    private boolean spreadSafe = false;
    private boolean safe = false;
    private boolean implicitThis;
    
    public static final Expression NO_ARGUMENTS = new TupleExpression();

    public MetaMethod getMetaMethod() {
        return metaMethod;
    }

    private MetaMethod metaMethod = null;

    public MethodCallExpression(Expression objectExpression, String method, Expression arguments) {
        this(objectExpression,new ConstantExpression(method),arguments);
    }
    
    public MethodCallExpression(Expression objectExpression, Expression method, Expression arguments) {
        this.objectExpression = objectExpression;
        this.method = method;
        this.arguments = arguments;
        //TODO: set correct type here
        // if setting type and a methodcall is the last expression in a method,
        // then the method will return null if the method itself is not void too!
        // (in bytecode after call: aconst_null, areturn)
        this.setType(ClassHelper.DYNAMIC_TYPE);
        this.setImplicitThis(true);
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitMethodCallExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        MethodCallExpression answer =
            new MethodCallExpression(transformer.transform(objectExpression), transformer.transform(method), transformer.transform(arguments));
        answer.setSafe(safe);
        answer.setSpreadSafe(spreadSafe);
        answer.setImplicitThis(implicitThis);
        answer.setSourcePosition(this);
        return answer;
    }

    public Expression getArguments() {
        return arguments;
    }

    public void setArguments(Expression arguments)
    {
      this.arguments = arguments;
    }

    public Expression getMethod() {
        return method;
    }

    public void setMethod(Expression method)
    {
      this.method = method;
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

    public void setObjectExpression(Expression objectExpression)
    {
      this.objectExpression = objectExpression;
    }

    public Expression getObjectExpression() {
        return objectExpression;
    }

    public String getText() {
        return objectExpression.getText() + "." + method.getText() + arguments.getText();
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

    public void setMetaMethod(MetaMethod mmeth) {
        this.metaMethod = mmeth;
        super.setType(ClassHelper.make(mmeth.getReturnType()));
    }
}

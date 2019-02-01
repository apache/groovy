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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.syntax.Token;

/**
 * Represents one or more local variables. Typically it is a single local variable
 * declared by name with an expression like "def foo" or with type "String foo". However, 
 * the multiple assignment feature allows you to create two or more variables using
 * an expression like: <code>def (x, y) = [1, 2]</code>.
 * <p>
 * You can access the left hand side of a declaration using the
 * "<code>Expression getLeftExpression()</code>" method. In which case you might then
 * use <code>instanceof</code> and casting to perform operations specific to a
 * single local variable (<code>VariableExpression</code>) or for the multiple
 * assignment case (<code>TupleExpression</code>).
 * <p>
 * Alternatively, if <code>isMultipleAssignmentDeclaration()</code> is <code>false</code>
 * you can use the method "<code>VariableExpression getVariableExpression()</code>" method.
 * Similarly, if <code>isMultipleAssignmentDeclaration()</code> is <code>true</code>
 * you can use the method "<code>TupleExpression getTupleExpression()</code>" method.
 * Calling either of these expression getters when the "isMultipleAssignment" condition
 * is not appropriate is unsafe and will result in a <code>ClassCastException</code>.
 */
public class DeclarationExpression extends BinaryExpression {
    
    /**
     * Creates a DeclarationExpression for VariableExpressions like "def x" or "String y = 'foo'". 
     * @param left
     *      the left hand side of a variable declaration
     * @param operation
     *      the operation, typically an assignment operator
     * @param right
     *      the right hand side of a declaration
     */ 
    public DeclarationExpression(VariableExpression left, Token operation, Expression right) {
        super(left,operation,right);
    }
    
    /**
     * Creates a DeclarationExpression for Expressions like "def (x, y) = [1, 2]"
     * @param left
     *      the left hand side of a declaration. Must be either a VariableExpression or 
     *      a TupleExpression with at least one element.  
     * @param operation
     *       the operation, typically an assignment operator
     * @param right
     *       the right hand side of a declaration
     */ 
    public DeclarationExpression(Expression left, Token operation, Expression right) {
        super(left,operation,right);
        check(left);
    }
    
    private static void check(Expression left) {
        if (left instanceof VariableExpression) {
            //nothing
        } else if (left instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) left;
            if (tuple.getExpressions().isEmpty()) throw new GroovyBugError("one element required for left side");
        } else {
            throw new GroovyBugError("illegal left expression for declaration: "+left);
        }
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitDeclarationExpression(this);
    }

    /**
     * This method returns the left hand side of the declaration cast to the VariableExpression type.
     * This is an unsafe method to call. In a multiple assignment statement, the left hand side will
     * be a TupleExpression and a ClassCastException will occur. If you invoke this method then
     * be sure to invoke isMultipleAssignmentDeclaration() first to check that it is safe to do so. 
     * If that method returns true then this method is safe to call.
     *
     * @return left hand side of normal variable declarations
     * @throws ClassCastException if the left hand side is not a VariableExpression (and is probably a multiple assignment statement).
     */
    public VariableExpression getVariableExpression() {
        return (VariableExpression) this.getLeftExpression();
    }
    
    /**
     * This method returns the left hand side of the declaration cast to the TupleExpression type.
     * This is an unsafe method to call. In a single assignment statement, the left hand side will
     * be a VariableExpression and a ClassCastException will occur. If you invoke this method then
     * be sure to invoke isMultipleAssignmentDeclaration() first to check that it is safe to do so. 
     * If that method returns true then this method is safe to call. 
     * @return
     *      left hand side of multiple assignment declarations
     * @throws ClassCastException 
     *      if the left hand side is not a TupleExpression (and is probably a VariableExpression).
     *
     */
    public TupleExpression getTupleExpression() {
        return (TupleExpression) this.getLeftExpression();
    }
    
    /**
     * This method sets the leftExpression for this BinaryExpression. The parameter must be
     * either a VariableExpression or a TupleExpression with one or more elements. 
     * @param leftExpression
     *      either a VariableExpression or a TupleExpression with one or more elements. 
     */ 
    public void setLeftExpression(Expression leftExpression) {
        check(leftExpression);
        super.setLeftExpression(leftExpression);
    }
    
    public void setRightExpression(Expression rightExpression) {
        super.setRightExpression(rightExpression);
    }
    
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new DeclarationExpression(transformer.transform(getLeftExpression()),
                getOperation(), transformer.transform(getRightExpression()));
        ret.setSourcePosition(this);
        ret.addAnnotations(getAnnotations());
        ret.setDeclaringClass(getDeclaringClass());
        ret.copyNodeMetaData(this);
        return ret;
    }
    
    /**
     * This method tells you if this declaration is a multiple assignment declaration, which 
     * has the form "def (x, y) = ..." in Groovy. If this method returns true, then the left
     * hand side is an ArgumentListExpression. Do not call "getVariableExpression()" on this 
     * object if this method returns true, instead use "getLeftExpression()". 
     * @return
     *      true if this declaration is a multiple assignment declaration, which means the
     *      left hand side is an ArgumentListExpression. 
     */ 
    public boolean isMultipleAssignmentDeclaration() {
        return getLeftExpression() instanceof TupleExpression;
    }
}


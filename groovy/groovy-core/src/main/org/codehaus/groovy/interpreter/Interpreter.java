/*
 $Id$

 Copyright 2003 (C) The Codehaus. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.interpreter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.AssertStatement;
import org.codehaus.groovy.ast.BinaryExpression;
import org.codehaus.groovy.ast.BooleanExpression;
import org.codehaus.groovy.ast.ClosureExpression;
import org.codehaus.groovy.ast.ConstantExpression;
import org.codehaus.groovy.ast.DoWhileLoop;
import org.codehaus.groovy.ast.Expression;
import org.codehaus.groovy.ast.ExpressionStatement;
import org.codehaus.groovy.ast.FieldExpression;
import org.codehaus.groovy.ast.ForLoop;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.IfElse;
import org.codehaus.groovy.ast.ListExpression;
import org.codehaus.groovy.ast.MapEntryExpression;
import org.codehaus.groovy.ast.MapExpression;
import org.codehaus.groovy.ast.MethodCallExpression;
import org.codehaus.groovy.ast.PropertyExpression;
import org.codehaus.groovy.ast.RangeExpression;
import org.codehaus.groovy.ast.RegexExpression;
import org.codehaus.groovy.ast.ReturnStatement;
import org.codehaus.groovy.ast.Statement;
import org.codehaus.groovy.ast.TryCatchFinally;
import org.codehaus.groovy.ast.TupleExpression;
import org.codehaus.groovy.ast.VariableExpression;
import org.codehaus.groovy.ast.WhileLoop;
import org.codehaus.groovy.lang.Range;
import org.codehaus.groovy.runtime.Invoker;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.syntax.Token;

/**
 * Represents a standard for loop in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Interpreter implements GroovyCodeVisitor {

    private RuntimeContext context;
    private Invoker invoker = new Invoker();
    private LinkedList expressionStack = new LinkedList();

    public Interpreter(RuntimeContext context) {
        this.context = context;
    }

    /**
     * @param expression
     * @return
     */
    public Iterator createIterator(Expression expression) {
        Object value = evaluate(expression);
        return invoker.asIterator(value);
    }

    /**
     * Evaluates the given expression returning the value
     * 
     * @param expression to be evaluated
     * @return the value of the expression
     */
    public Object evaluate(Expression expression) {
        expression.visit(this);

        // now lets pop the value
        return popExpressionValue();
    }

    /**
     * Evaluates the given expression as a boolean value
     * 
     * @param expression
     * @return the boolean value
     */
    public boolean evaluateBoolean(BooleanExpression expression) {
        Object value = evaluate(expression.getExpression());

        Boolean booleanValue = invoker.asBoolean(value);
        return booleanValue.booleanValue();
    }

    /**
     * Evaluates the given expression as an int value
     */
    public int evaluateInt(Expression expression) {
        Object value = evaluate(expression);
        return InvokerHelper.asInt(value);
    }


    // Statements
    //-------------------------------------------------------------------------

    public void visitExpressionStatement(ExpressionStatement statement) {
        evaluate(statement.getExpression());
    }

    public void visitIfElse(IfElse ifElse) {
        if (evaluateBoolean(ifElse.getBooleanExpression())) {
            ifElse.getIfBlock().visit(this);
        }
        else {
            ifElse.getElseBlock().visit(this);
        }
    }

    public void visitForLoop(ForLoop forLoop) {
        Statement block = forLoop.getLoopBlock();
        Iterator iter = createIterator(forLoop.getCollectionExpression());
        while (iter.hasNext()) {
            context.setVariable(forLoop.getVariable(), iter.next());
            block.visit(this);
        }
    }

    public void visitWhileLoop(WhileLoop loop) {
        Statement block = loop.getLoopBlock();
        BooleanExpression expression = loop.getBooleanExpression();
        while (evaluateBoolean(expression)) {
            block.visit(this);
        }
    }

    public void visitDoWhileLoop(DoWhileLoop loop) {
        Statement block = loop.getLoopBlock();
        BooleanExpression expression = loop.getBooleanExpression();
        do {
            block.visit(this);
        }
        while (evaluateBoolean(expression));
    }

    public void visitReturnStatement(ReturnStatement statement) {
    }

    public void visitAssertStatement(AssertStatement statement) {
        // TODO Auto-generated method stub

    }

    public void visitTryCatchFinally(TryCatchFinally finally1) {
        // TODO Auto-generated method stub

    }

    // Expressions
    //-------------------------------------------------------------------------

    public void visitBinaryExpression(BinaryExpression expression) {
        int compareToValue = 0;
        switch (expression.getOperation().getType()) {
            case Token.COMPARE_IDENTICAL :
                compareIdentical(expression);
                break;

            case Token.COMPARE_EQUAL :
                compareEqual(expression);
                break;

            case Token.COMPARE_NOT_EQUAL :
                compareNotEqual(expression);
                break;

            case Token.COMPARE_GREATER_THAN :
                compareToValue = compareTo(expression);
                pushBooleanExpressionValue(compareToValue > 0);
                break;

            case Token.COMPARE_GREATER_THAN_EQUAL :
                compareToValue = compareTo(expression);
                pushBooleanExpressionValue(compareToValue >= 0);
                break;

            case Token.COMPARE_LESS_THAN :
                compareToValue = compareTo(expression);
                pushBooleanExpressionValue(compareToValue < 0);
                break;

            case Token.COMPARE_LESS_THAN_EQUAL :
                compareToValue = compareTo(expression);
                pushBooleanExpressionValue(compareToValue <= 0);
                break;

            default :
                throw new InterpreterException("Operation: " + expression.getOperation() + " not supported");
        }
    }

    public void visitClosureExpression(ClosureExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitRegexExpression(RegexExpression expression) {
        pushExpressionValue(Pattern.compile(expression.getRegex()));
    }


    public void visitMethodCallExpression(MethodCallExpression call) {
        Object value = evaluate(call.getObjectExpression());
        if (value == null) {
            throw new InterpreterException(
                "Cannot invoke method: "
                    + call.getMethod()
                    + " on null object expression: "
                    + call.getObjectExpression());
        }
        Object arguments = evaluate(call.getArguments());

        try {
            Object answer = invoker.invokeMethod(value, call.getMethod(), arguments);
            pushExpressionValue(answer);
        }
        catch (InvokerInvocationException e) {
            throw new InterpreterException(
                "Exception occurred when invoking method: " + call.getMethod() + " on: " + value + " reason: " + e,
                e);
        }
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        Object value = evaluate(expression.getObjectExpression());
        if (value == null) {
            throw new InterpreterException(
                "Cannot invoke property: "
                    + expression.getProperty()
                    + " on null object expression: "
                    + expression.getObjectExpression());
        }
        try {
            Object answer = invoker.getProperty(value, expression.getProperty());
            pushExpressionValue(answer);
        }
        catch (InvokerInvocationException e) {
            throw new InterpreterException(
                "Exception occurred when invoking property: "
                    + expression.getProperty()
                    + " on: "
                    + value
                    + " reason: "
                    + e,
                e);
        }
    }

    public void visitRangeExpression(RangeExpression expression) {
        int from = evaluateInt(expression.getFrom());  
        int to = evaluateInt(expression.getTo());
        pushExpressionValue(new Range(from, to));
    }

    public void visitVariableExpression(VariableExpression expression) {
        Object value = context.getVariable(expression.getVariable());
        pushExpressionValue(value);
    }

    public void visitConstantExpression(ConstantExpression expression) {
        pushExpressionValue(expression.getValue());
    }

    public void visitFieldExpression(FieldExpression expression) {
    }

    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this);
    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
    }

    public void visitMapExpression(MapExpression expression) {
        /** @todo it'd probably be more efficient to just call put() on each entry */
        List expressions = expression.getMapEntryExpressions();
        Object[] values = new Object[expressions.size() * 2];
        int idx = 0;
        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            MapEntryExpression entryExpression = (MapEntryExpression) iter.next();
            values[idx++] = evaluate(entryExpression.getKeyExpression());
            values[idx++] = evaluate(entryExpression.getValueExpression());
        }
        pushExpressionValue(InvokerHelper.createMap(values));
    }

    public void visitTupleExpression(TupleExpression expression) {
        /** @todo it'd probably be more efficient to just call add() on each entry */
        List expressions = expression.getExpressions();
        Object[] values = new Object[expressions.size()];
        int idx = 0;
        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            values[idx++] = evaluate((Expression) iter.next());
        }
        pushExpressionValue(InvokerHelper.createTuple(values));
    }

    public void visitListExpression(ListExpression expression) {
        /** @todo it'd probably be more efficient to just call add() on each entry */
        List expressions = expression.getExpressions();
        Object[] values = new Object[expressions.size()];
        int idx = 0;
        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            values[idx++] = evaluate((Expression) iter.next());
        }
        pushExpressionValue(InvokerHelper.createList(values));
    }

    // Implementation methods
    //-------------------------------------------------------------------------


    /**
     * Performs a compareTo() on the two expressions and returns an integer comparison value
     * like the Comparable interface
     */
    protected int compareTo(BinaryExpression expression) {
        Object left = evaluate(expression.getLeftExpression());
        Object right = evaluate(expression.getRightExpression());
        return invoker.compareTo(left, right);
    }

    protected void compareIdentical(BinaryExpression expression) {
        Object left = evaluate(expression.getLeftExpression());
        Object right = evaluate(expression.getRightExpression());
        pushBooleanExpressionValue(left == right);
    }

    protected void compareEqual(BinaryExpression expression) {
        Object left = evaluate(expression.getLeftExpression());
        Object right = evaluate(expression.getRightExpression());
        pushBooleanExpressionValue(invoker.objectsEqual(left, right));
    }

    protected void compareNotEqual(BinaryExpression expression) {
        Object left = evaluate(expression.getLeftExpression());
        Object right = evaluate(expression.getRightExpression());
        pushBooleanExpressionValue(!invoker.objectsEqual(left, right));
    }

    /**
     * Pops an expression value off the stack
     * @return
     */
    protected synchronized Object popExpressionValue() {
        if (expressionStack.isEmpty()) {
            throw new InterpreterException("Interpreter error - expression static is empty!");
        }
        else {
            return expressionStack.removeFirst();
        }
    }

    protected void pushBooleanExpressionValue(boolean value) {
        pushExpressionValue((value) ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Pushes the expression value on to the expression stack
     * @param value
     */
    protected synchronized void pushExpressionValue(Object value) {
        expressionStack.add(value);
    }

}

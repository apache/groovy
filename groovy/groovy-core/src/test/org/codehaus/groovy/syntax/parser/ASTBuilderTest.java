/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package org.codehaus.groovy.syntax.parser;

import java.util.Iterator;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.Token;

/**
 * Test case for the AST builder
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ASTBuilderTest extends TestParserSupport {

    public void testStatementParsing() throws Exception {
        ModuleNode module =
            parse("import cheddar.cheese.Toast as Bread\n x = [1, 2, 3]; System.out.println(x)", "foo/Cheese.groovy");

        BlockStatement block = module.getStatementBlock();
        assertTrue("Contains some statements", !block.getStatements().isEmpty());

        //System.out.println("Statements: " + block.getStatements());
    }

    public void testBlock() throws Exception {
        ModuleNode module =
            parse("class Foo { void testMethod() { x = someMethod(); callMethod(x) } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 2, statement.getStatements().size());

        System.out.println(statement.getStatements());
    }

    public void testSubscript() throws Exception {
        ModuleNode module =
            parse("class Foo { void testMethod() { x = 1\n [1].each { println(it) }} }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 2, statement.getStatements().size());

        for (Iterator iter = statement.getStatements().iterator(); iter.hasNext();) {
            System.out.println(iter.next());
        }
    }

    public void testNewlinesInsideExpresssions() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { x = 1 +\n 5 * \n 2 / \n 5 } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        for (Iterator iter = statement.getStatements().iterator(); iter.hasNext();) {
            System.out.println(iter.next());
        }
    }

    public void testMethodCalls() throws Exception {
        ModuleNode module =
            parse(
                "class Foo { void testMethod() { array = getMockArguments()\n \n dummyMethod(array) } }",
                "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 2, statement.getStatements().size());

        for (Iterator iter = statement.getStatements().iterator(); iter.hasNext();) {
            System.out.println(iter.next());
        }
    }

    public void testSubscriptAssignment() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { x[12] = 'abc' } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) statement.getStatements().get(0);
        Expression exp = exprStmt.getExpression();
        System.out.println(exp);
        
        assertTrue(exp instanceof BinaryExpression);
        BinaryExpression binExpr = (BinaryExpression) exp;
        assertTrue("RHS is constant", binExpr.getRightExpression() instanceof ConstantExpression);

        Expression lhs = binExpr.getLeftExpression();
        assertTrue("LHS is binary expression", lhs instanceof BinaryExpression);

        BinaryExpression lhsBinExpr = (BinaryExpression) lhs;
        assertEquals(Token.LEFT_SQUARE_BRACKET, lhsBinExpr.getOperation().getType());

        assertTrue("Left of LHS is a variable", lhsBinExpr.getLeftExpression() instanceof VariableExpression);
        assertTrue("Right of LHS is a constant", lhsBinExpr.getRightExpression() instanceof ConstantExpression);

    }

    public void testNoReturn() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { x += 5 } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());

        ExpressionStatement exprStmt = (ExpressionStatement) statement.getStatements().get(0);
        Expression exp = exprStmt.getExpression();

        System.out.println("expr: " + exp);
    }

    public void testCastExpression() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { x = (Short) 5 } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());

        ExpressionStatement exprStmt = (ExpressionStatement) statement.getStatements().get(0);
        Expression exp = exprStmt.getExpression();

        System.out.println("expr: " + exp);
    }

    public void testTypedVariableExpression() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { Short x = 5 } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());

        ExpressionStatement exprStmt = (ExpressionStatement) statement.getStatements().get(0);
        Expression exp = exprStmt.getExpression();

        System.out.println("expr: " + exp);
    }

    public void testFullyQualifiedType() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { com.acme.Foo } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());

        ExpressionStatement exprStmt = (ExpressionStatement) statement.getStatements().get(0);
        Expression exp = exprStmt.getExpression();

        System.out.println("expr: " + exp);

        System.out.println("text: " + exp.getText());
    }

    public void testDoubleSubscript() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { x = foo[0][0] } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());

        ExpressionStatement exprStmt = (ExpressionStatement) statement.getStatements().get(0);
        Expression exp = exprStmt.getExpression();

        System.out.println("expr: " + exp);

        System.out.println("text: " + exp.getText());
    }

    public void testMethodCallWithDotAndNoParenthesis() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { foo.someMethod 1 } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());

        ExpressionStatement exprStmt = (ExpressionStatement) statement.getStatements().get(0);
        Expression exp = exprStmt.getExpression();

        System.out.println("expr: " + exp);

        System.out.println("text: " + exp.getText());
    }

    public void testMethodCallWithNoParenthesis() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { someMethod 1 } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());

        ExpressionStatement exprStmt = (ExpressionStatement) statement.getStatements().get(0);
        Expression exp = exprStmt.getExpression();

        System.out.println("expr: " + exp);

        System.out.println("text: " + exp.getText());
    }

    public void testSubscriptThenMethod() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { x = foo[0].foo() } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());

        ExpressionStatement exprStmt = (ExpressionStatement) statement.getStatements().get(0);
        Expression exp = exprStmt.getExpression();

        System.out.println("expr: " + exp);

        System.out.println("text: " + exp.getText());
    }

    public void testSubscriptThenOperation() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { foo[0] += 5 } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());

        ExpressionStatement exprStmt = (ExpressionStatement) statement.getStatements().get(0);
        Expression exp = exprStmt.getExpression();

        System.out.println("expr: " + exp);

        System.out.println("text: " + exp.getText());
    }
    
    
    public void testRodsBug() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { if (x) { String n = 'foo' } } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());

        IfStatement ifStmt = (IfStatement) statement.getStatements().get(0);
        BlockStatement trueStmt = (BlockStatement) ifStmt.getIfBlock();

        System.out.println("trueStmt: " + trueStmt);

        // ideally there would be 1 statement; though we're handling that in the verifier
        assertEquals(1, trueStmt.getStatements().size());
    }

    public void testStaticMethodCallBug() throws Exception {
        ModuleNode module =
            parse("class Foo { void testMethod() { ASTBuilderTest.mockHelperMethod() } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());
    }

    public void testInstanceofBug() throws Exception {
        ModuleNode module =
        parse("class Foo { void testMethod() { if (foo instanceof java.util.List) { println('hello') } } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());
        
        IfStatement ifStmt = (IfStatement) statement.getStatements().get(0);
        BinaryExpression exp = (BinaryExpression) ifStmt.getBooleanExpression().getExpression();
        
        System.out.println("exp: " + exp);
        
        Expression rhs = exp.getRightExpression();
        assertTrue("RHS should be a class expression", rhs instanceof ClassExpression);
        
        ClassExpression classExp = (ClassExpression) rhs;
        assertEquals("java.util.List", classExp.getType());
    }

    public void testMethodCallWithoutParensBug() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { println 3, 5 } }", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());
    }

    public void testReturnMethodClosure() throws Exception {
        ModuleNode module = parse("class Foo { void testMethod() { System.out.println\n}}", "Dummy.groovy");
        BlockStatement statement = getCode(module, "testMethod");

        assertEquals("Statements size: " + statement.getStatements(), 1, statement.getStatements().size());

        System.out.println(statement.getStatements());
    }

    public void testMethodWithArrayTypeParam() throws Exception {
        ModuleNode module = parse("class Foo { void main(String[] args) { println(args) } }", "Dummy.groovy");

        MethodNode method = getMethod(module, "main");

        System.out.println("Parameters: " + InvokerHelper.toString(method.getParameters()));
    }

    public static Object mockHelperMethod() {
        return "cheese";
    }

    protected BlockStatement getCode(ModuleNode module, String name) {
        MethodNode method = getMethod(module, name);

        BlockStatement statement = (BlockStatement) method.getCode();
        assertNotNull(statement);
        return statement;
    }

    protected MethodNode getMethod(ModuleNode module, String name) {
        assertEquals("class count", 1, module.getClasses().size());

        ClassNode node = (ClassNode) module.getClasses().get(0);

        assertNotNull(node);

        MethodNode method = node.getMethod(name);
        assertNotNull(method);
        return method;
    }
}

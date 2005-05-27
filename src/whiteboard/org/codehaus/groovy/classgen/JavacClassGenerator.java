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
package org.codehaus.groovy.classgen;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingClassException;
import groovy.lang.Reference;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
//import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
//import org.codehaus.groovy.ast.GroovyClassVisitor;
//import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Type;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NegationExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.RegexExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.syntax.parser.RuntimeParserException;
//import org.objectweb.asm.ClassVisitor;
//import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;
import com.sun.tools.javac.v8.tree.Tree;
import com.sun.tools.javac.v8.tree.TreeMaker;
import com.sun.tools.javac.v8.util.Name;
import com.sun.tools.javac.v8.util.List;
import com.sun.tools.javac.v8.code.Symbol;
import com.sun.tools.javac.v8.code.Scope;

/**
 * Generates Java class versions of Groovy classes using Sun's Javac Java AST model
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class JavacClassGenerator extends ClassGenerator {

    private Logger log = Logger.getLogger(getClass().getName());

    private Tree.Factory factory;
    private Tree.TopLevel topLevel;
    private Name.Table nameTable = new Name.Table();
    private Name sourceFileName;

    public JavacClassGenerator(GeneratorContext context, ClassLoader classLoader, String sourceFile) {
        super(classLoader);
        sourceFileName = nameTable.fromString(sourceFile);
    }


    // GroovyClassVisitor interface
    //-------------------------------------------------------------------------
//
//    public void visitClass(ClassNode classNode) {
//
//        //className = nameTable.fromString(classNode.getName());
//
//        Tree pid = null;
//        List defs = new List();
//        Symbol.PackageSymbol packageSymbol = null;
//
//        Symbol owner = null; /// new Symbol.ClassSymbol()
//        Scope namedImportScope = new Scope(owner);
//        Scope starImportScope = new Scope(owner);
//
//        topLevel = new Tree.TopLevel(pid, defs, sourceFileName, packageSymbol, namedImportScope, starImportScope);
//        factory = new TreeMaker(topLevel);
//
//
//
//        try {
//            syntheticStaticFields.clear();
//            this.classNode = classNode;
//            this.outermostClass = null;
//
//            //System.out.println("Generating class: " + classNode.getName());
//
//            // lets check that the classes are all valid
//            classNode.setSuperClass(checkValidType(classNode.getSuperClass(), classNode, "Must be a valid base class"));
//            String[] interfaces = classNode.getInterfaces();
//            for (int i = 0; i < interfaces.length; i++ ) {
//                interfaces[i] = checkValidType(interfaces[i], classNode, "Must be a valid interface name");
//            }
//
//
//            classNode.visitContents(this);
//
//            createSyntheticStaticFields();
//
//
//            factory.ClassDef();
//
//            for (Iterator iter = innerClasses.iterator(); iter.hasNext();) {
//                ClassNode innerClass = (ClassNode) iter.next();
//
//                /** TODO process innner classes */
//            }
//        }
//        catch (GroovyRuntimeException e) {
//            e.setModule(classNode.getModule());
//            throw e;
//        }
//    }
//
//    public void visitConstructor(ConstructorNode node) {
//        this.constructorNode = node;
//        this.methodNode = null;
//        this.variableScope = null;
//
//        visitParameters(node, node.getParameters());
//
//        findMutableVariables();
//        resetVariableStack(node.getParameters());
//
//        Statement code = node.getCode();
//        if (code != null) {
//            code.visit(this);
//        }
//        factory.MethodDef();
//    }
//
//    public void visitMethod(MethodNode node) {
//        this.constructorNode = null;
//        this.methodNode = node;
//        this.variableScope = null;
//
//        visitParameters(node, node.getParameters());
//
//        node.setReturnType(checkValidType(node.getReturnType(), node, "Must be a valid return type"));
//
//        findMutableVariables();
//        resetVariableStack(node.getParameters());
//
//        node.getCode().visit(this);
//        factory.MethodDef();
//    }
//
//    protected void visitParameters(ASTNode node, Parameter[] parameters) {
//        for (int i = 0, size = parameters.length; i < size; i++ ) {
//            visitParameter(node, parameters[i]);
//        }
//    }
//
//    protected void visitParameter(ASTNode node, Parameter parameter) {
//        if (! parameter.isDynamicType()) {
//            parameter.setType(checkValidType(parameter.getType(), node, "Must be a valid parameter class"));
//        }
//    }
//
//    public void visitField(FieldNode fieldNode) {
//        onLineNumber(fieldNode);
//
//        // lets check that the classes are all valid
//        fieldNode.setType(checkValidType(fieldNode.getType(), fieldNode, "Must be a valid field class for field: " + fieldNode.getName()));
//
//        //System.out.println("Visiting field: " + fieldNode.getName() + " on
//        // class: " + classNode.getName());
//
//        Object fieldValue = null;
//        Expression expression = fieldNode.getInitialValueExpression();
//        if (expression instanceof ConstantExpression) {
//            ConstantExpression constantExp = (ConstantExpression) expression;
//            Object value = constantExp.getValue();
//            if (isPrimitiveFieldType(fieldNode.getType())) {
//                // lets convert any primitive types
//                Class type = null;
//                try {
//                    type = loadClass(fieldNode.getType());
//                    fieldValue = InvokerHelper.asType(value, type);
//                }
//                catch (Exception e) {
//                    log.warning("Caught unexpected: " + e);
//                }
//            }
//        }
//
//    }
//
//    /**
//     * Creates a getter, setter and field
//     */
//    public void visitProperty(PropertyNode statement) {
//        onLineNumber(statement);
//        //this.propertyNode = statement;
//        this.methodNode = null;
//    }
//
//    // GroovyCodeVisitor interface
//    //-------------------------------------------------------------------------
//
//    // Statements
//    //-------------------------------------------------------------------------
//
//    public void visitForLoop(ForStatement loop) {
//        onLineNumber(loop);
//
//
//        factory.ForLoop()
//
//        //
//        // Declare the loop counter.
//
//        Type variableType = checkValidType(loop.getVariableType(), loop, "for loop variable");
//        Variable variable = defineVariable(loop.getVariable(), variableType, true);
//
//        if( isInScriptBody() ) {
//            variable.setProperty( true );
//        }
//
//
//        //
//        // Then initialize the iterator and generate the loop control
//
//        loop.getCollectionExpression().visit(this);
//
//        asIteratorMethod.call(cv);
//
//        final int iteratorIdx = defineVariable(createVariableName("iterator"), "java.util.Iterator", false).getIndex();
//        cv.visitVarInsn(ASTORE, iteratorIdx);
//
//        pushBlockScope();
//
//        Label continueLabel = scope.getContinueLabel();
//        cv.visitJumpInsn(GOTO, continueLabel);
//        Label label2 = new Label();
//        cv.visitLabel(label2);
//
//        BytecodeExpression expression = new BytecodeExpression() {
//            public void visit(GroovyCodeVisitor visitor) {
//                cv.visitVarInsn(ALOAD, iteratorIdx);
//
//                iteratorNextMethod.call(cv);
//            }
//        };
//
//        evaluateEqual( BinaryExpression.newAssignmentExpression(loop.getVariable(), expression) );
//
//
//        //
//        // Generate the loop body
//
//        loop.getLoopBlock().visit(this);
//
//
//        //
//        // Generate the loop tail
//
//        cv.visitLabel(continueLabel);
//        cv.visitVarInsn(ALOAD, iteratorIdx);
//
//        iteratorHasNextMethod.call(cv);
//
//        cv.visitJumpInsn(IFNE, label2);
//
//        cv.visitLabel(scope.getBreakLabel());
//        popScope();
//    }
//
//    public void visitWhileLoop(WhileStatement loop) {
//        onLineNumber(loop);
//
//        /*
//         * // quick hack if (!methodNode.isStatic()) { cv.visitVarInsn(ALOAD,
//         * 0); }
//         */
//
//        pushBlockScope();
//
//        Label continueLabel = scope.getContinueLabel();
//
//        cv.visitJumpInsn(GOTO, continueLabel);
//        Label l1 = new Label();
//        cv.visitLabel(l1);
//
//        loop.getLoopBlock().visit(this);
//
//        cv.visitLabel(continueLabel);
//        //cv.visitVarInsn(ALOAD, 0);
//
//        loop.getBooleanExpression().visit(this);
//
//        cv.visitJumpInsn(IFNE, l1);
//
//        cv.visitLabel(scope.getBreakLabel());
//        popScope();
//    }
//
//    public void visitDoWhileLoop(DoWhileStatement loop) {
//        onLineNumber(loop);
//
//        pushBlockScope();
//
//        Label breakLabel = scope.getBreakLabel();
//
//        Label continueLabel = scope.getContinueLabel();
//        cv.visitLabel(continueLabel);
//        Label l1 = new Label();
//
//        loop.getLoopBlock().visit(this);
//
//        cv.visitLabel(l1);
//
//        loop.getBooleanExpression().visit(this);
//
//        cv.visitJumpInsn(IFNE, continueLabel);
//
//        cv.visitLabel(breakLabel);
//        popScope();
//    }
//
//    public void visitIfElse(IfStatement ifElse) {
//        onLineNumber(ifElse);
//
//        ifElse.getBooleanExpression().visit(this);
//
//        Label l0 = new Label();
//        cv.visitJumpInsn(IFEQ, l0);
//        ifElse.getIfBlock().visit(this);
//
//        Label l1 = new Label();
//        cv.visitJumpInsn(GOTO, l1);
//        cv.visitLabel(l0);
//
//        ifElse.getElseBlock().visit(this);
//        cv.visitLabel(l1);
//    }
//
//    public void visitTernaryExpression(TernaryExpression expression) {
//        onLineNumber(expression);
//
//        expression.getBooleanExpression().visit(this);
//
//        Label l0 = new Label();
//        cv.visitJumpInsn(IFEQ, l0);
//        expression.getTrueExpression().visit(this);
//
//        Label l1 = new Label();
//        cv.visitJumpInsn(GOTO, l1);
//        cv.visitLabel(l0);
//
//        expression.getFalseExpression().visit(this);
//        cv.visitLabel(l1);
//    }
//
//    public void visitAssertStatement(AssertStatement statement) {
//        onLineNumber(statement);
//
//        //System.out.println("Assert: " + statement.getLineNumber() + " for: "
//        // + statement.getText());
//
//        BooleanExpression booleanExpression = statement.getBooleanExpression();
//        booleanExpression.visit(this);
//
//        Label l0 = new Label();
//        cv.visitJumpInsn(IFEQ, l0);
//
//        // do nothing
//
//        Label l1 = new Label();
//        cv.visitJumpInsn(GOTO, l1);
//        cv.visitLabel(l0);
//
//        // push expression string onto stack
//        String expressionText = booleanExpression.getText();
//        List list = new ArrayList();
//        addVariableNames(booleanExpression, list);
//        if (list.isEmpty()) {
//            cv.visitLdcInsn(expressionText);
//        }
//        else {
//            boolean first = true;
//
//            // lets create a new expression
//            cv.visitTypeInsn(NEW, "java/lang/StringBuffer");
//            cv.visitInsn(DUP);
//            cv.visitLdcInsn(expressionText + ". Values: ");
//
//            cv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "(Ljava/lang/String;)V");
//
//            int tempIndex = defineVariable(createVariableName("assert"), "java.lang.Object", false).getIndex();
//
//            cv.visitVarInsn(ASTORE, tempIndex);
//
//            for (Iterator iter = list.iterator(); iter.hasNext();) {
//                String name = (String) iter.next();
//                String text = name + " = ";
//                if (first) {
//                    first = false;
//                }
//                else {
//                    text = ", " + text;
//                }
//
//                cv.visitVarInsn(ALOAD, tempIndex);
//                cv.visitLdcInsn(text);
//                cv.visitMethodInsn(
//                    INVOKEVIRTUAL,
//                    "java/lang/StringBuffer",
//                    "append",
//                    "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
//                cv.visitInsn(POP);
//
//                cv.visitVarInsn(ALOAD, tempIndex);
//                new VariableExpression(name).visit(this);
//                cv.visitMethodInsn(
//                    INVOKEVIRTUAL,
//                    "java/lang/StringBuffer",
//                    "append",
//                    "(Ljava/lang/Object;)Ljava/lang/StringBuffer;");
//                cv.visitInsn(POP);
//
//            }
//            cv.visitVarInsn(ALOAD, tempIndex);
//        }
//
//        // now the optional exception expression
//        statement.getMessageExpression().visit(this);
//
//        assertFailedMethod.call(cv);
//        cv.visitLabel(l1);
//    }
//
//    public void visitTryCatchFinally(TryCatchStatement statement) {
//        onLineNumber(statement);
//
//        CatchStatement catchStatement = statement.getCatchStatement(0);
//
//        Statement tryStatement = statement.getTryStatement();
//
//        if (tryStatement.isEmpty() || catchStatement == null) {
//            final Label l0 = new Label();
//            cv.visitLabel(l0);
//
//            tryStatement.visit(this);
//
//            int index1 = defineVariable(this.createVariableName("exception"), "java.lang.Object").getIndex();
//            int index2 = defineVariable(this.createVariableName("exception"), "java.lang.Object").getIndex();
//
//            final Label l1 = new Label();
//            cv.visitJumpInsn(JSR, l1);
//            final Label l2 = new Label();
//            cv.visitLabel(l2);
//            final Label l3 = new Label();
//            cv.visitJumpInsn(GOTO, l3);
//            final Label l4 = new Label();
//            cv.visitLabel(l4);
//            cv.visitVarInsn(ASTORE, index1);
//            cv.visitJumpInsn(JSR, l1);
//            final Label l5 = new Label();
//            cv.visitLabel(l5);
//            cv.visitVarInsn(ALOAD, index1);
//            cv.visitInsn(ATHROW);
//            cv.visitLabel(l1);
//            cv.visitVarInsn(ASTORE, index2);
//
//            statement.getFinallyStatement().visit(this);
//
//            cv.visitVarInsn(RET, index2);
//            cv.visitLabel(l3);
//
//            exceptionBlocks.add(new Runnable() {
//                public void run() {
//                    cv.visitTryCatchBlock(l0, l2, l4, null);
//                    cv.visitTryCatchBlock(l4, l5, l4, null);
//                }
//            });
//
//        }
//        else {
//            String exceptionVar = catchStatement.getVariable();
//            String exceptionType =
//                checkValidType(catchStatement.getExceptionType(), catchStatement, "in catch statement");
//
//            int exceptionIndex = defineVariable(exceptionVar, exceptionType, false).getIndex();
//            int index2 = defineVariable(this.createVariableName("exception"), "java.lang.Object").getIndex();
//            int index3 = defineVariable(this.createVariableName("exception"), "java.lang.Object").getIndex();
//
//            final Label l0 = new Label();
//            cv.visitLabel(l0);
//
//            tryStatement.visit(this);
//
//            final Label l1 = new Label();
//            cv.visitLabel(l1);
//            Label l2 = new Label();
//            cv.visitJumpInsn(JSR, l2);
//            final Label l3 = new Label();
//            cv.visitLabel(l3);
//            Label l4 = new Label();
//            cv.visitJumpInsn(GOTO, l4);
//            final Label l5 = new Label();
//            cv.visitLabel(l5);
//
//            cv.visitVarInsn(ASTORE, exceptionIndex);
//
//            if (catchStatement != null) {
//                catchStatement.visit(this);
//            }
//
//            cv.visitJumpInsn(JSR, l2);
//            final Label l6 = new Label();
//            cv.visitLabel(l6);
//            cv.visitJumpInsn(GOTO, l4);
//
//            final Label l7 = new Label();
//            cv.visitLabel(l7);
//            cv.visitVarInsn(ASTORE, index2);
//            cv.visitJumpInsn(JSR, l2);
//
//            final Label l8 = new Label();
//            cv.visitLabel(l8);
//            cv.visitVarInsn(ALOAD, index2);
//            cv.visitInsn(ATHROW);
//            cv.visitLabel(l2);
//            cv.visitVarInsn(ASTORE, index3);
//
//            statement.getFinallyStatement().visit(this);
//
//            cv.visitVarInsn(RET, index3);
//            cv.visitLabel(l4);
//
//            // rest of code goes here...
//
//            //final String exceptionTypeInternalName = (catchStatement !=
//            // null) ?
//            // getTypeDescription(exceptionType) : null;
//            final String exceptionTypeInternalName =
//                (catchStatement != null) ? BytecodeHelper.getClassInternalName(exceptionType) : null;
//
//            exceptionBlocks.add(new Runnable() {
//                public void run() {
//                    cv.visitTryCatchBlock(l0, l1, l5, exceptionTypeInternalName);
//                    cv.visitTryCatchBlock(l0, l3, l7, null);
//                    cv.visitTryCatchBlock(l5, l6, l7, null);
//                    cv.visitTryCatchBlock(l7, l8, l7, null);
//                }
//            });
//        }
//    }
//
//    public void visitSwitch(SwitchStatement statement) {
//        onLineNumber(statement);
//
//        statement.getExpression().visit(this);
//
//        pushBlockScope();
//
//        int switchVariableIndex = defineVariable(createVariableName("switch"), "java.lang.Object").getIndex();
//        cv.visitVarInsn(ASTORE, switchVariableIndex);
//
//        List caseStatements = statement.getCaseStatements();
//        int caseCount = caseStatements.size();
//        Label[] labels = new Label[caseCount + 1];
//        for (int i = 0; i < caseCount; i++) {
//            labels[i] = new Label();
//        }
//
//        int i = 0;
//        for (Iterator iter = caseStatements.iterator(); iter.hasNext(); i++) {
//            CaseStatement caseStatement = (CaseStatement) iter.next();
//            visitCaseStatement(caseStatement, switchVariableIndex, labels[i], labels[i + 1]);
//        }
//
//        statement.getDefaultStatement().visit(this);
//
//        cv.visitLabel(scope.getBreakLabel());
//
//        popScope();
//    }
//
//    public void visitCaseStatement(CaseStatement statement) {
//    }
//
//    public void visitCaseStatement(
//        CaseStatement statement,
//        int switchVariableIndex,
//        Label thisLabel,
//        Label nextLabel) {
//
//        onLineNumber(statement);
//
//        cv.visitVarInsn(ALOAD, switchVariableIndex);
//        statement.getExpression().visit(this);
//
//        isCaseMethod.call(cv);
//
//        Label l0 = new Label();
//        cv.visitJumpInsn(IFEQ, l0);
//
//        cv.visitLabel(thisLabel);
//
//        statement.getCode().visit(this);
//
//        // now if we don't finish with a break we need to jump past
//        // the next comparison
//        if (nextLabel != null) {
//            cv.visitJumpInsn(GOTO, nextLabel);
//        }
//
//        cv.visitLabel(l0);
//    }
//
//    public void visitBreakStatement(BreakStatement statement) {
//        onLineNumber(statement);
//
//        cv.visitJumpInsn(GOTO, scope.getBreakLabel());
//    }
//
//    public void visitContinueStatement(ContinueStatement statement) {
//        onLineNumber(statement);
//
//        cv.visitJumpInsn(GOTO, scope.getContinueLabel());
//    }
//
//    public void visitSynchronizedStatement(SynchronizedStatement statement) {
//        onLineNumber(statement);
//
//        statement.getExpression().visit(this);
//
//        int index = defineVariable(createVariableName("synchronized"), "java.lang.Integer").getIndex();
//
//        cv.visitVarInsn(ASTORE, index);
//        cv.visitInsn(MONITORENTER);
//        final Label l0 = new Label();
//        cv.visitLabel(l0);
//
//        statement.getCode().visit(this);
//
//        cv.visitVarInsn(ALOAD, index);
//        cv.visitInsn(MONITOREXIT);
//        final Label l1 = new Label();
//        cv.visitJumpInsn(GOTO, l1);
//        final Label l2 = new Label();
//        cv.visitLabel(l2);
//        cv.visitVarInsn(ALOAD, index);
//        cv.visitInsn(MONITOREXIT);
//        cv.visitInsn(ATHROW);
//        cv.visitLabel(l1);
//
//        exceptionBlocks.add(new Runnable() {
//            public void run() {
//                cv.visitTryCatchBlock(l0, l2, l2, null);
//            }
//        });
//    }
//
//    public void visitThrowStatement(ThrowStatement statement) {
//        statement.getExpression().visit(this);
//
//        // we should infer the type of the exception from the expression
//        cv.visitTypeInsn(CHECKCAST, "java/lang/Throwable");
//
//        cv.visitInsn(ATHROW);
//    }
//
//    public void visitReturnStatement(ReturnStatement statement) {
//        onLineNumber(statement);
//
//        Expression expression = statement.getExpression();
//        evaluateExpression(expression);
//
//        //return is based on class type
//        //TODO: make work with arrays
//        // we may need to cast
//        String returnType = methodNode.getReturnType();
//        helper.unbox(returnType);
//        if (returnType.equals("double")) {
//            cv.visitInsn(DRETURN);
//        }
//        else if (returnType.equals("float")) {
//            cv.visitInsn(FRETURN);
//        }
//        else if (returnType.equals("long")) {
//            cv.visitInsn(LRETURN);
//        }
//        else if (returnType.equals("boolean")) {
//            cv.visitInsn(IRETURN);
//        }
//        else if (
//            returnType.equals("char")
//                || returnType.equals("byte")
//                || returnType.equals("int")
//                || returnType.equals("short")) { //byte,short,boolean,int are
//            // all IRETURN
//            cv.visitInsn(IRETURN);
//        }
//        else {
//            doConvertAndCast(returnType, expression);
//            cv.visitInsn(ARETURN);
//
//            /*
//            if (c == Boolean.class) {
//                Label l0 = new Label();
//                cv.visitJumpInsn(IFEQ, l0);
//                cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
//                cv.visitInsn(ARETURN);
//                cv.visitLabel(l0);
//                cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
//                cv.visitInsn(ARETURN);
//            }
//            else {
//                if (isValidTypeForCast(returnType) && !returnType.equals(c.getName())) {
//                    doConvertAndCast(returnType, expression);
//                }
//                cv.visitInsn(ARETURN);
//            }
//            */
//        }
//
//        outputReturn = true;
//    }


}

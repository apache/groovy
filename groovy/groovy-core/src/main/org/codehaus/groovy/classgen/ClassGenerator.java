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
import groovy.lang.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.RegexExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
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
import org.codehaus.groovy.runtime.InvokerException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.NoSuchClassException;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * Generates Java class versions of Groovy classes
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ClassGenerator implements GroovyClassVisitor, GroovyCodeVisitor, Constants {

    private ClassVisitor cw;
    private ClassLoader classLoader;
    private CodeVisitor cv;
    private GeneratorContext context;

    private String sourceFile;

    // current class details
    private ClassNode classNode;
    private String internalClassName;
    private String internalBaseClassName;

    /** maps the variable names to the JVM indices */
    private Map variableStack = new HashMap();

    /** have we output a return statement yet */
    private boolean outputReturn;

    /** are we on the left or right of an expression */
    private boolean leftHandExpression;

    // cached values
    MethodCaller invokeMethodMethod = MethodCaller.newStatic(InvokerHelper.class, "invokeMethod");
    MethodCaller invokeMethodSafeMethod = MethodCaller.newStatic(InvokerHelper.class, "invokeMethodSafe");
    MethodCaller invokeStaticMethodMethod = MethodCaller.newStatic(InvokerHelper.class, "invokeStaticMethod");
    MethodCaller invokeConstructorMethod = MethodCaller.newStatic(InvokerHelper.class, "invokeConstructor");
    MethodCaller invokeConstructorOfMethod = MethodCaller.newStatic(InvokerHelper.class, "invokeConstructorOf");
    MethodCaller invokeClosureMethod = MethodCaller.newStatic(InvokerHelper.class, "invokeClosure");
    MethodCaller getPropertyMethod = MethodCaller.newStatic(InvokerHelper.class, "getProperty");
    MethodCaller getPropertySafeMethod = MethodCaller.newStatic(InvokerHelper.class, "getPropertySafe");
    MethodCaller setPropertyMethod = MethodCaller.newStatic(InvokerHelper.class, "setProperty");
    MethodCaller setPropertyMethod2 = MethodCaller.newStatic(InvokerHelper.class, "setProperty2");
    MethodCaller setPropertySafeMethod2 = MethodCaller.newStatic(InvokerHelper.class, "setPropertySafe2");
    MethodCaller asIteratorMethod = MethodCaller.newStatic(InvokerHelper.class, "asIterator");
    MethodCaller asBool = MethodCaller.newStatic(InvokerHelper.class, "asBool");
    MethodCaller notBoolean = MethodCaller.newStatic(InvokerHelper.class, "notBoolean");
    MethodCaller notObject = MethodCaller.newStatic(InvokerHelper.class, "notObject");
    MethodCaller regexPattern = MethodCaller.newStatic(InvokerHelper.class, "regexPattern");

    MethodCaller compareIdenticalMethod = MethodCaller.newStatic(InvokerHelper.class, "compareIdentical");
    MethodCaller compareEqualMethod = MethodCaller.newStatic(InvokerHelper.class, "compareEqual");
    MethodCaller compareNotEqualMethod = MethodCaller.newStatic(InvokerHelper.class, "compareNotEqual");
    MethodCaller findRegexMethod = MethodCaller.newStatic(InvokerHelper.class, "findRegex");
    MethodCaller matchRegexMethod = MethodCaller.newStatic(InvokerHelper.class, "matchRegex");
    MethodCaller compareLessThanMethod = MethodCaller.newStatic(InvokerHelper.class, "compareLessThan");
    MethodCaller compareLessThanEqualMethod = MethodCaller.newStatic(InvokerHelper.class, "compareLessThanEqual");
    MethodCaller compareGreaterThanMethod = MethodCaller.newStatic(InvokerHelper.class, "compareGreaterThan");
    MethodCaller compareGreaterThanEqualMethod = MethodCaller.newStatic(InvokerHelper.class, "compareGreaterThanEqual");
    MethodCaller isCaseMethod = MethodCaller.newStatic(InvokerHelper.class, "isCase");

    MethodCaller createListMethod = MethodCaller.newStatic(InvokerHelper.class, "createList");
    MethodCaller createTupleMethod = MethodCaller.newStatic(InvokerHelper.class, "createTuple");
    MethodCaller createMapMethod = MethodCaller.newStatic(InvokerHelper.class, "createMap");
    MethodCaller createRangeMethod = MethodCaller.newStatic(InvokerHelper.class, "createRange");

    MethodCaller assertFailedMethod = MethodCaller.newStatic(InvokerHelper.class, "assertFailed");

    MethodCaller iteratorNextMethod = MethodCaller.newInterface(Iterator.class, "next");
    MethodCaller iteratorHasNextMethod = MethodCaller.newInterface(Iterator.class, "hasNext");

    // current stack index
    private int idx;

    // exception blocks list
    private List exceptionBlocks = new ArrayList();

    // inner classes created while generating bytecode
    private LinkedList innerClasses = new LinkedList();
    private boolean definingParameters;
    private Set syntheticStaticFields = new HashSet();
    private int lastVariableIndex;
    private Label breakLabel;
    private Label continueLabel;
    private Set mutableVars = new HashSet();
    private boolean passingClosureParams;

    private ConstructorNode constructorNode;
    private MethodNode methodNode;

    public ClassGenerator(
        GeneratorContext context,
        ClassVisitor classVisitor,
        ClassLoader classLoader,
        String sourceFile) {
        this.context = context;
        this.cw = classVisitor;
        this.classLoader = classLoader;
        this.sourceFile = sourceFile;
    }

    public LinkedList getInnerClasses() {
        return innerClasses;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    // GroovyClassVisitor interface
    //-------------------------------------------------------------------------
    public void visitClass(ClassNode classNode) {
        try {
            syntheticStaticFields.clear();
            this.classNode = classNode;
            this.internalClassName = getClassInternalName(classNode.getName());
            this.internalBaseClassName = getClassInternalName(classNode.getSuperClass());

            cw.visit(
                classNode.getModifiers(),
                internalClassName,
                internalBaseClassName,
                getClassInternalNames(classNode.getInterfaces()),
                sourceFile);

            classNode.visitContents(this);

            createSyntheticStaticFields();

            for (Iterator iter = innerClasses.iterator(); iter.hasNext();) {
                ClassNode innerClass = (ClassNode) iter.next();
                String innerClassName = innerClass.getName();
                String innerClassInternalName = getClassInternalName(innerClassName);
                cw.visitInnerClass(
                    innerClassInternalName,
                    internalClassName,
                    innerClassName,
                    innerClass.getModifiers());
            }

            cw.visitEnd();
        }
        catch (InvokerException e) {
            e.setModule(classNode.getModule());
            throw e;
        }
    }

    public void visitConstructor(ConstructorNode node) {
        // creates a MethodWriter for the (implicit) constructor
        //String methodType = Type.getMethodDescriptor(VOID_TYPE, )

        this.constructorNode = node;
        this.methodNode = null;

        String methodType = getMethodDescriptor("void", node.getParameters());
        cv = cw.visitMethod(node.getModifiers(), "<init>", methodType, null);

        resetVariableStack(node.getParameters());

        Statement code = node.getCode();
        if (code == null || !firstStatementIsSuperMethodCall(code)) {
            // invokes the super class constructor
            cv.visitVarInsn(ALOAD, 0);
            cv.visitMethodInsn(INVOKESPECIAL, internalBaseClassName, "<init>", "()V");
        }
        if (code != null) {
            code.visit(this);
        }

        cv.visitInsn(RETURN);
        cv.visitMaxs(0, 0);
    }

    public void visitMethod(MethodNode node) {
        //System.out.println("Visiting method: " + node.getName() + " with
        // return type: " + node.getReturnType());
        this.constructorNode = null;
        this.methodNode = node;

        String methodType = getMethodDescriptor(node.getReturnType(), node.getParameters());
        cv = cw.visitMethod(node.getModifiers(), node.getName(), methodType, null);

        resetVariableStack(node.getParameters());

        findMutableVariables(node);

        outputReturn = false;

        node.getCode().visit(this);

        if (!outputReturn) {
            cv.visitInsn(RETURN);
        }

        // lets do all the exception blocks
        for (Iterator iter = exceptionBlocks.iterator(); iter.hasNext();) {
            Runnable runnable = (Runnable) iter.next();
            runnable.run();
        }
        exceptionBlocks.clear();

        cv.visitMaxs(0, 0);
    }

    public void visitField(FieldNode fieldNode) {
        onLineNumber(fieldNode);

        //System.out.println("Visiting field: " + fieldNode.getName() + " on
        // class: " + classNode.getName());

        Object fieldValue = null;
        Expression expression = fieldNode.getInitialValueExpression();
        if (expression instanceof ConstantExpression) {
            ConstantExpression constantExp = (ConstantExpression) expression;
            Object value = constantExp.getValue();
            if (isPrimitiveFieldType(value)) {
                fieldValue = value;
            }
        }
        cw.visitField(
            fieldNode.getModifiers(),
            fieldNode.getName(),
            getTypeDescription(fieldNode.getType()),
            fieldValue);
    }

    /**
     * Creates a getter, setter and field
     */
    public void visitProperty(PropertyNode statement) {
        onLineNumber(statement);
    }

    // GroovyCodeVisitor interface
    //-------------------------------------------------------------------------

    // Statements
    //-------------------------------------------------------------------------

    public void visitForLoop(ForStatement loop) {
        onLineNumber(loop);

        loop.getCollectionExpression().visit(this);

        asIteratorMethod.call(cv);

        int iteratorIdx = defineVariable(createVariableName("iterator"), "java.util.Iterator", false).getIndex();
        cv.visitVarInsn(ASTORE, iteratorIdx);

        int iIdx = defineVariable(loop.getVariable(), "java.lang.Object", false).getIndex();

        Label label1 = new Label();
        Label previousContinueLable = continueLabel;
        continueLabel = label1;

        cv.visitJumpInsn(GOTO, label1);
        Label label2 = new Label();
        cv.visitLabel(label2);

        cv.visitVarInsn(ALOAD, iteratorIdx);

        iteratorNextMethod.call(cv);

        cv.visitVarInsn(ASTORE, iIdx);

        loop.getLoopBlock().visit(this);

        cv.visitLabel(label1);
        cv.visitVarInsn(ALOAD, iteratorIdx);

        iteratorHasNextMethod.call(cv);

        cv.visitJumpInsn(IFNE, label2);

        continueLabel = previousContinueLable;
    }

    public void visitWhileLoop(WhileStatement loop) {
        onLineNumber(loop);

        /*
        // quick hack
        if (!methodNode.isStatic()) {
            cv.visitVarInsn(ALOAD, 0);
        }
        */

        Label l0 = new Label();
        Label previousContinueLable = continueLabel;
        continueLabel = l0;

        cv.visitJumpInsn(GOTO, l0);
        Label l1 = new Label();
        cv.visitLabel(l1);

        loop.getLoopBlock().visit(this);

        cv.visitLabel(l0);
        //cv.visitVarInsn(ALOAD, 0);

        loop.getBooleanExpression().visit(this);

        cv.visitJumpInsn(IFNE, l1);

        continueLabel = previousContinueLable;
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        onLineNumber(loop);

        Label l0 = new Label();
        cv.visitLabel(l0);
        Label l1 = new Label();
        Label previousContinueLable = continueLabel;
        continueLabel = l1;

        loop.getLoopBlock().visit(this);

        cv.visitLabel(l1);

        loop.getBooleanExpression().visit(this);

        cv.visitJumpInsn(IFNE, l0);

        continueLabel = previousContinueLable;
    }

    public void visitIfElse(IfStatement ifElse) {
        onLineNumber(ifElse);

        ifElse.getBooleanExpression().visit(this);

        Label l0 = new Label();
        cv.visitJumpInsn(IFEQ, l0);
        //cv.visitVarInsn(ALOAD, 0);
        ifElse.getIfBlock().visit(this);

        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);

        ifElse.getElseBlock().visit(this);
        cv.visitLabel(l1);
    }

    public void visitAssertStatement(AssertStatement statement) {
        onLineNumber(statement);

        //System.out.println("Assert: " + statement.getLineNumber() + " for: " + statement.getText());

        BooleanExpression booleanExpression = statement.getBooleanExpression();
        booleanExpression.visit(this);

        Label l0 = new Label();
        cv.visitJumpInsn(IFEQ, l0);

        // do nothing

        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);

        // push expression string onto stack
        String expressionText = booleanExpression.getText();
        List list = new ArrayList();
        addVariableNames(booleanExpression, list);
        if (list.isEmpty()) {
            cv.visitLdcInsn(expressionText);
        }
        else {
            boolean first = true;

            // lets create a new expression
            cv.visitTypeInsn(NEW, "java/lang/StringBuffer");
            cv.visitInsn(DUP);
            cv.visitLdcInsn(expressionText + ". Values: ");

            cv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "(Ljava/lang/String;)V");

            int tempIndex = defineVariable(createVariableName("assert"), "java.lang.Object", false).getIndex();

            cv.visitVarInsn(ASTORE, tempIndex);

            for (Iterator iter = list.iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String text = name + " = ";
                if (first) {
                    first = false;
                }
                else {
                    text = ", " + text;
                }

                cv.visitVarInsn(ALOAD, tempIndex);
                cv.visitLdcInsn(text);
                cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "java/lang/StringBuffer",
                    "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
                cv.visitInsn(POP);

                cv.visitVarInsn(ALOAD, tempIndex);
                new VariableExpression(name).visit(this);
                cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "java/lang/StringBuffer",
                    "append",
                    "(Ljava/lang/Object;)Ljava/lang/StringBuffer;");
                cv.visitInsn(POP);

            }
            cv.visitVarInsn(ALOAD, tempIndex);
        }

        // now the optional exception expression
        statement.getMessageExpression().visit(this);

        assertFailedMethod.call(cv);
        cv.visitLabel(l1);
    }

    private void addVariableNames(Expression expression, List list) {
        if (expression instanceof BooleanExpression) {
            BooleanExpression boolExp = (BooleanExpression) expression;
            addVariableNames(boolExp.getExpression(), list);
        }
        else if (expression instanceof BinaryExpression) {
            BinaryExpression binExp = (BinaryExpression) expression;
            addVariableNames(binExp.getLeftExpression(), list);
            addVariableNames(binExp.getRightExpression(), list);
        }
        else if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            list.add(varExp.getVariable());
        }
    }

    public void visitTryCatchFinally(TryCatchStatement statement) {
        onLineNumber(statement);

        CatchStatement catchStatement = statement.getCatchStatement(0);

        String exceptionType = checkValidType(catchStatement.getExceptionType(), catchStatement, "in catch statement");

        String exceptionVar = (catchStatement != null) ? catchStatement.getVariable() : createVariableName("exception");

        int exceptionIndex = defineVariable(exceptionVar, exceptionType, false).getIndex();
        int index2 = exceptionIndex + 1;
        int index3 = index2 + 1;

        final Label l0 = new Label();
        cv.visitLabel(l0);

        statement.getTryStatement().visit(this);

        final Label l1 = new Label();
        cv.visitLabel(l1);
        Label l2 = new Label();
        cv.visitJumpInsn(JSR, l2);
        final Label l3 = new Label();
        cv.visitLabel(l3);
        Label l4 = new Label();
        cv.visitJumpInsn(GOTO, l4);
        final Label l5 = new Label();
        cv.visitLabel(l5);

        cv.visitVarInsn(ASTORE, exceptionIndex);

        if (catchStatement != null) {
            catchStatement.visit(this);
        }

        cv.visitJumpInsn(JSR, l2);
        final Label l6 = new Label();
        cv.visitLabel(l6);
        cv.visitJumpInsn(GOTO, l4);

        final Label l7 = new Label();
        cv.visitLabel(l7);
        cv.visitVarInsn(ASTORE, index2);
        cv.visitJumpInsn(JSR, l2);

        final Label l8 = new Label();
        cv.visitLabel(l8);
        cv.visitVarInsn(ALOAD, index2);
        cv.visitInsn(ATHROW);
        cv.visitLabel(l2);
        cv.visitVarInsn(ASTORE, index3);

        statement.getFinallyStatement().visit(this);

        cv.visitVarInsn(RET, index3);
        cv.visitLabel(l4);

        // rest of code goes here...

        //final String exceptionTypeInternalName = (catchStatement != null) ? getTypeDescription(exceptionType) : null;
        final String exceptionTypeInternalName = (catchStatement != null) ? getClassInternalName(exceptionType) : null;

        exceptionBlocks.add(new Runnable() {
            public void run() {
                cv.visitTryCatchBlock(l0, l1, l5, exceptionTypeInternalName);
                cv.visitTryCatchBlock(l0, l3, l7, null);
                cv.visitTryCatchBlock(l5, l6, l7, null);
                cv.visitTryCatchBlock(l7, l8, l7, null);
            }
        });
    }

    public void visitSwitch(SwitchStatement statement) {
        onLineNumber(statement);

        statement.getExpression().visit(this);

        int switchVariableIndex = defineVariable(createVariableName("switch"), "java.lang.Object").getIndex();
        cv.visitVarInsn(ASTORE, switchVariableIndex);

        Label lastBreakLabel = breakLabel;

        breakLabel = new Label();

        List caseStatements = statement.getCaseStatements();
        int caseCount = caseStatements.size();
        Label[] labels = new Label[caseCount + 1];
        for (int i = 0; i < caseCount; i++) {
            labels[i] = new Label();
        }

        int i = 0;
        for (Iterator iter = caseStatements.iterator(); iter.hasNext(); i++) {
            CaseStatement caseStatement = (CaseStatement) iter.next();
            visitCaseStatement(caseStatement, switchVariableIndex, labels[i], labels[i + 1]);
        }

        statement.getDefaultStatement().visit(this);

        cv.visitLabel(breakLabel);

        // lets restore the break label
        breakLabel = lastBreakLabel;
    }

    public void visitCaseStatement(CaseStatement statement) {
    }

    public void visitCaseStatement(
        CaseStatement statement,
        int switchVariableIndex,
        Label thisLabel,
        Label nextLabel) {

        onLineNumber(statement);

        cv.visitVarInsn(ALOAD, switchVariableIndex);
        statement.getExpression().visit(this);

        isCaseMethod.call(cv);

        Label l0 = new Label();
        cv.visitJumpInsn(IFEQ, l0);

        cv.visitLabel(thisLabel);

        statement.getCode().visit(this);

        // now if we don't finish with a break we need to jump past 
        // the next comparison
        if (nextLabel != null) {
            cv.visitJumpInsn(GOTO, nextLabel);
        }

        cv.visitLabel(l0);
    }

    public void visitBreakStatement(BreakStatement statement) {
        onLineNumber(statement);

        cv.visitJumpInsn(GOTO, breakLabel);
    }

    public void visitContinueStatement(ContinueStatement statement) {
        onLineNumber(statement);

        cv.visitJumpInsn(GOTO, continueLabel);
    }

    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        onLineNumber(statement);

        statement.getExpression().visit(this);

        int index = defineVariable(createVariableName("synchronized"), "java.lang.Integer").getIndex();

        cv.visitVarInsn(ASTORE, index);
        cv.visitInsn(MONITORENTER);
        final Label l0 = new Label();
        cv.visitLabel(l0);

        statement.getCode().visit(this);

        cv.visitVarInsn(ALOAD, index);
        cv.visitInsn(MONITOREXIT);
        final Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        final Label l2 = new Label();
        cv.visitLabel(l2);
        cv.visitVarInsn(ALOAD, index);
        cv.visitInsn(MONITOREXIT);
        cv.visitInsn(ATHROW);
        cv.visitLabel(l1);

        exceptionBlocks.add(new Runnable() {
            public void run() {
                cv.visitTryCatchBlock(l0, l2, l2, null);
            }
        });
    }

    public void visitThrowStatement(ThrowStatement statement) {
        statement.getExpression().visit(this);

        // we should infer the type of the exception from the expression
        cv.visitTypeInsn(CHECKCAST, "java/lang/Throwable");

        cv.visitInsn(ATHROW);
    }

    public void visitReturnStatement(ReturnStatement statement) {
        onLineNumber(statement);

        statement.getExpression().visit(this);

        Expression assignExpr = assignmentExpression(statement.getExpression());
        if (assignExpr != null) {
            leftHandExpression = false;
            assignExpr.visit(this);
        }

        Class c = getExpressionType(statement.getExpression());

        //return is based on class type
        //TODO: make work with arrays
        // we may need to cast
        String returnType = methodNode.getReturnType();
        if (returnType.equals("double")) {
        	MethodCaller.newVirtual(Double.class, "doubleValue").call(cv);
        	cv.visitInsn(DRETURN);
        } else if (returnType.equals("float")) {
        	MethodCaller.newVirtual(Double.class, "doubleValue").call(cv);
        	cv.visitInsn(D2F);
        	cv.visitInsn(FRETURN);
        } else if (returnType.equals("long")) {
        	MethodCaller.newVirtual(Integer.class, "intValue").call(cv);
        	cv.visitInsn(I2L);
        	cv.visitInsn(LRETURN);
        } else if (returnType.equals("boolean")) {
        	MethodCaller.newVirtual(Boolean.class, "booleanValue").call(cv);
        	cv.visitInsn(IRETURN);
        } else if (returnType.equals("char") ||
        		   returnType.equals("byte") ||
        		   returnType.equals("int") ||
        		   returnType.equals("short")) { //byte,short,boolean,int are all IRETURN
          	MethodCaller.newVirtual(Integer.class, "intValue").call(cv);
            cv.visitInsn(IRETURN);
        } else {
        	if (c == Boolean.class) {
        		Label l0 = new Label();
        		cv.visitJumpInsn(IFEQ, l0);
        		cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
        		cv.visitInsn(ARETURN);
        		cv.visitLabel(l0);
        		cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
        		cv.visitInsn(ARETURN);
        	} else {
        		if (returnType != null && !returnType.equals("java.lang.Object") && !returnType.equals(c.getName())) {
        			doCast(returnType);
        		}
        		cv.visitInsn(ARETURN);
        	}
        }

        outputReturn = true;
    }

    public void visitExpressionStatement(ExpressionStatement statement) {
        onLineNumber(statement);

        Expression expression = statement.getExpression();
        expression.visit(this);

        if (expression instanceof MethodCallExpression
            && !MethodCallExpression.isSuperMethodCall((MethodCallExpression) expression)) {
            cv.visitInsn(POP);
        }
    }

    // Expressions
    //-------------------------------------------------------------------------

    public void visitBinaryExpression(BinaryExpression expression) {
        switch (expression.getOperation().getType()) {
            case Token.EQUAL :
                evaluateEqual(expression);
                break;

            case Token.COMPARE_IDENTICAL :
                evaluateBinaryExpression(compareIdenticalMethod, expression);
                break;

            case Token.COMPARE_EQUAL :
                evaluateBinaryExpression(compareEqualMethod, expression);
                break;

            case Token.COMPARE_NOT_EQUAL :
                evaluateBinaryExpression(compareNotEqualMethod, expression);
                break;

            case Token.COMPARE_TO :
                evaluateBinaryExpression("compareTo", expression);
                break;

            case Token.COMPARE_GREATER_THAN :
                evaluateBinaryExpression(compareGreaterThanMethod, expression);
                break;

            case Token.COMPARE_GREATER_THAN_EQUAL :
                evaluateBinaryExpression(compareGreaterThanEqualMethod, expression);
                break;

            case Token.COMPARE_LESS_THAN :
                evaluateBinaryExpression(compareLessThanMethod, expression);
                break;

            case Token.COMPARE_LESS_THAN_EQUAL :
                evaluateBinaryExpression(compareLessThanEqualMethod, expression);
                break;

            case Token.LOGICAL_AND :
                evaluateLogicalAndExpression(expression);
                break;

            case Token.LOGICAL_OR :
                evaluateLogicalOrExpression(expression);
                break;

            case Token.PLUS :
                evaluateBinaryExpression("plus", expression);
                break;

            case Token.PLUS_EQUAL :
                evaluateBinaryExpressionWithAsignment("plus", expression);
                break;

            case Token.MINUS :
                evaluateBinaryExpression("minus", expression);
                break;

            case Token.MINUS_EQUAL :
                evaluateBinaryExpressionWithAsignment("minus", expression);
                break;

            case Token.MULTIPLY :
                evaluateBinaryExpression("multiply", expression);
                break;

            case Token.MULTIPLY_EQUAL :
                evaluateBinaryExpressionWithAsignment("multiply", expression);
                break;

            case Token.DIVIDE :
                evaluateBinaryExpression("divide", expression);
                break;

            case Token.DIVIDE_EQUAL :
                evaluateBinaryExpressionWithAsignment("divide", expression);
                break;

            case Token.KEYWORD_INSTANCEOF :
                evaluateInstanceof(expression);
                break;

            case Token.FIND_REGEX :
                evaluateBinaryExpression(findRegexMethod, expression);
                break;

            case Token.MATCH_REGEX :
                evaluateBinaryExpression(matchRegexMethod, expression);
                break;

            case Token.LEFT_SQUARE_BRACKET :
                evaluateBinaryExpression(leftHandExpression ? "set" : "get", expression);
                break;

            default :
                throw new ClassGeneratorException("Operation: " + expression.getOperation() + " not supported");
        }
    }

    public void visitPostfixExpression(PostfixExpression expression) {
        switch (expression.getOperation().getType()) {
            case Token.PLUS_PLUS :
                evaluatePostfixMethod("increment", expression.getExpression());
                break;
            case Token.MINUS_MINUS :
                evaluatePostfixMethod("decrement", expression.getExpression());
                break;
        }
    }

    public void visitPrefixExpression(PrefixExpression expression) {
        switch (expression.getOperation().getType()) {
            case Token.PLUS_PLUS :
                evaluatePrefixMethod("increment", expression.getExpression());
                break;
            case Token.MINUS_MINUS :
                evaluatePrefixMethod("decrement", expression.getExpression());
                break;
        }
    }

    public void visitClosureExpression(ClosureExpression expression) {
        ClassNode innerClass = createClosureClass(expression);
        addInnerClass(innerClass);
        String innerClassinternalName = getClassInternalName(innerClass.getName());

        ClassNode owner = innerClass.getOuterClass();
        String ownerTypeName = owner.getName();
        if (isStaticMethod()) {
            ownerTypeName = "java.lang.Class";
        }

        passingClosureParams = true;
        Parameter[] localVariableParams = getClosureSharedVariables(expression);
        for (int i = 0; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String name = param.getName();
            if (variableStack.get(name) == null) {
                defineVariable(name, "java.lang.Object");
            }
        }

        if (classNode instanceof InnerClassNode) {
            // lets load the outer this
            int paramIdx = defineVariable(createVariableName("iterator"), "java.lang.Object", false).getIndex();
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, internalClassName, "owner", getTypeDescription(ownerTypeName));
            cv.visitVarInsn(ASTORE, paramIdx);

            cv.visitTypeInsn(NEW, innerClassinternalName);
            cv.visitInsn(DUP);
            cv.visitVarInsn(ALOAD, paramIdx);
        }
        else {
            cv.visitTypeInsn(NEW, innerClassinternalName);
            cv.visitInsn(DUP);
            if (isStaticMethod()) {
                visitClassExpression(new ClassExpression(ownerTypeName));
            }
            else {
                cv.visitVarInsn(ALOAD, 0);
            }
        }

        if (innerClass.getSuperClass().equals("groovy.lang.Closure")) {
            if (isStaticMethod()) {
                /** @todo could maybe stash this expression in a JVM variable from previous statement above */
                visitClassExpression(new ClassExpression(ownerTypeName));
            }
            else {
                cv.visitVarInsn(ALOAD, 0);
            }
        }

        String prototype = "(L" + getClassInternalName(ownerTypeName) + ";Ljava/lang/Object;";

        // now lets load the various parameters we're passing
        for (int i = 0; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String name = param.getName();
            visitVariableExpression(new VariableExpression(name));

            prototype = prototype + "L" + getClassInternalName(param.getType()) + ";";
        }
        passingClosureParams = false;

        // we may need to pass in some other constructors
        cv.visitMethodInsn(INVOKESPECIAL, innerClassinternalName, "<init>", prototype + ")V");
    }

    public void visitRegexExpression(RegexExpression expression) {
        expression.getRegex().visit(this);
        regexPattern.call(cv);
    }

    public void visitConstantExpression(ConstantExpression expression) {
        Object value = expression.getValue();
        if (value == null) {
            cv.visitInsn(ACONST_NULL);
        }
        else if (value instanceof String) {
            cv.visitLdcInsn(value);
        }
        else if (value instanceof Number) {
            /** @todo it would be more efficient to generate class constants */
            Number n = (Number) value;
            String className = getClassInternalName(value.getClass().getName());
            cv.visitTypeInsn(NEW, className);
            cv.visitInsn(DUP);
            String methodType = "(I)V";
            if (n instanceof Double) {
                methodType = "(D)V";
            }
            else if (n instanceof Float) {
                methodType = "(F)V";
            }
            cv.visitLdcInsn(n);
            cv.visitMethodInsn(INVOKESPECIAL, className, "<init>", methodType);
        }
        else if (value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            String text = (bool.booleanValue()) ? "TRUE" : "FALSE";
            cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", text, "Ljava/lang/Boolean;");
        }
        else {
            throw new ClassGeneratorException(
                "Cannot generate bytecode for constant: " + value + " of type: " + value.getClass().getName());
        }
    }

    public void visitNotExpression(NotExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(this);

        // This is not the best way to do this.  Javac does it by reversing the underlying expressions but that proved
        // fairly complicated for not much gain.  Instead we'll just use a utility function for now.
        if (comparisonExpression(expression.getExpression())) {
            notBoolean.call(cv);
        }
        else {
            notObject.call(cv);
        }
    }

    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this);

        if (!comparisonExpression(expression.getExpression())) {
            asBool.call(cv);
        }
    }

    public void visitMethodCallExpression(MethodCallExpression call) {
        this.leftHandExpression = false;

        Expression arguments = call.getArguments();
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            int size = tupleExpression.getExpressions().size();
            if (size == 0) {
                arguments = ConstantExpression.EMPTY_ARRAY;
            }
        }
        /*
        else {
            // lets put the argument into a tuple
            arguments = new TupleExpression(new Expression[] { arguments });
        }
        */

        if (MethodCallExpression.isSuperMethodCall(call)) {
            /** @todo handle method types! */
            cv.visitVarInsn(ALOAD, 0);
            cv.visitVarInsn(ALOAD, 1);
            cv.visitMethodInsn(INVOKESPECIAL, internalBaseClassName, "<init>", "(Ljava/lang/Object;)V");
        }
        else {

            String method = call.getMethod();

            // are we a local variable
            if (isThisExpression(call.getObjectExpression()) && isFieldOrVariable(call.getMethod())) {
                /*
                if (arguments instanceof TupleExpression) {
                    TupleExpression tupleExpression = (TupleExpression) arguments;
                    int size = tupleExpression.getExpressions().size();
                    if (size == 1) {
                        arguments = (Expression) tupleExpression.getExpressions().get(0);
                    }
                }
                */

                // lets invoke the closure method
                visitVariableExpression(new VariableExpression(method));
                arguments.visit(this);
                invokeClosureMethod.call(cv);
            }
            else {
                if (argumentsUseStack(arguments)) {
                    int paramIdx = defineVariable(createVariableName("iterator"), "java.lang.Object", false).getIndex();

                    arguments.visit(this);

                    cv.visitVarInsn(ASTORE, paramIdx);

                    call.getObjectExpression().visit(this);

                    cv.visitLdcInsn(method);

                    cv.visitVarInsn(ALOAD, paramIdx);

                    //idx--;
                }
                else {
                    call.getObjectExpression().visit(this);
                    cv.visitLdcInsn(method);
                    arguments.visit(this);
                }

                if (call.isSafe()) {
                    invokeMethodSafeMethod.call(cv);

                }
                else {
                    invokeMethodMethod.call(cv);
                }
            }
        }
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        this.leftHandExpression = false;

        Expression arguments = call.getArguments();
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            int size = tupleExpression.getExpressions().size();
            if (size == 0) {
                arguments = ConstantExpression.EMPTY_ARRAY;
            }
            else if (size == 1) {
                arguments = (Expression) tupleExpression.getExpressions().get(0);
            }
        }

        cv.visitLdcInsn(call.getType());
        cv.visitLdcInsn(call.getMethod());
        arguments.visit(this);

        invokeStaticMethodMethod.call(cv);
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        this.leftHandExpression = false;

        Expression arguments = call.getArguments();
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            int size = tupleExpression.getExpressions().size();
            if (size == 0) {
                arguments = ConstantExpression.EMPTY_ARRAY;
            }
            else if (size == 1) {
                arguments = (Expression) tupleExpression.getExpressions().get(0);
            }
        }

        // lets check that the type exists
        String type = checkValidType(call.getType(), call, "in constructor call");

        //System.out.println("Constructing: " + type);

        visitClassExpression(new ClassExpression(type));
        arguments.visit(this);
        invokeConstructorOfMethod.call(cv);

        /*
        cv.visitLdcInsn(type);
        
        arguments.visit(this);
        
        invokeConstructorMethod.call(cv);
        */
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        boolean left = leftHandExpression;
        // we need to clear the LHS flag to avoid "this." evaluating as ASTORE
        // rather than ALOAD
        leftHandExpression = false;

        Expression objectExpression = expression.getObjectExpression();
        objectExpression.visit(this);

        cv.visitLdcInsn(expression.getProperty());

        if (expression.isSafe()) {
            if (left) {
                setPropertySafeMethod2.call(cv);
            }
            else {
                getPropertySafeMethod.call(cv);
            }
        }
        else {
            if (left) {
                setPropertyMethod2.call(cv);
            }
            else {
                getPropertyMethod.call(cv);
            }
        }
    }

    public void visitFieldExpression(FieldExpression expression) {
        FieldNode field = expression.getField();
        boolean isStatic = field.isStatic();

        boolean holder = field.isHolder() && !isInClosureConstructor();
        if (!isStatic && !leftHandExpression) {
            cv.visitVarInsn(ALOAD, 0);
        }
        String type = field.getType();
        int tempIndex = defineVariable(createVariableName("field"), "java.lang.Object", false).getIndex();

        if (leftHandExpression && !holder) {
            // this may be superflous
            doCast(type);
        }
        int opcode =
            (leftHandExpression && !holder) ? ((isStatic) ? PUTSTATIC : PUTFIELD) : ((isStatic) ? GETSTATIC : GETFIELD);
        String ownerName =
            (field.getOwner().equals(classNode.getName()))
                ? internalClassName
                : Type.getInternalName(loadClass(field.getOwner()));

        if (holder) {
            if (leftHandExpression) {
                cv.visitVarInsn(ASTORE, tempIndex);

                cv.visitVarInsn(ALOAD, 0);
                cv.visitFieldInsn(opcode, ownerName, expression.getFieldName(), getTypeDescription(type));

                cv.visitVarInsn(ALOAD, tempIndex);

                cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
            }
            else {
                cv.visitFieldInsn(opcode, ownerName, expression.getFieldName(), getTypeDescription(type));
                cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
            }
        }
        else {
            cv.visitFieldInsn(opcode, ownerName, expression.getFieldName(), getTypeDescription(type));
        }
    }

    protected void visitOuterFieldExpression(FieldExpression expression) {
        ClassNode outerClassNode = classNode.getOuterClass();

        int valueIdx = idx + 1;

        if (leftHandExpression) {
            cv.visitVarInsn(ASTORE, valueIdx);
        }
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, internalClassName, "owner", getTypeDescription(outerClassNode.getName()));

        FieldNode field = expression.getField();
        boolean isStatic = field.isStatic();

        int opcode = (leftHandExpression) ? ((isStatic) ? PUTSTATIC : PUTFIELD) : ((isStatic) ? GETSTATIC : GETFIELD);
        String ownerName = getClassInternalName(outerClassNode.getName());

        if (leftHandExpression) {
            cv.visitVarInsn(ALOAD, valueIdx);
        }
        cv.visitFieldInsn(opcode, ownerName, expression.getFieldName(), getTypeDescription(field.getType()));
    }

    public void visitVariableExpression(VariableExpression expression) {
        // lets see if the variable is a field
        String variableName = expression.getVariable();
        if (isStaticMethod() && variableName.equals("this")) {
            visitClassExpression(new ClassExpression(classNode.getName()));
            return;
        }
        if (variableName.equals("super")) {
            visitClassExpression(new ClassExpression(classNode.getSuperClass()));
            return;
        }
        String className = classNode.getClassNameForExpression(variableName);
        if (className != null) {
            visitClassExpression(new ClassExpression(className));
            return;
        }
        FieldNode field = classNode.getField(variableName);
        if (field != null && variableStack.get(variableName) == null) {
            visitFieldExpression(new FieldExpression(field));
        }
        else {
            field = classNode.getOuterField(variableName);
            if (field != null) {
                visitOuterFieldExpression(new FieldExpression(field));
            }
            else {
                String name = variableName;
                Variable variable = null;
                if (!leftHandExpression) {
                    variable = (Variable) variableStack.get(name);
                }
                else {
                    variable = defineVariable(name, "java.lang.Object");
                }
                if (variable == null) {
                    variable = defineVariable(name, "java.lang.Object", false);
                    visitPropertyExpression(new PropertyExpression(VariableExpression.THIS_EXPRESSION, name));
                    // We need to store this in a local variable now since it has been looked at in this scope and possibly
                    // compared and it hasn't been referenced before.
                    cv.visitInsn(DUP);
                    cv.visitVarInsn(ASTORE, variable.getIndex());
                    return;
                }
                String type = variable.getType();
                int index = variable.getIndex();
                lastVariableIndex = index;
                boolean holder = variable.isHolder() && !passingClosureParams;

                if (leftHandExpression) {
                    if (holder) {
                        int tempIndex =
                            defineVariable(createVariableName("reference"), "java.lang.Object", false).getIndex();
                        cv.visitVarInsn(ASTORE, tempIndex);

                        cv.visitVarInsn(ALOAD, index);
                        cv.visitVarInsn(ALOAD, tempIndex);
                        cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
                    }
                    else {
                        //TODO: make work with arrays
                        if (type.equals("double")) {
                            cv.visitVarInsn(DSTORE, index);
                        }
                        else if (type.equals("float")) {
                            cv.visitVarInsn(FSTORE, index);
                        }
                        else if (type.equals("long")) {
                            cv.visitVarInsn(LSTORE, index);
                        }
                        else if (
                            type.equals("byte")
                                || type.equals("short")
                                || type.equals("boolean")
                                || type.equals("int")) {
                            cv.visitVarInsn(ISTORE, index);
                        }
                        else {
                            cv.visitVarInsn(ASTORE, index);
                        }
                    }
                }
                else {
                    if (holder) {
                        cv.visitVarInsn(ALOAD, index);
                        cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
                    }
                    else {
                        //TODO: make work with arrays
                        if (type.equals("double")) {
                            cv.visitVarInsn(DLOAD, index);
                        }
                        else if (type.equals("float")) {
                            cv.visitVarInsn(FLOAD, index);
                        }
                        else if (type.equals("long")) {
                            cv.visitVarInsn(LLOAD, index);
                        }
                        else if (
                            type.equals("byte")
                                || type.equals("short")
                                || type.equals("boolean")
                                || type.equals("int")) {
                            cv.visitVarInsn(ILOAD, index);
                        }
                        else {
                            cv.visitVarInsn(ALOAD, index);
                        }
                    }
                }
            }
        }
    }

    protected boolean firstStatementIsSuperMethodCall(Statement code) {
        if (code instanceof BlockStatement) {
            BlockStatement block = (BlockStatement) code;
            if (!block.getStatements().isEmpty()) {
                Object expr = block.getStatements().get(0);
                if (expr instanceof ExpressionStatement) {
                    ExpressionStatement expStmt = (ExpressionStatement) expr;
                    expr = expStmt.getExpression();
                    if (expr instanceof MethodCallExpression) {
                        return MethodCallExpression.isSuperMethodCall((MethodCallExpression) expr);
                    }
                }
            }
        }
        return false;
    }

    protected void createSyntheticStaticFields() {
        for (Iterator iter = syntheticStaticFields.iterator(); iter.hasNext();) {
            String staticFieldName = (String) iter.next();
            // generate a field node
            cw.visitField(ACC_STATIC + ACC_SYNTHETIC, staticFieldName, "Ljava/lang/Class;", null);
        }

        if (!syntheticStaticFields.isEmpty()) {
            cv = cw.visitMethod(ACC_STATIC + ACC_SYNTHETIC, "class$", "(Ljava/lang/String;)Ljava/lang/Class;", null);
            Label l0 = new Label();
            cv.visitLabel(l0);
            cv.visitVarInsn(ALOAD, 0);
            cv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
            Label l1 = new Label();
            cv.visitLabel(l1);
            cv.visitInsn(ARETURN);
            Label l2 = new Label();
            cv.visitLabel(l2);
            cv.visitVarInsn(ASTORE, 1);
            cv.visitTypeInsn(NEW, "java/lang/NoClassDefFoundError");
            cv.visitInsn(DUP);
            cv.visitVarInsn(ALOAD, 1);
            cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassNotFoundException", "getMessage", "()Ljava/lang/String;");
            cv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoClassDefFoundError", "<init>", "(Ljava/lang/String;)V");
            cv.visitInsn(ATHROW);
            cv.visitTryCatchBlock(l0, l1, l2, "java/lang/ClassNotFoundException");
            cv.visitMaxs(3, 2);

            cw.visitEnd();
        }
    }

    public void visitClassExpression(ClassExpression expression) {
        String type = expression.getText();

        final String staticFieldName =
            (type.equals(classNode.getName())) ? "class$0" : "class$" + type.replace('.', '$');

        syntheticStaticFields.add(staticFieldName);

        cv.visitFieldInsn(GETSTATIC, internalClassName, staticFieldName, "Ljava/lang/Class;");
        Label l0 = new Label();
        cv.visitJumpInsn(IFNONNULL, l0);
        cv.visitLdcInsn(type);
        cv.visitMethodInsn(INVOKESTATIC, internalClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
        cv.visitInsn(DUP);
        cv.visitFieldInsn(PUTSTATIC, internalClassName, staticFieldName, "Ljava/lang/Class;");
        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);
        cv.visitFieldInsn(GETSTATIC, internalClassName, staticFieldName, "Ljava/lang/Class;");
        cv.visitLabel(l1);
    }

    public void visitRangeExpression(RangeExpression expression) {
        leftHandExpression = false;
        expression.getFrom().visit(this);

        leftHandExpression = false;
        expression.getTo().visit(this);

        createRangeMethod.call(cv);
    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
    }

    public void visitMapExpression(MapExpression expression) {
        List entries = expression.getMapEntryExpressions();
        int size = entries.size();
        pushConstant(size * 2);

        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        int i = 0;
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            MapEntryExpression entry = (MapEntryExpression) iter.next();

            cv.visitInsn(DUP);
            pushConstant(i++);
            entry.getKeyExpression().visit(this);
            cv.visitInsn(AASTORE);

            cv.visitInsn(DUP);
            pushConstant(i++);
            entry.getValueExpression().visit(this);
            cv.visitInsn(AASTORE);
        }
        createMapMethod.call(cv);
    }

    public void visitTupleExpression(TupleExpression expression) {
        int size = expression.getExpressions().size();
        pushConstant(size);

        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            pushConstant(i);
            expression.getExpression(i).visit(this);
            cv.visitInsn(AASTORE);
        }
        //createTupleMethod.call(cv);
    }

    public void visitArrayExpression(ArrayExpression expression) {
        int size = expression.getExpressions().size();
        pushConstant(size);

        String typeName = getClassInternalName(expression.getType());
        cv.visitTypeInsn(ANEWARRAY, typeName);

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            pushConstant(i);
            Expression elementExpression = expression.getExpression(i);
            if (elementExpression == null) {
                ConstantExpression.NULL.visit(this);
            }
            else {
                elementExpression.visit(this);
            }
            cv.visitInsn(AASTORE);
        }
    }

    public void visitListExpression(ListExpression expression) {
        int size = expression.getExpressions().size();
        pushConstant(size);

        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            pushConstant(i);
            expression.getExpression(i).visit(this);
            cv.visitInsn(AASTORE);
        }
        createListMethod.call(cv);
    }

    public void visitGStringExpression(GStringExpression expression) {
        int size = expression.getValues().size();
        pushConstant(size);

        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            pushConstant(i);
            expression.getValue(i).visit(this);
            cv.visitInsn(AASTORE);
        }

        int paramIdx = defineVariable(createVariableName("iterator"), "java.lang.Object", false).getIndex();
        cv.visitVarInsn(ASTORE, paramIdx);

        ClassNode innerClass = createGStringClass(expression);
        addInnerClass(innerClass);
        String innerClassinternalName = getClassInternalName(innerClass.getName());

        cv.visitTypeInsn(NEW, innerClassinternalName);
        cv.visitInsn(DUP);
        cv.visitVarInsn(ALOAD, paramIdx);

        cv.visitMethodInsn(INVOKESPECIAL, innerClassinternalName, "<init>", "([Ljava/lang/Object;)V");
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected boolean addInnerClass(ClassNode innerClass) {
        innerClass.setModule(classNode.getModule());
        return innerClasses.add(innerClass);
    }

    protected ClassNode createClosureClass(ClosureExpression expression) {
        ClassNode owner = classNode;
        if (owner instanceof InnerClassNode) {
            owner = owner.getOuterClass();
        }
        String outerClassName = owner.getName();
        String name = outerClassName + "$" + context.getNextInnerClassIdx();
        if (isStaticMethod()) {
            outerClassName = "java.lang.Class";
        }
        Parameter[] parameters = expression.getParameters();
        if (parameters == null || parameters.length == 0) {
            // lets create a default 'it' parameter
            parameters = new Parameter[] { new Parameter("it")};
        }

        Parameter[] localVariableParams = getClosureSharedVariables(expression);

        InnerClassNode answer = new InnerClassNode(owner, name, ACC_PUBLIC, "groovy.lang.Closure");
        answer.addMethod("doCall", ACC_PUBLIC, "java.lang.Object", parameters, expression.getCode());
        if (parameters.length > 1
            || (parameters.length == 1
                && parameters[0].getType() != null
                && !parameters[0].getType().equals("java.lang.Object"))) {

            // lets add a typesafe call method
            answer.addMethod(
                "call",
                ACC_PUBLIC,
                "java.lang.Object",
                parameters,
                new ReturnStatement(
                    new MethodCallExpression(
                        VariableExpression.THIS_EXPRESSION,
                        "doCall",
                        new ArgumentListExpression(parameters))));
        }

        FieldNode field = answer.addField("owner", ACC_PRIVATE, outerClassName, null);

        // lets make the constructor
        BlockStatement block = new BlockStatement();
        block.addStatement(
            new ExpressionStatement(
                new MethodCallExpression(
                    new VariableExpression("super"),
                    "<init>",
                    new VariableExpression("_outerInstance"))));
        block.addStatement(
            new ExpressionStatement(
                new BinaryExpression(
                    new FieldExpression(field),
                    Token.equal(-1, -1),
                    new VariableExpression("_outerInstance"))));

        // lets assign all the parameter fields from the outer context
        for (int i = 0; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String paramName = param.getName();
            boolean holder = mutableVars.contains(paramName);
            Expression initialValue = null;
            String type = param.getType();
            if (holder) {
                initialValue = new VariableExpression(paramName);
                type = Reference.class.getName();
            }

            FieldNode paramField = answer.addField(paramName, ACC_PRIVATE, type, initialValue);

            if (holder) {
                paramField.setHolder(true);
            }
            else {
                block.addStatement(
                    new ExpressionStatement(
                        new BinaryExpression(
                            new FieldExpression(paramField),
                            Token.equal(-1, -1),
                            new VariableExpression(paramName))));
            }
        }

        Parameter[] params = new Parameter[2 + localVariableParams.length];
        params[0] = new Parameter(outerClassName, "_outerInstance");
        params[1] = new Parameter("java.lang.Object", "_delegate");
        System.arraycopy(localVariableParams, 0, params, 2, localVariableParams.length);

        answer.addConstructor(ACC_PUBLIC, params, block);
        return answer;
    }

    protected ClassNode createGStringClass(GStringExpression expression) {
        ClassNode owner = classNode;
        if (owner instanceof InnerClassNode) {
            owner = owner.getOuterClass();
        }
        String outerClassName = owner.getName();
        String name = outerClassName + "$" + context.getNextInnerClassIdx();
        InnerClassNode answer = new InnerClassNode(owner, name, ACC_PUBLIC, GString.class.getName());
        FieldNode stringsField =
            answer.addField(
                "strings",
                ACC_PRIVATE | ACC_STATIC,
                "java.lang.String[]",
                new ArrayExpression("java.lang.String", expression.getStrings()));
        answer.addMethod(
            "getStrings",
            ACC_PUBLIC,
            "java.lang.String[]",
            Parameter.EMPTY_ARRAY,
            new ReturnStatement(new FieldExpression(stringsField)));
        // lets make the constructor
        BlockStatement block = new BlockStatement();
        block.addStatement(
            new ExpressionStatement(
                new MethodCallExpression(new VariableExpression("super"), "<init>", new VariableExpression("values"))));
        Parameter[] contructorParams = new Parameter[] { new Parameter("java.lang.Object[]", "values")};
        answer.addConstructor(ACC_PUBLIC, contructorParams, block);
        return answer;
    }

    protected void doCast(String type) {
        if (!type.equals("java.lang.Object")) {
            cv.visitTypeInsn(CHECKCAST, type.endsWith("[]") ? getTypeDescription(type) : getClassInternalName(type));
        }
    }

    protected void evaluateLogicalOrExpression(BinaryExpression expression) {
        visitBooleanExpression(new BooleanExpression(expression.getLeftExpression()));
        Label l0 = new Label();
        Label l2 = new Label();
        cv.visitJumpInsn(IFEQ, l0);

        cv.visitLabel(l2);

        visitConstantExpression(ConstantExpression.TRUE);

        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);

        visitBooleanExpression(new BooleanExpression(expression.getRightExpression()));

        cv.visitJumpInsn(IFNE, l2);

        visitConstantExpression(ConstantExpression.FALSE);
        cv.visitLabel(l1);
    }

    protected void evaluateLogicalAndExpression(BinaryExpression expression) {
        visitBooleanExpression(new BooleanExpression(expression.getLeftExpression()));
        Label l0 = new Label();
        cv.visitJumpInsn(IFEQ, l0);

        visitBooleanExpression(new BooleanExpression(expression.getRightExpression()));

        cv.visitJumpInsn(IFEQ, l0);

        visitConstantExpression(ConstantExpression.TRUE);

        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);

        visitConstantExpression(ConstantExpression.FALSE);

        cv.visitLabel(l1);
    }

    protected void evaluateBinaryExpression(String method, BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        //        if (isNonStaticField(leftExpression)) {
        //            cv.visitVarInsn(ALOAD, 0);
        //        }
        //
        leftHandExpression = false;
        leftExpression.visit(this);
        cv.visitLdcInsn(method);
        leftHandExpression = false;
        new ArgumentListExpression(new Expression[] { expression.getRightExpression()}).visit(this);
        // expression.getRightExpression().visit(this);
        invokeMethodMethod.call(cv);
    }

    protected void evaluateBinaryExpressionWithAsignment(String method, BinaryExpression expression) {
        evaluateBinaryExpression(method, expression);

        Expression leftExpression = expression.getLeftExpression();
        leftHandExpression = true;
        leftExpression.visit(this);
        leftHandExpression = false;
    }

    protected void evaluateBinaryExpression(MethodCaller compareMethod, BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        if (isNonStaticField(leftExpression)) {
            cv.visitVarInsn(ALOAD, 0);
        }

        leftHandExpression = false;
        leftExpression.visit(this);
        leftHandExpression = false;
        expression.getRightExpression().visit(this);
        // now lets invoke the method
        compareMethod.call(cv);
    }

    protected void evaluateEqual(BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        if (leftExpression instanceof BinaryExpression) {
            BinaryExpression leftBinExpr = (BinaryExpression) leftExpression;
            if (leftBinExpr.getOperation().getType() == Token.LEFT_SQUARE_BRACKET) {
                // lets replace this assignment to a subscript operator with a method call
                // e.g. x[5] = 10
                // -> (x, [], 5), =, 10 
                // -> methodCall(x, "set", [5, 10])
                visitMethodCallExpression(
                    new MethodCallExpression(
                        leftBinExpr.getLeftExpression(),
                        "put",
                        new ArgumentListExpression(
                            new Expression[] { leftBinExpr.getRightExpression(), expression.getRightExpression()})));
                return;
            }
        }
        if (isNonStaticField(leftExpression)) {
            cv.visitVarInsn(ALOAD, 0);
        }

        // lets evaluate the RHS then hopefully the LHS will be a field
        leftHandExpression = false;
        Expression rightExpression = expression.getRightExpression();
        rightExpression.visit(this);
        if (comparisonExpression(rightExpression)) {
            Label l0 = new Label();
            cv.visitJumpInsn(IFEQ, l0);
            cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
            Label l1 = new Label();
            cv.visitJumpInsn(GOTO, l1);
            cv.visitLabel(l0);
            cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
            cv.visitLabel(l1);
        }
        leftHandExpression = true;
        leftExpression.visit(this);
        leftHandExpression = false;
    }

    protected void evaluatePrefixMethod(String method, Expression expression) {
        expression.visit(this);
        cv.visitLdcInsn(method);
        new ArgumentListExpression().visit(this);
        invokeMethodMethod.call(cv);

        leftHandExpression = true;
        expression.visit(this);
        leftHandExpression = false;
        expression.visit(this);
    }

    protected void evaluatePostfixMethod(String method, Expression expression) {
        leftHandExpression = false;
        expression.visit(this);

        int tempIdx = defineVariable(createVariableName("postfix"), "java.lang.Object", false).getIndex();
        cv.visitVarInsn(ASTORE, tempIdx);
        cv.visitVarInsn(ALOAD, tempIdx);

        cv.visitLdcInsn(method);
        new ArgumentListExpression().visit(this);
        invokeMethodMethod.call(cv);

        leftHandExpression = true;
        expression.visit(this);
        leftHandExpression = false;

        cv.visitVarInsn(ALOAD, tempIdx);
    }

    protected void evaluateInstanceof(BinaryExpression expression) {
        expression.getLeftExpression().visit(this);
        Expression rightExp = expression.getRightExpression();
        String className = null;
        if (rightExp instanceof ClassExpression) {
            ClassExpression classExp = (ClassExpression) rightExp;
            className = classExp.getType();
        }
        else {
            throw new RuntimeException(
                "Right hand side of the instanceof keyworld must be a class name, not: " + rightExp);
        }
        String classInternalName = getClassInternalName(className);
        cv.visitTypeInsn(INSTANCEOF, classInternalName);
    }

    /**
     * @return true if the given argument expression requires the stack, in
     *         which case the arguments are evaluated first, stored in the
     *         variable stack and then reloaded to make a method call
     */
    protected boolean argumentsUseStack(Expression arguments) {
        return arguments instanceof TupleExpression || arguments instanceof ClosureExpression;
    }

    /**
     * @return true if the given expression represents a non-static field
     */
    protected boolean isNonStaticField(Expression expression) {
        FieldNode field = null;
        if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            field = classNode.getField(varExp.getVariable());
        }
        else if (expression instanceof FieldExpression) {
            FieldExpression fieldExp = (FieldExpression) expression;
            field = classNode.getField(fieldExp.getFieldName());
        }
        if (field != null) {
            return !field.isStatic();
        }
        return false;
    }

    protected boolean isThisExpression(Expression expression) {
        if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            return varExp.getVariable().equals("this");
        }
        return false;
    }

    protected Expression assignmentExpression(Expression expression) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binExp = (BinaryExpression) expression;
            if (binExp.getOperation().getType() == Token.EQUAL) {
                return binExp.getLeftExpression();
            }
        }
        return null;
    }

    protected boolean comparisonExpression(Expression expression) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binExpr = (BinaryExpression) expression;
            switch (binExpr.getOperation().getType()) {
                case Token.COMPARE_EQUAL :
                case Token.MATCH_REGEX :
                case Token.COMPARE_GREATER_THAN :
                case Token.COMPARE_GREATER_THAN_EQUAL :
                case Token.COMPARE_LESS_THAN :
                case Token.COMPARE_LESS_THAN_EQUAL :
                case Token.COMPARE_IDENTICAL :
                case Token.COMPARE_NOT_EQUAL :
                case Token.KEYWORD_INSTANCEOF :
                    return true;
            }
        }
        else if (expression instanceof BooleanExpression) {
            return true;
        }
        return false;
    }

    protected void onLineNumber(ASTNode statement) {
        int number = statement.getLineNumber();
        if (number >= 0 && cv != null) {
            cv.visitLineNumber(number, new Label());
        }
    }

    protected void pushConstant(int value) {
        switch (value) {
            case 0 :
                cv.visitInsn(ICONST_0);
                break;
            case 1 :
                cv.visitInsn(ICONST_1);
                break;
            case 2 :
                cv.visitInsn(ICONST_2);
                break;
            case 3 :
                cv.visitInsn(ICONST_3);
                break;
            case 4 :
                cv.visitInsn(ICONST_4);
                break;
            case 5 :
                cv.visitInsn(ICONST_5);
                break;
            default :
                cv.visitIntInsn(BIPUSH, value);
                break;
        }
    }

    /**
     * @return a list of parameters for each local variable which needs to be passed into
     * a closure
     */
    protected Parameter[] getClosureSharedVariables(ClosureExpression expression) {
        VariableScopeCodeVisitor outerVisitor = new VariableScopeCodeVisitor();
        VariableScopeCodeVisitor innerVisitor = new VariableScopeCodeVisitor();

        methodNode.getCode().visit(outerVisitor);
        expression.getCode().visit(innerVisitor);

        // now any variables declared in the outer context that are referred to
        // in the inner context need to be copied
        Set outerDecls = outerVisitor.getDeclaredVariables();
        Set outerRefs = outerVisitor.getReferencedVariables();
        Set innerDecls = innerVisitor.getDeclaredVariables();
        Set innerRefs = innerVisitor.getReferencedVariables();

        List vars = new ArrayList();
        Set varSet = new HashSet();
        for (Iterator iter = innerRefs.iterator(); iter.hasNext();) {
            String var = (String) iter.next();
            if (outerDecls.contains(var) && classNode.getField(var) == null) {
                String type = getVariableType(var);
                vars.add(new Parameter(type, var));
                varSet.add(var);
            }
        }
        for (Iterator iter = outerRefs.iterator(); iter.hasNext();) {
            String var = (String) iter.next();
            if (innerDecls.contains(var) && classNode.getField(var) == null && !varSet.contains(var)) {
                String type = getVariableType(var);
                vars.add(new Parameter(type, var));
            }
        }

        //        if (!vars.isEmpty()) {
        //            System.out.println(classNode.getName() + " - closure is copying variables from outer context: " + vars);
        //        }

        Parameter[] answer = new Parameter[vars.size()];
        vars.toArray(answer);
        return answer;
    }

    protected void findMutableVariables(MethodNode node) {
        VariableScopeCodeVisitor outerVisitor = new VariableScopeCodeVisitor();
        node.getCode().visit(outerVisitor);

        VariableScopeCodeVisitor innerVisitor = outerVisitor.getClosureVisitor();
        Set outerDecls = outerVisitor.getDeclaredVariables();
        Set outerRefs = outerVisitor.getReferencedVariables();
        Set innerDecls = innerVisitor.getDeclaredVariables();
        Set innerRefs = innerVisitor.getReferencedVariables();

        mutableVars.clear();

        for (Iterator iter = innerDecls.iterator(); iter.hasNext();) {
            String var = (String) iter.next();
            if ((outerDecls.contains(var) || outerRefs.contains(var)) && classNode.getField(var) == null) {
                mutableVars.add(var);
            }
        }
    }

    protected String getVariableType(String name) {
        Variable variable = (Variable) variableStack.get(name);
        if (variable != null) {
            return variable.getType();
        }
        return null;
    }

    /**
      * @return the last ID used by the stack
      */
    protected int getLastStackId() {
        return variableStack.size();
    }

    protected void resetVariableStack(Parameter[] parameters) {
        idx = 0;
        variableStack.clear();
        // lets push this onto the stack
        definingParameters = true;
        if (!isStaticMethod()) {
            defineVariable("this", classNode.getName()).getIndex();
        } // now lets create indices for the parameteres
        for (int i = 0; i < parameters.length; i++) {
            defineVariable(parameters[i].getName(), parameters[i].getType());
        }
        definingParameters = false;
    }

    /** @return true if the given name is a local variable or a field */
    protected boolean isFieldOrVariable(String name) {
        return variableStack.containsKey(name) || classNode.getField(name) != null;
    }

    /**
     * Defines the given variable in scope and assigns it to the stack
     */
    protected Variable defineVariable(String name, String type) {
        return defineVariable(name, type, true);
    }

    protected Variable defineVariable(String name, String type, boolean define) {
        Variable answer = (Variable) variableStack.get(name);
        if (answer == null) {
            idx = Math.max(idx, variableStack.size());
            answer = new Variable(idx, type, name);
            if (mutableVars.contains(name)) {
                answer.setHolder(true);
            }
            variableStack.put(name, answer);
            if (define && !definingParameters) {
                // using new variable inside a comparison expression
                // so lets initialize it too
                if (answer.isHolder()) {
                    //cv.visitVarInsn(ASTORE, idx + 1);

                    cv.visitTypeInsn(NEW, "groovy/lang/Reference");
                    cv.visitInsn(DUP);
                    cv.visitMethodInsn(INVOKESPECIAL, "groovy/lang/Reference", "<init>", "()V");

                    cv.visitVarInsn(ASTORE, idx);
                    //cv.visitVarInsn(ALOAD, idx + 1);
                }
                else {
                    if (!leftHandExpression) {
                        cv.visitInsn(ACONST_NULL);
                        cv.visitVarInsn(ASTORE, idx);
                    }
                }
            }
        }
        return answer;
    }

    protected String checkValidType(String type, ASTNode node, String message) {
        String original = type;
        if (type != null) {
            if (classNode.getNameWithoutPackage().equals(type)) {
                return classNode.getName();
            }
            for (int i = 0; i < 2; i++) {
                if (context.getCompileUnit().getClass(type) != null) {
                    return type;
                }

                try {
                    classLoader.loadClass(type);
                    return type;
                }
                catch (Throwable e) { // fall through
                } // lets try the system class loader
                try {
                    Class.forName(type);
                    return type;
                }
                catch (Throwable e) { // fall through
                } // lets try class in same package
                String packageName = classNode.getPackageName();
                if (packageName == null || packageName.length() <= 0) {
                    break;
                }
                type = packageName + "." + type;
            }
        }
        throw new NoSuchClassException(original, node, message);
    }

    protected String createVariableName(String type) {
        return "__" + type + idx;
    }

    /**
     * @return if the type of the expression can be determined at compile time
     *         then this method returns the type - otherwise java.lang.Object
     *         is returned.
     */
    protected Class getExpressionType(Expression expression) {
        if (comparisonExpression(expression)) {
            return Boolean.class;
        } /** @todo we need a way to determine this from an expression */
        return Object.class;
    }

    /**
     * @return true if the value is an Integer, a Float, a Long, a Double or a
     *         String .
     */
    protected boolean isPrimitiveFieldType(Object value) {
        return value instanceof String
            || value instanceof Integer
            || value instanceof Double
            || value instanceof Long
            || value instanceof Float;
    }

    protected boolean isInClosureConstructor() {
        return constructorNode != null
            && classNode.getOuterClass() != null
            && classNode.getSuperClass().equals(Closure.class.getName());
    }

    protected boolean isStaticMethod() {
        if (methodNode == null) { // we're in a constructor
            return false;
        }
        return methodNode.isStatic();
    }

    /**
     * @return an array of ASM internal names of the type
     */
    private String[] getClassInternalNames(String[] names) {
        int size = names.length;
        String[] answer = new String[size];
        for (int i = 0; i < size; i++) {
            answer[i] = getClassInternalName(names[i]);
        }
        return answer;
    }

    /**
     * @return the ASM internal name of the type
     */
    protected String getClassInternalName(String name) {
        if (name == null) {
            return "java/lang/Object";
        }
        String answer = name.replace('.', '/');
        if (answer.endsWith("[]")) {
            return "[" + answer.substring(0, answer.length() - 2);
        }
        return answer;
    }

    /**
     * @return the ASM method type descriptor
     */
    protected String getMethodDescriptor(String returnTypeName, Parameter[] paramTypeNames) {
        // lets avoid class loading
        StringBuffer buffer = new StringBuffer("(");
        for (int i = 0; i < paramTypeNames.length; i++) {
            buffer.append(getTypeDescription(paramTypeNames[i].getType()));
        }
        buffer.append(")");
        buffer.append(getTypeDescription(returnTypeName));
        return buffer.toString();
    }

    /**
     * @return the ASM type description
     */
    protected String getTypeDescription(String name) { // lets avoid class loading
        // return getType(name).getDescriptor();
        if (name == null) {
            return "Ljava/lang/Object;";
        }
        if (name.equals("void")) {
            return "V";
        }
        if (name.equals("int")) {
        	return "I";
        }
        if (name.equals("long")) {
        	return "J";
        }
        if (name.equals("short")) {
        	return "S";
        }
        if (name.equals("float")) {
        	return "F";
        }
        if (name.equals("double")) {
        	return "D";
        }
        if (name.equals("byte")) {
        	return "B";
        }
        if (name.equals("char")) {
        	return "C";
        }
        if (name.equals("boolean")) {
        	return "Z";
        }
        String prefix = "";
        if (name.endsWith("[]")) {
            prefix = "[";
            name = name.substring(0, name.length() - 2);
        }
        return prefix + "L" + name.replace('.', '/') + ";";
    }

    /**
     * @return loads the given type name
     */
    protected Class loadClass(String name) {
        try {
            return getClassLoader().loadClass(name);
        }
        catch (ClassNotFoundException e) {
            throw new ClassGeneratorException("Could not load class: " + name + " reason: " + e, e);
        }
    }
}

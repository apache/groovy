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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
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
import org.codehaus.groovy.syntax.parser.RuntimeParserException;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;

/**
 * Generates Java class versions of Groovy classes
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ClassGenerator extends CodeVisitorSupport implements GroovyClassVisitor, Constants {

    private Logger log = Logger.getLogger(getClass().getName());

    private ClassVisitor cw;
    private ClassLoader classLoader;
    private CodeVisitor cv;
    private GeneratorContext context;

    private String sourceFile;

    // current class details
    private ClassNode classNode;
    private ClassNode outermostClass;
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
    MethodCaller invokeSuperMethodMethod = MethodCaller.newStatic(InvokerHelper.class, "invokeSuperMethod");
    MethodCaller invokeNoArgumentsMethod = MethodCaller.newStatic(InvokerHelper.class, "invokeNoArgumentsMethod");
    MethodCaller invokeStaticNoArgumentsMethod =
        MethodCaller.newStatic(InvokerHelper.class, "invokeStaticNoArgumentsMethod");

    MethodCaller asIntMethod = MethodCaller.newStatic(InvokerHelper.class, "asInt");
    MethodCaller asTypeMethod = MethodCaller.newStatic(InvokerHelper.class, "asType");
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
    MethodCaller negation = MethodCaller.newStatic(InvokerHelper.class, "negate");

    MethodCaller compareIdenticalMethod = MethodCaller.newStatic(InvokerHelper.class, "compareIdentical");
    MethodCaller compareEqualMethod = MethodCaller.newStatic(InvokerHelper.class, "compareEqual");
    MethodCaller compareNotEqualMethod = MethodCaller.newStatic(InvokerHelper.class, "compareNotEqual");
    MethodCaller compareToMethod = MethodCaller.newStatic(InvokerHelper.class, "compareTo");
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
    private Set mutableVars = new HashSet();
    private boolean passingClosureParams;

    private ConstructorNode constructorNode;
    private MethodNode methodNode;
    //private PropertyNode propertyNode;
    private BlockScope scope;
    private BytecodeHelper helper = new BytecodeHelper(null);

    private VariableScope variableScope;


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
            this.outermostClass = null;
            this.internalClassName = BytecodeHelper.getClassInternalName(classNode.getName());
            this.internalBaseClassName = BytecodeHelper.getClassInternalName(classNode.getSuperClass());

            cw.visit(
                classNode.getModifiers(),
                internalClassName,
                internalBaseClassName,
                BytecodeHelper.getClassInternalNames(classNode.getInterfaces()),
                sourceFile);

            classNode.visitContents(this);

            createSyntheticStaticFields();

            for (Iterator iter = innerClasses.iterator(); iter.hasNext();) {
                ClassNode innerClass = (ClassNode) iter.next();
                String innerClassName = innerClass.getName();
                String innerClassInternalName = BytecodeHelper.getClassInternalName(innerClassName);
                cw.visitInnerClass(
                    innerClassInternalName,
                    internalClassName,
                    innerClassName,
                    innerClass.getModifiers());
            }

            cw.visitEnd();
        }
        catch (GroovyRuntimeException e) {
            e.setModule(classNode.getModule());
            throw e;
        }
    }

    public void visitConstructor(ConstructorNode node) {
        // creates a MethodWriter for the (implicit) constructor
        //String methodType = Type.getMethodDescriptor(VOID_TYPE, )

        this.constructorNode = node;
        this.methodNode = null;
        this.variableScope = null;

        String methodType = helper.getMethodDescriptor("void", node.getParameters());
        cv = cw.visitMethod(node.getModifiers(), "<init>", methodType, null, null);
        helper = new BytecodeHelper(cv);

        findMutableVariables();
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
        this.variableScope = null;

        String methodType = helper.getMethodDescriptor(node.getReturnType(), node.getParameters());
        cv = cw.visitMethod(node.getModifiers(), node.getName(), methodType, null, null);
        helper = new BytecodeHelper(cv);

        findMutableVariables();
        resetVariableStack(node.getParameters());

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
            if (isPrimitiveFieldType(fieldNode.getType())) {
                // lets convert any primitive types
                Class type = null;
                try {
                    type = loadClass(fieldNode.getType());
                    fieldValue = InvokerHelper.asType(value, type);
                }
                catch (Exception e) {
                    log.warning("Caught unexpected: " + e);
                }
            }
        }
        cw.visitField(
            fieldNode.getModifiers(),
            fieldNode.getName(),
            helper.getTypeDescription(fieldNode.getType()),
            fieldValue,
            null);
    }

    /**
     * Creates a getter, setter and field
     */
    public void visitProperty(PropertyNode statement) {
        onLineNumber(statement);
        //this.propertyNode = statement;
        this.methodNode = null;
    }

    // GroovyCodeVisitor interface
    //-------------------------------------------------------------------------

    // Statements
    //-------------------------------------------------------------------------

    public void visitForLoop(ForStatement loop) {
        onLineNumber(loop);

        Type variableType = checkValidType(loop.getVariableType(), loop, "for loop variable");
        int iIdx = defineVariable(loop.getVariable(), variableType, true).getIndex();

        loop.getCollectionExpression().visit(this);

        asIteratorMethod.call(cv);

        final int iteratorIdx = defineVariable(createVariableName("iterator"), "java.util.Iterator", false).getIndex();
        cv.visitVarInsn(ASTORE, iteratorIdx);

        pushBlockScope();

        Label continueLabel = scope.getContinueLabel();
        cv.visitJumpInsn(GOTO, continueLabel);
        Label label2 = new Label();
        cv.visitLabel(label2);

        BytecodeExpression expression = new BytecodeExpression() {
            public void visit(GroovyCodeVisitor visitor) {
                cv.visitVarInsn(ALOAD, iteratorIdx);

                iteratorNextMethod.call(cv);
            }
        };
        evaluateEqual(
            new BinaryExpression(new VariableExpression(loop.getVariable()), Token.equal(-1, -1), expression));

        loop.getLoopBlock().visit(this);

        cv.visitLabel(continueLabel);
        cv.visitVarInsn(ALOAD, iteratorIdx);

        iteratorHasNextMethod.call(cv);

        cv.visitJumpInsn(IFNE, label2);

        cv.visitLabel(scope.getBreakLabel());
        popScope();
    }

    public void visitWhileLoop(WhileStatement loop) {
        onLineNumber(loop);

        /*
         * // quick hack if (!methodNode.isStatic()) { cv.visitVarInsn(ALOAD,
         * 0); }
         */

        pushBlockScope();

        Label continueLabel = scope.getContinueLabel();

        cv.visitJumpInsn(GOTO, continueLabel);
        Label l1 = new Label();
        cv.visitLabel(l1);

        loop.getLoopBlock().visit(this);

        cv.visitLabel(continueLabel);
        //cv.visitVarInsn(ALOAD, 0);

        loop.getBooleanExpression().visit(this);

        cv.visitJumpInsn(IFNE, l1);

        cv.visitLabel(scope.getBreakLabel());
        popScope();
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        onLineNumber(loop);

        pushBlockScope();

        Label breakLabel = scope.getBreakLabel();

        Label continueLabel = scope.getContinueLabel();
        cv.visitLabel(continueLabel);
        Label l1 = new Label();

        loop.getLoopBlock().visit(this);

        cv.visitLabel(l1);

        loop.getBooleanExpression().visit(this);

        cv.visitJumpInsn(IFNE, continueLabel);

        cv.visitLabel(breakLabel);
        popScope();
    }

    public void visitIfElse(IfStatement ifElse) {
        onLineNumber(ifElse);

        ifElse.getBooleanExpression().visit(this);

        Label l0 = new Label();
        cv.visitJumpInsn(IFEQ, l0);
        ifElse.getIfBlock().visit(this);

        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);

        ifElse.getElseBlock().visit(this);
        cv.visitLabel(l1);
    }

    public void visitTernaryExpression(TernaryExpression expression) {
        onLineNumber(expression);

        expression.getBooleanExpression().visit(this);

        Label l0 = new Label();
        cv.visitJumpInsn(IFEQ, l0);
        expression.getTrueExpression().visit(this);

        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);

        expression.getFalseExpression().visit(this);
        cv.visitLabel(l1);
    }

    public void visitAssertStatement(AssertStatement statement) {
        onLineNumber(statement);

        //System.out.println("Assert: " + statement.getLineNumber() + " for: "
        // + statement.getText());

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

        Statement tryStatement = statement.getTryStatement();
        if (tryStatement.isEmpty()) {
            return;
        }

        if (catchStatement == null) {
            final Label l0 = new Label();
            cv.visitLabel(l0);

            tryStatement.visit(this);

            int index1 = defineVariable(this.createVariableName("exception"), "java.lang.Object").getIndex();
            int index2 = defineVariable(this.createVariableName("exception"), "java.lang.Object").getIndex();

            final Label l1 = new Label();
            cv.visitJumpInsn(JSR, l1);
            final Label l2 = new Label();
            cv.visitLabel(l2);
            final Label l3 = new Label();
            cv.visitJumpInsn(GOTO, l3);
            final Label l4 = new Label();
            cv.visitLabel(l4);
            cv.visitVarInsn(ASTORE, index1);
            cv.visitJumpInsn(JSR, l1);
            final Label l5 = new Label();
            cv.visitLabel(l5);
            cv.visitVarInsn(ALOAD, index1);
            cv.visitInsn(ATHROW);
            cv.visitLabel(l1);
            cv.visitVarInsn(ASTORE, index2);

            statement.getFinallyStatement().visit(this);

            cv.visitVarInsn(RET, index2);
            cv.visitLabel(l3);

            exceptionBlocks.add(new Runnable() {
                public void run() {
                    cv.visitTryCatchBlock(l0, l2, l4, null);
                    cv.visitTryCatchBlock(l4, l5, l4, null);
                }
            });

        }
        else {
            String exceptionVar = catchStatement.getVariable();
            String exceptionType =
                checkValidType(catchStatement.getExceptionType(), catchStatement, "in catch statement");

            int exceptionIndex = defineVariable(exceptionVar, exceptionType, false).getIndex();
            int index2 = defineVariable(this.createVariableName("exception"), "java.lang.Object").getIndex();
            int index3 = defineVariable(this.createVariableName("exception"), "java.lang.Object").getIndex();

            final Label l0 = new Label();
            cv.visitLabel(l0);

            tryStatement.visit(this);

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

            //final String exceptionTypeInternalName = (catchStatement !=
            // null) ?
            // getTypeDescription(exceptionType) : null;
            final String exceptionTypeInternalName =
                (catchStatement != null) ? BytecodeHelper.getClassInternalName(exceptionType) : null;

            exceptionBlocks.add(new Runnable() {
                public void run() {
                    cv.visitTryCatchBlock(l0, l1, l5, exceptionTypeInternalName);
                    cv.visitTryCatchBlock(l0, l3, l7, null);
                    cv.visitTryCatchBlock(l5, l6, l7, null);
                    cv.visitTryCatchBlock(l7, l8, l7, null);
                }
            });
        }
    }

    public void visitSwitch(SwitchStatement statement) {
        onLineNumber(statement);

        statement.getExpression().visit(this);

        pushBlockScope();

        int switchVariableIndex = defineVariable(createVariableName("switch"), "java.lang.Object").getIndex();
        cv.visitVarInsn(ASTORE, switchVariableIndex);

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

        cv.visitLabel(scope.getBreakLabel());

        popScope();
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

        cv.visitJumpInsn(GOTO, scope.getBreakLabel());
    }

    public void visitContinueStatement(ContinueStatement statement) {
        onLineNumber(statement);

        cv.visitJumpInsn(GOTO, scope.getContinueLabel());
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

        Expression assignExpr = createReturnLHSExpression(statement.getExpression());
        if (assignExpr != null) {
            leftHandExpression = false;
            assignExpr.visit(this);
        }

        Class c = getExpressionType(statement.getExpression());

        //return is based on class type
        //TODO: make work with arrays
        // we may need to cast
        String returnType = methodNode.getReturnType();
        helper.unbox(returnType);
        if (returnType.equals("double")) {
            cv.visitInsn(DRETURN);
        }
        else if (returnType.equals("float")) {
            cv.visitInsn(FRETURN);
        }
        else if (returnType.equals("long")) {
            cv.visitInsn(LRETURN);
        }
        else if (returnType.equals("boolean")) {
            cv.visitInsn(IRETURN);
        }
        else if (
            returnType.equals("char")
                || returnType.equals("byte")
                || returnType.equals("int")
                || returnType.equals("short")) { //byte,short,boolean,int are
            // all IRETURN
            cv.visitInsn(IRETURN);
        }
        else {
            if (c == Boolean.class) {
                Label l0 = new Label();
                cv.visitJumpInsn(IFEQ, l0);
                cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
                cv.visitInsn(ARETURN);
                cv.visitLabel(l0);
                cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
                cv.visitInsn(ARETURN);
            }
            else {
                if (isValidTypeForCast(returnType) && !returnType.equals(c.getName())) {
                    doConvertAndCast(returnType);
                }
                cv.visitInsn(ARETURN);
            }
        }

        outputReturn = true;
    }

    public void visitExpressionStatement(ExpressionStatement statement) {
        onLineNumber(statement);

        Expression expression = statement.getExpression();
        visitAndAutobox(expression);

        if (isPopRequired(expression)) {
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
                evaluateCompareTo(expression);
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
                //SPG don't use divide since BigInteger implements directly
                //and we want to dispatch through DefaultGroovyMethods to get a BigDecimal result
                evaluateBinaryExpression("div", expression);
                break;

            case Token.DIVIDE_EQUAL :
                //SPG don't use divide since BigInteger implements directly
                //and we want to dispatch through DefaultGroovyMethods to get a BigDecimal result
                evaluateBinaryExpressionWithAsignment("div", expression);
                break;

            case Token.LEFT_SHIFT :
                evaluateBinaryExpression("leftShift", expression);
                break;

            case Token.RIGHT_SHIFT :
                evaluateBinaryExpression("rightShift", expression);
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
                if (leftHandExpression) {
                    throw new RuntimeException("Should not be called");
                    //evaluateBinaryExpression("putAt", expression);
                }
                else {
                    evaluateBinaryExpression("getAt", expression);
                }
                break;

            default :
                throw new ClassGeneratorException("Operation: " + expression.getOperation() + " not supported");
        }
    }

    public void visitPostfixExpression(PostfixExpression expression) {
        switch (expression.getOperation().getType()) {
            case Token.PLUS_PLUS :
                evaluatePostfixMethod("next", expression.getExpression());
                break;
            case Token.MINUS_MINUS :
                evaluatePostfixMethod("previous", expression.getExpression());
                break;
        }
    }

    public void visitPrefixExpression(PrefixExpression expression) {
        switch (expression.getOperation().getType()) {
            case Token.PLUS_PLUS :
                evaluatePrefixMethod("next", expression.getExpression());
                break;
            case Token.MINUS_MINUS :
                evaluatePrefixMethod("previous", expression.getExpression());
                break;
        }
    }

    public void visitClosureExpression(ClosureExpression expression) {
        ClassNode innerClass = createClosureClass(expression);
        addInnerClass(innerClass);
        String innerClassinternalName = BytecodeHelper.getClassInternalName(innerClass.getName());

        ClassNode owner = innerClass.getOuterClass();
        String ownerTypeName = owner.getName();
        if (classNode.isStaticClass() || isStaticMethod()) {
            ownerTypeName = "java.lang.Class";
        }

        passingClosureParams = true;
        List constructors = innerClass.getConstructors();
        ConstructorNode node = (ConstructorNode) constructors.get(0);
        Parameter[] localVariableParams = node.getParameters();

        for (int i = 2; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String name = param.getName();

            if (variableStack.get(name) == null && classNode.getField(name) == null) {
                defineVariable(name, "java.lang.Object");
            }
        }

        /*
        if (classNode instanceof InnerClassNode) {
            // lets load the outer this
            int paramIdx = defineVariable(createVariableName("iterator"), "java.lang.Object", false).getIndex();
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, internalClassName, "owner", helper.getTypeDescription(ownerTypeName));
            cv.visitVarInsn(ASTORE, paramIdx);
        
            cv.visitTypeInsn(NEW, innerClassinternalName);
            cv.visitInsn(DUP);
            cv.visitVarInsn(ALOAD, paramIdx);
        }
        else {
        */
        cv.visitTypeInsn(NEW, innerClassinternalName);
        cv.visitInsn(DUP);
        if (isStaticMethod() || classNode.isStaticClass()) {
            visitClassExpression(new ClassExpression(ownerTypeName));
        }
        else {
            loadThisOrOwner();
        }
        /*
        }
        */

        if (innerClass.getSuperClass().equals("groovy.lang.Closure")) {
            if (isStaticMethod()) {
                /**
                 * @todo could maybe stash this expression in a JVM variable
                 * from previous statement above
                 */
                visitClassExpression(new ClassExpression(ownerTypeName));
            }
            else {
                loadThisOrOwner();
            }
        }

        //String prototype = "(L" + BytecodeHelper.getClassInternalName(ownerTypeName) + ";Ljava/lang/Object;";

        // now lets load the various parameters we're passing
        for (int i = 2; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String name = param.getName();

            if (variableStack.get(name) == null) {
                visitFieldExpression(new FieldExpression(classNode.getField(name)));
            }
            else {
                visitVariableExpression(new VariableExpression(name));
            }
            //prototype = prototype + "L" + BytecodeHelper.getClassInternalName(param.getType()) + ";";
        }
        passingClosureParams = false;

        // we may need to pass in some other constructors
        //cv.visitMethodInsn(INVOKESPECIAL, innerClassinternalName, "<init>", prototype + ")V");
        cv.visitMethodInsn(
            INVOKESPECIAL,
            innerClassinternalName,
            "<init>",
            BytecodeHelper.getMethodDescriptor("void", localVariableParams));
    }

    /**
     * Loads either this object or if we're inside a closure then load the top level owner
     */
    protected void loadThisOrOwner() {
        if (isInnerClass()) {
            visitFieldExpression(new FieldExpression(classNode.getField("owner")));
        }
        else {
            cv.visitVarInsn(ALOAD, 0);
        }
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
            String className = BytecodeHelper.getClassInternalName(value.getClass().getName());
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

    public void visitNegationExpression(NegationExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(this);
        negation.call(cv);
    }

    public void visitCastExpression(CastExpression expression) {
        String type = expression.getType();
        type = checkValidType(type, expression, "in cast");

        visitAndAutobox(expression.getExpression());

        doConvertAndCast(type);
    }

    public void visitNotExpression(NotExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(this);

        // This is not the best way to do this. Javac does it by reversing the
        // underlying expressions but that proved
        // fairly complicated for not much gain. Instead we'll just use a
        // utility function for now.
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
        /*
         * if (arguments instanceof TupleExpression) { TupleExpression
         * tupleExpression = (TupleExpression) arguments; int size =
         * tupleExpression.getExpressions().size(); if (size == 0) { arguments =
         * ConstantExpression.EMPTY_ARRAY; } }
         */

        boolean superMethodCall = MethodCallExpression.isSuperMethodCall(call);
        String method = call.getMethod();
        if (superMethodCall && method.equals("<init>")) {
            /** @todo handle method types! */
            cv.visitVarInsn(ALOAD, 0);
            cv.visitVarInsn(ALOAD, 1);
            cv.visitMethodInsn(INVOKESPECIAL, internalBaseClassName, "<init>", "(Ljava/lang/Object;)V");
        }
        else {
            // are we a local variable
            if (isThisExpression(call.getObjectExpression()) && isFieldOrVariable(call.getMethod())) {
                /*
                 * if (arguments instanceof TupleExpression) { TupleExpression
                 * tupleExpression = (TupleExpression) arguments; int size =
                 * tupleExpression.getExpressions().size(); if (size == 1) {
                 * arguments = (Expression)
                 * tupleExpression.getExpressions().get(0); } }
                 */

                // lets invoke the closure method
                visitVariableExpression(new VariableExpression(method));
                arguments.visit(this);
                invokeClosureMethod.call(cv);
            }
            else {
                if (emptyArguments(arguments) && !call.isSafe()) {
                    call.getObjectExpression().visit(this);
                    cv.visitLdcInsn(method);
                    invokeNoArgumentsMethod.call(cv);
                }
                else {
                    if (argumentsUseStack(arguments)) {
                        int paramIdx =
                            defineVariable(createVariableName("iterator"), "java.lang.Object", false).getIndex();

                        arguments.visit(this);

                        cv.visitVarInsn(ASTORE, paramIdx);

                        call.getObjectExpression().visit(this);

                        cv.visitLdcInsn(method);

                        cv.visitVarInsn(ALOAD, paramIdx);
                    }
                    else {
                        call.getObjectExpression().visit(this);
                        cv.visitLdcInsn(method);
                        arguments.visit(this);
                    }

                    if (superMethodCall) {
                        invokeSuperMethodMethod.call(cv);
                    }
                    else if (call.isSafe()) {
                        invokeMethodSafeMethod.call(cv);
                    }
                    else {
                        invokeMethodMethod.call(cv);
                    }
                }
            }
        }
    }

    protected boolean emptyArguments(Expression arguments) {
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            int size = tupleExpression.getExpressions().size();
            return size == 0;
        }
        return false;
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        this.leftHandExpression = false;

        Expression arguments = call.getArguments();
        if (emptyArguments(arguments)) {
            cv.visitLdcInsn(call.getType());
            cv.visitLdcInsn(call.getMethod());

            invokeStaticNoArgumentsMethod.call(cv);
        }
        else {
            if (arguments instanceof TupleExpression) {
                TupleExpression tupleExpression = (TupleExpression) arguments;
                int size = tupleExpression.getExpressions().size();
                if (size == 1) {
                    arguments = (Expression) tupleExpression.getExpressions().get(0);
                }
            }

            cv.visitLdcInsn(call.getType());
            cv.visitLdcInsn(call.getMethod());
            arguments.visit(this);

            invokeStaticMethodMethod.call(cv);
        }
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
         * cv.visitLdcInsn(type);
         * 
         * arguments.visit(this);
         * 
         * invokeConstructorMethod.call(cv);
         */
    }

    public void visitPropertyExpression(PropertyExpression expression) {

        // lets check if we're a fully qualified class name
        String className = checkForQualifiedClass(expression);
        if (className != null) {
            visitClassExpression(new ClassExpression(className));
            return;
        }
        Expression objectExpression = expression.getObjectExpression();
        if (expression.getProperty().equals("class")) {
            if ((objectExpression instanceof ClassExpression)) {
                visitClassExpression((ClassExpression) objectExpression);
                return;
            }
            else if (objectExpression instanceof VariableExpression) {
                VariableExpression varExp = (VariableExpression) objectExpression;
                className = varExp.getVariable();
                try {
                    className = resolveClassName(className);
                    visitClassExpression(new ClassExpression(className));
                    return;
                }
                catch (Exception e) {
                    // ignore
                }
            }
        }

        if (isThisExpression(objectExpression)) {
            // lets use the field expression if its available
            String name = expression.getProperty();
            FieldNode field = classNode.getField(name);
            if (field != null) {
                visitFieldExpression(new FieldExpression(field));
                return;
            }
        }

        boolean left = leftHandExpression;
        // we need to clear the LHS flag to avoid "this." evaluating as ASTORE
        // rather than ALOAD
        leftHandExpression = false;

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

    /**
     * Checks if the given property expression represents a fully qualified class name
     * @return the class name or null if the property is not a valid class name
     */
    protected String checkForQualifiedClass(PropertyExpression expression) {
        String text = expression.getText();
        try {
            return resolveClassName(text);
        }
        catch (Exception e) {
            if (text.endsWith(".class")) {
                text = text.substring(0, text.length() - 6);
                try {
                    return resolveClassName(text);
                }
                catch (Exception e2) {
                }
            }
            return null;
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
            if (isInClosureConstructor()) {
                helper.doCast(type);
            }
            else {
                // this may be superflous
                doConvertAndCast(type);
            }
        }
        int opcode =
            (leftHandExpression && !holder) ? ((isStatic) ? PUTSTATIC : PUTFIELD) : ((isStatic) ? GETSTATIC : GETFIELD);
        String ownerName =
            (field.getOwner().equals(classNode.getName()))
                ? internalClassName
                : org.objectweb.asm.Type.getInternalName(loadClass(field.getOwner()));

        if (holder) {
            if (leftHandExpression) {
                cv.visitVarInsn(ASTORE, tempIndex);

                cv.visitVarInsn(ALOAD, 0);
                cv.visitFieldInsn(opcode, ownerName, expression.getFieldName(), helper.getTypeDescription(type));

                cv.visitVarInsn(ALOAD, tempIndex);

                cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
            }
            else {
                cv.visitFieldInsn(opcode, ownerName, expression.getFieldName(), helper.getTypeDescription(type));
                cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
            }
        }
        else {
            cv.visitFieldInsn(opcode, ownerName, expression.getFieldName(), helper.getTypeDescription(type));
            if (!leftHandExpression && helper.isPrimitiveType(type)) {
                helper.box(type);
            }
        }
    }

    protected void visitOuterFieldExpression(FieldExpression expression) {
        ClassNode outerClassNode = classNode.getOuterClass();
        if (outerClassNode instanceof InnerClassNode) {
            visitPropertyExpression(
                new PropertyExpression(new FieldExpression(classNode.getField("owner")), expression.getFieldName()));
            return;
        }

        int valueIdx = idx + 1;

        if (leftHandExpression) {
            cv.visitVarInsn(ASTORE, valueIdx);
        }

        FieldNode field = expression.getField();
        boolean isStatic = field.isStatic();

        // lets load an extra one just in case as we may be getting & putting a
        // field
        if (!isStatic) {
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(
                GETFIELD,
                internalClassName,
                "owner",
                helper.getTypeDescription(outerClassNode.getName()));
        }

        int opcode = (leftHandExpression) ? ((isStatic) ? PUTSTATIC : PUTFIELD) : ((isStatic) ? GETSTATIC : GETFIELD);
        String ownerName = BytecodeHelper.getClassInternalName(outerClassNode.getName());

        if (leftHandExpression) {
            cv.visitVarInsn(ALOAD, valueIdx);
            boolean holder = field.isHolder() && !isInClosureConstructor();
            if (!holder) {
                doConvertAndCast(field.getType());
            }
        }

        cv.visitFieldInsn(opcode, ownerName, expression.getFieldName(), helper.getTypeDescription(field.getType()));
        /*
         * if (leftHandExpression) { cv.visitVarInsn(ALOAD, valueIdx); }
         */
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
        if (!variableName.equals("this")) {
            String className = resolveClassName(variableName);
            if (className != null) {
                if (leftHandExpression) {
                    throw new RuntimeParserException(
                        "Cannot use a class expression on the left hand side of an assignment",
                        expression);
                }
                visitClassExpression(new ClassExpression(className));
                return;
            }
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
                String variableType = expression.getType();
                boolean useProperty = isInScriptBody();
                if (!leftHandExpression) {
                    variable = (Variable) variableStack.get(name);
                }
                else {
                    if (!useProperty) {
                        variable = defineVariable(name, variableType);
                    }
                }
                if (variable == null || variable.isProperty()) {
                    if (variable == null) {
                        variable = defineVariable(name, variableType);
                        variable.setProperty(true);
                    }
                    //variable = defineVariable(name, variableType, false);
                    if (variable.isHolder() && passingClosureParams && useProperty) {
                        // lets create a ScriptReference to pass into the closure
                        cv.visitTypeInsn(NEW, "org/codehaus/groovy/runtime/ScriptReference");
                        cv.visitInsn(DUP);

                        loadThisOrOwner();
                        cv.visitLdcInsn(name);

                        cv.visitMethodInsn(
                            INVOKESPECIAL,
                            "org/codehaus/groovy/runtime/ScriptReference",
                            "<init>",
                            "(Lgroovy/lang/Script;Ljava/lang/String;)V");
                    }
                    else {
                        visitPropertyExpression(new PropertyExpression(VariableExpression.THIS_EXPRESSION, name));
                    }
                    // We need to store this in a local variable now since it
                    // has been looked at in this scope and possibly
                    // compared and it hasn't been referenced before.
                    //cv.visitInsn(DUP);
                    //cv.visitVarInsn(ASTORE, variable.getIndex());
                    return;
                }
                String type = variable.getTypeName();
                int index = variable.getIndex();
                //lastVariableIndex = index;
                boolean holder = variable.isHolder() && !passingClosureParams;

                if (leftHandExpression) {
                    if (holder) {
                        int tempIndex = defineVariable(createVariableName("reference"), variableType, false).getIndex();

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

                        /*
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
                        */
                        cv.visitVarInsn(ALOAD, index);
                    }
                }
            }
        }
    }

    /**
     * @return true if we are in a script body, where all variables declared are no longer
     * local variables but are properties
     */
    protected boolean isInScriptBody() {
        if (classNode.isScriptBody()) {
            return true;
        }
        else {
            return classNode.isScript() && methodNode != null && methodNode.getName().equals("run");
        }
    }

    /**
     * @return true if this expression will have left a value on the stack
     * that must be popped
     */
    protected boolean isPopRequired(Expression expression) {
        if (expression instanceof MethodCallExpression) {
            return !MethodCallExpression.isSuperMethodCall((MethodCallExpression) expression);
        }
        if (expression instanceof BinaryExpression) {
            BinaryExpression binExp = (BinaryExpression) expression;
            switch (binExp.getOperation().getType()) {
                case Token.EQUAL :
                case Token.PLUS_EQUAL :
                case Token.MINUS_EQUAL :
                case Token.MULTIPLY_EQUAL :
                case Token.DIVIDE_EQUAL :
                case Token.MOD_EQUAL :
                    return false;
            }
        }
        return true;
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
            cw.visitField(ACC_STATIC + ACC_SYNTHETIC, staticFieldName, "Ljava/lang/Class;", null, null);
        }

        if (!syntheticStaticFields.isEmpty()) {
            cv =
                cw.visitMethod(
                    ACC_STATIC + ACC_SYNTHETIC,
                    "class$",
                    "(Ljava/lang/String;)Ljava/lang/Class;",
                    null,
                    null);
            helper = new BytecodeHelper(cv);

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

        if (helper.isPrimitiveType(type)) {
            String objectType = helper.getObjectTypeForPrimitive(type);
            cv.visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(objectType), "TYPE", "Ljava/lang/Class;");
        }
        else {
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
    }

    public void visitRangeExpression(RangeExpression expression) {
        leftHandExpression = false;
        expression.getFrom().visit(this);

        leftHandExpression = false;
        expression.getTo().visit(this);

        helper.pushConstant(expression.isInclusive());

        createRangeMethod.call(cv);
    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
    }

    public void visitMapExpression(MapExpression expression) {
        List entries = expression.getMapEntryExpressions();
        int size = entries.size();
        helper.pushConstant(size * 2);

        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        int i = 0;
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            MapEntryExpression entry = (MapEntryExpression) iter.next();

            cv.visitInsn(DUP);
            helper.pushConstant(i++);
            visitAndAutobox(entry.getKeyExpression());
            cv.visitInsn(AASTORE);

            cv.visitInsn(DUP);
            helper.pushConstant(i++);
            visitAndAutobox(entry.getValueExpression());
            cv.visitInsn(AASTORE);
        }
        createMapMethod.call(cv);
    }

    public void visitTupleExpression(TupleExpression expression) {
        int size = expression.getExpressions().size();

        helper.pushConstant(size);

        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            helper.pushConstant(i);
            visitAndAutobox(expression.getExpression(i));
            cv.visitInsn(AASTORE);
        }
        //createTupleMethod.call(cv);
    }

    public void visitArrayExpression(ArrayExpression expression) {
        String typeName = BytecodeHelper.getClassInternalName(expression.getType());
        Expression sizeExpression = expression.getSizeExpression();
        if (sizeExpression != null) {
            // lets convert to an int
            visitAndAutobox(sizeExpression);
            asIntMethod.call(cv);

            cv.visitTypeInsn(ANEWARRAY, typeName);
        }
        else {
            int size = expression.getExpressions().size();
            helper.pushConstant(size);

            cv.visitTypeInsn(ANEWARRAY, typeName);

            for (int i = 0; i < size; i++) {
                cv.visitInsn(DUP);
                helper.pushConstant(i);
                Expression elementExpression = expression.getExpression(i);
                if (elementExpression == null) {
                    ConstantExpression.NULL.visit(this);
                }
                else {
                    visitAndAutobox(elementExpression);
                }
                cv.visitInsn(AASTORE);
            }
        }
    }

    public void visitListExpression(ListExpression expression) {
        int size = expression.getExpressions().size();
        helper.pushConstant(size);

        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            helper.pushConstant(i);
            visitAndAutobox(expression.getExpression(i));
            cv.visitInsn(AASTORE);
        }
        createListMethod.call(cv);
    }

    public void visitGStringExpression(GStringExpression expression) {
        int size = expression.getValues().size();
        helper.pushConstant(size);

        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            helper.pushConstant(i);
            visitAndAutobox(expression.getValue(i));
            cv.visitInsn(AASTORE);
        }

        int paramIdx = defineVariable(createVariableName("iterator"), "java.lang.Object", false).getIndex();
        cv.visitVarInsn(ASTORE, paramIdx);

        ClassNode innerClass = createGStringClass(expression);
        addInnerClass(innerClass);
        String innerClassinternalName = BytecodeHelper.getClassInternalName(innerClass.getName());

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
        ClassNode owner = getOutermostClass();
        boolean parentIsInnerClass = owner instanceof InnerClassNode;
        String outerClassName = owner.getName();
        String name = outerClassName + "$" + context.getNextInnerClassIdx();
        boolean staticMethodOrInStaticClass = isStaticMethod() || classNode.isStaticClass();
        if (staticMethodOrInStaticClass) {
            outerClassName = "java.lang.Class";
        }
        Parameter[] parameters = expression.getParameters();
        if (parameters == null || parameters.length == 0) {
            // lets create a default 'it' parameter
            parameters = new Parameter[] { new Parameter("it")};
        }

        Parameter[] localVariableParams = getClosureSharedVariables(expression);

        InnerClassNode answer = new InnerClassNode(owner, name, ACC_PUBLIC, "groovy.lang.Closure");
        if (staticMethodOrInStaticClass) {
            answer.setStaticClass(true);
        }
        if (isInScriptBody()) {
            answer.setScriptBody(true);
        }
        MethodNode method =
            answer.addMethod("doCall", ACC_PUBLIC, "java.lang.Object", parameters, expression.getCode());

        method.setLineNumber(expression.getLineNumber());
        method.setColumnNumber(expression.getColumnNumber());

        VariableScope scope = expression.getVariableScope();
        if (scope == null) {
            throw new RuntimeException(
                "Must have a VariableScope by now! for expression: " + expression + " class: " + name);
        }
        else {
            method.setVariableScope(scope);
        }
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
                param.makeReference();
            }

            FieldNode paramField = null;
            if (holder) {
                paramField = answer.addField(paramName, ACC_PRIVATE, type, initialValue);
                paramField.setHolder(true);
                String realType = param.getRealType();
                String methodName = Verifier.capitalize(paramName);

                // lets add a getter & setter
                Expression fieldExp = new FieldExpression(paramField);
                answer.addMethod(
                    "get" + methodName,
                    ACC_PUBLIC,
                    realType,
                    Parameter.EMPTY_ARRAY,
                    new ReturnStatement(fieldExp));

                /*
                answer.addMethod(
                    "set" + methodName,
                    ACC_PUBLIC,
                    "void",
                    new Parameter[] { new Parameter(realType, "__value") },
                    new ExpressionStatement(
                        new BinaryExpression(expression, Token.equal(0, 0), new VariableExpression("__value"))));
                        */
            }
            else {
                PropertyNode propertyNode = answer.addProperty(paramName, ACC_PUBLIC, type, initialValue, null, null);
                paramField = propertyNode.getField();
            }

            if (!holder) {
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

    protected ClassNode getOutermostClass() {
        if (outermostClass == null) {
            outermostClass = classNode;
            while (outermostClass instanceof InnerClassNode) {
                outermostClass = outermostClass.getOuterClass();
            }
        }
        return outermostClass;
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

    protected void doConvertAndCast(String type) {
        if (!type.equals("java.lang.Object")) {
            /** @todo should probably support array coercions */
            if (!type.endsWith("[]") && isValidTypeForCast(type)) {
                visitClassExpression(new ClassExpression(type));
                asTypeMethod.call(cv);
            }

            helper.doCast(type);
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
        leftHandExpression = false;
        leftExpression.visit(this);
        cv.visitLdcInsn(method);
        leftHandExpression = false;
        new ArgumentListExpression(new Expression[] { expression.getRightExpression()}).visit(this);
        // expression.getRightExpression().visit(this);
        invokeMethodMethod.call(cv);
    }

    protected void evaluateCompareTo(BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        leftHandExpression = false;
        leftExpression.visit(this);
        expression.getRightExpression().visit(this);
        compareToMethod.call(cv);
    }

    protected void evaluateBinaryExpressionWithAsignment(String method, BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        if (leftExpression instanceof BinaryExpression) {
            BinaryExpression leftBinExpr = (BinaryExpression) leftExpression;
            if (leftBinExpr.getOperation().getType() == Token.LEFT_SQUARE_BRACKET) {
                // lets replace this assignment to a subscript operator with a
                // method call
                // e.g. x[5] += 10
                // -> (x, [], 5), =, x[5] + 10
                // -> methodCall(x, "putAt", [5, methodCall(x[5], "plus", 10)])

                MethodCallExpression methodCall =
                    new MethodCallExpression(
                        expression.getLeftExpression(),
                        method,
                        new ArgumentListExpression(new Expression[] { expression.getRightExpression()}));

                Expression safeIndexExpr = createReusableExpression(leftBinExpr.getRightExpression());

                visitMethodCallExpression(
                    new MethodCallExpression(
                        leftBinExpr.getLeftExpression(),
                        "putAt",
                        new ArgumentListExpression(new Expression[] { safeIndexExpr, methodCall })));
                cv.visitInsn(POP);
                return;
            }
        }

        evaluateBinaryExpression(method, expression);

        leftHandExpression = true;
        leftExpression.visit(this);
        leftHandExpression = false;
    }

    protected void evaluateBinaryExpression(MethodCaller compareMethod, BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        /*
        if (isNonStaticField(leftExpression)) {
            cv.visitVarInsn(ALOAD, 0);
        }
        */

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
                // lets replace this assignment to a subscript operator with a
                // method call
                // e.g. x[5] = 10
                // -> (x, [], 5), =, 10
                // -> methodCall(x, "putAt", [5, 10])
                visitMethodCallExpression(
                    new MethodCallExpression(
                        leftBinExpr.getLeftExpression(),
                        "putAt",
                        new ArgumentListExpression(
                            new Expression[] { leftBinExpr.getRightExpression(), expression.getRightExpression()})));
                cv.visitInsn(POP);
                return;
            }
        }
        if (isNonStaticField(leftExpression)) {
            cv.visitVarInsn(ALOAD, 0);
        }

        // lets evaluate the RHS then hopefully the LHS will be a field
        leftHandExpression = false;
        Expression rightExpression = expression.getRightExpression();

        String type = getLHSType(leftExpression);
        if (type != null) {
            //System.out.println("### expression: " + leftExpression);
            //System.out.println("### type: " + type);

            // lets not cast for primitive types as we handle these in field setting etc
            if (helper.isPrimitiveType(type)) {
                rightExpression.visit(this);
            }
            else {
                visitCastExpression(new CastExpression(type, rightExpression));
            }
        }
        else {
            visitAndAutobox(rightExpression);
        }

        leftHandExpression = true;
        leftExpression.visit(this);
        leftHandExpression = false;
    }

    /**
     * Deduces the type name required for some casting
     * 
     * @return the type of the given (LHS) expression or null if it is java.lang.Object or it cannot be deduced
     */
    protected String getLHSType(Expression leftExpression) {
        if (leftExpression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) leftExpression;
            String type = varExp.getType();
            if (isValidTypeForCast(type)) {
                return type;
            }
            String variableName = varExp.getVariable();
            Variable variable = (Variable) variableStack.get(variableName);
            if (variable != null) {
                if (variable.isHolder() || variable.isProperty()) {
                    return null;
                }
                type = variable.getTypeName();
                if (isValidTypeForCast(type)) {
                    return type;
                }
            }
            else {
                FieldNode field = classNode.getField(variableName);
                if (field == null) {
                    field = classNode.getOuterField(variableName);
                }
                if (field != null) {
                    type = field.getType();
                    if (!field.isHolder() && isValidTypeForCast(type)) {
                        return type;
                    }
                }
            }
        }
        return null;
    }

    protected boolean isValidTypeForCast(String type) {
        return type != null && !type.equals("java.lang.Object") && !type.equals("groovy.lang.Reference");
    }

    protected void visitAndAutobox(Expression expression) {
        expression.visit(this);

        if (comparisonExpression(expression)) {
            Label l0 = new Label();
            cv.visitJumpInsn(IFEQ, l0);
            cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
            Label l1 = new Label();
            cv.visitJumpInsn(GOTO, l1);
            cv.visitLabel(l0);
            cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
            cv.visitLabel(l1);
        }
    }

    protected void evaluatePrefixMethod(String method, Expression expression) {
        expression.visit(this);
        cv.visitLdcInsn(method);
        ArgumentListExpression.EMPTY_ARGUMENTS.visit(this);
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
        /*
         * if (! isStaticMethod() && ! isHolderVariable(expression)) {
         * cv.visitVarInsn(ALOAD, 0); }
         */
        cv.visitVarInsn(ALOAD, tempIdx);

        cv.visitLdcInsn(method);
        ArgumentListExpression.EMPTY_ARGUMENTS.visit(this);
        invokeMethodMethod.call(cv);

        leftHandExpression = true;
        expression.visit(this);
        leftHandExpression = false;

        cv.visitVarInsn(ALOAD, tempIdx);
    }

    protected boolean isHolderVariable(Expression expression) {
        if (expression instanceof FieldExpression) {
            FieldExpression fieldExp = (FieldExpression) expression;
            return fieldExp.getField().isHolder();
        }
        if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            FieldNode field = classNode.getField(varExp.getVariable());
            Variable variable = (Variable) variableStack.get(varExp.getVariable());
            if (field != null && variable == null) {
                return field.isHolder();
            }
            return variable.isHolder();
        }
        return false;
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
        String classInternalName = BytecodeHelper.getClassInternalName(className);
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
        else if (expression instanceof PropertyExpression) {
            PropertyExpression fieldExp = (PropertyExpression) expression;
            field = classNode.getField(fieldExp.getProperty());
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

    /**
     * For assignment expressions, return a safe expression for the LHS we can use
     * to return the value 
     */
    protected Expression createReturnLHSExpression(Expression expression) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binExpr = (BinaryExpression) expression;
            if (binExpr.getOperation().isAssignmentToken()) {
                return createReusableExpression(binExpr.getLeftExpression());
            }
        }
        return null;
    }

    protected Expression createReusableExpression(Expression expression) {
        ExpressionTransformer transformer = new ExpressionTransformer() {
            public Expression transform(Expression expression) {
                if (expression instanceof PostfixExpression) {
                    PostfixExpression postfixExp = (PostfixExpression) expression;
                    return postfixExp.getExpression();
                }
                else if (expression instanceof PrefixExpression) {
                    PrefixExpression prefixExp = (PrefixExpression) expression;
                    return prefixExp.getExpression();
                }
                return expression;
            }
        };

        // could just be a postfix / prefix expression or nested inside some other expression
        return transformer.transform(expression.transformExpression(transformer));
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

    protected void popScope() {
        scope = scope.getParent();
    }

    protected void pushBlockScope() {
        scope = new BlockScope(scope);
    }

    protected VariableScope getVariableScope() {
        if (variableScope == null) {
            if (methodNode != null) {
                // if we're a closure method we'll have our variable scope already created
                variableScope = methodNode.getVariableScope();
                if (variableScope == null) {
                    variableScope = new VariableScope();
                    methodNode.setVariableScope(variableScope);
                    VariableScopeCodeVisitor visitor = new VariableScopeCodeVisitor(variableScope);
                    visitor.setParameters(methodNode.getParameters());
                    Statement code = methodNode.getCode();
                    if (code != null) {
                        code.visit(visitor);
                    }
                }
                addFieldsToVisitor(variableScope);
            }
            else if (constructorNode != null) {
                variableScope = new VariableScope();
                constructorNode.setVariableScope(variableScope);
                VariableScopeCodeVisitor visitor = new VariableScopeCodeVisitor(variableScope);
                visitor.setParameters(constructorNode.getParameters());
                Statement code = constructorNode.getCode();
                if (code != null) {
                    code.visit(visitor);
                }
                addFieldsToVisitor(variableScope);
            }
            else {
                throw new RuntimeException("Can't create a variable scope outside of a method or constructor");
            }
        }
        return variableScope;
    }

    /**
     * @return a list of parameters for each local variable which needs to be
     *         passed into a closure
     */
    protected Parameter[] getClosureSharedVariables(ClosureExpression expression) {
        List vars = new ArrayList();
        VariableScope outerScope = getVariableScope().createRecursiveParentScope();
        VariableScope innerScope = expression.getVariableScope();
        if (innerScope == null) {
            System.out.println(
                "No variable scope for: " + expression + " method: " + methodNode + " constructor: " + constructorNode);
            innerScope = new VariableScope(getVariableScope());
        }
        else {
            innerScope = innerScope.createRecursiveChildScope();
        }

        // now any variables declared in the outer context that are referred to
        // in the inner context need to be copied
        Set outerDecls = outerScope.getDeclaredVariables();
        Set outerRefs = outerScope.getReferencedVariables();
        Set innerDecls = innerScope.getDeclaredVariables();
        Set innerRefs = innerScope.getReferencedVariables();

        Set varSet = new HashSet();
        for (Iterator iter = innerRefs.iterator(); iter.hasNext();) {
            String var = (String) iter.next();
            // lets not pass in fields from the most-outer class, but pass in values from an outer closure
            if (outerDecls.contains(var) && (isNotFieldOfOutermostClass(var))) {
                String type = getVariableType(var);
                vars.add(new Parameter(type, var));
                varSet.add(var);
            }
        }
        for (Iterator iter = outerRefs.iterator(); iter.hasNext();) {
            String var = (String) iter.next();
            // lets not pass in fields from the most-outer class, but pass in values from an outer closure
            if (innerDecls.contains(var) && (isNotFieldOfOutermostClass(var)) && !varSet.contains(var)) {
                String type = getVariableType(var);
                vars.add(new Parameter(type, var));
            }
        }
        Parameter[] answer = new Parameter[vars.size()];
        vars.toArray(answer);
        return answer;
    }

    protected boolean isNotFieldOfOutermostClass(String var) {
        //return classNode.getField(var) == null || isInnerClass();
        return getOutermostClass().getField(var) == null;
    }

    protected void findMutableVariables() {
        /*
        VariableScopeCodeVisitor outerVisitor = new VariableScopeCodeVisitor(true);
        node.getCode().visit(outerVisitor);
        
        addFieldsToVisitor(outerVisitor);
        
        VariableScopeCodeVisitor innerVisitor = outerVisitor.getClosureVisitor();
        */
        VariableScope outerScope = getVariableScope();

        // lets create a scope concatenating all the closure expressions
        VariableScope innerScope = outerScope.createCompositeChildScope();

        Set outerDecls = outerScope.getDeclaredVariables();
        Set outerRefs = outerScope.getReferencedVariables();
        Set innerDecls = innerScope.getDeclaredVariables();
        Set innerRefs = innerScope.getReferencedVariables();

        mutableVars.clear();

        for (Iterator iter = innerDecls.iterator(); iter.hasNext();) {
            String var = (String) iter.next();
            if ((outerDecls.contains(var) || outerRefs.contains(var)) && classNode.getField(var) == null) {
                mutableVars.add(var);
            }
        }

        // we may call the closure twice and modify the variable in the outer scope
        // so for now lets assume that all variables are mutable
        for (Iterator iter = innerRefs.iterator(); iter.hasNext();) {
            String var = (String) iter.next();
            if (outerDecls.contains(var) && classNode.getField(var) == null) {
                mutableVars.add(var);
            }
        }

        //                System.out.println();
        //                System.out.println("method: " + methodNode + " classNode: " + classNode);
        //                System.out.println("child scopes: " + outerScope.getChildren());
        //                System.out.println("outerDecls: " + outerDecls);
        //                System.out.println("outerRefs: " + outerRefs);
        //                System.out.println("innerDecls: " + innerDecls);
        //                System.out.println("innerRefs: " + innerRefs);
    }

    protected void addFieldsToVisitor(VariableScope scope) {
        for (Iterator iter = classNode.getFields().iterator(); iter.hasNext();) {
            FieldNode field = (FieldNode) iter.next();
            String name = field.getName();

            scope.getDeclaredVariables().add(name);
            scope.getReferencedVariables().add(name);
        }
    }

    private boolean isInnerClass() {
        return classNode instanceof InnerClassNode;
    }

    protected String getVariableType(String name) {
        Variable variable = (Variable) variableStack.get(name);
        if (variable != null) {
            return variable.getTypeName();
        }
        return null;
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
            Parameter parameter = parameters[i];
            String type = parameter.getType();
            int idx = defineVariable(parameter.getName(), type).getIndex();
            if (helper.isPrimitiveType(type)) {
                helper.load(type, idx);
                helper.box(type);
                cv.visitVarInsn(ASTORE, idx);
            }
        }
        definingParameters = false;
    }

    protected Variable defineVariable(String name, String type, boolean define) {
        return defineVariable(name, new Type(type), define);
    }

    private Variable defineVariable(String name, Type type, boolean define) {
        Variable answer = (Variable) variableStack.get(name);
        if (answer == null) {
            idx = Math.max(idx, variableStack.size());
            answer = new Variable(idx, type, name);
            if (mutableVars.contains(name)) {
                answer.setHolder(true);
            }
            variableStack.put(name, answer);
            if (define) {
                if (definingParameters) {
                    if (answer.isHolder()) {
                        cv.visitTypeInsn(NEW, "groovy/lang/Reference");
                        cv.visitInsn(DUP);
                        cv.visitVarInsn(ALOAD, idx);
                        cv.visitMethodInsn(INVOKESPECIAL, "groovy/lang/Reference", "<init>", "(Ljava/lang/Object;)V");
                        cv.visitVarInsn(ASTORE, idx);
                    }
                }
                else {
                    // using new variable inside a comparison expression
                    // so lets initialize it too
                    if (answer.isHolder() && !isInScriptBody()) {
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
        }
        return answer;
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

    protected Type checkValidType(Type type, ASTNode node, String message) {
        if (type.isDynamic()) {
            return type;
        }
        String name = checkValidType(type.getName(), node, message);
        if (type.getName().equals(name)) {
            return type;
        }
        return new Type(name);
    }

    protected String checkValidType(String type, ASTNode node, String message) {
        if (helper.isPrimitiveType(type)) {
            return type;
        }
        String original = type;
        type = resolveClassName(type);
        if (type != null) {
            return type;
        }
        throw new MissingClassException(original, node, message);
    }

    protected String resolveClassName(String type) {
        return classNode.resolveClassName(type);
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
    protected boolean isPrimitiveFieldType(String type) {
        return type.equals("java.lang.String")
            || type.equals("java.lang.Integer")
            || type.equals("java.lang.Double")
            || type.equals("java.lang.Long")
            || type.equals("java.lang.Float");
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
     * @return loads the given type name
     */
    protected Class loadClass(String name) {
        try {
            CompileUnit compileUnit = getCompileUnit();
            if (compileUnit != null) {
                return compileUnit.loadClass(name);
            }
            else {
                throw new ClassGeneratorException("Could not load class: " + name);
            }
        }
        catch (ClassNotFoundException e) {
            throw new ClassGeneratorException("Could not load class: " + name + " reason: " + e, e);
        }
    }

    protected CompileUnit getCompileUnit() {
        CompileUnit answer = classNode.getCompileUnit();
        if (answer == null) {
            answer = context.getCompileUnit();
        }
        return answer;
    }
}

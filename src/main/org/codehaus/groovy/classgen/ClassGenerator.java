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
package org.codehaus.groovy.classgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.AssertStatement;
import org.codehaus.groovy.ast.BinaryExpression;
import org.codehaus.groovy.ast.BooleanExpression;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ClosureExpression;
import org.codehaus.groovy.ast.ConstantExpression;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DoWhileLoop;
import org.codehaus.groovy.ast.Expression;
import org.codehaus.groovy.ast.ExpressionStatement;
import org.codehaus.groovy.ast.FieldExpression;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ForLoop;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.IfElse;
import org.codehaus.groovy.ast.ListExpression;
import org.codehaus.groovy.ast.MapEntryExpression;
import org.codehaus.groovy.ast.MapExpression;
import org.codehaus.groovy.ast.MethodCallExpression;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyExpression;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.RangeExpression;
import org.codehaus.groovy.ast.RegexExpression;
import org.codehaus.groovy.ast.ReturnStatement;
import org.codehaus.groovy.ast.Statement;
import org.codehaus.groovy.ast.TryCatchFinally;
import org.codehaus.groovy.ast.TupleExpression;
import org.codehaus.groovy.ast.VariableExpression;
import org.codehaus.groovy.ast.WhileLoop;
import org.codehaus.groovy.runtime.InvokerHelper;
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

    private static final String[] EMPTY_PARAMETER_TYPES = {
    };

    private ClassVisitor classVisitor;
    private ClassLoader classLoader;
    private CodeVisitor cv;
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
    MethodCaller getPropertyMethod = MethodCaller.newStatic(InvokerHelper.class, "getProperty");
    MethodCaller setPropertyMethod = MethodCaller.newStatic(InvokerHelper.class, "setProperty");
    MethodCaller asIteratorMethod = MethodCaller.newStatic(InvokerHelper.class, "asIterator");

    MethodCaller compareIdenticalMethod = MethodCaller.newStatic(InvokerHelper.class, "compareIdentical");
    MethodCaller compareEqualMethod = MethodCaller.newStatic(InvokerHelper.class, "compareEqual");
    MethodCaller compareNotEqualMethod = MethodCaller.newStatic(InvokerHelper.class, "compareNotEqual");
    MethodCaller compareLessThanMethod = MethodCaller.newStatic(InvokerHelper.class, "compareLessThan");
    MethodCaller compareLessThanEqualMethod = MethodCaller.newStatic(InvokerHelper.class, "compareLessThanEqual");
    MethodCaller compareGreaterThanMethod = MethodCaller.newStatic(InvokerHelper.class, "compareGreaterThan");
    MethodCaller compareGreaterThanEqualMethod = MethodCaller.newStatic(InvokerHelper.class, "compareGreaterThanEqual");

    MethodCaller createListMethod = MethodCaller.newStatic(InvokerHelper.class, "createList");
    MethodCaller createTupleMethod = MethodCaller.newStatic(InvokerHelper.class, "createTuple");
    MethodCaller createMapMethod = MethodCaller.newStatic(InvokerHelper.class, "createMap");
    MethodCaller createRangeMethod = MethodCaller.newStatic(InvokerHelper.class, "createRange");

    MethodCaller assertFailedMethod = MethodCaller.newStatic(InvokerHelper.class, "assertFailed");

    MethodCaller iteratorNextMethod = MethodCaller.newInterface(Iterator.class, "next");
    MethodCaller iteratorHasNextMethod = MethodCaller.newInterface(Iterator.class, "hasNext");

    // current stack index
    private int idx;

    public ClassGenerator(ClassVisitor classVisitor, ClassLoader classLoader, String sourceFile) {
        this.classVisitor = classVisitor;
        this.classLoader = classLoader;
        this.sourceFile = sourceFile;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    // GroovyClassVisitor interface
    //-------------------------------------------------------------------------
    public void visitClass(ClassNode classNode) {
        this.classNode = classNode;
        this.internalClassName = getClassInternalName(classNode.getName());
        this.internalBaseClassName = getClassInternalName(classNode.getSuperClass());
        classVisitor.visit(
            classNode.getModifiers(),
            internalClassName,
            internalBaseClassName,
            getClassInternalNames(classNode.getInterfaces()),
            sourceFile);

        ensureClassNodeHasConstructor(classNode);

        // now lets visit the contents of the class
        for (Iterator iter = classNode.getProperties().iterator(); iter.hasNext();) {
            visitProperty((PropertyNode) iter.next());
        }

        for (Iterator iter = classNode.getFields().iterator(); iter.hasNext();) {
            visitField((FieldNode) iter.next());
        }

        for (Iterator iter = classNode.getConstructors().iterator(); iter.hasNext();) {
            visitConstructor((ConstructorNode) iter.next());
        }

        for (Iterator iter = classNode.getMethods().iterator(); iter.hasNext();) {
            visitMethod((MethodNode) iter.next());
        }

        classVisitor.visitEnd();
    }

    public void visitConstructor(ConstructorNode node) {
        // creates a MethodWriter for the (implicit) constructor
        //String methodType = Type.getMethodDescriptor(VOID_TYPE, )

        String methodType = getMethodDescriptor("void", node.getParameters());
        cv = classVisitor.visitMethod(node.getModifiers(), "<init>", methodType, null);

        resetVariableStack(node.getParameters());

        // invokes the super class constructor
        cv.visitVarInsn(ALOAD, 0);
        cv.visitMethodInsn(INVOKESPECIAL, internalBaseClassName, "<init>", "()V");

        cv.visitInsn(RETURN);
        cv.visitMaxs(0, 0);
    }

    public void visitMethod(MethodNode node) {
        //System.out.println("Visiting method: " + node.getName() + " with return type: " + node.getReturnType());

        String methodType = getMethodDescriptor(node.getReturnType(), node.getParameters());
        cv = classVisitor.visitMethod(node.getModifiers(), node.getName(), methodType, null);

        resetVariableStack(node.getParameters());

        outputReturn = false;

        node.getCode().visit(this);

        if (!outputReturn) {
            cv.visitInsn(RETURN);
        }
        cv.visitMaxs(0, 0);
    }

    public void visitField(FieldNode fieldNode) {
        onLineNumber(fieldNode);

        //System.out.println("Visiting field: " + fieldNode.getName() + " on class: " + classNode.getName());

        Object fieldValue = null;
        Expression expression = fieldNode.getInitialValueExpression();
        if (expression instanceof ConstantExpression) {
            ConstantExpression constantExp = (ConstantExpression) expression;
            Object value = constantExp.getValue();
            if (isPrimitiveFieldType(value)) {
                fieldValue = value;
            }
        }
        classVisitor.visitField(
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

    public void visitForLoop(ForLoop loop) {
        onLineNumber(loop);

        loop.getCollectionExpression().visit(this);

        asIteratorMethod.call(cv);

        cv.visitVarInsn(ASTORE, ++idx);

        Label label1 = new Label();
        cv.visitJumpInsn(GOTO, label1);
        Label label2 = new Label();
        cv.visitLabel(label2);

        cv.visitVarInsn(ALOAD, idx);

        iteratorNextMethod.call(cv);

        cv.visitVarInsn(ASTORE, ++idx);

        loop.getLoopBlock().visit(this);

        cv.visitLabel(label1);
        cv.visitVarInsn(ALOAD, --idx);

        iteratorHasNextMethod.call(cv);

        cv.visitJumpInsn(IFNE, label2);
    }

    public void visitWhileLoop(WhileLoop loop) {
        onLineNumber(loop);

        // TODO Auto-generated method stub

    }

    public void visitDoWhileLoop(DoWhileLoop loop) {
        onLineNumber(loop);

        // TODO Auto-generated method stub

    }

    public void visitIfElse(IfElse ifElse) {
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
            int line = statement.getLineNumber();
            boolean first = true;

            // lets create a new expression
            cv.visitTypeInsn(NEW, "java/lang/StringBuffer");
            cv.visitInsn(DUP);
            cv.visitLdcInsn(expressionText + ". Values: ");

            cv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "(Ljava/lang/String;)V");
            cv.visitVarInsn(ASTORE, ++idx);

            for (Iterator iter = list.iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String text = name + " = ";
                if (first) {
                    first = false;
                }
                else {
                    text = ", " + name;
                }

                cv.visitVarInsn(ALOAD, idx);
                cv.visitLdcInsn(text);
                cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "java/lang/StringBuffer",
                    "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
                cv.visitInsn(POP);

                cv.visitVarInsn(ALOAD, idx);
                new VariableExpression(name).visit(this);
                cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "java/lang/StringBuffer",
                    "append",
                    "(Ljava/lang/Object;)Ljava/lang/StringBuffer;");
                cv.visitInsn(POP);

            }
            cv.visitVarInsn(ALOAD, idx);
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

    public void visitTryCatchFinally(TryCatchFinally statement) {
        onLineNumber(statement);

        // TODO Auto-generated method stub

    }

    public void visitReturnStatement(ReturnStatement statement) {
        onLineNumber(statement);

        statement.getExpression().visit(this);

        Class c = getExpressionType(statement.getExpression());
        //return is based on class type
        //TODO: make work with arrays
        if (!c.isPrimitive()) {
            cv.visitInsn(ARETURN);
        }
        else {
            if (c == double.class) {
                cv.visitInsn(DRETURN);
            }
            else if (c == float.class) {
                cv.visitInsn(FRETURN);
            }
            else if (c == long.class) {
                cv.visitInsn(LRETURN);
            }
            else { //byte,short,boolean,int are all IRETURN
                cv.visitInsn(IRETURN);
            }
        }
        outputReturn = true;
    }

    public void visitExpressionStatement(ExpressionStatement statement) {
        onLineNumber(statement);

        statement.getExpression().visit(this);
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

            case Token.PLUS :
                evaluateBinaryExpression("plus", expression);
                break;

            case Token.MINUS :
                evaluateBinaryExpression("minus", expression);
                break;

            case Token.MULTIPLY :
                evaluateBinaryExpression("multiply", expression);
                break;

            case Token.DIVIDE :
                evaluateBinaryExpression("divide", expression);
                break;

            default :
                throw new ClassGeneratorException("Operation: " + expression.getOperation() + " not supported");
        }
    }

    public void visitClosureExpression(ClosureExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitRegexExpression(RegexExpression expression) {
        // TODO Auto-generated method stub

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
        else {
            throw new ClassGeneratorException(
                "Cannot generate bytecode for constant: " + value + " of type: " + value.getClass().getName());
        }
    }

    public void visitFieldExpression(FieldExpression expression) {
        FieldNode field = expression.getField();
        boolean isStatic = field.isStatic();

        if (!isStatic && !leftHandExpression) {
            cv.visitVarInsn(ALOAD, 0);
        }
        int opcode = (leftHandExpression) ? ((isStatic) ? PUTSTATIC : PUTFIELD) : ((isStatic) ? GETSTATIC : GETFIELD);
        String ownerName =
            (field.getOwner().equals(classNode.getName()))
                ? internalClassName
                : Type.getInternalName(loadClass(field.getOwner()));

        cv.visitFieldInsn(opcode, ownerName, expression.getFieldName(), getTypeDescription(field.getType()));

        // lets push this back on the stack 
        //        if (! isStatic && leftHandExpression) {
        //            cv.visitVarInsn(ALOAD, 0);
        //        }
    }

    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this);
    }

    public void visitMethodCallExpression(MethodCallExpression call) {
        this.leftHandExpression = false;

        call.getObjectExpression().visit(this);

        cv.visitLdcInsn(call.getMethod());

        call.getArguments().visit(this);

        invokeMethodMethod.call(cv);

        cv.visitInsn(POP);
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        this.leftHandExpression = false;

        expression.getObjectExpression().visit(this);

        cv.visitLdcInsn(expression.getProperty());

        getPropertyMethod.call(cv);

        cv.visitInsn(POP);
    }

    public void visitVariableExpression(VariableExpression expression) {
        // lets see if the variable is a field
        FieldNode field = classNode.getField(expression.getVariable());
        if (field != null) {
            visitFieldExpression(new FieldExpression(field));
        }
        else {
            String name = expression.getVariable();
            Variable variable = defineVariable(name, "java.lang.Object");
            String type = variable.getType();
            int index = variable.getIndex();

            if (leftHandExpression) {
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
                else if (type.equals("byte") || type.equals("short") || type.equals("boolean") || type.equals("int")) {
                    cv.visitVarInsn(ISTORE, index);
                }
                else {
                    cv.visitVarInsn(ASTORE, index);
                }
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
                else if (type.equals("byte") || type.equals("short") || type.equals("boolean") || type.equals("int")) {
                    cv.visitVarInsn(ILOAD, index);
                }
                else {
                    cv.visitVarInsn(ALOAD, index);
                }
            }
        }
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
        createTupleMethod.call(cv);
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

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Adds a default constructor if there isn't one already
     * 
     * @todo refactor as part of the analyzer
     */
    protected void ensureClassNodeHasConstructor(ClassNode node) {
        if (node.getConstructors().isEmpty()) {
            node.addConstructor(new ConstructorNode(ACC_PUBLIC, null));
        }
    }

    protected void evaluateBinaryExpression(String method, BinaryExpression expression) {
        leftHandExpression = false;
        expression.getLeftExpression().visit(this);

        cv.visitLdcInsn(method);

        leftHandExpression = false;
        expression.getRightExpression().visit(this);

        invokeMethodMethod.call(cv);
    }

    protected void evaluateBinaryExpression(MethodCaller compareMethod, BinaryExpression expression) {
        leftHandExpression = false;
        expression.getLeftExpression().visit(this);

        leftHandExpression = false;
        expression.getRightExpression().visit(this);

        // now lets invoke the method
        compareMethod.call(cv);
    }

    protected void evaluateEqual(BinaryExpression expression) {
        // lets evaluate the RHS then hopefully the LHS will be a field

        // this may be redundant - we should maybe do this after each method call?
        cv.visitVarInsn(ALOAD, 0);

        leftHandExpression = false;
        expression.getRightExpression().visit(this);

        leftHandExpression = true;
        expression.getLeftExpression().visit(this);
        leftHandExpression = false;
    }

    protected void onLineNumber(Statement statement) {
        int number = statement.getLineNumber();
        if (number >= 0) {
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
     * @return the last ID used by the stack
     */
    protected int getLastStackId() {
        return variableStack.size();
    }

    protected void resetVariableStack(Parameter[] parameters) {
        idx = 0;
        variableStack.clear();

        // lets push this onto the stack
        defineVariable("this", classNode.getName()).getIndex();
        //cv.visitVarInsn(ALOAD, idx);

        // now lets create indices for the parameteres
        for (int i = 0; i < parameters.length; i++) {
            defineVariable(parameters[i].getName(), parameters[i].getType());
        }
    }

    /**
     * Defines the given variable in scope and assigns it to the stack
     */
    protected Variable defineVariable(String name, String type) {
        Variable answer = (Variable) variableStack.get(name);
        if (answer == null) {
            idx = Math.max(idx, variableStack.size());
            answer = new Variable(idx, type, name);
            variableStack.put(name, answer);
        }
        return answer;
    }

    /**
     * @return looks up the given variable name
     */
    protected Variable lookupVariable(String name) {
        Variable answer = (Variable) variableStack.get(name);
        if (answer == null) {
            throw new ClassGeneratorException("Undefined variable: " + name);
        }
        return answer;
    }

    /**
     * @return if the type of the expression can be determined at compile time then 
     * this method returns the type - otherwise java.lang.Object is returned.
     */
    protected Class getExpressionType(Expression expression) {
        /** @todo we need a way to determine this from an expression */
        return Object.class;
    }

    /**
     * @return true if the value is an Integer, a Float, a Long, a Double or a String .
     */
    protected boolean isPrimitiveFieldType(Object value) {
        return value instanceof String
            || value instanceof Integer
            || value instanceof Double
            || value instanceof Long
            || value instanceof Float;
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
        return name.replace('.', '/');
    }

    /**
     * @return the ASM method type descriptor
     */
    protected String getMethodDescriptor(String returnTypeName, Parameter[] paramTypeNames) {
        Type returnType = getType(returnTypeName);
        Type[] paramTypes = new Type[paramTypeNames.length];
        for (int i = 0; i < paramTypeNames.length; i++) {
            paramTypes[i] = getType(paramTypeNames[i].getType());
        }
        return Type.getMethodDescriptor(returnType, paramTypes);
    }

    /**
     * @return the ASM type description
     */
    protected String getTypeDescription(String name) {
        return getType(name).getDescriptor();
    }

    /**
     * @return the ASM type for the given class name
     */
    protected Type getType(String className) {
        if (className.equals("void")) {
            return Type.VOID_TYPE;
        }
        return Type.getType(loadClass(className));
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

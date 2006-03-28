/*
$Id$

Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

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

import groovy.lang.GroovyRuntimeException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NegationExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.RegexExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
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
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;


/**
 * Generates Java class versions of Groovy classes using ASM.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:b55r@sina.com">Bing Ran</a>
 * @author Jochen Theodorou
 *
 * @version $Revision$
 */
public class AsmClassGenerator extends ClassGenerator {

    private Logger log = Logger.getLogger(getClass().getName());

    private ClassVisitor cw;
    private MethodVisitor cv;
    private GeneratorContext context;

    private String sourceFile;

    // current class details
    private ClassNode classNode;
    private ClassNode outermostClass;
    private String internalClassName;
    private String internalBaseClassName;

    /** maps the variable names to the JVM indices */
    private CompileStack compileStack;

    /** have we output a return statement yet */
    private boolean outputReturn;

    /** are we on the left or right of an expression */
    private boolean leftHandExpression;

    // cached values
    MethodCaller invokeMethodMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeMethod");
    MethodCaller invokeMethodSafeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeMethodSafe");
    MethodCaller invokeMethodSpreadSafeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeMethodSpreadSafe");
    MethodCaller invokeStaticMethodMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeStaticMethod");
    MethodCaller invokeConstructorMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeConstructor");
    MethodCaller invokeConstructorOfMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeConstructorOf");
    MethodCaller invokeNoArgumentsConstructorOf = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeNoArgumentsConstructorOf");
    MethodCaller invokeConstructorAtMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeConstructorAt");
    MethodCaller invokeNoArgumentsConstructorAt = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeNoArgumentsConstructorAt");
    MethodCaller invokeClosureMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeClosure");
    MethodCaller invokeSuperMethodMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeSuperMethod");
    MethodCaller invokeNoArgumentsMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeNoArgumentsMethod");
    MethodCaller invokeNoArgumentsSafeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeNoArgumentsSafeMethod");
    MethodCaller invokeNoArgumentsSpreadSafeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeNoArgumentsSpreadSafeMethod");
    MethodCaller invokeStaticNoArgumentsMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeStaticNoArgumentsMethod");

    MethodCaller asIntMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "asInt");
    MethodCaller asTypeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "asType");

    MethodCaller getAttributeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getAttribute");
    MethodCaller getAttributeSafeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getAttributeSafe");
    MethodCaller getAttributeSpreadSafeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getAttributeSpreadSafe");
    MethodCaller setAttributeMethod2 = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "setAttribute2");
    MethodCaller setAttributeSafeMethod2 = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "setAttributeSafe2");

    MethodCaller getPropertyMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getProperty");
    MethodCaller getPropertySafeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getPropertySafe");
    MethodCaller getPropertySpreadSafeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getPropertySpreadSafe");
    MethodCaller setPropertyMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "setProperty");
    MethodCaller setPropertyMethod2 = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "setProperty2");
    MethodCaller setPropertySafeMethod2 = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "setPropertySafe2");

    MethodCaller getGroovyObjectPropertyMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getGroovyObjectProperty");
    MethodCaller setGroovyObjectPropertyMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "setGroovyObjectProperty");
    MethodCaller asIteratorMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "asIterator");
    MethodCaller asBool = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "asBool");
    MethodCaller notBoolean = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "notBoolean");
    MethodCaller notObject = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "notObject");
    MethodCaller regexPattern = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "regexPattern");
    MethodCaller spreadList = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "spreadList");
    MethodCaller spreadMap = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "spreadMap");
    MethodCaller getMethodPointer = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getMethodPointer");
    MethodCaller negation = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "negate");
    MethodCaller bitNegation = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "bitNegate");
    MethodCaller convertPrimitiveArray = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "convertPrimitiveArray");
    MethodCaller convertToPrimitiveArray = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "convertToPrimitiveArray");

    MethodCaller compareIdenticalMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareIdentical");
    MethodCaller compareEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareEqual");
    MethodCaller compareNotEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareNotEqual");
    MethodCaller compareToMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareTo");
    MethodCaller findRegexMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "findRegex");
    MethodCaller matchRegexMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "matchRegex");
    MethodCaller compareLessThanMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareLessThan");
    MethodCaller compareLessThanEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareLessThanEqual");
    MethodCaller compareGreaterThanMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareGreaterThan");
    MethodCaller compareGreaterThanEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareGreaterThanEqual");
    MethodCaller isCaseMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "isCase");

    MethodCaller createListMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createList");
    MethodCaller createTupleMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createTuple");
    MethodCaller createMapMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createMap");
    MethodCaller createRangeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createRange");

    MethodCaller assertFailedMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "assertFailed");

    MethodCaller iteratorNextMethod = MethodCaller.newInterface(Iterator.class, "next");
    MethodCaller iteratorHasNextMethod = MethodCaller.newInterface(Iterator.class, "hasNext");

    
    // exception blocks list
    private List exceptionBlocks = new ArrayList();

    private Set syntheticStaticFields = new HashSet();
    private boolean passingClosureParams;

    private ConstructorNode constructorNode;
    private MethodNode methodNode;
    private BytecodeHelper helper = new BytecodeHelper(null);

    public static final boolean CREATE_DEBUG_INFO = true;
    public static final boolean CREATE_LINE_NUMBER_INFO = true;
    private static final boolean MARK_START = true;

    /*public static final String EB_SWITCH_NAME = "static.dispatching";
    public boolean ENABLE_EARLY_BINDING;
    {    //
        String ebSwitch = (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty(EB_SWITCH_NAME, "false"); // set default to true if early binding is on by default.
            }
        });
        //System.out.println("ebSwitch = " + ebSwitch);
        if (ebSwitch.equals("true")) {
            ENABLE_EARLY_BINDING  = true;
        }
        else if (ebSwitch.equals("false")) {
            ENABLE_EARLY_BINDING  = false;
        }
        else {
            ENABLE_EARLY_BINDING  = false;
            log.warning("The value of system property " + EB_SWITCH_NAME + " is not recognized. Late dispatching is assumed. ");
        }
    }*/
    public static final boolean ASM_DEBUG = false; // add marker in the bytecode to show source-byecode relationship
    private int lineNumber = -1;
    private int columnNumber = -1;
    private ASTNode currentASTNode = null;

    private DummyClassGenerator dummyGen = null;
    private ClassWriter dummyClassWriter = null;
    

    public AsmClassGenerator(
            GeneratorContext context, ClassVisitor classVisitor,
            ClassLoader classLoader, String sourceFile
    ) {
        super(classLoader);
        this.context = context;
        this.cw = classVisitor;
        this.sourceFile = sourceFile;

        this.dummyClassWriter = new ClassWriter(true);
        dummyGen  = new DummyClassGenerator(context, dummyClassWriter, classLoader, sourceFile);
        compileStack = new CompileStack();

    }
    
    protected SourceUnit getSourceUnit() {
        return null;
    }

    // GroovyClassVisitor interface
    //-------------------------------------------------------------------------
    public void visitClass(ClassNode classNode) {
        // todo to be tested
        // createDummyClass(classNode);

        try {
            syntheticStaticFields.clear();
            this.classNode = classNode;
            this.outermostClass = null;
            this.internalClassName = BytecodeHelper.getClassInternalName(classNode);

            this.internalBaseClassName = BytecodeHelper.getClassInternalName(classNode.getSuperClass());

            cw.visit(
                asmJDKVersion,
                classNode.getModifiers(),
                internalClassName,
                null,
                internalBaseClassName,
                BytecodeHelper.getClassInternalNames(classNode.getInterfaces())
            );            
            cw.visitSource(sourceFile,null);
            
            super.visitClass(classNode);

            // set the optional enclosing method attribute of the current inner class
//          br comment out once Groovy uses the latest CVS HEAD of ASM
//            MethodNode enclosingMethod = classNode.getEnclosingMethod();
//            String ownerName = BytecodeHelper.getClassInternalName(enclosingMethod.getDeclaringClass().getName());
//            String descriptor = BytecodeHelper.getMethodDescriptor(enclosingMethod.getReturnType(), enclosingMethod.getParameters());
//            EnclosingMethodAttribute attr = new EnclosingMethodAttribute(ownerName,enclosingMethod.getName(),descriptor);
//            cw.visitAttribute(attr);

            createSyntheticStaticFields();

            for (Iterator iter = innerClasses.iterator(); iter.hasNext();) {
                ClassNode innerClass = (ClassNode) iter.next();
                String innerClassName = innerClass.getName();
                String innerClassInternalName = BytecodeHelper.getClassInternalName(innerClassName);
                {
                    int index = innerClassName.lastIndexOf('$');
                    if (index>=0) innerClassName = innerClassName.substring(index+1);
                }
                String outerClassName = internalClassName; // default for inner classes
                MethodNode enclosingMethod = innerClass.getEnclosingMethod();
                if (enclosingMethod != null) {
                    // local inner classes do not specify the outer class name
                    outerClassName = null;
                    innerClassName = null;
                }
                cw.visitInnerClass(
                    innerClassInternalName,
                    outerClassName,
                    innerClassName,
                    innerClass.getModifiers());
            }
            // br TODO an inner class should have an entry of itself
            cw.visitEnd();
        }
        catch (GroovyRuntimeException e) {
            e.setModule(classNode.getModule());
            throw e;
        }
    }
    
    private String[] buildExceptions(ClassNode[] exceptions) {
        if (exceptions==null) return null;
        String[] ret = new String[exceptions.length];
        for (int i = 0; i < exceptions.length; i++) {
            ret[i] = BytecodeHelper.getClassInternalName(exceptions[i]);
        }
        return ret;
    }
    
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        String methodType = BytecodeHelper.getMethodDescriptor(node.getReturnType(), node.getParameters());

        cv = cw.visitMethod(node.getModifiers(), node.getName(), methodType, null, buildExceptions(node.getExceptions()));
        helper = new BytecodeHelper(cv);
        if (!node.isAbstract()) { 
            Statement code = node.getCode();
            if (isConstructor && (code == null || !firstStatementIsSpecialConstructorCall(node))) {
                // invokes the super class constructor
                cv.visitVarInsn(ALOAD, 0);
                cv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(classNode.getSuperClass()), "<init>", "()V");
            }
            
            compileStack.init(node.getVariableScope(),node.getParameters(),cv, BytecodeHelper.getTypeDescription(classNode));
            
            super.visitConstructorOrMethod(node, isConstructor);
            
            if (!outputReturn || node.isVoidMethod()) {
                cv.visitInsn(RETURN);
            }
            
            compileStack.clear();
            
            // lets do all the exception blocks
            for (Iterator iter = exceptionBlocks.iterator(); iter.hasNext();) {
                Runnable runnable = (Runnable) iter.next();
                runnable.run();
            }
            exceptionBlocks.clear();
    
            cv.visitMaxs(0, 0);
        }
    }

    private boolean firstStatementIsSpecialConstructorCall(MethodNode node) {
        Statement code = node.getFirstStatement();
        if (code == null || !(code instanceof ExpressionStatement)) return false;

        Expression expression = ((ExpressionStatement)code).getExpression();
        if (!(expression instanceof ConstructorCallExpression)) return false;
        ConstructorCallExpression cce = (ConstructorCallExpression) expression;
        return cce.isSpecialCall();
    }

    public void visitConstructor(ConstructorNode node) {
        this.constructorNode = node;
        this.methodNode = null;
        outputReturn = false;
        super.visitConstructor(node);
    }

    public void visitMethod(MethodNode node) {
        this.constructorNode = null;
        this.methodNode = node;
        outputReturn = false;
        
        super.visitMethod(node);
    }

    public void visitField(FieldNode fieldNode) {
        onLineNumber(fieldNode, "visitField: " + fieldNode.getName());
        ClassNode t = fieldNode.getType();
        cw.visitField(
            fieldNode.getModifiers(),
            fieldNode.getName(),
            BytecodeHelper.getTypeDescription(t),
            null, //fieldValue,  //br  all the sudden that one cannot init the field here. init is done in static initilizer and instace intializer.
            null);
        visitAnnotations(fieldNode);
    }

    public void visitProperty(PropertyNode statement) {
        // the verifyer created the field and the setter/getter methods, so here is
        // not really something to do
        onLineNumber(statement, "visitProperty:" + statement.getField().getName());
        this.methodNode = null;
    }

    // GroovyCodeVisitor interface
    //-------------------------------------------------------------------------

    // Statements
    //-------------------------------------------------------------------------

    protected void visitStatement(Statement statement) {
        String name = statement.getStatementLabel();
        if (name!=null) {
            Label label = compileStack.createLocalLabel(name);
            cv.visitLabel(label);
        }
    }
    
    public void visitBlockStatement(BlockStatement block) {
        onLineNumber(block, "visitBlockStatement");
        visitStatement(block);
        
        compileStack.pushVariableScope(block.getVariableScope());
        super.visitBlockStatement(block);
        compileStack.pop();
    }

    public void visitForLoop(ForStatement loop) {
        onLineNumber(loop, "visitForLoop");
        visitStatement(loop);

        compileStack.pushLoop(loop.getVariableScope(),loop.getStatementLabel());

        //
        // Declare the loop counter.
        Variable variable = compileStack.defineVariable(loop.getVariable(),false);

        //
        // Then initialize the iterator and generate the loop control
        loop.getCollectionExpression().visit(this);

        asIteratorMethod.call(cv);

        final int iteratorIdx = compileStack.defineTemporaryVariable("iterator", ClassHelper.make(java.util.Iterator.class),true);

        Label continueLabel = compileStack.getContinueLabel();
        Label breakLabel = compileStack.getBreakLabel();
        
        cv.visitLabel(continueLabel);
        cv.visitVarInsn(ALOAD, iteratorIdx);
        iteratorHasNextMethod.call(cv);
        // note: ifeq tests for ==0, a boolean is 0 if it is false
        cv.visitJumpInsn(IFEQ, breakLabel);
        
        cv.visitVarInsn(ALOAD, iteratorIdx);
        iteratorNextMethod.call(cv);
        helper.storeVar(variable);

        // Generate the loop body
        loop.getLoopBlock().visit(this);

        cv.visitJumpInsn(GOTO, continueLabel);        
        cv.visitLabel(breakLabel);
        
        compileStack.pop();
    }

    public void visitWhileLoop(WhileStatement loop) {
        onLineNumber(loop, "visitWhileLoop");
        visitStatement(loop);

        compileStack.pushLoop(loop.getStatementLabel());
        Label continueLabel = compileStack.getContinueLabel();
        Label breakLabel = compileStack.getBreakLabel();
        
        cv.visitLabel(continueLabel);
        loop.getBooleanExpression().visit(this);
        cv.visitJumpInsn(IFEQ, breakLabel);
        
        loop.getLoopBlock().visit(this);
        
        cv.visitJumpInsn(GOTO, continueLabel);
        cv.visitLabel(breakLabel);
        
        compileStack.pop();
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        onLineNumber(loop, "visitDoWhileLoop");
        visitStatement(loop);

        compileStack.pushLoop(loop.getStatementLabel());
        Label breakLabel = compileStack.getBreakLabel();
        Label continueLabel = compileStack.getContinueLabel();
        cv.visitLabel(continueLabel);

        loop.getLoopBlock().visit(this);

        loop.getBooleanExpression().visit(this);
        cv.visitJumpInsn(IFEQ, continueLabel);
        cv.visitLabel(breakLabel);
        
        compileStack.pop();
    }

    public void visitIfElse(IfStatement ifElse) {
        onLineNumber(ifElse, "visitIfElse");
        visitStatement(ifElse);
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
        onLineNumber(expression, "visitTernaryExpression");

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
        onLineNumber(statement, "visitAssertStatement");
        visitStatement(statement);

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

            int tempIndex = compileStack.defineTemporaryVariable("assert",true);

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
                    "(Ljava/lang/Object;)Ljava/lang/StringBuffer;");
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
            compileStack.removeVar(tempIndex);
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
            list.add(varExp.getName());
        }
    }

    public void visitTryCatchFinally(TryCatchStatement statement) {
        onLineNumber(statement, "visitTryCatchFinally");
        visitStatement(statement);
        
// todo need to add blockscope handling
        CatchStatement catchStatement = statement.getCatchStatement(0);

        Statement tryStatement = statement.getTryStatement();

        if (tryStatement.isEmpty() || catchStatement == null) {
            final Label l0 = new Label();
            cv.visitLabel(l0);

            tryStatement.visit(this);


            int index1 = compileStack.defineTemporaryVariable("exception",false);
            int index2 = compileStack.defineTemporaryVariable("exception",false);

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
            int finallySubAddress = compileStack.defineTemporaryVariable("exception",false);
            int anyExceptionIndex = compileStack.defineTemporaryVariable("exception",false);

            // start try block, label needed for exception table
            final Label tryStart = new Label();
            cv.visitLabel(tryStart);
            tryStatement.visit(this);
            // goto finally part
            final Label finallyStart = new Label();
            cv.visitJumpInsn(GOTO, finallyStart);
            // marker needed for Exception table
            final Label tryEnd = new Label();
            cv.visitLabel(tryEnd);
            
            for (Iterator it=statement.getCatchStatements().iterator(); it.hasNext();) {
                catchStatement = (CatchStatement) it.next();
                ClassNode exceptionType = catchStatement.getExceptionType();
                // start catch block, label needed for exception table
                final Label catchStart = new Label();
                cv.visitLabel(catchStart);
                // create exception variable and store the exception 
                compileStack.defineVariable(catchStatement.getVariable(),true);
                // handle catch body
                catchStatement.visit(this);
                // goto finally start
                cv.visitJumpInsn(GOTO, finallyStart);
                // add exception to table
                final String exceptionTypeInternalName = BytecodeHelper.getClassInternalName(exceptionType);
                exceptionBlocks.add(new Runnable() {
                    public void run() {
                        cv.visitTryCatchBlock(tryStart, tryEnd, catchStart, exceptionTypeInternalName);
                    }
                });
            }
            
            // marker needed for the exception table
            final Label endOfAllCatches = new Label();
            cv.visitLabel(endOfAllCatches);
            
            // start finally
            cv.visitLabel(finallyStart);
            Label finallySub = new Label();
            // run finally sub
            cv.visitJumpInsn(JSR, finallySub);
            // goto end of finally
            Label afterFinally = new Label();
            cv.visitJumpInsn(GOTO, afterFinally);
            
            // start a block catching any Exception
            final Label catchAny = new Label();
            cv.visitLabel(catchAny);
            //store exception
            cv.visitVarInsn(ASTORE, anyExceptionIndex);
            // run finally subroutine
            cv.visitJumpInsn(JSR, finallySub);
            // load the exception and rethrow it
            cv.visitVarInsn(ALOAD, anyExceptionIndex);
            cv.visitInsn(ATHROW);
            
            // start the finally subroutine
            cv.visitLabel(finallySub);
            // store jump address
            cv.visitVarInsn(ASTORE, finallySubAddress);
            if (!statement.getFinallyStatement().isEmpty())
                statement.getFinallyStatement().visit(this);
            // return from subroutine
            cv.visitVarInsn(RET, finallySubAddress);
            
            // end of all catches and finally parts
            cv.visitLabel(afterFinally);
            
            // add catch any block to exception table
            exceptionBlocks.add(new Runnable() {
                public void run() {
                    cv.visitTryCatchBlock(tryStart, endOfAllCatches, catchAny, null);
                }
            });
        }
    }
    
    public void visitSwitch(SwitchStatement statement) {
        onLineNumber(statement, "visitSwitch");
        visitStatement(statement);

        statement.getExpression().visit(this);

        // switch does not have a continue label. use its parent's for continue
        Label breakLabel = compileStack.pushSwitch();
        
        int switchVariableIndex = compileStack.defineTemporaryVariable("switch",true);

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

        compileStack.pop();
    }

    public void visitCaseStatement(CaseStatement statement) {
    }

    public void visitCaseStatement(
        CaseStatement statement,
        int switchVariableIndex,
        Label thisLabel,
        Label nextLabel) {

        onLineNumber(statement, "visitCaseStatement");

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
        onLineNumber(statement, "visitBreakStatement");
        visitStatement(statement);
        
        String name = statement.getLabel();
        Label breakLabel;
        if (name!=null) {
        	breakLabel = compileStack.getNamedBreakLabel(name);
        } else {
        	breakLabel= compileStack.getBreakLabel();
        }
        cv.visitJumpInsn(GOTO, breakLabel);
    }

    public void visitContinueStatement(ContinueStatement statement) {
        onLineNumber(statement, "visitContinueStatement");
        visitStatement(statement);
        
        String name = statement.getLabel();
        Label continueLabel = compileStack.getContinueLabel();
        if (name!=null) continueLabel = compileStack.getNamedContinueLabel(name);
        cv.visitJumpInsn(GOTO, continueLabel);
    }

    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        onLineNumber(statement, "visitSynchronizedStatement");
        visitStatement(statement);
        
        statement.getExpression().visit(this);

        int index = compileStack.defineTemporaryVariable("synchronized", ClassHelper.Integer_TYPE,true);

        cv.visitVarInsn(ALOAD, index);
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
        onLineNumber(statement, "visitThrowStatement");
        visitStatement(statement);
        
        statement.getExpression().visit(this);

        // we should infer the type of the exception from the expression
        cv.visitTypeInsn(CHECKCAST, "java/lang/Throwable");

        cv.visitInsn(ATHROW);
    }

    public void visitReturnStatement(ReturnStatement statement) {
        onLineNumber(statement, "visitReturnStatement");
        visitStatement(statement);
        
        ClassNode returnType = methodNode.getReturnType();
        if (returnType==ClassHelper.VOID_TYPE) {
        	if (!(statement == ReturnStatement.RETURN_NULL_OR_VOID)) {
                throwException("Cannot use return statement with an expression on a method that returns void");
        	}
            cv.visitInsn(RETURN);
            outputReturn = true;
            return;
        }

        Expression expression = statement.getExpression();
        evaluateExpression(expression);
        if (returnType==ClassHelper.OBJECT_TYPE && expression.getType() != null && expression.getType()==ClassHelper.VOID_TYPE) {
            cv.visitInsn(ACONST_NULL); // cheat the caller
            cv.visitInsn(ARETURN);
        } else {
            // return is based on class type
            // we may need to cast
            helper.unbox(returnType);
            if (returnType==ClassHelper.double_TYPE) {
                cv.visitInsn(DRETURN);
            }
            else if (returnType==ClassHelper.float_TYPE) {
                cv.visitInsn(FRETURN);
            }
            else if (returnType==ClassHelper.long_TYPE) {
                cv.visitInsn(LRETURN);
            }
            else if (returnType==ClassHelper.boolean_TYPE) {
                cv.visitInsn(IRETURN);
            }
            else if (
                       returnType==ClassHelper.char_TYPE
                    || returnType==ClassHelper.byte_TYPE
                    || returnType==ClassHelper.int_TYPE
                    || returnType==ClassHelper.short_TYPE) 
            { 
                //byte,short,boolean,int are all IRETURN
                cv.visitInsn(IRETURN);
            }
            else {
                doConvertAndCast(returnType, expression, false, true);
                cv.visitInsn(ARETURN);
            }
        }
        outputReturn = true;
    }

    /**
     * Casts to the given type unless it can be determined that the cast is unnecessary
     */
    protected void doConvertAndCast(ClassNode type, Expression expression, boolean ignoreAutoboxing, boolean forceCast) {
        ClassNode expType = getExpressionType(expression);
        // temp resolution: convert all primitive casting to corresponsing Object type
        if (!ignoreAutoboxing && ClassHelper.isPrimitiveType(type)) {
            type = ClassHelper.getWrapper(type);
        }
        if (forceCast || (type!=null && !type.equals(expType))) {
            doConvertAndCast(type);
        }
    }    

    /**
     * @param expression
     */
    protected void evaluateExpression(Expression expression) {
        visitAndAutoboxBoolean(expression);
        //expression.visit(this);

        Expression assignExpr = createReturnLHSExpression(expression);
        if (assignExpr != null) {
            leftHandExpression = false;
            assignExpr.visit(this);
        }
    }

    public void visitExpressionStatement(ExpressionStatement statement) {
        onLineNumber(statement, "visitExpressionStatement: " + statement.getExpression().getClass().getName());
        visitStatement(statement);
        
        Expression expression = statement.getExpression();
// disabled in favor of JIT resolving
//        if (ENABLE_EARLY_BINDING)
//            expression.resolve(this);

        visitAndAutoboxBoolean(expression);

        if (isPopRequired(expression)) {
            cv.visitInsn(POP);
        }
    }

    // Expressions
    //-------------------------------------------------------------------------

    public void visitDeclarationExpression(DeclarationExpression expression) {
        onLineNumber(expression, "visitDeclarationExpression: \""+expression.getVariableExpression().getName()+"\"");

        Expression rightExpression = expression.getRightExpression();
        // no need to visit left side, just get the variable name
        VariableExpression vex = expression.getVariableExpression();
        ClassNode type = vex.getType();
        // lets not cast for primitive types as we handle these in field setting etc
        if (ClassHelper.isPrimitiveType(type)) {
            rightExpression.visit(this);
        } else {
            if (type!=ClassHelper.OBJECT_TYPE){
                visitCastExpression(new CastExpression(type, rightExpression));
            } else {
                visitAndAutoboxBoolean(rightExpression);
            }
        }
        compileStack.defineVariable(vex,true);
    }
    
    public void visitBinaryExpression(BinaryExpression expression) {
        onLineNumber(expression, "visitBinaryExpression: \"" + expression.getOperation().getText() + "\" ");
        switch (expression.getOperation().getType()) {
            case Types.EQUAL : // = assignment
                evaluateEqual(expression);
                break;

            case Types.COMPARE_IDENTICAL : // ===
                evaluateBinaryExpression(compareIdenticalMethod, expression);
                break;

            case Types.COMPARE_EQUAL : // ==
                evaluateBinaryExpression(compareEqualMethod, expression);
                break;

            case Types.COMPARE_NOT_EQUAL :
                evaluateBinaryExpression(compareNotEqualMethod, expression);
                break;

            case Types.COMPARE_TO :
                evaluateCompareTo(expression);
                break;

            case Types.COMPARE_GREATER_THAN :
                evaluateBinaryExpression(compareGreaterThanMethod, expression);
                break;

            case Types.COMPARE_GREATER_THAN_EQUAL :
                evaluateBinaryExpression(compareGreaterThanEqualMethod, expression);
                break;

            case Types.COMPARE_LESS_THAN :
                evaluateBinaryExpression(compareLessThanMethod, expression);
                break;

            case Types.COMPARE_LESS_THAN_EQUAL :
                evaluateBinaryExpression(compareLessThanEqualMethod, expression);
                break;

            case Types.LOGICAL_AND :
                evaluateLogicalAndExpression(expression);
                break;

            case Types.LOGICAL_OR :
                evaluateLogicalOrExpression(expression);
                break;

	    case Types.BITWISE_AND :
                evaluateBinaryExpression("and", expression);
                break;

            case Types.BITWISE_AND_EQUAL :
                evaluateBinaryExpressionWithAsignment("and", expression);
                break;

            case Types.BITWISE_OR :
                evaluateBinaryExpression("or", expression);
                break;

            case Types.BITWISE_OR_EQUAL :
                evaluateBinaryExpressionWithAsignment("or", expression);
                break;

            case Types.BITWISE_XOR :
                evaluateBinaryExpression("xor", expression);
                break;

            case Types.BITWISE_XOR_EQUAL :
                evaluateBinaryExpressionWithAsignment("xor", expression);
                break;

            case Types.PLUS :
                evaluateBinaryExpression("plus", expression);
                break;

            case Types.PLUS_EQUAL :
                evaluateBinaryExpressionWithAsignment("plus", expression);
                break;
                
            case Types.MINUS :
                evaluateBinaryExpression("minus", expression);
                break;
                
            case Types.MINUS_EQUAL :
                evaluateBinaryExpressionWithAsignment("minus", expression);
                break;

            case Types.MULTIPLY :
                evaluateBinaryExpression("multiply", expression);
                break;

            case Types.MULTIPLY_EQUAL :
                evaluateBinaryExpressionWithAsignment("multiply", expression);
                break;

            case Types.DIVIDE :
                evaluateBinaryExpression("div", expression);
                break;

            case Types.DIVIDE_EQUAL :
                //SPG don't use divide since BigInteger implements directly
                //and we want to dispatch through DefaultGroovyMethods to get a BigDecimal result
                evaluateBinaryExpressionWithAsignment("div", expression);
                break;

            case Types.INTDIV :
                evaluateBinaryExpression("intdiv", expression);
                break;

            case Types.INTDIV_EQUAL :
                evaluateBinaryExpressionWithAsignment("intdiv", expression);
                break;

            case Types.MOD :
                evaluateBinaryExpression("mod", expression);
                break;

            case Types.MOD_EQUAL :
                evaluateBinaryExpressionWithAsignment("mod", expression);
                break;

            case Types.POWER :
                evaluateBinaryExpression("power", expression);
                break;

            case Types.POWER_EQUAL :
                evaluateBinaryExpressionWithAsignment("power", expression);
                break;

            case Types.LEFT_SHIFT :
                evaluateBinaryExpression("leftShift", expression);
                break;

            case Types.LEFT_SHIFT_EQUAL :
                evaluateBinaryExpressionWithAsignment("leftShift", expression);
                break;

            case Types.RIGHT_SHIFT :
                evaluateBinaryExpression("rightShift", expression);
                break;

            case Types.RIGHT_SHIFT_EQUAL :
                evaluateBinaryExpressionWithAsignment("rightShift", expression);
                break;

            case Types.RIGHT_SHIFT_UNSIGNED :
                evaluateBinaryExpression("rightShiftUnsigned", expression);
                break;

            case Types.RIGHT_SHIFT_UNSIGNED_EQUAL :
                evaluateBinaryExpressionWithAsignment("rightShiftUnsigned", expression);
                break;

            case Types.KEYWORD_INSTANCEOF :
                evaluateInstanceof(expression);
                break;

            case Types.FIND_REGEX :
                evaluateBinaryExpression(findRegexMethod, expression);
                break;

            case Types.MATCH_REGEX :
                evaluateBinaryExpression(matchRegexMethod, expression);
                break;

            case Types.LEFT_SQUARE_BRACKET :
                if (leftHandExpression) {
                    throwException("Should not be called here. Possible reason: postfix operation on array.");
                    // This is handled right now in the evaluateEqual()
                    // should support this here later
                    //evaluateBinaryExpression("putAt", expression);
                } else {
                    evaluateBinaryExpression("getAt", expression);
                }
                break;

            default :
                throwException("Operation: " + expression.getOperation() + " not supported");
        }
    }

    private void load(Expression exp) {

        boolean wasLeft = leftHandExpression;
        leftHandExpression = false;
//        if (CREATE_DEBUG_INFO)
//            helper.mark("-- loading expression: " + exp.getClass().getName() +
//                    " at [" + exp.getLineNumber() + ":" + exp.getColumnNumber() + "]");
        //exp.visit(this);
        visitAndAutoboxBoolean(exp);
//        if (CREATE_DEBUG_INFO)
//            helper.mark(" -- end of loading --");

        leftHandExpression  = wasLeft;
    }

    public void visitPostfixExpression(PostfixExpression expression) {
        switch (expression.getOperation().getType()) {
            case Types.PLUS_PLUS :
                evaluatePostfixMethod("next", expression.getExpression());
                break;
            case Types.MINUS_MINUS :
                evaluatePostfixMethod("previous", expression.getExpression());
                break;
        }
    }

    // store the data on the stack to the expression (variablem, property, field, etc.
    private void store(Expression expression) {
        if (expression instanceof BinaryExpression) {
            throwException("BinaryExpression appeared on LHS. ");
        }
        if (ASM_DEBUG) {
            if (expression instanceof VariableExpression) {
                helper.mark(((VariableExpression)expression).getName());
            }
        }
        boolean wasLeft = leftHandExpression;
        leftHandExpression = true;
        expression.visit(this);
        //evaluateExpression(expression);
        leftHandExpression = wasLeft;
        return;
    }

    private void throwException(String s) {
        throw new RuntimeParserException(s, currentASTNode);
    }

    public void visitPrefixExpression(PrefixExpression expression) {
        switch (expression.getOperation().getType()) {
            case Types.PLUS_PLUS :
                evaluatePrefixMethod("next", expression.getExpression());
                break;
            case Types.MINUS_MINUS :
                evaluatePrefixMethod("previous", expression.getExpression());
                break;
        }
    }

    public void visitClosureExpression(ClosureExpression expression) {
        ClassNode innerClass = createClosureClass(expression);
        addInnerClass(innerClass);
        String innerClassinternalName = BytecodeHelper.getClassInternalName(innerClass);

        ClassNode owner = innerClass.getOuterClass();

        passingClosureParams = true;
        List constructors = innerClass.getDeclaredConstructors();
        ConstructorNode node = (ConstructorNode) constructors.get(0);
        Parameter[] localVariableParams = node.getParameters();

        cv.visitTypeInsn(NEW, innerClassinternalName);
        cv.visitInsn(DUP);
        if (isStaticMethod() || classNode.isStaticClass()) {
            visitClassExpression(new ClassExpression(owner));
        }
        else {
            loadThisOrOwner();
        }

        if (innerClass.getSuperClass()==ClassHelper.CLOSURE_TYPE) {
            if (isStaticMethod()) {
                /**
                 * todo could maybe stash this expression in a JVM variable
                 * from previous statement above
                 */
                visitClassExpression(new ClassExpression(owner));
            }
            else {
              cv.visitVarInsn(ALOAD, 0);
            }
        }

        // now lets load the various parameters we're passing
        // we start at index 2 because the first 2 variables we pass
        // are always the delegate and the outer instance and at this
        // point they are already on the stack
        for (int i = 2; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String name = param.getName();

            if (compileStack.getScope().isReferencedClassVariable(name)) {
                visitFieldExpression(new FieldExpression(classNode.getField(name)));
            } else { 
                Variable v = compileStack.getVariable(name,classNode.getSuperClass()!=ClassHelper.CLOSURE_TYPE);
                if (v==null) {
                    // variable is not on stack because we are
                    // inside a nested Closure and this variable
                    // was not used before
                    // then load it from the Closure field
                    FieldNode field = classNode.getField(name);
                    cv.visitVarInsn(ALOAD, 0);
                    cv.visitFieldInsn(GETFIELD, internalClassName, name, BytecodeHelper.getTypeDescription(field.getType()));
                    // and define it
                    // Note:
                    // we can simply define it here and don't have to
                    // be afraid about name problems because a second
                    // variable with that name is not allowed inside the closure
                    param.setClosureSharedVariable(false);
                    v = compileStack.defineVariable(param,true);
                    param.setClosureSharedVariable(true);
                    v.setHolder(true);
                } 
                cv.visitVarInsn(ALOAD, v.getIndex());
            }
        }
        passingClosureParams = false;

        // we may need to pass in some other constructors
        //cv.visitMethodInsn(INVOKESPECIAL, innerClassinternalName, "<init>", prototype + ")V");
        cv.visitMethodInsn(
            INVOKESPECIAL,
            innerClassinternalName,
            "<init>",
            BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, localVariableParams));
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

    /**
     * Generate byte code for constants
     * @see <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#14152">Class field types</a>
     */
    public void visitConstantExpression(ConstantExpression expression) {
        Object value = expression.getValue();
        helper.loadConstant(value);
    }

    public void visitSpreadExpression(SpreadExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(this);
        spreadList.call(cv);
    }

    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(this);
        spreadMap.call(cv);
    }

    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(this);
        helper.loadConstant(expression.getMethodName());
        getMethodPointer.call(cv);
    }

    public void visitNegationExpression(NegationExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(this);
        negation.call(cv);
    }

    public void visitBitwiseNegExpression(BitwiseNegExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(this);
        bitNegation.call(cv);
    }

    public void visitCastExpression(CastExpression expression) {
        ClassNode type = expression.getType();
        visitAndAutoboxBoolean(expression.getExpression());
        doConvertAndCast(type, expression.getExpression(), expression.isIgnoringAutoboxing(),false);
    }

    public void visitNotExpression(NotExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(this);

        // This is not the best way to do this. Javac does it by reversing the
        // underlying expressions but that proved
        // fairly complicated for not much gain. Instead we'll just use a
        // utility function for now.
        if (isComparisonExpression(expression.getExpression())) {
            notBoolean.call(cv);
        }
        else {
            notObject.call(cv);
        }
    }

    /**
     * return a primitive boolean value of the BooleanExpresion.
     * @param expression
     */
    public void visitBooleanExpression(BooleanExpression expression) {
        compileStack.pushBooleanExpression();
        expression.getExpression().visit(this);

        if (!isComparisonExpression(expression.getExpression())) {
// comment out for optimization when boolean values are not autoboxed for eg. function calls.
//           Class typeClass = expression.getExpression().getTypeClass();
//           if (typeClass != null && typeClass != boolean.class) {
                asBool.call(cv); // to return a primitive boolean
//            }
        }
        compileStack.pop();
    }

    private void prepareMethodcallObjectAndName(Expression objectExpression, boolean objectExpressionIsMethodName, String method) {
        if (objectExpressionIsMethodName) {
            VariableExpression.THIS_EXPRESSION.visit(this);
            objectExpression.visit(this);
        } else {
            objectExpression.visit(this);
            cv.visitLdcInsn(method);    
        }
    }

    public void visitMethodCallExpression(MethodCallExpression call) {
        onLineNumber(call, "visitMethodCallExpression: \"" + call.getMethod() + "\":");

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
        // are we a local variable
        if (isThisExpression(call.getObjectExpression()) && isFieldOrVariable(method) && ! classNode.hasPossibleMethod(method, arguments)) {
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
        } else {
            if (superMethodCall) {
                MethodNode superMethodNode = findSuperMethod(call);
                
                cv.visitVarInsn(ALOAD, 0);
                
                loadArguments(superMethodNode.getParameters(), arguments);
                
                String descriptor = BytecodeHelper.getMethodDescriptor(superMethodNode.getReturnType(), superMethodNode.getParameters());
                cv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(superMethodNode.getDeclaringClass()), method, descriptor);
                // as long as we want to work with objects where possible, we may have to
                // box the value here. This can be removed when we know the return type 
                // and save it in the MethodCallExpression. By default it returns Object
                // so boxing is always save.
                helper.box(superMethodNode.getReturnType());
            }
            else {
                Expression objectExpression = call.getObjectExpression();
                boolean objectExpressionIsMethodName = false;
                if (method.equals("call")) {
                    if (objectExpression instanceof GStringExpression) {
                        objectExpressionIsMethodName=true;
                        objectExpression = new CastExpression(ClassHelper.STRING_TYPE, objectExpression);
                    } else if (objectExpression instanceof ConstantExpression) {
                        Object value = ((ConstantExpression) objectExpression).getValue();
                        if ( value != null && value instanceof String) objectExpressionIsMethodName=true;
                    }
                }
                
                if (emptyArguments(arguments) && !call.isSafe() && !call.isSpreadSafe()) {
                    prepareMethodcallObjectAndName(objectExpression, objectExpressionIsMethodName,method);
                    invokeNoArgumentsMethod.call(cv);
                } else {
                    if (argumentsUseStack(arguments)) {
                        
                        arguments.visit(this);
                        
                        int paramIdx = compileStack.defineTemporaryVariable(method + "_arg",true);
                        
                        prepareMethodcallObjectAndName(objectExpression, objectExpressionIsMethodName,method);
                        
                        cv.visitVarInsn(ALOAD, paramIdx);
                        compileStack.removeVar(paramIdx);
                    } else {
                        prepareMethodcallObjectAndName(objectExpression, objectExpressionIsMethodName,method);
                        arguments.visit(this);
                    }
                    
                    if (call.isSpreadSafe()) {
                        invokeMethodSpreadSafeMethod.call(cv);
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

    /**
     * Loads and coerces the argument values for the given method call
     */
    protected void loadArguments(Parameter[] parameters, Expression expression) {
        TupleExpression argListExp = (TupleExpression) expression;
        List arguments = argListExp.getExpressions();
        for (int i = 0, size = arguments.size(); i < size; i++) {
            Expression argExp = argListExp.getExpression(i);
            Parameter param = parameters[i];
            visitAndAutoboxBoolean(argExp);

            ClassNode type = param.getType();
            ClassNode expType = getExpressionType(argExp);
            if (!type.equals(expType)) {
                doConvertAndCast(type);
            }
        }
    }

    /**
     * Attempts to find the method of the given name in a super class
     */
    protected MethodNode findSuperMethod(MethodCallExpression call) {
        String methodName = call.getMethod();
        TupleExpression argExpr = (TupleExpression) call.getArguments();
        int argCount = argExpr.getExpressions().size();
        ClassNode superClassNode = classNode.getSuperClass();
        if (superClassNode != null) {
            List methods = superClassNode.getMethods(methodName);
            for (Iterator iter = methods.iterator(); iter.hasNext(); ) {
                MethodNode method = (MethodNode) iter.next();
                if (method.getParameters().length == argCount) {
                    return method;
                }
            }
        }
        throwException("No such method: " + methodName + " for class: " + classNode.getName());
        return null; // should not come here
    }

    /**
     * Attempts to find the constructor in a super class
     */
    protected ConstructorNode findConstructor(ConstructorCallExpression call, ClassNode searchNode) {
        TupleExpression argExpr = (TupleExpression) call.getArguments();
        int argCount = argExpr.getExpressions().size();
        if (searchNode != null) {
            List constructors = searchNode.getDeclaredConstructors();
            for (Iterator iter = constructors.iterator(); iter.hasNext(); ) {
                ConstructorNode constructor = (ConstructorNode) iter.next();
                if (constructor.getParameters().length == argCount) {
                    return constructor;
                }
            }
        }
        throwException("No such constructor for class: " + classNode.getName());
        return null; // should not come here
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
            cv.visitLdcInsn(call.getOwnerType().getName());
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

            cv.visitLdcInsn(call.getOwnerType().getName());
            cv.visitLdcInsn(call.getMethod());
            arguments.visit(this);

            invokeStaticMethodMethod.call(cv);
        }
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        onLineNumber(call, "visitConstructorCallExpression: \"" + call.getType().getName() + "\":");
        this.leftHandExpression = false;

        if (call.isSpecialCall()){
            ClassNode callNode = classNode;
            if (call.isSuperCall()) callNode = callNode.getSuperClass();
            ConstructorNode constructorNode = findConstructor(call, callNode);
            cv.visitVarInsn(ALOAD, 0);
            loadArguments(constructorNode.getParameters(), call.getArguments());

            String descriptor = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, constructorNode.getParameters());
            cv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(callNode), "<init>", descriptor);
            return;
        }
        
        Expression arguments = call.getArguments();
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            int size = tupleExpression.getExpressions().size();
            if (size == 0) {
                arguments = null;
            }
        }

        // lets check that the type exists
        ClassNode type = call.getType();
        
        if (this.classNode != null) {
            // TODO: GROOVY-435
            pushClassTypeArgument(this.classNode, this.classNode);
            pushClassTypeArgument(this.classNode, type);

            if (arguments != null) {
                arguments.visit(this);
                invokeConstructorAtMethod.call(cv);
            } else {
                invokeNoArgumentsConstructorAt.call(cv);
            }
        }
        else {
            pushClassTypeArgument(this.classNode, type);

            if (arguments !=null) {
                arguments.visit(this);
                invokeConstructorOfMethod.call(cv);
            } else {
                invokeNoArgumentsConstructorOf.call(cv);
            }
        }
    }
    
    private static String makeFiledClassName(ClassNode type) {
        String internalName = BytecodeHelper.getClassInternalName(type);
        StringBuffer ret = new StringBuffer(internalName.length());
        for (int i=0; i<internalName.length(); i++) {
            char c = internalName.charAt(i);
            if (c=='/') {
                ret.append('$');
            } else if (c==';') {
                //append nothing -> delete ';'
            } else {
                ret.append(c);
            }
        }
        return ret.toString();
    }
    
    private static String getStaticFieldName(ClassNode type) {
        ClassNode componentType = type;
        String prefix = "";
        for (; componentType.isArray(); componentType=componentType.getComponentType()){
            prefix+="$";
        }
        if (prefix.length()!=0) prefix = "array"+prefix;
        String name = prefix+"class$" + makeFiledClassName(componentType);
        return name;
    }
    
    protected void pushClassTypeArgument(ClassNode ownerType, ClassNode type) {
        String name = type.getName();
    	String staticFieldName = getStaticFieldName(type);
        String ownerName = ownerType.getName().replace('.','/');

        syntheticStaticFields.add(staticFieldName);
        cv.visitFieldInsn(GETSTATIC, ownerName, staticFieldName, "Ljava/lang/Class;");
        Label l0 = new Label();
        cv.visitJumpInsn(IFNONNULL, l0);
        cv.visitLdcInsn(name);
        cv.visitMethodInsn(INVOKESTATIC, ownerName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
        cv.visitInsn(DUP);
        cv.visitFieldInsn(PUTSTATIC, ownerName, staticFieldName, "Ljava/lang/Class;");
        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);
        cv.visitFieldInsn(GETSTATIC, ownerName, staticFieldName, "Ljava/lang/Class;");
        cv.visitLabel(l1);
    }
 
    public void visitPropertyExpression(PropertyExpression expression) {
        Expression objectExpression = expression.getObjectExpression();
        if (isThisExpression(objectExpression)) {
            // lets use the field expression if its available
            String name = expression.getProperty();
            FieldNode field = classNode.getField(name);
            if (field != null) {
                visitFieldExpression(new FieldExpression(field));
                return;
            }
        }

        // we need to clear the LHS flag to avoid "this." evaluating as ASTORE
        // rather than ALOAD
        boolean left = leftHandExpression;
        leftHandExpression = false;
        objectExpression.visit(this);
        leftHandExpression = left;

        cv.visitLdcInsn(expression.getProperty());

        if (isGroovyObject(objectExpression) && ! expression.isSafe()) {
            if (left) {
                setGroovyObjectPropertyMethod.call(cv);
            }
            else {
                getGroovyObjectPropertyMethod.call(cv);
            }
        }
        else {
            if (expression.isSafe()) {
                if (left) {
                    setPropertySafeMethod2.call(cv);
                }
                else {
                    if (expression.isSpreadSafe()) {
                        getPropertySpreadSafeMethod.call(cv);
                    }
                    else {
                        getPropertySafeMethod.call(cv);
                    }
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
    }

    public void visitAttributeExpression(AttributeExpression expression) {
        Expression objectExpression = expression.getObjectExpression();
        if (isThisExpression(objectExpression)) {
            // lets use the field expression if its available
            String name = expression.getProperty();
            FieldNode field = classNode.getField(name);
            if (field != null) {
                visitFieldExpression(new FieldExpression(field));
                return;
            }
        }

        // we need to clear the LHS flag to avoid "this." evaluating as ASTORE
        // rather than ALOAD
        boolean left = leftHandExpression;
        leftHandExpression = false;
        objectExpression.visit(this);
        leftHandExpression = left;

        cv.visitLdcInsn(expression.getProperty());

        if (expression.isSafe()) {
            if (left) {
                setAttributeSafeMethod2.call(cv);
            }
            else {
                if (expression.isSpreadSafe()) {
                    getAttributeSpreadSafeMethod.call(cv);
                }
                else {
                    getAttributeSafeMethod.call(cv);
                }
            }
        }
        else {
            if (left) {
                setAttributeMethod2.call(cv);
            }
            else {
                getAttributeMethod.call(cv);
            }
        }
    }

    protected boolean isGroovyObject(Expression objectExpression) {
        return isThisExpression(objectExpression);
    }

    public void visitFieldExpression(FieldExpression expression) {
        FieldNode field = expression.getField();


	    if (field.isStatic()) {
        	if (leftHandExpression) {
        		storeStaticField(expression);
        	}
        	else {
        		loadStaticField(expression);
        	}
        } else {
        	if (leftHandExpression) {
        		storeThisInstanceField(expression);
        	}
        	else {
        		loadInstanceField(expression);
        	}
		}
    }

    /**
     *
     * @param fldExp
     */
    public void loadStaticField(FieldExpression fldExp) {
        FieldNode field = fldExp.getField();
        boolean holder = field.isHolder() && !isInClosureConstructor();
        ClassNode type = field.getType();

        String ownerName = (field.getOwner().equals(classNode))
                ? internalClassName
                : BytecodeHelper.getClassInternalName(field.getOwner());
        if (holder) {
            cv.visitFieldInsn(GETSTATIC, ownerName, fldExp.getFieldName(), BytecodeHelper.getTypeDescription(type));
            cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
        }
        else {
            cv.visitFieldInsn(GETSTATIC, ownerName, fldExp.getFieldName(), BytecodeHelper.getTypeDescription(type));
            if (ClassHelper.isPrimitiveType(type)) {
                helper.box(type);
			} else {
			}
        }
    }

	/**
	 * RHS instance field. should move most of the code in the BytecodeHelper
	 * @param fldExp
	 */
    public void loadInstanceField(FieldExpression fldExp) {
    	FieldNode field = fldExp.getField();
        boolean holder = field.isHolder() && !isInClosureConstructor();
        ClassNode type = field.getType();
        String ownerName = (field.getOwner().equals(classNode))
				? internalClassName
				: helper.getClassInternalName(field.getOwner());

        cv.visitVarInsn(ALOAD, 0);
		cv.visitFieldInsn(GETFIELD, ownerName, fldExp.getFieldName(), BytecodeHelper.getTypeDescription(type));

		if (holder) {
			cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
		} else {
			if (ClassHelper.isPrimitiveType(type)) {
				helper.box(type);
			} else {
			}
		}
    }

    public void storeThisInstanceField(FieldExpression expression) {
        FieldNode field = expression.getField();

        boolean holder = field.isHolder() && !isInClosureConstructor();
        ClassNode type = field.getType();

        String ownerName =  (field.getOwner().equals(classNode)) ?
        		internalClassName : BytecodeHelper.getClassInternalName(field.getOwner());
        if (holder) {
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, ownerName, expression.getFieldName(), BytecodeHelper.getTypeDescription(type));
            cv.visitInsn(SWAP);
            cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
        }
        else {
            if (isInClosureConstructor()) {
                helper.doCast(type);
            }
            else if (!ClassHelper.isPrimitiveType(type)){
                doConvertAndCast(type);
            }
            helper.loadThis();
            //helper.swapObjectWith(type);
            cv.visitInsn(SWAP);
            helper.unbox(type);
            helper.putField(field, ownerName);
        }
    }


    public void storeStaticField(FieldExpression expression) {
    	FieldNode field = expression.getField();

        boolean holder = field.isHolder() && !isInClosureConstructor();

        ClassNode type = field.getType();

        String ownerName = (field.getOwner().equals(classNode))
                ? internalClassName
                : helper.getClassInternalName(field.getOwner());
        if (holder) {
            cv.visitFieldInsn(GETSTATIC, ownerName, expression.getFieldName(), BytecodeHelper.getTypeDescription(type));
            cv.visitInsn(SWAP);
            cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
        }
        else {
            if (isInClosureConstructor()) {
                helper.doCast(type);
            }
            else {
                // this may be superfluous
                //doConvertAndCast(type);
                // use weaker cast
                helper.doCast(type);
            }
            cv.visitFieldInsn(PUTSTATIC, ownerName, expression.getFieldName(), BytecodeHelper.getTypeDescription(type));
        }
    }

    protected void visitOuterFieldExpression(FieldExpression expression, ClassNode outerClassNode, int steps, boolean first ) {
        FieldNode field = expression.getField();
        boolean isStatic = field.isStatic();

        int tempIdx = compileStack.defineTemporaryVariable(field, leftHandExpression && first);

        if (steps > 1 || !isStatic) {
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(
                GETFIELD,
                internalClassName,
                "owner",
                BytecodeHelper.getTypeDescription(outerClassNode));
        }

        if( steps == 1 ) {
            int opcode = (leftHandExpression) ? ((isStatic) ? PUTSTATIC : PUTFIELD) : ((isStatic) ? GETSTATIC : GETFIELD);
            String ownerName = BytecodeHelper.getClassInternalName(outerClassNode);

            if (leftHandExpression) {
                cv.visitVarInsn(ALOAD, tempIdx);
                boolean holder = field.isHolder() && !isInClosureConstructor();
                if ( !holder) {
                    doConvertAndCast(field.getType());
                }
            }
            cv.visitFieldInsn(opcode, ownerName, expression.getFieldName(), BytecodeHelper.getTypeDescription(field.getType()));
            if (!leftHandExpression) {
                if (ClassHelper.isPrimitiveType(field.getType())) {
                    helper.box(field.getType());
                }
            }
        }

        else {
            visitOuterFieldExpression( expression, outerClassNode.getOuterClass(), steps - 1, false );
        }
    }



    /**
     *  Visits a bare (unqualified) variable expression.
     */

    public void visitVariableExpression(VariableExpression expression) {

        String variableName = expression.getName();

      //-----------------------------------------------------------------------
      // SPECIAL CASES

        //
        // "this" for static methods is the Class instance

        if (isStaticMethod() && variableName.equals("this")) {
            visitClassExpression(new ClassExpression(classNode));
            return;                                               // <<< FLOW CONTROL <<<<<<<<<
        }

        //
        // "super" also requires special handling

        if (variableName.equals("super")) {
            visitClassExpression(new ClassExpression(classNode.getSuperClass()));
            return;                                               // <<< FLOW CONTROL <<<<<<<<<
        }

        Variable variable = compileStack.getVariable(variableName, false);

        VariableScope scope = compileStack.getScope();
        if (variable==null) {
            processClassVariable(variableName);
        } else {
            processStackVariable(variable);
        }
    }


    protected void processStackVariable(Variable variable) {
        if( leftHandExpression ) {
            helper.storeVar(variable);
        } else {
        	helper.loadVar(variable);
        }
        if (ASM_DEBUG) {
            helper.mark("var: " + variable.getName());
        }
    }

    protected void processClassVariable(String name) {
        if (passingClosureParams && isInScriptBody() ) {
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
    }


    protected void processFieldAccess( String name, FieldNode field, int steps ) {
        FieldExpression expression = new FieldExpression(field);

        if( steps == 0 ) {
            visitFieldExpression( expression );
        }
        else {
            visitOuterFieldExpression( expression, classNode.getOuterClass(), steps, true );
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
            if (expression.getType()==ClassHelper.VOID_TYPE) { // nothing on the stack
                return false;
            } else {
                return !MethodCallExpression.isSuperMethodCall((MethodCallExpression) expression);
            }
        }
        if (expression instanceof DeclarationExpression) {
            return false;
        }
        if (expression instanceof BinaryExpression) {
            BinaryExpression binExp = (BinaryExpression) expression;
            switch (binExp.getOperation().getType()) {   // br todo should leave a copy of the value on the stack for all the assignemnt.
//                case Types.EQUAL :   // br a copy of the right value is left on the stack (see evaluateEqual()) so a pop is required for a standalone assignment
//                case Types.PLUS_EQUAL : // this and the following are related to evaluateBinaryExpressionWithAsignment()
//                case Types.MINUS_EQUAL :
//                case Types.MULTIPLY_EQUAL :
//                case Types.DIVIDE_EQUAL :
//                case Types.INTDIV_EQUAL :
//                case Types.MOD_EQUAL :
//                    return false;
            }
        }
        if (expression instanceof ConstructorCallExpression) {
            ConstructorCallExpression cce = (ConstructorCallExpression) expression;
            return !cce.isSpecialCall();
        }
        return true;
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
            cv.visitTryCatchBlock(l0, l2, l2, "java/lang/ClassNotFoundException"); // br using l2 as the 2nd param seems create the right table entry
            cv.visitMaxs(3, 2);

            cw.visitEnd();
        }
    }

    /** load class object on stack */
    public void visitClassExpression(ClassExpression expression) {
        ClassNode type = expression.getType();
        //type = checkValidType(type, expression, "Must be a valid type name for a constructor call");


        if (ClassHelper.isPrimitiveType(type)) {
            ClassNode objectType = ClassHelper.getWrapper(type);
            cv.visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(objectType), "TYPE", "Ljava/lang/Class;");
        }
        else {
            final String staticFieldName =
                (type.equals(classNode)) ? "class$0" : getStaticFieldName(type);

            syntheticStaticFields.add(staticFieldName);

            cv.visitFieldInsn(GETSTATIC, internalClassName, staticFieldName, "Ljava/lang/Class;");
            Label l0 = new Label();
            cv.visitJumpInsn(IFNONNULL, l0);
            cv.visitLdcInsn(type.getName());
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
    System.out.println("here");
    }

    public void visitMapExpression(MapExpression expression) {
        List entries = expression.getMapEntryExpressions();
        int size = entries.size();
        helper.pushConstant(size * 2);

        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        int i = 0;
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            Object object = iter.next();
            MapEntryExpression entry = (MapEntryExpression) object;

            cv.visitInsn(DUP);
            helper.pushConstant(i++);
            visitAndAutoboxBoolean(entry.getKeyExpression());
            cv.visitInsn(AASTORE);

            cv.visitInsn(DUP);
            helper.pushConstant(i++);
            visitAndAutoboxBoolean(entry.getValueExpression());
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
            visitAndAutoboxBoolean(expression.getExpression(i));
            cv.visitInsn(AASTORE);
        }
    }
    
    public void visitArrayExpression(ArrayExpression expression) {
        ClassNode elementType = expression.getElementType();
        String arrayTypeName = BytecodeHelper.getClassInternalName(elementType);        
        List sizeExpression = expression.getSizeExpression();

        int size=0;
        int dimensions=0;
        if (sizeExpression!=null) {
        	for (Iterator iter = sizeExpression.iterator(); iter.hasNext();) {
				Expression element = (Expression) iter.next();
				if (element==ConstantExpression.EMTPY_EXPRESSION) break;
				dimensions++;
	            // lets convert to an int
	            visitAndAutoboxBoolean(element);
	            asIntMethod.call(cv);
			}
        } else {
            size = expression.getExpressions().size();
            helper.pushConstant(size);
        }

        int storeIns=AASTORE;
        if (sizeExpression!=null) {
            arrayTypeName = BytecodeHelper.getTypeDescription(expression.getType());
        	cv.visitMultiANewArrayInsn(arrayTypeName, dimensions);
        } else if (ClassHelper.isPrimitiveType(elementType)) {
            int primType=0;
            if (elementType==ClassHelper.boolean_TYPE) {
                primType = T_BOOLEAN;
                storeIns = BASTORE;
            } else if (elementType==ClassHelper.char_TYPE) {
                primType = T_CHAR;
                storeIns = CASTORE;
            } else if (elementType==ClassHelper.float_TYPE) {
                primType = T_FLOAT;
                storeIns = FASTORE;
            } else if (elementType==ClassHelper.double_TYPE) {
                primType = T_DOUBLE;
                storeIns = DASTORE;
            } else if (elementType==ClassHelper.byte_TYPE) {
                primType = T_BYTE;
                storeIns = BASTORE;
            } else if (elementType==ClassHelper.short_TYPE) {
                primType = T_SHORT;
                storeIns = SASTORE;
            } else if (elementType==ClassHelper.int_TYPE) {
                primType = T_INT;
                storeIns=IASTORE;
            } else if (elementType==ClassHelper.long_TYPE) {
                primType = T_LONG;
                storeIns = LASTORE;
            } 
            cv.visitIntInsn(NEWARRAY, primType);
        } else {
            cv.visitTypeInsn(ANEWARRAY, arrayTypeName);
        } 

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            helper.pushConstant(i);
            Expression elementExpression = expression.getExpression(i);
            if (elementExpression == null) {
                ConstantExpression.NULL.visit(this);
            } else {
                if (!elementType.equals(elementExpression.getType())) {
                    visitCastExpression(new CastExpression(elementType, elementExpression, true));
                } else {
                    visitAndAutoboxBoolean(elementExpression);
                }
            }
            cv.visitInsn(storeIns);            
        }
        
        if (sizeExpression==null && ClassHelper.isPrimitiveType(elementType)) {
            int par = compileStack.defineTemporaryVariable("par",true);
            cv.visitVarInsn(ALOAD, par);
        }
    }

    public void visitListExpression(ListExpression expression) {
        int size = expression.getExpressions().size();
        helper.pushConstant(size);

        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            helper.pushConstant(i);
            visitAndAutoboxBoolean(expression.getExpression(i));
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
            visitAndAutoboxBoolean(expression.getValue(i));
            cv.visitInsn(AASTORE);
        }

        int paramIdx = compileStack.defineTemporaryVariable("iterator",true);

        ClassNode innerClass = createGStringClass(expression);
        addInnerClass(innerClass);
        String innerClassinternalName = BytecodeHelper.getClassInternalName(innerClass);

        cv.visitTypeInsn(NEW, innerClassinternalName);
        cv.visitInsn(DUP);
        cv.visitVarInsn(ALOAD, paramIdx);

        cv.visitMethodInsn(INVOKESPECIAL, innerClassinternalName, "<init>", "([Ljava/lang/Object;)V");
        compileStack.removeVar(paramIdx);
    }
    
    public void visitAnnotations(AnnotatedNode node) {
        Map annotionMap = node.getAnnotations();
        if (annotionMap.isEmpty()) return;
        Iterator it = annotionMap.values().iterator(); 
        while (it.hasNext()) {
            AnnotationNode an = (AnnotationNode) it.next();
            //skip builtin properties
            if (an.isBuiltIn()) continue;
            ClassNode type = an.getClassNode();

            String clazz = type.getName();
            AnnotationVisitor av = cw.visitAnnotation(BytecodeHelper.formatNameForClassLoading(clazz),false);

            Iterator mIt = an.getMembers().keySet().iterator();
            while (mIt.hasNext()) {
                String name = (String) mIt.next();
                ConstantExpression exp = (ConstantExpression) an.getMember(name);
                av.visit(name,exp.getValue());
            }
            av.visitEnd();
        }
    }
    
    
    // Implementation methods
    //-------------------------------------------------------------------------
    protected boolean addInnerClass(ClassNode innerClass) {
        innerClass.setModule(classNode.getModule());
        return innerClasses.add(innerClass);
    }

    protected ClassNode createClosureClass(ClosureExpression expression) {
        ClassNode owner = getOutermostClass();
        ClassNode outerClass = owner;
        String name = owner.getName() + "$"
                + context.getNextClosureInnerName(owner, classNode, methodNode); // br added a more infomative name
        boolean staticMethodOrInStaticClass = isStaticMethod() || classNode.isStaticClass();
        if (staticMethodOrInStaticClass) {
            outerClass = ClassHelper.make(Class.class);
        }
        Parameter[] parameters = expression.getParameters();
        if (parameters==null){
            parameters = new Parameter[0];
        } else if (parameters.length == 0) {
            // lets create a default 'it' parameter
            parameters = new Parameter[] { new Parameter(ClassHelper.OBJECT_TYPE, "it", ConstantExpression.NULL)};
        } 

        Parameter[] localVariableParams = getClosureSharedVariables(expression);

        InnerClassNode answer = new InnerClassNode(owner, name, 0, ClassHelper.CLOSURE_TYPE); // closures are local inners and not public
        answer.setEnclosingMethod(this.methodNode);
        answer.setSynthetic(true);
        
        if (staticMethodOrInStaticClass) {
            answer.setStaticClass(true);
        }
        if (isInScriptBody()) {
            answer.setScriptBody(true);
        }
        MethodNode method =
            answer.addMethod("doCall", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, parameters, ClassNode.EMPTY_ARRAY, expression.getCode());
        method.setSourcePosition(expression);

        VariableScope varScope = expression.getVariableScope();
        if (varScope == null) {
            throw new RuntimeException(
                "Must have a VariableScope by now! for expression: " + expression + " class: " + name);
        } else {
            method.setVariableScope(varScope.copy());
        }
        if (parameters.length > 1
            || (parameters.length == 1
                && parameters[0].getType() != null
                && parameters[0].getType() != ClassHelper.OBJECT_TYPE)) {

            // lets add a typesafe call method
            MethodNode call = answer.addMethod(
                "call",
                ACC_PUBLIC,
                ClassHelper.OBJECT_TYPE,
                parameters,
                ClassNode.EMPTY_ARRAY,
                new ReturnStatement(
                    new MethodCallExpression(
                        VariableExpression.THIS_EXPRESSION,
                        "doCall",
                        new ArgumentListExpression(parameters))));
            call.setSourcePosition(expression);
        }

        FieldNode ownerField = answer.addField("owner", ACC_PRIVATE, outerClass, null);
        ownerField.setSourcePosition(expression);

        // lets make the constructor
        BlockStatement block = new BlockStatement();
        block.setSourcePosition(expression);
        VariableExpression outer = new VariableExpression("_outerInstance");
        outer.setSourcePosition(expression);
        block.getVariableScope().getReferencedLocalVariables().put("_outerInstance",outer);
        block.addStatement(
            new ExpressionStatement(
                new ConstructorCallExpression(
                    ClassNode.SUPER,
                    outer)));
        block.addStatement(
            new ExpressionStatement(
                new BinaryExpression(
                    new FieldExpression(ownerField),
                    Token.newSymbol(Types.EQUAL, -1, -1),
                    outer)));

        // lets assign all the parameter fields from the outer context
        for (int i = 0; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String paramName = param.getName();
            Expression initialValue = null;
            ClassNode type = param.getType();
            FieldNode paramField = null;
            if (true) {
            	initialValue = new VariableExpression(paramName);
                ClassNode realType = type;
                type = ClassHelper.makeReference();
                param.setType(type);
                paramField = answer.addField(paramName, ACC_PRIVATE, type, initialValue);
                paramField.setHolder(true);
                String methodName = Verifier.capitalize(paramName);

                // lets add a getter & setter
                Expression fieldExp = new FieldExpression(paramField);
                answer.addMethod(
                    "get" + methodName,
                    ACC_PUBLIC,
                    realType,
                    Parameter.EMPTY_ARRAY,
                    ClassNode.EMPTY_ARRAY,
                    new ReturnStatement(fieldExp));

                /*
                answer.addMethod(
                    "set" + methodName,
                    ACC_PUBLIC,
                    "void",
                    new Parameter[] { new Parameter(realType, "__value") },
                    new ExpressionStatement(
                        new BinaryExpression(expression, Token.newSymbol(Types.EQUAL, 0, 0), new VariableExpression("__value"))));
                        */
            }
        }

        Parameter[] params = new Parameter[2 + localVariableParams.length];
        params[0] = new Parameter(outerClass, "_outerInstance");
        params[1] = new Parameter(ClassHelper.OBJECT_TYPE, "_delegate");
        System.arraycopy(localVariableParams, 0, params, 2, localVariableParams.length);

        ASTNode sn = answer.addConstructor(ACC_PUBLIC, params, ClassNode.EMPTY_ARRAY, block);
        sn.setSourcePosition(expression);
        return answer;
    }
    
    protected Parameter[] getClosureSharedVariables(ClosureExpression ce){
        VariableScope scope =  ce.getVariableScope();
        Map references = scope.getReferencedLocalVariables();
        Parameter[] ret = new Parameter[references.size()];
        int index = 0;
        for (Iterator iter = references.values().iterator(); iter.hasNext();) {
            org.codehaus.groovy.ast.Variable element = (org.codehaus.groovy.ast.Variable) iter.next();
            if (element instanceof Parameter) {
                ret[index] = (Parameter) element;
            } else {
                Parameter p = new Parameter(element.getType(),element.getName());
                ret[index] = p;
            }
            index++;
        }
        return ret;
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
        InnerClassNode answer = new InnerClassNode(owner, name, 0, ClassHelper.GSTRING_TYPE);
        answer.setEnclosingMethod(this.methodNode);
        FieldNode stringsField =
            answer.addField(
                "strings",
                ACC_PRIVATE /*| ACC_STATIC*/,
                ClassHelper.STRING_TYPE.makeArray(),
                new ArrayExpression(ClassHelper.STRING_TYPE, expression.getStrings()));
        answer.addMethod(
            "getStrings",
            ACC_PUBLIC,
            ClassHelper.STRING_TYPE.makeArray(),
            Parameter.EMPTY_ARRAY,
            ClassNode.EMPTY_ARRAY,
            new ReturnStatement(new FieldExpression(stringsField)));
        // lets make the constructor
        BlockStatement block = new BlockStatement();
        block.addStatement(
            new ExpressionStatement(
                new ConstructorCallExpression(ClassNode.SUPER, new VariableExpression("values"))));
        Parameter[] contructorParams = new Parameter[] { new Parameter(ClassHelper.OBJECT_TYPE.makeArray(), "values")};
        answer.addConstructor(ACC_PUBLIC, contructorParams, ClassNode.EMPTY_ARRAY, block);
        return answer;
    }

    protected void doConvertAndCast(ClassNode type) {
        if (type==ClassHelper.OBJECT_TYPE) return;
        if (isValidTypeForCast(type)) {
            visitClassExpression(new ClassExpression(type));
            asTypeMethod.call(cv);
        } 
        helper.doCast(type);
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

    // todo: optimization: change to return primitive boolean. need to adjust the BinaryExpression and isComparisonExpression for
    // consistancy.
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
        if (isComparisonExpression(leftExpression)) {
            helper.boxBoolean();
        }

        // if the right hand side is a boolean expression, we need to autobox
        Expression rightExpression = expression.getRightExpression();
        rightExpression.visit(this);
        if (isComparisonExpression(rightExpression)) {
            helper.boxBoolean();
        }
        compareToMethod.call(cv);
    }

    protected void evaluateBinaryExpressionWithAsignment(String method, BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        if (leftExpression instanceof BinaryExpression) {
            BinaryExpression leftBinExpr = (BinaryExpression) leftExpression;
            if (leftBinExpr.getOperation().getType() == Types.LEFT_SQUARE_BRACKET) {
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
                //cv.visitInsn(POP);
                return;
            }
        }

        evaluateBinaryExpression(method, expression);

        // br to leave a copy of rvalue on the stack. see also isPopRequired()
        cv.visitInsn(DUP);

        leftHandExpression = true;
        evaluateExpression(leftExpression);
        leftHandExpression = false;
    }

    private void evaluateBinaryExpression(MethodCaller compareMethod, BinaryExpression bin) {
        evalBinaryExp_LateBinding(compareMethod, bin);
    }

    protected void evalBinaryExp_LateBinding(MethodCaller compareMethod, BinaryExpression expression) {
        Expression leftExp = expression.getLeftExpression();
        Expression rightExp = expression.getRightExpression();
        load(leftExp);
        load(rightExp);
        compareMethod.call(cv);
    }

    protected void evaluateEqual(BinaryExpression expression) {

        Expression leftExpression = expression.getLeftExpression();
        if (leftExpression instanceof BinaryExpression) {
            BinaryExpression leftBinExpr = (BinaryExpression) leftExpression;
            if (leftBinExpr.getOperation().getType() == Types.LEFT_SQUARE_BRACKET) {
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
                 // cv.visitInsn(POP); //this is realted to isPopRequired()
                return;
            }
        }

        // lets evaluate the RHS then hopefully the LHS will be a field
        leftHandExpression = false;
        Expression rightExpression = expression.getRightExpression();

        ClassNode type = getLHSType(leftExpression);
        // lets not cast for primitive types as we handle these in field setting etc
        if (ClassHelper.isPrimitiveType(type)) {
            visitAndAutoboxBoolean(rightExpression);
        } else if (type!=ClassHelper.OBJECT_TYPE){
            visitCastExpression(new CastExpression(type, rightExpression));
        } else {
            visitAndAutoboxBoolean(rightExpression);
        }

        cv.visitInsn(DUP);  // to leave a copy of the rightexpression value on the stack after the assignment.
        leftHandExpression = true;
        leftExpression.visit(this);
        leftHandExpression = false;
    }
    
    /**
     * Deduces the type name required for some casting
     *
     * @return the type of the given (LHS) expression or null if it is java.lang.Object or it cannot be deduced
     */
    protected ClassNode getLHSType(Expression leftExpression) {
        if (leftExpression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) leftExpression; 
            ClassNode type = varExp.getType();
            if (isValidTypeForCast(type)) {
                return type;
            }
            String variableName = varExp.getName();
            Variable variable = compileStack.getVariable(variableName,false);
            if (variable != null) {
                if (variable.isHolder()) {
                    return type;
                }
                if (variable.isProperty()) return variable.getType();
                type = variable.getType();
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
        else if (leftExpression instanceof FieldExpression) {
            FieldExpression fieldExp = (FieldExpression) leftExpression;
            ClassNode type = fieldExp.getType();
            if (isValidTypeForCast(type)) {
                return type;
            }
        }
        return ClassHelper.DYNAMIC_TYPE;
    }

    protected boolean isValidTypeForCast(ClassNode type) {
        return type!=ClassHelper.DYNAMIC_TYPE && 
               type!=ClassHelper.REFERENCE_TYPE;
    }

    protected void visitAndAutoboxBoolean(Expression expression) {
        expression.visit(this);

        if (isComparisonExpression(expression)) {
            helper.boxBoolean(); // convert boolean to Boolean
        }
    }

    protected void evaluatePrefixMethod(String method, Expression expression) {
        if (isNonStaticField(expression) && ! isHolderVariable(expression) && !isStaticMethod()) {
            cv.visitVarInsn(ALOAD, 0);
        }
        expression.visit(this);
        cv.visitLdcInsn(method);
        invokeNoArgumentsMethod.call(cv);

        leftHandExpression = true;
        expression.visit(this);
        leftHandExpression = false;
        expression.visit(this);
    }

    protected void evaluatePostfixMethod(String method, Expression expression) {
        leftHandExpression = false;
        expression.visit(this);

        int tempIdx = compileStack.defineTemporaryVariable("postfix_" + method, true);
        cv.visitVarInsn(ALOAD, tempIdx);

        cv.visitLdcInsn(method);
        invokeNoArgumentsMethod.call(cv);

        store(expression);

        cv.visitVarInsn(ALOAD, tempIdx);
        compileStack.removeVar(tempIdx);
    }

    protected void evaluateInstanceof(BinaryExpression expression) {
        expression.getLeftExpression().visit(this);
        Expression rightExp = expression.getRightExpression();
        ClassNode classType = ClassHelper.DYNAMIC_TYPE;
        if (rightExp instanceof ClassExpression) {
            ClassExpression classExp = (ClassExpression) rightExp;
            classType = classExp.getType();
        }
        else {
            throw new RuntimeException(
                "Right hand side of the instanceof keyworld must be a class name, not: " + rightExp);
        }
        String classInternalName = BytecodeHelper.getClassInternalName(classType);
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
            field = classNode.getField(varExp.getName());
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
            return varExp.getName().equals("this");
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
            if (binExpr.getOperation().isA(Types.ASSIGNMENT_OPERATOR)) {
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

    protected boolean isComparisonExpression(Expression expression) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binExpr = (BinaryExpression) expression;
            switch (binExpr.getOperation().getType()) {
                case Types.COMPARE_EQUAL :
                case Types.MATCH_REGEX :
                case Types.COMPARE_GREATER_THAN :
                case Types.COMPARE_GREATER_THAN_EQUAL :
                case Types.COMPARE_LESS_THAN :
                case Types.COMPARE_LESS_THAN_EQUAL :
                case Types.COMPARE_IDENTICAL :
                case Types.COMPARE_NOT_EQUAL :
                case Types.KEYWORD_INSTANCEOF :
                    return true;
            }
        }
        else if (expression instanceof BooleanExpression) {
            return true;
        }
        return false;
    }

    protected void onLineNumber(ASTNode statement, String message) {
        int line = statement.getLineNumber();
        int col = statement.getColumnNumber();
        this.currentASTNode = statement;

        if (line >=0) {
            lineNumber = line;
            columnNumber = col;
        }
        if (CREATE_LINE_NUMBER_INFO && line >= 0 && cv != null) {
            Label l = new Label();
            cv.visitLabel(l);
            cv.visitLineNumber(line, l);
            if (ASM_DEBUG) {
                helper.mark(message + "[" + statement.getLineNumber() + ":" + statement.getColumnNumber() + "]");
            }
        }
    }

    protected boolean isNotFieldOfOutermostClass(String var) {
        return getOutermostClass().getField(var) == null;
    }

    private boolean isInnerClass() {
        return classNode instanceof InnerClassNode;
    }

    /** @return true if the given name is a local variable or a field */
    protected boolean isFieldOrVariable(String name) {
        return compileStack.containsVariable(name) || classNode.getField(name) != null;
    }

    /**
     * @return if the type of the expression can be determined at compile time
     *         then this method returns the type - otherwise null
     */
    protected ClassNode getExpressionType(Expression expression) {
        if (isComparisonExpression(expression)) {
            return ClassHelper.boolean_TYPE;
        }
        if (expression instanceof VariableExpression) {
        	if (expression == VariableExpression.THIS_EXPRESSION) {
        		return classNode;
        	}else  if (expression==VariableExpression.SUPER_EXPRESSION) {
        		return classNode.getSuperClass();
        	}
        	
            VariableExpression varExpr = (VariableExpression) expression;
            Variable variable = compileStack.getVariable(varExpr.getName(),false);
            if (variable != null && !variable.isHolder()) {
                ClassNode type = variable.getType();
                if (! variable.isDynamicTyped()) return type;
            }
            if (variable == null) {
                org.codehaus.groovy.ast.Variable var = (org.codehaus.groovy.ast.Variable) compileStack.getScope().getReferencedClassVariables().get(varExpr.getName());
                if (var!=null && !var.isDynamicTyped()) return var.getType();
            }
        }
        return expression.getType();
    }

    protected boolean isInClosureConstructor() {
        return constructorNode != null
            && classNode.getOuterClass() != null
            && classNode.getSuperClass()==ClassHelper.CLOSURE_TYPE;
    }

    protected boolean isStaticMethod() {
        if (methodNode == null) { // we're in a constructor
            return false;
        }
        return methodNode.isStatic();
    }

    protected CompileUnit getCompileUnit() {
        CompileUnit answer = classNode.getCompileUnit();
        if (answer == null) {
            answer = context.getCompileUnit();
        }
        return answer;
    }

    protected boolean isHolderVariable(Expression expression) {
        if (expression instanceof FieldExpression) {
            FieldExpression fieldExp = (FieldExpression) expression;
            return fieldExp.getField().isHolder();
        }
        if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            Variable variable = compileStack.getVariable(varExp.getName(),false);
            if (variable != null) {
                return variable.isHolder();
            }
            FieldNode field = classNode.getField(varExp.getName());
            if (field != null) {
                return field.isHolder();
            }
        }
        return false;
    }
}

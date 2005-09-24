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

import groovy.lang.*;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.logging.Logger;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Type;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.*;
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
import org.codehaus.groovy.runtime.RegexSupport;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.ClassWriter;


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
    private Map variableStack = new HashMap();

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


    // current stack index
    private int lastVariableIndex;
    private static int tempVariableNameCounter;

    // exception blocks list
    private List exceptionBlocks = new ArrayList();

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
    public static final boolean CREATE_DEBUG_INFO = false;
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
        GeneratorContext context,
        ClassVisitor classVisitor,
        ClassLoader classLoader,
        String sourceFile) {
        super(classLoader);
        this.context = context;
        this.cw = classVisitor;
        this.sourceFile = sourceFile;

        this.dummyClassWriter = new ClassWriter(true);
        dummyGen  = new DummyClassGenerator(context, dummyClassWriter, classLoader, sourceFile);

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
            this.internalClassName = BytecodeHelper.getClassInternalName(classNode.getType());

            //System.out.println("Generating class: " + classNode.getName());

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
            visitAnnotations(classNode);

            // set the optional enclosing method attribute of the current inner class
//          br comment out once Groovy uses the latest CVS HEAD of ASM
//            MethodNode enclosingMethod = classNode.getEnclosingMethod();
//            String ownerName = BytecodeHelper.getClassInternalName(enclosingMethod.getDeclaringClass().getName());
//            String descriptor = BytecodeHelper.getMethodDescriptor(enclosingMethod.getReturnType(), enclosingMethod.getParameters());
//            EnclosingMethodAttribute attr = new EnclosingMethodAttribute(ownerName,enclosingMethod.getName(),descriptor);
//            cw.visitAttribute(attr);

            classNode.visitContents(this);

            createSyntheticStaticFields();

            for (Iterator iter = innerClasses.iterator(); iter.hasNext();) {
                ClassNode innerClass = (ClassNode) iter.next();
                String innerClassName = innerClass.getType().getName();
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

    // create a surrogate class that represents the classNode
    // the surrogate has the "face" of the real class. It's used for
    // type resolving "this"
    private void createDummyClass(ClassNode classNode) {
        dummyGen.visitClass(classNode);
        byte[] code = dummyClassWriter.toByteArray();

        ClassLoader parentLoader = getClass().getClassLoader();
        GroovyClassLoader groovyLoader = new GroovyClassLoader(parentLoader);
        Type type = classNode.getType();
        Class theClass = groovyLoader.defineClass(type.getName(), code);

        if (theClass != null) {
            classCache.put(type.getName(), theClass);
            type.setTypeClass(theClass);
        }
    }

    public void visitConstructor(ConstructorNode node) {
        // creates a MethodWriter for the (implicit) constructor
        //String methodType = Type.getMethodDescriptor(VOID_TYPE, )

        this.constructorNode = node;
        this.methodNode = null;
        this.variableScope = null;

        String methodType = BytecodeHelper.getMethodDescriptor("void", node.getParameters());
        cv = cw.visitMethod(node.getModifiers(), "<init>", methodType, null, null);
        helper = new BytecodeHelper(cv);

        findMutableVariables();
        resetVariableStack(node.getParameters());

        Statement code = node.getCode();
        if (code == null || !firstStatementIsSuperInit(code)) {
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

        String methodType = BytecodeHelper.getMethodDescriptor(node.getReturnType(), node.getParameters());
        cv = cw.visitMethod(node.getModifiers(), node.getName(), methodType, null, null);
        visitAnnotations(node);
        if (node.getCode()!=null) {
            Label labelStart = new Label();
            cv.visitLabel(labelStart);
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
    
            Label labelEnd = new Label();
            cv.visitLabel(labelEnd);
    
            // br experiment with local var table so debuggers can retrieve variable names
            if (CREATE_DEBUG_INFO) {
                Set vars = this.variableStack.keySet();
                for (Iterator iterator = vars.iterator(); iterator.hasNext();) {
                    String varName = (String) iterator.next();
                    Variable v = (Variable)variableStack.get(varName);
                    String type = v.getTypeName();
                    type = BytecodeHelper.getTypeDescription(type);
                    Label start = v.getStartLabel() != null ? v.getStartLabel() : labelStart;
                    Label end = v.getEndLabel() != null ? v.getEndLabel() : labelEnd;
                    cv.visitLocalVariable(varName, type, null, start, end, v.getIndex());
                }
            }
            cv.visitMaxs(0, 0);
        }
    }

    public void visitField(FieldNode fieldNode) {
        onLineNumber(fieldNode, "visitField: " + fieldNode.getName());
        cw.visitField(
            fieldNode.getModifiers(),
            fieldNode.getName(),
            BytecodeHelper.getTypeDescription(fieldNode.getType()),
            null, //fieldValue,  //br  all the sudden that one cannot init the field here. init is done in static initilizer and instace intializer.
            null);
        visitAnnotations(fieldNode);
    }


    /**
     * Creates a getter, setter and field
     */
    public void visitProperty(PropertyNode statement) {
        onLineNumber(statement, "visitProperty:" + statement.getField().getName());
        //this.propertyNode = statement;
        this.methodNode = null;
    }

    // GroovyCodeVisitor interface
    //-------------------------------------------------------------------------

    // Statements
    //-------------------------------------------------------------------------

    public void visitForLoop(ForStatement loop) {
        onLineNumber(loop, "visitForLoop");
        Class elemType = null;

        //
        // Declare the loop counter.
        Type variableType = loop.getVariableType();
        Variable variable = defineVariable(loop.getVariable(), variableType, true);

        if( isInScriptBody() ) {
            variable.setProperty( true );
        }


        //
        // Then initialize the iterator and generate the loop control

        loop.getCollectionExpression().visit(this);

        asIteratorMethod.call(cv);

        final Variable iterTemp = storeInTemp("iterator", Type.makeType(java.util.Iterator.class));
        final int iteratorIdx = iterTemp.getIndex();

        // to push scope here allows the iterator available after the loop, such as the i in: for (i in 1..5)
        // move it to the top will make the iterator a local var in the for loop.
        pushBlockScope();

        Label continueLabel = scope.getContinueLabel();
        cv.visitJumpInsn(GOTO, continueLabel);
        Label label2 = new Label();
        cv.visitLabel(label2);

        final Class elemClass = elemType;
        BytecodeExpression expression = new BytecodeExpression() {
            public void visit(GroovyCodeVisitor visitor) {
                cv.visitVarInsn(ALOAD, iteratorIdx);
                iteratorNextMethod.call(cv);
            }

            protected void resolveType(AsmClassGenerator resolver) {
                setType(Type.makeType(elemClass));
            }
        };

        evaluateEqual( BinaryExpression.newAssignmentExpression(loop.getVariable(), expression) );
        cv.visitInsn(POP); // br now the evaluateEqual() will leave a value on the stack. pop it.

        //
        // Generate the loop body

        loop.getLoopBlock().visit(this);


        //
        // Generate the loop tail

        cv.visitLabel(continueLabel);
        cv.visitVarInsn(ALOAD, iteratorIdx);

        iteratorHasNextMethod.call(cv);

        cv.visitJumpInsn(IFNE, label2);

        cv.visitLabel(scope.getBreakLabel());
        popScope();
    }

    public void visitWhileLoop(WhileStatement loop) {
        onLineNumber(loop, "visitWhileLoop");

        pushBlockScope();

        Label continueLabel = scope.getContinueLabel();

        cv.visitJumpInsn(GOTO, continueLabel);
        Label l1 = new Label();
        cv.visitLabel(l1);

        loop.getLoopBlock().visit(this);

        cv.visitLabel(continueLabel);

        loop.getBooleanExpression().visit(this);

        cv.visitJumpInsn(IFNE, l1);

        cv.visitLabel(scope.getBreakLabel());
        popScope();
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        onLineNumber(loop, "visitDoWhileLoop");

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
        onLineNumber(ifElse, "visitIfElse");

        ifElse.getBooleanExpression().visit(this);

        Label l0 = new Label();
        cv.visitJumpInsn(IFEQ, l0);
        pushBlockScope(false, false);
        ifElse.getIfBlock().visit(this);
        popScope();

        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);

        pushBlockScope(false, false);
        ifElse.getElseBlock().visit(this);
        cv.visitLabel(l1);
        popScope();
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

            Variable assertTemp = visitASTOREInTemp("assert");
            int tempIndex  = assertTemp.getIndex();

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
            removeVar(assertTemp);
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
// todo need to add blockscope handling
        CatchStatement catchStatement = statement.getCatchStatement(0);

        Statement tryStatement = statement.getTryStatement();

        if (tryStatement.isEmpty() || catchStatement == null) {
            final Label l0 = new Label();
            cv.visitLabel(l0);

            tryStatement.visit(this);


            int index1 = defineVariable(this.createVariableName("exception"), Type.OBJECT_TYPE).getIndex();
            int index2 = defineVariable(this.createVariableName("exception"), Type.OBJECT_TYPE).getIndex();

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
            int finallySubAddress = defineVariable(this.createVariableName("exception"), Type.OBJECT_TYPE).getIndex();
            int anyExceptionIndex = defineVariable(this.createVariableName("exception"), Type.OBJECT_TYPE).getIndex();

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
                Type exceptionType = catchStatement.getExceptionType();
                int exceptionIndex = defineVariable(catchStatement.getVariable(), exceptionType, false).getIndex();
                
                // start catch block, label needed for exception table
                final Label catchStart = new Label();
                cv.visitLabel(catchStart);
                // store the exception 
                cv.visitVarInsn(ASTORE, exceptionIndex);
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

    private Variable storeInTemp(String name, Type type) {
        Variable var  = defineVariable(createVariableName(name), type, false);
        int varIdx = var.getIndex();
        cv.visitVarInsn(ASTORE, varIdx);
        if (CREATE_DEBUG_INFO) cv.visitLabel(var.getStartLabel());
        return var;
    }

    public void visitSwitch(SwitchStatement statement) {
        onLineNumber(statement, "visitSwitch");

        statement.getExpression().visit(this);

        // switch does not have a continue label. use its parent's for continue
        pushBlockScope(false, true);
        //scope.setContinueLabel(scope.getParent().getContinueLabel());


        int switchVariableIndex = defineVariable(createVariableName("switch"), Type.OBJECT_TYPE).getIndex();
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

        Label breakLabel = scope.getBreakLabel();
        if (breakLabel != null ) {
            cv.visitJumpInsn(GOTO, breakLabel);
        } else {
            // should warn that break is not allowed in the context.
        }
    }

    public void visitContinueStatement(ContinueStatement statement) {
        onLineNumber(statement, "visitContinueStatement");

        Label continueLabel = scope.getContinueLabel();
        if (continueLabel != null ) {
            cv.visitJumpInsn(GOTO, continueLabel);
        } else {
            // should warn that continue is not allowed in the context.
        }
    }

    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        onLineNumber(statement, "visitSynchronizedStatement");

        statement.getExpression().visit(this);

        int index = defineVariable(createVariableName("synchronized"), Type.Integer_TYPE).getIndex();

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
        onLineNumber(statement, "visitReturnStatement");
        Type returnType = methodNode.getReturnType();
        if (returnType==Type.VOID_TYPE) {
        	if (!(statement == ReturnStatement.RETURN_NULL_OR_VOID)) {
                throwException("Cannot use return statement with an expression on a method that returns void");
        	}
            cv.visitInsn(RETURN);
            outputReturn = true;
            return;
        }

        Expression expression = statement.getExpression();
        evaluateExpression(expression);
        if (returnType==Type.OBJECT_TYPE && expression.getType() != null && expression.getType()==Type.VOID_TYPE) {
            cv.visitInsn(ACONST_NULL); // cheat the caller
            cv.visitInsn(ARETURN);
        } else {
            //return is based on class type
            //TODO: make work with arrays
            // we may need to cast
            helper.unbox(returnType);
            String returnTypeName = returnType.getName();
            if (returnTypeName.equals("double")) {
                cv.visitInsn(DRETURN);
            }
            else if (returnTypeName.equals("float")) {
                cv.visitInsn(FRETURN);
            }
            else if (returnTypeName.equals("long")) {
                cv.visitInsn(LRETURN);
            }
            else if (returnTypeName.equals("boolean")) {
                cv.visitInsn(IRETURN);
            }
            else if (
                    returnTypeName.equals("char")
                    || returnTypeName.equals("byte")
                    || returnTypeName.equals("int")
                    || returnTypeName.equals("short")) { //byte,short,boolean,int are
                // all IRETURN
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
    protected void doConvertAndCast(Type type, Expression expression, boolean ignoreAutoboxing, boolean forceCast) {
        Type expType = getExpressionType(expression);
        // temp resolution: convert all primitive casting to corresponsing Object type
        if (!ignoreAutoboxing && type.isPrimitiveType()) {
            type = type.getWrapper();
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
        //throw new ClassGeneratorException(s + ". Source: " + classNode.getName() + ":[" + this.lineNumber + ":" + this.columnNumber + "]");
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
        String innerClassinternalName = BytecodeHelper.getClassInternalName(innerClass.getType());

        ClassNode owner = innerClass.getOuterClass();
        Type ownerType = owner.getType();
/*
        if (classNode.isStaticClass() || isStaticMethod()) {
            ownerTypeName = "java.lang.Class";
        }
*/
        passingClosureParams = true;
        List constructors = innerClass.getDeclaredConstructors();
        ConstructorNode node = (ConstructorNode) constructors.get(0);
        Parameter[] localVariableParams = node.getParameters();


        //
        // Define in the context any variables that will be
        // created inside the closure.  Note that the first two
        // parameters are always _outerInstance and _delegate,
        // so we don't worry about them.

        for (int i = 2; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String name = param.getName();

            if (variableStack.get(name) == null && classNode.getField(name) == null) {
                defineVariable(name, Type.OBJECT_TYPE); // todo  should use param type is available
            }
        }

        cv.visitTypeInsn(NEW, innerClassinternalName);
        cv.visitInsn(DUP);
        if (isStaticMethod() || classNode.isStaticClass()) {
            visitClassExpression(new ClassExpression(ownerType));
        }
        else {
            loadThisOrOwner();
        }

        if (innerClass.getSuperClass()==Type.CLOSURE_TYPE) {
            if (isStaticMethod()) {
                /**
                 * todo could maybe stash this expression in a JVM variable
                 * from previous statement above
                 */
                visitClassExpression(new ClassExpression(ownerType));
            }
            else {
              cv.visitVarInsn(ALOAD, 0);
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
        Type type = expression.getType();
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
        expression.getExpression().visit(this);

        if (!isComparisonExpression(expression.getExpression())) {
// comment out for optimization when boolean values are not autoboxed for eg. function calls.
//           Class typeClass = expression.getExpression().getTypeClass();
//           if (typeClass != null && typeClass != boolean.class) {
                asBool.call(cv); // to return a primitive boolean
//            }
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
        if (superMethodCall && method.equals("<init>")) {
            /** todo handle method types! */
            cv.visitVarInsn(ALOAD, 0);
            if (isInClosureConstructor()) { // br use the second param to init the super class (Closure)
                cv.visitVarInsn(ALOAD, 2);
                cv.visitMethodInsn(INVOKESPECIAL, internalBaseClassName, "<init>", "(Ljava/lang/Object;)V");
            }
            else {
                cv.visitVarInsn(ALOAD, 1);
                cv.visitMethodInsn(INVOKESPECIAL, internalBaseClassName, "<init>", "(Ljava/lang/Object;)V");
            }
        }
        else {
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
            }
            else {
                if (superMethodCall) {
                    if (method.equals("super") || method.equals("<init>")) {
                        ConstructorNode superConstructorNode = findSuperConstructor(call);

                        cv.visitVarInsn(ALOAD, 0);

                        loadArguments(superConstructorNode.getParameters(), arguments);

                        String descriptor = BytecodeHelper.getMethodDescriptor("void", superConstructorNode.getParameters());
                        cv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(classNode.getSuperClass()), "<init>", descriptor);
                    }
                    else {
                        MethodNode superMethodNode = findSuperMethod(call);

                        cv.visitVarInsn(ALOAD, 0);

                        loadArguments(superMethodNode.getParameters(), arguments);

                        String descriptor = BytecodeHelper.getMethodDescriptor(superMethodNode.getReturnType(), superMethodNode.getParameters());
                        cv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(superMethodNode.getDeclaringClass().getType()), method, descriptor);
                    }
                }
                else {
                    if (emptyArguments(arguments) && !call.isSafe() && !call.isSpreadSafe()) {
                        call.getObjectExpression().visit(this);
                        cv.visitLdcInsn(method);
                        invokeNoArgumentsMethod.call(cv); // todo try if we can do early binding
                    }
                    else {
                        if (argumentsUseStack(arguments)) {

                            arguments.visit(this);

                            Variable tv = visitASTOREInTemp(method + "_arg");
                            int paramIdx = tv.getIndex();

                            call.getObjectExpression().visit(this); // xxx

                            cv.visitLdcInsn(method);

                            cv.visitVarInsn(ALOAD, paramIdx);
                            removeVar(tv);
                        }
                        else {
                            call.getObjectExpression().visit(this);
                            cv.visitLdcInsn(method);
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

            Type type = param.getType();
            Type expType = getExpressionType(argExp);
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
        ClassNode superClassNode = classNode.getSuperClassNode();
        if (superClassNode != null) {
            List methods = superClassNode.getMethods(methodName);
            for (Iterator iter = methods.iterator(); iter.hasNext(); ) {
                MethodNode method = (MethodNode) iter.next();
                if (method.getParameters().length == argCount) {
                    return method;
                }
            }
        }
        throwException("No such method: " + methodName + " for class: " + classNode.getType().getName());
        return null; // should not come here
    }

    /**
     * Attempts to find the constructor in a super class
     */
    protected ConstructorNode findSuperConstructor(MethodCallExpression call) {
        TupleExpression argExpr = (TupleExpression) call.getArguments();
        int argCount = argExpr.getExpressions().size();
        ClassNode superClassNode = classNode.getSuperClassNode();
        if (superClassNode != null) {
            List constructors = superClassNode.getDeclaredConstructors();
            for (Iterator iter = constructors.iterator(); iter.hasNext(); ) {
                ConstructorNode constructor = (ConstructorNode) iter.next();
                if (constructor.getParameters().length == argCount) {
                    return constructor;
                }
            }
        }
        throwException("No such constructor for class: " + classNode.getType().getName());
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

            cv.visitLdcInsn(call.getOwnerType().getName());
            cv.visitLdcInsn(call.getMethod());
            arguments.visit(this);

            invokeStaticMethodMethod.call(cv);
        }
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        onLineNumber(call, "visitConstructorCallExpression: \"" + call.getType().getName() + "\":");
        this.leftHandExpression = false;

        Expression arguments = call.getArguments();
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            int size = tupleExpression.getExpressions().size();
            if (size == 0) {
                arguments = null;
            }
        }

        // lets check that the type exists
        Type type = call.getType();
        type = classNode.resolveClassName(type, "");
        call.setType(type);
        
        if (this.classNode != null) {
            // TODO: GROOVY-435
            pushClassTypeArgument(this.classNode.getType(), this.classNode.getType());
            pushClassTypeArgument(this.classNode.getType(), type);

            if (arguments != null) {
                arguments.visit(this);
                invokeConstructorAtMethod.call(cv);
            } else {
                invokeNoArgumentsConstructorAt.call(cv);
            }
        }
        else {
            pushClassTypeArgument(this.classNode.getType(), type);

            if (arguments !=null) {
                arguments.visit(this);
                invokeConstructorOfMethod.call(cv);
            } else {
                invokeNoArgumentsConstructorOf.call(cv);
            }
        }
    }
    
    protected void pushClassTypeArgument(final Type ownerType, final Type type) {
        String staticFieldName = "class$" + type.getName().replace('.', '$').replace('[', '_').replace(';', '_');
        String ownerName = ownerType.getName().replace('.','/');

        syntheticStaticFields.add(staticFieldName);
        cv.visitFieldInsn(GETSTATIC, ownerName, staticFieldName, "Ljava/lang/Class;");
        Label l0 = new Label();
        cv.visitJumpInsn(IFNONNULL, l0);
        cv.visitLdcInsn(type.getName());
        cv.visitMethodInsn(INVOKESTATIC, ownerName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
        cv.visitInsn(DUP);
        cv.visitFieldInsn(PUTSTATIC, ownerName, staticFieldName, "Ljava/lang/Class;");
        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);
        cv.visitFieldInsn(GETSTATIC, ownerName, staticFieldName, "Ljava/lang/Class;");
        cv.visitLabel(l1);
    }
    
    // TODO: move this check before any scope checks, but after we know all classes
    // iterate from the inner most to the outer and check for classes
    // this check will ignore a .class property, for Exmaple Integer.class will be
    // a PropertyExpression with the ClassExpression of Integer as objectExprsssion
    // and class as property
    // NOTE: possible bug when a package name is equal to a classname
    private class PropertyClassTransformer extends CodeVisitorSupport {
        ClassExpression ce = null;
        String cname = "";
        boolean handled = false;
        Expression top = null;
        
        public void visitClassExpression(ClassExpression expression) {
            ce = expression;
        }
        
        public void visitPropertyExpression(PropertyExpression expression) {
            Expression oe = expression.getObjectExpression();
            // spreadsafe expressions will stop resolving
            // anything other than PropertyExpressions and VariableExpressions will stop resolving
            if (expression.isSpreadSafe() || !(oe instanceof PropertyExpression || oe instanceof VariableExpression || oe instanceof ClassExpression)) {
                handled = true;
                return;
            }                
            oe.visit(this);
            // if handled nothing to do
            if (handled) return;
            String pname = expression.getProperty();
            
            if (pname.equals("class")){
                if (ce!=null){
                    // we have found a class, the property is class
                    // so we possible want the resolved class here, like in Integer.class
                    // But if we have already used the classProperty, we don't want the 
                    // resolved class, example: Integer.class.class
                    // so simply return and set new top if this is the top level expression
                    if (expression==top) top=ce;
                }
                handled=true;
                return;
            }
            
            // if ce==null we have still resolve work to do
            else if (ce==null) {
                cname = cname+"."+pname;
                Type type = Type.makeType(cname);
                type = classNode.resolveClassName(type);
                if (type==null) return;
                ce = new ClassExpression(type);
                // if we are at top level, set new top
                if (expression==top) top=ce;
                return;
            } 
            // possible static access. But this is ignored for now and handled
            // as normal property
            expression.setObjectExpression(ce);
            handled=true;
            return;
        }
        
        public void visitVariableExpression(VariableExpression expression) {
            cname = expression.getName();
            // no resolving for "this"
            if (cname.equals("this")) {
                handled = true;
                return;
            }
            Type type = Type.makeType(cname);
            type = classNode.resolveClassName(type);
            if (type==null) return;
            ce = new ClassExpression(type);
        }
        
        public Expression transform(PropertyExpression pe) {
            top = pe;
            ce = null;
            cname = "";
            handled = false;
            
            pe.visit(this);
            // top may be modified to a ClassExpression now
            return top;
        }
    }    
    private PropertyClassTransformer propertyClassTransformer = new PropertyClassTransformer();
    
    public void visitPropertyExpression(PropertyExpression expression) {
        // lets check if we're a fully qualified class name
        Expression result = propertyClassTransformer.transform(expression);
        if (result!=expression) {
            result.visit(this);
            return;
        }

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
        Type type = field.getType();

        String ownerName = (field.getOwner().equals(classNode.getType()))
                ? internalClassName
                : org.objectweb.asm.Type.getInternalName(loadClass(field.getOwner()));
        if (holder) {
            cv.visitFieldInsn(GETSTATIC, ownerName, fldExp.getFieldName(), BytecodeHelper.getTypeDescription(type));
            cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
        }
        else {
            cv.visitFieldInsn(GETSTATIC, ownerName, fldExp.getFieldName(), BytecodeHelper.getTypeDescription(type));
            if (type.isPrimitiveType()) {
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
        Type type = field.getType();
        String ownerName = (field.getOwner().equals(classNode.getType()))
				? internalClassName
				: org.objectweb.asm.Type.getInternalName(loadClass(field.getOwner()));

        cv.visitVarInsn(ALOAD, 0);
		cv.visitFieldInsn(GETFIELD, ownerName, fldExp.getFieldName(), BytecodeHelper.getTypeDescription(type));

		if (holder) {
			cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
		} else {
			if (type.isPrimitiveType()) {
				helper.box(type);
			} else {
			}
		}
    }

    public void storeThisInstanceField(FieldExpression expression) {
        FieldNode field = expression.getField();

        boolean holder = field.isHolder() && !isInClosureConstructor();
        Type type = field.getType();

        String ownerName =  (field.getOwner().equals(classNode.getType())) ?
        		internalClassName : org.objectweb.asm.Type.getInternalName(loadClass(field.getOwner()));
        if (holder) {
            Variable tv = visitASTOREInTemp(field.getName());
            int tempIndex = tv.getIndex();
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, ownerName, expression.getFieldName(), BytecodeHelper.getTypeDescription(type));
            cv.visitVarInsn(ALOAD, tempIndex);
            cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
            removeVar(tv);
        }
        else {
            if (isInClosureConstructor()) {
                helper.doCast(type);
            }
            else {
                doConvertAndCast(type);
            }
            //Variable tmpVar = defineVariable(createVariableName(field.getName()), "java.lang.Object", false);
            Variable tmpVar = defineVariable(createVariableName(field.getName()), field.getType(), false);
            //int tempIndex = tmpVar.getIndex();
            //helper.store(field.getType(), tempIndex);
            helper.store(tmpVar, MARK_START);
            helper.loadThis(); //cv.visitVarInsn(ALOAD, 0);
            helper.load(tmpVar);
            helper.putField(field, ownerName);
            //cv.visitFieldInsn(PUTFIELD, ownerName, expression.getFieldName(), BytecodeHelper.getTypeDescription(type));
            // let's remove the temp var
            removeVar(tmpVar);
        }
    }


    public void storeStaticField(FieldExpression expression) {
    	FieldNode field = expression.getField();

        boolean holder = field.isHolder() && !isInClosureConstructor();

        Type type = field.getType();

        String ownerName = (field.getOwner().equals(classNode.getType()))
                ? internalClassName
                : org.objectweb.asm.Type.getInternalName(loadClass(field.getOwner()));
        if (holder) {
            Variable tv = visitASTOREInTemp(field.getName());
            int tempIndex = tv.getIndex();
            cv.visitFieldInsn(GETSTATIC, ownerName, expression.getFieldName(), BytecodeHelper.getTypeDescription(type));
            cv.visitVarInsn(ALOAD, tempIndex);
            cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
            removeVar(tv);
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

        Variable fieldTemp = defineVariable(createVariableName(field.getName()), Type.OBJECT_TYPE, false);
        int valueIdx = fieldTemp.getIndex();

        if (leftHandExpression && first) {
            cv.visitVarInsn(ASTORE, valueIdx);
            visitVariableStartLabel(fieldTemp);
        }

        if (steps > 1 || !isStatic) {
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(
                GETFIELD,
                internalClassName,
                "owner",
                BytecodeHelper.getTypeDescription(outerClassNode.getType()));
        }

        if( steps == 1 ) {
            int opcode = (leftHandExpression) ? ((isStatic) ? PUTSTATIC : PUTFIELD) : ((isStatic) ? GETSTATIC : GETFIELD);
            String ownerName = BytecodeHelper.getClassInternalName(outerClassNode.getType());

            if (leftHandExpression) {
                cv.visitVarInsn(ALOAD, valueIdx);
                boolean holder = field.isHolder() && !isInClosureConstructor();
                if ( !holder) {
                    doConvertAndCast(field.getType());
                }
            }
            cv.visitFieldInsn(opcode, ownerName, expression.getFieldName(), BytecodeHelper.getTypeDescription(field.getType()));
            if (!leftHandExpression) {
                if (field.getType().isPrimitiveType()) {
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
            visitClassExpression(new ClassExpression(classNode.getType()));
            return;                                               // <<< FLOW CONTROL <<<<<<<<<
        }

        //
        // "super" also requires special handling

        if (variableName.equals("super")) {
            visitClassExpression(new ClassExpression(classNode.getSuperClass()));
            return;                                               // <<< FLOW CONTROL <<<<<<<<<
        }


        //
        // class names return a Class instance, too

//        if (!variableName.equals("this")) {
//            String className = resolveClassName(variableName);
//            if (className != null) {
//                if (leftHandExpression) {
//                    throw new RuntimeParserException(
//                        "Cannot use a class expression on the left hand side of an assignment",
//                        expression);
//                }
//                visitClassExpression(new ClassExpression(className));
//                return;                                               // <<< FLOW CONTROL <<<<<<<<<
//            }
//        }


      //-----------------------------------------------------------------------
      // GENERAL VARIABLE LOOKUP


        //
        // We are handling only unqualified variables here.  Therefore,
        // we do not care about accessors, because local access doesn't
        // go through them.  Therefore, precedence is as follows:
        //   1) local variables, nearest block first
        //   2) class fields
        //   3) repeat search from 2) in next outer class

        boolean  handled  = false;
        Variable variable = (Variable)variableStack.get( variableName );

        if( variable != null ) {

            if( variable.isProperty() ) {
                processPropertyVariable(variable );
            }
            else {
                processStackVariable(variable );
            }

            handled = true;
        } else {
            //
            // Loop through outer classes for fields

            int       steps   = 0;
            ClassNode currentClassNode = classNode;
            FieldNode field   = null;

            do {
                if( (field = currentClassNode.getField(variableName)) != null ) {
                    if (methodNode == null || !methodNode.isStatic() || field.isStatic() )
                        break; //this is a match. break out. todo to be tested
                }
                steps++;

            } while( (currentClassNode = currentClassNode.getOuterClass()) != null );

            if( field != null ) {
                processFieldAccess( variableName, field, steps );
                handled = true;
            }
        }

        //
        // class names return a Class instance, too
        if (!handled  && !variableName.equals("this")) {
            Type classType = classNode.resolveClassName(Type.makeType(variableName));
            if (classType != null) {
                if (leftHandExpression) {
                    throwException("The variable name '"+variableName+"' conflicts with the class name '"+classType.getName()+"'. Please use another variable name");
                }
                visitClassExpression(new ClassExpression(classType));
                return;                                               // <<< FLOW CONTROL <<<<<<<<<
            }
        }

        //
        // Finally, if unhandled, create a variable for it.
        // Except there a stack variable should be created,
        // we define the variable as a property accessor and
        // let other parts of the classgen report the error
        // if the property doesn't exist.

        if( !handled ) {
            Type variableType = expression.getType();
            variable = defineVariable( variableName, variableType );

            if (leftHandExpression && variableType.isDynamic()) {
                variable.setDynamic(true); // false  by default
            }
            else {
                variable.setDynamic(false);
            }

            if( isInScriptBody() || !leftHandExpression ) { // todo problematic: if on right hand not defined, should I report undefined var error?
                variable.setProperty( true );
                processPropertyVariable(variable );
            }
            else {
                processStackVariable(variable );
            }
        }
    }


    protected void processStackVariable(Variable variable ) {
        boolean holder = variable.isHolder() && !passingClosureParams;

        if( leftHandExpression ) {
            helper.storeVar(variable, holder);
        }
        else {
        	helper.loadVar(variable, holder);
        }
        if (ASM_DEBUG) {
            helper.mark("var: " + variable.getName());
        }
    }

    private void visitVariableStartLabel(Variable variable) {
        if (CREATE_DEBUG_INFO) {
            Label l = variable.getStartLabel();
            if (l != null) {
                cv.visitLabel(l);
            } else {
                System.out.println("start label == null! what to do about this?");
            }
        }
    }

    protected void processPropertyVariable(Variable variable ) {
    	String name = variable.getName();
        if (variable.isHolder() && passingClosureParams && isInScriptBody() ) {
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
            if (expression.getType()==Type.VOID_TYPE) { // nothing on the stack
                return false;
            } else {
                return !MethodCallExpression.isSuperMethodCall((MethodCallExpression) expression);
            }
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
        return true;
    }

    protected boolean firstStatementIsSuperInit(Statement code) {
        ExpressionStatement expStmt = null;
        if (code instanceof ExpressionStatement) {
            expStmt = (ExpressionStatement) code;
        }
        else if (code instanceof BlockStatement) {
            BlockStatement block = (BlockStatement) code;
            if (!block.getStatements().isEmpty()) {
                Object expr = block.getStatements().get(0);
                if (expr instanceof ExpressionStatement) {
                    expStmt = (ExpressionStatement) expr;
                }
            }
        }
        if (expStmt != null) {
            Expression expr = expStmt.getExpression();
            if (expr instanceof MethodCallExpression) {
            	MethodCallExpression call = (MethodCallExpression) expr;
                if (MethodCallExpression.isSuperMethodCall(call)) {
                    // not sure which one is constantly used as the super class ctor call. To cover both for now
                	return call.getMethod().equals("<init>") || call.getMethod().equals("super");
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
            cv.visitTryCatchBlock(l0, l2, l2, "java/lang/ClassNotFoundException"); // br using l2 as the 2nd param seems create the right table entry
            cv.visitMaxs(3, 2);

            cw.visitEnd();
        }
    }

    /** load class object on stack */
    public void visitClassExpression(ClassExpression expression) {
        Type type = expression.getType();
        //type = checkValidType(type, expression, "Must be a valid type name for a constructor call");


        if (type.isPrimitiveType()) {
            Type objectType = type.getWrapper();
            cv.visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(objectType), "TYPE", "Ljava/lang/Class;");
        }
        else {
            final String staticFieldName =
                (type.equals(classNode.getType())) ? "class$0" : "class$" + type.getName().replace('.', '$').replace('[', '_').replace(';', '_');

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
        //createTupleMethod.call(cv);
    }

    public void visitArrayExpression(ArrayExpression expression) {
        Type type = expression.getType().getComponentType();
        String typeName = BytecodeHelper.getClassInternalName(type);        
        Expression sizeExpression = expression.getSizeExpression();

        int size=0;
        if (sizeExpression != null) {
            // lets convert to an int
            visitAndAutoboxBoolean(sizeExpression);
            asIntMethod.call(cv);
        } else {
            size = expression.getExpressions().size();
            helper.pushConstant(size);
        }

        int storeIns=AASTORE;
        if (type.isPrimitiveType()) {
            int primType=0;
            if (type==Type.boolean_TYPE) {
                primType = T_BOOLEAN;
                storeIns = BASTORE;
            } else if (type==Type.char_TYPE) {
                primType = T_CHAR;
                storeIns = CASTORE;
            } else if (type==Type.float_TYPE) {
                primType = T_FLOAT;
                storeIns = FASTORE;
            } else if (type==Type.double_TYPE) {
                primType = T_DOUBLE;
                storeIns = DASTORE;
            } else if (type==Type.byte_TYPE) {
                primType = T_BYTE;
                storeIns = BASTORE;
            } else if (type==Type.short_TYPE) {
                primType = T_SHORT;
                storeIns = SASTORE;
            } else if (type==Type.int_TYPE) {
                primType = T_INT;
                storeIns=IASTORE;
            } else if (type==Type.long_TYPE) {
                primType = T_LONG;
                storeIns = LASTORE;
            } 
            cv.visitIntInsn(NEWARRAY, primType);
        } else {
            cv.visitTypeInsn(ANEWARRAY, typeName);
        }

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            helper.pushConstant(i);
            Expression elementExpression = expression.getExpression(i);
            if (elementExpression == null) {
                ConstantExpression.NULL.visit(this);
            } else {
                if (!type.equals(elementExpression.getType())) {
                    visitCastExpression(new CastExpression(type, elementExpression, true));
                } else {
                    visitAndAutoboxBoolean(elementExpression);
                }
            }
            cv.visitInsn(storeIns);            
        }
        
        if (type.isPrimitiveType()) {
            int par = defineVariable("par",Type.OBJECT_TYPE).getIndex();
            cv.visitVarInsn(ASTORE, par);
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

        Variable tv = visitASTOREInTemp("iterator");
        int paramIdx = tv.getIndex();

        ClassNode innerClass = createGStringClass(expression);
        addInnerClass(innerClass);
        String innerClassinternalName = BytecodeHelper.getClassInternalName(innerClass.getType());

        cv.visitTypeInsn(NEW, innerClassinternalName);
        cv.visitInsn(DUP);
        cv.visitVarInsn(ALOAD, paramIdx);

        cv.visitMethodInsn(INVOKESPECIAL, innerClassinternalName, "<init>", "([Ljava/lang/Object;)V");
        removeVar(tv);
    }

    private Variable visitASTOREInTemp(String s) {
        return storeInTemp(s, Type.OBJECT_TYPE);
    }

    public void visitAnnotations(AnnotatedNode node) {
        Map annotionMap = node.getAnnotations();
        if (annotionMap.isEmpty()) return;
        Iterator it = annotionMap.keySet().iterator(); 
        while (it.hasNext()) {
            String clazz = (String) it.next();
            
            //skip builtin properties
            if (clazz.equals("Property")) continue;
            Type type = Type.makeType(clazz);
            type = classNode.resolveClassName(type,"unable to find class for annotation");
            
            AnnotationVisitor av = cw.visitAnnotation(BytecodeHelper.formatNameForClassLoading(clazz),false);
            
            AnnotationNode an = (AnnotationNode) node.getAnnotations(clazz);
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
        boolean parentIsInnerClass = owner instanceof InnerClassNode;
        String outerClassName = owner.getType().getName();
        String name = outerClassName + "$"
                + context.getNextClosureInnerName(owner, classNode, methodNode); // br added a more infomative name
        boolean staticMethodOrInStaticClass = isStaticMethod() || classNode.isStaticClass();
        if (staticMethodOrInStaticClass) {
            outerClassName = "java.lang.Class";
        }
        Parameter[] parameters = expression.getParameters();
        if (parameters == null || parameters.length == 0) {
            // lets create a default 'it' parameter
            parameters = new Parameter[] { new Parameter(Type.OBJECT_TYPE, "it", ConstantExpression.NULL)};
        }

        Parameter[] localVariableParams = getClosureSharedVariables(expression);

        InnerClassNode answer = new InnerClassNode(owner, Type.makeType(name), 0, Type.CLOSURE_TYPE); // clsures are local inners and not public
        answer.setEnclosingMethod(this.methodNode);
        answer.setSynthetic(true);
        
        if (staticMethodOrInStaticClass) {
            answer.setStaticClass(true);
        }
        if (isInScriptBody()) {
            answer.setScriptBody(true);
        }
        MethodNode method =
            answer.addMethod("doCall", ACC_PUBLIC, Type.OBJECT_TYPE, parameters, expression.getCode());

        method.setLineNumber(expression.getLineNumber());
        method.setColumnNumber(expression.getColumnNumber());

        VariableScope varScope = expression.getVariableScope();
        if (varScope == null) {
            throw new RuntimeException(
                "Must have a VariableScope by now! for expression: " + expression + " class: " + name);
        }
        else {
            method.setVariableScope(varScope);
        }
        if (parameters.length > 1
            || (parameters.length == 1
                && parameters[0].getType() != null
                && parameters[0].getType() != Type.OBJECT_TYPE)) {

            // lets add a typesafe call method
            answer.addMethod(
                "call",
                ACC_PUBLIC,
                Type.OBJECT_TYPE,
                parameters,
                new ReturnStatement(
                    new MethodCallExpression(
                        VariableExpression.THIS_EXPRESSION,
                        "doCall",
                        new ArgumentListExpression(parameters))));
        }

        FieldNode ownerField = answer.addField("owner", ACC_PRIVATE, Type.makeType(outerClassName), null);

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
                    new FieldExpression(ownerField),
                    Token.newSymbol(Types.EQUAL, -1, -1),
                    new VariableExpression("_outerInstance"))));

        // lets assign all the parameter fields from the outer context
        for (int i = 0; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String paramName = param.getName();
            boolean holder = mutableVars.contains(paramName);
            Expression initialValue = null;
            Type type = param.getType();
            FieldNode paramField = null;
            if (holder) {
            	initialValue = new VariableExpression(paramName);
                type = type.makeReference();
                param.setType(type);
                paramField = answer.addField(paramName, ACC_PRIVATE, type, initialValue);
                paramField.setHolder(true);
                Type realType = type.getRealType();
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
                        new BinaryExpression(expression, Token.newSymbol(Types.EQUAL, 0, 0), new VariableExpression("__value"))));
                        */
            }
            else {
            	PropertyNode propertyNode = answer.addProperty(paramName, ACC_PUBLIC, type, initialValue, null, null);
                paramField = propertyNode.getField();
                block.addStatement(
                    new ExpressionStatement(
                        new BinaryExpression(
                            new FieldExpression(paramField),
                            Token.newSymbol(Types.EQUAL, -1, -1),
                            new VariableExpression(paramName))));
            }
        }

        Parameter[] params = new Parameter[2 + localVariableParams.length];
        params[0] = new Parameter(Type.makeType(outerClassName), "_outerInstance");
        params[1] = new Parameter(Type.OBJECT_TYPE, "_delegate");
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
        String outerClassName = owner.getType().getName();
        String name = outerClassName + "$" + context.getNextInnerClassIdx();
        InnerClassNode answer = new InnerClassNode(owner, Type.makeType(name), 0, Type.GSTRING_TYPE);
        answer.setEnclosingMethod(this.methodNode);
        FieldNode stringsField =
            answer.addField(
                "strings",
                ACC_PRIVATE /*| ACC_STATIC*/,
                Type.STRING_TYPE.makeArray(),
                new ArrayExpression(Type.STRING_TYPE, expression.getStrings()));
        answer.addMethod(
            "getStrings",
            ACC_PUBLIC,
            Type.STRING_TYPE.makeArray(),
            Parameter.EMPTY_ARRAY,
            new ReturnStatement(new FieldExpression(stringsField)));
        // lets make the constructor
        BlockStatement block = new BlockStatement();
        block.addStatement(
            new ExpressionStatement(
                new MethodCallExpression(new VariableExpression("super"), "<init>", new VariableExpression("values"))));
        Parameter[] contructorParams = new Parameter[] { new Parameter(Type.OBJECT_TYPE.makeArray(), "values")};
        answer.addConstructor(ACC_PUBLIC, contructorParams, block);
        return answer;
    }

    protected void doConvertAndCast(Type type) {
        if (type==Type.OBJECT_TYPE) return;
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

    /**
     * note: leave the primitive boolean on staock for comparison expressions. All the result types need to match the
     * utility methods in the ScriptBytecodeAdapter.
     * @param compareMethod
     * @param expression
     */
    protected void evalBinaryExp_EarlyBinding(MethodCaller compareMethod, BinaryExpression expression) {
        Expression leftExp = expression.getLeftExpression();
        Expression rightExp = expression.getRightExpression();

        if (expression.getType() == Type.DYNAMIC_TYPE){
            evalBinaryExp_LateBinding(compareMethod, expression);
            return;
        }
        else {
            Class lclass = leftExp.getType().getTypeClass();
            Class rclass = rightExp.getType().getTypeClass();
            if (lclass == null || rclass == null) {
                if ((lclass == null && rclass != null) || (lclass != null && rclass == null)) {
                    // lets treat special cases: obj == null / obj != null . leave primitive boolean on the stack, which will be boxed by visitAndAutoBox()
                    if (leftExp == ConstantExpression.NULL && !rclass.isPrimitive() ||
                            rightExp == ConstantExpression.NULL && !lclass.isPrimitive()) {
                        Expression exp = leftExp == ConstantExpression.NULL? rightExp : leftExp;
                        int type = expression.getOperation().getType();
                        switch (type) {
                            case Types.COMPARE_EQUAL :
                                load(exp);
                                cv.visitInsn(ICONST_1);
                                cv.visitInsn(SWAP);
                                Label l1 = new Label();
                                cv.visitJumpInsn(IFNULL, l1);
                                cv.visitInsn(POP);
                                cv.visitInsn(ICONST_0);
                                cv.visitLabel(l1);
                                return;
                            case Types.COMPARE_NOT_EQUAL :
                                load(exp);
                                cv.visitInsn(ICONST_1);
                                cv.visitInsn(SWAP);
                                Label l2 = new Label();
                                cv.visitJumpInsn(IFNONNULL, l2);
                                cv.visitInsn(POP);
                                cv.visitInsn(ICONST_0);
                                cv.visitLabel(l2);
                                return;
                            default:
                                evalBinaryExp_LateBinding(compareMethod, expression);
                                return;
                        }
                    }
                    else {
                        evalBinaryExp_LateBinding(compareMethod, expression);
                        return;
                    }
                }
                else {
                    evalBinaryExp_LateBinding(compareMethod, expression);
                    return;
                }
            }
            else if (lclass == String.class && rclass == String.class) {
                int type = expression.getOperation().getType();
                switch (type) {
                    case Types.COMPARE_EQUAL : // ==
                        load(leftExp); cast(String.class);
                        load(rightExp); cast(String.class);
                        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
                        //helper.quickBoxIfNecessary(boolean.class);
                        return;
                    case Types.COMPARE_NOT_EQUAL :
                        load(leftExp);cast(String.class);
                        load(rightExp); cast(String.class);
                        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
                        cv.visitInsn(ICONST_1);
                        cv.visitInsn(IXOR);
                        //helper.quickBoxIfNecessary(boolean.class);
                        return;
                    case Types.COMPARE_TO :
                        load(leftExp);cast(String.class);
                        load(rightExp); cast(String.class);
                        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/Object;)I");
                        helper.quickBoxIfNecessary(int.class); // object type
                        return;
                    case Types.COMPARE_GREATER_THAN :
                    case Types.COMPARE_GREATER_THAN_EQUAL :
                    case Types.COMPARE_LESS_THAN :
                    case Types.COMPARE_LESS_THAN_EQUAL :
                        {
                            int op;
                            switch (type) {
                                case Types.COMPARE_GREATER_THAN :
                                    op = IFLE;
                                    break;
                                case Types.COMPARE_GREATER_THAN_EQUAL :
                                    op = IFLT;
                                    break;
                                case Types.COMPARE_LESS_THAN :
                                    op = IFGE;
                                    break;
                                case Types.COMPARE_LESS_THAN_EQUAL :
                                    op = IFGT;
                                    break;
                                default:
                                    System.err.println("flow control error: should not be here. type: " + type);
                                    return;
                            }
                            load(leftExp);cast(String.class);
                            load(rightExp); cast(String.class);
                            cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/Object;)I");

                            // set true/false on stack
                            Label l4 = new Label();
                            cv.visitJumpInsn(op, l4);
                            // need to use primitive boolean //cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
                            cv.visitInsn(ICONST_1);  // true
                            Label l5 = new Label();
                            cv.visitJumpInsn(GOTO, l5);
                            cv.visitLabel(l4);
                            cv.visitInsn(ICONST_0); //cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
                            cv.visitLabel(l5);
                        }
                        return;

                    default:
                        evalBinaryExp_LateBinding(compareMethod, expression);
                        return;
                }
            }
            else if (Integer.class == lclass && Integer.class == rclass) {
                int type = expression.getOperation().getType();
                switch (type) {
                    case Types.COMPARE_EQUAL : // ==
                        load(leftExp); cast(Integer.class);
                        load(rightExp);
                        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "equals", "(Ljava/lang/Object;)Z");
                        //helper.quickBoxIfNecessary(boolean.class);
                        return;
                    case Types.COMPARE_NOT_EQUAL :
                        load(leftExp); cast(Integer.class);
                        load(rightExp);
                        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "equals", "(Ljava/lang/Object;)Z");
                        cv.visitInsn(ICONST_1);
                        cv.visitInsn(IXOR);
                        //helper.quickBoxIfNecessary(boolean.class);
                        return;
                    case Types.COMPARE_TO :
                        load(leftExp); cast(Integer.class);
                        load(rightExp);
                        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "compareTo", "(Ljava/lang/Object;)I");
                        helper.quickBoxIfNecessary(int.class);
                        return;
                    case Types.COMPARE_GREATER_THAN :
                    case Types.COMPARE_GREATER_THAN_EQUAL :
                    case Types.COMPARE_LESS_THAN :
                    case Types.COMPARE_LESS_THAN_EQUAL :
                        {
                            int op;
                            switch (type) {
                                case Types.COMPARE_GREATER_THAN :
                                    op = IFLE;
                                    break;
                                case Types.COMPARE_GREATER_THAN_EQUAL :
                                    op = IFLT;
                                    break;
                                case Types.COMPARE_LESS_THAN :
                                    op = IFGE;
                                    break;
                                case Types.COMPARE_LESS_THAN_EQUAL :
                                    op = IFGT;
                                    break;
                                default:
                                    System.err.println("flow control error: should not be here. type: " + type);
                                    return;
                            }
                            load(leftExp); cast(Integer.class);
                            load(rightExp);
                            cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "compareTo", "(Ljava/lang/Object;)I");

                            Label l4 = new Label();
                            cv.visitJumpInsn(op, l4);
                            cv.visitInsn(ICONST_1); //cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
                            Label l5 = new Label();
                            cv.visitJumpInsn(GOTO, l5);
                            cv.visitLabel(l4);
                            cv.visitInsn(ICONST_0);//cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
                            cv.visitLabel(l5);
                        }
                        return;

                    default:
                        evalBinaryExp_LateBinding(compareMethod, expression);
                        return;
                }
            }
            else {
                evalBinaryExp_LateBinding(compareMethod, expression);
                return;
            }
        }
    }

    private void cast(Class aClass) {
        if (!aClass.isPrimitive() && aClass != Object.class) {
            cv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(aClass.getName()));
        }
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

        Type type = getLHSType(leftExpression);
        // lets not cast for primitive types as we handle these in field setting etc
        if (type.isPrimitiveType()) {
            rightExpression.visit(this);
        } else {
            if (type!=Type.OBJECT_TYPE){
                visitCastExpression(new CastExpression(type, rightExpression));
            } else {
                visitAndAutoboxBoolean(rightExpression);
            }
        }

        cv.visitInsn(DUP);  // to leave a copy of the rightexpression value on the stack after the assignment.
        leftHandExpression = true;
        leftExpression.visit(this);
        leftHandExpression = false;
    }

    private void copyTypeClass(Expression leftExpression, Expression rightExpression) {
        // copy type class from the right to the left, boxing numbers & treat ClassExpression specially
        Type rType = rightExpression.getType();
        if (rightExpression instanceof ClassExpression) {
            leftExpression.setType(Type.makeType(Class.class));
        }
        else {
            rType = BytecodeHelper.boxOnPrimitive(rType);
            leftExpression.setType(rType);
        }
    }

    
    /**
     * Deduces the type name required for some casting
     *
     * @return the type of the given (LHS) expression or null if it is java.lang.Object or it cannot be deduced
     */
    protected Type getLHSType(Expression leftExpression) {
        do {
// commented out. not quiteworking yet. would complain something like:
//java.lang.ClassFormatError: Foo$1 (Illegal Field name "class$[Ljava$lang$String;")
//
//            if (ENABLE_EARLY_BINDING) {
//                String type = leftExpression.getType();
//                if (type == null)
//                    break;
//                return isValidTypeForCast(type) ? type : null;
//            }
        } while (false);

        if (leftExpression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) leftExpression;
            Type type = varExp.getType();
            if (isValidTypeForCast(type)) {
                return type;
            }
            String variableName = varExp.getName();
            Variable variable = (Variable) variableStack.get(variableName);
            if (variable != null) {
                if (variable.isHolder() || variable.isProperty()) {
                    return variable.getType();
                }
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
            Type type = fieldExp.getType();
            if (isValidTypeForCast(type)) {
                return type;
            }
        }
        return Type.DYNAMIC_TYPE;
    }

    protected boolean isValidTypeForCast(Type type) {
        return type!=Type.DYNAMIC_TYPE && !type.getName().equals("groovy.lang.Reference") && !type.isPrimitiveType();
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

        Variable tv = visitASTOREInTemp("postfix_" + method);
        int tempIdx  = tv.getIndex();
        cv.visitVarInsn(ALOAD, tempIdx);

        cv.visitLdcInsn(method);
        invokeNoArgumentsMethod.call(cv);

        store(expression);

        cv.visitVarInsn(ALOAD, tempIdx);
        removeVar(tv);
    }

    protected boolean isHolderVariable(Expression expression) {
        if (expression instanceof FieldExpression) {
            FieldExpression fieldExp = (FieldExpression) expression;
            return fieldExp.getField().isHolder();
        }
        if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            Variable variable = (Variable) variableStack.get(varExp.getName());
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

    protected void evaluateInstanceof(BinaryExpression expression) {
        expression.getLeftExpression().visit(this);
        Expression rightExp = expression.getRightExpression();
        Type classType = Type.DYNAMIC_TYPE;
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

    protected VariableScope getVariableScope() {
        if (variableScope == null) {
            if (methodNode != null) {
                // if we're a closure method we'll have our variable scope already created
                variableScope = methodNode.getVariableScope();
            }
            else if (constructorNode != null) {
                variableScope = constructorNode.getVariableScope();
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

        //
        // First up, get the scopes for outside and inside the closure.
        // The inner scope must cover all nested closures, as well, as
        // everything that will be needed must be imported.

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


        //
        // DeclaredVariables include any name that was assigned to within
        // the scope.  ReferencedVariables include any name that was read
        // from within the scope.  We get the sets from each and must piece
        // together the stack variable import list for the closure.  Note
        // that we don't worry about field variables here, as we don't have
        // to do anything special with them.  Stack variables, on the other
        // hand, have to be wrapped up in References for use.

        Set outerDecls = outerScope.getDeclaredVariables();
        Set outerRefs  = outerScope.getReferencedVariables();
        Set innerDecls = innerScope.getDeclaredVariables();
        Set innerRefs  = innerScope.getReferencedVariables();


        //
        // So, we care about any name referenced in the closure UNLESS:
        //   1) it's not declared in the outer context;
        //   2) it's a parameter;
        //   3) it's a field in the context class that isn't overridden
        //      by a stack variable in the outer context.
        //
        // BUG: We don't actually have the necessary information to do
        //      this right!  The outer declarations don't distinguish
        //      between assignments and variable declarations.  Therefore
        //      we can't tell when field variables have been overridden
        //      by stack variables in the outer context.  This must
        //      be fixed!

        Set varSet = new HashSet();
        for (Iterator iter = innerRefs.iterator(); iter.hasNext();) {
            String var = (String) iter.next();
            // lets not pass in fields from the most-outer class, but pass in values from an outer closure
            if (outerDecls.contains(var) && (isNotFieldOfOutermostClass(var))) {
                Type type = getVariableType(var);
                vars.add(new Parameter(type, var));
                varSet.add(var);
            }
        }
        for (Iterator iter = outerRefs.iterator(); iter.hasNext();) {
            String var = (String) iter.next();
            // lets not pass in fields from the most-outer class, but pass in values from an outer closure
            if (innerDecls.contains(var) && (isNotFieldOfOutermostClass(var)) && !varSet.contains(var)) {
                Type type = getVariableType(var);
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

    private boolean isInnerClass() {
        return classNode instanceof InnerClassNode;
    }

    protected Type getVariableType(String name) {
        Variable variable = (Variable) variableStack.get(name);
        if (variable != null) {
            return Type.makeType(variable.getTypeName());
        }
        return Type.DYNAMIC_TYPE;
    }

    protected void resetVariableStack(Parameter[] parameters) {
        lastVariableIndex = -1;
        variableStack.clear();

        scope = new BlockScope(null);
        //pushBlockScope();

        // lets push this onto the stack
        definingParameters = true;
        if (!isStaticMethod()) {
            defineVariable("this", classNode.getType()).getIndex();
        } // now lets create indices for the parameteres
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Type type = parameter.getType();
            Variable v = defineVariable(parameter.getName(), type);
            int idx = v.getIndex();
            if (type.isPrimitiveType()) {
                helper.load(type, idx);
                helper.box(type);
                cv.visitVarInsn(ASTORE, idx);
            }
        }
        definingParameters = false;
    }

    protected void popScope() {
        int lastID = scope.getFirstVariableIndex();

        List removeKeys = new ArrayList();
        for (Iterator iter = variableStack.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            Variable value = (Variable) entry.getValue();
            if (value.getIndex() >= lastID) {
                removeKeys.add(name);
            }
        }
        for (Iterator iter = removeKeys.iterator(); iter.hasNext();) {
            Variable v  = (Variable) variableStack.remove(iter.next());
            if (CREATE_DEBUG_INFO) { // set localvartable
                if (v != null) {
                    visitVariableEndLabel(v);
                    cv.visitLocalVariable(
                                          v.getName(),
                                          BytecodeHelper.getTypeDescription(v.getTypeName()),
                                          null,
                                          v.getStartLabel(),
                                          v.getEndLabel(),
                                          v.getIndex()
                                          );
                }
            }
        }
        scope = scope.getParent();
    }

    void removeVar(Variable v ) {
    	variableStack.remove(v.getName());
        if (CREATE_DEBUG_INFO) { // set localvartable
        	Label endl = new Label();
        	cv.visitLabel(endl);
        	cv.visitLocalVariable(
                                v.getName(),
                                BytecodeHelper.getTypeDescription(v.getTypeName()),
                                null,
                                v.getStartLabel(),
                                endl,
                                v.getIndex()
                                );
        }
    }
    private void visitVariableEndLabel(Variable v) {
        if (CREATE_DEBUG_INFO) {
            if(v.getEndLabel() == null) {
                Label end = new Label();
                v.setEndLabel(end);
            }
            cv.visitLabel(v.getEndLabel());
        }
    }

    protected void pushBlockScope() {
        pushBlockScope(true, true);
    }

    /**
     * create a new scope. Set break/continue label if the canXXX parameter is true. Otherwise
     * inherit parent's label.
     * @param canContinue   true if the start of the scope can take continue label
     * @param canBreak  true if the end of the scope can take break label
     */
    protected void pushBlockScope(boolean canContinue, boolean canBreak) {
        BlockScope parentScope = scope;
        scope = new BlockScope(parentScope);
        scope.setContinueLabel(canContinue ? new Label() : (parentScope == null ? null : parentScope.getContinueLabel()));
        scope.setBreakLabel(canBreak? new Label() : (parentScope == null ? null : parentScope.getBreakLabel()));
        scope.setFirstVariableIndex(getNextVariableID());
    }

    /**
     * Defines the given variable in scope and assigns it to the stack
     */
    protected Variable defineVariable(String name, Type type) {
        return defineVariable(name, type, true);
    }

    private Variable defineVariable(String name, Type type, boolean define) {
        Variable answer = (Variable) variableStack.get(name);
        if (answer == null) {
            lastVariableIndex = getNextVariableID();
            answer = new Variable(lastVariableIndex, type, name);
            if (mutableVars.contains(name)) {
                answer.setHolder(true);
            }
            variableStack.put(name, answer);

            Label startLabel  = new Label();
            answer.setStartLabel(startLabel);
            if (define) {
                if (definingParameters) {
                    if (answer.isHolder()) {
                        cv.visitTypeInsn(NEW, "groovy/lang/Reference"); // br todo to associate a label with the variable
                        cv.visitInsn(DUP);
                        cv.visitVarInsn(ALOAD, lastVariableIndex);
                        cv.visitMethodInsn(INVOKESPECIAL, "groovy/lang/Reference", "<init>", "(Ljava/lang/Object;)V");
                        cv.visitVarInsn(ASTORE, lastVariableIndex);
                        cv.visitLabel(startLabel);
                    }
                }
                else {
                    // using new variable inside a comparison expression
                    // so lets initialize it too
                    if (answer.isHolder() && !isInScriptBody()) {
                        //cv.visitVarInsn(ASTORE, lastVariableIndex + 1); // I might need this to set the reference value

                        cv.visitTypeInsn(NEW, "groovy/lang/Reference");
                        cv.visitInsn(DUP);
                        cv.visitMethodInsn(INVOKESPECIAL, "groovy/lang/Reference", "<init>", "()V");

                        cv.visitVarInsn(ASTORE, lastVariableIndex);
                        cv.visitLabel(startLabel);
                        //cv.visitVarInsn(ALOAD, idx + 1);
                    }
                    else {
                        if (!leftHandExpression) { // new var on the RHS: init with null
                            cv.visitInsn(ACONST_NULL);
                            cv.visitVarInsn(ASTORE, lastVariableIndex);
                            cv.visitLabel(startLabel);
                        }
                    }
                }
            }
        }
        return answer;
    }

    private boolean isDoubleSizeVariable(Type type) {
        return type==Type.long_TYPE || type==Type.double_TYPE;
    }

    private int getNextVariableID() {
    	int index = 0;
    	for (Iterator iter = variableStack.values().iterator(); iter.hasNext();) {
    		Variable var = (Variable) iter.next();
    		if (isDoubleSizeVariable(var.getType())) {
    			index += 2;
    		} else {
    			index++;
    		}
    	}
    	return index;
    }

    /** @return true if the given name is a local variable or a field */
    protected boolean isFieldOrVariable(String name) {
        return variableStack.containsKey(name) || classNode.getField(name) != null;
    }

    /*protected String resolveClassName(String type) {
        return classNode.resolveClassName(type);
    }*/

    protected String createVariableName(String type) {
        return "__" + type + (++tempVariableNameCounter);
    }

    /**
     * @return if the type of the expression can be determined at compile time
     *         then this method returns the type - otherwise null
     */
    protected Type getExpressionType(Expression expression) {
        if (isComparisonExpression(expression)) {
            return Type.boolean_TYPE;
        }
        if (expression instanceof VariableExpression) {
            VariableExpression varExpr = (VariableExpression) expression;
            Variable variable = (Variable) variableStack.get(varExpr.getName());
            if (variable != null && !variable.isHolder()) {
                Type type = variable.getType();
                if (! type.isDynamic()) return type;
            }
        }
        return expression.getType();
    }

    /**
     * @return true if the value is an Integer, a Float, a Long, a Double or a
     *         String .
     */
    protected static boolean isPrimitiveFieldType(Type t) {
        String type = t.getName();
        return type.equals("java.lang.String")
            || type.equals("java.lang.Integer")
            || type.equals("java.lang.Double")
            || type.equals("java.lang.Long")
            || type.equals("java.lang.Float");
    }

    protected boolean isInClosureConstructor() {
        return constructorNode != null
            && classNode.getOuterClass() != null
            && classNode.getSuperClass()==Type.CLOSURE_TYPE;
    }

    protected boolean isStaticMethod() {
        if (methodNode == null) { // we're in a constructor
            return false;
        }
        return methodNode.isStatic();
    }

    Map classCache = new HashMap();
    {
        classCache.put("int", Integer.TYPE);
        classCache.put("byte", Byte.TYPE);
        classCache.put("short", Short.TYPE);
        classCache.put("char", Character.TYPE);
        classCache.put("boolean", Boolean.TYPE);
        classCache.put("long", Long.TYPE);
        classCache.put("double", Double.TYPE);
        classCache.put("float", Float.TYPE);
        classCache.put("void", Void.TYPE);
    }
    /**
     * @return loads the given type name
     */
    protected Class loadClass(Type t) {
        if (t.getTypeClass()!=null) return t.getTypeClass();
        String name = t.getName();

        if (t.equals(this.classNode.getType())) {
            return Object.class;
        }

        if (name == null) {
            return null;
        }
        else if (name.length() == 0) {
            return Object.class;
        }

        name = BytecodeHelper.formatNameForClassLoading(name);

    	try {
    		Class cls = (Class)classCache.get(name);
    		if (cls != null)
    			return cls;

    		CompileUnit compileUnit = getCompileUnit();
            if (compileUnit != null) {
            	cls = compileUnit.loadClass(name);
                classCache.put(name, cls);
            	return cls;
            }
            else {
                throw new ClassGeneratorException("Could not load class: " + name);
            }
        }
        catch (ClassNotFoundException e) {
            throw new ClassGeneratorException("Error when compiling class: " + classNode.getType().getName() + ". Reason: could not load class: " + name + " reason: " + e, e);
        }
    }

    protected CompileUnit getCompileUnit() {
        CompileUnit answer = classNode.getCompileUnit();
        if (answer == null) {
            answer = context.getCompileUnit();
        }
        return answer;
    }
 
    /** search in the current classNode and super class for matching method */
    private MetaMethod getMethodOfThisAndSuper(String methName, Class[] argsArray, boolean isStaticCall) {
        MethodNode candidate = null;
        List meths = classNode.getMethods();
        Class[] candidateParamClasses = null;
        for (int i = 0; i < meths.size(); i++) {
            MethodNode meth = (MethodNode) meths.get(i);
            if (meth.getName().equals(methName)) {
                Parameter[] params = meth.getParameters();
                if  (params.length == argsArray.length) {
                    Class[] paramClasses = new Class[params.length];
                    for (int j = 0; j < params.length; j++) {
                        Parameter param = params[j];
                        Type type = param.getType();
                        Class paramClass = null;
                        try {
                            paramClass = loadClass(type);
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                            return null;
                        }
                        paramClasses[j] = paramClass;
                    }
                    if (MetaClass.isValidMethod(paramClasses, argsArray, false)) {
                        candidateParamClasses = paramClasses;
                        candidate = meth;
                        break;
                    }
                    else {
                        if (MetaClass.isValidMethod(paramClasses, argsArray, true)){
                            candidateParamClasses = paramClasses;
                            candidate = meth;
                            break;
                        }
                    }
                }
            }
        }

        if (candidate != null && candidateParamClasses != null) {
            // let's synth a MetaMethod from the MethodNode
            try {
                return new MetaMethod(methName, null, candidateParamClasses, loadClass(candidate.getReturnType()), candidate.getModifiers());
            } catch (Exception e) {
                log.warning(e.getMessage());
                return null;
            }
        }
        else {
            // try super class
            Class superClass = null;
            try {
                superClass = loadClass(classNode.getSuperClass());
            }
            catch(Exception e) {
                // the super may be a groovy class that's not compiled yet
                log.warning(e.getMessage());
            }
            // should I filter out GroovyObject super class here?
            if (superClass != null ) {
                MetaMethod mmethod = MetaClassRegistry.getIntance(MetaClassRegistry.DONT_LOAD_DEFAULT).getDefinedMethod(superClass, methName, argsArray, isStaticCall);
                if (mmethod == null)
                    return null;
                int modies = mmethod.getModifiers();
                if (Modifier.isPrivate(modies)) {
                    return null;
                }
                else if(modies == 0) {
                    // match package
                    int pThis = classNode.getType().getName().lastIndexOf(".");
                    String packageNameThis = pThis > 0? classNode.getType().getName().substring(0, pThis) : "";

                    int pSuper = classNode.getSuperClass().getName().lastIndexOf(".");
                    String packageNameSuper = pSuper > 0? classNode.getSuperClass().getName().substring(0, pSuper) : "";
                    if (packageNameThis.equals(packageNameSuper)) {
                        return new MetaMethod(methName, null, mmethod.getParameterTypes(), mmethod.getReturnType(), mmethod.getModifiers());
                    }
                    else {
                        return null;
                    }
                }
                else {
                    // let changes the declaring class back to null (meaning "this"), so that proper class inheritance permission control is obeyed
                    return new MetaMethod(methName, null, mmethod.getParameterTypes(), mmethod.getReturnType(), mmethod.getModifiers());
                }
            }
            return null;
        }
    }

    // the fowllowing asXXX() methods are copied from the Invoker class, to avoid initilization of an invoker instance,
    // which has lots of baggage with it, notably the meta class stuff.
    private static Object asType(Object object, Class type) {
        if (object == null) {
            return null;
        }
        if (type.isInstance(object)) {
            return object;
        }
        if (type.equals(Type.STRING_TYPE)) {
            return object.toString();
        }
        if (type.equals(Type.Character_TYPE)) {
            if (object instanceof Number) {
                return asCharacter((Number) object);
            }
            else {
                String text = object.toString();
                if (text.length() == 1) {
                    return new Character(text.charAt(0));
                }
                else {
                    throw new ClassCastException("Cannot cast: " + text + " to a Character");
                }
            }
        }
        if (Number.class.isAssignableFrom(type)) {
            if (object instanceof Character) {
                return new Integer(((Character) object).charValue());
            }
            else if (object instanceof String) {
                String c = (String) object;
                if (c.length() == 1) {
                    return new Integer(c.charAt(0));
                }
                else {
                    throw new ClassCastException("Cannot cast: '" + c + "' to an Integer");
                }
            }
        }
        if (object instanceof Number) {
            Number n = (Number) object;
            if (type.isPrimitive()) {
                if (type == byte.class) {
                    return new Byte(n.byteValue());
                }
                if (type == char.class) {
                    return new Character((char) n.intValue());
                }
                if (type == short.class) {
                    return new Short(n.shortValue());
                }
                if (type == int.class) {
                    return new Integer(n.intValue());
                }
                if (type == long.class) {
                    return new Long(n.longValue());
                }
                if (type == float.class) {
                    return new Float(n.floatValue());
                }
                if (type == double.class) {
                    Double answer = new Double(n.doubleValue());
                    //throw a runtime exception if conversion would be out-of-range for the type.
                    if (!(n instanceof Double) && (answer.doubleValue() == Double.NEGATIVE_INFINITY
                            || answer.doubleValue() == Double.POSITIVE_INFINITY)) {
                        throw new  GroovyRuntimeException("Automatic coercion of "+n.getClass().getName()
                                +" value "+n+" to double failed.  Value is out of range.");
                    }
                    return answer;
                }
            }
            else {
                if (Number.class.isAssignableFrom(type)) {
                    if (type == Byte.class) {
                        return new Byte(n.byteValue());
                    }
                    if (type == Character.class) {
                        return new Character((char) n.intValue());
                    }
                    if (type == Short.class) {
                        return new Short(n.shortValue());
                    }
                    if (type == Integer.class) {
                        return new Integer(n.intValue());
                    }
                    if (type == Long.class) {
                        return new Long(n.longValue());
                    }
                    if (type == Float.class) {
                        return new Float(n.floatValue());
                    }
                    if (type == Double.class) {
                        Double answer = new Double(n.doubleValue());
                        //throw a runtime exception if conversion would be out-of-range for the type.
                        if (!(n instanceof Double) && (answer.doubleValue() == Double.NEGATIVE_INFINITY
                                || answer.doubleValue() == Double.POSITIVE_INFINITY)) {
                            throw new  GroovyRuntimeException("Automatic coercion of "+n.getClass().getName()
                                    +" value "+n+" to double failed.  Value is out of range.");
                        }
                        return answer;
                    }

                }
            }
        }
        if (type == Boolean.class) {
            return asBool(object) ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }

    public static boolean asBool(Object object) {
       if (object instanceof Boolean) {
            Boolean booleanValue = (Boolean) object;
            return booleanValue.booleanValue();
        }
        else if (object instanceof Matcher) {
            Matcher matcher = (Matcher) object;
            RegexSupport.setLastMatcher(matcher);
            return matcher.find();
        }
        else if (object instanceof Collection) {
            Collection collection = (Collection) object;
            return !collection.isEmpty();
        }
        else if (object instanceof Map) {
            Map map = (Map) object;
            return !map.isEmpty();
        }
        else if (object instanceof String) {
            String string = (String) object;
            return string.length() > 0;
        }
        else if (object instanceof Number) {
            Number n = (Number) object;
            return n.doubleValue() != 0;
        }
        else {
            return object != null;
        }
    }
    private static Character asCharacter(Number value) {
        return new Character((char) value.intValue());
    }

    private static Character asCharacter(String text) {
        return new Character(text.charAt(0));
    }
}

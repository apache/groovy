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

import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.codehaus.groovy.GroovyBugError;
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
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
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
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


/**
 * Generates Java class versions of Groovy classes using ASM.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:b55r@sina.com">Bing Ran</a>
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
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
    private boolean leftHandExpression=false;
    /**
     * Notes for leftHandExpression:
     * The default is false, that menas the right side is default.
     * The right side means that variables are read and not written.
     * Any change of leftHandExpression to true, should be made carefully.
     * If such a change is needed, then it should be set to false as soon as
     * possible, but most important in the same method. Setting 
     * leftHandExpression to false is needed for writing variables.
     */

    // method invocation
    MethodCallerMultiAdapter invokeMethodOnCurrent = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"invokeMethodOnCurrent",true,false);
    MethodCallerMultiAdapter invokeMethodOnSuper   = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"invokeMethodOnSuper",true,false);
    MethodCallerMultiAdapter invokeMethod          = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"invokeMethod",true,false);
    MethodCallerMultiAdapter invokeStaticMethod    = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"invokeStaticMethod",true,true);
    MethodCallerMultiAdapter invokeNew             = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"invokeNew",true,true);
    
    // fields & properties
    MethodCallerMultiAdapter setField             = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"setField",false,false);
    MethodCallerMultiAdapter getField             = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"getField",false,false);
    MethodCallerMultiAdapter setGroovyObjectField = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"setGroovyObjectField",false,false);
    MethodCallerMultiAdapter getGroovyObjectField = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"getGroovyObjectField",false,false);
    MethodCallerMultiAdapter setFieldOnSuper      = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"setFieldOnSuper",false,false);
    MethodCallerMultiAdapter getFieldOnSuper      = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"getFieldOnSuper",false,false);
    
    MethodCallerMultiAdapter setProperty             = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"setProperty",false,false);
    MethodCallerMultiAdapter getProperty             = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"getProperty",false,false);
    MethodCallerMultiAdapter setGroovyObjectProperty = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"setGroovyObjectProperty",false,false);
    MethodCallerMultiAdapter getGroovyObjectProperty = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"getGroovyObjectProperty",false,false);
    MethodCallerMultiAdapter setPropertyOnSuper      = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"setPropertyOnSuper",false,false);
    MethodCallerMultiAdapter getPropertyOnSuper      = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class,"getPropertyOnSuper",false,false);
    
    // iterator
    MethodCaller iteratorNextMethod = MethodCaller.newInterface(Iterator.class, "next");
    MethodCaller iteratorHasNextMethod = MethodCaller.newInterface(Iterator.class, "hasNext");
    // assert
    MethodCaller assertFailedMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "assertFailed");
    // isCase
    MethodCaller isCaseMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "isCase");
    //compare
    MethodCaller compareIdenticalMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareIdentical");
    MethodCaller compareEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareEqual");
    MethodCaller compareNotEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareNotEqual");
    MethodCaller compareToMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareTo");
    MethodCaller compareLessThanMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareLessThan");
    MethodCaller compareLessThanEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareLessThanEqual");
    MethodCaller compareGreaterThanMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareGreaterThan");
    MethodCaller compareGreaterThanEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareGreaterThanEqual");
    //regexpr
    MethodCaller findRegexMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "findRegex");
    MethodCaller matchRegexMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "matchRegex");
    MethodCaller regexPattern = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "regexPattern");
    // spread expressions
    MethodCaller spreadMap = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "spreadMap");
    MethodCaller despreadList = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "despreadList");
    // Closure
    MethodCaller getMethodPointer = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getMethodPointer");
    MethodCaller invokeClosureMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeClosure");
    //negation
    MethodCaller negation = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "negate");
    MethodCaller bitNegation = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "bitNegate");

    // type converions
    MethodCaller asTypeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "asType");
    MethodCaller castToTypeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "castToType");
    MethodCaller createListMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createList");
    MethodCaller createTupleMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createTuple");
    MethodCaller createMapMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createMap");
    MethodCaller createRangeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createRange");
    
    // wrapper creation methods
    MethodCaller createPojoWrapperMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createPojoWrapper");
    MethodCaller createGroovyObjectWrapperMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createGroovyObjectWrapper");

    // constructor calls with this() and super()
    MethodCaller selectConstructorAndTransformArguments = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "selectConstructorAndTransformArguments");
 
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

    public static final boolean ASM_DEBUG = false; // add marker in the bytecode to show source-byecode relationship
    private int lineNumber = -1;
    private int columnNumber = -1;
    private ASTNode currentASTNode = null;

    private DummyClassGenerator dummyGen = null;
    private ClassWriter dummyClassWriter = null;
    
    private ClassNode interfaceClassLoadingClass;

    private boolean implicitThis = false;

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
                getBytecodeVersion(),
                classNode.getModifiers(),
                internalClassName,
                null,
                internalBaseClassName,
                BytecodeHelper.getClassInternalNames(classNode.getInterfaces())
            );            
            cw.visitSource(sourceFile,null);
            visitAnnotations(classNode, cw);
            
            if (classNode.isInterface()) {
                ClassNode owner = classNode;
                if (owner instanceof InnerClassNode) {
                    owner = owner.getOuterClass();
                }
                String outerClassName = owner.getName();
                String name = outerClassName + "$" + context.getNextInnerClassIdx();
                interfaceClassLoadingClass = new InnerClassNode(owner, name, 4128, ClassHelper.OBJECT_TYPE);
                
                super.visitClass(classNode);
                createInterfaceSyntheticStaticFields();                
            } else {
                super.visitClass(classNode);
                createMopMethods();
                createSyntheticStaticFields();
            }
            
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
            //TODO: an inner class should have an entry of itself
            cw.visitEnd();
        }
        catch (GroovyRuntimeException e) {
            e.setModule(classNode.getModule());
            throw e;
        }
    }
   
    private void createMopMethods() {
        visitMopMethodList(classNode.getMethods(), true);
        visitMopMethodList(classNode.getSuperClass().getAllDeclaredMethods(), false);
    }

    private String[] buildExceptions(ClassNode[] exceptions) {
        if (exceptions==null) return null;
        String[] ret = new String[exceptions.length];
        for (int i = 0; i < exceptions.length; i++) {
            ret[i] = BytecodeHelper.getClassInternalName(exceptions[i]);
        }
        return ret;
    }
    
    /**
     * filters a list of method for MOP methods. For all methods that are no 
     * MOP methods a MOP method is created if the method is not public and the
     * call would be a call on "this" (isThis == true). If the call is not on
     * "this", then the call is a call on "super" and all methods are used, 
     * unless they are already a MOP method
     *  
     * @see #generateMopCalls(LinkedList, boolean)
     *  
     * @param methods unfiltered list of methods for MOP 
     * @param isThis  if true, then we are creating a MOP method on "this", "super" else 
     */
    private void visitMopMethodList(List methods, boolean isThis){
        LinkedList mopCalls = new LinkedList();
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode mn = (MethodNode) iter.next();
            if ((mn.getModifiers() & ACC_ABSTRACT) !=0 ) continue;
            // no this$ methods for protected/public isThis=true
            // super$ method for protected/public isThis=false
            // --> results in XOR
            if (isThis ^ (mn.getModifiers() & (ACC_PUBLIC|ACC_PROTECTED)) == 0) continue; 
            String methodName = mn.getName();
            if (isMopMethod(methodName) || methodName.startsWith("<")) continue;
            String name = getMopMethodName(mn,isThis);
            if (containsMethod(methods,name,mn.getParameters())) continue;
            mopCalls.add(mn);
        }
        generateMopCalls(mopCalls, isThis);
        mopCalls.clear();
    }
    
    private boolean containsMethod(List methods, String name, Parameter[] paras) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode element = (MethodNode) iter.next();
            if (element.getName().equals(name) && equalParameterTypes(paras,element.getParameters())) return true;
        }
        return false;
    }
    
    private boolean equalParameterTypes(Parameter[] p1, Parameter[] p2) {
        if (p1.length!=p2.length) return false;
        for (int i=0; i<p1.length; i++) {
            if (!p1[i].getType().equals(p2[i].getType())) return false;
        }
        return true;
    }
    
    /**
     * generates a Meta Object Protocoll method, that is used to call a non public
     * method, or to make a call to super.
     * @param mopCalls list of methods a mop call method should be generated for
     * @param useThis true if "this" should be used for the naming
     */
    private void generateMopCalls(LinkedList mopCalls, boolean useThis) {
        for (Iterator iter = mopCalls.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            String name = getMopMethodName(method,useThis);
            Parameter[] parameters = method.getParameters();
            String methodDescriptor = BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameters());
            cv = cw.visitMethod(Opcodes.ACC_PUBLIC & Opcodes.ACC_SYNTHETIC, name, methodDescriptor, null, null);
            cv.visitVarInsn(ALOAD,0);
            BytecodeHelper helper = new BytecodeHelper(cv);
            int newRegister = 1;
            for (int i=0; i<parameters.length; i++) {
                ClassNode type = parameters[i].getType();
                helper.load(parameters[i].getType(),newRegister);
                // increment to next register, double/long are using two places
                newRegister++;
                if (type == ClassHelper.double_TYPE || type == ClassHelper.long_TYPE) newRegister++;
            }
            cv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(method.getDeclaringClass()), method.getName(), methodDescriptor); 
            helper.doReturn(method.getReturnType());
            cv.visitMaxs(0, 0);
            cv.visitEnd();
            classNode.addMethod(name,Opcodes.ACC_PUBLIC & Opcodes.ACC_SYNTHETIC,method.getReturnType(),parameters,null,null);
        }
    }

    /**
     * creates a MOP method name from a method
     * @param method the method to be called by the mop method
     * @param useThis if true, then it is a call on "this", "super" else
     * @return the mop method name
     */
    public static String getMopMethodName(MethodNode method, boolean useThis) {
        ClassNode declaringNode = method.getDeclaringClass();
        int distance = 0;
        for (;declaringNode!=null; declaringNode=declaringNode.getSuperClass()) {
            distance++;
        }
        return (useThis?"this":"super")+"$"+distance+"$"+method.getName();
    }
   
    /**
     * method to determine if a method is a MOP method. This is done by the
     * method name. If the name starts with "this$" or "super$", then it is
     * a MOP method
     * @param methodName name of the method to test
     * @return true if the method is a MOP method
     */
    public static boolean isMopMethod(String methodName) {
        return  methodName.startsWith("this$") || 
                methodName.startsWith("super$");
    }
    
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        String methodType = BytecodeHelper.getMethodDescriptor(node.getReturnType(), node.getParameters());

        cv = cw.visitMethod(node.getModifiers(), node.getName(), methodType, null, buildExceptions(node.getExceptions()));
        visitAnnotations(node, cv);
        helper = new BytecodeHelper(cv);
        if (!node.isAbstract()) { 
            Statement code = node.getCode();
            if (isConstructor && (code == null || !firstStatementIsSpecialConstructorCall(node))) {
                // invokes the super class constructor
                cv.visitVarInsn(ALOAD, 0);
                cv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(classNode.getSuperClass()), "<init>", "()V");
            }
            
            compileStack.init(node.getVariableScope(),node.getParameters(),cv, classNode);
            
            // ensure we save the current (meta) class in a register
            (new ClassExpression(classNode)).visit(this);
            cv.visitInsn(POP);
            (new ClassExpression(ClassHelper.METACLASS_TYPE)).visit(this);
            cv.visitInsn(POP);
            
            // handle body
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
        cv.visitEnd();
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
        FieldVisitor fv = cw.visitField(
            fieldNode.getModifiers(),
            fieldNode.getName(),
            BytecodeHelper.getTypeDescription(t),
            null, //fieldValue,  //br  all the sudden that one cannot init the field here. init is done in static initilizer and instace intializer.
            null);
        visitAnnotations(fieldNode, fv);
        fv.visitEnd();
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
        // Then get the iterator and generate the loop control
        MethodCallExpression iterator = new MethodCallExpression(loop.getCollectionExpression(),"iterator",new ArgumentListExpression());
        iterator.visit(this);

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
        
        // if-else is here handled as a special version
        // of a booelan expression
        compileStack.pushBooleanExpression();
        ifElse.getIfBlock().visit(this);
        compileStack.pop();

        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);

        compileStack.pushBooleanExpression();
        ifElse.getElseBlock().visit(this);
        compileStack.pop();
        
        cv.visitLabel(l1);
    }

    public void visitTernaryExpression(TernaryExpression expression) {
        onLineNumber(expression, "visitTernaryExpression");

        expression.getBooleanExpression().visit(this);

        Label l0 = new Label();
        cv.visitJumpInsn(IFEQ, l0);
        visitAndAutoboxBoolean(expression.getTrueExpression());

        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);

        visitAndAutoboxBoolean(expression.getFalseExpression());
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
        
        CatchStatement catchStatement = statement.getCatchStatement(0);
        Statement tryStatement = statement.getTryStatement();
        final Statement finallyStatement = statement.getFinallyStatement();

        int anyExceptionIndex = compileStack.defineTemporaryVariable("exception",false);
        if (!finallyStatement.isEmpty()) {
            compileStack.pushFinallyBlock(
                new Runnable(){
                    public void run(){finallyStatement.visit(AsmClassGenerator.this);}
                }
            );
        }
        
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
        
        // remove the finally, don't let it visit itself
        if (!finallyStatement.isEmpty()) compileStack.popFinallyBlock();
        
        // start finally
        cv.visitLabel(finallyStart);
        finallyStatement.visit(this);
        // goto end of finally
        Label afterFinally = new Label();
        cv.visitJumpInsn(GOTO, afterFinally);
        
        // start a block catching any Exception
        final Label catchAny = new Label();
        cv.visitLabel(catchAny);
        //store exception
        cv.visitVarInsn(ASTORE, anyExceptionIndex);
        finallyStatement.visit(this);
        // load the exception and rethrow it
        cv.visitVarInsn(ALOAD, anyExceptionIndex);
        cv.visitInsn(ATHROW);
        
        // end of all catches and finally parts
        cv.visitLabel(afterFinally);
        
        // add catch any block to exception table
        exceptionBlocks.add(new Runnable() {
            public void run() {
                cv.visitTryCatchBlock(tryStart, endOfAllCatches, catchAny, null);
            }
        });
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
        Label breakLabel = compileStack.getNamedBreakLabel(name);
        compileStack.applyFinallyBlocks(breakLabel, true);
        
        cv.visitJumpInsn(GOTO, breakLabel);
    }

    public void visitContinueStatement(ContinueStatement statement) {
        onLineNumber(statement, "visitContinueStatement");
        visitStatement(statement);
        
        String name = statement.getLabel();
        Label continueLabel = compileStack.getContinueLabel();
        if (name!=null) continueLabel = compileStack.getNamedContinueLabel(name);
        compileStack.applyFinallyBlocks(continueLabel, false);
        cv.visitJumpInsn(GOTO, continueLabel);
    }

    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        onLineNumber(statement, "visitSynchronizedStatement");
        visitStatement(statement);
        
        statement.getExpression().visit(this);
        final int index = compileStack.defineTemporaryVariable("synchronized", ClassHelper.Integer_TYPE,true);

        final Label synchronizedStart = new Label();
        final Label synchronizedEnd = new Label();
        final Label catchAll = new Label();
        
        cv.visitVarInsn(ALOAD, index);
        cv.visitInsn(MONITORENTER);
        cv.visitLabel(synchronizedStart);

        Runnable finallyPart = new Runnable(){
            public void run(){
                cv.visitVarInsn(ALOAD, index);
                cv.visitInsn(MONITOREXIT);
            }
        };
        compileStack.pushFinallyBlock(finallyPart);
        statement.getCode().visit(this);

        finallyPart.run();
        cv.visitJumpInsn(GOTO, synchronizedEnd);
        cv.visitLabel(catchAll);
        finallyPart.run();
        cv.visitInsn(ATHROW);
        cv.visitLabel(synchronizedEnd);

        compileStack.popFinallyBlock();
        exceptionBlocks.add(new Runnable() {
            public void run() {
                cv.visitTryCatchBlock(synchronizedStart, catchAll, catchAll, null);
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
        
        ClassNode returnType;
        if (methodNode!=null) {
            returnType = methodNode.getReturnType();
        } else if (constructorNode!=null){
            returnType = constructorNode.getReturnType();
        } else {
            throw new GroovyBugError("I spotted a return that is neither in a method nor in a constructor... I can not handle that");
        }
        
        if (returnType==ClassHelper.VOID_TYPE) {
        	if (!(statement == ReturnStatement.RETURN_NULL_OR_VOID)) {
                throwException("Cannot use return statement with an expression on a method that returns void");
        	}
            compileStack.applyFinallyBlocks();
            cv.visitInsn(RETURN);
            outputReturn = true;
            return;
        }

        Expression expression = statement.getExpression();
        evaluateExpression(expression);
        if (returnType==ClassHelper.OBJECT_TYPE && expression.getType() != null && expression.getType()==ClassHelper.VOID_TYPE) {
            cv.visitInsn(ACONST_NULL); // cheat the caller
        } else {
            // return is based on class type
            // we may need to cast
            doConvertAndCast(returnType, expression, false, true, false);
        }
        if (compileStack.hasFinallyBlocks()) {
            // value is always saved in boxed form, so no need to have a special load routine here
            int returnValueIdx = compileStack.defineTemporaryVariable("returnValue",ClassHelper.OBJECT_TYPE,true);
            compileStack.applyFinallyBlocks();
            helper.load(ClassHelper.OBJECT_TYPE,returnValueIdx);
        }
        // value is always saved in boxed form, so we need to unbox it here        
        helper.unbox(returnType);
        helper.doReturn(returnType);
        outputReturn = true;
    }

    /**
     * Casts to the given type unless it can be determined that the cast is unnecessary
     */
    protected void doConvertAndCast(ClassNode type, Expression expression, boolean ignoreAutoboxing, boolean forceCast, boolean coerce) {
        ClassNode expType = getExpressionType(expression);
        // temp resolution: convert all primitive casting to corresponsing Object type
        if (!ignoreAutoboxing && ClassHelper.isPrimitiveType(type)) {
            type = ClassHelper.getWrapper(type);
        }
        if (forceCast || (type!=null && !type.equals(expType))) {
            doConvertAndCast(type,coerce);
        }
    }    

    /**
     * @param expression
     */
    protected void evaluateExpression(Expression expression) {
        visitAndAutoboxBoolean(expression);

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
             
            case Types.KEYWORD_IN :
                evaluateBinaryExpression(isCaseMethod, expression);
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

        passingClosureParams = true;
        List constructors = innerClass.getDeclaredConstructors();
        ConstructorNode node = (ConstructorNode) constructors.get(0);
        
        Parameter[] localVariableParams = node.getParameters();

        cv.visitTypeInsn(NEW, innerClassinternalName);
        cv.visitInsn(DUP);
        if (isStaticMethod() || classNode.isStaticClass()) {
            visitClassExpression(new ClassExpression(classNode));
            visitClassExpression(new ClassExpression(getOutermostClass()));
        } else {
            cv.visitVarInsn(ALOAD, 0);
            loadThis();
        }

        // now lets load the various parameters we're passing
        // we start at index 1 because the first variable we pass
        // is the owner instance and at this point it is already 
        // on the stack
        for (int i = 2; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String name = param.getName();

            // compileStack.containsVariable(name) means to ask if the variable is already declared
            // compileStack.getScope().isReferencedClassVariable(name) means to ask if the variable is a field
            // If it is no field and is not yet declared, then it is either a closure shared variable or 
            // an already declared variable. 
            if (!compileStack.containsVariable(name) && compileStack.getScope().isReferencedClassVariable(name)) {
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
        } else {
            loadThis();
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
        throw new GroovyBugError("SpreadExpression should not be visited here");
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
        doConvertAndCast(type, expression.getExpression(), expression.isIgnoringAutoboxing(),false,expression.isCoerce());
    }

    public void visitNotExpression(NotExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(this);
        // if we do !object, then the cast to boolean will
        // do the conversion of Object to boolean. so a simple
        // call to unbox is enough here.
        if (
                !isComparisonExpression(subExpression) && 
                !(subExpression instanceof BooleanExpression))
        {
            helper.unbox(boolean.class);
        }
        helper.negateBoolean();
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
                helper.unbox(boolean.class); // to return a primitive boolean
//            }
        }
        compileStack.pop();
    }
    
    private void makeInvokeMethodCall(MethodCallExpression call, boolean useSuper, MethodCallerMultiAdapter adapter) {
        // receiver
        // we operate on GroovyObject if possible
        Expression objectExpression = call.getObjectExpression();
        if (!isStaticMethod() && !isStaticContext() && isThisExpression(call.getObjectExpression())) 
        {
            objectExpression = new CastExpression(ClassHelper.make(GroovyObject.class),objectExpression);
        }
        // message name
        Expression messageName = new CastExpression(ClassHelper.STRING_TYPE,call.getMethod());
        if (useSuper) {
            makeCall(new ClassExpression(getOutermostClass().getSuperClass()),
                    objectExpression, messageName,
                    call.getArguments(), adapter,
                    call.isSafe(), call.isSpreadSafe(), 
                    false
            );
        } else {
            makeCall(objectExpression, messageName,
                    call.getArguments(), adapter,
                    call.isSafe(), call.isSpreadSafe(), 
                    call.isImplicitThis()
            );
        }
    }
    
    private void makeCall( 
            Expression receiver, Expression message, Expression arguments, 
            MethodCallerMultiAdapter adapter, 
            boolean safe, boolean spreadSafe, boolean implicitThis
    ) {
        ClassNode cn = classNode;
        if (isInClosure() && !implicitThis) {
            cn = getOutermostClass();
        }
        makeCall(new ClassExpression(cn), receiver, message, arguments,
                adapter, safe, spreadSafe, implicitThis);
    }
    
    private void makeCall( 
            ClassExpression sender,
            Expression receiver, Expression message, Expression arguments, 
            MethodCallerMultiAdapter adapter, 
            boolean safe, boolean spreadSafe, boolean implicitThis
    ) {
        // ensure VariableArguments are read, not stored
        boolean lhs = leftHandExpression;
        leftHandExpression = false;
        
        // sender
        sender.visit(this);
        // receiver
        boolean oldVal = this.implicitThis;
        this.implicitThis = implicitThis;
        visitAndAutoboxBoolean(receiver);
        this.implicitThis = oldVal;
        // message
        if (message!=null) message.visit(this);

        // arguments
        boolean containsSpreadExpression = containsSpreadExpression(arguments);
        int numberOfArguments = containsSpreadExpression?-1:argumentSize(arguments);
        if (numberOfArguments > adapter.maxArgs || containsSpreadExpression) {
            ArgumentListExpression ae;
            if (arguments instanceof ArgumentListExpression) {
                ae = (ArgumentListExpression) arguments;
            } else if (arguments instanceof TupleExpression){
                TupleExpression te = (TupleExpression) arguments;
                ae = new ArgumentListExpression(te.getExpressions());
            } else {
                ae = new ArgumentListExpression();
                ae.addExpression(arguments);
            }
            if (containsSpreadExpression){
                despreadList(ae.getExpressions(),true);
            } else {
                ae.visit(this);
            }
        } else if (numberOfArguments > 0) {
            TupleExpression te = (TupleExpression) arguments;
            for (int i = 0; i < numberOfArguments; i++) {
                Expression argument = te.getExpression(i);
                visitAndAutoboxBoolean(argument);
                if (argument instanceof CastExpression) loadWrapper(argument);
            }
        }
                
        adapter.call(cv,numberOfArguments,safe,spreadSafe);
        
        leftHandExpression = lhs;
    }

    private void despreadList(List expressions, boolean wrap) {
        
        ArrayList spreadIndexes = new ArrayList();
        ArrayList spreadExpressions = new ArrayList();
        ArrayList normalArguments = new ArrayList();
        for (int i=0; i<expressions.size(); i++) {
            Object expr = expressions.get(i);
            if ( !(expr instanceof SpreadExpression) ) {
                normalArguments.add(expr);
            } else {
                spreadIndexes.add(new ConstantExpression(new Integer(i-spreadExpressions.size())));
                spreadExpressions.add(((SpreadExpression)expr).getExpression());                
            }
        }

        //load normal arguments as array
        visitTupleExpression(new ArgumentListExpression(normalArguments),wrap);
        //load spread expressions as array
        (new TupleExpression(spreadExpressions)).visit(this);
        //load insertion index
        (new ArrayExpression(ClassHelper.int_TYPE,spreadIndexes,null)).visit(this);
        despreadList.call(cv);
    }

    public void visitMethodCallExpression(MethodCallExpression call) {
        onLineNumber(call, "visitMethodCallExpression: \"" + call.getMethod() + "\":");

        Expression arguments = call.getArguments();
        String methodName = call.getMethodAsString();
        boolean isSuperMethodCall = usesSuper(call);
        boolean isThisExpression = isThisExpression(call.getObjectExpression());
        
        // are we a local variable
        if (methodName!=null && isThisExpression && isFieldOrVariable(methodName) && ! classNode.hasPossibleMethod(methodName, arguments)) {
            // lets invoke the closure method
            visitVariableExpression(new VariableExpression(methodName));
            arguments.visit(this);
            invokeClosureMethod.call(cv);
        } else {
            MethodCallerMultiAdapter adapter = invokeMethod;
            if (isThisExpression) adapter = invokeMethodOnCurrent;
            if (isSuperMethodCall) adapter = invokeMethodOnSuper;
            if (isStaticInvocation(call)) adapter = invokeStaticMethod;
            makeInvokeMethodCall(call,isSuperMethodCall,adapter);
        }
    }

    private boolean isStaticInvocation(MethodCallExpression call) {
        if (!isThisExpression(call.getObjectExpression())) return false;
        if (isStaticMethod()) return true;
        return isStaticContext() && !call.isImplicitThis();
    }

    protected boolean emptyArguments(Expression arguments) {
        return argumentSize(arguments) == 0;
    }
    
    protected static boolean containsSpreadExpression(Expression arguments) {
        List args = null;
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            args = tupleExpression.getExpressions();
        } else if (arguments instanceof ListExpression) {
            ListExpression le = (ListExpression) arguments;
            args = le.getExpressions();
        } else {
            return arguments instanceof SpreadExpression;
        }
        for (Iterator iter = args.iterator(); iter.hasNext();) {
            if (iter.next() instanceof SpreadExpression) return true;
        }
        return false;
    }
    
    protected static int argumentSize(Expression arguments) {
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            int size = tupleExpression.getExpressions().size();
            return size;
        }
        return 1;
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        onLineNumber(call, "visitStaticMethodCallExpression: \"" + call.getMethod() + "\":");

        makeCall(
                new ClassExpression(call.getOwnerType()),
                new ConstantExpression(call.getMethod()),
                call.getArguments(),
                invokeStaticMethod,
                false,false,false);
    }
    
    private void visitSpecialConstructorCall(ConstructorCallExpression call) {
        ClassNode callNode = classNode;
        if (call.isSuperCall()) callNode = callNode.getSuperClass();
        List constructors = sortConstructors(call, callNode);
        call.getArguments().visit(this);
        // keep Objet[] on stack
        cv.visitInsn(DUP);
        // to select the constructor we need also the number of
        // available constructors and the class we want to make
        // the call on
        helper.pushConstant(constructors.size());
        visitClassExpression(new ClassExpression(callNode));
        // removes one Object[] leaves the int containing the 
        // call flags and the construtcor number
        selectConstructorAndTransformArguments.call(cv);
        // Object[],int -> int,Object[],int
        // we need to examine the flags and maybe change the 
        // Object[] later, so this reordering will do the job
        cv.visitInsn(DUP_X1);
        // test if rewrap flag is set
        cv.visitInsn(ICONST_1);
        cv.visitInsn(IAND);
        Label afterIf = new Label();
        cv.visitJumpInsn(IFEQ, afterIf);
        // true part, so rewrap using the first argument
        cv.visitInsn(ICONST_0);
        cv.visitInsn(AALOAD);
        cv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
        cv.visitLabel(afterIf);
        // here the stack is int,Object[], but we need the
        // the int for our table, so swap it
        cv.visitInsn(SWAP);
        //load "this"
        cv.visitVarInsn(ALOAD, 0);
        cv.visitInsn(SWAP);
        //prepare switch with >>8        
        cv.visitIntInsn(BIPUSH,8);
        cv.visitInsn(ISHR);
        Label[] targets = new Label[constructors.size()];
        int[] indices = new int[constructors.size()];
        for (int i=0; i<targets.length; i++) {
            targets[i] = new Label();
            indices[i] = i;
        }
        // create switch targets
        Label defaultLabel = new Label();
        Label afterSwitch = new Label();
        cv.visitLookupSwitchInsn(defaultLabel, indices, targets);
        for (int i=0; i<targets.length; i++) {
            cv.visitLabel(targets[i]);
            // to keep the stack height, we need to leave
            // one Object[] on the stack as last element. At the 
            // same time, we need the Object[] on top of the stack
            // to extract the parameters. So a SWAP will exchange 
            // "this" and Object[], a DUP_X1 will then copy the Object[]
            /// to the last place in the stack: 
            //     Object[],this -SWAP-> this,Object[]
            //     this,Object[] -DUP_X1-> Object[],this,Object[] 
            cv.visitInsn(SWAP);
            cv.visitInsn(DUP_X1);
            
            ConstructorNode cn = (ConstructorNode) constructors.get(i);
            String descriptor = helper.getMethodDescriptor(ClassHelper.VOID_TYPE, cn.getParameters());
            // unwrap the Object[] and make transformations if needed
            // that means, to duplicate the Object[], make a cast with possible
            // unboxing and then swap it with the Object[] for each parameter
            Parameter[] parameters = cn.getParameters();
            for (int p=0; p<parameters.length; p++) {
                cv.visitInsn(DUP);
                helper.pushConstant(p);
                cv.visitInsn(AALOAD);
                ClassNode type = parameters[p].getType();
                if (ClassHelper.isPrimitiveType(type)) {
                    helper.unbox(type);
                } else {
                    helper.doCast(type);
                }
                helper.swapWithObject(type);
            }
            // at the end we remove the Object[]
            cv.visitInsn(POP);
            // make the constructor call
            cv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(callNode), "<init>", descriptor);
            cv.visitJumpInsn(GOTO, afterSwitch);
        }
        cv.visitLabel(defaultLabel);
        // this part should never be reached!
        cv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        cv.visitInsn(DUP);
        cv.visitLdcInsn("illegal constructor number");
        cv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
        cv.visitInsn(ATHROW);
        cv.visitLabel(afterSwitch);
    }

    private List sortConstructors(ConstructorCallExpression call, ClassNode callNode) {
        // sort in a new list to prevent side effects
        List constructors = new ArrayList(callNode.getDeclaredConstructors());
        Comparator comp = new Comparator() {
            public int compare(Object arg0, Object arg1) {
                ConstructorNode c0 = (ConstructorNode) arg0;
                ConstructorNode c1 = (ConstructorNode) arg1;
                String descriptor0 = helper.getMethodDescriptor(ClassHelper.VOID_TYPE, c0.getParameters()); 
                String descriptor1 = helper.getMethodDescriptor(ClassHelper.VOID_TYPE, c1.getParameters());
                return descriptor0.compareTo(descriptor1);
            }            
        };
        Collections.sort(constructors,comp);
        return constructors;
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        onLineNumber(call, "visitConstructorCallExpression: \"" + call.getType().getName() + "\":");

        if (call.isSpecialCall()){
            visitSpecialConstructorCall(call);
            return;
        }
        
        Expression arguments = call.getArguments();
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            int size = tupleExpression.getExpressions().size();
            if (size == 0) {
                arguments = MethodCallExpression.NO_ARGUMENTS;
            }
        }
        
        Expression receiverClass = new ClassExpression(call.getType());
        makeCall(
                receiverClass, null,
                arguments,
                invokeNew, false, false, false
        );
    }
    
    private static String makeFieldClassName(ClassNode type) {
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
        String name = prefix+"class$" + makeFieldClassName(componentType);
        return name;
    }
    
    private void visitAttributeOrProperty(PropertyExpression expression, MethodCallerMultiAdapter adapter) {
        Expression objectExpression = expression.getObjectExpression();
        if (isThisExpression(objectExpression)) {
            // lets use the field expression if its available
            String name = expression.getPropertyAsString();
            if (name!=null) {
                FieldNode field = classNode.getField(name);
                if (field != null) {
                    visitFieldExpression(new FieldExpression(field));
                    return;
                }
            }
        }  

        // arguments already on stack if any
        makeCall( 
                objectExpression, // receiver
                new CastExpression(ClassHelper.STRING_TYPE, expression.getProperty()), // messageName
                MethodCallExpression.NO_ARGUMENTS,
                adapter,
                expression.isSafe(), expression.isSpreadSafe(), expression.isImplicitThis()
        );
    }
    
    private boolean isStaticContext(){
        if (!isInClosure()) return false;
        if (constructorNode != null) return false;
        return classNode.isStaticClass() || methodNode.isStatic();
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        Expression objectExpression = expression.getObjectExpression();
        MethodCallerMultiAdapter adapter;
        if (leftHandExpression) {
            adapter = setProperty;
            if (isGroovyObject(objectExpression)) adapter = setGroovyObjectProperty;
            if (isStaticContext() && isThisOrSuper(objectExpression)) adapter = setProperty;
        } else {
            adapter = getProperty;
            if (isGroovyObject(objectExpression)) adapter = getGroovyObjectProperty;
            if (isStaticContext() && isThisOrSuper(objectExpression)) adapter = getProperty;
        }
        visitAttributeOrProperty(expression,adapter);
    }        
    
    public void visitAttributeExpression(AttributeExpression expression) {
        Expression objectExpression = expression.getObjectExpression();
        MethodCallerMultiAdapter adapter;
        if (leftHandExpression) {
            adapter = setField;
            if (isGroovyObject(objectExpression)) adapter = setGroovyObjectField;
            if (usesSuper(expression)) adapter = getFieldOnSuper;
        } else {
            adapter = getField;
            if (isGroovyObject(objectExpression)) adapter = getGroovyObjectField;
            if (usesSuper(expression)) adapter = getFieldOnSuper;
        }
        visitAttributeOrProperty(expression,adapter);
    }

    protected boolean isGroovyObject(Expression objectExpression) {
        return isThisExpression(objectExpression);
    }

    public void visitFieldExpression(FieldExpression expression) {
        FieldNode field = expression.getField();

	    if (field.isStatic()) {
        	if (leftHandExpression) {
        		storeStaticField(expression);
        	}else {
        		loadStaticField(expression);
        	}
        } else {
        	if (leftHandExpression) {
        		storeThisInstanceField(expression);
        	} else {
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
            } else if (!ClassHelper.isPrimitiveType(type)){
                doConvertAndCast(type);
            }
            cv.visitVarInsn(ALOAD, 0);
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
        } else {
            helper.doCast(type);
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

        ClassNode classNode = this.classNode;
        if (isInClosure()) classNode = getOutermostClass();
        
        if (variableName.equals("this")) {
            if (isStaticMethod() || (!implicitThis && isStaticContext())) {
                visitClassExpression(new ClassExpression(classNode));
            } else {
                loadThis();
            }
            return;
        }

        //
        // "super" also requires special handling

        if (variableName.equals("super")) {
            if (isStaticMethod()) {
                visitClassExpression(new ClassExpression(classNode.getSuperClass()));
            } else {
                loadThis();
            }
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

    private void loadThis() {
        cv.visitVarInsn(ALOAD, 0);
        if (!implicitThis  && isInClosure()) {
            cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "groovy/lang/Closure",
                    "getThisObject",
                    "()Ljava/lang/Object;"
            );
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
            PropertyExpression pexp = new PropertyExpression(VariableExpression.THIS_EXPRESSION, name);
            pexp.setImplicitThis(true);
            visitPropertyExpression(pexp);
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
                return true;
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
    
    protected void createInterfaceSyntheticStaticFields() {
        if (syntheticStaticFields.isEmpty()) return;

        addInnerClass(interfaceClassLoadingClass);
        
        for (Iterator iter = syntheticStaticFields.iterator(); iter.hasNext();) {
            String staticFieldName = (String) iter.next();
            // generate a field node
            interfaceClassLoadingClass.addField(staticFieldName,ACC_STATIC + ACC_SYNTHETIC,ClassHelper.CLASS_Type,null);
        }
    }
    
    protected void createSyntheticStaticFields() {
        for (Iterator iter = syntheticStaticFields.iterator(); iter.hasNext();) {
            String staticFieldName = (String) iter.next();
            // generate a field node
            FieldNode fn = classNode.getField(staticFieldName);
            if (fn!=null) {
                boolean type = fn.getType()==ClassHelper.CLASS_Type;
                boolean modifiers = fn.getModifiers() == ACC_STATIC + ACC_SYNTHETIC;
                if (type && modifiers) continue;
                String text = "";
                if (!type) text = " with wrong type: "+fn.getType()+" (java.lang.Class needed)";
                if (!modifiers) text = " with wrong modifiers: "+fn.getModifiers()+" ("+(ACC_STATIC + ACC_SYNTHETIC)+" needed)";
                throwException(
                        "tried to set a static syntethic field "+staticFieldName+" in "+classNode.getName()+
                        " for class resolving, but found alreeady a node of that"+
                        " name "+text);
            } else {
                cw.visitField(ACC_STATIC + ACC_SYNTHETIC, staticFieldName, "Ljava/lang/Class;", null, null);
            }
        }

        cv =
            cw.visitMethod(
                    ACC_STATIC + ACC_SYNTHETIC,
                    "class$",
                    "(Ljava/lang/String;)Ljava/lang/Class;",
                    null,
                    null);
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
    }

    /** load class object on stack */
    public void visitClassExpression(ClassExpression expression) {
        ClassNode type = expression.getType();

        if (ClassHelper.isPrimitiveType(type)) {
            ClassNode objectType = ClassHelper.getWrapper(type);
            cv.visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(objectType), "TYPE", "Ljava/lang/Class;");          
        } else {
            String staticFieldName;
            if (type.equals(classNode)) {
                staticFieldName = "class$0";
                if (compileStack.getCurrentClassIndex()!=-1) {
                    cv.visitVarInsn(ALOAD,compileStack.getCurrentClassIndex());
                    return;
                } 
            } else if (type.equals(ClassHelper.METACLASS_TYPE)) {
                staticFieldName = getStaticFieldName(type);
                if (compileStack.getCurrentMetaClassIndex()!=-1) {
                    cv.visitVarInsn(ALOAD,compileStack.getCurrentMetaClassIndex());
                    return;
                }
            } else {
                staticFieldName = getStaticFieldName(type);
            }
            
            syntheticStaticFields.add(staticFieldName);

            String internalClassName = this.internalClassName;
            if (classNode.isInterface()) {
                internalClassName = BytecodeHelper.getClassInternalName(interfaceClassLoadingClass);
            }
            
            cv.visitFieldInsn(GETSTATIC, internalClassName, staticFieldName, "Ljava/lang/Class;");
            
            Label l0 = new Label();
            cv.visitJumpInsn(IFNONNULL, l0);
            cv.visitLdcInsn(BytecodeHelper.getClassLoadingTypeDescription(type));
            cv.visitMethodInsn(INVOKESTATIC, internalClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
            cv.visitInsn(DUP);
            cv.visitFieldInsn(PUTSTATIC, internalClassName, staticFieldName, "Ljava/lang/Class;");
            Label l1 = new Label();
            cv.visitJumpInsn(GOTO, l1);
            cv.visitLabel(l0);
            cv.visitFieldInsn(GETSTATIC, internalClassName, staticFieldName, "Ljava/lang/Class;");
            cv.visitLabel(l1);
            
            if (type.equals(classNode)) {
                cv.visitInsn(DUP);
                int index = compileStack.defineTemporaryVariable("class$0",ClassHelper.CLASS_Type,true);
                compileStack.setCurrentClassIndex(index);
            } else if (type.equals(ClassHelper.METACLASS_TYPE)) {
                cv.visitInsn(DUP);
                int index = compileStack.defineTemporaryVariable("meta$class$0",ClassHelper.CLASS_Type,true);
                compileStack.setCurrentMetaClassIndex(index);
            }
        }
    }

    public void visitRangeExpression(RangeExpression expression) {
        expression.getFrom().visit(this);
        expression.getTo().visit(this);

        helper.pushConstant(expression.isInclusive());

        createRangeMethod.call(cv);
    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
        throw new GroovyBugError("MapEntryExpression should not be visited here");
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
    
    public void visitArgumentlistExpression(ArgumentListExpression ale) {
        if (containsSpreadExpression(ale)) {
            despreadList(ale.getExpressions(),true);
        } else {
            visitTupleExpression(ale,true);
        }
    }
    
    public void visitTupleExpression(TupleExpression expression) {
        visitTupleExpression(expression,false);
    }

    private void visitTupleExpression(TupleExpression expression,boolean useWrapper) {
        int size = expression.getExpressions().size();

        helper.pushConstant(size);

        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            helper.pushConstant(i);
            Expression argument = expression.getExpression(i);
            visitAndAutoboxBoolean(argument);
            if (useWrapper && argument instanceof CastExpression) loadWrapper(argument);
            
            cv.visitInsn(AASTORE);
        }
    }
    
    private void loadWrapper(Expression argument) {
        ClassNode goalClass = argument.getType();
        visitClassExpression(new ClassExpression(goalClass));
        if (goalClass.isDerivedFromGroovyObject()) {
            createGroovyObjectWrapperMethod.call(cv);
        } else {
            createPojoWrapperMethod.call(cv);
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
                helper.unbox(int.class);
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
        boolean containsSpreadExpression = containsSpreadExpression(expression);
        if (!containsSpreadExpression) {
            helper.pushConstant(size);
    
            cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    
            for (int i = 0; i < size; i++) {
                cv.visitInsn(DUP);
                helper.pushConstant(i);
                visitAndAutoboxBoolean(expression.getExpression(i));
                cv.visitInsn(AASTORE);
            }
        } else {
            despreadList(expression.getExpressions(),false);
        }
        createListMethod.call(cv);
    }

    public void visitGStringExpression(GStringExpression expression) {
   	
        cv.visitTypeInsn(NEW, "org/codehaus/groovy/runtime/GStringImpl");
        cv.visitInsn(DUP);
        
        int size = expression.getValues().size();
        helper.pushConstant(size);
        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < size; i++) {
            cv.visitInsn(DUP);
            helper.pushConstant(i);
            visitAndAutoboxBoolean(expression.getValue(i));
            cv.visitInsn(AASTORE);
        }
        
        List strings = expression.getStrings();
    	size = strings.size();
    	helper.pushConstant(size);
    	cv.visitTypeInsn(ANEWARRAY, "java/lang/String");
    	
    	for (int i = 0; i < size; i++) {
    		cv.visitInsn(DUP);
    		helper.pushConstant(i);
    		cv.visitLdcInsn( ((ConstantExpression)strings.get(i)).getValue());
    		cv.visitInsn(AASTORE);
    	}

    	cv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/GStringImpl", "<init>", "([Ljava/lang/Object;[Ljava/lang/String;)V");
    }
    
    /**
     * Note: ignore it. Annotation generation needs the current visitor.
     */
    public void visitAnnotations(AnnotatedNode node) {
    }
    
    private void visitAnnotations(AnnotatedNode targetNode, Object visitor) {
        Map annotionMap = targetNode.getAnnotations();
        if (annotionMap.isEmpty()) return;
        
        Iterator it = annotionMap.values().iterator(); 
        while (it.hasNext()) {
            AnnotationNode an = (AnnotationNode) it.next();
            //skip builtin properties
            if (an.isBuiltIn()) continue;
            if (an.hasSourceRetention()) continue;

            AnnotationVisitor av = getAnnotationVisitor(targetNode, an, visitor);
            visitAnnotationAttributes(an, av);
            av.visitEnd();
        }        
    }
    
    private AnnotationVisitor getAnnotationVisitor(AnnotatedNode targetNode, AnnotationNode an, Object visitor) {
        final String annotationDescriptor = BytecodeHelper.getTypeDescription(an.getClassNode());
        if(targetNode instanceof MethodNode) {
            return ((MethodVisitor) visitor).visitAnnotation(annotationDescriptor, an.hasRuntimeRetention());
        }
        else if(targetNode instanceof FieldNode) {
            return ((FieldVisitor) visitor).visitAnnotation(annotationDescriptor, an.hasRuntimeRetention());
        }
        else if(targetNode instanceof ClassNode) {
            return ((ClassVisitor) visitor).visitAnnotation(annotationDescriptor, an.hasRuntimeRetention());
        }
        
        throwException("Cannot create an AnnotationVisitor. Please report Groovy bug");
        
        return null;
    }
    
    /**
     * Generate the annotation attributes.
     */
    private void visitAnnotationAttributes(AnnotationNode an, AnnotationVisitor av) {
        Map constantAttrs = new HashMap();
        Map enumAttrs = new HashMap();
        Map atAttrs = new HashMap();
        Map arrayAttrs = new HashMap();

        Iterator mIt = an.getMembers().keySet().iterator();
        while (mIt.hasNext()) {
            String name = (String) mIt.next();
            Expression expr = an.getMember(name);
            if(expr instanceof AnnotationConstantExpression) {
                atAttrs.put(name, ((AnnotationConstantExpression) expr).getValue());
            }
            else if(expr instanceof ConstantExpression) {
                constantAttrs.put(name, ((ConstantExpression) expr).getValue());
            }
            else if(expr instanceof ClassExpression) {
                constantAttrs.put(name, 
                        Type.getType(BytecodeHelper.getTypeDescription(expr.getType())));
            }
            else if(expr instanceof PropertyExpression) {
                enumAttrs.put(name, expr);
            }
            else if(expr instanceof ListExpression) {
                arrayAttrs.put(name, expr);
            }
        }
        
        for(Iterator it = constantAttrs.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            av.visit((String) entry.getKey(), entry.getValue());
        }
        for(Iterator it = enumAttrs.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            PropertyExpression propExp = (PropertyExpression) entry.getValue();
            av.visitEnum((String) entry.getKey(),
                    BytecodeHelper.getTypeDescription(propExp.getObjectExpression().getType()),
                    String.valueOf(((ConstantExpression) propExp.getProperty()).getValue()));
        }
        for(Iterator it = atAttrs.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            AnnotationNode atNode = (AnnotationNode) entry.getValue();
            AnnotationVisitor av2 = av.visitAnnotation((String) entry.getKey(),
                    BytecodeHelper.getTypeDescription(atNode.getClassNode()));
            visitAnnotationAttributes(atNode, av2);
            av2.visitEnd();
        }
        
        visitArrayAttributes(an, arrayAttrs, av);
    }
    
    private void visitArrayAttributes(AnnotationNode an, Map arrayAttr, AnnotationVisitor av) {
        if(arrayAttr.isEmpty()) return;
        
        for(Iterator it = arrayAttr.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String attrName = (String) entry.getKey();
            ListExpression listExpr = (ListExpression) entry.getValue();
            AnnotationVisitor av2 = av.visitArray(attrName);
            List values = listExpr.getExpressions();
            if(values.size() > 0) {
                Expression expr = (Expression) values.get(0);
                int arrayElementType = -1;
                if(expr instanceof AnnotationConstantExpression) {
                    arrayElementType = 1;
                }
                else if(expr instanceof ConstantExpression) {
                    arrayElementType = 2;
                }
                else if(expr instanceof ClassExpression) {
                    arrayElementType = 3;
                }
                else if(expr instanceof PropertyExpression) {
                    arrayElementType = 4;
                }
                for(Iterator exprIt = listExpr.getExpressions().iterator(); exprIt.hasNext(); ) {
                    switch(arrayElementType) {
                        case 1:
                            AnnotationNode atAttr = 
                                (AnnotationNode) ((AnnotationConstantExpression) exprIt.next()).getValue();
                            AnnotationVisitor av3 = av2.visitAnnotation(null,
                                    BytecodeHelper.getTypeDescription(atAttr.getClassNode()));
                            visitAnnotationAttributes(atAttr, av3);
                            av3.visitEnd();
                            break;
                        case 2:
                            av2.visit(null, ((ConstantExpression) exprIt.next()).getValue());
                            break;
                        case 3:
                            av2.visit(null, Type.getType(
                                    BytecodeHelper.getTypeDescription(((Expression) exprIt.next()).getType())));
                            break;
                        case 4:
                            PropertyExpression propExpr = (PropertyExpression) exprIt.next();
                            av2.visitEnum(null, 
                                    BytecodeHelper.getTypeDescription(propExpr.getObjectExpression().getType()),
                                    String.valueOf(((ConstantExpression) propExpr.getProperty()).getValue()));
                            break;
                    }
                }

            }
            av2.visitEnd();
        }
    }
    
    // Implementation methods
    //-------------------------------------------------------------------------
    protected boolean addInnerClass(ClassNode innerClass) {
        innerClass.setModule(classNode.getModule());
        return innerClasses.add(innerClass);
    }

    protected ClassNode createClosureClass(ClosureExpression expression) {
        ClassNode outerClass = getOutermostClass();
        String name = outerClass.getName() + "$"
                + context.getNextClosureInnerName(outerClass, classNode, methodNode); // br added a more infomative name
        boolean staticMethodOrInStaticClass = isStaticMethod() || classNode.isStaticClass();

        Parameter[] parameters = expression.getParameters();
        if (parameters==null){
            parameters = new Parameter[0];
        } else if (parameters.length == 0) {
            // lets create a default 'it' parameter
            parameters = new Parameter[] { new Parameter(ClassHelper.OBJECT_TYPE, "it", ConstantExpression.NULL)};
        } 

        Parameter[] localVariableParams = getClosureSharedVariables(expression);
        removeInitialValues(localVariableParams);

        InnerClassNode answer = new InnerClassNode(outerClass, name, 0, ClassHelper.CLOSURE_TYPE); // closures are local inners and not public
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

        // lets make the constructor
        BlockStatement block = new BlockStatement();
        block.setSourcePosition(expression);
        VariableExpression outer = new VariableExpression("_outerInstance");
        outer.setSourcePosition(expression);
        block.getVariableScope().getReferencedLocalVariables().put("_outerInstance",outer);
        VariableExpression thisObject = new VariableExpression("_thisObject");
        thisObject.setSourcePosition(expression);
        block.getVariableScope().getReferencedLocalVariables().put("_thisObject",thisObject);
        TupleExpression conArgs = new TupleExpression();
        conArgs.addExpression(outer);
        conArgs.addExpression(thisObject);
        block.addStatement(
            new ExpressionStatement(
                new ConstructorCallExpression(
                    ClassNode.SUPER,
                    conArgs)));

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
        params[0] = new Parameter(ClassHelper.OBJECT_TYPE, "_outerInstance");
        params[1] = new Parameter(ClassHelper.OBJECT_TYPE, "_thisObject");
        System.arraycopy(localVariableParams, 0, params, 2, localVariableParams.length);

        ASTNode sn = answer.addConstructor(ACC_PUBLIC, params, ClassNode.EMPTY_ARRAY, block);
        sn.setSourcePosition(expression);
        return answer;
    }
    
    /**
     * this method is called for local variables shared between scopes. 
     * These variables must not have init values because these would 
     * then in later steps be used to create multiple versions of the
     * same method, in this case the constructor. A closure should not 
     * have more than one constructor! 
     */
    private void removeInitialValues(Parameter[] params) {
        for (int i = 0; i < params.length; i++) {
            if (params[i].hasInitialExpression()) {
                params[i] = new Parameter(params[i].getType(),params[i].getName());
            }
        }
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

    protected void doConvertAndCast(ClassNode type){
        doConvertAndCast(type,false);
    }
    
    protected void doConvertAndCast(ClassNode type, boolean coerce) {
        if (type==ClassHelper.OBJECT_TYPE) return;
        if (isValidTypeForCast(type)) {
            visitClassExpression(new ClassExpression(type));
            if (coerce) {
                asTypeMethod.call(cv);
            } else {
                castToTypeMethod.call(cv);
            }
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
        makeCall(
                expression.getLeftExpression(),
                new ConstantExpression(method),
                new ArgumentListExpression().addExpression(expression.getRightExpression()),
                invokeMethod, false, false, false
        );
    }

    protected void evaluateCompareTo(BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
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

    private void evaluateBinaryExpression(MethodCaller compareMethod, BinaryExpression expression) {
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
        // execute method
        makeCall(
                expression, 
                new ConstantExpression(method),
                MethodCallExpression.NO_ARGUMENTS,invokeMethod,
                false,false,false);
        
        // store 
        leftHandExpression = true;
        expression.visit(this);
        
        // reload new value
        leftHandExpression = false;
        expression.visit(this);
    }

    protected void evaluatePostfixMethod(String method, Expression expression) {
        // load 
        expression.visit(this);

        // save value for later
        int tempIdx = compileStack.defineTemporaryVariable("postfix_" + method, true);
        
        //execute method
        makeCall(
                expression, new ConstantExpression(method),
                MethodCallExpression.NO_ARGUMENTS,
                invokeMethod,false,false, false);

        // store
        leftHandExpression = true;
        expression.visit(this);
        leftHandExpression = false;
        
        //reload saved value
        cv.visitVarInsn(ALOAD, tempIdx);
        compileStack.removeVar(tempIdx);
    }

    protected void evaluateInstanceof(BinaryExpression expression) {
        visitAndAutoboxBoolean(expression.getLeftExpression());
        Expression rightExp = expression.getRightExpression();
        ClassNode classType = ClassHelper.DYNAMIC_TYPE;
        if (rightExp instanceof ClassExpression) {
            ClassExpression classExp = (ClassExpression) rightExp;
            classType = classExp.getType();
        }
        else {
            throw new RuntimeException(
                "Right hand side of the instanceof keyword must be a class name, not: " + rightExp);
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
        else if (expression.getClass()==PropertyExpression.class) {
            PropertyExpression fieldExp = (PropertyExpression) expression;
            String possibleField = fieldExp.getPropertyAsString();
            if (possibleField!=null) field = classNode.getField(possibleField);
        }
        if (field != null) {
            return !field.isStatic();
        }
        return false;
    }

    private static boolean isThisExpression(Expression expression) {
        if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            return varExp.getName().equals("this");
        }
        return false;
    }
    
    private static boolean isSuperExpression(Expression expression) {
        if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            return varExp.getName().equals("super");
        }
        return false;
    }
    
    private static boolean isThisOrSuper(Expression expression) {
        return isThisExpression(expression) || isSuperExpression(expression);
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
                case Types.KEYWORD_IN :
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

    protected boolean isInClosure() {
        return classNode.getOuterClass() != null
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
    
    public static boolean usesSuper(MethodCallExpression call) {
        Expression expression = call.getObjectExpression();
        if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            String variable = varExp.getName();
            return variable.equals("super");
        }
        return false;
    }
    
    public static boolean usesSuper(PropertyExpression pe) {
        Expression expression = pe.getObjectExpression();
        if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            String variable = varExp.getName();
            return variable.equals("super");
        }
        return false;
    }    
    
    protected int getBytecodeVersion() {
        if(!this.classNode.isAnnotated()) return Opcodes.V1_3;
        
        final String target = getCompileUnit().getConfig().getTargetBytecode();
        
        return CompilerConfiguration.POST_JDK5.equals(target) ? Opcodes.V1_5 : Opcodes.V1_3;
    }

}

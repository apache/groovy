/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.classgen;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.GroovyBugError;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.classgen.asm.*;

import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.RuntimeParserException;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.*;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Generates Java class versions of Groovy classes using ASM.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:b55r@sina.com">Bing Ran</a>
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 * @author Alex Tkachman
 * @version $Revision$
 */
public class AsmClassGenerator extends ClassGenerator {

    private final ClassVisitor cv;
    private GeneratorContext context;
    private String sourceFile;

    // fields and properties
    static final MethodCallerMultiAdapter setField = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setField", false, false);
    public static final MethodCallerMultiAdapter getField = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getField", false, false);
    static final MethodCallerMultiAdapter setGroovyObjectField = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setGroovyObjectField", false, false);
    public static final MethodCallerMultiAdapter getGroovyObjectField = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getGroovyObjectField", false, false);
    static final MethodCallerMultiAdapter setFieldOnSuper = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setFieldOnSuper", false, false);
    static final MethodCallerMultiAdapter getFieldOnSuper = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getFieldOnSuper", false, false);

    public static final MethodCallerMultiAdapter setProperty = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setProperty", false, false);
    static final MethodCallerMultiAdapter getProperty = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getProperty", false, false);
    static final MethodCallerMultiAdapter setGroovyObjectProperty = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setGroovyObjectProperty", false, false);
    static final MethodCallerMultiAdapter getGroovyObjectProperty = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getGroovyObjectProperty", false, false);
    static final MethodCallerMultiAdapter setPropertyOnSuper = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setPropertyOnSuper", false, false);
    static final MethodCallerMultiAdapter getPropertyOnSuper = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getPropertyOnSuper", false, false);

     // spread expressions
    static final MethodCaller spreadMap = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "spreadMap");
    static final MethodCaller despreadList = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "despreadList");
    // Closure
    static final MethodCaller getMethodPointer = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getMethodPointer");

    // type conversions
    static final MethodCaller createListMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createList");
    static final MethodCaller createMapMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createMap");
    static final MethodCaller createRangeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createRange");

    // wrapper creation methods
    static final MethodCaller createPojoWrapperMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createPojoWrapper");
    static final MethodCaller createGroovyObjectWrapperMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createGroovyObjectWrapper");

    // constructor calls with this() and super()
    static final MethodCaller selectConstructorAndTransformArguments = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "selectConstructorAndTransformArguments");

    // exception blocks list
    private Map<String,ClassNode> referencedClasses = new HashMap<String,ClassNode>();
    private boolean passingParams;

    public static final boolean CREATE_DEBUG_INFO = true;
    public static final boolean CREATE_LINE_NUMBER_INFO = true;
    public static final boolean ASM_DEBUG = false; // add marker in the bytecode to show source-bytecode relationship

    private ASTNode currentASTNode = null;
    private Map genericParameterNames = null;
    private SourceUnit source;
    private WriterController controller;
    
    public AsmClassGenerator(
            SourceUnit source, GeneratorContext context,
            ClassVisitor classVisitor, String sourceFile
    ) {
        this.source = source;
        this.context = context;
        this.cv = classVisitor;
        this.sourceFile = sourceFile;
        genericParameterNames = new HashMap();
    }

    public SourceUnit getSourceUnit() {
        return source;
    }

    public WriterController getController() {
        return controller;
    }

    // GroovyClassVisitor interface
    //-------------------------------------------------------------------------
    public void visitClass(ClassNode classNode) {
        referencedClasses.clear();
        WriterControllerFactory factory = (WriterControllerFactory) classNode.getNodeMetaData(WriterControllerFactory.class);
        WriterController normalController = new WriterController();
        if (factory!=null) {
            this.controller = factory.makeController(normalController);
        } else {
            this.controller = normalController;
        }
        this.controller.init(this, context, cv, classNode);

        if (controller.shouldOptimizeForInt() || factory!=null) {
            OptimizingStatementWriter.setNodeMeta(controller.getTypeChooser(),classNode);
        }

        try {
            cv.visit(
                    controller.getBytecodeVersion(),
                    adjustedClassModifiers(classNode.getModifiers()),
                    controller.getInternalClassName(),
                    BytecodeHelper.getGenericsSignature(classNode),
                    controller.getInternalBaseClassName(),
                    BytecodeHelper.getClassInternalNames(classNode.getInterfaces())
            );
            cv.visitSource(sourceFile, null);
            if (classNode.getName().endsWith("package-info")) {
                PackageNode packageNode = classNode.getPackage();
                if (packageNode != null) {
                    // pull them out of package node but treat them like they were on class node
                    for (AnnotationNode an : packageNode.getAnnotations()) {
                        // skip built-in properties
                        if (an.isBuiltIn()) continue;
                        if (an.hasSourceRetention()) continue;

                        AnnotationVisitor av = getAnnotationVisitor(classNode, an, cv);
                        visitAnnotationAttributes(an, av);
                        av.visitEnd();
                    }
                }
                cv.visitEnd();
                return;
            } else {
                visitAnnotations(classNode, cv);
            }

            if (classNode.isInterface()) {
                ClassNode owner = classNode;
                if (owner instanceof InnerClassNode) {
                    owner = owner.getOuterClass();
                }
                String outerClassName = classNode.getName();
                String name = outerClassName + "$" + context.getNextInnerClassIdx();
                controller.setInterfaceClassLoadingClass(
                        new InterfaceHelperClassNode (
                                owner, name, 4128, ClassHelper.OBJECT_TYPE,
                                controller.getCallSiteWriter().getCallSites()));
                super.visitClass(classNode);
                createInterfaceSyntheticStaticFields();
            } else {
                super.visitClass(classNode);
                MopWriter mopWriter = new MopWriter(controller);
                mopWriter.createMopMethods();
                controller.getCallSiteWriter().generateCallSiteArray();
                createSyntheticStaticFields();
            }

            for (Iterator<InnerClassNode> iter = classNode.getInnerClasses(); iter.hasNext();) {
                InnerClassNode innerClass = iter.next();
                makeInnerClassEntry(innerClass);
            }
            makeInnerClassEntry(classNode);

            cv.visitEnd();
        } catch (GroovyRuntimeException e) {
            e.setModule(classNode.getModule());
            throw e;
        } catch (NegativeArraySizeException nase) {
            throw new GroovyRuntimeException("NegativeArraySizeException while processing "+sourceFile, nase);
        } catch (NullPointerException npe) {
            throw new GroovyRuntimeException("NPE while processing "+sourceFile, npe);
        }
    }

    private void makeInnerClassEntry(ClassNode cn) {
        if (!(cn instanceof InnerClassNode)) return;
        InnerClassNode innerClass = (InnerClassNode) cn;
        String innerClassName = innerClass.getName();
        String innerClassInternalName = BytecodeHelper.getClassInternalName(innerClassName);
        {
            int index = innerClassName.lastIndexOf('$');
            if (index >= 0) innerClassName = innerClassName.substring(index + 1);
        }
        String outerClassName = BytecodeHelper.getClassInternalName(innerClass.getOuterClass().getName());
        MethodNode enclosingMethod = innerClass.getEnclosingMethod();
        if (enclosingMethod != null) {
            // local inner classes do not specify the outer class name
            outerClassName = null;
            innerClassName = null;
        }
        int mods = innerClass.getModifiers();
        cv.visitInnerClass(
                innerClassInternalName,
                outerClassName,
                innerClassName,
                mods);
    }

    /*
     * Classes but not interfaces should have ACC_SUPER set
     */
    private int adjustedClassModifiers(int modifiers) {
        boolean needsSuper = (modifiers & ACC_INTERFACE) == 0;
        modifiers = needsSuper ? modifiers | ACC_SUPER : modifiers;
        // eliminate static
        modifiers = modifiers & ~ACC_STATIC;
        return modifiers;
    }

    public void visitGenericType(GenericsType genericsType) {
        ClassNode type = genericsType.getType();
        genericParameterNames.put(type.getName(), genericsType);
    }

    private String[] buildExceptions(ClassNode[] exceptions) {
        if (exceptions == null) return null;
        String[] ret = new String[exceptions.length];
        for (int i = 0; i < exceptions.length; i++) {
            ret[i] = BytecodeHelper.getClassInternalName(exceptions[i]);
        }
        return ret;
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        controller.resetLineNumber();
    	Parameter[] parameters = node.getParameters();
        String methodType = BytecodeHelper.getMethodDescriptor(node.getReturnType(), parameters);
        String signature = BytecodeHelper.getGenericsMethodSignature(node);
        int modifiers = node.getModifiers();
        if (isVargs(node.getParameters())) modifiers |= Opcodes.ACC_VARARGS;
        MethodVisitor mv = cv.visitMethod(modifiers, node.getName(), methodType, signature, buildExceptions(node.getExceptions()));
        controller.setMethodVisitor(mv);

        visitAnnotations(node, mv);
        for (int i = 0; i < parameters.length; i++) {
            visitParameterAnnotations(parameters[i], i, mv);
        }

        if (controller.getClassNode().isAnnotationDefinition() && !node.isStaticConstructor()) {
            visitAnnotationDefault(node, mv);
        } else if (!node.isAbstract()) {
            Statement code = node.getCode();
            mv.visitCode();

            // fast path for getter/setters etc.
            if (code instanceof BytecodeSequence && ((BytecodeSequence)code).getInstructions().size() == 1 && ((BytecodeSequence)code).getInstructions().get(0) instanceof BytecodeInstruction) {
               ((BytecodeInstruction)((BytecodeSequence)code).getInstructions().get(0)).visit(mv);
            } else {
                visitStdMethod(node, isConstructor, parameters, code);
            }
            // we use this NOP to have a valid jump target for the various labels
            //mv.visitInsn(NOP);
            mv.visitMaxs(0, 0);
        }
        mv.visitEnd();
    }

    private void visitStdMethod(MethodNode node, boolean isConstructor, Parameter[] parameters, Statement code) {
        MethodVisitor mv = controller.getMethodVisitor();
        final ClassNode superClass = controller.getClassNode().getSuperClass();
        if (isConstructor && (code == null || !((ConstructorNode) node).firstStatementIsSpecialConstructorCall())) {
            boolean hasCallToSuper = false;
            if (code!=null && controller.getClassNode() instanceof InnerClassNode) {
                // if the class not is an inner class node, there are chances that the call to super is already added
                // so we must ensure not to add it twice (see GROOVY-4471)
                if (code instanceof BlockStatement) {
                    for (Statement statement : ((BlockStatement) code).getStatements()) {
                        if (statement instanceof ExpressionStatement) {
                            final Expression expression = ((ExpressionStatement) statement).getExpression();
                            if (expression instanceof ConstructorCallExpression) {
                                ConstructorCallExpression call = (ConstructorCallExpression) expression;
                                if (call.isSuperCall()) {
                                    hasCallToSuper = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!hasCallToSuper) {
                // invokes the super class constructor
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(superClass), "<init>", "()V");
            }
        }

        controller.getCompileStack().init(node.getVariableScope(), parameters);
        controller.getCallSiteWriter().makeSiteEntry();

        // handle body
        super.visitConstructorOrMethod(node, isConstructor);

        controller.getCompileStack().clear();
        if (node.isVoidMethod()) {
            mv.visitInsn(RETURN);
        } else {
            // we make a dummy return for label ranges that reach here
            ClassNode type = node.getReturnType().redirect();
            if (ClassHelper.isPrimitiveType(type)) {
                mv.visitLdcInsn(0);
                controller.getOperandStack().push(ClassHelper.int_TYPE);
                controller.getOperandStack().doGroovyCast(type);
                BytecodeHelper.doReturn(mv, type);
                controller.getOperandStack().remove(1);
            } else {
                mv.visitInsn(ACONST_NULL);
                BytecodeHelper.doReturn(mv, type);
            }
        }
    }

    void visitAnnotationDefaultExpression(AnnotationVisitor av, ClassNode type, Expression exp) {
        if (exp instanceof ClosureExpression) {
            ClassNode closureClass = controller.getClosureWriter().getOrAddClosureClass((ClosureExpression) exp, ACC_PUBLIC);
            Type t = Type.getType(BytecodeHelper.getTypeDescription(closureClass));
            av.visit(null, t);
       } else if (type.isArray()) {
           ListExpression list = (ListExpression) exp;
           AnnotationVisitor avl = av.visitArray(null);
           ClassNode componentType = type.getComponentType();
           for (Expression lExp : list.getExpressions()) {
               visitAnnotationDefaultExpression(avl, componentType, lExp);
           }
       } else if (ClassHelper.isPrimitiveType(type) || type.equals(ClassHelper.STRING_TYPE)) {
           ConstantExpression constExp = (ConstantExpression) exp;
           av.visit(null, constExp.getValue());
       } else if (ClassHelper.CLASS_Type.equals(type)) {
           ClassNode clazz = exp.getType();
           Type t = Type.getType(BytecodeHelper.getTypeDescription(clazz));
           av.visit(null, t);
       } else if (type.isDerivedFrom(ClassHelper.Enum_Type)) {
           PropertyExpression pExp = (PropertyExpression) exp;
           ClassExpression cExp = (ClassExpression) pExp.getObjectExpression();
           String desc = BytecodeHelper.getTypeDescription(cExp.getType());
           String name = pExp.getPropertyAsString();
           av.visitEnum(null, desc, name);
       } else if (type.implementsInterface(ClassHelper.Annotation_TYPE)) {
           AnnotationConstantExpression avExp = (AnnotationConstantExpression) exp;
           AnnotationNode value = (AnnotationNode) avExp.getValue();
           AnnotationVisitor avc = av.visitAnnotation(null, BytecodeHelper.getTypeDescription(avExp.getType()));
           visitAnnotationAttributes(value,avc);
       } else {
           throw new GroovyBugError("unexpected annotation type " + type.getName());
       }
       av.visitEnd();
   }

    private void visitAnnotationDefault(MethodNode node, MethodVisitor mv) {
        if (!node.hasAnnotationDefault()) return;
        Expression exp = ((ReturnStatement) node.getCode()).getExpression();
        AnnotationVisitor av = mv.visitAnnotationDefault();
        visitAnnotationDefaultExpression(av,node.getReturnType(),exp);
    }

    private static boolean isVargs(Parameter[] p) {
        if (p.length==0) return false;
        ClassNode clazz = p[p.length-1].getType();
        return (clazz.isArray());
    }

    public void visitConstructor(ConstructorNode node) {
        controller.setConstructorNode(node);
        super.visitConstructor(node);
    }

    public void visitMethod(MethodNode node) {
        controller.setMethodNode(node);
        super.visitMethod(node);
    }

    public void visitField(FieldNode fieldNode) {
        onLineNumber(fieldNode, "visitField: " + fieldNode.getName());
        ClassNode t = fieldNode.getType();
        String signature = BytecodeHelper.getGenericsBounds(t);

        Expression initialValueExpression = fieldNode.getInitialValueExpression();
        ConstantExpression cexp = initialValueExpression instanceof ConstantExpression? (ConstantExpression) initialValueExpression :null;
        if (cexp!=null) {
            cexp = Verifier.transformToPrimitiveConstantIfPossible(cexp);
        }
        Object value = cexp!=null && ClassHelper.isStaticConstantInitializerType(cexp.getType())
                && cexp.getType().equals(t)
                && fieldNode.isStatic() && fieldNode.isFinal()
                ?cexp.getValue() // GROOVY-5150
                :null;
        if (value!=null) {
            // byte, char and short require an extra cast
            if (ClassHelper.byte_TYPE.equals(t) || ClassHelper.short_TYPE.equals(t)) {
                value = ((Number) value).intValue();
            } else if (ClassHelper.char_TYPE.equals(t)) {
                value = Integer.valueOf((Character)value);
            }
        }
        FieldVisitor fv = cv.visitField(
                fieldNode.getModifiers(),
                fieldNode.getName(),
                BytecodeHelper.getTypeDescription(t),
                signature,
                value);
        visitAnnotations(fieldNode, fv);
        fv.visitEnd();
    }

    public void visitProperty(PropertyNode statement) {
        // the verifier created the field and the setter/getter methods, so here is
        // not really something to do
        onLineNumber(statement, "visitProperty:" + statement.getField().getName());
        controller.setMethodNode(null);
    }

    // GroovyCodeVisitor interface
    //-------------------------------------------------------------------------

    // Statements
    //-------------------------------------------------------------------------

    protected void visitStatement(Statement statement) {
        throw new GroovyBugError("visitStatement should not be visited here.");
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
        statement.getCode().visit(this);
    }

    public void visitBlockStatement(BlockStatement block) {
        controller.getStatementWriter().writeBlockStatement(block);
    }

    public void visitForLoop(ForStatement loop) {
        controller.getStatementWriter().writeForStatement(loop);
    }

    public void visitWhileLoop( WhileStatement loop) {
        controller.getStatementWriter().writeWhileLoop(loop);
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        controller.getStatementWriter().writeDoWhileLoop(loop);
    }

    public void visitIfElse(IfStatement ifElse) {
        controller.getStatementWriter().writeIfElse(ifElse);
    }

    public void visitAssertStatement(AssertStatement statement) {
        controller.getStatementWriter().writeAssert(statement);
    }

    public void visitTryCatchFinally(TryCatchStatement statement) {
        controller.getStatementWriter().writeTryCatchFinally(statement);
    }

    public void visitSwitch(SwitchStatement statement) {
        controller.getStatementWriter().writeSwitch(statement);
    }

    public void visitCaseStatement(CaseStatement statement) {}

    public void visitBreakStatement(BreakStatement statement) {
        controller.getStatementWriter().writeBreak(statement);
    }

    public void visitContinueStatement(ContinueStatement statement) {
        controller.getStatementWriter().writeContinue(statement);
    }

    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        controller.getStatementWriter().writeSynchronized(statement);
    }

    public void visitThrowStatement(ThrowStatement statement) {
        controller.getStatementWriter().writeThrow(statement);
    }

    public void visitReturnStatement(ReturnStatement statement) {
        controller.getStatementWriter().writeReturn(statement);
    }

    public void visitExpressionStatement(ExpressionStatement statement) {
        controller.getStatementWriter().writeExpressionStatement(statement);
    }

    // Expressions
    //-------------------------------------------------------------------------

    public void visitTernaryExpression(TernaryExpression expression) {
        onLineNumber(expression, "visitTernaryExpression");
        controller.getBinaryExpressionHelper().evaluateTernary(expression);
    }

    public void visitDeclarationExpression(DeclarationExpression expression) {
        onLineNumber(expression, "visitDeclarationExpression: \"" + expression.getText() + "\"");
        controller.getBinaryExpressionHelper().evaluateEqual(expression,true);
    }

    public void visitBinaryExpression(BinaryExpression expression) {
        onLineNumber(expression, "visitBinaryExpression: \"" + expression.getOperation().getText() + "\" ");
        controller.getBinaryExpressionHelper().eval(expression);
        controller.getAssertionWriter().record(expression.getOperation());
    }

    public void visitPostfixExpression(PostfixExpression expression) {
        controller.getBinaryExpressionHelper().evaluatePostfixMethod(expression);
        controller.getAssertionWriter().record(expression);
    }

    public void throwException(String s) {
        throw new RuntimeParserException(s, currentASTNode);
    }

    public void visitPrefixExpression(PrefixExpression expression) {
        controller.getBinaryExpressionHelper().evaluatePrefixMethod(expression);
        controller.getAssertionWriter().record(expression);
    }

    public void visitClosureExpression(ClosureExpression expression) {
        controller.getClosureWriter().writeClosure(expression);
    }

    /**
     * Loads either this object or if we're inside a closure then load the top level owner
     */
    protected void loadThisOrOwner() {
        if (isInnerClass()) {
            visitFieldExpression(new FieldExpression(controller.getClassNode().getDeclaredField("owner")));
        } else {
            loadThis();
        }
    }

    /**
     * Generate byte code for constants
     *
     * @see <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#14152">Class field types</a>
     */
    public void visitConstantExpression(ConstantExpression expression) {
        final String constantName = expression.getConstantName();
        if (controller.isStaticConstructor() || constantName == null) {
            controller.getOperandStack().pushConstant(expression);
        } else {
            controller.getMethodVisitor().visitFieldInsn(GETSTATIC, controller.getInternalClassName(),constantName, BytecodeHelper.getTypeDescription(expression.getType()));
            controller.getOperandStack().push(expression.getType());
        }
    }

    public void visitSpreadExpression(SpreadExpression expression) {
        throw new GroovyBugError("SpreadExpression should not be visited here");
    }

    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        Expression subExpression = expression.getExpression();
        // to not record the underlying MapExpression twice,
        // we disable the assertion tracker
        // see http://jira.codehaus.org/browse/GROOVY-3421
        controller.getAssertionWriter().disableTracker();
        subExpression.visit(this);
        controller.getOperandStack().box();
        spreadMap.call(controller.getMethodVisitor());
        controller.getAssertionWriter().reenableTracker();
        controller.getOperandStack().replace(ClassHelper.OBJECT_TYPE);
    }

    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(this);
        controller.getOperandStack().box();
        controller.getOperandStack().pushDynamicName(expression.getMethodName());
        getMethodPointer.call(controller.getMethodVisitor());
        controller.getOperandStack().replace(ClassHelper.CLOSURE_TYPE,2);
    }

    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        controller.getUnaryExpressionHelper().writeUnaryMinus(expression);
    }

    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        controller.getUnaryExpressionHelper().writeUnaryPlus(expression);
    }

    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        controller.getUnaryExpressionHelper().writeBitwiseNegate(expression);
    }

    public void visitCastExpression(CastExpression castExpression) {
        ClassNode type = castExpression.getType();
        Expression subExpression = castExpression.getExpression();
        subExpression.visit(this);
        if (castExpression.isCoerce()) {
            controller.getOperandStack().doAsType(type);
        } else {
            if (isNullConstant(subExpression)) {
                controller.getOperandStack().replace(type);
            } else {
                controller.getOperandStack().doGroovyCast(type);
            }
        }
    }

    public void visitNotExpression(NotExpression expression) {
        controller.getUnaryExpressionHelper().writeNotExpression(expression);
    }

    /**
     * return a primitive boolean value of the BooleanExpression.
     *
     * @param expression
     */
    public void visitBooleanExpression(BooleanExpression expression) {
        controller.getCompileStack().pushBooleanExpression();
        int mark = controller.getOperandStack().getStackLength();
        Expression inner = expression.getExpression();
        inner.visit(this);
        controller.getOperandStack().castToBool(mark, true);
        controller.getCompileStack().pop();
    }

    public void visitMethodCallExpression(MethodCallExpression call) {
        onLineNumber(call, "visitMethodCallExpression: \"" + call.getMethod() + "\":");
        controller.getInvocationWriter().writeInvokeMethod(call);
        controller.getAssertionWriter().record(call.getMethod());
    }

    protected boolean emptyArguments(Expression arguments) {
        return argumentSize(arguments) == 0;
    }

    public static boolean containsSpreadExpression(Expression arguments) {
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

    public static int argumentSize(Expression arguments) {
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            int size = tupleExpression.getExpressions().size();
            return size;
        }
        return 1;
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        onLineNumber(call, "visitStaticMethodCallExpression: \"" + call.getMethod() + "\":");
        controller.getInvocationWriter().writeInvokeStaticMethod(call);
        controller.getAssertionWriter().record(call);
    }

    private void visitSpecialConstructorCall(ConstructorCallExpression call) {
        if (controller.getClosureWriter().addGeneratedClosureConstructorCall(call)) return;

        ClassNode callNode = controller.getClassNode();
        if (call.isSuperCall()) callNode = callNode.getSuperClass();
        List<ConstructorNode> constructors = sortConstructors(call, callNode);
        if (!makeDirectConstructorCall(constructors, call, callNode)) {
            makeMOPBasedConstructorCall(constructors, call, callNode);
        }
    }

    // we match only on the number of arguments, not anything else
    private static ConstructorNode getMatchingConstructor(List<ConstructorNode> constructors, List<Expression> argumentList) {
        ConstructorNode lastMatch = null;
        for (int i=0; i<constructors.size(); i++) {
            ConstructorNode cn = constructors.get(i);
            Parameter[] params = cn.getParameters();
            // if number of parameters does not match we have no match
            if (argumentList.size()!=params.length) continue;
            if (lastMatch==null) {
                lastMatch = cn;
            } else {
                // we already had a match so we don't make a direct call at all
                return null;
            }
        }
        return lastMatch;
    }

    private boolean makeDirectConstructorCall(List<ConstructorNode> constructors, ConstructorCallExpression call, ClassNode callNode) {
        if (!controller.isConstructor()) return false;

        Expression arguments = call.getArguments();
        List<Expression> argumentList;
        if (arguments instanceof TupleExpression) {
            argumentList = ((TupleExpression) arguments).getExpressions();
        } else {
            argumentList = new ArrayList();
            argumentList.add(arguments);
        }
        for (Expression expression : argumentList) {
            if (expression instanceof SpreadExpression) return false;
        }

        ConstructorNode cn = getMatchingConstructor(constructors, argumentList);
        if (cn==null) return false;
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        Parameter[] params = cn.getParameters();

        mv.visitVarInsn(ALOAD, 0);
        for (int i=0; i<params.length; i++) {
            Expression expression = argumentList.get(i);
            expression.visit(this);
            if (!isNullConstant(expression)) {
                operandStack.doGroovyCast(params[i].getType());
            }
            operandStack.remove(1);
        }
        String descriptor = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, params);
        mv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(callNode), "<init>", descriptor);

        return true;
    }
     
    private static boolean isNullConstant(Expression expr) {
        return expr instanceof ConstantExpression && ((ConstantExpression) expr).getValue()==null;
    }

    private void makeMOPBasedConstructorCall(List<ConstructorNode> constructors, ConstructorCallExpression call, ClassNode callNode) {
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        call.getArguments().visit(this);
        // keep Object[] on stack
        mv.visitInsn(DUP);
        // to select the constructor we need also the number of
        // available constructors and the class we want to make
        // the call on
        BytecodeHelper.pushConstant(mv, constructors.size());
        visitClassExpression(new ClassExpression(callNode));
        operandStack.remove(1);
        // removes one Object[] leaves the int containing the
        // call flags and the constructor number
        selectConstructorAndTransformArguments.call(mv);
        // Object[],int -> int,Object[],int
        // we need to examine the flags and maybe change the
        // Object[] later, so this reordering will do the job
        mv.visitInsn(DUP_X1);
        // test if rewrap flag is set
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IAND);
        Label afterIf = new Label();
        mv.visitJumpInsn(IFEQ, afterIf);
        // true part, so rewrap using the first argument
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
        mv.visitLabel(afterIf);
        // here the stack is int,Object[], but we need the
        // the int for our table, so swap it
        mv.visitInsn(SWAP);
        //load "this"
        if (controller.isConstructor()) {
            mv.visitVarInsn(ALOAD, 0);
        } else {
            mv.visitTypeInsn(NEW, BytecodeHelper.getClassInternalName(callNode));
        }
        mv.visitInsn(SWAP);
        //prepare switch with >>8
        mv.visitIntInsn(BIPUSH, 8);
        mv.visitInsn(ISHR);
        Label[] targets = new Label[constructors.size()];
        int[] indices = new int[constructors.size()];
        for (int i = 0; i < targets.length; i++) {
            targets[i] = new Label();
            indices[i] = i;
        }
        // create switch targets
        Label defaultLabel = new Label();
        Label afterSwitch = new Label();
        mv.visitLookupSwitchInsn(defaultLabel, indices, targets);
        for (int i = 0; i < targets.length; i++) {
            mv.visitLabel(targets[i]);
            // to keep the stack height, we need to leave
            // one Object[] on the stack as last element. At the
            // same time, we need the Object[] on top of the stack
            // to extract the parameters.
            if (controller.isConstructor()) {
                // in this case we need one "this", so a SWAP will exchange
                // "this" and Object[], a DUP_X1 will then copy the Object[]
                /// to the last place in the stack:
                //     Object[],this -SWAP-> this,Object[]
                //     this,Object[] -DUP_X1-> Object[],this,Object[]
                mv.visitInsn(SWAP);
                mv.visitInsn(DUP_X1);
            } else {
                // in this case we need two "this" in between and the Object[]
                // at the bottom of the stack as well as on top for our invokeSpecial
                // So we do DUP_X1, DUP2_X1, POP
                //     Object[],this -DUP_X1-> this,Object[],this
                //     this,Object[],this -DUP2_X1-> Object[],this,this,Object[],this
                //     Object[],this,this,Object[],this -POP->  Object[],this,this,Object[]
                mv.visitInsn(DUP_X1);
                mv.visitInsn(DUP2_X1);
                mv.visitInsn(POP);
            }

            ConstructorNode cn = constructors.get(i);
            String descriptor = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, cn.getParameters());
            // unwrap the Object[] and make transformations if needed
            // that means, to duplicate the Object[], make a cast with possible
            // unboxing and then swap it with the Object[] for each parameter
            Parameter[] parameters = cn.getParameters();
            for (int p = 0; p < parameters.length; p++) {
                operandStack.push(ClassHelper.OBJECT_TYPE);
                mv.visitInsn(DUP);
                BytecodeHelper.pushConstant(mv, p);
                mv.visitInsn(AALOAD);
                operandStack.push(ClassHelper.OBJECT_TYPE);
                ClassNode type = parameters[p].getType();
                operandStack.doGroovyCast(type);
                operandStack.swap();
                operandStack.remove(2);
            }
            // at the end we remove the Object[]
            mv.visitInsn(POP);
            // make the constructor call
            mv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(callNode), "<init>", descriptor);
            mv.visitJumpInsn(GOTO, afterSwitch);
        }
        mv.visitLabel(defaultLabel);
        // this part should never be reached!
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("illegal constructor number");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(afterSwitch);

        // For a special constructor call inside a constructor we don't need
        // any result object on the stack, for outside the constructor we do.
        // to keep the stack height for the able we kept one object as dummy
        // result on the stack, which we can remove now if inside a constructor.
        if (!controller.isConstructor()) {
            // in case we are not in a constructor we have an additional
            // object on the stack, the result of our constructor call
            // which we want to keep, so we swap with the dummy object and
            // do normal removal of it. In the end, the call result will be
            // on the stack then
            mv.visitInsn(SWAP);
            operandStack.push(callNode); // for call result
        }
        mv.visitInsn(POP);
    }

    private List<ConstructorNode> sortConstructors(ConstructorCallExpression call, ClassNode callNode) {
        // sort in a new list to prevent side effects
        List<ConstructorNode> constructors = new ArrayList<ConstructorNode>(callNode.getDeclaredConstructors());
        Comparator comp = new Comparator() {
            public int compare(Object arg0, Object arg1) {
                ConstructorNode c0 = (ConstructorNode) arg0;
                ConstructorNode c1 = (ConstructorNode) arg1;
                String descriptor0 = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, c0.getParameters());
                String descriptor1 = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, c1.getParameters());
                return descriptor0.compareTo(descriptor1);
            }
        };
        Collections.sort(constructors, comp);
        return constructors;
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        onLineNumber(call, "visitConstructorCallExpression: \"" + call.getType().getName() + "\":");

        if (call.isSpecialCall()) {
            controller.getCompileStack().pushInSpecialConstructorCall();
            visitSpecialConstructorCall(call);
            controller.getCompileStack().pop();
            return;
        }
        controller.getInvocationWriter().writeInvokeConstructor(call);
        controller.getAssertionWriter().record(call);
    }

    private static String makeFieldClassName(ClassNode type) {
        String internalName = BytecodeHelper.getClassInternalName(type);
        StringBuffer ret = new StringBuffer(internalName.length());
        for (int i = 0; i < internalName.length(); i++) {
            char c = internalName.charAt(i);
            if (c == '/') {
                ret.append('$');
            } else if (c == ';') {
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
        for (; componentType.isArray(); componentType = componentType.getComponentType()) {
            prefix += "$";
        }
        if (prefix.length() != 0) prefix = "array" + prefix;
        String name = prefix + "$class$" + makeFieldClassName(componentType);
        return name;
    }

    private void visitAttributeOrProperty(PropertyExpression expression, MethodCallerMultiAdapter adapter) {
        MethodVisitor mv = controller.getMethodVisitor();

        Expression objectExpression = expression.getObjectExpression();
        ClassNode classNode = controller.getClassNode();
        if (isThisOrSuper(objectExpression)) {
            // let's use the field expression if it's available
            String name = expression.getPropertyAsString();
            if (name != null) {
                FieldNode field = null;
                boolean privateSuperField = false;
                if (isSuperExpression(objectExpression)) {
                    field = classNode.getSuperClass().getDeclaredField(name);
                    if (field != null && ((field.getModifiers() & ACC_PRIVATE) != 0)) {
                        privateSuperField = true;
                    }
                } else {
                	if (controller.isNotExplicitThisInClosure(expression.isImplicitThis())) {
                        field = classNode.getDeclaredField(name);
                        if (field==null && classNode instanceof InnerClassNode) {
                            ClassNode outer = classNode.getOuterClass();
                            FieldNode outerClassField;
                            while (outer!=null) {
                                outerClassField = outer.getDeclaredField(name);
                                if (outerClassField!=null && outerClassField.isStatic() && outerClassField.isFinal()) {
                                    if (outer!=classNode.getOuterClass() && Modifier.isPrivate(outerClassField.getModifiers())) {
                                        throw new GroovyBugError("Trying to access private constant field ["+outerClassField.getDeclaringClass()+"#"+outerClassField.getName()+"] from inner class");
                                    }
                                    PropertyExpression pexp = new PropertyExpression(
                                            new ClassExpression(outer),
                                            expression.getProperty()
                                    );
                                    pexp.visit(controller.getAcg());
                                    return;
                                }
                                outer = outer.getSuperClass();
                            }
                        }
                        if (field==null
                                && expression instanceof AttributeExpression
                                && isThisExpression(objectExpression)
                                && controller.isStaticContext()) {
                            // GROOVY-6183
                            ClassNode current = classNode.getSuperClass();
                            while (field==null && current!=null) {
                                field = current.getDeclaredField(name);
                                current = current.getSuperClass();
                            }
                            if (field!=null && (field.isProtected() || field.isPublic())) {
                                visitFieldExpression(new FieldExpression(field));
                                return;
                            }
                        }
                	}
                }
                if (field != null && !privateSuperField) {//GROOVY-4497: don't visit super field if it is private
                    visitFieldExpression(new FieldExpression(field));
                    return;
                }
            }
            if (isSuperExpression(objectExpression)) {
                String prefix;
                if (controller.getCompileStack().isLHS()) {
                    prefix = "set";
                } else {
                    prefix = "get";
                }
                String propName = prefix + MetaClassHelper.capitalize(name);
                visitMethodCallExpression(new MethodCallExpression(objectExpression, propName, MethodCallExpression.NO_ARGUMENTS));
                return;
            }
        }

        final String propName = expression.getPropertyAsString();
        //TODO: add support for super here too
        if (expression.getObjectExpression() instanceof ClassExpression &&
            propName!=null && propName.equals("this"))
        {
            // we have something like A.B.this, and need to make it
            // into this.this$0.this$0, where this.this$0 returns
            // A.B and this.this$0.this$0 return A.
            ClassNode type = objectExpression.getType();
            ClassNode iterType = classNode;
            mv.visitVarInsn(ALOAD, 0);
            while (!iterType.equals(type)) {
                String ownerName = BytecodeHelper.getClassInternalName(iterType);
                if (iterType.getOuterClass()==null) break;
                iterType = iterType.getOuterClass();
                String typeName = BytecodeHelper.getTypeDescription(iterType);
                mv.visitFieldInsn(GETFIELD, ownerName, "this$0", typeName);
            }
            controller.getOperandStack().push(type);
            return;
        }

        if (adapter == getProperty && !expression.isSpreadSafe() && propName != null) {
            controller.getCallSiteWriter().makeGetPropertySite(objectExpression, propName, expression.isSafe(), expression.isImplicitThis());
        } else if (adapter == getGroovyObjectProperty && !expression.isSpreadSafe() && propName != null) {
            controller.getCallSiteWriter().makeGroovyObjectGetPropertySite(objectExpression, propName, expression.isSafe(), expression.isImplicitThis());
        } else {
            // todo: for improved modularity and extensibility, this should be moved into a writer
            if (controller.getCompileStack().isLHS()) controller.getOperandStack().box();
            controller.getInvocationWriter().makeCall(
                    expression,
                    objectExpression, // receiver
                    new CastExpression(ClassHelper.STRING_TYPE, expression.getProperty()), // messageName
                    MethodCallExpression.NO_ARGUMENTS, adapter,
                    expression.isSafe(), expression.isSpreadSafe(), expression.isImplicitThis()
            );
        }
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        Expression objectExpression = expression.getObjectExpression();
        OperandStack operandStack = controller.getOperandStack();
        int mark = operandStack.getStackLength()-1;
        MethodCallerMultiAdapter adapter;
        if (controller.getCompileStack().isLHS()) {
            //operandStack.box();
            adapter = setProperty;
            if (isGroovyObject(objectExpression)) adapter = setGroovyObjectProperty;
            if (controller.isStaticContext() && isThisOrSuper(objectExpression)) adapter = setProperty;
        } else {
            adapter = getProperty;
            if (isGroovyObject(objectExpression)) adapter = getGroovyObjectProperty;
            if (controller.isStaticContext() && isThisOrSuper(objectExpression)) adapter = getProperty;
        }
        visitAttributeOrProperty(expression, adapter);
        if (controller.getCompileStack().isLHS()) {
            // remove surplus values
            operandStack.remove(operandStack.getStackLength()-mark);
        } else {
            controller.getAssertionWriter().record(expression.getProperty());
        }
    }

    public void visitAttributeExpression(AttributeExpression expression) {
        Expression objectExpression = expression.getObjectExpression();
        MethodCallerMultiAdapter adapter;
        OperandStack operandStack = controller.getOperandStack();
        int mark = operandStack.getStackLength()-1;
        if (controller.getCompileStack().isLHS()) {
            adapter = setField;
            if (isGroovyObject(objectExpression)) adapter = setGroovyObjectField;
            if (usesSuper(expression)) adapter = setFieldOnSuper;
        } else {
            adapter = getField;
            if (isGroovyObject(objectExpression)) adapter = getGroovyObjectField;
            if (usesSuper(expression)) adapter = getFieldOnSuper;
        }
        visitAttributeOrProperty(expression, adapter);
        if (!controller.getCompileStack().isLHS()) {
            controller.getAssertionWriter().record(expression.getProperty());
        } else {
            operandStack.remove(operandStack.getStackLength() - mark);
        }
    }

    private static boolean usesSuper(PropertyExpression pe) {
        Expression expression = pe.getObjectExpression();
        if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            String variable = varExp.getName();
            return variable.equals("super");
        }
        return false;
    }

    private static boolean isGroovyObject(Expression objectExpression) {
        return isThisExpression(objectExpression) || objectExpression.getType().isDerivedFromGroovyObject() && !(objectExpression instanceof ClassExpression);
    }

    public void visitFieldExpression(FieldExpression expression) {
        FieldNode field = expression.getField();

        if (field.isStatic()) {
            if (controller.getCompileStack().isLHS()) {
                storeStaticField(expression);
            } else {
                loadStaticField(expression);
            }
        } else {
            if (controller.getCompileStack().isLHS()) {
                storeThisInstanceField(expression);
            } else {
                loadInstanceField(expression);
            }
        }
        if (controller.getCompileStack().isLHS()) controller.getAssertionWriter().record(expression);
    }

    /**
     * @param fldExp
     */
    public void loadStaticField(FieldExpression fldExp) {
        MethodVisitor mv = controller.getMethodVisitor();
        FieldNode field = fldExp.getField();
        boolean holder = field.isHolder() && !controller.isInClosureConstructor();
        ClassNode type = field.getType();

        String ownerName = (field.getOwner().equals(controller.getClassNode()))
                ? controller.getInternalClassName()
                : BytecodeHelper.getClassInternalName(field.getOwner());
        if (holder) {
            mv.visitFieldInsn(GETSTATIC, ownerName, fldExp.getFieldName(), BytecodeHelper.getTypeDescription(type));
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
            controller.getOperandStack().push(ClassHelper.OBJECT_TYPE);
        } else {
            mv.visitFieldInsn(GETSTATIC, ownerName, fldExp.getFieldName(), BytecodeHelper.getTypeDescription(type));
            controller.getOperandStack().push(field.getType());
        }
    }

    /**
     * RHS instance field. should move most of the code in the BytecodeHelper
     *
     * @param fldExp
     */
    public void loadInstanceField(FieldExpression fldExp) {
        MethodVisitor mv = controller.getMethodVisitor();
        FieldNode field = fldExp.getField();
        boolean holder = field.isHolder() && !controller.isInClosureConstructor();
        ClassNode type = field.getType();
        String ownerName = (field.getOwner().equals(controller.getClassNode()))
                ? controller.getInternalClassName()
                : BytecodeHelper.getClassInternalName(field.getOwner());

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ownerName, fldExp.getFieldName(), BytecodeHelper.getTypeDescription(type));

        if (holder) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
            controller.getOperandStack().push(ClassHelper.OBJECT_TYPE);
        } else {
            controller.getOperandStack().push(field.getType());
        }
    }

    private void storeThisInstanceField(FieldExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        FieldNode field = expression.getField();

        boolean setReferenceFromReference = field.isHolder() && expression.isUseReferenceDirectly();
        String ownerName = (field.getOwner().equals(controller.getClassNode())) ?
                controller.getInternalClassName() : BytecodeHelper.getClassInternalName(field.getOwner());
        OperandStack operandStack = controller.getOperandStack();

        if (setReferenceFromReference) {
            // rhs is ready to use reference, just put it in the field
            mv.visitVarInsn(ALOAD, 0);
            operandStack.push(controller.getClassNode());
            operandStack.swap();
            mv.visitFieldInsn(PUTFIELD, ownerName, field.getName(), BytecodeHelper.getTypeDescription(field.getType()));
        } else if (field.isHolder()){
            // rhs is normal value, set the value in the Reference
            operandStack.doGroovyCast(field.getOriginType());
            operandStack.box();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, ownerName, expression.getFieldName(), BytecodeHelper.getTypeDescription(field.getType()));
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
        } else {
            // rhs is normal value, set normal value
            operandStack.doGroovyCast(field.getOriginType());
            mv.visitVarInsn(ALOAD, 0);
            operandStack.push(controller.getClassNode());
            operandStack.swap();
            mv.visitFieldInsn(PUTFIELD, ownerName, field.getName(), BytecodeHelper.getTypeDescription(field.getType()));
        }
    }

    private void storeStaticField(FieldExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        FieldNode field = expression.getField();

        boolean holder = field.isHolder() && !controller.isInClosureConstructor();
        controller.getOperandStack().doGroovyCast(field);

        String ownerName = (field.getOwner().equals(controller.getClassNode())) ?
                controller.getInternalClassName() : BytecodeHelper.getClassInternalName(field.getOwner());
        if (holder) {
            controller.getOperandStack().box();
            mv.visitFieldInsn(GETSTATIC, ownerName, expression.getFieldName(), BytecodeHelper.getTypeDescription(field.getType()));
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
        } else {
            mv.visitFieldInsn(PUTSTATIC, ownerName, expression.getFieldName(), BytecodeHelper.getTypeDescription(field.getType()));
        }
        controller.getOperandStack().remove(1);
    }

    /**
     * Visits a bare (unqualified) variable expression.
     */
    public void visitVariableExpression(VariableExpression expression) {
        String variableName = expression.getName();

        //-----------------------------------------------------------------------
        // SPECIAL CASES

        // "this" for static methods is the Class instance
        ClassNode classNode = controller.getClassNode();
        //if (controller.isInClosure()) classNode = controller.getOutermostClass();

        if (variableName.equals("this")) {
            if (controller.isStaticMethod() || (!controller.getCompileStack().isImplicitThis() && controller.isStaticContext())) {
                if (controller.isInClosure()) classNode = controller.getOutermostClass();
                visitClassExpression(new ClassExpression(classNode));
            } else {
                loadThis();
            }
            return;
        }

        // "super" also requires special handling
        if (variableName.equals("super")) {
            if (controller.isStaticMethod()) {
                visitClassExpression(new ClassExpression(classNode.getSuperClass()));
            } else {
                loadThis();
            }
            return;
        }

        BytecodeVariable variable = controller.getCompileStack().getVariable(variableName, false);
        if (variable == null) {
            processClassVariable(variableName);
        } else {
            controller.getOperandStack().loadOrStoreVariable(variable, expression.isUseReferenceDirectly());
        }
        if (!controller.getCompileStack().isLHS()) controller.getAssertionWriter().record(expression);
    }

    private void loadThis() {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitVarInsn(ALOAD, 0);
        if (controller.isInClosure() && !controller.getCompileStack().isImplicitThis()) {
            mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "groovy/lang/Closure",
                    "getThisObject",
                    "()Ljava/lang/Object;"
            );
            controller.getOperandStack().push(ClassHelper.OBJECT_TYPE);
//            controller.getOperandStack().push(controller.getClassNode().getOuterClass());
        } else {
            controller.getOperandStack().push(controller.getClassNode());
        }
    }

    private void processClassVariable(String name) {
        if (passingParams && controller.isInScriptBody()) {
            //TODO: check if this part is actually used
            MethodVisitor mv = controller.getMethodVisitor();
            // let's create a ScriptReference to pass into the closure
            mv.visitTypeInsn(NEW, "org/codehaus/groovy/runtime/ScriptReference");
            mv.visitInsn(DUP);

            loadThisOrOwner();
            mv.visitLdcInsn(name);

            mv.visitMethodInsn(
                    INVOKESPECIAL,
                    "org/codehaus/groovy/runtime/ScriptReference",
                    "<init>",
                    "(Lgroovy/lang/Script;Ljava/lang/String;)V");
        } else {
            PropertyExpression pexp = new PropertyExpression(VariableExpression.THIS_EXPRESSION, name);
            pexp.setImplicitThis(true);
            visitPropertyExpression(pexp);
        }
    }

    protected void createInterfaceSyntheticStaticFields() {
        ClassNode icl =  controller.getInterfaceClassLoadingClass();

        if (referencedClasses.isEmpty()) {
            Iterator<InnerClassNode> it = controller.getClassNode().getInnerClasses();
            while(it.hasNext()) {
                InnerClassNode inner = it.next();
                if (inner==icl) {
                    it.remove();
                    return;
                }
            }
            return;
        }

        addInnerClass(icl);
        for (String staticFieldName : referencedClasses.keySet()) {            // generate a field node
            icl.addField(staticFieldName, ACC_STATIC + ACC_SYNTHETIC, ClassHelper.CLASS_Type.getPlainNodeReference(), new ClassExpression(referencedClasses.get(staticFieldName)));
        }
    }

    protected void createSyntheticStaticFields() {
        MethodVisitor mv;
        for (String staticFieldName : referencedClasses.keySet()) {
            // generate a field node
            FieldNode fn = controller.getClassNode().getDeclaredField(staticFieldName);
            if (fn != null) {
                boolean type = fn.getType().redirect() == ClassHelper.CLASS_Type;
                boolean modifiers = fn.getModifiers() == ACC_STATIC + ACC_SYNTHETIC;
                if (!type || !modifiers) {
                    String text = "";
                    if (!type) text = " with wrong type: " + fn.getType() + " (java.lang.Class needed)";
                    if (!modifiers)
                        text = " with wrong modifiers: " + fn.getModifiers() + " (" + (ACC_STATIC + ACC_SYNTHETIC) + " needed)";
                    throwException(
                            "tried to set a static synthetic field " + staticFieldName + " in " + controller.getClassNode().getName() +
                                    " for class resolving, but found already a node of that" +
                                    " name " + text);
                }
            } else {
                cv.visitField(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, staticFieldName, "Ljava/lang/Class;", null, null);
            }

            mv = cv.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "$get$" + staticFieldName,"()Ljava/lang/Class;",null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC,controller.getInternalClassName(),staticFieldName,"Ljava/lang/Class;");
            mv.visitInsn(DUP);
            Label l0 = new Label();
            mv.visitJumpInsn(IFNONNULL,l0);
            mv.visitInsn(POP);
            mv.visitLdcInsn(BytecodeHelper.getClassLoadingTypeDescription(referencedClasses.get(staticFieldName)));
            mv.visitMethodInsn(INVOKESTATIC,controller.getInternalClassName(),"class$","(Ljava/lang/String;)Ljava/lang/Class;");
            mv.visitInsn(DUP);
            mv.visitFieldInsn(PUTSTATIC,controller.getInternalClassName(),staticFieldName,"Ljava/lang/Class;");
            mv.visitLabel(l0);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0,0);
            mv.visitEnd();
        }

        mv =    cv.visitMethod(
                        ACC_STATIC + ACC_SYNTHETIC,
                        "class$",
                        "(Ljava/lang/String;)Ljava/lang/Class;",
                        null,
                        null);
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitInsn(ARETURN);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitTypeInsn(NEW, "java/lang/NoClassDefFoundError");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassNotFoundException", "getMessage", "()Ljava/lang/String;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoClassDefFoundError", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitTryCatchBlock(l0, l2, l2, "java/lang/ClassNotFoundException"); // br using l2 as the 2nd param seems create the right table entry
        mv.visitMaxs(3, 2);
    }

    /**
     * load class object on stack
     */
    public void visitClassExpression(ClassExpression expression) {
        ClassNode type = expression.getType();
        MethodVisitor mv = controller.getMethodVisitor();
        if (BytecodeHelper.isClassLiteralPossible(type) || BytecodeHelper.isSameCompilationUnit(controller.getClassNode(), type)) {
            if (controller.getClassNode().isInterface()) {
                InterfaceHelperClassNode interfaceClassLoadingClass = controller.getInterfaceClassLoadingClass();
                if (BytecodeHelper.isClassLiteralPossible(interfaceClassLoadingClass)) {
                    BytecodeHelper.visitClassLiteral(mv, interfaceClassLoadingClass);
                    controller.getOperandStack().push(ClassHelper.CLASS_Type);
                    return;
                }
            } else {
                BytecodeHelper.visitClassLiteral(mv, type);
                controller.getOperandStack().push(ClassHelper.CLASS_Type);
                return;
            }
        }
        String staticFieldName = getStaticFieldName(type);
        referencedClasses.put(staticFieldName, type);

        String internalClassName = controller.getInternalClassName();
        if (controller.getClassNode().isInterface()) {
            internalClassName = BytecodeHelper.getClassInternalName(controller.getInterfaceClassLoadingClass());
            mv.visitFieldInsn(GETSTATIC, internalClassName, staticFieldName, "Ljava/lang/Class;");
        } else {
            mv.visitMethodInsn(INVOKESTATIC, internalClassName, "$get$" + staticFieldName, "()Ljava/lang/Class;");
        }
        controller.getOperandStack().push(ClassHelper.CLASS_Type);
    }

    public void visitRangeExpression(RangeExpression expression) {
        OperandStack operandStack = controller.getOperandStack();
        expression.getFrom().visit(this);
        operandStack.box();
        expression.getTo().visit(this);
        operandStack.box();
        operandStack.pushBool(expression.isInclusive());

        createRangeMethod.call(controller.getMethodVisitor());
        operandStack.replace(ClassHelper.RANGE_TYPE, 3);
    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
        throw new GroovyBugError("MapEntryExpression should not be visited here");
    }

    public void visitMapExpression(MapExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();

        List entries = expression.getMapEntryExpressions();
        int size = entries.size();
        BytecodeHelper.pushConstant(mv, size * 2);

        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        int i = 0;
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            Object object = iter.next();
            MapEntryExpression entry = (MapEntryExpression) object;

            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i++);
            entry.getKeyExpression().visit(this);
            controller.getOperandStack().box();
            mv.visitInsn(AASTORE);

            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i++);
            entry.getValueExpression().visit(this);
            controller.getOperandStack().box();
            mv.visitInsn(AASTORE);

            controller.getOperandStack().remove(2);
        }
        createMapMethod.call(mv);
        controller.getOperandStack().push(ClassHelper.MAP_TYPE);
    }

    public void visitArgumentlistExpression(ArgumentListExpression ale) {
        if (containsSpreadExpression(ale)) {
            despreadList(ale.getExpressions(), true);
        } else {
            visitTupleExpression(ale, true);
        }
    }

    public void despreadList(List expressions, boolean wrap) {
        ArrayList spreadIndexes = new ArrayList();
        ArrayList spreadExpressions = new ArrayList();
        ArrayList normalArguments = new ArrayList();
        for (int i = 0; i < expressions.size(); i++) {
            Object expr = expressions.get(i);
            if (!(expr instanceof SpreadExpression)) {
                normalArguments.add(expr);
            } else {
                spreadIndexes.add(new ConstantExpression(Integer.valueOf(i - spreadExpressions.size()),true));
                spreadExpressions.add(((SpreadExpression) expr).getExpression());
            }
        }

        //load normal arguments as array
        visitTupleExpression(new ArgumentListExpression(normalArguments), wrap);
        //load spread expressions as array
        (new TupleExpression(spreadExpressions)).visit(this);
        //load insertion index
        (new ArrayExpression(ClassHelper.int_TYPE, spreadIndexes, null)).visit(this);
        controller.getOperandStack().remove(1);
        despreadList.call(controller.getMethodVisitor());
    }

    public void visitTupleExpression(TupleExpression expression) {
        visitTupleExpression(expression, false);
    }

    void visitTupleExpression(TupleExpression expression, boolean useWrapper) {
        MethodVisitor mv = controller.getMethodVisitor();
        int size = expression.getExpressions().size();

        BytecodeHelper.pushConstant(mv, size);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < size; i++) {
            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i);
            Expression argument = expression.getExpression(i);
            argument.visit(this);
            controller.getOperandStack().box();
            if (useWrapper && argument instanceof CastExpression) loadWrapper(argument);

            mv.visitInsn(AASTORE);
            controller.getOperandStack().remove(1);
        }
    }

    public void loadWrapper(Expression argument) {
        MethodVisitor mv = controller.getMethodVisitor();
        ClassNode goalClass = argument.getType();
        visitClassExpression(new ClassExpression(goalClass));
        if (goalClass.isDerivedFromGroovyObject()) {
            createGroovyObjectWrapperMethod.call(mv);
        } else {
            createPojoWrapperMethod.call(mv);
        }
        controller.getOperandStack().remove(1);
    }

    public void visitArrayExpression(ArrayExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        ClassNode elementType = expression.getElementType();
        String arrayTypeName = BytecodeHelper.getClassInternalName(elementType);
        List sizeExpression = expression.getSizeExpression();

        int size = 0;
        int dimensions = 0;
        if (sizeExpression != null) {
            for (Iterator iter = sizeExpression.iterator(); iter.hasNext();) {
                Expression element = (Expression) iter.next();
                if (element == ConstantExpression.EMPTY_EXPRESSION) break;
                dimensions++;
                // let's convert to an int
                element.visit(this);
                controller.getOperandStack().doGroovyCast(ClassHelper.int_TYPE);
            }
            controller.getOperandStack().remove(dimensions);
        } else {
            size = expression.getExpressions().size();
            BytecodeHelper.pushConstant(mv, size);
        }

        int storeIns = AASTORE;
        if (sizeExpression != null) {
            arrayTypeName = BytecodeHelper.getTypeDescription(expression.getType());
            mv.visitMultiANewArrayInsn(arrayTypeName, dimensions);
        } else if (ClassHelper.isPrimitiveType(elementType)) {
            int primType = 0;
            if (elementType == ClassHelper.boolean_TYPE) {
                primType = T_BOOLEAN;
                storeIns = BASTORE;
            } else if (elementType == ClassHelper.char_TYPE) {
                primType = T_CHAR;
                storeIns = CASTORE;
            } else if (elementType == ClassHelper.float_TYPE) {
                primType = T_FLOAT;
                storeIns = FASTORE;
            } else if (elementType == ClassHelper.double_TYPE) {
                primType = T_DOUBLE;
                storeIns = DASTORE;
            } else if (elementType == ClassHelper.byte_TYPE) {
                primType = T_BYTE;
                storeIns = BASTORE;
            } else if (elementType == ClassHelper.short_TYPE) {
                primType = T_SHORT;
                storeIns = SASTORE;
            } else if (elementType == ClassHelper.int_TYPE) {
                primType = T_INT;
                storeIns = IASTORE;
            } else if (elementType == ClassHelper.long_TYPE) {
                primType = T_LONG;
                storeIns = LASTORE;
            }
            mv.visitIntInsn(NEWARRAY, primType);
        } else {
            mv.visitTypeInsn(ANEWARRAY, arrayTypeName);
        }

        for (int i = 0; i < size; i++) {
            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i);
            Expression elementExpression = expression.getExpression(i);
            if (elementExpression == null) {
                ConstantExpression.NULL.visit(this);
            } else {
                ClassNode type = controller.getTypeChooser().resolveType(elementExpression, controller.getClassNode());
                if (!elementType.equals(type)) {
                    visitCastExpression(new CastExpression(elementType, elementExpression, true));
                } else {
                    elementExpression.visit(this);
                }
            }
            mv.visitInsn(storeIns);
            controller.getOperandStack().remove(1);
        }

        controller.getOperandStack().push(expression.getType());
    }

    public void visitClosureListExpression(ClosureListExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        controller.getCompileStack().pushVariableScope(expression.getVariableScope());

        List<Expression> expressions = expression.getExpressions();
        final int size = expressions.size();
        // init declarations
        LinkedList<DeclarationExpression> declarations = new LinkedList<DeclarationExpression>();
        for (int i = 0; i < size; i++) {
            Expression expr = expressions.get(i);
            if (expr instanceof DeclarationExpression) {
                declarations.add((DeclarationExpression) expr);
                DeclarationExpression de = (DeclarationExpression) expr;
                BinaryExpression be = new BinaryExpression(
                        de.getLeftExpression(),
                        de.getOperation(),
                        de.getRightExpression());
                expressions.set(i, be);
                de.setRightExpression(ConstantExpression.NULL);
                visitDeclarationExpression(de);
            }
        }

        LinkedList instructions = new LinkedList();
        BytecodeSequence seq = new BytecodeSequence(instructions);
        BlockStatement bs = new BlockStatement();
        bs.addStatement(seq);
        Parameter closureIndex = new Parameter(ClassHelper.int_TYPE, "__closureIndex");
        ClosureExpression ce = new ClosureExpression(new Parameter[]{closureIndex}, bs);
        ce.setVariableScope(expression.getVariableScope());

        // to keep stack height put a null on stack
        instructions.add(ConstantExpression.NULL);

        // init table
        final Label dflt = new Label();
        final Label tableEnd = new Label();
        final Label[] labels = new Label[size];
        instructions.add(new BytecodeInstruction() {
            public void visit(MethodVisitor mv) {
                mv.visitVarInsn(ILOAD, 1);
                mv.visitTableSwitchInsn(0, size - 1, dflt, labels);
            }
        });

        // visit cases
        for (int i = 0; i < size; i++) {
            final Label label = new Label();
            Object expr = expressions.get(i);
            final boolean isStatement = expr instanceof Statement;
            labels[i] = label;
            instructions.add(new BytecodeInstruction() {
                public void visit(MethodVisitor mv) {
                    mv.visitLabel(label);
                    // expressions will leave a value on stack, statements not
                    // so expressions need to pop the alibi null
                    if (!isStatement) mv.visitInsn(POP);
                }
            });
            instructions.add(expr);
            instructions.add(new BytecodeInstruction() {
                public void visit(MethodVisitor mv) {
                    mv.visitJumpInsn(GOTO, tableEnd);
                }
            });
        }

        // default case
        {
            instructions.add(new BytecodeInstruction() {
                public void visit(MethodVisitor mv) {
                    mv.visitLabel(dflt);
                }
            });
            ConstantExpression text = new ConstantExpression("invalid index for closure");
            ConstructorCallExpression cce = new ConstructorCallExpression(ClassHelper.make(IllegalArgumentException.class), text);
            ThrowStatement ts = new ThrowStatement(cce);
            instructions.add(ts);
        }

        // return
        instructions.add(new BytecodeInstruction() {
            public void visit(MethodVisitor mv) {
                mv.visitLabel(tableEnd);
                mv.visitInsn(ARETURN);
            }
        });

        // load main Closure
        visitClosureExpression(ce);

        // we need later an array to store the curried
        // closures, so we create it here and ave it
        // in a temporary variable
        BytecodeHelper.pushConstant(mv, size);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        int listArrayVar = controller.getCompileStack().defineTemporaryVariable("_listOfClosures", true);

        // add curried versions
        for (int i = 0; i < size; i++) {
            // stack: closure

            // we need to create a curried closure version
            // so we store the type on stack
            mv.visitTypeInsn(NEW, "org/codehaus/groovy/runtime/CurriedClosure");
            // stack: closure, type
            // for a constructor call we need the type two times

            // and the closure after them
            mv.visitInsn(DUP2);
            mv.visitInsn(SWAP);
            // stack: closure,type,type,closure

            // so we can create the curried closure
            mv.visitInsn(ICONST_1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitLdcInsn(i);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/CurriedClosure", "<init>", "(Lgroovy/lang/Closure;[Ljava/lang/Object;)V");
            // stack: closure,curriedClosure

            // we need to save the result
            mv.visitVarInsn(ALOAD, listArrayVar);
            mv.visitInsn(SWAP);
            BytecodeHelper.pushConstant(mv, i);
            mv.visitInsn(SWAP);
            mv.visitInsn(AASTORE);
            // stack: closure
        }

        // we don't need the closure any longer, so remove it
        mv.visitInsn(POP);
        // we load the array and create a list from it
        mv.visitVarInsn(ALOAD, listArrayVar);
        createListMethod.call(mv);

        // remove the temporary variable to keep the
        // stack clean
        controller.getCompileStack().removeVar(listArrayVar);
        controller.getOperandStack().pop();
    }

    public void visitBytecodeSequence(BytecodeSequence bytecodeSequence) {
        MethodVisitor mv = controller.getMethodVisitor();
        List instructions = bytecodeSequence.getInstructions();
        int mark = controller.getOperandStack().getStackLength();
        for (Iterator iterator = instructions.iterator(); iterator.hasNext();) {
            Object part = iterator.next();
            if (part == EmptyExpression.INSTANCE) {
                mv.visitInsn(ACONST_NULL);
            } else if (part instanceof Expression) {
                ((Expression) part).visit(this);
            } else if (part instanceof Statement) {
                Statement stm = (Statement) part;
                stm.visit(this);
                mv.visitInsn(ACONST_NULL);
            } else {
                BytecodeInstruction runner = (BytecodeInstruction) part;
                runner.visit(mv);
            }
        }
        controller.getOperandStack().remove(mark-controller.getOperandStack().getStackLength());
    }

    public void visitListExpression(ListExpression expression) {
        onLineNumber(expression,"ListExpression" );

        int size = expression.getExpressions().size();
        boolean containsSpreadExpression = containsSpreadExpression(expression);
        boolean containsOnlyConstants = !containsSpreadExpression && containsOnlyConstants(expression);
        OperandStack operandStack = controller.getOperandStack();
        if (!containsSpreadExpression) {
            MethodVisitor mv = controller.getMethodVisitor();
            BytecodeHelper.pushConstant(mv, size);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            int maxInit = 1000;
            if (size<maxInit || !containsOnlyConstants) {
                for (int i = 0; i < size; i++) {
                    mv.visitInsn(DUP);
                    BytecodeHelper.pushConstant(mv, i);
                    expression.getExpression(i).visit(this);
                    operandStack.box();
                    mv.visitInsn(AASTORE);
                }
                controller.getOperandStack().remove(size);
            } else {
                List<Expression> expressions = expression.getExpressions();
                List<String> methods = new ArrayList();
                MethodVisitor oldMv = mv;
                int index = 0;
                int methodIndex = 0;
                while (index<size) {
                    methodIndex++;
                    String methodName = "$createListEntry_" + methodIndex;
                    methods.add(methodName);
                    mv = controller.getClassVisitor().visitMethod(
                            ACC_PRIVATE+ACC_STATIC+ACC_SYNTHETIC,
                            methodName,
                            "([Ljava/lang/Object;)V",
                            null, null);
                    controller.setMethodVisitor(mv);
                    mv.visitCode();
                    int methodBlockSize = Math.min(size-index, maxInit);
                    int methodBlockEnd = index + methodBlockSize;
                    for (; index < methodBlockEnd; index++) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitLdcInsn(index);
                        expressions.get(index).visit(this);
                        operandStack.box();
                        mv.visitInsn(AASTORE);
                    }
                    operandStack.remove(methodBlockSize);
                    mv.visitInsn(RETURN);
                    mv.visitMaxs(0,0);
                    mv.visitEnd();
                }
                mv = oldMv;
                controller.setMethodVisitor(mv);
                for (String methodName : methods) {
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESTATIC,controller.getInternalClassName(),methodName,"([Ljava/lang/Object;)V");
                }
            }
        } else {
            despreadList(expression.getExpressions(), false);
        }
        createListMethod.call(controller.getMethodVisitor());
        operandStack.push(ClassHelper.LIST_TYPE);
    }

    private boolean containsOnlyConstants(ListExpression list) {
        for (Expression exp : list.getExpressions()) {
            if (exp instanceof ConstantExpression) continue;
            return false;
        }
        return true;
    }

    public void visitGStringExpression(GStringExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();

        mv.visitTypeInsn(NEW, "org/codehaus/groovy/runtime/GStringImpl");
        mv.visitInsn(DUP);

        int size = expression.getValues().size();
        BytecodeHelper.pushConstant(mv, size);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < size; i++) {
            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i);
            expression.getValue(i).visit(this);
            controller.getOperandStack().box();
            mv.visitInsn(AASTORE);
        }
        controller.getOperandStack().remove(size);

        List strings = expression.getStrings();
        size = strings.size();
        BytecodeHelper.pushConstant(mv, size);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/String");

        for (int i = 0; i < size; i++) {
            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i);
            controller.getOperandStack().pushConstant((ConstantExpression) strings.get(i));
            controller.getOperandStack().box();
            mv.visitInsn(AASTORE);
        }
        controller.getOperandStack().remove(size);

        mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/GStringImpl", "<init>", "([Ljava/lang/Object;[Ljava/lang/String;)V");
        controller.getOperandStack().push(ClassHelper.GSTRING_TYPE);
    }

    /**
     * Note: ignore it. Annotation generation needs the current visitor.
     */
    public void visitAnnotations(AnnotatedNode node) {
    }

    private void visitAnnotations(AnnotatedNode targetNode, Object visitor) {
        for (AnnotationNode an : targetNode.getAnnotations()) {
            // skip built-in properties
            if (an.isBuiltIn()) continue;
            if (an.hasSourceRetention()) continue;

            AnnotationVisitor av = getAnnotationVisitor(targetNode, an, visitor);
            visitAnnotationAttributes(an, av);
            av.visitEnd();
        }
    }

    private void visitParameterAnnotations(Parameter parameter, int paramNumber, MethodVisitor mv) {
        for (AnnotationNode an : parameter.getAnnotations()) {
            // skip built-in properties
            if (an.isBuiltIn()) continue;
            if (an.hasSourceRetention()) continue;

            final String annotationDescriptor = BytecodeHelper.getTypeDescription(an.getClassNode());
            AnnotationVisitor av = mv.visitParameterAnnotation(paramNumber, annotationDescriptor, an.hasRuntimeRetention());
            visitAnnotationAttributes(an, av);
            av.visitEnd();
        }
    }

    private AnnotationVisitor getAnnotationVisitor(AnnotatedNode targetNode, AnnotationNode an, Object visitor) {
        final String annotationDescriptor = BytecodeHelper.getTypeDescription(an.getClassNode());
        if (targetNode instanceof MethodNode) {
            return ((MethodVisitor) visitor).visitAnnotation(annotationDescriptor, an.hasRuntimeRetention());
        } else if (targetNode instanceof FieldNode) {
            return ((FieldVisitor) visitor).visitAnnotation(annotationDescriptor, an.hasRuntimeRetention());
        } else if (targetNode instanceof ClassNode) {
            return ((ClassVisitor) visitor).visitAnnotation(annotationDescriptor, an.hasRuntimeRetention());
        }
        throwException("Cannot create an AnnotationVisitor. Please report Groovy bug");
        return null;
    }

    /**
     * Generate the annotation attributes.
     * @param an the node with an annotation
     * @param av the visitor to use
     */
    private void visitAnnotationAttributes(AnnotationNode an, AnnotationVisitor av) {
        Map<String, Object> constantAttrs = new HashMap<String, Object>();
        Map<String, PropertyExpression> enumAttrs = new HashMap<String, PropertyExpression>();
        Map<String, Object> atAttrs = new HashMap<String, Object>();
        Map<String, ListExpression> arrayAttrs = new HashMap<String, ListExpression>();

        for (String name : an.getMembers().keySet()) {
            Expression expr = an.getMember(name);
            if (expr instanceof AnnotationConstantExpression) {
                atAttrs.put(name, ((AnnotationConstantExpression) expr).getValue());
            } else if (expr instanceof ConstantExpression) {
                constantAttrs.put(name, ((ConstantExpression) expr).getValue());
            } else if (expr instanceof ClassExpression) {
                constantAttrs.put(name,
                        Type.getType(BytecodeHelper.getTypeDescription((expr.getType()))));
            } else if (expr instanceof PropertyExpression) {
                enumAttrs.put(name, (PropertyExpression) expr);
            } else if (expr instanceof ListExpression) {
                arrayAttrs.put(name, (ListExpression) expr);
            } else if (expr instanceof ClosureExpression) {
                ClassNode closureClass = controller.getClosureWriter().getOrAddClosureClass((ClosureExpression) expr, ACC_PUBLIC);
                constantAttrs.put(name,
                        Type.getType(BytecodeHelper.getTypeDescription(closureClass)));
            }
        }

        for (Map.Entry entry : constantAttrs.entrySet()) {
            av.visit((String) entry.getKey(), entry.getValue());
        }
        for (Map.Entry entry : enumAttrs.entrySet()) {
            PropertyExpression propExp = (PropertyExpression) entry.getValue();
            av.visitEnum((String) entry.getKey(),
                    BytecodeHelper.getTypeDescription(propExp.getObjectExpression().getType()),
                    String.valueOf(((ConstantExpression) propExp.getProperty()).getValue()));
        }
        for (Map.Entry entry : atAttrs.entrySet()) {
            AnnotationNode atNode = (AnnotationNode) entry.getValue();
            AnnotationVisitor av2 = av.visitAnnotation((String) entry.getKey(),
                    BytecodeHelper.getTypeDescription(atNode.getClassNode()));
            visitAnnotationAttributes(atNode, av2);
            av2.visitEnd();
        }
        visitArrayAttributes(an, arrayAttrs, av);
    }

    private void visitArrayAttributes(AnnotationNode an, Map<String, ListExpression> arrayAttr, AnnotationVisitor av) {
        if (arrayAttr.isEmpty()) return;
        for (Map.Entry entry : arrayAttr.entrySet()) {
            AnnotationVisitor av2 = av.visitArray((String) entry.getKey());
            List<Expression> values = ((ListExpression) entry.getValue()).getExpressions();
            if (!values.isEmpty()) {
                int arrayElementType = determineCommonArrayType(values);
                for (Expression exprChild : values) {
                    visitAnnotationArrayElement(exprChild, arrayElementType, av2);
                }
            }
            av2.visitEnd();
        }
    }

    private int determineCommonArrayType(List values) {
        Expression expr = (Expression) values.get(0);
        int arrayElementType = -1;
        if (expr instanceof AnnotationConstantExpression) {
            arrayElementType = 1;
        } else if (expr instanceof ConstantExpression) {
            arrayElementType = 2;
        } else if (expr instanceof ClassExpression) {
            arrayElementType = 3;
        } else if (expr instanceof PropertyExpression) {
            arrayElementType = 4;
        }
        return arrayElementType;
    }

    private void visitAnnotationArrayElement(Expression expr, int arrayElementType, AnnotationVisitor av) {
        switch (arrayElementType) {
            case 1:
                AnnotationNode atAttr = (AnnotationNode) ((AnnotationConstantExpression) expr).getValue();
                AnnotationVisitor av2 = av.visitAnnotation(null, BytecodeHelper.getTypeDescription(atAttr.getClassNode()));
                visitAnnotationAttributes(atAttr, av2);
                av2.visitEnd();
                break;
            case 2:
                av.visit(null, ((ConstantExpression) expr).getValue());
                break;
            case 3:
                av.visit(null, Type.getType(BytecodeHelper.getTypeDescription(expr.getType())));
                break;
            case 4:
                PropertyExpression propExpr = (PropertyExpression) expr;
                av.visitEnum(null,
                        BytecodeHelper.getTypeDescription(propExpr.getObjectExpression().getType()),
                        String.valueOf(((ConstantExpression) propExpr.getProperty()).getValue()));
                break;
        }
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    public void visitBytecodeExpression(BytecodeExpression cle) {
        cle.visit(controller.getMethodVisitor());
        controller.getOperandStack().push(cle.getType());
    }

    public static boolean isThisExpression(Expression expression) {
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

    public void onLineNumber(ASTNode statement, String message) {
        MethodVisitor mv = controller.getMethodVisitor();

        if (statement==null) return;
        int line = statement.getLineNumber();
        this.currentASTNode = statement;

        if (line < 0) return;
        if (!ASM_DEBUG && line==controller.getLineNumber()) return;

        controller.setLineNumber(line);
        if (mv != null) {
            Label l = new Label();
            mv.visitLabel(l);
            mv.visitLineNumber(line, l);
        }
    }

    private boolean isInnerClass() {
        return controller.getClassNode() instanceof InnerClassNode;
    }

    protected CompileUnit getCompileUnit() {
        CompileUnit answer = controller.getClassNode().getCompileUnit();
        if (answer == null) {
            answer = context.getCompileUnit();
        }
        return answer;
    }

    public boolean addInnerClass(ClassNode innerClass) {
        ModuleNode mn = controller.getClassNode().getModule();
        innerClass.setModule(mn);
        mn.getUnit().addGeneratedInnerClass((InnerClassNode)innerClass);
        return innerClasses.add(innerClass);
    }
}

/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.classgen;

import groovy.lang.GroovyRuntimeException;
import groovy.transform.Sealed;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.InterfaceHelperClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.RecordComponentNode;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
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
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.BytecodeVariable;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.MethodCaller;
import org.codehaus.groovy.classgen.asm.MethodCallerMultiAdapter;
import org.codehaus.groovy.classgen.asm.MopWriter;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.OptimizingStatementWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.classgen.asm.WriterControllerFactory;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static org.apache.groovy.ast.tools.ClassNodeUtils.getField;
import static org.apache.groovy.ast.tools.ExpressionUtils.isNullConstant;
import static org.apache.groovy.ast.tools.ExpressionUtils.isSuperExpression;
import static org.codehaus.groovy.ast.ClassHelper.isClassType;
import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveByte;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveChar;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveDouble;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveFloat;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveInt;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveLong;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveShort;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.isStringType;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.maybeFallsThrough;
import static org.codehaus.groovy.ast.tools.GeneralUtils.thisPropX;
import static org.codehaus.groovy.ast.tools.ParameterUtils.isVargs;
import static org.codehaus.groovy.transform.SealedASTTransformation.sealedNative;
import static org.codehaus.groovy.transform.SealedASTTransformation.sealedSkipAnnotation;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACC_VARARGS;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BASTORE;
import static org.objectweb.asm.Opcodes.CASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.FASTORE;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.LASTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SASTORE;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.T_BOOLEAN;
import static org.objectweb.asm.Opcodes.T_BYTE;
import static org.objectweb.asm.Opcodes.T_CHAR;
import static org.objectweb.asm.Opcodes.T_DOUBLE;
import static org.objectweb.asm.Opcodes.T_FLOAT;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.T_LONG;
import static org.objectweb.asm.Opcodes.T_SHORT;
import static org.objectweb.asm.Opcodes.V_PREVIEW;
import static org.objectweb.asm.TypeReference.CLASS_TYPE_PARAMETER;
import static org.objectweb.asm.TypeReference.CLASS_TYPE_PARAMETER_BOUND;
import static org.objectweb.asm.TypeReference.FIELD;
import static org.objectweb.asm.TypeReference.METHOD_RECEIVER;
import static org.objectweb.asm.TypeReference.METHOD_RETURN;
import static org.objectweb.asm.TypeReference.METHOD_TYPE_PARAMETER;
import static org.objectweb.asm.TypeReference.METHOD_TYPE_PARAMETER_BOUND;
import static org.objectweb.asm.TypeReference.newExceptionReference;
import static org.objectweb.asm.TypeReference.newFormalParameterReference;
import static org.objectweb.asm.TypeReference.newSuperTypeReference;
import static org.objectweb.asm.TypeReference.newTypeParameterBoundReference;
import static org.objectweb.asm.TypeReference.newTypeParameterReference;
import static org.objectweb.asm.TypeReference.newTypeReference;

/**
 * Generates Java class versions of Groovy classes using ASM.
 */
public class AsmClassGenerator extends ClassGenerator {

    // fields
    public  static final MethodCallerMultiAdapter setField = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setField", false, false);
    public  static final MethodCallerMultiAdapter getField = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getField", false, false);
    private static final MethodCallerMultiAdapter setFieldOnSuper = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setFieldOnSuper", false, false);
    private static final MethodCallerMultiAdapter getFieldOnSuper = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getFieldOnSuper", false, false);
    public  static final MethodCallerMultiAdapter setGroovyObjectField = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setGroovyObjectField", false, false);
    public  static final MethodCallerMultiAdapter getGroovyObjectField = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getGroovyObjectField", false, false);

    // properties
    public  static final MethodCallerMultiAdapter setProperty = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setProperty", false, false);
    private static final MethodCallerMultiAdapter getProperty = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getProperty", false, false);
    private static final MethodCallerMultiAdapter setPropertyOnSuper = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setPropertyOnSuper", false, false);
    private static final MethodCallerMultiAdapter getPropertyOnSuper = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getPropertyOnSuper", false, false);
    private static final MethodCallerMultiAdapter setGroovyObjectProperty = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "setGroovyObjectProperty", false, false);
    private static final MethodCallerMultiAdapter getGroovyObjectProperty = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "getGroovyObjectProperty", false, false);

    // spread expressions
    private static final MethodCaller spreadMap = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "spreadMap");
    private static final MethodCaller despreadList = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "despreadList");

    // type conversions
    private static final MethodCaller createMapMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createMap");
    private static final MethodCaller createListMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createList");
    // The 3-parameter version of createRange is kept in for backwards compatibility, so we need to specify the
    // parameter count here
    private static final MethodCaller createRangeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createRange", 4);
    private static final MethodCaller createPojoWrapperMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createPojoWrapper");
    private static final MethodCaller createGroovyObjectWrapperMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "createGroovyObjectWrapper");

    private final Map<String,ClassNode> referencedClasses = new HashMap<>();
    private boolean passingParams;

    public static final boolean CREATE_DEBUG_INFO = true;
    public static final boolean CREATE_LINE_NUMBER_INFO = true;
    public static final boolean ASM_DEBUG = false; // add marker in the bytecode to show source-bytecode relationship
    public static final String MINIMUM_BYTECODE_VERSION = "_MINIMUM_BYTECODE_VERSION";

    private WriterController controller;
    private ASTNode currentASTNode;

    private final SourceUnit source;
    private final GeneratorContext context;
    private ClassVisitor classVisitor;
    private final String sourceFile;

    public AsmClassGenerator(final SourceUnit source, final GeneratorContext context, final ClassVisitor classVisitor, final String sourceFile) {
        this.source = source;
        this.context = context;
        this.classVisitor = classVisitor;
        this.sourceFile = sourceFile;
    }

    @Override
    public SourceUnit getSourceUnit() {
        return source;
    }

    public WriterController getController() {
        return controller;
    }

    // GroovyClassVisitor interface
    //--------------------------------------------------------------------------

    @Override
    public void visitClass(final ClassNode classNode) {
        referencedClasses.clear();

        WriterControllerFactory factory = classNode.getNodeMetaData(WriterControllerFactory.class);
        controller = new WriterController();
        if (factory != null) {
            controller = factory.makeController(controller);
        }
        controller.init(this, context, classVisitor, classNode);
        if (controller.shouldOptimizeForInt() || factory != null) {
            OptimizingStatementWriter.setNodeMeta(controller.getTypeChooser(), classNode);
        }

        classVisitor = controller.getClassVisitor();
        try {
            int bytecodeVersion = controller.getBytecodeVersion();
            Object min = classNode.getNodeMetaData(MINIMUM_BYTECODE_VERSION);
            if (min instanceof Integer) {
                int minVersion = (int) min;
                if ((bytecodeVersion ^ V_PREVIEW) < minVersion) {
                    bytecodeVersion = minVersion;
                }
            }
            classVisitor.visit(
                    bytecodeVersion,
                    adjustedClassModifiersForClassWriting(classNode),
                    controller.getInternalClassName(),
                    BytecodeHelper.getGenericsSignature(classNode),
                    controller.getInternalBaseClassName(),
                    BytecodeHelper.getClassInternalNames(classNode.getInterfaces())
            );
            classVisitor.visitSource(sourceFile, null);
            if (classNode instanceof InnerClassNode && !(classNode instanceof InterfaceHelperClassNode)) {
                makeInnerClassEntry(classNode); // GROOVY-4649, et al.

                ClassNode nestHost = controller.getOutermostClass(); // GROOVY-10687
                classVisitor.visitNestHost(BytecodeHelper.getClassInternalName(nestHost));

                MethodNode enclosingMethod = classNode.getEnclosingMethod();
                if (enclosingMethod != null) {
                    classVisitor.visitOuterClass(
                            BytecodeHelper.getClassInternalName(classNode.getOuterClass()),
                            enclosingMethod.getName(), BytecodeHelper.getMethodDescriptor(enclosingMethod));
                }
            }
            if (classNode.getName().endsWith("package-info")) {
                PackageNode packageNode = classNode.getPackage();
                if (packageNode != null) {
                    // pull them out of package node but treat them like they were on class node
                    visitAnnotations(classNode, packageNode, classVisitor);
                }
            } else {
                visitAnnotations(classNode, classVisitor);
                visitTypeParameters(classNode, classVisitor);
                visitType(classNode.getUnresolvedSuperClass(), classVisitor, newSuperTypeReference(-1), "", true);
                ClassNode[] interfaces = classNode.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    visitType(interfaces[i], classVisitor, newSuperTypeReference(i), "", true);
                }
                if (classNode.isInterface()) {
                    String outerClassName = classNode.getName();
                    String name = outerClassName + "$" + context.getNextInnerClassIdx();
                    controller.setInterfaceClassLoadingClass(
                            new InterfaceHelperClassNode(
                                    Optional.ofNullable(classNode.getOuterClass()).orElse(classNode),
                                    name, ACC_SUPER | ACC_STATIC | ACC_SYNTHETIC, ClassHelper.OBJECT_TYPE,
                                    controller.getCallSiteWriter().getCallSites()
                            )
                    );
                    super.visitClass(classNode);
                    createInterfaceSyntheticStaticFields();
                } else {
                    super.visitClass(classNode);
                    MopWriter.Factory mopWriterFactory = classNode.getNodeMetaData(MopWriter.Factory.class);
                    if (mopWriterFactory == null) {
                        mopWriterFactory = MopWriter.FACTORY;
                    }
                    MopWriter mopWriter = mopWriterFactory.create(controller);
                    mopWriter.createMopMethods();
                    controller.getCallSiteWriter().generateCallSiteArray();
                    createSyntheticStaticFields();
                }
            }
            // GROOVY-10687
            if (classNode.getOuterClass() == null && classNode.getInnerClasses().hasNext()) {
                makeNestMatesEntries(classNode);
            }
            // GROOVY-4649, GROOVY-6750, GROOVY-6808
            for (Iterator<InnerClassNode> it = classNode.getInnerClasses(); it.hasNext(); ) {
                makeInnerClassEntry(it.next());
            }
            if (sealedNative(classNode)) {
                for (ClassNode sub : classNode.getPermittedSubclasses()) {
                    classVisitor.visitPermittedSubclass(BytecodeHelper.getClassInternalName(sub));
                }
            }
            if (classNode.isRecord()) {
                visitRecordComponents(classNode);
            }
            classVisitor.visitEnd();
        } catch (GroovyRuntimeException e) {
            e.setModule(classNode.getModule());
            throw e;
        } catch (NullPointerException | NegativeArraySizeException e) {
            String m = e.getClass().getSimpleName() + " while processing " + sourceFile;
            GroovyRuntimeException gre = new GroovyRuntimeException(m, e);
            gre.setModule(classNode.getModule());
            throw gre;
        }
    }

    private void visitRecordComponents(final ClassNode classNode) {
        for (RecordComponentNode recordComponent : classNode.getRecordComponents()) {
            ClassNode type = recordComponent.getType();
            RecordComponentVisitor visitor = classVisitor.visitRecordComponent(
                    recordComponent.getName(),
                    BytecodeHelper.getTypeDescription(type),
                    BytecodeHelper.getTypeGenericsSignature(type));

            visitAnnotations(recordComponent, visitor);

            // the int encoded value of the type reference is ALWAYS `318767104`
            // TODO Get the magic number `318767104` via `TypeReference.newXXX()`
            TypeReference typeRef = new TypeReference(318767104);

            visitTypeAnnotations(type, visitor, typeRef, "", true);

            visitor.visitEnd();
        }
    }

    private void maybeInnerClassEntry(final ClassNode classNode) {
        if (classNode.getOuterClass() != null) makeInnerClassEntry(classNode);
    }

    private void makeInnerClassEntry(final ClassNode innerClass) {
        ClassNode outerClass = innerClass.getOuterClass();
        maybeInnerClassEntry(outerClass); // GROOVY-9842

        String innerClassName = innerClass.getName();
        String innerClassInternalName = BytecodeHelper.getClassInternalName(innerClassName);
        {
            int index = innerClassName.lastIndexOf('$');
            if (index >= 0) innerClassName = innerClassName.substring(index + 1);
        }
        String outerClassInternalName;
        if (innerClass.getEnclosingMethod() == null) {
            outerClassInternalName = BytecodeHelper.getClassInternalName(outerClass.getName());
        } else {
            outerClassInternalName = null; // local inner classes don't specify the outer class name
            if (innerClass instanceof InnerClassNode && ((InnerClassNode) innerClass).isAnonymous()) innerClassName = null;
        }

        int modifiers = adjustedClassModifiersForInnerClassTable(innerClass);
        classVisitor.visitInnerClass(innerClassInternalName, outerClassInternalName, innerClassName, modifiers);
    }

    private void makeNestMatesEntries(final ClassNode classNode) {
        for (Iterator<InnerClassNode> it = classNode.getInnerClasses(); it.hasNext(); ) {
            ClassNode innerClass = it.next();
            classVisitor.visitNestMember(BytecodeHelper.getClassInternalName(innerClass));
            makeNestMatesEntries(innerClass);
        }
    }

    /*
     * See http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.6-300-D.2-5
     * for what flags are allowed depending on the fact we are writing the inner class table
     * or the class itself
     */
    private static int adjustedClassModifiersForInnerClassTable(final ClassNode classNode) {
        return fixInterfaceModifiers(classNode, classNode.getModifiers()) & ~ACC_SUPER;
    }

    private static int fixInterfaceModifiers(final ClassNode classNode, int modifiers) {
        // (JLS §9.1.1.1). Such a class file must not have its ACC_FINAL, ACC_SUPER or ACC_ENUM flags set.
        if (classNode.isInterface()) {
            modifiers = modifiers & ~ACC_ENUM;
            modifiers = modifiers & ~ACC_FINAL;
        }
        return modifiers;
    }

    private static int fixInnerClassModifiers(final ClassNode classNode, int modifiers) {
        // on the inner class node itself, private/protected are not allowed
        if (classNode.getOuterClass() != null) {
            if ((modifiers & ACC_PRIVATE) != 0) {
                // GROOVY-6357: The JVM does not allow private modifier on inner classes: should be package private
                modifiers = (modifiers & ~ACC_PRIVATE);
            }
            if ((modifiers & ACC_PROTECTED) != 0) {
                // GROOVY-6357: Following Java's behavior for protected modifier on inner classes: should be public
                modifiers = (modifiers & ~ACC_PROTECTED) | ACC_PUBLIC;
            }
        }
        return modifiers;
    }

    /*
     * Classes but not interfaces should have ACC_SUPER set
     * See http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.6-300-D.2-5
     * for what flags are allowed depending on the fact we are writing the inner class table
     * or the class itself
     */
    private static int adjustedClassModifiersForClassWriting(final ClassNode classNode) {
        int modifiers = classNode.getModifiers();
        boolean needsSuper = !classNode.isInterface();
        modifiers = needsSuper ? modifiers | ACC_SUPER : modifiers & ~ACC_SUPER;
        // eliminate static
        modifiers = modifiers & ~ACC_STATIC;
        modifiers = fixInnerClassModifiers(classNode, modifiers);
        modifiers = fixInterfaceModifiers(classNode, modifiers);
        return modifiers;
    }

    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        Parameter[] parameters = node.getParameters();
        Parameter   receiver = null; // JSR 308 "this" parameter
        if (parameters.length > 0 && parameters[0].isReceiver()) {
            receiver = parameters[0]; // non-static method or inner class ctor
            parameters = Arrays.copyOfRange(parameters, 1, parameters.length);
        }
        MethodVisitor mv = classVisitor.visitMethod(
                node.getModifiers() | (isVargs(parameters) ? ACC_VARARGS : 0), node.getName(),
                BytecodeHelper.getMethodDescriptor(node.getReturnType(), parameters),
                BytecodeHelper.getGenericsMethodSignature(node),
                buildExceptions(node.getExceptions()));
        controller.setMethodVisitor(mv);
        controller.resetLineNumber();

        visitAnnotations(node, mv);
        visitTypeParameters(node, mv);
        if (!node.isConstructor() || node.getReturnType().isAnnotated()) {
            visitType(node.getReturnType(), mv, newTypeReference(METHOD_RETURN), "", true);
        }
        if (receiver != null) {
            visitTypeAnnotations(receiver.getType(), mv, newTypeReference(METHOD_RECEIVER), "", true);
        }
        // add parameter names to the MethodVisitor (JDK8+)
        if (Optional.ofNullable(controller.getClassNode().getCompileUnit())
                .orElseGet(context::getCompileUnit).getConfig().getParameters()) {
            for (Parameter parameter : parameters) {
                mv.visitParameter(parameter.getName(), parameter.getModifiers());
            }
        }
        for (int i = 0, j = 0, n = parameters.length; i < n; i += 1) {
            if (parameters[i].isImplicit()) continue;
            visitParameterAnnotations(parameters[i], i, mv);
            ClassNode paramType = parameters[i].getType();
            if (paramType.isGenericsPlaceHolder()) {
                visitTypeAnnotations(paramType, mv, newFormalParameterReference(j++), "", true);
            } else {
                visitType(paramType, mv, newFormalParameterReference(j++), "", true);
            }
        }
        if (node.getExceptions() != null) {
            for (int i = 0, n = node.getExceptions().length; i < n; i += 1) {
                visitType(node.getExceptions()[i], mv, newExceptionReference(i), "", true);
            }
        }

        if (controller.getClassNode().isAnnotationDefinition() && !node.isStaticConstructor()) {
            visitAnnotationDefault(node, mv);
        } else if (!node.isAbstract()) {
            mv.visitCode();

            Statement code = node.getCode();
            BytecodeInstruction instruction; // fast path for getters, setters, etc.
            if (code instanceof BytecodeSequence && (instruction = ((BytecodeSequence) code).getBytecodeInstruction()) != null) {
               instruction.visit(mv);
            } else {
                visitStdMethod(node, isConstructor, parameters, code);
            }

            try {
                mv.visitMaxs(0, 0);
            } catch (Exception e) {
                Writer writer = null;
                if (mv instanceof TraceMethodVisitor) {
                    writer = new StringBuilderWriter();
                    PrintWriter p = new PrintWriter(writer);
                    ((TraceMethodVisitor) mv).p.print(p);
                    p.flush();
                }
                StringBuilder message = new StringBuilder(64);
                message.append("ASM reporting processing error for ");
                message.append(controller.getClassNode().toString(false)).append('#').append(node.getName());
                message.append(" with signature ").append(node.getTypeDescriptor());
                message.append(" in ").append(sourceFile).append(':').append(node.getLineNumber());
                if (writer != null) {
                    message.append("\nLast known generated bytecode in last generated method or constructor:\n");
                    message.append(writer);
                }
                throw new GroovyRuntimeException(message.toString(), e);
            }
        }
        mv.visitEnd();
    }

    private void visitStdMethod(final MethodNode node, final boolean isConstructor, final Parameter[] parameters, final Statement code) {
        controller.getCompileStack().init(node.getVariableScope(), parameters);
        controller.getCallSiteWriter().makeSiteEntry();

        ClassNode cn = controller.getClassNode();
        MethodVisitor mv = controller.getMethodVisitor();
        if (isConstructor && (code == null || !((ConstructorNode) node).firstStatementIsSpecialConstructorCall())) {
            boolean hasCallToSuper = false;
            if (code != null && cn.getOuterClass() != null) {
                // GROOVY-4471: if the class is an inner class node, there are chances that
                // the call to super is already added so we must ensure not to add it twice
                if (code instanceof BlockStatement) {
                    hasCallToSuper = ((BlockStatement) code).getStatements().stream()
                        .map(statement -> statement instanceof ExpressionStatement ? ((ExpressionStatement) statement).getExpression() : null)
                        .anyMatch(expression -> expression instanceof ConstructorCallExpression && ((ConstructorCallExpression) expression).isSuperCall());
                }
            }
            if (!hasCallToSuper) {
                if (code != null) { // GROOVY-9373
                    controller.visitLineNumber(code.getLineNumber());
                }
                if (cn.getSuperClass().getDeclaredConstructor(Parameter.EMPTY_ARRAY) == null) { ASTNode where = node; // GROOVY-9857
                    String error = "Implicit super constructor " + cn.getSuperClass().getNameWithoutPackage() + "() is undefined";
                    if (node.getLineNumber() > 0) error += ". Must explicitly invoke another constructor.";
                    else {
                        error += " for generated constructor. Must define an explicit constructor.";
                        where = cn;
                    }
                    addError(error, where);
                }
                // add call to "super()"
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, controller.getInternalBaseClassName(), "<init>", "()V", false);
            }
        }

        if (code != null) {
            code.visit(this);
        }

        if (code == null || maybeFallsThrough(code)) {
            if (code != null) { // GROOVY-7647, GROOVY-9373
                controller.visitLineNumber(code.getLastLineNumber());
            }
            if (node.isVoidMethod()) {
                mv.visitInsn(RETURN);
            } else {
                ClassNode type = node.getReturnType();
                if (isPrimitiveType(type)) {
                    mv.visitLdcInsn(Integer.valueOf(0));
                    OperandStack operandStack = controller.getOperandStack();
                    operandStack.push(ClassHelper.int_TYPE);
                    operandStack.doGroovyCast(type);
                    BytecodeHelper.doReturn(mv, type);
                    operandStack.remove(1);
                } else {
                    mv.visitInsn(ACONST_NULL);
                    BytecodeHelper.doReturn(mv, type);
                }
            }
        }

        controller.getCompileStack().clear();
    }

    private void visitAnnotationDefaultExpression(final AnnotationVisitor av, final ClassNode type, final Expression exp) {
        if (exp instanceof ClosureExpression) {
            ClassNode closureClass = controller.getClosureWriter().getOrAddClosureClass((ClosureExpression) exp, ACC_PUBLIC);
            Type t = Type.getType(BytecodeHelper.getTypeDescription(closureClass));
            av.visit(null, t);
        } else if (type.isArray()) {
            AnnotationVisitor avl = av.visitArray(null);
            ClassNode componentType = type.getComponentType();
            if (exp instanceof ListExpression) {
                ListExpression list = (ListExpression) exp;
                for (Expression lExp : list.getExpressions()) {
                    visitAnnotationDefaultExpression(avl, componentType, lExp);
                }
            } else {
                visitAnnotationDefaultExpression(avl, componentType, exp);
            }
        } else if (isPrimitiveType(type) || isStringType(type)) {
            ConstantExpression constExp = (ConstantExpression) exp;
            av.visit(null, constExp.getValue());
        } else if (isClassType(type)) {
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
            visitAnnotationAttributes(value, avc);
        } else {
            throw new GroovyBugError("unexpected annotation type " + type.getName());
        }
        av.visitEnd();
    }

    private void visitAnnotationDefault(final MethodNode node, final MethodVisitor mv) {
        if (!node.hasAnnotationDefault()) return;
        Expression exp = ((ReturnStatement) node.getCode()).getExpression();
        AnnotationVisitor av = mv.visitAnnotationDefault();
        visitAnnotationDefaultExpression(av,node.getReturnType(),exp);
    }

    @Override
    public void visitConstructor(final ConstructorNode node) {
        controller.setConstructorNode(node);
        super.visitConstructor(node);
    }

    @Override
    public void visitMethod(final MethodNode node) {
        controller.setMethodNode(node);
        super.visitMethod(node);
    }

    @Override
    public void visitField(final FieldNode fieldNode) {
        onLineNumber(fieldNode, "visitField: " + fieldNode.getName());
        ClassNode t = fieldNode.getType();
        String signature = BytecodeHelper.getGenericsBounds(t);

        Expression initialValueExpression = fieldNode.getInitialValueExpression();
        ConstantExpression cexp = initialValueExpression instanceof ConstantExpression? (ConstantExpression) initialValueExpression :null;
        if (cexp!=null) {
            cexp = Verifier.transformToPrimitiveConstantIfPossible(cexp);
        }
        Object value = cexp != null && ClassHelper.isStaticConstantInitializerType(cexp.getType())
                && cexp.getType().equals(t) && fieldNode.isStatic() && fieldNode.isFinal()
                ? cexp.getValue() : null; // GROOVY-5150
        if (value != null) {
            // byte, char and short require an extra cast
            if (isPrimitiveByte(t) || isPrimitiveShort(t)) {
                value = ((Number) value).intValue();
            } else if (isPrimitiveChar(t)) {
                value = Integer.valueOf((Character)value);
            }
        }
        FieldVisitor fv = classVisitor.visitField(
                fieldNode.getModifiers(),
                fieldNode.getName(),
                BytecodeHelper.getTypeDescription(t),
                signature,
                value);
        visitAnnotations(fieldNode, fv);
        visitType(fieldNode.getType(), fv, newTypeReference(FIELD), "", true);
        fv.visitEnd();
    }

    @Override
    public void visitProperty(final PropertyNode statement) {
        // the verifier created the field and the setter/getter methods, so here is
        // not really something to do
        onLineNumber(statement, "visitProperty:" + statement.getField().getName());
        controller.setMethodNode(null);
    }

    // GroovyCodeVisitor interface
    //--------------------------------------------------------------------------

    // Statements
    //--------------------------------------------------------------------------

    @Override
    protected void visitStatement(final Statement statement) {
        throw new GroovyBugError("visitStatement should not be visited here.");
    }

    @Override
    public void visitBlockStatement(final BlockStatement statement) {
        controller.getStatementWriter().writeBlockStatement(statement);
    }

    @Override
    public void visitForLoop(final ForStatement statement) {
        controller.getStatementWriter().writeForStatement(statement);
    }

    @Override
    public void visitWhileLoop(final WhileStatement statement) {
        controller.getStatementWriter().writeWhileLoop(statement);
    }

    @Override
    public void visitDoWhileLoop(final DoWhileStatement statement) {
        controller.getStatementWriter().writeDoWhileLoop(statement);
    }

    @Override
    public void visitIfElse(final IfStatement statement) {
        controller.getStatementWriter().writeIfElse(statement);
    }

    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        controller.getStatementWriter().writeAssert(statement);
    }

    @Override
    public void visitTryCatchFinally(final TryCatchStatement statement) {
        controller.getStatementWriter().writeTryCatchFinally(statement);
    }

    @Override
    public void visitCatchStatement(final CatchStatement statement) {
        maybeInnerClassEntry(statement.getExceptionType());
        statement.getCode().visit(this);
    }

    @Override
    public void visitSwitch(final SwitchStatement statement) {
        controller.getStatementWriter().writeSwitch(statement);
    }

    @Override
    public void visitCaseStatement(final CaseStatement statement) {
    }

    @Override
    public void visitBreakStatement(final BreakStatement statement) {
        controller.getStatementWriter().writeBreak(statement);
    }

    @Override
    public void visitContinueStatement(final ContinueStatement statement) {
        controller.getStatementWriter().writeContinue(statement);
    }

    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement statement) {
        controller.getStatementWriter().writeSynchronized(statement);
    }

    @Override
    public void visitThrowStatement(final ThrowStatement statement) {
        controller.getStatementWriter().writeThrow(statement);
    }

    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        controller.getStatementWriter().writeReturn(statement);
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        controller.getStatementWriter().writeExpressionStatement(statement);
    }

    // Expressions
    //--------------------------------------------------------------------------

    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        onLineNumber(expression, "visitTernaryExpression");
        controller.getBinaryExpressionHelper().evaluateTernary(expression);
        doPostVisit(expression); // GROOVY-7473
    }

    @Override
    public void visitDeclarationExpression(final DeclarationExpression expression) {
        onLineNumber(expression, "visitDeclarationExpression: " + expression.getText());
        controller.getBinaryExpressionHelper().evaluateEqual(expression, true);
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        onLineNumber(expression, "visitBinaryExpression: " + expression.getOperation().getText());
        controller.getBinaryExpressionHelper().eval(expression);
        controller.getAssertionWriter().record(expression.getOperation());
        doPostVisit(expression); // GROOVY-5746
    }

    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        controller.getBinaryExpressionHelper().evaluatePostfixMethod(expression);
        controller.getAssertionWriter().record(expression);
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        controller.getBinaryExpressionHelper().evaluatePrefixMethod(expression);
        controller.getAssertionWriter().record(expression);
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        controller.getClosureWriter().writeClosure(expression);
    }

    @Override
    public void visitLambdaExpression(final LambdaExpression expression) {
        controller.getLambdaWriter().writeLambda(expression);
    }

    /**
     * Loads either this object or if we're inside a closure then load the top level owner
     */
    protected void loadThisOrOwner() {
        ClassNode classNode = controller.getClassNode();
        if (classNode.getOuterClass() == null) {
            loadThis(VariableExpression.THIS_EXPRESSION);
        } else {
            fieldX(classNode.getDeclaredField("owner")).visit(this);
        }
    }

    /**
     * Generates byte code for constants.
     *
     * @see <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#14152">Class field types</a>
     */
    @Override
    public void visitConstantExpression(final ConstantExpression expression) {
        final String constantName = expression.getConstantName();
        OperandStack operandStack = controller.getOperandStack();
        if (controller.isStaticConstructor() || constantName == null) {
            operandStack.pushConstant(expression);
        } else {
            controller.getMethodVisitor().visitFieldInsn(GETSTATIC, controller.getInternalClassName(),constantName, BytecodeHelper.getTypeDescription(expression.getType()));
            operandStack.push(expression.getType());
        }
    }

    @Override
    public void visitSpreadExpression(final SpreadExpression expression) {
        throw new GroovyBugError("SpreadExpression should not be visited here");
    }

    @Override
    public void visitSpreadMapExpression(final SpreadMapExpression expression) {
        // GROOVY-3421: SpreadMapExpression is key expression and contains value
        callX(ClassHelper.make(Collections.class), "emptyMap").visit(this);
        spreadMap.call(controller.getMethodVisitor()); // dummy SpreadMap
        controller.getOperandStack().replace(ClassHelper.OBJECT_TYPE);
    }

    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        controller.getMethodPointerExpressionWriter().writeMethodPointerExpression(expression);
    }

    @Override
    public void visitMethodReferenceExpression(final MethodReferenceExpression expression) {
        controller.getMethodReferenceExpressionWriter().writeMethodReferenceExpression(expression);
    }

    @Override
    public void visitUnaryMinusExpression(final UnaryMinusExpression expression) {
        controller.getUnaryExpressionHelper().writeUnaryMinus(expression);
    }

    @Override
    public void visitUnaryPlusExpression(final UnaryPlusExpression expression) {
        controller.getUnaryExpressionHelper().writeUnaryPlus(expression);
    }

    @Override
    public void visitBitwiseNegationExpression(final BitwiseNegationExpression expression) {
        controller.getUnaryExpressionHelper().writeBitwiseNegate(expression);
    }

    @Override
    public void visitCastExpression(final CastExpression castExpression) {
        Expression subExpression = castExpression.getExpression();
        subExpression.visit(this);

        ClassNode type = castExpression.getType();
        if (isObjectType(type)) return;
        maybeInnerClassEntry(type);

        OperandStack operandStack = controller.getOperandStack();
        if (castExpression.isCoerce()) {
            operandStack.doAsType(type);
        } else {
            if (isNullConstant(subExpression) && !isPrimitiveType(type)) {
                operandStack.replace(type);
            } else {
                ClassNode subExprType = controller.getTypeChooser().resolveType(subExpression, controller.getClassNode());
                if (castExpression.isStrict() ||
                        (!isPrimitiveType(type) && WideningCategories.implementsInterfaceOrSubclassOf(subExprType, type))) {
                    BytecodeHelper.doCast(controller.getMethodVisitor(), type);
                    operandStack.replace(type);
                } else {
                    operandStack.doGroovyCast(type);
                }
            }
        }
    }

    @Override
    public void visitNotExpression(final NotExpression expression) {
        controller.getUnaryExpressionHelper().writeNotExpression(expression);
    }

    @Override
    public void visitBooleanExpression(final BooleanExpression expression) {
        OperandStack operandStack = controller.getOperandStack();
        int mark = operandStack.getStackLength();

        expression.getExpression().visit(this);
        operandStack.castToBool(mark, true);
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        onLineNumber(call, "visitMethodCallExpression: \"" + call.getMethod() + "\":");
        controller.getInvocationWriter().writeInvokeMethod(call);
        controller.getAssertionWriter().record(call.getMethod());
    }

    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
        onLineNumber(call, "visitStaticMethodCallExpression: \"" + call.getMethod() + "\":");
        controller.getInvocationWriter().writeInvokeStaticMethod(call);
        controller.getAssertionWriter().record(call);
        maybeInnerClassEntry(call.getOwnerType());
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        onLineNumber(call, "visitConstructorCallExpression: \"" + call.getType().getName() + "\":");
        if (call.isSpecialCall()) {
            controller.getInvocationWriter().writeSpecialConstructorCall(call);
            return;
        }
        controller.getInvocationWriter().writeInvokeConstructor(call);
        controller.getAssertionWriter().record(call);
        maybeInnerClassEntry(call.getType());
    }

    private static String makeFieldClassName(final ClassNode type) {
        String internalName = BytecodeHelper.getClassInternalName(type);
        StringBuilder ret = new StringBuilder(internalName.length());
        for (int i = 0, n = internalName.length(); i < n; i += 1) {
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

    private static String getStaticFieldName(final ClassNode type) {
        ClassNode componentType = type;
        StringBuilder prefix = new StringBuilder();
        for (; componentType.isArray(); componentType = componentType.getComponentType()) {
            prefix.append("$");
        }
        if (prefix.length() != 0) prefix.insert(0, "array");
        String name = prefix + "$class$" + makeFieldClassName(componentType);
        return name;
    }

    /**
     * Determines if the given class can directly access the given field (via
     * {@code GETFIELD}, {@code GETSTATIC}, etc. bytecode instructions).
     */
    public static boolean isFieldDirectlyAccessible(final FieldNode field, final ClassNode accessingClass) {
        return field != null && isMemberDirectlyAccessible(field.getModifiers(), field.getDeclaringClass(), accessingClass);
    }

    public static boolean isMemberDirectlyAccessible(final int modifiers, final ClassNode declaringClass, final ClassNode accessingClass) {
        // a public member is accessible from anywhere
        if (Modifier.isPublic(modifiers)) return true;

        // any member is accessible from the declaring class
        if (accessingClass.equals(declaringClass)) return true;

        // a private member isn't accessible beyond the declaring class
        if (Modifier.isPrivate(modifiers)) return false;

        // a protected member is accessible from any subclass of the declaring class
        if (Modifier.isProtected(modifiers) && accessingClass.isDerivedFrom(declaringClass)) return true;

        // a protected or package-private member is accessible from the declaring package
        if (Objects.equals(accessingClass.getPackageName(), declaringClass.getPackageName())) return true;

        return false;
    }

    public static FieldNode getDeclaredFieldOfCurrentClassOrAccessibleFieldOfSuper(final ClassNode accessingNode, final ClassNode current, final String fieldName, final boolean skipCurrent) {
        return getField(current, fieldName, fieldNode ->
            (!skipCurrent || !current.equals(fieldNode.getDeclaringClass())) && isFieldDirectlyAccessible(fieldNode, accessingNode)
        );
    }

    private void visitAttributeOrProperty(final PropertyExpression pexp, final MethodCallerMultiAdapter adapter) {
        ClassNode classNode = controller.getClassNode();
        String propertyName = pexp.getPropertyAsString();
        Expression objectExpression = pexp.getObjectExpression();

        if (objectExpression instanceof ClassExpression && "this".equals(propertyName)) {
            // we have something like A.B.this, and need to make it
            // into this.this$0.this$0, where this.this$0 returns
            // A.B and this.this$0.this$0 return A.
            ClassNode type = objectExpression.getType();
            if (controller.getCompileStack().isInSpecialConstructorCall() && type.equals(classNode.getOuterClass())) {
                // Outer.this in a special constructor call
                ConstructorNode ctor = controller.getConstructorNode();
                Expression receiver = !classNode.isStaticClass() ? new VariableExpression(ctor.getParameters()[0]) : new ClassExpression(type);
                receiver.setSourcePosition(pexp);
                receiver.visit(this);
                return;
            }

            MethodVisitor mv = controller.getMethodVisitor();
            mv.visitVarInsn(ALOAD, 0);
            ClassNode thisType = classNode;
            while (!thisType.equals(type)) {
                if (thisType.getOuterClass() == null) break;
                String ownerName = BytecodeHelper.getClassInternalName(thisType);
                FieldNode thisField = thisType.getField("this$0");
                thisType = thisType.getOuterClass();
                if (thisField == null) {
                    // closure within inner class
                    while (ClassHelper.isGeneratedFunction(thisType)) {
                        // GROOVY-8881: cater for closures within closures - getThisObject is already outer class of all closures
                        thisType = thisType.getOuterClass();
                    }
                    mv.visitMethodInsn(INVOKEVIRTUAL, BytecodeHelper.getClassInternalName(ClassHelper.CLOSURE_TYPE), "getThisObject", "()Ljava/lang/Object;", false);
                    mv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(thisType));
                } else {
                    ClassNode thisFieldType = thisField.getType();
                    if (ClassHelper.CLOSURE_TYPE.equals(thisFieldType)) {
                        mv.visitFieldInsn(GETFIELD, ownerName, "this$0", BytecodeHelper.getTypeDescription(ClassHelper.CLOSURE_TYPE));
                        mv.visitMethodInsn(INVOKEVIRTUAL, BytecodeHelper.getClassInternalName(ClassHelper.CLOSURE_TYPE), "getThisObject", "()Ljava/lang/Object;", false);
                        mv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(thisType));
                    } else {
                        String typeName = BytecodeHelper.getTypeDescription(thisType);
                        mv.visitFieldInsn(GETFIELD, ownerName, "this$0", typeName);
                    }
                }
            }
            controller.getOperandStack().push(type);
            return;
        }

        if (propertyName != null) {
            // TODO: spread safe should be handled inside
            if (adapter == getProperty && !pexp.isSpreadSafe()) {
                controller.getCallSiteWriter().makeGetPropertySite(objectExpression, propertyName, pexp.isSafe(), pexp.isImplicitThis());
            } else if (adapter == getGroovyObjectProperty && !pexp.isSpreadSafe()) {
                controller.getCallSiteWriter().makeGroovyObjectGetPropertySite(objectExpression, propertyName, pexp.isSafe(), pexp.isImplicitThis());
            } else {
                controller.getCallSiteWriter().fallbackAttributeOrPropertySite(pexp, objectExpression, propertyName, adapter);
            }
        } else {
            controller.getCallSiteWriter().fallbackAttributeOrPropertySite(pexp, objectExpression, null, adapter);
        }
    }

    private boolean isGroovyObject(final Expression objectExpression) {
        if (objectExpression instanceof ClassExpression) return false;
        if (isThisOrSuper(objectExpression)) return true;//GROOVY-8693

        ClassNode objectExpressionType = controller.getTypeChooser().resolveType(objectExpression, controller.getClassNode());
        if (isObjectType(objectExpressionType)) objectExpressionType = objectExpression.getType();
        return objectExpressionType.isDerivedFromGroovyObject();
    }

    private boolean isThisExpression(final Expression expression) {
        return org.apache.groovy.ast.tools.ExpressionUtils.isThisExpression(expression)
            // GROOVY-10695: "Type.name" within body of Type should get explicit-this treatment
            || (expression instanceof ClassExpression && expression.getType().equals(controller.getClassNode()));
    }

    private boolean isThisOrSuper(final Expression expression) {
        return isThisExpression(expression) || isSuperExpression(expression);
    }

    private boolean isStatic(final Expression expression) { // Type, this or super
        return expression instanceof ClassExpression || controller.isStaticContext();
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        Expression objectExpression = expression.getObjectExpression();
        OperandStack operandStack = controller.getOperandStack();
        int mark = operandStack.getStackLength() - 1;
        boolean visited = false;

        if (isThisOrSuper(objectExpression)) {
            String name = expression.getPropertyAsString();
            if (name != null) {
                FieldNode fieldNode = null;
                ClassNode classNode = controller.getClassNode();

                if (isThisExpression(objectExpression)) {
                    if (controller.isInGeneratedFunction()) { // params/variables are stored as fields
                        if (expression.isImplicitThis()) fieldNode = classNode.getDeclaredField(name);
                    } else {
                        fieldNode = classNode.getDeclaredField(name);
                        // GROOVY-8448: "this.name" from anon. inner class
                        if (fieldNode != null && !expression.isImplicitThis()
                                && (fieldNode.getModifiers() & ACC_SYNTHETIC) != 0
                                && fieldNode.getType().equals(ClassHelper.REFERENCE_TYPE)) {
                            fieldNode = null;
                        }
                        // GROOVY-10695: "this.name" or "Type.name" where "name" is non-static
                        if (fieldNode != null && !fieldNode.isStatic() && isStatic(objectExpression)) {
                            fieldNode = null;
                        }
                    }
                } else {
                    fieldNode = classNode.getSuperClass().getDeclaredField(name);
                    // GROOVY-4497: do not visit super class field if it is private
                    if (fieldNode != null && fieldNode.isPrivate()) fieldNode = null;
                }

                if (fieldNode != null) {
                    fieldX(fieldNode).visit(this);
                    visited = true;
                }
            }
        }

        if (!visited) {
            boolean useMetaObjectProtocol = isGroovyObject(objectExpression)
                    && (!isThisOrSuper(objectExpression) || !controller.isStaticContext() || controller.isInGeneratedFunction());

            MethodCallerMultiAdapter adapter;
            if (controller.getCompileStack().isLHS()) {
                adapter = isSuperExpression(objectExpression) ? setPropertyOnSuper : useMetaObjectProtocol ? setGroovyObjectProperty : setProperty;
            } else {
                adapter = isSuperExpression(objectExpression) ? getPropertyOnSuper : useMetaObjectProtocol ? getGroovyObjectProperty : getProperty;
            }
            visitAttributeOrProperty(expression, adapter);
        }

        if (controller.getCompileStack().isLHS()) {
            operandStack.remove(operandStack.getStackLength() - mark);
        } else {
            controller.getAssertionWriter().record(expression.getProperty());
        }
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        Expression objectExpression = expression.getObjectExpression();
        OperandStack operandStack = controller.getOperandStack();
        int mark = operandStack.getStackLength() - 1;
        boolean visited = false;

        if (isThisOrSuper(objectExpression)) {
            String name = expression.getPropertyAsString();
            if (name != null) {
                ClassNode classNode = controller.getClassNode();
                FieldNode fieldNode = getDeclaredFieldOfCurrentClassOrAccessibleFieldOfSuper(classNode, classNode, name, isSuperExpression(objectExpression));
                if (fieldNode != null && (fieldNode.isStatic() || !isStatic(objectExpression))) {
                    fieldX(fieldNode).visit(this);
                    visited = true;
                }
            }
        }

        if (!visited) {
            MethodCallerMultiAdapter adapter;
            if (controller.getCompileStack().isLHS()) {
                adapter = isSuperExpression(objectExpression) ? setFieldOnSuper : isGroovyObject(objectExpression) ? setGroovyObjectField : setField;
            } else {
                adapter = isSuperExpression(objectExpression) ? getFieldOnSuper : isGroovyObject(objectExpression) ? getGroovyObjectField : getField;
            }
            visitAttributeOrProperty(expression, adapter);
        }

        if (controller.getCompileStack().isLHS()) {
            operandStack.remove(operandStack.getStackLength() - mark);
        } else {
            controller.getAssertionWriter().record(expression.getProperty());
        }
    }

    @Override
    public void visitFieldExpression(final FieldExpression expression) {
        if (expression.getField().isStatic()) {
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
    }

    public void loadStaticField(final FieldExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        FieldNode field = expression.getField();
        ClassNode type = field.getType();

        OperandStack operandStack = controller.getOperandStack();
        if (field.isHolder() && !controller.isInGeneratedFunctionConstructor()) {
            mv.visitFieldInsn(GETSTATIC, getFieldOwnerName(field), field.getName(), BytecodeHelper.getTypeDescription(type));
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;", false);
            operandStack.push(ClassHelper.OBJECT_TYPE); // erased return type
            operandStack.doGroovyCast(field.getOriginType()); // GROOVY-11068
        } else {
            mv.visitFieldInsn(GETSTATIC, getFieldOwnerName(field), field.getName(), BytecodeHelper.getTypeDescription(type));
            operandStack.push(type);
        }
    }

    /**
     * RHS instance field. should move most of the code in the BytecodeHelper
     */
    public void loadInstanceField(final FieldExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        FieldNode field = expression.getField();
        ClassNode type = field.getType();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, getFieldOwnerName(field), field.getName(), BytecodeHelper.getTypeDescription(type));

        OperandStack operandStack = controller.getOperandStack();
        if (field.isHolder() && !controller.isInGeneratedFunctionConstructor()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;", false);
            operandStack.push(ClassHelper.OBJECT_TYPE); // erased return type
            operandStack.doGroovyCast(field.getOriginType()); // GROOVY-11068
        } else {
            operandStack.push(type);
        }
    }

    private void storeThisInstanceField(final FieldExpression expression) {
        OperandStack operandStack = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();
        FieldNode field = expression.getField();
        ClassNode type = field.getType();

        if (field.isHolder() && expression.isUseReferenceDirectly()) {
            // rhs is ready to use reference, just put it in the field
            mv.visitVarInsn(ALOAD, 0);
            operandStack.push(controller.getClassNode());
            operandStack.swap();
            mv.visitFieldInsn(PUTFIELD, getFieldOwnerName(field), field.getName(), BytecodeHelper.getTypeDescription(type));
        } else if (field.isHolder()) {
            // rhs is normal value, set the value in the Reference
            operandStack.doGroovyCast(field.getOriginType());
            operandStack.box();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, getFieldOwnerName(field), field.getName(), BytecodeHelper.getTypeDescription(type));
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V", false);
        } else {
            // rhs is normal value, set normal value
            operandStack.doGroovyCast(field.getOriginType());
            mv.visitVarInsn(ALOAD, 0);
            operandStack.push(controller.getClassNode());
            operandStack.swap();
            mv.visitFieldInsn(PUTFIELD, getFieldOwnerName(field), field.getName(), BytecodeHelper.getTypeDescription(type));
        }
    }

    private void storeStaticField(final FieldExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        FieldNode field = expression.getField();
        ClassNode type = field.getType();

        OperandStack operandStack = controller.getOperandStack();
        operandStack.doGroovyCast(field);

        if (field.isHolder() && !controller.isInGeneratedFunctionConstructor()) {
            operandStack.box();
            mv.visitFieldInsn(GETSTATIC, getFieldOwnerName(field), field.getName(), BytecodeHelper.getTypeDescription(type));
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V", false);
        } else {
            mv.visitFieldInsn(PUTSTATIC, getFieldOwnerName(field), field.getName(), BytecodeHelper.getTypeDescription(type));
        }

        operandStack.remove(1);
    }

    private String getFieldOwnerName(final FieldNode field) {
        if (field.getOwner().equals(controller.getClassNode())) {
            return controller.getInternalClassName();
        }
        return BytecodeHelper.getClassInternalName(field.getOwner());
    }

    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        final String variableName = expression.getName();

        if (expression.isThisExpression()) {
            // "this" in static context is Class instance
            if (controller.isStaticMethod() || controller.getCompileStack().isInSpecialConstructorCall()
                    || (!controller.getCompileStack().isImplicitThis() && controller.isStaticContext())) {
                classX(controller.getThisType()).visit(this);
            } else {
                loadThis(expression);
            }
            return;
        }

        if (expression.isSuperExpression()) {
            // "super" in static context is Class instance
            if (controller.isStaticMethod()) {
                ClassNode superType = controller.getClassNode().getSuperClass();
                classX(superType).visit(this);
            } else {
                loadThis(expression);
            }
            return;
        }

        BytecodeVariable variable = controller.getCompileStack().getVariable(variableName, false);
        if (variable != null) {
            controller.getOperandStack().loadOrStoreVariable(variable, expression.isUseReferenceDirectly());
        } else if (passingParams && controller.isInScriptBody()) {
            MethodVisitor mv = controller.getMethodVisitor();
            mv.visitTypeInsn(NEW, "org/codehaus/groovy/runtime/ScriptReference");
            mv.visitInsn(DUP);
            loadThisOrOwner();
            mv.visitLdcInsn(variableName);
            mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/ScriptReference", "<init>", "(Lgroovy/lang/Script;Ljava/lang/String;)V", false);
        } else {
            PropertyExpression pexp = thisPropX(true, variableName);
            pexp.getObjectExpression().setSourcePosition(expression);
            pexp.getProperty().setSourcePosition(expression);
            pexp.setType(expression.getType());
            pexp.copyNodeMetaData(expression);
            pexp.visit(this);
        }

        if (!controller.getCompileStack().isLHS()) {
            controller.getAssertionWriter().record(expression);
        }
    }

    protected void createInterfaceSyntheticStaticFields() {
        if (referencedClasses.isEmpty()) return;
        var icl = controller.getInterfaceClassLoadingClass();
        addInnerClass(icl);
        for (Map.Entry<String, ClassNode> entry : referencedClasses.entrySet()) {
            // generate a field node
            String staticFieldName = entry.getKey();
            ClassNode cn = entry.getValue();
            icl.addField(staticFieldName, ACC_STATIC + ACC_SYNTHETIC, ClassHelper.CLASS_Type.getPlainNodeReference(), new ClassExpression(cn));
        }
    }

    protected void createSyntheticStaticFields() {
        if (referencedClasses.isEmpty()) {
            return;
        }
        MethodVisitor mv;
        for (Map.Entry<String, ClassNode> entry : referencedClasses.entrySet()) {
            String staticFieldName = entry.getKey();
            ClassNode cn = entry.getValue();
            // generate a field node
            FieldNode fn = controller.getClassNode().getDeclaredField(staticFieldName);
            if (fn != null) {
                boolean type = isClassType(fn.getType());
                boolean modifiers = fn.getModifiers() == ACC_STATIC + ACC_SYNTHETIC;
                if (!type || !modifiers) {
                    String text = "";
                    if (!type) text = " with wrong type: " + fn.getType() + " (java.lang.Class needed)";
                    if (!modifiers)
                        text = " with wrong modifiers: " + fn.getModifiers() + " (" + (ACC_STATIC + ACC_SYNTHETIC) + " needed)";
                    throwException("tried to set a static synthetic field " + staticFieldName + " in " + controller.getClassNode().getName() +
                            " for class resolving, but found already a node of that name " + text);
                }
            } else {
                classVisitor.visitField(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, staticFieldName, "Ljava/lang/Class;", null, null);
            }

            mv = classVisitor.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "$get$" + staticFieldName,"()Ljava/lang/Class;",null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC,controller.getInternalClassName(),staticFieldName,"Ljava/lang/Class;");
            mv.visitInsn(DUP);
            Label l0 = new Label();
            mv.visitJumpInsn(IFNONNULL,l0);
            mv.visitInsn(POP);
            mv.visitLdcInsn(BytecodeHelper.getClassLoadingTypeDescription(cn));
            mv.visitMethodInsn(INVOKESTATIC, controller.getInternalClassName(), "class$", "(Ljava/lang/String;)Ljava/lang/Class;", false);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(PUTSTATIC,controller.getInternalClassName(),staticFieldName,"Ljava/lang/Class;");
            mv.visitLabel(l0);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        mv = classVisitor.visitMethod(ACC_STATIC + ACC_SYNTHETIC, "class$", "(Ljava/lang/String;)Ljava/lang/Class;", null, null);
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitInsn(ARETURN);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitTypeInsn(NEW, "java/lang/NoClassDefFoundError");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassNotFoundException", "getMessage", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoClassDefFoundError", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitTryCatchBlock(l0, l2, l2, "java/lang/ClassNotFoundException"); // br using l2 as the 2nd param seems create the right table entry
        mv.visitMaxs(0, 0);
    }

    @Override
    public void visitClassExpression(final ClassExpression expression) {
        ClassNode type = expression.getType();
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        if (BytecodeHelper.isClassLiteralPossible(type) || BytecodeHelper.isSameCompilationUnit(controller.getClassNode(), type)) {
            if (controller.getClassNode().isInterface()) {
                var interfaceClassLoadingClass = controller.getInterfaceClassLoadingClass();
                if (BytecodeHelper.isClassLiteralPossible(interfaceClassLoadingClass)) {
                    BytecodeHelper.visitClassLiteral(mv, interfaceClassLoadingClass);
                    operandStack.push(ClassHelper.CLASS_Type);
                    maybeInnerClassEntry(type);
                    return;
                }
            } else {
                BytecodeHelper.visitClassLiteral(mv, type);
                operandStack.push(ClassHelper.CLASS_Type);
                maybeInnerClassEntry(type);
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
            mv.visitMethodInsn(INVOKESTATIC, internalClassName, "$get$" + staticFieldName, "()Ljava/lang/Class;", false);
        }
        operandStack.push(ClassHelper.CLASS_Type);
    }

    @Override
    public void visitRangeExpression(final RangeExpression expression) {
        OperandStack operandStack = controller.getOperandStack();
        expression.getFrom().visit(this);
        operandStack.box();
        expression.getTo().visit(this);
        operandStack.box();
        operandStack.pushBool(expression.isExclusiveLeft());
        operandStack.pushBool(expression.isExclusiveRight());

        createRangeMethod.call(controller.getMethodVisitor());
        operandStack.replace(ClassHelper.RANGE_TYPE, 4);
    }

    @Override
    public void visitMapEntryExpression(final MapEntryExpression expression) {
        throw new GroovyBugError("MapEntryExpression should not be visited here");
    }

    @Override
    public void visitMapExpression(final MapExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();

        List<MapEntryExpression> entries = expression.getMapEntryExpressions();
        int size = entries.size();
        BytecodeHelper.pushConstant(mv, size * 2);

        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        int i = 0;
        OperandStack operandStack = controller.getOperandStack();
        for (MapEntryExpression entry : entries) {
            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i++);
            entry.getKeyExpression().visit(this);
            operandStack.box();
            mv.visitInsn(AASTORE);

            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i++);
            entry.getValueExpression().visit(this);
            operandStack.box();
            mv.visitInsn(AASTORE);

            operandStack.remove(2);
        }
        createMapMethod.call(mv);
        operandStack.push(ClassHelper.MAP_TYPE);
    }

    @Override
    public void visitArgumentlistExpression(final ArgumentListExpression ale) {
        if (containsSpreadExpression(ale)) {
            despreadList(ale.getExpressions(), true);
        } else {
            visitTupleExpression(ale, true);
        }
    }

    @Override
    public void visitTupleExpression(final TupleExpression expression) {
        visitTupleExpression(expression, false);
    }

    void visitTupleExpression(final TupleExpression expression, final boolean useWrapper) {
        MethodVisitor mv = controller.getMethodVisitor();
        int size = expression.getExpressions().size();

        BytecodeHelper.pushConstant(mv, size);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        OperandStack operandStack = controller.getOperandStack();
        for (int i = 0; i < size; i += 1) {
            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i);
            Expression argument = expression.getExpression(i);
            argument.visit(this);
            operandStack.box();
            if (useWrapper && (argument instanceof CastExpression || (isParameterReference(argument)
                    && controller.getCompileStack().isInSpecialConstructorCall()))) { // GROOVY-6285
                loadWrapper(argument);
            }
            mv.visitInsn(AASTORE);
            operandStack.remove(1);
        }
    }

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        CompileStack  compileStack = controller.getCompileStack();
        OperandStack  operandStack = controller.getOperandStack();

        ClassNode arrayType = expression.getType();
        ClassNode elementType = arrayType.getComponentType();

        int size = 0, dimensions = 0;
        if (expression.hasInitializer()) {
            if (containsSpreadExpression(expression)) {
                despreadList(expression.getExpressions(), false);
                if (elementType.equals(ClassHelper.OBJECT_TYPE)){
                    operandStack.push(arrayType);
                    return;
                }
                mv.visitInsn(DUP); // Object[] from despreadList
                mv.visitInsn(ARRAYLENGTH);
                mv.visitInsn(DUP); // store value count
                operandStack.push(ClassHelper.int_TYPE);
                size = -compileStack.defineTemporaryVariable("value$count", ClassHelper.int_TYPE, true);
            } else {
                size = expression.getExpressions().size();
                BytecodeHelper.pushConstant(mv, size);
            }
            // stack: ..., size
        } else {
            for (final Expression sizeExpr : expression.getSizeExpression()) {
                if (sizeExpr == ConstantExpression.EMPTY_EXPRESSION) break;
                dimensions += 1;
                sizeExpr.visit(this);
                operandStack.doGroovyCast(ClassHelper.int_TYPE);
            }
            operandStack.remove(dimensions);
            // stack: ..., size (one per dimension)
        }

        int storeIns = AASTORE;
        if (!elementType.isArray() || expression.hasInitializer()) {
            if (isPrimitiveType(elementType)) {
                int primType = 0;
                if (isPrimitiveBoolean(elementType)) {
                    primType = T_BOOLEAN;
                    storeIns = BASTORE;
                } else if (isPrimitiveChar(elementType)) {
                    primType = T_CHAR;
                    storeIns = CASTORE;
                } else if (isPrimitiveFloat(elementType)) {
                    primType = T_FLOAT;
                    storeIns = FASTORE;
                } else if (isPrimitiveDouble(elementType)) {
                    primType = T_DOUBLE;
                    storeIns = DASTORE;
                } else if (isPrimitiveByte(elementType)) {
                    primType = T_BYTE;
                    storeIns = BASTORE;
                } else if (isPrimitiveShort(elementType)) {
                    primType = T_SHORT;
                    storeIns = SASTORE;
                } else if (isPrimitiveInt(elementType)) {
                    primType = T_INT;
                    storeIns = IASTORE;
                } else if (isPrimitiveLong(elementType)) {
                    primType = T_LONG;
                    storeIns = LASTORE;
                }
                mv.visitIntInsn(NEWARRAY, primType);
            } else {
                mv.visitTypeInsn(ANEWARRAY, BytecodeHelper.getClassInternalName(elementType));
                maybeInnerClassEntry(elementType);
            }
        } else {
            mv.visitMultiANewArrayInsn(BytecodeHelper.getTypeDescription(arrayType), dimensions);
        }
        // stack: ..., array

        if (size >= 0) {
            for (int i = 0; i < size; i += 1) {
                mv.visitInsn(DUP); // array ref
                BytecodeHelper.pushConstant(mv, i);
                Optional.ofNullable(expression.getExpression(i))
                        .orElse(ConstantExpression.NULL)
                        .visit(this);
                operandStack.doGroovyCast(elementType);
                mv.visitInsn(storeIns);
                operandStack.remove(1);
            }
        } else {
            // stack: ..., source, target
            Label top = new Label();
            mv.visitLabel(top);

            {
                final int idx = -size;
                mv.visitIincInsn(idx, -1);

                mv.visitInsn(DUP2);
                mv.visitInsn(SWAP);
                // stack: ..., target, source
                mv.visitVarInsn(ILOAD, idx);
                // stack: ..., target, source, index
                mv.visitInsn(AALOAD);
                // stack: ..., target, value
                operandStack.push(ClassHelper.OBJECT_TYPE);
                operandStack.doGroovyCast(elementType);

                mv.visitVarInsn(ILOAD, idx);
                // stack: ..., target, value, index
                operandStack.push(ClassHelper.int_TYPE);
                operandStack.swap();
                // stack: ..., target, index, value
                mv.visitInsn(storeIns);
                operandStack.remove(2);
                // stack: ...

                mv.visitVarInsn(ILOAD, idx);
                mv.visitJumpInsn(IFNE, top);

                compileStack.removeVar(idx);
            }

            // stack: ..., source, target
            mv.visitInsn(SWAP);
            // stack: ..., target, source
            mv.visitInsn(POP);
            // stack: ..., target
        }

        operandStack.push(arrayType);
    }

    @Override
    public void visitClosureListExpression(final ClosureListExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        controller.getCompileStack().pushVariableScope(expression.getVariableScope());

        List<Expression> expressions = expression.getExpressions();
        final int size = expressions.size();
        // init declarations
        for (int i = 0; i < size; i += 1) {
            Expression expr = expressions.get(i);
            if (expr instanceof DeclarationExpression) {
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

        List<Object> instructions = new LinkedList<>();
        // to keep stack height put a null on stack
        instructions.add(ConstantExpression.NULL);

        // init table
        final Label dflt = new Label();
        final Label tableEnd = new Label();
        final Label[] labels = new Label[size];
        instructions.add(new BytecodeInstruction() {
            @Override
            public void visit(MethodVisitor mv) {
                mv.visitVarInsn(ILOAD, 1);
                mv.visitTableSwitchInsn(0, size - 1, dflt, labels);
            }
        });

        // visit cases
        for (int i = 0; i < size; i += 1) {
            Label label = new Label();
            Expression expr = expressions.get(i);
            labels[i] = label;
            instructions.add(new BytecodeInstruction() {
                @Override
                public void visit(MethodVisitor mv) {
                    mv.visitLabel(label);
                    // expressions will leave a value on stack, so need to pop the alibi null
                    mv.visitInsn(POP);
                }
            });
            instructions.add(expr);
            instructions.add(new BytecodeInstruction() {
                @Override
                public void visit(MethodVisitor mv) {
                    mv.visitJumpInsn(GOTO, tableEnd);
                }
            });
        }

        // default case
        instructions.add(new BytecodeInstruction() {
            @Override
            public void visit(MethodVisitor mv) {
                mv.visitLabel(dflt);
            }
        });
        ConstantExpression text = new ConstantExpression("invalid index for closure");
        ConstructorCallExpression cce = new ConstructorCallExpression(ClassHelper.make(IllegalArgumentException.class), text);
        ThrowStatement ts = new ThrowStatement(cce);
        instructions.add(ts);

        // return
        instructions.add(new BytecodeInstruction() {
            @Override
            public void visit(MethodVisitor mv) {
                mv.visitLabel(tableEnd);
                mv.visitInsn(ARETURN);
            }
        });

        BlockStatement bs = new BlockStatement();
        bs.addStatement(new BytecodeSequence(instructions));
        Parameter closureIndex = new Parameter(ClassHelper.int_TYPE, "__closureIndex");
        ClosureExpression ce = new ClosureExpression(new Parameter[]{closureIndex}, bs);
        ce.setVariableScope(expression.getVariableScope());
        visitClosureExpression(ce);

        // we need later an array to store the curried
        // closures, so we create it here and ave it
        // in a temporary variable
        BytecodeHelper.pushConstant(mv, size);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        int listArrayVar = controller.getCompileStack().defineTemporaryVariable("_listOfClosures", true);

        // add curried versions
        for (int i = 0; i < size; i += 1) {
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
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/CurriedClosure", "<init>", "(Lgroovy/lang/Closure;[Ljava/lang/Object;)V", false);
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

    @Override
    public void visitBytecodeExpression(final BytecodeExpression expression) {
        expression.visit(controller.getMethodVisitor());
        controller.getOperandStack().push(expression.getType());
    }

    @Override
    public void visitBytecodeSequence(final BytecodeSequence bytecodeSequence) {
        MethodVisitor mv = controller.getMethodVisitor();
        List<?> sequence = bytecodeSequence.getInstructions();
        OperandStack operandStack = controller.getOperandStack();
        int mark = operandStack.getStackLength();

        for (Object element : sequence) {
            if (element instanceof EmptyExpression) {
                mv.visitInsn(ACONST_NULL);
            } else if (element instanceof Expression) {
                ((Expression) element).visit(this);
            } else if (element instanceof Statement) {
                ((Statement) element).visit(this);
                mv.visitInsn(ACONST_NULL);
            } else {
                ((BytecodeInstruction) element).visit(mv);
            }
        }

        operandStack.remove(mark - operandStack.getStackLength());
    }

    @Override
    public void visitListExpression(final ListExpression expression) {
        onLineNumber(expression, "ListExpression");

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
                for (int i = 0; i < size; i += 1) {
                    mv.visitInsn(DUP);
                    BytecodeHelper.pushConstant(mv, i);
                    expression.getExpression(i).visit(this);
                    operandStack.box();
                    mv.visitInsn(AASTORE);
                }
                operandStack.remove(size);
            } else {
                List<Expression> expressions = expression.getExpressions();
                List<String> methods = new ArrayList<>();
                MethodVisitor oldMv = mv;
                int index = 0;
                while (index<size) {
                    String methodName = "$createListEntry_" + controller.getNextHelperMethodIndex();
                    methods.add(methodName);
                    mv = controller.getClassVisitor().visitMethod(
                            ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC,
                            methodName,
                            "([Ljava/lang/Object;)V",
                            null, null);
                    controller.setMethodVisitor(mv);
                    mv.visitCode();
                    int methodBlockSize = Math.min(size-index, maxInit);
                    int methodBlockEnd = index + methodBlockSize;
                    for (; index < methodBlockEnd; index += 1) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitLdcInsn(index);
                        expressions.get(index).visit(this);
                        operandStack.box();
                        mv.visitInsn(AASTORE);
                    }
                    operandStack.remove(methodBlockSize);
                    mv.visitInsn(RETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }
                mv = oldMv;
                controller.setMethodVisitor(mv);
                for (String methodName : methods) {
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESTATIC, controller.getInternalClassName(), methodName, "([Ljava/lang/Object;)V", false);
                }
            }
        } else {
            despreadList(expression.getExpressions(), false);
        }
        createListMethod.call(controller.getMethodVisitor());
        operandStack.push(ClassHelper.LIST_TYPE);
    }

    @Override
    public void visitGStringExpression(final GStringExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();

        mv.visitTypeInsn(NEW, "org/codehaus/groovy/runtime/GStringImpl");
        mv.visitInsn(DUP);

        int size = expression.getValues().size();
        BytecodeHelper.pushConstant(mv, size);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        OperandStack operandStack = controller.getOperandStack();
        for (int i = 0; i < size; i += 1) {
            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i);
            expression.getValue(i).visit(this);
            operandStack.box();
            mv.visitInsn(AASTORE);
        }
        operandStack.remove(size);

        List<ConstantExpression> strings = expression.getStrings();
        size = strings.size();
        BytecodeHelper.pushConstant(mv, size);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/String");

        for (int i = 0; i < size; i += 1) {
            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i);
            operandStack.pushConstant(strings.get(i));
            operandStack.box();
            mv.visitInsn(AASTORE);
        }
        operandStack.remove(size);

        mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/GStringImpl", "<init>", "([Ljava/lang/Object;[Ljava/lang/String;)V", false);
        operandStack.push(ClassHelper.GSTRING_TYPE);
    }

    @Override
    public void visitAnnotations(final AnnotatedNode node) {
        // ignore it; annotation generation needs the current visitor
    }

    private void visitAnnotations(final AnnotatedNode targetNode, final Object visitor) {
        visitAnnotations(targetNode, targetNode, visitor);
    }

    private void visitAnnotations(final AnnotatedNode targetNode, final AnnotatedNode sourceNode, final Object visitor) {
        for (AnnotationNode an : sourceNode.getAnnotations()) {
            // skip built-in properties
            if (an.isBuiltIn()) continue;
            if (an.hasSourceRetention()) continue;
            if (an.getClassNode().getName().equals(Sealed.class.getName()) && sealedNative(sourceNode) && sealedSkipAnnotation(sourceNode)) continue;

            maybeInnerClassEntry(an.getClassNode());

            AnnotationVisitor av = getAnnotationVisitor(targetNode, an, visitor);
            visitAnnotationAttributes(an, av);
            av.visitEnd();
        }
    }

    private void visitTypeAnnotations(final ClassNode sourceNode, final Object visitor, final TypeReference typeRef, final String typePathStr, boolean typeUse) {
        for (AnnotationNode an : sourceNode.getTypeAnnotations()) {
            if (an.isBuiltIn() || an.hasSourceRetention()) continue;
            if (typeUse && !an.isTargetAllowed(AnnotationNode.TYPE_USE_TARGET)) continue;

            AnnotationVisitor av = null;
            final TypePath typePath;
            try {
                typePath = TypePath.fromString(typePathStr);
            } catch (IllegalArgumentException ex) {
                throw new GroovyBugError("Illegal type path for " + sourceNode.getText() + ", typeRef = " + typeRef + ", typePath = " + typePathStr);
            }
            final int typeRefInt = typeRef.getValue();
            final String annotationDescriptor = BytecodeHelper.getTypeDescription(an.getClassNode());
            if (visitor instanceof ClassVisitor) {
                av = ((ClassVisitor) visitor).visitTypeAnnotation(typeRefInt, typePath, annotationDescriptor, an.hasRuntimeRetention());
            } else if (visitor instanceof MethodVisitor) {
                av = ((MethodVisitor) visitor).visitTypeAnnotation(typeRefInt, typePath, annotationDescriptor, an.hasRuntimeRetention());
            } else if (visitor instanceof FieldVisitor) {
                av = ((FieldVisitor) visitor).visitTypeAnnotation(typeRefInt, typePath, annotationDescriptor, an.hasRuntimeRetention());
            } else if (visitor instanceof RecordComponentVisitor) {
                av = ((RecordComponentVisitor) visitor).visitTypeAnnotation(typeRefInt, typePath, annotationDescriptor, an.hasRuntimeRetention());
            } else {
                throwException("Cannot create an AnnotationVisitor. Please report Groovy bug");
            }
            visitAnnotationAttributes(an, av);
            av.visitEnd();
        }
    }

    private void visitGenericsTypeAnnotations(final ClassNode classNode, final Object visitor, final TypeReference typeRef,
                                              final String typePath, final boolean typeUse) {
        if (!classNode.isUsingGenerics() || classNode.getGenericsTypes() == null) {
            return;
        }
        visitGenericsTypeAnnotations(classNode.getGenericsTypes(), visitor, typePath, typeRef, typeUse);
    }

    private void visitTypeParameters(final MethodNode methodNode, final Object visitor) {
        if (methodNode.getGenericsTypes() == null) {
            return;
        }
        visitGenericsTypeParameterAnnotations(methodNode.getGenericsTypes(), visitor, "", METHOD_TYPE_PARAMETER, METHOD_TYPE_PARAMETER_BOUND);
    }

    private void visitTypeParameters(final ClassNode classNode, final Object visitor) {
        if (classNode.getGenericsTypes() == null) {
            return;
        }
        visitGenericsTypeParameterAnnotations(classNode.getGenericsTypes(), visitor, "", CLASS_TYPE_PARAMETER, CLASS_TYPE_PARAMETER_BOUND);
    }

    private void visitGenericsTypeParameterAnnotations(final GenericsType[] genericsTypes, final Object visitor, final String typePath, final int sort, final int boundSort) {
        for (int paramIdx = 0; paramIdx < genericsTypes.length; paramIdx++) {
            GenericsType gt = genericsTypes[paramIdx];
            visitType(gt.getType(), visitor, newTypeParameterReference(sort, paramIdx), typePath, false);
            if (gt.getLowerBound() != null) {
                visitType(gt.getLowerBound(), visitor, newTypeParameterBoundReference(boundSort, paramIdx, 0), typePath, false);
            }
            if (gt.getUpperBounds() != null) {
                ClassNode[] upperBounds = gt.getUpperBounds();
                for (int boundIdx = 0; boundIdx < upperBounds.length; boundIdx++) {
                    visitType(upperBounds[boundIdx], visitor, newTypeParameterBoundReference(boundSort, paramIdx, boundIdx), typePath, false);
                }
            }
        }
    }

    private void visitGenericsTypeAnnotations(final GenericsType[] genericsTypes, final Object visitor,
                                              final String typePath, final TypeReference typeRef, final boolean typeUse) {
        for (int paramIdx = 0; paramIdx < genericsTypes.length; paramIdx++) {
            GenericsType gt = genericsTypes[paramIdx];
            String prefix = typePath + paramIdx + ";";
            visitType(gt.getType(), visitor, typeRef, prefix, typeUse);
            if (!gt.isPlaceholder()) {
                if (gt.getLowerBound() != null) {
                    visitType(gt.getLowerBound(), visitor, typeRef, gt.isWildcard() ? prefix + "*" : prefix + "0;", typeUse);
                }
                if (gt.getUpperBounds() != null) {
                    ClassNode[] upperBounds = gt.getUpperBounds();
                    for (int boundIdx = 0; boundIdx < upperBounds.length; boundIdx++) {
                        visitType(upperBounds[boundIdx], visitor, typeRef, gt.isWildcard() ? prefix + "*" : prefix + boundIdx + ";", typeUse);
                    }
                }
            }
        }
    }

    private void visitType(final ClassNode classNode, final Object visitor, final TypeReference typeRef, final String typePath, boolean typeUse) {
        maybeInnerClassEntry(classNode); // GROOVY-8863
        visitTypeAnnotations(classNode, visitor, typeRef, typePath, typeUse);
        visitGenericsTypeAnnotations(classNode, visitor, typeRef, typePath, typeUse);
    }

    private void visitParameterAnnotations(final Parameter parameter, final int paramNumber, final MethodVisitor mv) {
        for (AnnotationNode an : parameter.getAnnotations()) {
            // skip built-in properties
            if (an.isBuiltIn() || an.hasSourceRetention()) continue;

            final String annotationDescriptor = BytecodeHelper.getTypeDescription(an.getClassNode());
            AnnotationVisitor av = mv.visitParameterAnnotation(paramNumber, annotationDescriptor, an.hasRuntimeRetention());
            visitAnnotationAttributes(an, av);
            av.visitEnd();
        }
    }

    private AnnotationVisitor getAnnotationVisitor(final AnnotatedNode targetNode, final AnnotationNode an, final Object visitor) {
        final String annotationDescriptor = BytecodeHelper.getTypeDescription(an.getClassNode());
        if (targetNode instanceof MethodNode) {
            return ((MethodVisitor) visitor).visitAnnotation(annotationDescriptor, an.hasRuntimeRetention());
        } else if (targetNode instanceof FieldNode) {
            return ((FieldVisitor) visitor).visitAnnotation(annotationDescriptor, an.hasRuntimeRetention());
        } else if (targetNode instanceof ClassNode) {
            return ((ClassVisitor) visitor).visitAnnotation(annotationDescriptor, an.hasRuntimeRetention());
        } else if (visitor instanceof RecordComponentVisitor) {
            return ((RecordComponentVisitor) visitor).visitAnnotation(annotationDescriptor, true);
        }
        throwException("Cannot create an AnnotationVisitor. Please report Groovy bug");
        return null;
    }

    /**
     * Generates the annotation attributes.
     *
     * @param an the node with an annotation
     * @param av the visitor to use
     */
    private void visitAnnotationAttributes(final AnnotationNode an, final AnnotationVisitor av) {
        Map<String, Object> constantAttrs = new HashMap<>();
        Map<String, PropertyExpression> enumAttrs = new HashMap<>();
        Map<String, Object> atAttrs = new HashMap<>();
        Map<String, ListExpression> arrayAttrs = new HashMap<>();

        for (Map.Entry<String, Expression> member : an.getMembers().entrySet()) {
            String name = member.getKey();
            Expression expr = member.getValue();
            if (expr instanceof AnnotationConstantExpression) {
                atAttrs.put(name, ((AnnotationConstantExpression) expr).getValue());
            } else if (expr instanceof ConstantExpression) {
                constantAttrs.put(name, ((ConstantExpression) expr).getValue());
            } else if (expr instanceof ClassExpression) {
                constantAttrs.put(name, Type.getType(BytecodeHelper.getTypeDescription((expr.getType()))));
            } else if (expr instanceof PropertyExpression) {
                enumAttrs.put(name, (PropertyExpression) expr);
            } else if (expr instanceof ListExpression) {
                arrayAttrs.put(name, (ListExpression) expr);
            } else if (expr instanceof ClosureExpression) {
                ClassNode closureClass = controller.getClosureWriter().getOrAddClosureClass((ClosureExpression) expr, ACC_PUBLIC);
                constantAttrs.put(name, Type.getType(BytecodeHelper.getTypeDescription(closureClass)));
            }
        }

        for (Map.Entry<String, Object> entry : constantAttrs.entrySet()) {
            av.visit(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, PropertyExpression> entry : enumAttrs.entrySet()) {
            PropertyExpression propExp = entry.getValue();
            av.visitEnum(entry.getKey(),
                    BytecodeHelper.getTypeDescription(propExp.getObjectExpression().getType()),
                    String.valueOf(((ConstantExpression) propExp.getProperty()).getValue()));
        }
        for (Map.Entry<String, Object> entry : atAttrs.entrySet()) {
            AnnotationNode atNode = (AnnotationNode) entry.getValue();
            AnnotationVisitor av2 = av.visitAnnotation(entry.getKey(),
                    BytecodeHelper.getTypeDescription(atNode.getClassNode()));
            visitAnnotationAttributes(atNode, av2);
            av2.visitEnd();
        }
        visitArrayAttributes(an, arrayAttrs, av);
    }

    private void visitArrayAttributes(final AnnotationNode an, final Map<String, ListExpression> arrayAttr, final AnnotationVisitor av) {
        if (arrayAttr.isEmpty()) return;
        for (Map.Entry<String, ListExpression> entry : arrayAttr.entrySet()) {
            AnnotationVisitor av2 = av.visitArray(entry.getKey());
            List<Expression> values = entry.getValue().getExpressions();
            if (!values.isEmpty()) {
                int arrayElementType = determineCommonArrayType(values);
                for (Expression exprChild : values) {
                    visitAnnotationArrayElement(exprChild, arrayElementType, av2);
                }
            }
            av2.visitEnd();
        }
    }

    private void visitAnnotationArrayElement(final Expression expr, final int arrayElementType, final AnnotationVisitor av) {
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
                ClassNode type = expr.getType();
                if (expr instanceof ClosureExpression) {
                    type = controller.getClosureWriter().getOrAddClosureClass((ClosureExpression) expr, ACC_PUBLIC);
                }
                av.visit(null, Type.getType(BytecodeHelper.getTypeDescription(type)));
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
    //--------------------------------------------------------------------------

    public boolean addInnerClass(final ClassNode innerClass) {
        ModuleNode mn = controller.getClassNode().getModule();
        innerClass.setModule(mn);
        mn.getUnit().addGeneratedInnerClass((InnerClassNode) innerClass);
        return innerClasses.add(innerClass);
    }

    public static int argumentSize(final Expression arguments) {
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            int size = tupleExpression.getExpressions().size();
            return size;
        }
        return 1;
    }

    private static String[] buildExceptions(final ClassNode[] exceptions) {
        if (exceptions == null) return null;
        return Arrays.stream(exceptions).map(BytecodeHelper::getClassInternalName).toArray(String[]::new);
    }

    private static boolean containsOnlyConstants(final ListExpression list) {
        for (Expression exp : list.getExpressions()) {
            if (exp instanceof ConstantExpression) continue;
            return false;
        }
        return true;
    }

    public static boolean containsSpreadExpression(final Expression expression) {
        List<Expression> expressions;
        if (expression instanceof TupleExpression) {
            expressions = ((TupleExpression) expression).getExpressions();
        } else if (expression instanceof ListExpression) {
            expressions = ((ListExpression)  expression).getExpressions();
        } else if (expression instanceof ArrayExpression) {
            expressions = ((ArrayExpression) expression).getExpressions();
        } else {
            return expression instanceof SpreadExpression;
        }
        for (Expression expr : expressions) {
            if (expr instanceof SpreadExpression) return true;
        }
        return false;
    }

    public void despreadList(final List<Expression> expressions, final boolean wrap) {
        final int expressionCnt = expressions.size();
        List<Expression> spreadIndexes = new ArrayList<>(expressionCnt);
        List<Expression> spreadExpressions = new ArrayList<>(expressionCnt);
        List<Expression> normalArguments = new ArrayList<>(expressionCnt);

        for (int i = 0; i < expressionCnt; i += 1) {
            Expression expr = expressions.get(i);
            if (!(expr instanceof SpreadExpression)) {
                normalArguments.add(expr);
            } else {
                spreadIndexes.add(new ConstantExpression(i - spreadExpressions.size(), true));
                spreadExpressions.add(((SpreadExpression) expr).getExpression());
            }
        }

        // load normal arguments as array
        visitTupleExpression(new ArgumentListExpression(normalArguments), wrap);
        // load spread expressions as array
        new TupleExpression(spreadExpressions).visit(this);
        // load insertion index
        new ArrayExpression(ClassHelper.int_TYPE, spreadIndexes, null).visit(this);

        controller.getOperandStack().remove(1);
        despreadList.call(controller.getMethodVisitor());
    }

    private static int determineCommonArrayType(final List<Expression> values) {
        Expression expr = values.get(0);
        int arrayElementType = -1;
        if (expr instanceof AnnotationConstantExpression) {
            arrayElementType = 1;
        } else if (expr instanceof ConstantExpression) {
            arrayElementType = 2;
        } else if (expr instanceof ClassExpression || expr instanceof ClosureExpression) {
            arrayElementType = 3;
        } else if (expr instanceof PropertyExpression) {
            arrayElementType = 4;
        }
        return arrayElementType;
    }

    private void doPostVisit(final ASTNode node) {
        Consumer<WriterController> callback = node.getNodeMetaData("classgen.callback");
        if (callback != null) callback.accept(controller);
    }

    private static boolean isParameterReference(Expression exp) {
        return (exp instanceof VariableExpression && ((VariableExpression) exp).getAccessedVariable() instanceof Parameter);
    }

    private void loadThis(final VariableExpression thisOrSuper) {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitVarInsn(ALOAD, 0);
        OperandStack operandStack = controller.getOperandStack();
        if (controller.isInGeneratedFunction() && !controller.getCompileStack().isImplicitThis()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Closure", "getThisObject", "()Ljava/lang/Object;", false);
            ClassNode expectedType = controller.getTypeChooser().resolveType(thisOrSuper, controller.getOutermostClass());
            if (!isObjectType(expectedType) && !isPrimitiveType(expectedType)) {
                BytecodeHelper.doCast(mv, expectedType);
                operandStack.push(expectedType);
            } else {
                operandStack.push(ClassHelper.OBJECT_TYPE);
            }
        } else {
            operandStack.push(controller.getClassNode());
        }
    }

    public void loadWrapper(final Expression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        ClassNode goalClass = expression.getType();
        visitClassExpression(classX(goalClass));
        if (goalClass.isDerivedFromGroovyObject()) {
            createGroovyObjectWrapperMethod.call(mv);
        } else {
            createPojoWrapperMethod.call(mv);
        }
        controller.getOperandStack().remove(1);
    }

    public void onLineNumber(final ASTNode node, final String message) {
        if (node != null && !(node instanceof BlockStatement)) {
            currentASTNode = node;
            int line = node.getLineNumber();
            if (line != controller.getLineNumber() || ASM_DEBUG) {
                controller.visitLineNumber(line);
            }
        }
    }

    public void throwException(final String message) {
        throw new RuntimeParserException(message, currentASTNode);
    }
}

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
package org.codehaus.groovy.transform;

import groovy.lang.GroovyClassLoader;
import groovy.transform.CompilationUnitAware;
import groovy.transform.NamedParam;
import groovy.transform.RecordBase;
import groovy.transform.RecordOptions;
import groovy.transform.RecordTypeMode;
import groovy.transform.options.PropertyHandler;
import org.apache.groovy.ast.tools.MethodNodeUtils;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.RecordComponentNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.TUPLE_CLASSES;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.arrayX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.bytecodeX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.caseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasDeclaredMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.indexX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.listX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.mapEntryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.mapX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.plusX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.switchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.thisPropX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.runtime.StringGroovyMethods.isAtLeast;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.IRETURN;

/**
 * Handles generation of code for the @RecordType annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class RecordTypeASTTransformation extends AbstractASTTransformation implements CompilationUnitAware {

    private static final ClassNode ILLEGAL_ARGUMENT = makeWithoutCaching(IllegalArgumentException.class);
    private static final ClassNode NAMED_PARAM_TYPE = make(NamedParam.class);
    private static final ClassNode RECORD_OPTIONS_TYPE = make(RecordOptions.class);
    private static final ClassNode SIMPLE_BEAN_INFO_TYPE = make(SimpleBeanInfo.class);
    private static final ClassNode BEAN_DESCRIPTOR_TYPE = make(BeanDescriptor.class);
    private static final ClassNode COLLECTIONS_TYPE = makeWithoutCaching(Collections.class, false);
    private static final ClassNode PROPERTY_DESCRIPTOR_TYPE = make(PropertyDescriptor.class);
    private static final ClassNode PROPERTY_DESCRIPTOR_ARRAY_TYPE = make(PropertyDescriptor[].class);
    private static final ClassNode EVENT_SET_DESCRIPTOR_TYPE = make(EventSetDescriptor.class);
    private static final ClassNode EVENT_SET_DESCRIPTOR_ARRAY_TYPE = make(EventSetDescriptor[].class);
    private static final ClassNode METHOD_DESCRIPTOR_TYPE = make(MethodDescriptor.class);
    private static final ClassNode METHOD_DESCRIPTOR_ARRAY_TYPE = make(MethodDescriptor[].class);
    private static final ClassNode IMAGE_TYPE = make(Image.class);

    private static final String COMPONENTS = "components";
    private static final String COPY_WITH = "copyWith";
    private static final String GET_AT = "getAt";
    private static final String NAMED_ARGS = "namedArgs";
    private static final String RECORD_CLASS_NAME = "java.lang.Record";
    private static final String SIZE = "size";
    private static final String TO_LIST = "toList";
    private static final String TO_MAP = "toMap";

    private static final Class<? extends Annotation> MY_CLASS = RecordBase.class;
    public static final ClassNode MY_TYPE = makeWithoutCaching(MY_CLASS, false);
    private static final String MY_TYPE_NAME = MY_TYPE.getNameWithoutPackage();

    private CompilationUnit compilationUnit;

    @Override
    public String getAnnotationName() {
        return MY_TYPE_NAME;
    }

    @Override
    public void setCompilationUnit(final CompilationUnit unit) {
        this.compilationUnit = unit;
    }

    protected GroovyClassLoader getTransformLoader() {
        return compilationUnit != null ? compilationUnit.getTransformLoader() : sourceUnit.getClassLoader();
    }

    /**
     * Indicates that the given classnode is a native JVM record class.
     * For classes being compiled, this will only be valid after the
     * {@code RecordTypeASTTransformation} transform has been invoked.
     */
    @Incubating
    public static boolean recordNative(final ClassNode node) {
        return node.getUnresolvedSuperClass() != null && RECORD_CLASS_NAME.equals(node.getUnresolvedSuperClass().getName());
    }

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (parent instanceof ClassNode && MY_TYPE.equals(anno.getClassNode())) {
            PropertyHandler handler = PropertyHandler.createPropertyHandler(this, getTransformLoader(), (ClassNode) parent);
            if (handler != null && handler.validateAttributes(this, anno)) {
                doProcessRecordType((ClassNode) parent, handler);
            }
        }
    }

    //--------------------------------------------------------------------------

    private void doProcessRecordType(final ClassNode cNode, final PropertyHandler handler) {
        if (cNode.getNodeMetaData("_RECORD_HEADER") != null) {
            cNode.putNodeMetaData("_SKIPPABLE_ANNOTATIONS", Boolean.TRUE);
        }
        List<AnnotationNode> annotations = cNode.getAnnotations(RECORD_OPTIONS_TYPE);
        AnnotationNode options = annotations.isEmpty() ? null : annotations.get(0);
        RecordTypeMode mode = getMode(options, "mode");
        boolean isAtLeastJDK16 = false;
        String message = "Expecting JDK16+ but unable to determine target bytecode";
        if (sourceUnit != null) {
            CompilerConfiguration config = sourceUnit.getConfiguration();
            String targetBytecode = config.getTargetBytecode();
            isAtLeastJDK16 = isAtLeast(targetBytecode, CompilerConfiguration.JDK16);
            message = "Expecting JDK16+ but found " + targetBytecode;
        }
        boolean isNative = (isAtLeastJDK16 && mode != RecordTypeMode.EMULATE);
        if (isNative) {
            String sName = cNode.getUnresolvedSuperClass().getName();
            // don't expect any parent to be set at this point but we only check at grammar
            // level when using the record keyword so do a few more sanity checks here
            if (!sName.equals(OBJECT) && !sName.equals(RECORD_CLASS_NAME)) {
                addError("Invalid superclass for native record found: " + sName, cNode);
            }
            cNode.setSuperClass(compilationUnit.getClassNodeResolver().resolveName(RECORD_CLASS_NAME, compilationUnit).getClassNode());
            cNode.setModifiers(cNode.getModifiers() | Opcodes.ACC_RECORD);
            final List<PropertyNode> pList = getInstanceProperties(cNode);
            if (!pList.isEmpty()) {
                cNode.setRecordComponents(new ArrayList<>());
            }
            for (PropertyNode pNode : pList) {
                ClassNode pType = pNode.getOriginType();
                ClassNode type = pType.getPlainNodeReference();
                type.setGenericsPlaceHolder(pType.isGenericsPlaceHolder());
                type.setGenericsTypes(pType.getGenericsTypes());
                RecordComponentNode rec = new RecordComponentNode(cNode, pNode.getName(), type, pNode.getAnnotations());
                rec.putNodeMetaData("_SKIPPABLE_ANNOTATIONS", Boolean.TRUE);
                cNode.getRecordComponents().add(rec);
            }
        } else if (mode == RecordTypeMode.NATIVE) {
            addError(message + " when attempting to create a native record", cNode);
        } else {
            createBeanInfoClass(cNode);
        }

        String cName = cNode.getName();
        if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
        makeClassFinal(this, cNode);
        makeInnerRecordStatic(cNode);

        final List<PropertyNode> pList = getInstanceProperties(cNode);
        for (PropertyNode pNode : pList) {
            adjustPropertyForShallowImmutability(cNode, pNode, handler);
            pNode.setModifiers(pNode.getModifiers() | ACC_FINAL);
        }
        final List<FieldNode> fList = cNode.getFields();
        for (FieldNode fNode : fList) {
            ensureNotPublic(this, cName, fNode);
        }
        // 0L serialVersionUID by default
        if (cNode.getDeclaredField("serialVersionUID") == null) {
            cNode.addField("serialVersionUID", ACC_PRIVATE | ACC_STATIC | ACC_FINAL, long_TYPE, constX(0L));
        }

        if (!hasAnnotation(cNode, ToStringASTTransformation.MY_TYPE)) {
            if (isNative) {
                createRecordToString(cNode);
            } else {
                ToStringASTTransformation.createToString(cNode, false, false, null,
                        null, true, false, false, false,
                        false, false, false, false, true,
                        new String[]{"[", "]", "=", ", "}, false);
            }
        }

        if (!hasAnnotation(cNode, EqualsAndHashCodeASTTransformation.MY_TYPE)) {
            if (isNative) {
                createRecordEquals(cNode);
                createRecordHashCode(cNode);
            } else {
                EqualsAndHashCodeASTTransformation.createEquals(cNode, false, false, false, null, null, false, false, true, false);
                EqualsAndHashCodeASTTransformation.createHashCode(cNode, true, false, false, null, null, false, false, true, false);
            }
        }

        if (hasAnnotation(cNode, TupleConstructorASTTransformation.MY_TYPE)) {
            AnnotationNode tupleCons = cNode.getAnnotations(TupleConstructorASTTransformation.MY_TYPE).get(0);
            if (unsupportedTupleAttribute(tupleCons, "excludes")) return;
            if (unsupportedTupleAttribute(tupleCons, "includes")) return;
            if (unsupportedTupleAttribute(tupleCons, "includeProperties")) return;
            if (unsupportedTupleAttribute(tupleCons, "includeSuperFields")) return;
        }

        if (options != null && memberHasValue(options, COPY_WITH, Boolean.TRUE) && !hasDeclaredMethod(cNode, COPY_WITH, 1)) {
            createCopyWith(cNode, pList);
        }

        if ((options == null || !memberHasValue(options, GET_AT, Boolean.FALSE)) && !hasDeclaredMethod(cNode, GET_AT, 1)) {
            createGetAt(cNode, pList);
        }

        if ((options == null || !memberHasValue(options, TO_LIST, Boolean.FALSE)) && !hasDeclaredMethod(cNode, TO_LIST, 0)) {
            createToList(cNode, pList);
        }

        if ((options == null || !memberHasValue(options, TO_MAP, Boolean.FALSE)) && !hasDeclaredMethod(cNode, TO_MAP, 0)) {
            createToMap(cNode, pList);
        }

        if (options != null && memberHasValue(options, COMPONENTS, Boolean.TRUE) && !hasDeclaredMethod(cNode, COMPONENTS, 0)) {
            createComponents(cNode, pList);
        }

        if ((options == null || !memberHasValue(options, SIZE, Boolean.FALSE)) && !hasDeclaredMethod(cNode, SIZE, 0)) {
            addGeneratedMethod(cNode, SIZE, ACC_PUBLIC | ACC_FINAL, int_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(constX(pList.size(), true)));
        }
    }

    private void createBeanInfoClass(ClassNode cNode) {
        ClassNode beanInfoClass = new ClassNode(cNode.getName() + "BeanInfo", ACC_PUBLIC, SIMPLE_BEAN_INFO_TYPE);
        beanInfoClass.addMethod("getBeanDescriptor", ACC_PUBLIC, BEAN_DESCRIPTOR_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(
            ctorX(BEAN_DESCRIPTOR_TYPE, args(classX(cNode)))
        ));
        final List<PropertyNode> pList = getInstanceProperties(cNode);
        BlockStatement block = new BlockStatement();
        VariableExpression p = varX("p", PROPERTY_DESCRIPTOR_ARRAY_TYPE);
        block.addStatement(
            declS(p, arrayX(PROPERTY_DESCRIPTOR_TYPE, Collections.emptyList(), List.of(constX(pList.size()))))
        );
        for (int i = 0; i < pList.size(); i++) {
            String name = pList.get(i).getName();
            block.addStatement(tryCatchS(
                assignS(indexX(p, constX(i)), ctorX(PROPERTY_DESCRIPTOR_TYPE, args(constX(name), classX(cNode), constX(name), nullX())))
            ));
        }
        block.addStatement(returnS(p));
        beanInfoClass.addMethod("getPropertyDescriptors", ACC_PUBLIC, PROPERTY_DESCRIPTOR_ARRAY_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, block);
        beanInfoClass.addMethod("getEventSetDescriptors", ACC_PUBLIC, EVENT_SET_DESCRIPTOR_ARRAY_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(
            arrayX(EVENT_SET_DESCRIPTOR_TYPE, Collections.emptyList(), List.of(constX(0)))
        ));
        beanInfoClass.addMethod("getMethodDescriptors", ACC_PUBLIC, METHOD_DESCRIPTOR_ARRAY_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(
            arrayX(METHOD_DESCRIPTOR_TYPE, Collections.emptyList(), List.of(constX(0)))
        ));
        beanInfoClass.addMethod("getDefaultPropertyIndex", ACC_PUBLIC, int_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(
            constX(-1)
        ));
        beanInfoClass.addMethod("getDefaultEventIndex", ACC_PUBLIC, int_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(
            constX(-1)
        ));
        beanInfoClass.addMethod("getIcon", ACC_PUBLIC, IMAGE_TYPE, params(param(int_TYPE, "iconKind")), ClassNode.EMPTY_ARRAY, returnS(
            nullX()
        ));
        cNode.getModule().addClass(beanInfoClass);
    }

    private void createComponents(ClassNode cNode, List<PropertyNode> pList) {
        if (pList.size() > 16) { // Groovy currently only goes to Tuple16
            addError("Record has too many components for a components() method", cNode);
        }
        ClassNode tuple = makeWithoutCaching(TUPLE_CLASSES[pList.size()], false);
        Statement body;
        if (pList.isEmpty()) {
            body = returnS(propX(classX(tuple), "INSTANCE"));
        } else {
            List<GenericsType> gtypes = new ArrayList<>();
            ArgumentListExpression args = new ArgumentListExpression();
            for (PropertyNode pNode : pList) {
                args.addExpression(callThisX(pNode.getName()));
                gtypes.add(new GenericsType(getWrapper(pNode.getType())));
            }
            tuple.setGenericsTypes(gtypes.toArray(GenericsType.EMPTY_ARRAY));
            body = returnS(ctorX(tuple, args));
        }
        addGeneratedMethod(cNode, COMPONENTS, ACC_PUBLIC | ACC_FINAL, tuple, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private void createToList(ClassNode cNode, List<PropertyNode> pList) {
        List<Expression> args = new ArrayList<>();
        for (PropertyNode pNode : pList) {
            args.add(callThisX(pNode.getName()));
        }
        Statement body = returnS(callX(COLLECTIONS_TYPE, "unmodifiableList", listX(args)));
        addGeneratedMethod(cNode, TO_LIST, ACC_PUBLIC | ACC_FINAL, LIST_TYPE.getPlainNodeReference(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private void createToMap(ClassNode cNode, List<PropertyNode> pList) {
        List<MapEntryExpression> entries = new ArrayList<>();
        for (PropertyNode pNode : pList) {
            String name = pNode.getName();
            entries.add(mapEntryX(name, callThisX(name)));
        }
        Statement body = returnS(callX(COLLECTIONS_TYPE, "unmodifiableMap", mapX(entries)));
        addGeneratedMethod(cNode, TO_MAP, ACC_PUBLIC | ACC_FINAL, MAP_TYPE.getPlainNodeReference(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private void createGetAt(ClassNode cNode, List<PropertyNode> pList) {
        Expression i = varX("i");
        SwitchStatement body = switchS(i, throwS(ctorX(ILLEGAL_ARGUMENT, args(plusX(constX("No record component with index: "), i)))));
        for (int j = 0; j < pList.size(); j++) {
            body.addCase(caseS(constX(j), returnS(callThisX(pList.get(j).getName()))));
        }
        addGeneratedMethod(cNode, GET_AT, ACC_PUBLIC | ACC_FINAL, OBJECT_TYPE, params(param(int_TYPE, "i")), ClassNode.EMPTY_ARRAY, body);
    }

    private void createCopyWith(ClassNode cNode, List<PropertyNode> pList) {
        ArgumentListExpression args = new ArgumentListExpression();
        Parameter mapParam = param(GenericsUtils.nonGeneric(MAP_TYPE), NAMED_ARGS);
        Expression mapArg = varX(mapParam);
        for (PropertyNode pNode : pList) {
            String name = pNode.getName();
            args.addExpression(ternaryX(callX(mapArg, "containsKey", args(constX(name))), propX(mapArg, name), thisPropX(true, name)));
            ClassNode pType = pNode.getType();
            ClassNode type = pType.getPlainNodeReference();
            type.setGenericsPlaceHolder(pType.isGenericsPlaceHolder());
            type.setGenericsTypes(pType.getGenericsTypes());
            AnnotationNode namedParam = new AnnotationNode(NAMED_PARAM_TYPE);
            namedParam.addMember("value", constX(name));
            namedParam.addMember("type", classX(type));
            namedParam.addMember("required", constX(false, true));
            mapParam.addAnnotation(namedParam);
        }
        Statement body = returnS(ctorX(cNode.getPlainNodeReference(), args));
        addGeneratedMethod(cNode, COPY_WITH, ACC_PUBLIC | ACC_FINAL, cNode.getPlainNodeReference(), params(mapParam), ClassNode.EMPTY_ARRAY, body);
    }

    private void createRecordToString(ClassNode cNode) {
        String desc = BytecodeHelper.getMethodDescriptor(STRING_TYPE, new ClassNode[]{cNode});
        Statement body = stmt(bytecodeX(STRING_TYPE, mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInvokeDynamicInsn("toString", desc, createBootstrapMethod(), createBootstrapMethodArguments(cNode));
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                })
        );
        addGeneratedMethod(cNode, "toString", ACC_PUBLIC | ACC_FINAL, STRING_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private void createRecordEquals(ClassNode cNode) {
        String desc = BytecodeHelper.getMethodDescriptor(boolean_TYPE, new ClassNode[]{cNode, OBJECT_TYPE});
        Statement body = stmt(bytecodeX(boolean_TYPE, mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitInvokeDynamicInsn("equals", desc, createBootstrapMethod(), createBootstrapMethodArguments(cNode));
                    mv.visitInsn(IRETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                })
        );
        addGeneratedMethod(cNode, "equals", ACC_PUBLIC | ACC_FINAL, boolean_TYPE, params(param(OBJECT_TYPE, "other")), ClassNode.EMPTY_ARRAY, body);
    }

    private void createRecordHashCode(ClassNode cNode) {
        String desc = BytecodeHelper.getMethodDescriptor(int_TYPE, new ClassNode[]{cNode});
        Statement body = stmt(bytecodeX(int_TYPE, mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInvokeDynamicInsn("hashCode", desc, createBootstrapMethod(), createBootstrapMethodArguments(cNode));
                    mv.visitInsn(IRETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                })
        );
        addGeneratedMethod(cNode, "hashCode", ACC_PUBLIC | ACC_FINAL, int_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private Object[] createBootstrapMethodArguments(ClassNode cNode) {
        String internalName = cNode.getName().replace('.', '/');
        String names = cNode.getRecordComponents().stream()
                                                    .map(RecordComponentNode::getName)
                                                    .collect(Collectors.joining(";"));
        List<Object> args = new LinkedList<>();
        args.add(Type.getType(BytecodeHelper.getTypeDescription(cNode)));
        args.add(names);
        cNode.getRecordComponents().forEach(rcn -> args.add(createFieldHandle(rcn, internalName)));
        return args.toArray();
    }

    private Object createFieldHandle(RecordComponentNode rcn, String cName) {
        return new Handle(
                Opcodes.H_GETFIELD,
                cName,
                rcn.getName(),
                BytecodeHelper.getTypeDescription(rcn.getType()),
                false
        );
    }

    private Handle createBootstrapMethod() {
        return new Handle(
                Opcodes.H_INVOKESTATIC,
                "java/lang/runtime/ObjectMethods",
                "bootstrap",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;",
                false
        );
    }

    private static RecordTypeMode getMode(AnnotationNode node, String name) {
        if (node != null) {
            final Expression member = node.getMember(name);
            if (member instanceof PropertyExpression) {
                PropertyExpression prop = (PropertyExpression) member;
                Expression oe = prop.getObjectExpression();
                if (oe instanceof ClassExpression) {
                    ClassExpression ce = (ClassExpression) oe;
                    if (ce.getType().getName().equals("groovy.transform.RecordTypeMode")) {
                        return RecordTypeMode.valueOf(prop.getPropertyAsString());
                    }
                }
            }
        }
        return null;
    }

    private boolean unsupportedTupleAttribute(AnnotationNode anno, String memberName) {
        if (getMemberValue(anno, memberName) != null) {
            String tname = TupleConstructorASTTransformation.MY_TYPE_NAME;
            addError("Error during " + MY_TYPE_NAME + " processing: Annotation attribute '" + memberName +
                    "' not supported for " + tname + " when used with " + MY_TYPE_NAME, anno);
            return true;
        }
        return false;
    }

    private static void makeInnerRecordStatic(ClassNode cNode) {
        if (cNode instanceof InnerClassNode) {
            cNode.setModifiers(cNode.getModifiers() | ACC_STATIC);
        }
    }

    private static void makeClassFinal(AbstractASTTransformation xform, ClassNode cNode) {
        int modifiers = cNode.getModifiers();
        if ((modifiers & ACC_FINAL) == 0) {
            if ((modifiers & (ACC_ABSTRACT | ACC_SYNTHETIC)) == (ACC_ABSTRACT | ACC_SYNTHETIC)) {
                xform.addError("Error during " + MY_TYPE_NAME + " processing: annotation found on inappropriate class " + cNode.getName(), cNode);
                return;
            }
            cNode.setModifiers(modifiers | ACC_FINAL);
        }
    }

    private static void ensureNotPublic(AbstractASTTransformation xform, String cNode, FieldNode fNode) {
        String fName = fNode.getName();
        if (fNode.isPublic() && !fName.contains("$") && !(fNode.isStatic() && fNode.isFinal())) {
            xform.addError("Public field '" + fName + "' not allowed for " + MY_TYPE_NAME + " class '" + cNode + "'.", fNode);
        }
    }

    // guarantee shallow immutability but property handler may do defensive copying
    private static void adjustPropertyForShallowImmutability(ClassNode cNode, PropertyNode pNode, PropertyHandler handler) {
        final FieldNode fNode = pNode.getField();
        fNode.setModifiers((pNode.getModifiers() & (~ACC_PUBLIC)) | ACC_FINAL | ACC_PRIVATE);
        boolean isGetterDefined = cNode.getDeclaredMethods(pNode.getName()).stream()
                .anyMatch(MethodNodeUtils::isGetterCandidate);
        pNode.setGetterName(pNode.getName());
        if (!isGetterDefined) {
            Statement getter = handler.createPropGetter(pNode);
            if (getter != null) {
                pNode.setGetterBlock(getter);
            }
        }
    }
}

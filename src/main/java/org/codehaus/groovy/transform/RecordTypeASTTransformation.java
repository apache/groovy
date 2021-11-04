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
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.bytecodeX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.caseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasDeclaredMethod;
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
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
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
    private CompilationUnit compilationUnit;
    private static final ClassNode ARRAYLIST_TYPE = makeWithoutCaching(ArrayList.class, false);
    private static final String COMPONENTS = "components";
    private static final String COPY_WITH = "copyWith";
    private static final String GET_AT = "getAt";
    private static final ClassNode ILLEGAL_ARGUMENT = makeWithoutCaching(IllegalArgumentException.class);
    private static final ClassNode LHMAP_TYPE = makeWithoutCaching(LinkedHashMap.class, false);
    private static final String NAMED_ARGS = "namedArgs";
    private static final ClassNode NAMED_PARAM_TYPE = makeWithoutCaching(NamedParam.class, false);
    private static final int PUBLIC_FINAL = ACC_PUBLIC | ACC_FINAL;
    private static final String RECORD_CLASS_NAME = "java.lang.Record";
    private static final ClassNode RECORD_OPTIONS_TYPE = make(RecordOptions.class);
    private static final String SIZE = "size";
    private static final String TO_LIST = "toList";
    private static final String TO_MAP = "toMap";

    private static final Class<? extends Annotation> MY_CLASS = RecordBase.class;
    public static final ClassNode MY_TYPE = makeWithoutCaching(MY_CLASS, false);
    private static final String MY_TYPE_NAME = MY_TYPE.getNameWithoutPackage();

    @Override
    public String getAnnotationName() {
        return MY_TYPE_NAME;
    }

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            final GroovyClassLoader classLoader = compilationUnit != null ? compilationUnit.getTransformLoader() : source.getClassLoader();
            final PropertyHandler handler = PropertyHandler.createPropertyHandler(this, classLoader, (ClassNode) parent);
            if (handler == null) return;
            if (!handler.validateAttributes(this, anno)) return;
            doProcessRecordType((ClassNode) parent, handler);
        }
    }

    private void doProcessRecordType(ClassNode cNode, PropertyHandler handler) {
        List<AnnotationNode> annotations = cNode.getAnnotations(RECORD_OPTIONS_TYPE);
        AnnotationNode options = annotations.isEmpty() ? null : annotations.get(0);
        RecordTypeMode mode = getMode(options, "mode");
        boolean isPostJDK16 = false;
        String message = "Expecting JDK16+ but unable to determine target bytecode";
        if (sourceUnit != null) {
            CompilerConfiguration config = sourceUnit.getConfiguration();
            String targetBytecode = config.getTargetBytecode();
            isPostJDK16 = CompilerConfiguration.isPostJDK16(targetBytecode);
            message = "Expecting JDK16+ but found " + targetBytecode;
        }
        boolean isNative = isPostJDK16 && mode != RecordTypeMode.EMULATE;
        if (isNative) {
            String sName = cNode.getUnresolvedSuperClass().getName();
            // don't expect any parent to be set at this point but we only check at grammar
            // level when using the record keyword so do a few more sanity checks here
            if (!sName.equals("java.lang.Object") && !sName.equals(RECORD_CLASS_NAME)) {
                addError("Invalid superclass for native record found: " + sName, cNode);
            }
            cNode.setSuperClass(ClassHelper.makeWithoutCaching(RECORD_CLASS_NAME));
            cNode.setModifiers(cNode.getModifiers() | Opcodes.ACC_RECORD);
            final List<PropertyNode> pList = getInstanceProperties(cNode);
            if (!pList.isEmpty()) {
                cNode.setRecordComponentNodes(new ArrayList<>());
            }
            for (PropertyNode pNode : pList) {
                cNode.getRecordComponentNodes().add(new RecordComponentNode(cNode, pNode.getName(), pNode.getOriginType(), pNode.getAnnotations()));
            }
        } else if (mode == RecordTypeMode.NATIVE) {
            addError(message + " when attempting to create a native record", cNode);
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
            cNode.addField("serialVersionUID", ACC_PRIVATE | ACC_STATIC | ACC_FINAL, ClassHelper.long_TYPE, constX(0L));
        }

        if (!hasAnnotation(cNode, ToStringASTTransformation.MY_TYPE)) {
            if (isNative) {
                createRecordToString(cNode);
            } else {
                ToStringASTTransformation.createToString(cNode, false, false, null,
                        null, true, false, false, true,
                        false, false, false, false, false,
                        new String[]{"[", "]", "=", ", "});
            }
        }

        if (!hasAnnotation(cNode, EqualsAndHashCodeASTTransformation.MY_TYPE)) {
            if (isNative) {
                createRecordEquals(cNode);
                createRecordHashCode(cNode);
            } else {
                EqualsAndHashCodeASTTransformation.createEquals(cNode, false, false, false, null, null);
                EqualsAndHashCodeASTTransformation.createHashCode(cNode, false, false, false, null, null);
            }
        }

        if (hasAnnotation(cNode, TupleConstructorASTTransformation.MY_TYPE)) {
            AnnotationNode tupleCons = cNode.getAnnotations(TupleConstructorASTTransformation.MY_TYPE).get(0);
            if (unsupportedTupleAttribute(tupleCons, "excludes")) return;
            if (unsupportedTupleAttribute(tupleCons, "includes")) return;
            if (unsupportedTupleAttribute(tupleCons, "includeProperties")) return;
            if (unsupportedTupleAttribute(tupleCons, "includeSuperFields")) return;
            if (unsupportedTupleAttribute(tupleCons, "callSuper")) return;
        }

        if ((options == null || !memberHasValue(options, COPY_WITH, Boolean.FALSE)) && !hasDeclaredMethod(cNode, COPY_WITH, 1)) {
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
            addGeneratedMethod(cNode, SIZE, PUBLIC_FINAL, int_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(constX(pList.size())));
        }
    }

    private void createComponents(ClassNode cNode, List<PropertyNode> pList) {
        if (pList.size() > 16) { // Groovy currently only goes to Tuple16
            addError("Record has too many components for a components() method", cNode);
        }
        ClassNode tupleClass = getClass(cNode, "groovy.lang.Tuple" + pList.size());
        if (tupleClass == null) return;
        Statement body;
        if (pList.isEmpty()) {
            body = returnS(propX(classX(tupleClass), "INSTANCE"));
        } else {
            List<GenericsType> gtypes = new ArrayList<>();
            ArgumentListExpression args = new ArgumentListExpression();
            for (PropertyNode pNode : pList) {
                args.addExpression(callThisX(pNode.getName()));
                gtypes.add(new GenericsType(getWrapper(pNode.getType())));
            }
            tupleClass.setGenericsTypes(gtypes.toArray(new GenericsType[0]));
            body = returnS(ctorX(tupleClass, args));
        }
        addGeneratedMethod(cNode, COMPONENTS, PUBLIC_FINAL, tupleClass, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private ClassNode getClass(ClassNode cNode, String tupleName) {
        try {
            return ClassHelper.makeWithoutCaching(Class.forName(tupleName)).getPlainNodeReference();
        } catch(ClassNotFoundException cnfe) {
            addError("Unable to find Tuple class '" + tupleName + "'", cNode);
            return null;
        }
    }

    private void createToList(ClassNode cNode, List<PropertyNode> pList) {
        List<Expression> args = new ArrayList<>();
        for (PropertyNode pNode : pList) {
            args.add(callThisX(pNode.getName()));
        }
        Statement body = returnS(ctorX(ARRAYLIST_TYPE.getPlainNodeReference(), listX(args)));
        addGeneratedMethod(cNode, TO_LIST, PUBLIC_FINAL, LIST_TYPE.getPlainNodeReference(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private void createToMap(ClassNode cNode, List<PropertyNode> pList) {
        List<MapEntryExpression> entries = new ArrayList<>();
        for (PropertyNode pNode : pList) {
            String name = pNode.getName();
            entries.add(mapEntryX(name, callThisX(name)));
        }
        Statement body = returnS(ctorX(LHMAP_TYPE.getPlainNodeReference(), args(mapX(entries))));
        addGeneratedMethod(cNode, TO_MAP, PUBLIC_FINAL, MAP_TYPE.getPlainNodeReference(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private void createGetAt(ClassNode cNode, List<PropertyNode> pList) {
        Expression i = varX("i");
        SwitchStatement body = switchS(i, throwS(ctorX(ILLEGAL_ARGUMENT, args(plusX(constX("No record component with index: "), i)))));
        for (int j = 0; j < pList.size(); j++) {
            body.addCase(caseS(constX(j), returnS(callThisX(pList.get(j).getName()))));
        }
        addGeneratedMethod(cNode, GET_AT, PUBLIC_FINAL, ClassHelper.OBJECT_TYPE.getPlainNodeReference(), params(param(int_TYPE, "i")), ClassNode.EMPTY_ARRAY, body);
    }

    private void createCopyWith(ClassNode cNode, List<PropertyNode> pList) {
        ArgumentListExpression args = new ArgumentListExpression();
        Parameter mapParam = param(GenericsUtils.nonGeneric(MAP_TYPE), NAMED_ARGS);
        Expression mapArg = varX(mapParam);
        for (PropertyNode pNode : pList) {
            String name = pNode.getName();
            args.addExpression(ternaryX(callX(mapArg, "containsKey", args(constX(name))), propX(mapArg, name), thisPropX(true, name)));
            AnnotationNode namedParam = new AnnotationNode(NAMED_PARAM_TYPE);
            namedParam.addMember("value", constX(name));
            namedParam.addMember("type", classX(pNode.getType()));
            namedParam.addMember("required", constX(false, true));
            mapParam.addAnnotation(namedParam);
        }
        Statement body = returnS(nullX() /*ctorX(cNode.getPlainNodeReference(), args)*/); // TODO FIX
        addGeneratedMethod(cNode, COPY_WITH, PUBLIC_FINAL, cNode.getPlainNodeReference(), params(mapParam), ClassNode.EMPTY_ARRAY, body);
    }

    private void createRecordToString(ClassNode cNode) {
        String desc = BytecodeHelper.getMethodDescriptor(ClassHelper.STRING_TYPE, new ClassNode[]{cNode});
        Statement body = stmt(bytecodeX(ClassHelper.STRING_TYPE, mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInvokeDynamicInsn("toString", desc, createBootstrapMethod(), createBootstrapMethodArguments(cNode));
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                })
        );
        addGeneratedMethod(cNode, "toString", PUBLIC_FINAL, ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private void createRecordEquals(ClassNode cNode) {
        String desc = BytecodeHelper.getMethodDescriptor(ClassHelper.boolean_TYPE, new ClassNode[]{cNode, ClassHelper.OBJECT_TYPE});
        Statement body = stmt(bytecodeX(ClassHelper.boolean_TYPE, mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitInvokeDynamicInsn("equals", desc, createBootstrapMethod(), createBootstrapMethodArguments(cNode));
                    mv.visitInsn(IRETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                })
        );
        addGeneratedMethod(cNode, "equals", PUBLIC_FINAL, ClassHelper.boolean_TYPE, params(param(ClassHelper.OBJECT_TYPE, "other")), ClassNode.EMPTY_ARRAY, body);
    }

    private void createRecordHashCode(ClassNode cNode) {
        String desc = BytecodeHelper.getMethodDescriptor(ClassHelper.int_TYPE, new ClassNode[]{cNode});
        Statement body = stmt(bytecodeX(ClassHelper.int_TYPE, mv -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInvokeDynamicInsn("hashCode", desc, createBootstrapMethod(), createBootstrapMethodArguments(cNode));
                    mv.visitInsn(IRETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                })
        );
        addGeneratedMethod(cNode, "hashCode", PUBLIC_FINAL, ClassHelper.int_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private Object[] createBootstrapMethodArguments(ClassNode cNode) {
        String internalName = cNode.getName().replace('.', '/');
        String names = cNode.getRecordComponentNodes().stream().map(RecordComponentNode::getName).collect(Collectors.joining(";"));
        List<Object> args = new LinkedList<>();
        args.add(Type.getType(BytecodeHelper.getTypeDescription(cNode)));
        args.add(names);
        cNode.getRecordComponentNodes().stream().forEach(rcn -> args.add(createFieldHandle(rcn, internalName)));
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
        if (!isGetterDefined) {
            pNode.setGetterName(pNode.getName());
            Statement getter = handler.createPropGetter(pNode);
            if (getter != null) {
                pNode.setGetterBlock(getter);
            }
        }
    }

    @Override
    public void setCompilationUnit(CompilationUnit unit) {
        this.compilationUnit = unit;
    }
}

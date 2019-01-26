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
import groovy.lang.MetaClass;
import groovy.lang.MissingPropertyException;
import groovy.transform.CompilationUnitAware;
import groovy.transform.ImmutableBase;
import groovy.transform.options.PropertyHandler;
import org.apache.groovy.ast.tools.ImmutablePropertyUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasExplicitConstructor;
import static org.apache.groovy.ast.tools.ImmutablePropertyUtils.builtinOrMarkedImmutableClass;
import static org.apache.groovy.ast.tools.ImmutablePropertyUtils.createErrorMessage;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.eqX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getGetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasDeclaredMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isTrueX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.neX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.orX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles generation of code for the @Immutable annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ImmutableASTTransformation extends AbstractASTTransformation implements CompilationUnitAware {
    private CompilationUnit compilationUnit;

    private static final Class<? extends Annotation> MY_CLASS = ImmutableBase.class;
    public static final ClassNode MY_TYPE = makeWithoutCaching(MY_CLASS, false);
    private static final String MY_TYPE_NAME = MY_TYPE.getNameWithoutPackage();

    private static final String MEMBER_ADD_COPY_WITH = "copyWith";
    private static final String COPY_WITH_METHOD = "copyWith";

    private static final ClassNode HMAP_TYPE = makeWithoutCaching(HashMap.class, false);

    @Override
    public String getAnnotationName() {
        return MY_TYPE_NAME;
    }

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
            doMakeImmutable((ClassNode) parent, anno, handler);
        }
    }

    private void doMakeImmutable(ClassNode cNode, AnnotationNode node, PropertyHandler handler) {
        List<PropertyNode> newProperties = new ArrayList<>();

        String cName = cNode.getName();
        if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
        makeClassFinal(this, cNode);

        final List<PropertyNode> pList = getInstanceProperties(cNode);
        for (PropertyNode pNode : pList) {
            adjustPropertyForImmutability(pNode, newProperties, handler);
        }
        for (PropertyNode pNode : newProperties) {
            cNode.getProperties().remove(pNode);
            addProperty(cNode, pNode);
        }
        final List<FieldNode> fList = cNode.getFields();
        for (FieldNode fNode : fList) {
            ensureNotPublic(this, cName, fNode);
        }
        if (hasAnnotation(cNode, TupleConstructorASTTransformation.MY_TYPE)) {
            // TODO make this a method to be called from TupleConstructor xform, add check for 'defaults'?
            AnnotationNode tupleCons = cNode.getAnnotations(TupleConstructorASTTransformation.MY_TYPE).get(0);
            if (unsupportedTupleAttribute(tupleCons, "excludes")) return;
            if (unsupportedTupleAttribute(tupleCons, "includes")) return;
            if (unsupportedTupleAttribute(tupleCons, "includeFields")) return;
            if (unsupportedTupleAttribute(tupleCons, "includeProperties")) return;
            if (unsupportedTupleAttribute(tupleCons, "includeSuperFields")) return;
            if (unsupportedTupleAttribute(tupleCons, "callSuper")) return;
//            if (unsupportedTupleAttribute(tupleCons, "useSetters")) return;
            if (unsupportedTupleAttribute(tupleCons, "force")) return;
        }
        if (hasExplicitConstructor(this, cNode)) return;
        if (memberHasValue(node, MEMBER_ADD_COPY_WITH, true) && !pList.isEmpty() &&
                !hasDeclaredMethod(cNode, COPY_WITH_METHOD, 1)) {
            createCopyWith(cNode, pList);
        }
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

    static boolean isSpecialNamedArgCase(List<PropertyNode> list, boolean checkSize) {
        if (checkSize && list.size() != 1) return false;
        if (list.size() == 0) return false;
        ClassNode firstParamType = list.get(0).getField().getType();
        if (firstParamType.equals(ClassHelper.MAP_TYPE)) {
            return true;
        }
        ClassNode candidate = HMAP_TYPE;
        while (candidate != null) {
            if (candidate.equals(firstParamType)) {
                return true;
            }
            candidate = candidate.getSuperClass();
        }
        return false;
    }

    private static void ensureNotPublic(AbstractASTTransformation xform, String cNode, FieldNode fNode) {
        String fName = fNode.getName();
        // TODO: do we need to lock down things like: $ownClass
        if (fNode.isPublic() && !fName.contains("$") && !(fNode.isStatic() && fNode.isFinal())) {
            xform.addError("Public field '" + fName + "' not allowed for " + MY_TYPE_NAME + " class '" + cNode + "'.", fNode);
        }
    }

    private static void addProperty(ClassNode cNode, PropertyNode pNode) {
        final FieldNode fn = pNode.getField();
        cNode.getFields().remove(fn);
        cNode.addProperty(pNode.getName(), pNode.getModifiers() | ACC_FINAL, pNode.getType(),
                pNode.getInitialExpression(), pNode.getGetterBlock(), pNode.getSetterBlock());
        final FieldNode newfn = cNode.getField(fn.getName());
        cNode.getFields().remove(newfn);
        cNode.addField(fn);
    }

    static boolean makeImmutable(ClassNode cNode) {
        List<AnnotationNode> annotations = cNode.getAnnotations(ImmutablePropertyUtils.IMMUTABLE_OPTIONS_TYPE);
        AnnotationNode annoImmutable = annotations.isEmpty() ? null : annotations.get(0);
        return annoImmutable != null;
    }

    private static void adjustPropertyForImmutability(PropertyNode pNode, List<PropertyNode> newNodes, PropertyHandler handler) {
        final FieldNode fNode = pNode.getField();
        fNode.setModifiers((pNode.getModifiers() & (~ACC_PUBLIC)) | ACC_FINAL | ACC_PRIVATE);
        pNode.setSetterBlock(null);
        Statement getter = handler.createPropGetter(pNode);
        if (getter != null) {
            pNode.setGetterBlock(getter);
        }
        newNodes.add(pNode);
    }

    private static Statement createCheckForProperty(final PropertyNode pNode) {
        return block(
                new VariableScope(),
                ifElseS(
                        callX(
                                varX("map", HMAP_TYPE),
                                "containsKey",
                                args(constX(pNode.getName()))
                        ),
                        block(
                                new VariableScope(),
                                declS(
                                        varX("newValue", ClassHelper.OBJECT_TYPE),
                                        callX(
                                                varX("map", HMAP_TYPE),
                                                "get",
                                                args(constX(pNode.getName()))
                                        )
                                ),
                                declS(
                                        varX("oldValue", ClassHelper.OBJECT_TYPE),
                                        callThisX(getGetterName(pNode))
                                ),
                                ifS(
                                        neX(
                                                varX("newValue", ClassHelper.OBJECT_TYPE),
                                                varX("oldValue", ClassHelper.OBJECT_TYPE)
                                        ),
                                        block(
                                                new VariableScope(),
                                                assignS(
                                                        varX("oldValue", ClassHelper.OBJECT_TYPE),
                                                        varX("newValue", ClassHelper.OBJECT_TYPE)),
                                                assignS(
                                                        varX("dirty", ClassHelper.boolean_TYPE),
                                                        ConstantExpression.TRUE)
                                        )
                                ),
                                stmt(callX(
                                        varX("construct", HMAP_TYPE),
                                        "put",
                                        args(
                                                constX(pNode.getName()),
                                                varX("oldValue", ClassHelper.OBJECT_TYPE)
                                        )
                                ))
                        ),
                        block(
                                new VariableScope(),
                                stmt(callX(
                                        varX("construct", HMAP_TYPE),
                                        "put",
                                        args(
                                                constX(pNode.getName()),
                                                callThisX(getGetterName(pNode))
                                        )
                                ))
                        )
                )
        );
    }

    private static void createCopyWith(final ClassNode cNode, final List<PropertyNode> pList) {
        BlockStatement body = new BlockStatement();
        body.addStatement(ifS(
                orX(
                        equalsNullX(varX("map", ClassHelper.MAP_TYPE)),
                        eqX(callX(varX("map", HMAP_TYPE), "size"), constX(0))
                ),
                returnS(varX("this", cNode))
        ));
        body.addStatement(declS(varX("dirty", ClassHelper.boolean_TYPE), ConstantExpression.PRIM_FALSE));
        body.addStatement(declS(varX("construct", HMAP_TYPE), ctorX(HMAP_TYPE)));

        // Check for each property
        for (final PropertyNode pNode : pList) {
            body.addStatement(createCheckForProperty(pNode));
        }

        body.addStatement(returnS(ternaryX(
                isTrueX(varX("dirty", ClassHelper.boolean_TYPE)),
                ctorX(cNode, args(varX("construct", HMAP_TYPE))),
                varX("this", cNode)
        )));

        final ClassNode clonedNode = cNode.getPlainNodeReference();

        addGeneratedMethod(cNode, COPY_WITH_METHOD,
                ACC_PUBLIC | ACC_FINAL,
                clonedNode,
                params(new Parameter(new ClassNode(Map.class), "map")),
                null,
                body);
    }

    /**
     * This method exists to be binary compatible with 1.7 - 1.8.6 compiled code.
     */
    @SuppressWarnings("Unchecked")
    public static Object checkImmutable(String className, String fieldName, Object field) {
        if (field == null || field instanceof Enum || ImmutablePropertyUtils.isBuiltinImmutable(field.getClass().getName())) return field;
        if (field instanceof Collection) return DefaultGroovyMethods.asImmutable((Collection) field);
        if (getAnnotationByName(field, "groovy.transform.Immutable") != null) return field;

        final String typeName = field.getClass().getName();
        throw new RuntimeException(createErrorMessage(className, fieldName, typeName, "constructing"));
    }

    private static Annotation getAnnotationByName(Object field, String name) {
        // find longhand since the annotation from earlier versions is now a meta annotation
        for (Annotation an : field.getClass().getAnnotations()) {
            if (an.getClass().getName().equals(name)) {
                return an;
            }
        }
        return null;
    }

    /**
     * For compatibility with pre 2.5 compiled classes
     */
    @SuppressWarnings("Unchecked")
    public static Object checkImmutable(Class<?> clazz, String fieldName, Object field) {
        if (field == null || field instanceof Enum || builtinOrMarkedImmutableClass(field.getClass())) {
            return field;
        }

        boolean isImmutable = false;
        for (Annotation an : field.getClass().getAnnotations()) {
            if (an.getClass().getName().startsWith("groovy.transform.Immutable")) {
                isImmutable = true;
                break;
            }
        }
        if (isImmutable) return field;

        if (field instanceof Collection) {
            Field declaredField;
            try {
                declaredField = clazz.getDeclaredField(fieldName);
                Class<?> fieldType = declaredField.getType();
                if (Collection.class.isAssignableFrom(fieldType)) {
                    return DefaultGroovyMethods.asImmutable((Collection) field);
                }
                // potentially allow Collection coercion for a constructor
                if (builtinOrMarkedImmutableClass(fieldType)) {
                    return field;
                }
            } catch (NoSuchFieldException ignore) {
                // ignore
            }
        }
        final String typeName = field.getClass().getName();
        throw new RuntimeException(createErrorMessage(clazz.getName(), fieldName, typeName, "constructing"));
    }

    @SuppressWarnings("Unchecked")
    public static Object checkImmutable(Class<?> clazz, String fieldName, Object field, List<String> knownImmutableFieldNames, List<Class> knownImmutableClasses) {
        if (field == null || field instanceof Enum || builtinOrMarkedImmutableClass(field.getClass()) || knownImmutableFieldNames.contains(fieldName) || knownImmutableClasses.contains(field.getClass())) {
            return field;
        }

        boolean isImmutable = false;
        for (Annotation an : field.getClass().getAnnotations()) {
            if (an.getClass().getName().startsWith("groovy.transform.Immutable")) {
                isImmutable = true;
                break;
            }
        }
        if (isImmutable) return field;

        if (field instanceof Collection) {
            Field declaredField;
            try {
                declaredField = clazz.getDeclaredField(fieldName);
                Class<?> fieldType = declaredField.getType();
                if (Collection.class.isAssignableFrom(fieldType)) {
                    return DefaultGroovyMethods.asImmutable((Collection) field);
                }
                // potentially allow Collection coercion for a constructor
                if (builtinOrMarkedImmutableClass(fieldType) || knownImmutableClasses.contains(fieldType)) {
                    return field;
                }
            } catch (NoSuchFieldException ignore) {
                // ignore
            }
        }
        final String typeName = field.getClass().getName();
        throw new RuntimeException(createErrorMessage(clazz.getName(), fieldName, typeName, "constructing"));
    }

    public static void checkPropNames(Object instance, Map<String, Object> args) {
        final MetaClass metaClass = InvokerHelper.getMetaClass(instance);
        for (String k : args.keySet()) {
            if (metaClass.hasProperty(instance, k) == null)
                throw new MissingPropertyException(k, instance.getClass());
        }
    }

    @Override
    public void setCompilationUnit(CompilationUnit unit) {
        this.compilationUnit = unit;
    }
}

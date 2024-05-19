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

import groovy.lang.Delegate;
import groovy.lang.Lazy;
import groovy.lang.Reference;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.tools.BeanUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.copyOf;
import static java.util.stream.Collectors.toSet;
import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.DEPRECATED_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isGroovyObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllMethods;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getGetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInterfacesAndSuperInterfaces;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.addMethodGenerics;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpecRecurse;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.extractSuperClassGenerics;
import static org.codehaus.groovy.ast.tools.GenericsUtils.nonGeneric;
import static org.codehaus.groovy.ast.tools.ParameterUtils.parametersEqual;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * Handles generation of code for the <code>@Delegate</code> annotation
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class DelegateASTTransformation extends AbstractASTTransformation {

    private static final Class<?> MY_CLASS = Delegate.class;
    private static final ClassNode MY_TYPE = make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode LAZY_TYPE = make(Lazy.class);

    private static final String MEMBER_DEPRECATED = "deprecated";
    private static final String MEMBER_INTERFACES = "interfaces";
    private static final String MEMBER_INCLUDES = "includes";
    private static final String MEMBER_EXCLUDES = "excludes";
    private static final String MEMBER_INCLUDE_TYPES = "includeTypes";
    private static final String MEMBER_EXCLUDE_TYPES = "excludeTypes";
    private static final String MEMBER_PARAMETER_ANNOTATIONS = "parameterAnnotations";
    private static final String MEMBER_METHOD_ANNOTATIONS = "methodAnnotations";
    private static final String MEMBER_ALL_NAMES = "allNames";

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        DelegateDescription delegate = null;

        if (parent instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) parent;

            delegate = new DelegateDescription();
            delegate.delegate = fieldNode;
            delegate.annotation = node;
            delegate.name = fieldNode.getName();
            delegate.type = fieldNode.getType();
            delegate.owner = fieldNode.getOwner();
            delegate.getOp = varX(fieldNode);
            delegate.origin = "field";
        } else if (parent instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) parent;

            delegate = new DelegateDescription();
            delegate.delegate = methodNode;
            delegate.annotation = node;
            delegate.name = methodNode.getName();
            delegate.type = methodNode.getReturnType();
            delegate.owner = methodNode.getDeclaringClass();
            delegate.getOp = callThisX(delegate.name);
            delegate.origin = "method";

            if (methodNode.getParameters().length > 0) {
                addError("You can only delegate to methods that take no parameters, but " +
                         delegate.name + " takes " + methodNode.getParameters().length +
                         " parameters.", parent);
                return;
            }
        }

        if (delegate != null) {
            if (isObjectType(delegate.type) || isGroovyObjectType(delegate.type)) {
                addError(MY_TYPE_NAME + " " + delegate.origin + " '" + delegate.name + "' has an inappropriate type: " + delegate.type.getName() +
                        ". Please add an explicit type but not java.lang.Object or groovy.lang.GroovyObject.", parent);
                return;
            }
            if (delegate.type.equals(delegate.owner)) {
                addError(MY_TYPE_NAME + " " + delegate.origin + " '" + delegate.name + "' has an inappropriate type: " + delegate.type.getName() +
                        ". Delegation to own type not supported. Please use a different type.", parent);
                return;
            }

            final boolean skipInterfaces = memberHasValue(node, MEMBER_INTERFACES, Boolean.FALSE);
            final boolean includeDeprecated = memberHasValue(node, MEMBER_DEPRECATED, Boolean.TRUE) || (delegate.type.isInterface() && !skipInterfaces);
            final boolean allNames = memberHasValue(node, MEMBER_ALL_NAMES, Boolean.TRUE);
            delegate.excludes = getMemberStringList(node, MEMBER_EXCLUDES);
            delegate.includes = getMemberStringList(node, MEMBER_INCLUDES);
            delegate.excludeTypes = getMemberClassList(node, MEMBER_EXCLUDE_TYPES);
            delegate.includeTypes = getMemberClassList(node, MEMBER_INCLUDE_TYPES);
            checkIncludeExcludeUndefinedAware(node, delegate.excludes, delegate.includes,
                                              delegate.excludeTypes, delegate.includeTypes, MY_TYPE_NAME);
            if (!checkPropertyOrMethodList(delegate.type, delegate.excludes, "excludes", node, MY_TYPE_NAME)) return;
            if (!checkPropertyOrMethodList(delegate.type, delegate.includes, "includes", node, MY_TYPE_NAME)) return;

            final Iterable<MethodNode> ownerMethods = getAllMethods(delegate.owner);
            final Iterable<MethodNode> delegateMethods = filterMethods(collectMethods(delegate.type), delegate, allNames, includeDeprecated);

            for (MethodNode mn : delegateMethods) {
                addDelegateMethod(mn, delegate, ownerMethods);
            }

            for (PropertyNode prop : getAllProperties(delegate.type)) {
                if (prop.isStatic() || !prop.isPublic())
                    continue;
                String name = prop.getName();
                addGetterIfNeeded(delegate, prop, name, allNames);
                addSetterIfNeeded(delegate, prop, name, allNames);
            }

            if (delegate.type.isArray()) {
                boolean skipLength = delegate.excludes != null && (delegate.excludes.contains("length") || delegate.excludes.contains("getLength"));
                if (!skipLength) {
                    addGeneratedMethod(
                            delegate.owner,
                            "getLength",
                            ACC_PUBLIC,
                            ClassHelper.int_TYPE,
                            Parameter.EMPTY_ARRAY,
                            null,
                            returnS(propX(delegate.getOp, "length"))
                    );
                }
            }

            if (skipInterfaces) return;

            Set<ClassNode> addedInterfaces = getInterfacesAndSuperInterfaces(delegate.type);
            addedInterfaces.removeIf(i -> (i.getModifiers() & (ACC_PUBLIC | ACC_SYNTHETIC)) != ACC_PUBLIC || i.isSealed()); // GROOVY-7288 and JDK16+
            if (!addedInterfaces.isEmpty()) {
                Set<ClassNode> ownerInterfaces = getInterfacesAndSuperInterfaces(delegate.owner);
                for (ClassNode i : addedInterfaces) {
                    if (!ownerInterfaces.contains(i)) {
                        ClassNode[] faces = delegate.owner.getInterfaces();
                        faces = copyOf(faces, faces.length + 1);
                        faces[faces.length - 1] = i;

                        delegate.owner.setInterfaces(faces);
                    }
                }
            }
        }
    }

    private static Collection<MethodNode> collectMethods(final ClassNode type) {
        List<MethodNode> methods = new java.util.LinkedList<>(getAllMethods(type));
        // GROOVY-4320, GROOVY-4516
        for (ListIterator<MethodNode> it = methods.listIterator(); it.hasNext();) {
            MethodNode next = it.next();
            if (next.isPublic() && !next.isAbstract() && next.hasDefaultValue()) {
                int n = 0;
                for (Parameter p : next.getParameters())
                    if (p.hasInitialExpression()) n += 1;
                for (int i = 1; i <= n; i += 1) { // from Verifier#addDefaultParameters
                    Parameter[] params = new Parameter[next.getParameters().length - i];
                    int index = 0, j = 1;
                    for (Parameter parameter : next.getParameters()) {
                        if (j > n - i && parameter.hasInitialExpression()) {
                            j += 1;
                        } else {
                            params[index++] = parameter;
                            if (parameter.hasInitialExpression()) j += 1;
                        }
                    }

                    if (methods.stream().noneMatch(mn -> mn.getName().equals(next.getName()) && parametersEqual(mn.getParameters(), params))) {
                        MethodNode mn = new MethodNode(next.getName(), next.getModifiers(), next.getReturnType(), params, next.getExceptions(), null);
                        mn.setDeclaringClass(next.getDeclaringClass());
                        mn.setGenericsTypes(next.getGenericsTypes());
                        mn.addAnnotations(next.getAnnotations());
                        it.add(mn);
                    }
                }
            }
        }

        for (ClassNode face : type.getAllInterfaces()) {
            methods.addAll(face.getMethods());
        }

        return methods;
    }

    private static Collection<MethodNode> filterMethods(final Collection<MethodNode> methods, final DelegateDescription delegate, final boolean allNames, final boolean includeDeprecated) {
        Set<String> groovyObjectMethods = ClassHelper.GROOVY_OBJECT_TYPE.getMethods().stream().map(MethodNode::getTypeDescriptor).collect(toSet());
        Set<String> javaObjectMethods = ClassHelper.OBJECT_TYPE.getMethods().stream().map(MethodNode::getTypeDescriptor).collect(toSet());
        Set<String> ownClassMethods = delegate.owner.getMethods().stream().map(MethodNode::getTypeDescriptor).collect(toSet());

        methods.removeIf(candidate -> {
            if (!candidate.isPublic() || candidate.isStatic() || (candidate.getModifiers () & ACC_SYNTHETIC) != 0) return true;

            if (shouldSkip(candidate.getName(), delegate.excludes, delegate.includes, allNames)) return true;

            if (!includeDeprecated && !candidate.getAnnotations(DEPRECATED_TYPE).isEmpty()) return true;

            if (groovyObjectMethods.contains(candidate.getTypeDescriptor())) return true;

            if (javaObjectMethods.contains(candidate.getTypeDescriptor())) return true;

            if (ownClassMethods.contains(candidate.getTypeDescriptor())) return true;

            return false;
        });

        return methods;
    }

    private boolean checkPropertyOrMethodList(final ClassNode cNode, final List<String> propertyNameList, final String listName, final AnnotationNode anno, final String typeName) {
        if (propertyNameList == null || propertyNameList.isEmpty()) {
            return true;
        }
        final Set<String> pNames = new HashSet<>();
        final Set<String> mNames = new HashSet<>();
        for (PropertyNode pNode : BeanUtils.getAllProperties(cNode, false, false, false)) {
            String name = pNode.getField().getName();
            pNames.add(name);
            // add getter/setters since Groovy compiler hasn't added property accessors yet
            if ((pNode.getModifiers() & ACC_FINAL) == 0) {
                mNames.add(getSetterName(name));
            }
            mNames.add(getGetterName(name));
            if (isPrimitiveBoolean(pNode.getOriginType())) {
                mNames.add(getGetterName(name, Boolean.TYPE));
            }
        }
        for (MethodNode mNode : cNode.getAllDeclaredMethods()) {
            mNames.add(mNode.getName());
        }
        boolean result = true;
        for (String name : propertyNameList) {
            if (!pNames.contains(name) && !mNames.contains(name)) {
                addError("Error during " + typeName + " processing: '" + listName + "' property or method '" + name + "' does not exist.", anno);
                result = false;
            }
        }
        return result;
    }

    private static void addSetterIfNeeded(final DelegateDescription delegate, final PropertyNode prop, final String name, final boolean allNames) {
        String setterName = getSetterName(name);
        if (!prop.isFinal()
                && delegate.owner.getSetterMethod(setterName) == null && delegate.owner.getProperty(name) == null
                && !shouldSkipPropertyMethod(name, setterName, delegate.excludes, delegate.includes, allNames)) {
            addGeneratedMethod(
                    delegate.owner,
                    setterName,
                    ACC_PUBLIC,
                    ClassHelper.VOID_TYPE,
                    params(new Parameter(nonGeneric(prop.getType()), "value")),
                    null,
                    assignS(propX(delegate.getOp, name), varX("value"))
            );
        }
    }

    private static void addGetterIfNeeded(final DelegateDescription delegate, final PropertyNode prop, final String name, final boolean allNames) {
        boolean isPrimBool = isPrimitiveBoolean(prop.getOriginType());
        // do a little bit of pre-work since Groovy compiler hasn't added property accessors yet
        boolean willHaveGetAccessor = true;
        boolean willHaveIsAccessor = isPrimBool;
        String getterName = getGetterName(name);
        String  isserName = getGetterName(name, Boolean.TYPE);
        if (isPrimBool) {
            ClassNode cNode = prop.getDeclaringClass();
            if (cNode.getGetterMethod(isserName) != null && cNode.getGetterMethod(getterName) == null)
                willHaveGetAccessor = false;
            if (cNode.getGetterMethod(getterName) != null && cNode.getGetterMethod(isserName) == null)
                willHaveIsAccessor = false;
        }
        Reference<Boolean> ownerWillHaveGetAccessor = new Reference<Boolean>();
        Reference<Boolean> ownerWillHaveIsAccessor = new Reference<Boolean>();
        extractAccessorInfo(delegate.owner, name, ownerWillHaveGetAccessor, ownerWillHaveIsAccessor);

        if (willHaveGetAccessor && !ownerWillHaveGetAccessor.get()
                && !shouldSkipPropertyMethod(name, getterName, delegate.excludes, delegate.includes, allNames)) {
            addGeneratedMethod(
                    delegate.owner,
                    getterName,
                    ACC_PUBLIC,
                    nonGeneric(prop.getType()),
                    Parameter.EMPTY_ARRAY,
                    null,
                    returnS(propX(delegate.getOp, name))
            );
        }

        if (willHaveIsAccessor && !ownerWillHaveIsAccessor.get()
                && !shouldSkipPropertyMethod(name, getterName, delegate.excludes, delegate.includes, allNames)) {
            addGeneratedMethod(
                    delegate.owner,
                    isserName,
                    ACC_PUBLIC,
                    nonGeneric(prop.getType()),
                    Parameter.EMPTY_ARRAY,
                    null,
                    returnS(propX(delegate.getOp, name))
            );
        }
    }

    private static void extractAccessorInfo(final ClassNode owner, final String name, final Reference<Boolean> willHaveGetAccessor, final Reference<Boolean> willHaveIsAccessor) {
        boolean hasGetAccessor = owner.getGetterMethod(getGetterName(name)) != null;
        boolean hasIsAccessor  = owner.getGetterMethod(getGetterName(name, Boolean.TYPE)) != null;
        PropertyNode prop = owner.getProperty(name);
        willHaveGetAccessor.set(hasGetAccessor || (prop != null && !hasIsAccessor));
        willHaveIsAccessor.set(hasIsAccessor || (prop != null && !hasGetAccessor && isPrimitiveBoolean(prop.getOriginType())));
    }

    private static boolean shouldSkipPropertyMethod(final String propertyName, final String methodName, final List<String> excludes, final List<String> includes, final boolean allNames) {
        return ((!allNames && deemedInternalName(propertyName))
                    || excludes != null && (excludes.contains(propertyName) || excludes.contains(methodName))
                    || (includes != null && !includes.isEmpty() && !includes.contains(propertyName) && !includes.contains(methodName)));
    }

    private void addDelegateMethod(final MethodNode candidate, final DelegateDescription delegate, final Iterable<MethodNode> ownMethods) {
        Map<String,ClassNode> genericsSpec = addMethodGenerics(candidate, createGenericsSpec(delegate.owner));
        extractSuperClassGenerics(delegate.type, candidate.getDeclaringClass(), genericsSpec);

        if ((delegate.excludeTypes != null && !delegate.excludeTypes.isEmpty()) || delegate.includeTypes != null) {
            MethodNode correctedMethodNode = correctToGenericsSpec(genericsSpec, candidate);
            boolean checkReturn = delegate.type.getMethods().contains(candidate);
            if (shouldSkipOnDescriptorUndefinedAware(checkReturn, genericsSpec, correctedMethodNode, delegate.excludeTypes, delegate.includeTypes))
                return;
        }

        // give precedence to methods of self (but not abstract or static superclass methods)
        // also allows abstract or static self methods to be selected for overriding but they are ignored later
        MethodNode existingNode = null;
        for (MethodNode mn : ownMethods) {
            if (!mn.isAbstract() && !mn.isStatic() && mn.getTypeDescriptor().equals(candidate.getTypeDescriptor())) {
                existingNode = mn;
                break;
            }
        }
        if (existingNode == null || existingNode.getCode() == null) {
            final ArgumentListExpression args = new ArgumentListExpression();
            final Parameter[] params = candidate.getParameters();
            final Parameter[] newParams = new Parameter[params.length];
            List<String> currentMethodGenPlaceholders = getGenericPlaceholderNames(candidate);
            for (int i = 0, n = newParams.length; i < n; i += 1) {
                ClassNode newParamType = correctToGenericsSpecRecurse(genericsSpec, params[i].getType(), currentMethodGenPlaceholders);
                Parameter newParam = new Parameter(newParamType, getParamName(params, i, delegate.name));
                if (memberHasValue(delegate.annotation, MEMBER_PARAMETER_ANNOTATIONS, Boolean.TRUE)) {
                    newParam.addAnnotations(copyAnnotatedNodeAnnotations(params[i], MY_TYPE_NAME));
                }
                newParams[i] = newParam;
                args.addExpression(varX(newParam));
            }
            boolean alsoLazy = !delegate.delegate.getAnnotations(LAZY_TYPE).isEmpty();
            // addMethod will ignore attempts to override abstract or static methods with same signature on self
            MethodCallExpression mce = callX(
                    // use propX when lazy, because lazy is only allowed on fields/properties
                    alsoLazy ? propX(varX("this"), delegate.name.substring(1)) : delegate.getOp,
                    candidate.getName(),
                    args);
            mce.setImplicitThis(false); // GROOVY-9938
            mce.setSourcePosition(delegate.delegate); // GROOVY-6542
            ClassNode returnType = correctToGenericsSpecRecurse(genericsSpec, candidate.getReturnType(), currentMethodGenPlaceholders);
            MethodNode newMethod = addGeneratedMethod(
                    delegate.owner,
                    candidate.getName(),
                    candidate.getModifiers() & (~ACC_ABSTRACT) & (~ACC_NATIVE),
                    returnType,
                    newParams,
                    candidate.getExceptions(),
                    candidate.isVoidMethod() ? stmt(mce) : returnS(mce)
            );
            newMethod.setGenericsTypes(candidate.getGenericsTypes());
            if (memberHasValue(delegate.annotation, MEMBER_METHOD_ANNOTATIONS, Boolean.TRUE)) {
                newMethod.addAnnotations(copyAnnotatedNodeAnnotations(candidate, MY_TYPE_NAME, false));
            }
        }
    }

    private static List<String> getGenericPlaceholderNames(final MethodNode candidate) {
        GenericsType[] candidateGenericsTypes = candidate.getGenericsTypes();
        List<String> names = new ArrayList<>();
        if (candidateGenericsTypes != null) {
            for (GenericsType gt : candidateGenericsTypes) {
                names.add(gt.getName());
            }
        }
        return names;
    }

    private static String getParamName(final Parameter[] params, final int i, final String fieldName) {
        String name = params[i].getName();
        while(name.equals(fieldName) || clashesWithOtherParams(name, params, i)) {
            name = "_" + name;
        }
        return name;
    }

    private static boolean clashesWithOtherParams(final String name, final Parameter[] params, final int i) {
        for (int j = 0, n = params.length; j < n; j += 1) {
            if (i == j) continue;
            if (params[j].getName().equals(name)) return true;
        }
        return false;
    }

    static class DelegateDescription {
        AnnotationNode annotation;
        AnnotatedNode delegate;
        String name;
        ClassNode type;
        ClassNode owner;
        Expression getOp;
        String origin;
        List<String> includes;
        List<String> excludes;
        List<ClassNode> includeTypes;
        List<ClassNode> excludeTypes;
    }
}

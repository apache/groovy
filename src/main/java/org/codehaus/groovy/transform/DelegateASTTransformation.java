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
import groovy.lang.GroovyObject;
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
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllMethods;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInterfacesAndSuperInterfaces;
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

/**
 * Handles generation of code for the <code>@Delegate</code> annotation
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class DelegateASTTransformation extends AbstractASTTransformation {

    private static final Class MY_CLASS = Delegate.class;
    private static final ClassNode MY_TYPE = make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode DEPRECATED_TYPE = make(Deprecated.class);
    private static final ClassNode GROOVYOBJECT_TYPE = make(GroovyObject.class);
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

    public void visit(ASTNode[] nodes, SourceUnit source) {
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
            if (delegate.type.equals(ClassHelper.OBJECT_TYPE) || delegate.type.equals(GROOVYOBJECT_TYPE)) {
                addError(MY_TYPE_NAME + " " + delegate.origin + " '" + delegate.name + "' has an inappropriate type: " + delegate.type.getName() +
                        ". Please add an explicit type but not java.lang.Object or groovy.lang.GroovyObject.", parent);
                return;
            }
            if (delegate.type.equals(delegate.owner)) {
                addError(MY_TYPE_NAME + " " + delegate.origin + " '" + delegate.name + "' has an inappropriate type: " + delegate.type.getName() +
                        ". Delegation to own type not supported. Please use a different type.", parent);
                return;
            }
            final List<MethodNode> delegateMethods = getAllMethods(delegate.type);
            for (ClassNode next : delegate.type.getAllInterfaces()) {
                delegateMethods.addAll(getAllMethods(next));
            }

            final boolean skipInterfaces = memberHasValue(node, MEMBER_INTERFACES, false);
            final boolean includeDeprecated = memberHasValue(node, MEMBER_DEPRECATED, true) || (delegate.type.isInterface() && !skipInterfaces);
            final boolean allNames = memberHasValue(node, MEMBER_ALL_NAMES, true);
            delegate.excludes = getMemberStringList(node, MEMBER_EXCLUDES);
            delegate.includes = getMemberStringList(node, MEMBER_INCLUDES);
            delegate.excludeTypes = getMemberClassList(node, MEMBER_EXCLUDE_TYPES);
            delegate.includeTypes = getMemberClassList(node, MEMBER_INCLUDE_TYPES);
            checkIncludeExcludeUndefinedAware(node, delegate.excludes, delegate.includes,
                                              delegate.excludeTypes, delegate.includeTypes, MY_TYPE_NAME);

            final List<MethodNode> ownerMethods = getAllMethods(delegate.owner);
            for (MethodNode mn : delegateMethods) {
                addDelegateMethod(delegate, ownerMethods, mn, includeDeprecated, allNames);
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
                    addGeneratedMethod(delegate.owner, "getLength",
                            ACC_PUBLIC,
                            ClassHelper.int_TYPE,
                            Parameter.EMPTY_ARRAY,
                            null,
                            returnS(propX(delegate.getOp, "length")));
                }
            }

            if (skipInterfaces) return;

            final Set<ClassNode> allInterfaces = getInterfacesAndSuperInterfaces(delegate.type);
            final Set<ClassNode> ownerIfaces = delegate.owner.getAllInterfaces();
            Map<String,ClassNode> genericsSpec = createGenericsSpec(delegate.owner);
            genericsSpec = createGenericsSpec(delegate.type, genericsSpec);
            for (ClassNode iface : allInterfaces) {
                if (Modifier.isPublic(iface.getModifiers()) && !ownerIfaces.contains(iface)) {
                    final ClassNode[] ifaces = delegate.owner.getInterfaces();
                    final ClassNode[] newIfaces = new ClassNode[ifaces.length + 1];
                    for (int i = 0; i < ifaces.length; i++) {
                        newIfaces[i] = correctToGenericsSpecRecurse(genericsSpec, ifaces[i]);
                    }
                    newIfaces[ifaces.length] = correctToGenericsSpecRecurse(genericsSpec, iface);
                    delegate.owner.setInterfaces(newIfaces);
                }
            }
        }
    }

    private static void addSetterIfNeeded(DelegateDescription delegate, PropertyNode prop, String name, boolean allNames) {
        String setterName = "set" + Verifier.capitalize(name);
        if ((prop.getModifiers() & ACC_FINAL) == 0
                && delegate.owner.getSetterMethod(setterName) == null && delegate.owner.getProperty(name) == null
                && !shouldSkipPropertyMethod(name, setterName, delegate.excludes, delegate.includes, allNames)) {
            addGeneratedMethod(delegate.owner, setterName,
                    ACC_PUBLIC,
                    ClassHelper.VOID_TYPE,
                    params(new Parameter(GenericsUtils.nonGeneric(prop.getType()), "value")),
                    null,
                    assignS(propX(delegate.getOp, name), varX("value"))
            );
        }
    }

    private static void addGetterIfNeeded(DelegateDescription delegate, PropertyNode prop, String name, boolean allNames) {
        boolean isPrimBool = prop.getOriginType().equals(ClassHelper.boolean_TYPE);
        // do a little bit of pre-work since Groovy compiler hasn't added property accessors yet
        boolean willHaveGetAccessor = true;
        boolean willHaveIsAccessor = isPrimBool;
        String suffix = Verifier.capitalize(name);
        if (isPrimBool) {
            ClassNode cNode = prop.getDeclaringClass();
            if (cNode.getGetterMethod("is" + suffix) != null && cNode.getGetterMethod("get" + suffix) == null)
                willHaveGetAccessor = false;
            if (cNode.getGetterMethod("get" + suffix) != null && cNode.getGetterMethod("is" + suffix) == null)
                willHaveIsAccessor = false;
        }
        Reference<Boolean> ownerWillHaveGetAccessor = new Reference<>();
        Reference<Boolean> ownerWillHaveIsAccessor = new Reference<>();
        extractAccessorInfo(delegate.owner, name, ownerWillHaveGetAccessor, ownerWillHaveIsAccessor);

        for (String prefix : new String[]{"get", "is"}) {
            String getterName = prefix + suffix;
            if ((prefix.equals("get") && willHaveGetAccessor && !ownerWillHaveGetAccessor.get()
                    || prefix.equals("is") && willHaveIsAccessor && !ownerWillHaveIsAccessor.get())
                    && !shouldSkipPropertyMethod(name, getterName, delegate.excludes, delegate.includes, allNames)) {
                addGeneratedMethod(delegate.owner, getterName,
                        ACC_PUBLIC,
                        GenericsUtils.nonGeneric(prop.getType()),
                        Parameter.EMPTY_ARRAY,
                        null,
                        returnS(propX(delegate.getOp, name)));
            }
        }
    }

    private static void extractAccessorInfo(ClassNode owner, String name, Reference<Boolean> willHaveGetAccessor, Reference<Boolean> willHaveIsAccessor) {
        String suffix = Verifier.capitalize(name);
        boolean hasGetAccessor = owner.getGetterMethod("get" + suffix) != null;
        boolean hasIsAccessor = owner.getGetterMethod("is" + suffix) != null;
        PropertyNode prop = owner.getProperty(name);
        willHaveGetAccessor.set(hasGetAccessor || (prop != null && !hasIsAccessor));
        willHaveIsAccessor.set(hasIsAccessor || (prop != null && !hasGetAccessor && prop.getOriginType().equals(ClassHelper.boolean_TYPE)));
    }
    
    private static boolean shouldSkipPropertyMethod(String propertyName, String methodName, List<String> excludes, List<String> includes, boolean allNames) {
        return ((!allNames && deemedInternalName(propertyName))
                    || excludes != null && (excludes.contains(propertyName) || excludes.contains(methodName)) 
                    || (includes != null && !includes.isEmpty() && !includes.contains(propertyName) && !includes.contains(methodName)));
    }

    private void addDelegateMethod(DelegateDescription delegate, List<MethodNode> ownMethods, MethodNode candidate, boolean includeDeprecated, boolean allNames) {
        if (!candidate.isPublic() || candidate.isStatic() || 0 != (candidate.getModifiers () & ACC_SYNTHETIC))
            return;

        if (!candidate.getAnnotations(DEPRECATED_TYPE).isEmpty() && !includeDeprecated)
            return;

        if (shouldSkip(candidate.getName(), delegate.excludes, delegate.includes, allNames)) return;

        Map<String,ClassNode> genericsSpec = createGenericsSpec(delegate.owner);
        genericsSpec = addMethodGenerics(candidate, genericsSpec);
        extractSuperClassGenerics(delegate.type, candidate.getDeclaringClass(), genericsSpec);

        if ((delegate.excludeTypes != null && !delegate.excludeTypes.isEmpty()) || delegate.includeTypes != null) {
            MethodNode correctedMethodNode = correctToGenericsSpec(genericsSpec, candidate);
            boolean checkReturn = delegate.type.getMethods().contains(candidate);
            if (shouldSkipOnDescriptorUndefinedAware(checkReturn, genericsSpec, correctedMethodNode, delegate.excludeTypes, delegate.includeTypes))
                return;
        }

        // ignore methods from GroovyObject
        for (MethodNode mn : GROOVYOBJECT_TYPE.getMethods()) {
            if (mn.getTypeDescriptor().equals(candidate.getTypeDescriptor())) {
                return;
            }
        }

        // ignore methods already in owner
        for (MethodNode mn : delegate.owner.getMethods()) {
            if (mn.getTypeDescriptor().equals(candidate.getTypeDescriptor())) {
                return;
            }
        }

        // give precedence to methods of self (but not abstract or static superclass methods)
        // also allows abstract or static self methods to be selected for overriding but they are ignored later
        MethodNode existingNode = null;
        for (MethodNode mn : ownMethods) {
            if (mn.getTypeDescriptor().equals(candidate.getTypeDescriptor()) && !mn.isAbstract() && !mn.isStatic()) {
                existingNode = mn;
                break;
            }
        }
        if (existingNode == null || existingNode.getCode() == null) {

            final ArgumentListExpression args = new ArgumentListExpression();
            final Parameter[] params = candidate.getParameters();
            final Parameter[] newParams = new Parameter[params.length];
            List<String> currentMethodGenPlaceholders = genericPlaceholderNames(candidate);
            for (int i = 0; i < newParams.length; i++) {
                ClassNode newParamType = correctToGenericsSpecRecurse(genericsSpec, params[i].getType(), currentMethodGenPlaceholders);
                Parameter newParam = new Parameter(newParamType, getParamName(params, i, delegate.name));
                newParam.setInitialExpression(params[i].getInitialExpression());

                if (memberHasValue(delegate.annotation, MEMBER_PARAMETER_ANNOTATIONS, true)) {
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
            mce.setSourcePosition(delegate.delegate);
            ClassNode returnType = correctToGenericsSpecRecurse(genericsSpec, candidate.getReturnType(), currentMethodGenPlaceholders);
            MethodNode newMethod = addGeneratedMethod(delegate.owner, candidate.getName(),
                    candidate.getModifiers() & (~ACC_ABSTRACT) & (~ACC_NATIVE),
                    returnType,
                    newParams,
                    candidate.getExceptions(),
                    stmt(mce));
            newMethod.setGenericsTypes(candidate.getGenericsTypes());

            if (memberHasValue(delegate.annotation, MEMBER_METHOD_ANNOTATIONS, true)) {
                newMethod.addAnnotations(copyAnnotatedNodeAnnotations(candidate, MY_TYPE_NAME));
            }
        }
    }

    private static List<String> genericPlaceholderNames(MethodNode candidate) {
        GenericsType[] candidateGenericsTypes = candidate.getGenericsTypes();
        List<String> names = new ArrayList<>();
        if (candidateGenericsTypes != null) {
            for (GenericsType gt : candidateGenericsTypes) {
                names.add(gt.getName());
            }
        }
        return names;
    }

    private static String getParamName(Parameter[] params, int i, String fieldName) {
        String name = params[i].getName();
        while(name.equals(fieldName) || clashesWithOtherParams(name, params, i)) {
            name = "_" + name;
        }
        return name;
    }

    private static boolean clashesWithOtherParams(String name, Parameter[] params, int i) {
        for (int j = 0; j < params.length; j++) {
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

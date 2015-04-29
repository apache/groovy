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

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
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
 *
 * @author Alex Tkachman
 * @author Guillaume Laforge
 * @author Paul King
 * @author Andre Steingress
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

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];

        if (parent instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) parent;
            final ClassNode type = fieldNode.getType();
            final ClassNode owner = fieldNode.getOwner();
            if (type.equals(ClassHelper.OBJECT_TYPE) || type.equals(GROOVYOBJECT_TYPE)) {
                addError(MY_TYPE_NAME + " field '" + fieldNode.getName() + "' has an inappropriate type: " + type.getName() +
                        ". Please add an explicit type but not java.lang.Object or groovy.lang.GroovyObject.", parent);
                return;
            }
            if (type.equals(owner)) {
                addError(MY_TYPE_NAME + " field '" + fieldNode.getName() + "' has an inappropriate type: " + type.getName() +
                        ". Delegation to own type not supported. Please use a different type.", parent);
                return;
            }
            final List<MethodNode> fieldMethods = getAllMethods(type);
            for (ClassNode next : type.getAllInterfaces()) {
                fieldMethods.addAll(getAllMethods(next));
            }

            final boolean skipInterfaces = memberHasValue(node, MEMBER_INTERFACES, false);
            final boolean includeDeprecated = memberHasValue(node, MEMBER_DEPRECATED, true) || (type.isInterface() && !skipInterfaces);
            List<String> excludes = getMemberList(node, MEMBER_EXCLUDES);
            List<String> includes = getMemberList(node, MEMBER_INCLUDES);
            List<ClassNode> excludeTypes = getClassList(node, MEMBER_EXCLUDE_TYPES);
            List<ClassNode> includeTypes = getClassList(node, MEMBER_INCLUDE_TYPES);
            checkIncludeExclude(node, excludes, includes, excludeTypes, includeTypes, MY_TYPE_NAME);

            final List<MethodNode> ownerMethods = getAllMethods(owner);
            for (MethodNode mn : fieldMethods) {
                addDelegateMethod(node, fieldNode, owner, ownerMethods, mn, includeDeprecated, includes, excludes, includeTypes, excludeTypes);
            }

            for (PropertyNode prop : getAllProperties(type)) {
                if (prop.isStatic() || !prop.isPublic())
                    continue;
                String name = prop.getName();
                addGetterIfNeeded(fieldNode, owner, prop, name, includes, excludes);
                addSetterIfNeeded(fieldNode, owner, prop, name, includes, excludes);
            }

            if (skipInterfaces) return;

            final Set<ClassNode> allInterfaces = getInterfacesAndSuperInterfaces(type);
            final Set<ClassNode> ownerIfaces = owner.getAllInterfaces();
            Map<String,ClassNode> genericsSpec = createGenericsSpec(fieldNode.getDeclaringClass());
            genericsSpec = createGenericsSpec(fieldNode.getType(), genericsSpec);
            for (ClassNode iface : allInterfaces) {
                if (Modifier.isPublic(iface.getModifiers()) && !ownerIfaces.contains(iface)) {
                    final ClassNode[] ifaces = owner.getInterfaces();
                    final ClassNode[] newIfaces = new ClassNode[ifaces.length + 1];
                    for (int i = 0; i < ifaces.length; i++) {
                        newIfaces[i] = correctToGenericsSpecRecurse(genericsSpec, ifaces[i]);
                    }
                    newIfaces[ifaces.length] = correctToGenericsSpecRecurse(genericsSpec, iface);
                    owner.setInterfaces(newIfaces);
                }
            }
        }
    }

    private void addSetterIfNeeded(FieldNode fieldNode, ClassNode owner, PropertyNode prop, String name, List<String> includes, List<String> excludes) {
        String setterName = "set" + Verifier.capitalize(name);
        if ((prop.getModifiers() & ACC_FINAL) == 0
                && owner.getSetterMethod(setterName) == null
                && !shouldSkipPropertyMethod(name, setterName, excludes, includes)) {
            owner.addMethod(setterName,
                    ACC_PUBLIC,
                    ClassHelper.VOID_TYPE,
                    params(new Parameter(GenericsUtils.nonGeneric(prop.getType()), "value")),
                    null,
                    assignS(propX(varX(fieldNode), name), varX("value"))
            );
        }
    }

    private void addGetterIfNeeded(FieldNode fieldNode, ClassNode owner, PropertyNode prop, String name, List<String> includes, List<String> excludes) {
        String getterName = "get" + Verifier.capitalize(name);
        if (owner.getGetterMethod(getterName) == null
                && !shouldSkipPropertyMethod(name, getterName, excludes, includes)) {
            owner.addMethod(getterName,
                    ACC_PUBLIC,
                    GenericsUtils.nonGeneric(prop.getType()),
                    Parameter.EMPTY_ARRAY,
                    null,
                    returnS(propX(varX(fieldNode), name)));
        }
    }
    
    private boolean shouldSkipPropertyMethod(String propertyName, String methodName, List<String> excludes, List<String> includes) {
        return (deemedInternalName(propertyName)
                    || excludes != null && (excludes.contains(propertyName) || excludes.contains(methodName)) 
                    || (includes != null && !includes.isEmpty() && !includes.contains(propertyName) && !includes.contains(methodName)));
    }

    private void addDelegateMethod(AnnotationNode node, FieldNode fieldNode, ClassNode owner, List<MethodNode> ownMethods, MethodNode candidate, boolean includeDeprecated, List<String> includes, List<String> excludes, List<ClassNode> includeTypes, List<ClassNode> excludeTypes) {
        if (!candidate.isPublic() || candidate.isStatic() || 0 != (candidate.getModifiers () & ACC_SYNTHETIC))
            return;

        if (!candidate.getAnnotations(DEPRECATED_TYPE).isEmpty() && !includeDeprecated)
            return;

        if (shouldSkip(candidate.getName(), excludes, includes)) return;

        Map<String,ClassNode> genericsSpec = createGenericsSpec(fieldNode.getDeclaringClass());
        genericsSpec = addMethodGenerics(candidate, genericsSpec);
        extractSuperClassGenerics(fieldNode.getType(), candidate.getDeclaringClass(), genericsSpec);

        if (!excludeTypes.isEmpty() || !includeTypes.isEmpty()) {
            MethodNode correctedMethodNode = correctToGenericsSpec(genericsSpec, candidate);
            boolean checkReturn = fieldNode.getType().getMethods().contains(candidate);
            if (shouldSkipOnDescriptor(checkReturn, genericsSpec, correctedMethodNode, excludeTypes, includeTypes)) return;
        }

        // ignore methods from GroovyObject
        for (MethodNode mn : GROOVYOBJECT_TYPE.getMethods()) {
            if (mn.getTypeDescriptor().equals(candidate.getTypeDescriptor())) {
                return;
            }
        }

        // ignore methods already in owner
        for (MethodNode mn : owner.getMethods()) {
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
                Parameter newParam = new Parameter(newParamType, getParamName(params, i, fieldNode.getName()));
                newParam.setInitialExpression(params[i].getInitialExpression());

                if (memberHasValue(node, MEMBER_PARAMETER_ANNOTATIONS, true)) {
                    newParam.addAnnotations(copyAnnotatedNodeAnnotations(params[i], MY_TYPE_NAME));
                }

                newParams[i] = newParam;
                args.addExpression(varX(newParam));
            }
            boolean alsoLazy = !fieldNode.getAnnotations(LAZY_TYPE).isEmpty();
            // addMethod will ignore attempts to override abstract or static methods with same signature on self
            MethodCallExpression mce = callX(
                    alsoLazy ? propX(varX("this"), fieldNode.getName().substring(1)) :
                    varX(fieldNode.getName(), correctToGenericsSpecRecurse(genericsSpec, fieldNode.getType())),
                    candidate.getName(),
                    args);
            mce.setSourcePosition(fieldNode);
            ClassNode returnType = correctToGenericsSpecRecurse(genericsSpec, candidate.getReturnType(), currentMethodGenPlaceholders);
            MethodNode newMethod = owner.addMethod(candidate.getName(),
                    candidate.getModifiers() & (~ACC_ABSTRACT) & (~ACC_NATIVE),
                    returnType,
                    newParams,
                    candidate.getExceptions(),
                    stmt(mce));
            newMethod.setGenericsTypes(candidate.getGenericsTypes());

            if (memberHasValue(node, MEMBER_METHOD_ANNOTATIONS, true)) {
                newMethod.addAnnotations(copyAnnotatedNodeAnnotations(candidate, MY_TYPE_NAME));
            }
        }
    }

    private List<String> genericPlaceholderNames(MethodNode candidate) {
        GenericsType[] candidateGenericsTypes = candidate.getGenericsTypes();
        List<String> names = new ArrayList<String>();
        if (candidateGenericsTypes != null) {
            for (GenericsType gt : candidateGenericsTypes) {
                names.add(gt.getName());
            }
        }
        return names;
    }

    private String getParamName(Parameter[] params, int i, String fieldName) {
        String name = params[i].getName();
        while(name.equals(fieldName) || clashesWithOtherParams(name, params, i)) {
            name = "_" + name;
        }
        return name;
    }

    private boolean clashesWithOtherParams(String name, Parameter[] params, int i) {
        for (int j = 0; j < params.length; j++) {
            if (i == j) continue;
            if (params[j].getName().equals(name)) return true;
        }
        return false;
    }

}

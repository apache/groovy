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
import groovy.lang.Reference;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.codehaus.groovy.ast.ClassHelper.isGroovyObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.defaultValueX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllMethods;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getGetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInterfacesAndSuperInterfaces;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.addMethodGenerics;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpecRecurse;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.extractSuperClassGenerics;
import static org.codehaus.groovy.ast.tools.GenericsUtils.nonGeneric;
import static org.codehaus.groovy.transform.DelegateASTTransformation.DelegateDescription;
import static org.codehaus.groovy.transform.DelegateASTTransformation.collectMethods;
import static org.codehaus.groovy.transform.DelegateASTTransformation.extractAccessorInfo;
import static org.codehaus.groovy.transform.DelegateASTTransformation.filterMethods;
import static org.codehaus.groovy.transform.DelegateASTTransformation.getGenericPlaceholderNames;
import static org.codehaus.groovy.transform.DelegateASTTransformation.getParamName;
import static org.codehaus.groovy.transform.DelegateASTTransformation.shouldSkipPropertyMethod;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * Joint-compilation stubber for {@link Delegate}. Walks the delegate type
 * with the same enumeration and filtering rules as the full transform —
 * sharing helpers from {@link DelegateASTTransformation} — and emits
 * placeholder methods on the owner so Java consumers can call the delegated
 * surface against the joint-compilation stub.
 *
 * <p>The stub surface is a strict <em>subset</em> of the runtime: the
 * stubber honours {@code interfaces}, {@code deprecated},
 * {@code includes}/{@code excludes}, {@code includeTypes}/{@code excludeTypes},
 * and {@code allNames} the same way the runtime does, so no method appears
 * on the stub that the full transform won't add at runtime.
 *
 * <p>Member-body content is a default-value placeholder; the full transform
 * at CANONICALIZATION discards stubber-tagged methods first, then installs
 * its real {@code this.delegate.method(args)} bodies.
 *
 * <p><b>Boundary.</b> When the delegate type is in the same compilation
 * unit and gains methods from another transform that runs after CONVERSION
 * (or another CONVERSION-phase stubber that hasn't run yet — e.g. the
 * delegate type is declared <em>after</em> the owner in source), those
 * methods are not visible to the stubber and not delegated in the stub.
 * The runtime still produces the full surface; the stub remains a subset.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class DelegateASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(Delegate.class);

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;

        DelegateDescription delegate = buildDescription(parent, annotation);
        if (delegate == null) return;

        // The runtime errors on these; we silently bail and let the full
        // transform report at CANONICALIZATION.
        if (isObjectType(delegate.type) || isGroovyObjectType(delegate.type)) return;
        if (delegate.type.equals(delegate.owner)) return;

        boolean skipInterfaces = memberHasValue(annotation, "interfaces", Boolean.FALSE);
        boolean includeDeprecated = memberHasValue(annotation, "deprecated", Boolean.TRUE)
                || (delegate.type.isInterface() && !skipInterfaces);
        boolean allNames = memberHasValue(annotation, "allNames", Boolean.TRUE);

        delegate.excludes = getMemberStringList(annotation, "excludes");
        delegate.includes = getMemberStringList(annotation, "includes");
        delegate.excludeTypes = getMemberClassList(annotation, "excludeTypes");
        delegate.includeTypes = getMemberClassList(annotation, "includeTypes");

        Iterable<MethodNode> delegateMethods = filterMethods(
                collectMethods(delegate.type), delegate, allNames, includeDeprecated);

        List<MethodNode> ownerMethods = getAllMethods(delegate.owner);

        for (MethodNode candidate : delegateMethods) {
            addDelegateStub(candidate, delegate, ownerMethods);
        }

        for (PropertyNode prop : getAllProperties(delegate.type)) {
            if (prop.isStatic() || !prop.isPublic()) continue;
            String name = prop.getName();
            addStubGetter(delegate, prop, name, allNames);
            addStubSetter(delegate, prop, name, allNames);
        }

        if (delegate.type.isArray()) {
            boolean skipLength = delegate.excludes != null
                    && (delegate.excludes.contains("length") || delegate.excludes.contains("getLength"));
            if (!skipLength && delegate.owner.getDeclaredMethod("getLength", Parameter.EMPTY_ARRAY) == null) {
                addStubMethod(delegate.owner, "getLength", ACC_PUBLIC,
                        ClassHelper.int_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                        returnS(defaultValueX(ClassHelper.int_TYPE)));
            }
        }

        if (skipInterfaces) return;

        Set<ClassNode> interfaces;
        if (delegate.type.isInterface()) { // GROOVY-11736
            interfaces = new HashSet<>(Set.of(delegate.type));
        } else {
            interfaces = getInterfacesAndSuperInterfaces(delegate.type);
            interfaces.removeIf(i1 -> interfaces.stream().anyMatch(i2 -> i2 != i1 && i2.implementsInterface(i1)));
        }
        interfaces.removeIf(i -> (i.getModifiers() & (ACC_PUBLIC | ACC_SYNTHETIC)) != ACC_PUBLIC || i.isSealed());
        interfaces.removeAll(getInterfacesAndSuperInterfaces(delegate.owner));

        if (!interfaces.isEmpty()) {
            delegate.owner.setInterfaces(Stream.concat(
                    Stream.of(delegate.owner.getInterfaces()), interfaces.stream())
                    .toArray(ClassNode[]::new));
        }
    }

    private DelegateDescription buildDescription(final AnnotatedNode parent, final AnnotationNode anno) {
        DelegateDescription d = new DelegateDescription();
        d.annotation = anno;
        if (parent instanceof FieldNode fn) {
            d.delegate = fn;
            d.name = fn.getName();
            d.type = fn.getType();
            d.owner = fn.getOwner();
            d.getOp = varX(fn);
            d.origin = "field";
            return d;
        }
        if (parent instanceof MethodNode mn) {
            // Full transform errors on parameterised method targets; nothing to stub.
            if (mn.getParameters().length > 0) return null;
            d.delegate = mn;
            d.name = mn.getName();
            d.type = mn.getReturnType();
            d.owner = mn.getDeclaringClass();
            d.getOp = callThisX(d.name);
            d.origin = "method";
            return d;
        }
        return null;
    }

    private void addDelegateStub(final MethodNode candidate, final DelegateDescription delegate, final List<MethodNode> ownMethods) {
        Map<String, ClassNode> genericsSpec = addMethodGenerics(candidate, createGenericsSpec(delegate.owner));
        extractSuperClassGenerics(delegate.type, candidate.getDeclaringClass(), genericsSpec);

        if ((delegate.excludeTypes != null && !delegate.excludeTypes.isEmpty()) || delegate.includeTypes != null) {
            MethodNode correctedMethodNode = correctToGenericsSpec(genericsSpec, candidate);
            boolean checkReturn = delegate.type.getMethods().contains(candidate);
            if (shouldSkipOnDescriptorUndefinedAware(checkReturn, genericsSpec, correctedMethodNode,
                    delegate.excludeTypes, delegate.includeTypes)) return;
        }

        // Same precedence rule as runtime: skip if owner has a non-abstract,
        // non-static method with the same descriptor.
        for (MethodNode mn : ownMethods) {
            if (!mn.isAbstract() && !mn.isStatic()
                    && mn.getTypeDescriptor().equals(candidate.getTypeDescriptor())) return;
        }

        Parameter[] params = candidate.getParameters();
        Parameter[] newParams = new Parameter[params.length];
        List<String> currentMethodGenPlaceholders = getGenericPlaceholderNames(candidate);
        for (int i = 0; i < newParams.length; i++) {
            ClassNode newParamType = correctToGenericsSpecRecurse(genericsSpec, params[i].getType(), currentMethodGenPlaceholders);
            newParams[i] = new Parameter(newParamType, getParamName(params, i, delegate.name));
        }
        ClassNode returnType = correctToGenericsSpecRecurse(genericsSpec, candidate.getReturnType(), currentMethodGenPlaceholders);
        Statement body = candidate.isVoidMethod()
                ? EmptyStatement.INSTANCE
                : returnS(defaultValueX(returnType));

        MethodNode stub = addStubMethod(delegate.owner,
                candidate.getName(),
                candidate.getModifiers() & (~ACC_ABSTRACT) & (~ACC_NATIVE),
                returnType,
                newParams,
                candidate.getExceptions(),
                body);
        stub.setGenericsTypes(candidate.getGenericsTypes());
    }

    private void addStubGetter(final DelegateDescription delegate, final PropertyNode prop, final String name, final boolean allNames) {
        boolean isPrimBool = isPrimitiveBoolean(prop.getOriginType());
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
        Reference<Boolean> ownerWillHaveGetAccessor = new Reference<>();
        Reference<Boolean> ownerWillHaveIsAccessor = new Reference<>();
        extractAccessorInfo(delegate.owner, name, ownerWillHaveGetAccessor, ownerWillHaveIsAccessor);

        ClassNode propertyType = nonGeneric(prop.getType());

        if (willHaveGetAccessor && !ownerWillHaveGetAccessor.get()
                && !shouldSkipPropertyMethod(prop, getterName, delegate.excludes, delegate.includes, allNames)) {
            if (delegate.owner.getDeclaredMethod(getterName, Parameter.EMPTY_ARRAY) == null) {
                addStubMethod(delegate.owner, getterName, ACC_PUBLIC, propertyType,
                        Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                        returnS(defaultValueX(propertyType)));
            }
        }

        if (willHaveIsAccessor && !ownerWillHaveIsAccessor.get()
                && !shouldSkipPropertyMethod(prop, isserName, delegate.excludes, delegate.includes, allNames)) {
            if (delegate.owner.getDeclaredMethod(isserName, Parameter.EMPTY_ARRAY) == null) {
                addStubMethod(delegate.owner, isserName, ACC_PUBLIC, propertyType,
                        Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                        returnS(defaultValueX(propertyType)));
            }
        }
    }

    private void addStubSetter(final DelegateDescription delegate, final PropertyNode prop, final String name, final boolean allNames) {
        if (prop.isFinal()) return;
        String setterName = getSetterName(name);
        if (delegate.owner.getSetterMethod(setterName) != null) return;
        if (delegate.owner.getProperty(name) != null) return;
        if (shouldSkipPropertyMethod(prop, setterName, delegate.excludes, delegate.includes, allNames)) return;
        if (delegate.owner.getDeclaredMethod(setterName, params(new Parameter(nonGeneric(prop.getType()), "value"))) != null) return;

        addStubMethod(delegate.owner, setterName, ACC_PUBLIC, ClassHelper.VOID_TYPE,
                params(new Parameter(nonGeneric(prop.getType()), "value")), ClassNode.EMPTY_ARRAY,
                EmptyStatement.INSTANCE);
    }
}

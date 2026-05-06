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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.BytecodeInstruction;
import org.codehaus.groovy.classgen.BytecodeSequence;
import org.codehaus.groovy.classgen.asm.MethodReferenceExpressionWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInterfacesAndSuperInterfaces;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.extractPlaceholders;
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafe0;
import static org.codehaus.groovy.ast.tools.ParameterUtils.isVargs;
import static org.codehaus.groovy.ast.tools.ParameterUtils.parametersCompatible;
import static org.codehaus.groovy.classgen.asm.BytecodeHelper.getClassInternalName;
import static org.codehaus.groovy.classgen.asm.sc.StaticTypesFunctionalInterfaceMetadataKey.METHOD_REFERENCE_DESERIALIZE_METHOD_NAME;
import static org.codehaus.groovy.runtime.ArrayGroovyMethods.last;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.allParametersAndArgumentsMatch;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.filterMethodsByVisibility;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsForClassNode;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isAssignableTo;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.resolveClassNodeGenerics;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Generates bytecode for method reference expressions in statically-compiled code.
 *
 * @since 3.0.0
 */
public class StaticTypesMethodReferenceExpressionWriter extends MethodReferenceExpressionWriter implements AbstractFunctionalInterfaceWriter {

    public StaticTypesMethodReferenceExpressionWriter(final WriterController controller) {
        super(controller);
    }

    @Override
    public void writeMethodReferenceExpression(final MethodReferenceExpression methodReferenceExpression) {
        FunctionalInterfaceContext functionalInterface = resolveFunctionalInterfaceContext(methodReferenceExpression);
        if (functionalInterface == null) {
            // generate the default bytecode -- most likely a method closure
            super.writeMethodReferenceExpression(methodReferenceExpression);
            return;
        }

        MethodReferenceTarget referenceTarget = resolveMethodReferenceTarget(methodReferenceExpression);
        ResolvedMethodReference resolvedMethodReference = resolveMethodReference(methodReferenceExpression, functionalInterface, referenceTarget);
        validate(methodReferenceExpression, referenceTarget.type(), resolvedMethodReference.methodName(),
            resolvedMethodReference.implementationMethod(), functionalInterface.parametersWithExactType(),
            resolveClassNodeGenerics(extractPlaceholders(functionalInterface.functionalType()), null, functionalInterface.abstractMethod().getReturnType()));

        ResolvedMethodReference adaptedMethodReference = adaptMethodReference(functionalInterface, resolvedMethodReference);
        ResolvedMethodReference invocationReadyMethodReference = prepareInvocationTarget(methodReferenceExpression, adaptedMethodReference);
        MethodReferenceInvocation invocation = createMethodReferenceInvocation(functionalInterface.functionalType(), invocationReadyMethodReference);

        if (functionalInterface.serializable()) {
            ensureDeserializeLambdaSupport(methodReferenceExpression, functionalInterface, invocationReadyMethodReference, invocation);
        }

        writeFunctionalInterfaceIndy(
            controller.getMethodVisitor(),
            functionalInterface.abstractMethod().getName(),
            invocation.invokedTypeDescriptor(),
            functionalInterface.samMethodDescriptor(),
            invocation.implMethodKind(),
            invocationReadyMethodReference.implementationMethod().getDeclaringClass(),
            invocationReadyMethodReference.implementationMethod(),
            functionalInterface.parametersWithExactType(),
            functionalInterface.serializable(),
            functionalInterface.markers()
        );

        updateOperandStack(functionalInterface.functionalType(), invocation.capturing());
    }

    private FunctionalInterfaceContext resolveFunctionalInterfaceContext(final MethodReferenceExpression methodReferenceExpression) {
        // functional interface target is required for native method reference generation
        ClassNode functionalType = methodReferenceExpression.getNodeMetaData(StaticTypesMarker.PARAMETER_TYPE);
        if (functionalType == null || !functionalType.isInterface()) return null;

        MethodNode abstractMethod = ClassHelper.findSAM(functionalType);
        if (abstractMethod == null) return null;

        ClassNode[] inferredParameterTypes = methodReferenceExpression.getNodeMetaData(StaticTypesMarker.CLOSURE_ARGUMENTS);
        // GROOVY-11998: pick up intersection-cast markers populated by STC
        @SuppressWarnings("unchecked")
        java.util.List<ClassNode> rawMarkers = (java.util.List<ClassNode>) methodReferenceExpression.getNodeMetaData(StaticTypesMarker.LAMBDA_MARKERS);
        boolean fromIntersection = rawMarkers != null && rawMarkers.stream().anyMatch(m ->
                m != null && (m.equals(ClassHelper.SERIALIZABLE_TYPE) || m.implementsInterface(ClassHelper.SERIALIZABLE_TYPE)));
        boolean serializable = functionalType.implementsInterface(ClassHelper.SERIALIZABLE_TYPE) || fromIntersection;
        ClassNode[] markers = filterMarkers(rawMarkers, functionalType);
        return new FunctionalInterfaceContext(
            functionalType,
            abstractMethod,
            createParametersWithExactType(abstractMethod, inferredParameterTypes),
            createMethodDescriptor(abstractMethod),
            serializable,
            markers
        );
    }

    private static ClassNode[] filterMarkers(final java.util.List<ClassNode> raw, final ClassNode functionalType) {
        if (raw == null || raw.isEmpty()) return ClassNode.EMPTY_ARRAY;
        java.util.List<ClassNode> out = new java.util.ArrayList<>(raw.size());
        for (ClassNode m : raw) {
            if (m == null || !m.isInterface()) continue;
            if (m.equals(ClassHelper.SERIALIZABLE_TYPE)
                    || ClassHelper.SERIALIZABLE_TYPE.equals(m.redirect())) continue;
            if (functionalType != null && functionalType.implementsInterface(m)) continue;
            out.add(m);
        }
        return out.toArray(ClassNode.EMPTY_ARRAY);
    }

    private MethodReferenceTarget resolveMethodReferenceTarget(final MethodReferenceExpression methodReferenceExpression) {
        Expression typeOrTargetRef = methodReferenceExpression.getExpression();
        boolean classExpression = typeOrTargetRef instanceof ClassExpression;
        ClassNode targetType = classExpression
            ? typeOrTargetRef.getType()
            : controller.getTypeChooser().resolveType(typeOrTargetRef, controller.getClassNode());

        if (ClassHelper.isPrimitiveType(targetType)) { // GROOVY-11353
            targetType = ClassHelper.getWrapper(targetType);
        }

        return new MethodReferenceTarget(typeOrTargetRef, targetType, classExpression, false);
    }

    private ResolvedMethodReference resolveMethodReference(final MethodReferenceExpression methodReferenceExpression,
                                                           final FunctionalInterfaceContext functionalInterface,
                                                           final MethodReferenceTarget referenceTarget) {
        String methodName = methodReferenceExpression.getMethodName().getText();
        if (isConstructorReference(methodName)) {
            String syntheticMethodName = controller.getContext().getNextConstructorReferenceSyntheticMethodName(controller.getMethodNode());
            MethodNode constructorReferenceMethod = addSyntheticMethodForConstructorReference(
                syntheticMethodName,
                referenceTarget.type(),
                functionalInterface.parametersWithExactType()
            );
            return new ResolvedMethodReference(referenceTarget, syntheticMethodName, constructorReferenceMethod, true);
        }

        return new ResolvedMethodReference(
            referenceTarget,
            methodName,
            findMethodReferenceImplementation(methodReferenceExpression, methodName, functionalInterface.parametersWithExactType(), referenceTarget),
            false
        );
    }

    private MethodNode findMethodReferenceImplementation(final MethodReferenceExpression methodReferenceExpression, final String methodName,
                                                         final Parameter[] samParameters, final MethodReferenceTarget referenceTarget) {
        // TODO: move the method lookup and validation to StaticTypeCheckingVisitor
        MethodNode methodRefMethod = findMethodRefMethod(methodName, samParameters, referenceTarget.expression(), referenceTarget.type());
        if (methodReferenceExpression.getNodeMetaData(StaticTypesMarker.PV_METHODS_ACCESS) != null) { // GROOVY-11301, GROOVY-11365: access bridge indicated
            Map<MethodNode, MethodNode> bridgeMethods = referenceTarget.type().redirect().getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_BRIDGE_METHODS);
            if (bridgeMethods != null) {
                methodRefMethod = bridgeMethods.getOrDefault(methodRefMethod, methodRefMethod); // bridge may not have been generated
            }
        }
        if (methodRefMethod == null && referenceTarget.classExpression()) {
            Expression classValue = varX("_class_", referenceTarget.type());
            ClassNode classClass = makeClassSafe0(ClassHelper.CLASS_Type, new GenericsType(referenceTarget.type()));
            methodRefMethod = findMethodRefMethod(methodName, samParameters, classValue, classClass);
            if (methodRefMethod != null) {
                methodRefMethod = addSyntheticMethodForClassReference(methodRefMethod, referenceTarget.type());
            }
        }
        return methodRefMethod;
    }

    private ResolvedMethodReference adaptMethodReference(final FunctionalInterfaceContext functionalInterface,
                                                         final ResolvedMethodReference resolvedMethodReference) {
        MethodReferenceTarget referenceTarget = resolvedMethodReference.referenceTarget();
        MethodNode methodRefMethod = resolvedMethodReference.implementationMethod();

        if (isBridgeMethod(methodRefMethod)) {
            referenceTarget = referenceTarget.markTargetAsArgument(); // GROOVY-11301, GROOVY-11365
            if (referenceTarget.classExpression()) { // method expects an instance argument
                methodRefMethod = addSyntheticMethodForDGSM(methodRefMethod);
            }
            return resolvedMethodReference.with(referenceTarget, methodRefMethod);
        }

        if (isExtensionMethod(methodRefMethod)) {
            ExtensionMethodNode extensionMethodNode = (ExtensionMethodNode) methodRefMethod;
            methodRefMethod = extensionMethodNode.getExtensionMethodNode();
            boolean staticExtension = extensionMethodNode.isStaticExtension();
            if (staticExtension) { // create adapter method to pass extra argument
                methodRefMethod = addSyntheticMethodForDGSM(methodRefMethod);
            }
            if (staticExtension || referenceTarget.classExpression()) {
                referenceTarget = referenceTarget.asClassTarget(methodRefMethod.getDeclaringClass());
            } else { // GROOVY-10653
                referenceTarget = referenceTarget.markTargetAsArgument(); // ex: "string"::size
            }
            return resolvedMethodReference.with(referenceTarget, methodRefMethod);
        }

        if (needsVariadicAdapter(functionalInterface, referenceTarget, methodRefMethod)) {
            int samParameterCount = functionalInterface.abstractMethod().getParameters().length;
            if (!referenceTarget.classExpression() && !methodRefMethod.isStatic() && !methodRefMethod.getDeclaringClass().equals(controller.getClassNode())) {
                referenceTarget = referenceTarget.markTargetAsArgument(); // GROOVY-10653: create static adapter in source class with target as first parameter
                samParameterCount += 1;
            }
            methodRefMethod = addSyntheticMethodForVariadicReference(methodRefMethod, samParameterCount,
                referenceTarget.classExpression() || referenceTarget.targetIsArgument());
            if (methodRefMethod.isStatic() && !referenceTarget.targetIsArgument()) {
                referenceTarget = referenceTarget.asClassTarget(methodRefMethod.getDeclaringClass());
            }
        }

        return resolvedMethodReference.with(referenceTarget, methodRefMethod);
    }

    private boolean needsVariadicAdapter(final FunctionalInterfaceContext functionalInterface,
                                         final MethodReferenceTarget referenceTarget,
                                         final MethodNode methodRefMethod) {
        if (!isVargs(methodRefMethod.getParameters())) {
            return false;
        }

        int samParameterCount = functionalInterface.abstractMethod().getParameters().length;
        int methodParameterCount = methodRefMethod.getParameters().length;
        if (isTypeReferringInstanceMethod(referenceTarget.expression(), methodRefMethod)) {
            methodParameterCount += 1;
        }

        return samParameterCount > methodParameterCount
            || samParameterCount == methodParameterCount - 1
            || (samParameterCount == methodParameterCount
                && !isAssignableTo(last(functionalInterface.parametersWithExactType()).getType(), last(methodRefMethod.getParameters()).getType()));
    }

    private ResolvedMethodReference prepareInvocationTarget(final MethodReferenceExpression methodReferenceExpression,
                                                            final ResolvedMethodReference resolvedMethodReference) {
        MethodReferenceTarget referenceTarget = resolvedMethodReference.referenceTarget();
        if (referenceTarget.classExpression()) {
            return resolvedMethodReference;
        }

        if (resolvedMethodReference.constructorReference()) { // TODO: move this check to the parser
            addFatalError("Constructor reference must be TypeName::new", methodReferenceExpression);
        } else if (resolvedMethodReference.implementationMethod().isStatic() && !referenceTarget.targetIsArgument()) {
            // "string"::valueOf refers to static method, so the bound instance is superfluous.
            return resolvedMethodReference.withTarget(referenceTarget.asClassTarget(referenceTarget.type()));
        } else {
            referenceTarget.expression().visit(controller.getAcg());
            controller.getOperandStack().box(); // GROOVY-11353
        }

        return resolvedMethodReference;
    }

    private MethodReferenceInvocation createMethodReferenceInvocation(final ClassNode functionalType,
                                                                     final ResolvedMethodReference resolvedMethodReference) {
        MethodNode methodRefMethod = resolvedMethodReference.implementationMethod();
        int implMethodKind;
        if (resolvedMethodReference.constructorReference() || methodRefMethod.isStatic()) {
            implMethodKind = Opcodes.H_INVOKESTATIC;
        } else if (methodRefMethod.getDeclaringClass().isInterface()) {
            implMethodKind = Opcodes.H_INVOKEINTERFACE; // GROOVY-9853
        } else {
            implMethodKind = Opcodes.H_INVOKEVIRTUAL;
        }

        MethodReferenceTarget referenceTarget = resolvedMethodReference.referenceTarget();
        Parameter[] capturedParameters = referenceTarget.classExpression()
            ? Parameter.EMPTY_ARRAY
            : new Parameter[]{createCapturedReceiverParameter(referenceTarget.type(), "__METHODREF_EXPR_INSTANCE")};
        return new MethodReferenceInvocation(
            implMethodKind,
            createFunctionalInterfaceFactoryDescriptor(functionalType, capturedParameters),
            referenceTarget.isCapturing()
        );
    }

    private void updateOperandStack(final ClassNode functionalType, final boolean capturing) {
        if (capturing) {
            controller.getOperandStack().replace(functionalType, 1);
        } else {
            controller.getOperandStack().push(functionalType);
        }
    }

    private void validate(final MethodReferenceExpression methodReference, final ClassNode targetType, final String methodName, final MethodNode methodNode, final Parameter[] samParameters, final ClassNode samReturnType) {
        if (methodNode == null) {
            String error;
            if (!(methodReference.getExpression() instanceof ClassExpression)) {
                error = "Failed to find method '%s(%s)'";
            } else {
                error = "Failed to find class method '%s(%s)'";
                if (samParameters.length > 0)
                    error += " or instance method '%1$s(" + Arrays.stream(samParameters).skip(1).map(e -> e.getType().toString(false)).collect(joining(",")) + ")'";
            }
            error = String.format(error + " for the type: %s", methodName, Arrays.stream(samParameters).map(e -> e.getType().toString(false)).collect(joining(",")), targetType.toString(false));
            addFatalError(error, methodReference);
        } else if (methodNode.isVoidMethod() && !ClassHelper.isPrimitiveVoid(samReturnType)) {
            addFatalError("Invalid return type: void is not convertible to " + samReturnType.getText(), methodReference);
        } else if (!AsmClassGenerator.isMemberDirectlyAccessible(methodNode.getModifiers(), methodNode.getDeclaringClass(), controller.getClassNode())) {
            addFatalError("Cannot access method: " + methodName + " of class: " + methodNode.getDeclaringClass().getText(), methodReference); // GROOVY-11365
        } else if (samParameters.length > 0 && isTypeReferringInstanceMethod(methodReference.getExpression(), methodNode) && !isAssignableTo(samParameters[0].getType(), targetType)) {
            throw new RuntimeParserException("Invalid receiver type: " + samParameters[0].getType().getText() + " is not compatible with " + targetType.getText(), methodReference.getExpression());
        }
    }

    private MethodNode addSyntheticMethodForDGSM(final MethodNode mn) {
        Parameter[] parameters = removeFirstParameter(mn.getParameters());
        ArgumentListExpression args = new ArgumentListExpression(parameters);
        args.getExpressions().add(0, nullX());

        MethodCallExpression methodCall = callX(classX(mn.getDeclaringClass()), mn.getName(), args);
        methodCall.setImplicitThis(false);
        methodCall.setMethodTarget(mn);
        methodCall.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, mn);

        String methodName = "dgsm$$" + mn.getParameters()[0].getType().getName().replace('.', '$') + "$$" + mn.getName();

        MethodNode delegateMethod = addSyntheticMethod(methodName, mn.getReturnType(), methodCall, parameters, mn.getExceptions());
        delegateMethod.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
        return delegateMethod;
    }

    private MethodNode addSyntheticMethodForClassReference(final MethodNode mn, final ClassNode classType) {
        MethodCallExpression methodCall = callX(classX(classType), mn.getName(), new ArgumentListExpression(mn.getParameters()));
        methodCall.setImplicitThis(false);
        methodCall.setMethodTarget(mn);
        methodCall.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, mn);

        String methodName = createSyntheticMethodName("class", classType, mn.getName());

        ClassNode returnType = resolveClassNodeGenerics(Map.of(new GenericsType.GenericsTypeName("T"), new GenericsType(classType)), null, mn.getReturnType());

        MethodNode delegateMethod = addSyntheticMethod(methodName, returnType, methodCall, mn.getParameters(), mn.getExceptions());
        delegateMethod.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
        return delegateMethod;
    }

    private MethodNode addSyntheticMethodForVariadicReference(final MethodNode mn, final int samParameters, final boolean isStaticTarget) {
        Parameter[] parameters = new Parameter[samParameters];
        Expression arguments, receiver;
        if (mn.isStatic()) {
            for (int i = 0, j = mn.getParameters().length-1; i < samParameters; i += 1) {
                ClassNode t = mn.getParameters()[Math.min(i, j)].getType();
                if (i >= j) t = t.getComponentType(); // targets the array
                parameters[i] = new Parameter(t, "p" + i);
            }
            arguments = new ArgumentListExpression(parameters);
            receiver = classX(mn.getDeclaringClass());
        } else {
            int p = 0;
            if (isStaticTarget) parameters[p++] = new Parameter(mn.getDeclaringClass(), "target");
            for (int i = 0, j = mn.getParameters().length-1, n = samParameters-p; i < n; i += 1) {
                ClassNode t = mn.getParameters()[Math.min(i, j)].getType();
                if (i >= j) t = t.getComponentType(); // targets the array
                parameters[p] = new Parameter(t, "p" + p); p += 1;
            }
            if (isStaticTarget) {
                arguments = new ArgumentListExpression(removeFirstParameter(parameters));
                receiver = varX(parameters[0]);
            } else {
                arguments = new ArgumentListExpression(parameters);
                receiver = varX("this", controller.getClassNode());
            }
        }

        MethodCallExpression methodCall = callX(receiver, mn.getName(), arguments);
        methodCall.setImplicitThis(false);
        methodCall.setMethodTarget(mn);
        methodCall.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, mn);

        String methodName = createSyntheticMethodName("adapt", mn.getDeclaringClass(), mn.getName());

        MethodNode delegateMethod = addSyntheticMethod(methodName, mn.getReturnType(), methodCall, parameters, mn.getExceptions());
        if (!isStaticTarget && !mn.isStatic()) delegateMethod.setModifiers(delegateMethod.getModifiers() & ~Opcodes.ACC_STATIC);
        delegateMethod.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
        delegateMethod.setGenericsTypes(mn.getGenericsTypes());
        return delegateMethod;
    }

    private MethodNode addSyntheticMethodForConstructorReference(final String methodName, final ClassNode returnType, final Parameter[] parametersWithExactType) {
        ArgumentListExpression ctorArgs = new ArgumentListExpression(parametersWithExactType);

        Expression returnValue;
        if (returnType.isArray()) {
            returnValue = new ArrayExpression(
                    returnType.getComponentType(),
                    null, ctorArgs.getExpressions());
        } else {
            returnValue = ctorX(returnType, ctorArgs);
        }

        MethodNode delegateMethod = addSyntheticMethod(methodName, returnType, returnValue, parametersWithExactType, ClassNode.EMPTY_ARRAY);
        // TODO: if StaticTypesMarker.DIRECT_METHOD_CALL_TARGET or
        // OptimizingStatementWriter.StatementMeta.class metadatas
        // can bet set for the ctorX above, then this can be TRUE:
        delegateMethod.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.FALSE);
        return delegateMethod;
    }

    private MethodNode addSyntheticMethod(final String methodName, final ClassNode returnType, final Expression returnValue, final Parameter[] parameters, final ClassNode[] exceptions) {
        return controller.getClassNode().addSyntheticMethod(
            methodName,
            Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
            returnType,
            parameters,
            exceptions,
            returnS(returnValue));
    }

    private Parameter[] createParametersWithExactType(final MethodNode abstractMethod, final ClassNode[] inferredParamTypes) {
        // MUST clone the parameters to avoid impacting the original parameter type of SAM
        Parameter[] parameters = GeneralUtils.cloneParams(abstractMethod.getParameters());

        if (inferredParamTypes != null) {
            for (int i = 0, n = parameters.length; i < n; i += 1) {
                ClassNode inferredParamType = i < inferredParamTypes.length ? inferredParamTypes[i] : parameters[i].getType();
                if (inferredParamType == null) continue;
                Parameter parameter = parameters[i];

                ClassNode type = convertParameterType(parameter.getType(), parameter.getType(), inferredParamType);
                parameter.setOriginType(type);
                parameter.setType(type);
            }
        }

        return parameters;
    }

    private MethodNode findMethodRefMethod(final String methodName, final Parameter[] samParameters, final Expression typeOrTargetRef, final ClassNode typeOrTargetRefType) {
        List<MethodNode> methods = findVisibleMethods(methodName, typeOrTargetRefType);

        java.util.function.ToIntFunction<Parameter[]> distance = (parameters) -> { // GROOVY-10972: select from closest matches
            return allParametersAndArgumentsMatch(parameters, Arrays.stream(samParameters).map(Parameter::getType).toArray(ClassNode[]::new));
        };

        int[] distances = methods.stream().mapToInt(method -> {
            Parameter[] parameters = method.getParameters();
            if (isTypeReferringInstanceMethod(typeOrTargetRef, method)) {
                // there is an implicit parameter for "String::length"
                ClassNode firstParamType = method.getDeclaringClass();

                int n = parameters.length;
                Parameter[] plusOne = new Parameter[n + 1];
                plusOne[0] = new Parameter(firstParamType, "");
                System.arraycopy(parameters, 0, plusOne, 1, n);

                parameters = plusOne;
            }

            if (parameters.length > 0 && !method.isStatic() && !isExtensionMethod(method)
                    && typeOrTargetRefType.redirect().getGenericsTypes() != null) { // GROOVY-10994
                Map<GenericsType.GenericsTypeName, GenericsType> spec = extractPlaceholders(typeOrTargetRefType);
                parameters = Arrays.stream(parameters).map(p -> new Parameter(resolveClassNodeGenerics(spec, null, p.getType()), p.getName())).toArray(Parameter[]::new);
            }

            // check direct match
            if (parametersCompatible(samParameters, parameters)) return distance.applyAsInt(parameters);

            // check vararg match
            if (isVargs(parameters)) {
                int nParameters = parameters.length;
                if (samParameters.length == nParameters - 1) { // 0 case
                    parameters = Arrays.copyOf(parameters, nParameters - 1);
                    if (parametersCompatible(samParameters, parameters)) return distance.applyAsInt(parameters);
                }
                else if (samParameters.length >= nParameters) { // 1+ case
                    Parameter p = new Parameter(parameters[nParameters - 1].getType().getComponentType(), "");
                    parameters = Arrays.copyOf(parameters, samParameters.length);
                    for (int i = nParameters - 1; i < parameters.length; i += 1){
                        parameters[i] = p;
                    }
                    if (parametersCompatible(samParameters, parameters)) return distance.applyAsInt(parameters);
                }
            }

            return -1; // no match
        }).toArray();

        int i = 0, bestDistance = Arrays.stream(distances).filter(d -> d >= 0).min().orElse(0);
        for (Iterator<MethodNode> it = methods.iterator(); it.hasNext(); ) {
            it.next(); if (distances[i++] != bestDistance) it.remove();
        }

        if (methods.isEmpty()) return null;
        if (methods.size() == 1) return methods.get(0);
        return chooseMethodRefMethod(methods, typeOrTargetRef, typeOrTargetRefType);
    }

    private MethodNode chooseMethodRefMethod(final List<MethodNode> methods, final Expression typeOrTargetRef, final ClassNode typeOrTargetRefType) {
        return methods.stream().max(comparingInt((MethodNode mn) -> {
            int score = 9;
            for (ClassNode cn = typeOrTargetRefType; cn != null && !cn.equals(mn.getDeclaringClass()); cn = cn.getSuperClass()) {
                score -= 1;
            }
            if (score < 0) {
                score = 0;
            }
            score *= 10;
            if ((typeOrTargetRef instanceof ClassExpression) == isStaticMethod(mn)) {
                score += 9;
            }
            return score;
        }).thenComparing(StaticTypesMethodReferenceExpressionWriter::isExtensionMethod)).get();
    }

    private List<MethodNode> findVisibleMethods(final String name, final ClassNode type) {
        List<MethodNode> methods = type.getMethods(name);
        // GROOVY-10791, GROOVY-11467: include non-static interface methods
        Set<ClassNode> implemented = getInterfacesAndSuperInterfaces(type);
        implemented.remove(type);
        for (ClassNode cn : implemented) {
            for (MethodNode mn : cn.getDeclaredMethods(name)) {
                if (mn.isDefault() || (mn.isPublic() && !mn.isStatic() && type.isAbstract())) {
                    methods.add(mn);
                }
            }
        }
        methods.addAll(findDGMMethodsForClassNode(controller.getSourceUnit().getClassLoader(), type, name));
        return filterMethodsByVisibility(methods, controller.getClassNode());
    }

    private void addFatalError(final String msg, final ASTNode node) {
        controller.getSourceUnit().addFatalError(msg, node);
        // GRECLIPSE: addFatalError won't throw for quick parse
        throw new MultipleCompilationErrorsException(controller.getSourceUnit().getErrorCollector());
    }

    private void ensureDeserializeLambdaSupport(final MethodReferenceExpression methodReferenceExpression,
                                                final FunctionalInterfaceContext functionalInterface,
                                                final ResolvedMethodReference resolvedMethodReference,
                                                final MethodReferenceInvocation invocation) {
        String helperName = getOrCreateDeserializeLambdaMethodName(methodReferenceExpression);
        Parameter[] parameters = createDeserializeMethodParameters();
        if (controller.getClassNode().hasMethod(helperName, parameters)) {
            return;
        }

        MethodReferenceTarget referenceTarget = resolvedMethodReference.referenceTarget();
        MethodNode methodRefMethod = resolvedMethodReference.implementationMethod();
        MethodNode helperMethod = addDeserializeLambdaMethodForMethodReference(
            helperName,
            functionalInterface.abstractMethod(),
            methodRefMethod,
            functionalInterface.parametersWithExactType(),
            invocation.implMethodKind(),
            invocation.capturing(),
            referenceTarget.type(),
            invocation.invokedTypeDescriptor(),
            functionalInterface.samMethodDescriptor()
        );
        addDeserializeDispatcherEntry(controller, parameters,
            createSerializedLambdaFingerprint(functionalInterface.samMethodDescriptor(), controller.getClassNode(), invocation.implMethodKind(),
                methodRefMethod.getDeclaringClass(), methodRefMethod,
                functionalInterface.parametersWithExactType(), functionalInterface.functionalType(),
                functionalInterface.abstractMethod(), invocation.capturing() ? 1 : 0),
            helperMethod);
    }

    private String getOrCreateDeserializeLambdaMethodName(final MethodReferenceExpression methodReferenceExpression) {
        String helperName = methodReferenceExpression.getNodeMetaData(METHOD_REFERENCE_DESERIALIZE_METHOD_NAME);
        if (helperName == null) {
            helperName = createDeserializeLambdaMethodName();
            methodReferenceExpression.putNodeMetaData(METHOD_REFERENCE_DESERIALIZE_METHOD_NAME, helperName);
        }
        return helperName;
    }

    private MethodNode addDeserializeLambdaMethodForMethodReference(final String methodName, final MethodNode abstractMethod,
                                                                    final MethodNode methodRefMethod, final Parameter[] parametersWithExactType,
                                                                    final int implMethodKind, final boolean capturing,
                                                                    final ClassNode capturedTargetType, final String invokedTypeDescriptor,
                                                                    final String samMethodDescriptor) {
        return controller.getClassNode().addSyntheticMethod(
                methodName,
                ACC_PRIVATE | ACC_STATIC,
                ClassHelper.OBJECT_TYPE,
                createDeserializeMethodParameters(),
                ClassNode.EMPTY_ARRAY,
                new BytecodeSequence(new BytecodeInstruction() {
                    @Override
                    public void visit(final MethodVisitor mv) {
                        if (capturing) {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitInsn(ICONST_0);
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/SerializedLambda", "getCapturedArg", "(I)Ljava/lang/Object;", false);
                            mv.visitTypeInsn(CHECKCAST, getClassInternalName(capturedTargetType));
                        }
                        writeFunctionalInterfaceIndy(
                            mv,
                            abstractMethod.getName(),
                            invokedTypeDescriptor,
                            samMethodDescriptor,
                            implMethodKind,
                            methodRefMethod.getDeclaringClass(),
                            methodRefMethod,
                            parametersWithExactType,
                            true
                        );
                        mv.visitInsn(ARETURN);
                    }
                }));
    }

    private String createSyntheticMethodName(final String prefix, final ClassNode owner, final String name) {
        return prefix + "$" + owner.getNameWithoutPackage() + "$" + name + "$" + controller.getNextHelperMethodIndex();
    }

    private String createDeserializeLambdaMethodName() {
        return "$deserializeLambda_methodref$" + controller.getNextHelperMethodIndex() + "$";
    }

    //--------------------------------------------------------------------------

    private static boolean isBridgeMethod(final MethodNode mn) {
        int staticSynthetic = Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC;
        return ((mn.getModifiers() & staticSynthetic) == staticSynthetic) && mn.getName().startsWith("access$");
    }

    private static boolean isConstructorReference(final String name) {
        return "new".equals(name);
    }

    private static boolean isExtensionMethod(final MethodNode mn) {
        return (mn instanceof ExtensionMethodNode);
    }

    private static boolean isStaticMethod(final MethodNode mn) {
        return isExtensionMethod(mn) ? ((ExtensionMethodNode) mn).isStaticExtension() : mn.isStatic();
    }

    private static boolean isTypeReferringInstanceMethod(final Expression typeOrTargetRef, final MethodNode mn) {
        // class::instanceMethod
        return (typeOrTargetRef instanceof ClassExpression) && (mn != null && !isStaticMethod(mn));
    }

    private static Expression makeClassTarget(final ClassNode target, final Expression source) {
        Expression expression = classX(target);
        expression.setSourcePosition(source);
        return expression;
    }

    private static Parameter[] removeFirstParameter(final Parameter[] parameters) {
        return Arrays.copyOfRange(parameters, 1, parameters.length);
    }

    /**
     * Captures the functional-interface side of a method reference after type
     * inference has fixed the SAM signature.
     */
    private record FunctionalInterfaceContext(ClassNode functionalType, MethodNode abstractMethod,
                                              Parameter[] parametersWithExactType, String samMethodDescriptor,
                                              boolean serializable, ClassNode[] markers) {
    }

    /**
     * Models the source-side target of a method reference, including whether
     * the target must still be captured at runtime.
     */
    private record MethodReferenceTarget(Expression expression, ClassNode type, boolean classExpression, boolean targetIsArgument) {
        private MethodReferenceTarget markTargetAsArgument() {
            return new MethodReferenceTarget(expression, type, classExpression, true);
        }

        private MethodReferenceTarget asClassTarget(final ClassNode targetType) {
            return new MethodReferenceTarget(makeClassTarget(targetType, expression), targetType, true, targetIsArgument);
        }

        private boolean isCapturing() {
            return !classExpression;
        }
    }

    /**
     * Selected implementation method together with the possibly rewritten
     * method-reference target used to invoke it.
     */
    private record ResolvedMethodReference(MethodReferenceTarget referenceTarget, String methodName,
                                           MethodNode implementationMethod, boolean constructorReference) {
        private ResolvedMethodReference with(final MethodReferenceTarget updatedTarget, final MethodNode updatedMethod) {
            return new ResolvedMethodReference(updatedTarget, methodName, updatedMethod, constructorReference);
        }

        private ResolvedMethodReference withTarget(final MethodReferenceTarget updatedTarget) {
            return new ResolvedMethodReference(updatedTarget, methodName, implementationMethod, constructorReference);
        }
    }

    /**
     * Bytecode-level invocation details derived from the resolved method reference.
     */
    private record MethodReferenceInvocation(int implMethodKind, String invokedTypeDescriptor, boolean capturing) {
    }
}

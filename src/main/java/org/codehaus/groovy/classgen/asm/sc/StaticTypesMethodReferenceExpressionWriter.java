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
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.MethodReferenceExpressionWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import static org.codehaus.groovy.ast.tools.ParameterUtils.isVargs;
import static org.codehaus.groovy.ast.tools.ParameterUtils.parametersCompatible;
import static org.codehaus.groovy.runtime.ArrayGroovyMethods.last;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.allParametersAndArgumentsMatch;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.filterMethodsByVisibility;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsForClassNode;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isAssignableTo;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.resolveClassNodeGenerics;

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
        // functional interface target is required for native method reference generation
        ClassNode  functionalType = methodReferenceExpression.getNodeMetaData(StaticTypesMarker.PARAMETER_TYPE);
        MethodNode abstractMethod = ClassHelper.findSAM(functionalType);
        if (abstractMethod == null || !functionalType.isInterface()) {
            // generate the default bytecode -- most likely a method closure
            super.writeMethodReferenceExpression(methodReferenceExpression);
            return;
        }

        ClassNode classNode = controller.getClassNode();
        Expression typeOrTargetRef = methodReferenceExpression.getExpression();
        boolean isClassExpression = (typeOrTargetRef instanceof ClassExpression);
        boolean targetIsArgument = false; // implied argument for expr::staticMethod?
        ClassNode typeOrTargetRefType = isClassExpression ? typeOrTargetRef.getType()
                : controller.getTypeChooser().resolveType(typeOrTargetRef, classNode);

        if (ClassHelper.isPrimitiveType(typeOrTargetRefType)) // GROOVY-11353
            typeOrTargetRefType = ClassHelper.getWrapper(typeOrTargetRefType);

        ClassNode[] methodReferenceParamTypes = methodReferenceExpression.getNodeMetaData(StaticTypesMarker.CLOSURE_ARGUMENTS);
        Parameter[] parametersWithExactType = createParametersWithExactType(abstractMethod, methodReferenceParamTypes);
        String methodRefName = methodReferenceExpression.getMethodName().getText();
        boolean isConstructorReference = isConstructorReference(methodRefName);

        MethodNode methodRefMethod;
        if (isConstructorReference) {
            methodRefName = controller.getContext().getNextConstructorReferenceSyntheticMethodName(controller.getMethodNode());
            methodRefMethod = addSyntheticMethodForConstructorReference(methodRefName, typeOrTargetRefType, parametersWithExactType);
        } else {
            // TODO: move the findMethodRefMethod and checking to StaticTypeCheckingVisitor
            methodRefMethod = findMethodRefMethod(methodRefName, parametersWithExactType, typeOrTargetRef, typeOrTargetRefType);
        }

        validate(methodReferenceExpression, typeOrTargetRefType, methodRefName, methodRefMethod, parametersWithExactType,
                resolveClassNodeGenerics(extractPlaceholders(functionalType), null, abstractMethod.getReturnType()));

        if (isExtensionMethod(methodRefMethod)) {
            ExtensionMethodNode extensionMethodNode = (ExtensionMethodNode) methodRefMethod;
            methodRefMethod  = extensionMethodNode.getExtensionMethodNode();
            boolean isStatic = extensionMethodNode.isStaticExtension();
            if (isStatic) { // create adapter method to pass extra argument
                methodRefMethod = addSyntheticMethodForDGSM(methodRefMethod);
            }
            if (isStatic || isClassExpression) {
                // replace expression with declaring type
                typeOrTargetRefType = methodRefMethod.getDeclaringClass();
                typeOrTargetRef = makeClassTarget(typeOrTargetRefType, typeOrTargetRef);
            } else { // GROOVY-10653
                targetIsArgument = true; // ex: "string"::size
            }
        } else if (isVargs(methodRefMethod.getParameters())) {
            int mParameters = abstractMethod.getParameters().length;
            int nParameters = methodRefMethod.getParameters().length;
            if (isTypeReferringInstanceMethod(typeOrTargetRef, methodRefMethod)) nParameters += 1;
            if (mParameters > nParameters || mParameters == nParameters-1 || (mParameters == nParameters
                    && !isAssignableTo(last(parametersWithExactType).getType(), last(methodRefMethod.getParameters()).getType()))) {
                // GROOVY-9813: reference to variadic method which needs adapter method to match runtime arguments to its parameters
                if (!isClassExpression && !methodRefMethod.isStatic() && !methodRefMethod.getDeclaringClass().equals(classNode)) {
                    targetIsArgument = true; // GROOVY-10653: create static adapter in source class with target as first parameter
                    mParameters += 1;
                }
                methodRefMethod = addSyntheticMethodForVariadicReference(methodRefMethod, mParameters, isClassExpression || targetIsArgument);
                if (methodRefMethod.isStatic() && !targetIsArgument) {
                    // replace expression with declaring type
                    typeOrTargetRefType = methodRefMethod.getDeclaringClass();
                    typeOrTargetRef = makeClassTarget(typeOrTargetRefType, typeOrTargetRef);
                }
            }
        }

        if (!isClassExpression) {
            if (isConstructorReference) { // TODO: move this check to the parser
                addFatalError("Constructor reference must be TypeName::new", methodReferenceExpression);
            } else if (methodRefMethod.isStatic() && !targetIsArgument) {
                // "string"::valueOf refers to static method, so instance is superfluous
                typeOrTargetRef = makeClassTarget(typeOrTargetRefType, typeOrTargetRef);
                isClassExpression = true;
            } else {
                typeOrTargetRef.visit(controller.getAcg());
                controller.getOperandStack().box(); // GROOVY-11353
            }
        }

        int referenceKind;
        if (isConstructorReference || methodRefMethod.isStatic()) {
            referenceKind = Opcodes.H_INVOKESTATIC;
        } else if (methodRefMethod.getDeclaringClass().isInterface()) {
            referenceKind = Opcodes.H_INVOKEINTERFACE; // GROOVY-9853
        } else {
            referenceKind = Opcodes.H_INVOKEVIRTUAL;
        }

        String methodName = abstractMethod.getName();
        String methodDesc = BytecodeHelper.getMethodDescriptor(functionalType.redirect(),
                isClassExpression ? Parameter.EMPTY_ARRAY : new Parameter[]{new Parameter(typeOrTargetRefType, "__METHODREF_EXPR_INSTANCE")});

        Handle bootstrapMethod = createBootstrapMethod(classNode.isInterface(), false);
        Object[] bootstrapArgs = createBootstrapMethodArguments(
                createMethodDescriptor(abstractMethod),
                referenceKind,
                methodRefMethod.getDeclaringClass(),
                methodRefMethod,
                parametersWithExactType,
                false
        );
        controller.getMethodVisitor().visitInvokeDynamicInsn(methodName, methodDesc, bootstrapMethod, bootstrapArgs);

        if (isClassExpression) {
            controller.getOperandStack().push(functionalType);
        } else {
            controller.getOperandStack().replace(functionalType, 1);
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

        String methodName = "adapt$" + mn.getDeclaringClass().getNameWithoutPackage() + "$" + mn.getName() + "$" + System.nanoTime();

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

                ClassNode type = convertParameterType(parameter.getType(), inferredParamType);
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
        // GROOVY-10791: include interface default methods in search
        for (ClassNode cn : getInterfacesAndSuperInterfaces(type)) {
            for (MethodNode mn : cn.getDeclaredMethods(name)) {
                if (mn.isDefault()) methods.add(mn);
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

    //--------------------------------------------------------------------------

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
}

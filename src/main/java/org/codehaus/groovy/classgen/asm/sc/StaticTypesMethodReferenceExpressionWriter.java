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

import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
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
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.ParameterUtils.isVargs;
import static org.codehaus.groovy.ast.tools.ParameterUtils.parametersCompatible;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.filterMethodsByVisibility;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsForClassNode;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isAssignableTo;

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
        ClassNode functionalInterfaceType = getFunctionalInterfaceType(methodReferenceExpression);
        if (!ClassHelper.isFunctionalInterface(functionalInterfaceType)) {
            // generate the default bytecode; most likely a method closure
            super.writeMethodReferenceExpression(methodReferenceExpression);
            return;
        }

        ClassNode redirect = functionalInterfaceType.redirect();
        MethodNode abstractMethod = ClassHelper.findSAM(redirect);
        String abstractMethodDesc = createMethodDescriptor(abstractMethod);

        ClassNode classNode = controller.getClassNode();
        Expression typeOrTargetRef = methodReferenceExpression.getExpression();
        boolean isClassExpression = (typeOrTargetRef instanceof ClassExpression);
        ClassNode typeOrTargetRefType = isClassExpression ? typeOrTargetRef.getType()
                : controller.getTypeChooser().resolveType(typeOrTargetRef, classNode);

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

        validate(methodReferenceExpression, typeOrTargetRef, typeOrTargetRefType, methodRefName, parametersWithExactType, methodRefMethod);

        if (isExtensionMethod(methodRefMethod)) {
            ExtensionMethodNode extensionMethodNode = (ExtensionMethodNode) methodRefMethod;
            methodRefMethod = extensionMethodNode.getExtensionMethodNode();
            if (extensionMethodNode.isStaticExtension()) {
                methodRefMethod = addSyntheticMethodForDGSM(methodRefMethod);
            }
            typeOrTargetRef = makeClassTarget(methodRefMethod.getDeclaringClass(), typeOrTargetRef);
            typeOrTargetRefType = typeOrTargetRef.getType();

        } else if (isVargs(methodRefMethod.getParameters())) {
            int mParameters = abstractMethod.getParameters().length;
            int nParameters = methodRefMethod.getParameters().length;
            if (isTypeReferringInstanceMethod(typeOrTargetRef, methodRefMethod)) nParameters += 1;
            if (mParameters > nParameters || mParameters == nParameters-1 || (mParameters == nParameters
                    && !isAssignableTo(last(parametersWithExactType).getType(), last(methodRefMethod.getParameters()).getType()))) {
                methodRefMethod = addSyntheticMethodForVariadicReference(methodRefMethod, mParameters, isClassExpression); // GROOVY-9813
                if (methodRefMethod.isStatic()) {
                    typeOrTargetRef = makeClassTarget(methodRefMethod.getDeclaringClass(), typeOrTargetRef);
                    typeOrTargetRefType = typeOrTargetRef.getType();
                }
            }
        }

        if (!isClassExpression) {
            if (isConstructorReference) {
                // TODO: move the checking code to the parser
                addFatalError("Constructor reference must be className::new", methodReferenceExpression);
            } else if (methodRefMethod.isStatic()) {
                ClassExpression classExpression = classX(typeOrTargetRefType);
                classExpression.setSourcePosition(typeOrTargetRef);
                typeOrTargetRef = classExpression;
                isClassExpression = true;
            } else {
                typeOrTargetRef.visit(controller.getAcg());
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

        methodRefMethod.putNodeMetaData(ORIGINAL_PARAMETERS_WITH_EXACT_TYPE, parametersWithExactType);
        try {
            controller.getMethodVisitor().visitInvokeDynamicInsn(
                    abstractMethod.getName(),
                    createAbstractMethodDesc(functionalInterfaceType, typeOrTargetRef),
                    createBootstrapMethod(classNode.isInterface(), false),
                    createBootstrapMethodArguments(
                            abstractMethodDesc,
                            referenceKind,
                            isConstructorReference ? classNode : typeOrTargetRefType,
                            methodRefMethod,
                            false
                    )
            );
        } finally {
            methodRefMethod.removeNodeMetaData(ORIGINAL_PARAMETERS_WITH_EXACT_TYPE);
        }

        if (isClassExpression) {
            controller.getOperandStack().push(redirect);
        } else {
            controller.getOperandStack().replace(redirect, 1);
        }
    }

    private void validate(final MethodReferenceExpression methodReferenceExpression, final Expression typeOrTargetRef, final ClassNode typeOrTargetRefType, final String methodRefName, final Parameter[] parametersWithExactType, final MethodNode methodRefMethod) {
        if (methodRefMethod == null) {
            addFatalError("Failed to find the expected method["
                    + methodRefName + "("
                    + Arrays.stream(parametersWithExactType)
                            .map(e -> e.getType().getText())
                            .collect(Collectors.joining(","))
                    + ")] in the type[" + typeOrTargetRefType.getText() + "]", methodReferenceExpression);
        } else if (parametersWithExactType.length > 0 && isTypeReferringInstanceMethod(typeOrTargetRef, methodRefMethod)) {
            ClassNode firstParameterType = parametersWithExactType[0].getType();
            if (!isAssignableTo(firstParameterType, typeOrTargetRefType)) {
                throw new RuntimeParserException("Invalid receiver type: " + firstParameterType.getText() + " is not compatible with " + typeOrTargetRefType.getText(), typeOrTargetRef);
            }
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
        Expression arguments = null, receiver = null;
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
            if (isStaticTarget) parameters[p++] = new Parameter(mn.getDeclaringClass(), "o");
            for (int i = 0, j = mn.getParameters().length-1; i < samParameters - p; i += 1) {
                ClassNode t = mn.getParameters()[Math.min(i, j)].getType();
                if (i >= j) t = t.getComponentType(); // targets the array
                parameters[p++] = new Parameter(t, "p" + p);
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

    private String createAbstractMethodDesc(final ClassNode functionalInterfaceType, final Expression methodRef) {
        List<Parameter> methodReferenceSharedVariableList = new ArrayList<>();

        if (!(methodRef instanceof ClassExpression)) {
            prependParameter(methodReferenceSharedVariableList, "__METHODREF_EXPR_INSTANCE",
                controller.getTypeChooser().resolveType(methodRef, controller.getClassNode()));
        }

        return BytecodeHelper.getMethodDescriptor(functionalInterfaceType.redirect(), methodReferenceSharedVariableList.toArray(Parameter.EMPTY_ARRAY));
    }

    private Parameter[] createParametersWithExactType(final MethodNode abstractMethod, final ClassNode[] inferredParamTypes) {
        // MUST clone the parameters to avoid impacting the original parameter type of SAM
        Parameter[] parameters = GeneralUtils.cloneParams(abstractMethod.getParameters());

        if (inferredParamTypes != null) {
            for (int i = 0, n = parameters.length; i < n; i += 1) {
                ClassNode inferredParamType = inferredParamTypes[i];
                if (inferredParamType == null) continue;

                Parameter parameter = parameters[i];
                Parameter targetParameter = parameter;

                ClassNode type = convertParameterType(targetParameter.getType(), parameter.getType(), inferredParamType);
                parameter.setOriginType(type);
                parameter.setType(type);
            }
        }

        return parameters;
    }

    private MethodNode findMethodRefMethod(final String methodName, final Parameter[] samParameters, final Expression typeOrTargetRef, final ClassNode typeOrTargetRefType) {
        List<MethodNode> methods = findVisibleMethods(methodName, typeOrTargetRefType);

        methods.removeIf(method -> {
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

            // check direct match
            if (parametersCompatible(samParameters, parameters)) return false;

            // check vararg match
            if (isVargs(parameters)) {
                int nParameters = parameters.length;
                if (samParameters.length == nParameters - 1) { // 0 case
                    parameters = Arrays.copyOf(parameters, nParameters - 1);
                    if (parametersCompatible(samParameters, parameters)) return false;
                }
                else if (samParameters.length >= nParameters) { // 1+ case
                    Parameter p = new Parameter(parameters[nParameters - 1].getType().getComponentType(), "");
                    parameters = Arrays.copyOf(parameters, samParameters.length);
                    for (int i = nParameters - 1; i < parameters.length; i += 1){
                        parameters[i] = p;
                    }
                    if (parametersCompatible(samParameters, parameters)) return false;
                }
            }

            return true; // no match; remove method
        });

        return chooseMethodRefMethodCandidate(typeOrTargetRef, methods);
    }

    private List<MethodNode> findVisibleMethods(final String name, final ClassNode type) {
        List<MethodNode> methods = type.getMethods(name);
        methods.addAll(findDGMMethodsForClassNode(controller.getSourceUnit().getClassLoader(), type, name));
        methods = filterMethodsByVisibility(methods, controller.getClassNode());
        return methods;
    }

    private void addFatalError(final String msg, final ASTNode node) {
        controller.getSourceUnit().addFatalError(msg, node);
    }

    //--------------------------------------------------------------------------

    private static boolean isConstructorReference(final String methodRefName) {
        return "new".equals(methodRefName);
    }

    private static boolean isExtensionMethod(final MethodNode methodRefMethod) {
        return (methodRefMethod instanceof ExtensionMethodNode);
    }

    private static boolean isTypeReferringInstanceMethod(final Expression typeOrTargetRef, final MethodNode mn) {
        // class::instanceMethod
        return (typeOrTargetRef instanceof ClassExpression) && ((mn != null && !mn.isStatic())
                || (isExtensionMethod(mn) && !((ExtensionMethodNode) mn).isStaticExtension()));
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
     * Chooses the best method node for method reference.
     */
    private static MethodNode chooseMethodRefMethodCandidate(final Expression methodRef, final List<MethodNode> candidates) {
        if (candidates.size() == 1) return candidates.get(0);

        return candidates.stream()
                .map(e -> Tuple.tuple(e, matchingScore(e, methodRef)))
                .min((t1, t2) -> Integer.compare(t2.getV2(), t1.getV2()))
                .map(Tuple2::getV1)
                .orElse(null);
    }

    private static Integer matchingScore(final MethodNode mn, final Expression typeOrTargetRef) {
        ClassNode typeOrTargetRefType = typeOrTargetRef.getType(); // TODO: pass this type in

        int score = 9;
        for (ClassNode cn = mn.getDeclaringClass(); null != cn && !cn.equals(typeOrTargetRefType); cn = cn.getSuperClass()) {
            score -= 1;
        }
        if (score < 0) {
            score = 0;
        }
        score *= 10;

        if ((typeOrTargetRef instanceof ClassExpression) == mn.isStatic()) {
            score += 9;
        }

        if (isExtensionMethod(mn)) {
            score += 100;
        }

        return score;
    }
}

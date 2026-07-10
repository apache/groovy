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

import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.query.AstQuery;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.classgen.BytecodeInstruction;
import org.codehaus.groovy.classgen.BytecodeSequence;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.LambdaWriter;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.classgen.asm.WriterControllerFactory;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.GENERATED_LAMBDA_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.SERIALIZABLE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.tools.ClosureUtils.getParametersSafe;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.classgen.asm.sc.StaticTypesFunctionalInterfaceMetadataKey.LAMBDA_GENERATED_CONSTRUCTOR;
import static org.codehaus.groovy.classgen.asm.sc.StaticTypesFunctionalInterfaceMetadataKey.LAMBDA_PRELOADED_RECEIVER;
import static org.codehaus.groovy.classgen.asm.sc.StaticTypesFunctionalInterfaceMetadataKey.LAMBDA_SHARED_VARIABLES;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.CLOSURE_ARGUMENTS;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.LAMBDA_MARKERS;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PARAMETER_TYPE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * Writer responsible for generating lambda classes in statically compiled mode.
 */
public class StaticTypesLambdaWriter extends LambdaWriter implements AbstractFunctionalInterfaceWriter {

    /**
     * Creates a lambda writer for statically compiled code generation.
     */
    public StaticTypesLambdaWriter(final WriterController controller) {
        super(controller);
        this.staticTypesClosureWriter = new StaticTypesClosureWriter(controller);
        this.lambdaAnalyzer = new StaticTypesLambdaAnalyzer(controller.getSourceUnit());
    }

    /** {@inheritDoc} */
    @Override
    public void writeLambda(final LambdaExpression expression) {
        // functional interface target is required for native lambda generation
        ClassNode functionalType = expression.getNodeMetaData(PARAMETER_TYPE);
        MethodNode abstractMethod = resolveFunctionalInterfaceMethod(functionalType);
        if (abstractMethod == null) {
            // generate bytecode for closure
            super.writeLambda(expression);
            return;
        }

        boolean serializable = makeSerializableIfNeeded(expression, functionalType);
        // GROOVY-11998: gather intersection-cast marker interfaces, filtering out
        // Serializable (already conveyed via FLAG_SERIALIZABLE) and any interface
        // already implemented by the SAM target.
        ClassNode[] markers = collectLambdaMarkers(expression, functionalType);
        GeneratedLambda generatedLambda = getOrAddGeneratedLambda(expression, abstractMethod);

        if (generatedLambda.isHoistedCapturing()) {
            writeHoistedCapturingLambda(functionalType.redirect(), abstractMethod, generatedLambda, serializable, markers);
            return;
        }

        ensureDeserializeLambdaSupport(expression, functionalType, abstractMethod, generatedLambda, serializable);
        if (generatedLambda.isCapturing() && !isPreloadedLambdaReceiver(generatedLambda)) {
            loadLambdaReceiver(generatedLambda);
        }

        writeLambdaFactoryInvocation(functionalType.redirect(), abstractMethod, generatedLambda, serializable, markers);
    }

    @SuppressWarnings("unchecked")
    private static ClassNode[] collectLambdaMarkers(final LambdaExpression expression, final ClassNode functionalType) {
        Object md = expression.getNodeMetaData(LAMBDA_MARKERS);
        if (!(md instanceof java.util.List)) return ClassNode.EMPTY_ARRAY;
        java.util.List<ClassNode> raw = (java.util.List<ClassNode>) md;
        java.util.List<ClassNode> out = new java.util.ArrayList<>(raw.size());
        for (ClassNode m : raw) {
            if (m == null || !m.isInterface()) continue;
            if (m.equals(SERIALIZABLE_TYPE) || SERIALIZABLE_TYPE.equals(m.redirect())) continue;
            if (functionalType != null && functionalType.implementsInterface(m)) continue;
            out.add(m);
        }
        return out.toArray(ClassNode.EMPTY_ARRAY);
    }

    private static MethodNode resolveFunctionalInterfaceMethod(final ClassNode functionalType) {
        if (functionalType == null || !functionalType.isInterface()) {
            return null;
        }
        return ClassHelper.findSAM(functionalType);
    }

    private boolean makeSerializableIfNeeded(final LambdaExpression expression, final ClassNode functionalType) {
        if (!expression.isSerializable() && functionalType.implementsInterface(SERIALIZABLE_TYPE)) {
            expression.setSerializable(true);
        }
        return expression.isSerializable();
    }

    private void ensureDeserializeLambdaSupport(final LambdaExpression expression, final ClassNode functionalType, final MethodNode abstractMethod, final GeneratedLambda generatedLambda, final boolean serializable) {
        if (!serializable || hasDeserializeLambdaMethod(generatedLambda.lambdaClass)) {
            return;
        }

        String samMethodDescriptor = createMethodDescriptor(abstractMethod);
        MethodNode helperMethod = addDeserializeLambdaMethodForLambdaExpression(expression, generatedLambda);
        addDeserializeDispatcherEntry(controller, createDeserializeMethodParameters(), createSerializedLambdaFingerprint(
                samMethodDescriptor,
                controller.getClassNode(),
                generatedLambda.getImplMethodKind(),
                generatedLambda.lambdaClass,
                generatedLambda.lambdaMethod,
                generatedLambda.lambdaMethod.getParameters(),
                functionalType,
                abstractMethod,
                generatedLambda.isCapturing() ? 1 : 0
        ), helperMethod);
    }

    private void writeLambdaFactoryInvocation(final ClassNode functionalType, final MethodNode abstractMethod, final GeneratedLambda generatedLambda, final boolean serializable, final ClassNode[] markers) {
        writeFunctionalInterfaceIndy(
            controller.getMethodVisitor(),
            abstractMethod.getName(),
            createLambdaFactoryMethodDescriptor(functionalType, generatedLambda),
            createMethodDescriptor(abstractMethod),
            generatedLambda.getImplMethodKind(),
            generatedLambda.lambdaClass,
            generatedLambda.lambdaMethod,
            generatedLambda.lambdaMethod.getParameters(),
            serializable,
            markers
        );

        if (generatedLambda.nonCapturing()) {
            controller.getOperandStack().push(functionalType);
        } else {
            controller.getOperandStack().replace(functionalType, 1);
        }
    }

    private boolean hasDeserializeLambdaMethod(final ClassNode lambdaClass) {
        return controller.getClassNode().hasMethod(createDeserializeLambdaMethodName(lambdaClass), createDeserializeMethodParameters());
    }

    private static MethodNode getLambdaMethod(final ClassNode lambdaClass) {
        List<MethodNode> lambdaMethods = lambdaClass.getMethods(DO_CALL);
        if (lambdaMethods.isEmpty()) {
            throw new GroovyBugError("Failed to find the synthetic lambda method in " + lambdaClass.getName());
        }
        return lambdaMethods.get(0);
    }

    private static ConstructorNode getGeneratedConstructor(final ClassNode lambdaClass) {
        for (ConstructorNode constructorNode : lambdaClass.getDeclaredConstructors()) {
            if (Boolean.TRUE.equals(constructorNode.getNodeMetaData(LAMBDA_GENERATED_CONSTRUCTOR))) {
                return constructorNode;
            }
        }
        throw new GroovyBugError("Failed to find the generated constructor in " + lambdaClass.getName());
    }

    private boolean isPreloadedLambdaReceiver(final GeneratedLambda generatedLambda) {
        MethodNode enclosingMethod = controller.getMethodNode();
        return enclosingMethod != null
            && enclosingMethod.getNodeMetaData(LAMBDA_PRELOADED_RECEIVER) == generatedLambda.lambdaClass;
    }

    private void loadLambdaReceiver(final GeneratedLambda generatedLambda) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();

        String lambdaClassInternalName = BytecodeHelper.getClassInternalName(generatedLambda.lambdaClass);
        mv.visitTypeInsn(NEW, lambdaClassInternalName);
        mv.visitInsn(DUP);

        if (controller.isStaticMethod() || compileStack.isInSpecialConstructorCall() || !generatedLambda.accessingInstanceMembers) {
            classX(controller.getThisType()).visit(controller.getAcg());
        } else {
            loadThis();
        }

        operandStack.dup();

        loadSharedVariables(generatedLambda.sharedVariables);

        Parameter[] lambdaClassConstructorParameters = generatedLambda.constructor.getParameters();
        mv.visitMethodInsn(INVOKESPECIAL, lambdaClassInternalName, "<init>", createMethodDescriptor(VOID_TYPE, lambdaClassConstructorParameters), generatedLambda.lambdaClass.isInterface());

        operandStack.replace(CLOSURE_TYPE, lambdaClassConstructorParameters.length);
    }

    private void loadSharedVariables(final Parameter[] lambdaSharedVariableParameters) {
        for (Parameter parameter : lambdaSharedVariableParameters) {
            loadReference(parameter.getName(), controller);
            if (parameter.getNodeMetaData(UseExistingReference.class) == null) {
                parameter.setNodeMetaData(UseExistingReference.class, Boolean.TRUE);
            }
        }
    }

    private String createLambdaFactoryMethodDescriptor(final ClassNode functionalInterface, final GeneratedLambda generatedLambda) {
        return createFunctionalInterfaceFactoryDescriptor(functionalInterface,
            generatedLambda.nonCapturing()
                ? Parameter.EMPTY_ARRAY
                : new Parameter[]{createCapturedReceiverParameter(generatedLambda.lambdaClass, "__lambda_this")});
    }

    private GeneratedLambda getOrAddGeneratedLambda(final LambdaExpression expression, final MethodNode abstractMethod) {
        return generatedLambdas.computeIfAbsent(expression, expr -> {
            // Build the lambda class first: this resolves capture/instance-member analysis correctly (a
            // doCall on the lambda class sees the enclosing class as an outer), and makes doCall static iff
            // the lambda is non-capturing.
            ClassNode lambdaClass = createLambdaClass(expr, ACC_FINAL | ACC_PUBLIC | ACC_STATIC, abstractMethod);
            MethodNode lambdaMethod = getLambdaMethod(lambdaClass);

            // Like Java, a non-capturing (static doCall), non-serializable lambda needs no generated class:
            // hoist its static impl onto the enclosing class and bootstrap the metafactory directly against it.
            // Opt-in (default off) while IDE/tooling compatibility is verified; set -Dgroovy.target.lambda.hoist=true.
            // Only hoist lambdas sitting directly in a real method: a lambda inside a closure/lambda would be
            // hoisted onto that generated function rather than a user class, so leave those as generated classes.
            boolean baseHoistable = isLambdaHoistEnabled() && !controller.isInGeneratedFunction()
                    && !expr.isSerializable() && !containsNestedFunction(expr.getCode());

            if (baseHoistable && !requiresLambdaInstance(lambdaMethod)) {
                MethodNode implMethod = hoistLambdaMethodToEnclosingClass(lambdaMethod);
                return new GeneratedLambda(controller.getClassNode(), implMethod, null, Parameter.EMPTY_ARRAY, true, false, null);
            }

            // Capturing but needing no 'this': hoist onto the enclosing class as a static method taking the
            // captured values as leading params, with the metafactory capturing them directly (like Java) -
            // eliminating the lambda class, its instance, and the Reference wrapping. Read-only captures only.
            Parameter[] shared = getStoredLambdaSharedVariables(expr);
            if (baseHoistable && shared.length != 0 && !lambdaAnalyzer.accessesInstanceMembers(lambdaMethod)
                    && !writesAnyCapture(lambdaMethod.getCode(), shared)) {
                GeneratedLambda hoisted = hoistCapturingLambda(lambdaClass, lambdaMethod, shared);
                if (hoisted != null) return hoisted;
            }

            controller.getAcg().addInnerClass(lambdaClass);
            lambdaClass.addInterface(GENERATED_LAMBDA_TYPE);
            lambdaClass.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
            lambdaClass.putNodeMetaData(WriterControllerFactory.class, (WriterControllerFactory) x -> controller);
            return new GeneratedLambda(
                lambdaClass,
                lambdaMethod,
                getGeneratedConstructor(lambdaClass),
                getStoredLambdaSharedVariables(expr),
                !requiresLambdaInstance(lambdaMethod),
                lambdaAnalyzer.accessesInstanceMembers(lambdaMethod),
                null
            );
        });
    }

    /**
     * Re-homes a non-capturing lambda's already-prepared {@code static doCall} (types resolved, outer static
     * references qualified) as a {@code private static} synthetic method on the enclosing class, so no per-lambda
     * inner class is generated and the metafactory bootstraps directly against the enclosing class.
     */
    private MethodNode hoistLambdaMethodToEnclosingClass(final MethodNode lambdaMethod) {
        MethodNode implMethod = new MethodNode(nextLambdaImplMethodName(),
                ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, lambdaMethod.getReturnType(),
                lambdaMethod.getParameters(), lambdaMethod.getExceptions(), lambdaMethod.getCode());
        implMethod.setSourcePosition(lambdaMethod);
        implMethod.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
        addGeneratedMethod(controller.getClassNode(), implMethod);
        // Added after the ReturnAdder phase (an inner class would get it via full compilation), so wire the
        // implicit return for an expression-bodied lambda here (idempotent when returns already present).
        new org.codehaus.groovy.classgen.ReturnAdder().visitMethod(implMethod);
        return implMethod;
    }

    /**
     * Hoists a read-only capturing lambda onto the enclosing class as a static method taking the captured
     * values as leading parameters. The lambda-class {@code doCall} already reads its captures from Reference
     * fields (accessedVariable = FieldNode); repointing those accesses to plain value parameters makes codegen
     * load the value directly, and the metafactory then captures the values as factory arguments.
     */
    private GeneratedLambda hoistCapturingLambda(final ClassNode lambdaClass, final MethodNode lambdaMethod, final Parameter[] shared) {
        Parameter[] samParams = lambdaMethod.getParameters();
        Parameter[] valueParams = new Parameter[shared.length];
        Map<String, Parameter> byName = new HashMap<>();
        for (int i = 0; i < shared.length; i++) {
            Parameter p = new Parameter(shared[i].getOriginType(), shared[i].getName());
            valueParams[i] = p;
            byName.put(p.getName(), p);
        }
        Parameter[] implParams = new Parameter[valueParams.length + samParams.length];
        System.arraycopy(valueParams, 0, implParams, 0, valueParams.length);
        System.arraycopy(samParams, 0, implParams, valueParams.length, samParams.length);

        Statement body = lambdaMethod.getCode();
        rebindCapturedFieldsToValueParams(body, byName);

        MethodNode implMethod = new MethodNode(nextLambdaImplMethodName(),
                ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, lambdaMethod.getReturnType(),
                implParams, lambdaMethod.getExceptions(), body);
        implMethod.setSourcePosition(lambdaMethod);
        implMethod.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
        addGeneratedMethod(controller.getClassNode(), implMethod);
        new org.codehaus.groovy.classgen.ReturnAdder().visitMethod(implMethod);

        // nonCapturing=true so no lambda instance is loaded; capturedValueParams drives the dedicated factory.
        return new GeneratedLambda(controller.getClassNode(), implMethod, null, Parameter.EMPTY_ARRAY, true, false, valueParams);
    }

    /** Repoints captured Reference-field accesses in a hoisted body to plain value parameters. */
    private static void rebindCapturedFieldsToValueParams(final Statement body, final Map<String, Parameter> valueParams) {
        body.visit(new CodeVisitorSupport() {
            @Override public void visitVariableExpression(final VariableExpression ve) {
                Parameter p = valueParams.get(ve.getName());
                if (p != null && ve.getAccessedVariable() instanceof FieldNode) {
                    ve.setAccessedVariable(p);
                    ve.setClosureSharedVariable(false);
                }
            }
        });
    }

    /**
     * True if the body assigns (or increments) a captured variable — such a lambda needs the shared Reference,
     * so decline the value-capture hoist. Matched by name (conservative: never value-captures a mutated capture).
     */
    private static boolean writesAnyCapture(final Statement body, final Parameter[] shared) {
        Set<String> names = new HashSet<>();
        for (Parameter p : shared) names.add(p.getName());
        boolean[] written = {false};
        body.visit(new CodeVisitorSupport() {
            @Override public void visitBinaryExpression(final BinaryExpression be) {
                if (Types.isAssignment(be.getOperation().getType())) flagIfCaptured(be.getLeftExpression());
                super.visitBinaryExpression(be);
            }
            @Override public void visitPostfixExpression(final PostfixExpression pe) { flagIfCaptured(pe.getExpression()); super.visitPostfixExpression(pe); }
            @Override public void visitPrefixExpression(final PrefixExpression pe) { flagIfCaptured(pe.getExpression()); super.visitPrefixExpression(pe); }
            private void flagIfCaptured(final Expression e) {
                if (e instanceof VariableExpression && names.contains(((VariableExpression) e).getName())) {
                    written[0] = true;
                }
            }
        });
        return written[0];
    }

    /**
     * Emits the invokedynamic for a hoisted capturing lambda: push the captured values as metafactory factory
     * arguments and bootstrap against the static impl method on the enclosing class (H_INVOKESTATIC).
     */
    private void writeHoistedCapturingLambda(final ClassNode functionalType, final MethodNode abstractMethod, final GeneratedLambda gl, final boolean serializable, final ClassNode[] markers) {
        Parameter[] captured = gl.capturedValueParams();
        for (Parameter p : captured) {
            new VariableExpression(p.getName()).visit(controller.getAcg()); // load captured value (unwraps the Reference)
            controller.getOperandStack().doGroovyCast(p.getType());
        }
        Parameter[] full = gl.lambdaMethod().getParameters();
        Parameter[] samParams = Arrays.copyOfRange(full, captured.length, full.length);
        writeFunctionalInterfaceIndy(
            controller.getMethodVisitor(),
            abstractMethod.getName(),
            createFunctionalInterfaceFactoryDescriptor(functionalType, captured),
            createMethodDescriptor(abstractMethod),
            H_INVOKESTATIC,
            controller.getClassNode(),
            gl.lambdaMethod(),
            samParams,
            serializable,
            markers);
        controller.getOperandStack().replace(functionalType, captured.length);
    }

    /** {@inheritDoc} */
    @Override
    protected ClassNode createClosureClass(final ClosureExpression expression, final int modifiers) {
        return staticTypesClosureWriter.createClosureClass(expression, modifiers);
    }

    private static boolean containsNestedFunction(final Statement code) {
        // LambdaExpression extends ClosureExpression, so this matches both nested closures and lambdas.
        return code != null && AstQuery.from(code).descendants(ClosureExpression.class).any();
    }

    /**
     * Whether non-capturing SAM lambdas are hoisted onto the enclosing class (no generated lambda class).
     * Opt-in via {@code -Dgroovy.target.lambda.hoist=true} while IDE/tooling compatibility is verified;
     * the property is read per lambda so it can be toggled without a JVM restart.
     */
    private static boolean isLambdaHoistEnabled() {
        return SystemUtil.getBooleanSafe("groovy.target.lambda.hoist");
    }

    private int lambdaImplCounter;

    private String nextLambdaImplMethodName() {
        ClassNode enclosing = controller.getClassNode();
        MethodNode enclosingMethod = controller.getMethodNode();
        String owner = (enclosingMethod != null) ? enclosingMethod.getName().replace('<', '_').replace('>', '_') : "init";
        String base = "$lambda$" + owner + "$" + (lambdaImplCounter++);
        String name = base;
        int extra = 0;
        while (!enclosing.getMethods(name).isEmpty()) {
            name = base + "$" + (extra++);
        }
        return name;
    }

    /**
     * Creates the synthetic inner class that backs a statically compiled lambda expression.
     */
    protected ClassNode createLambdaClass(final LambdaExpression expression, final int modifiers, final MethodNode abstractMethod) {
        ClassNode enclosingClass = controller.getClassNode();
        ClassNode outermostClass = controller.getOutermostClass();

        InnerClassNode lambdaClass = new InnerClassNode(enclosingClass, nextLambdaClassName(), modifiers, CLOSURE_TYPE.getPlainNodeReference());
        lambdaClass.setEnclosingMethod(controller.getMethodNode());
        lambdaClass.setSourcePosition(expression);
        lambdaClass.setSynthetic(true);

        if (controller.isInScriptBody()) {
            lambdaClass.setScriptBody(true);
        }
        if (controller.isStaticMethod()
            || enclosingClass.isStaticClass()) {
            lambdaClass.setStaticClass(true);
        }
        if (expression.isSerializable()) {
            addSerialVersionUIDField(lambdaClass);
        }

        MethodNode syntheticLambdaMethodNode = addSyntheticLambdaMethodNode(expression, lambdaClass, abstractMethod);

        Parameter[] localVariableParameters = getStoredLambdaSharedVariables(expression);
        addFieldsForLocalVariables(lambdaClass, localVariableParameters);

        ConstructorNode constructorNode = addConstructor(expression, localVariableParameters, lambdaClass, createBlockStatementForConstructor(expression, outermostClass, enclosingClass));
        constructorNode.putNodeMetaData(LAMBDA_GENERATED_CONSTRUCTOR, Boolean.TRUE);

        syntheticLambdaMethodNode.getCode().visit(new CorrectAccessedVariableVisitor(lambdaClass));

        return lambdaClass;
    }

    private String nextLambdaClassName() {
        ClassNode enclosingClass = controller.getClassNode();
        ClassNode outermostClass = controller.getOutermostClass();
        return enclosingClass.getName() + "$" + controller.getContext().getNextLambdaInnerName(outermostClass, enclosingClass, controller.getMethodNode());
    }

    private MethodNode addSyntheticLambdaMethodNode(final LambdaExpression expression, final ClassNode lambdaClass, final MethodNode abstractMethod) {
        Parameter[] parametersWithExactType = createParametersWithExactType(expression, abstractMethod);
        Parameter[] localVariableParameters = getLambdaSharedVariables(expression);
        removeInitialValues(localVariableParameters);

        expression.putNodeMetaData(LAMBDA_SHARED_VARIABLES, localVariableParameters);

        MethodNode doCallMethod = lambdaClass.addMethod(
            DO_CALL,
            ACC_PUBLIC,
            abstractMethod.getReturnType(),
            parametersWithExactType.clone(),
            ClassNode.EMPTY_ARRAY,
            expression.getCode()
        );
        doCallMethod.setSourcePosition(expression);
        if (lambdaAnalyzer.isNonCapturing(doCallMethod, localVariableParameters)) {
            lambdaAnalyzer.qualifyOuterStaticMemberReferences(doCallMethod);
            doCallMethod.setModifiers(doCallMethod.getModifiers() | ACC_STATIC);
        }
        return doCallMethod;
    }

    private Parameter[] createParametersWithExactType(final LambdaExpression expression, final MethodNode abstractMethod) {
        Parameter[] targetParameters = abstractMethod.getParameters();
        Parameter[] lambdaParameters = getParametersSafe(expression);
        ClassNode[] lambdaParamTypes = expression.getNodeMetaData(CLOSURE_ARGUMENTS);
        for (int i = 0, n = lambdaParameters.length; i < n; i += 1) {
            final Parameter lambdaParameter = lambdaParameters[i];
            ClassNode resolvedType = convertParameterType(targetParameters[i].getType(), lambdaParameter.getType(), lambdaParamTypes[i]);
            lambdaParameter.setType(resolvedType);
        }
        return lambdaParameters;
    }

    private static boolean requiresLambdaInstance(final MethodNode lambdaMethod) {
        return 0 == (lambdaMethod.getModifiers() & ACC_STATIC);
    }

    private MethodNode addDeserializeLambdaMethodForLambdaExpression(final LambdaExpression expression, final GeneratedLambda generatedLambda) {
        ClassNode enclosingClass = controller.getClassNode();
        Statement code;
        if (generatedLambda.nonCapturing()) {
            code = block(returnS(expression));
        } else {
            code = block(
                new BytecodeSequence(new BytecodeInstruction() {
                    /**
                     * Restores the captured lambda receiver from the serialized form.
                     */
                    @Override
                    public void visit(final MethodVisitor mv) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitInsn(ICONST_0);
                        mv.visitMethodInsn(
                            INVOKEVIRTUAL,
                            "java/lang/invoke/SerializedLambda",
                            "getCapturedArg",
                            "(I)Ljava/lang/Object;",
                            false);
                        mv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(generatedLambda.lambdaClass));
                        OperandStack operandStack = controller.getOperandStack();
                        operandStack.push(generatedLambda.lambdaClass);
                    }
                }),
                returnS(expression)
            );
        }

        MethodNode deserializeLambdaMethod = enclosingClass.addSyntheticMethod(
            createDeserializeLambdaMethodName(generatedLambda.lambdaClass),
            ACC_PRIVATE | ACC_STATIC,
            OBJECT_TYPE,
            createDeserializeMethodParameters(),
            ClassNode.EMPTY_ARRAY,
            code);
        if (generatedLambda.isCapturing()) {
            // The deserialize helper preloads the captured receiver before it reuses the original lambda expression.
            deserializeLambdaMethod.putNodeMetaData(LAMBDA_PRELOADED_RECEIVER, generatedLambda.lambdaClass);
        }
        return deserializeLambdaMethod;
    }

    private static String createDeserializeLambdaMethodName(final ClassNode lambdaClass) {
        return "$deserializeLambda_" + lambdaClass.getName().replace('.', '$') + "$";
    }

    private static Parameter[] getStoredLambdaSharedVariables(final LambdaExpression expression) {
        Parameter[] sharedVariables = expression.getNodeMetaData(LAMBDA_SHARED_VARIABLES);
        if (sharedVariables == null) {
            throw new GroovyBugError("Failed to find shared variables for lambda expression");
        }
        return sharedVariables;
    }

    /**
     * Cached lambda generation result reused across the emitted indy call and
     * any synthetic deserialization helpers for the same expression.
     */
    private record GeneratedLambda(ClassNode lambdaClass, MethodNode lambdaMethod, ConstructorNode constructor,
                                   Parameter[] sharedVariables, boolean nonCapturing,
                                   boolean accessingInstanceMembers, Parameter[] capturedValueParams) {

        private boolean isCapturing() {
            return !nonCapturing;
        }

        /** Non-null when the lambda body is hoisted onto the enclosing class as a static method that takes the
         *  captured values as leading parameters, and the metafactory captures those values directly. */
        private boolean isHoistedCapturing() {
            return capturedValueParams != null;
        }

        private int getImplMethodKind() {
            return nonCapturing ? H_INVOKESTATIC : H_INVOKEVIRTUAL;
        }
    }

    private static final String DO_CALL = "doCall";
    private final Map<LambdaExpression, GeneratedLambda> generatedLambdas = new HashMap<>();
    private final StaticTypesClosureWriter staticTypesClosureWriter;
    private final StaticTypesLambdaAnalyzer lambdaAnalyzer;
}

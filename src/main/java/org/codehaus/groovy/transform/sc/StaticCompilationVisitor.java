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
package org.codehaus.groovy.transform.sc;

import groovy.lang.Reference;
import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.classgen.asm.MopWriter;
import org.codehaus.groovy.classgen.asm.WriterControllerFactory;
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationMopWriter;
import org.codehaus.groovy.control.CompilationUnit.IPrimaryClassNodeOperation;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.hasAnnotation;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.attrX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorThisS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.addMethodGenerics;
import static org.codehaus.groovy.ast.tools.GenericsUtils.applyGenericsContextToPlaceHolders;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpecRecurse;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.extractSuperClassGenerics;
import static org.codehaus.groovy.classgen.Verifier.DEFAULT_PARAMETER_GENERATED;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.BINARY_EXP_TARGET;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.COMPONENT_TYPE;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.DYNAMIC_OUTER_NODE_CALLBACK;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PRIVATE_BRIDGE_METHODS;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PRIVATE_FIELDS_ACCESSORS;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PRIVATE_FIELDS_MUTATORS;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PROPERTY_OWNER;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.RECEIVER_OF_DYNAMIC_PROPERTY;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.STATIC_COMPILE_NODE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DYNAMIC_RESOLUTION;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INITIAL_EXPRESSION;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PARAMETER_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PV_FIELDS_ACCESS;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PV_FIELDS_MUTATION;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PV_METHODS_ACCESS;
import static org.codehaus.groovy.transform.trait.TraitASTTransformation.POST_TYPECHECKING_REPLACEMENT;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * This visitor is responsible for amending the AST with static compilation metadata or transform the AST so that
 * a class or a method can be statically compiled. It may also throw errors specific to static compilation which
 * are not considered as an error at the type check pass. For example, usage of spread operator is not allowed
 * in statically compiled portions of code, while it may be statically checked.
 *
 * Static compilation relies on static type checking, which explains why this visitor extends the type checker
 * visitor.
 */
public class StaticCompilationVisitor extends StaticTypeCheckingVisitor {

    public static final ClassNode TYPECHECKED_CLASSNODE = ClassHelper.make(TypeChecked.class);
    public static final ClassNode COMPILESTATIC_CLASSNODE = ClassHelper.make(CompileStatic.class);

    public static final ClassNode  ARRAYLIST_CLASSNODE = ClassHelper.makeWithoutCaching(ArrayList.class);
    public static final MethodNode ARRAYLIST_ADD_METHOD = ARRAYLIST_CLASSNODE.getDeclaredMethod("add", new Parameter[]{new Parameter(ClassHelper.OBJECT_TYPE, "o")});
    public static final MethodNode ARRAYLIST_CONSTRUCTOR = ARRAYLIST_CLASSNODE.getDeclaredConstructor(Parameter.EMPTY_ARRAY);

    public StaticCompilationVisitor(final SourceUnit unit, final ClassNode node) {
        super(unit, node);
    }

    @Override
    protected ClassNode[] getTypeCheckingAnnotations() {
        return new ClassNode[]{TYPECHECKED_CLASSNODE, COMPILESTATIC_CLASSNODE};
    }

    @Override
    public void visitClass(final ClassNode node) {
        boolean skip = shouldSkipClassNode(node);
        if (!skip && !anyMethodSkip(node)) {
            node.putNodeMetaData(MopWriter.Factory.class, StaticCompilationMopWriter.FACTORY);
        }

        node.getInnerClasses().forEachRemaining(innerClass -> {
            boolean isSC = !isSkipMode(innerClass) && (isStaticallyCompiled(node) || hasAnnotation(innerClass, COMPILESTATIC_CLASSNODE));
            // GROOVY-10238: @CompileDynamic outer class, @CompileStatic inner class ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            innerClass.putNodeMetaData(STATIC_COMPILE_NODE, isSC);
            if (isSC && !anyMethodSkip(innerClass)) {
                innerClass.putNodeMetaData(MopWriter.Factory.class, StaticCompilationMopWriter.FACTORY);
            }
            innerClass.putNodeMetaData(WriterControllerFactory.class, node.getNodeMetaData(WriterControllerFactory.class));
        });

        super.visitClass(node);

        if (isStaticallyCompiled(node)) {
            ClassNode outerClass = node.getOuterClass();
            addDynamicOuterClassAccessorsCallback(outerClass);
        }
        addPrivateFieldAndMethodAccessors(node); // includes inner types
    }

    private boolean anyMethodSkip(final ClassNode node) {
        for (MethodNode methodNode : node.getMethods()) {
            if (isSkipMode(methodNode)) return true;
        }
        return false;
    }

    private void visitConstructorOrMethod(final MethodNode node) {
        boolean isSkipped = isSkipMode(node); // @CompileDynamic
        boolean isSC = !isSkipped && isStaticallyCompiled(node);
        if (isSkipped) {
            node.putNodeMetaData(STATIC_COMPILE_NODE, Boolean.FALSE);
        }
        if (node instanceof ConstructorNode) {
            super.visitConstructor((ConstructorNode) node);
            ClassNode declaringClass = node.getDeclaringClass();
            if (isSC && !isStaticallyCompiled(declaringClass)) {
                // In a constructor that is statically compiled within a class that is
                // not, it may happen that init code from object initializers, fields or
                // properties is added into the constructor code. The backend assumes a
                // purely static constructor, so it may fail if it encounters dynamic
                // code here. Thus we make this kind of code fail.
                if (!declaringClass.getFields().isEmpty()
                        || !declaringClass.getProperties().isEmpty()
                        || !declaringClass.getObjectInitializerStatements().isEmpty()) {
                    addStaticTypeError("Cannot statically compile constructor implicitly including non-static elements from fields, properties or initializers", node);
                }
            }
        } else {
            super.visitMethod(node);
        }
        if (isSC) {
            ClassNode declaringClass = node.getDeclaringClass();
            addDynamicOuterClassAccessorsCallback(declaringClass);
        }
    }

    @Override
    public void visitConstructor(final ConstructorNode node) {
        visitConstructorOrMethod(node);
    }

    @Override
    public void visitMethod(final MethodNode node) {
        visitConstructorOrMethod(node);
    }

    private AnnotatedNode getEnclosingDeclaration() {
        ClassNode  cn = typeCheckingContext.getEnclosingClassNode();
        MethodNode mn = typeCheckingContext.getEnclosingMethod();
        if (cn != null && cn.getEnclosingMethod() == mn) {
            return cn;
        } else {
            return mn;
        }
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        super.visitMethodCallExpression(call);

        if (!isStaticallyCompiled(getEnclosingDeclaration())) return;

        MethodNode target = call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (target != null) {
            call.setMethodTarget(target);
            memorizeInitialExpressions(target);
        }

        if (call.getMethodTarget() == null && call.getLineNumber() > 0) {
            addError("Target method for method call expression hasn't been set", call);
        }
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);

        if (call.isUsingAnonymousInnerClass() && call.getType().getNodeMetaData(StaticTypeCheckingVisitor.class) != null) {
            ClassNode anonType = call.getType();
            anonType.putNodeMetaData(STATIC_COMPILE_NODE, anonType.getEnclosingMethod().getNodeMetaData(STATIC_COMPILE_NODE));
            anonType.putNodeMetaData(WriterControllerFactory.class, anonType.getOuterClass().getNodeMetaData(WriterControllerFactory.class));
        }

        if (!isStaticallyCompiled(getEnclosingDeclaration())) return;

        MethodNode target = call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (target == null && call.getLineNumber() > 0) {
            addError("Target constructor for constructor call expression hasn't been set", call);
        } else if (target == null) { assert call.isSpecialCall(); // try to find target constructor
            ClassNode enclosingClass = typeCheckingContext.getEnclosingMethod().getDeclaringClass();
            ClassNode[] args = getArgumentTypes(InvocationWriter.makeArgumentList(call.getArguments()));
            target = findMethodOrFail(call, call.isSuperCall() ? enclosingClass.getSuperClass() : enclosingClass, "<init>", args);
            call.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, target);
        }
        if (target != null) {
            memorizeInitialExpressions(target);
        }
    }

    @Override
    public void visitForLoop(final ForStatement statement) {
        super.visitForLoop(statement);
        var collectionExpression = statement.getCollectionExpression();
        if (!(collectionExpression instanceof ClosureListExpression)) {
            var valueVariable = statement.getValueVariable();
            if (valueVariable.isDynamicTyped()) { // GROOVY-8169
                ClassNode inferredType = getType(valueVariable);
                valueVariable.setType(inferredType); // GROOVY-5640, GROOVY-5641
            }
        }
    }

    @Override
    public void visitLambdaExpression(final LambdaExpression expression) {
        super.visitLambdaExpression(expression);
        // GROOVY-11256: static compiler uses lambda factory not proxy generator
        if (ClassHelper.isFunctionalInterface(expression.getNodeMetaData(PARAMETER_TYPE))) {
            expression.removeNodeMetaData(POST_TYPECHECKING_REPLACEMENT); // for trait: lambda.rehydrate($self,$self,$self)
        }
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        super.visitPropertyExpression(expression);
        Object dynamic = expression.getNodeMetaData(DYNAMIC_RESOLUTION);
        if (dynamic != null) {
            expression.getObjectExpression().putNodeMetaData(RECEIVER_OF_DYNAMIC_PROPERTY, dynamic);
        }
    }

    @Override
    protected boolean existsProperty(final PropertyExpression pexp, final boolean checkForReadOnly, final ClassCodeVisitorSupport visitor) {
        Expression objectExpression = pexp.getObjectExpression();
        ClassNode objectExpressionType = getType(objectExpression);
        Reference<ClassNode> rType = new Reference<>(objectExpressionType);
        ClassCodeVisitorSupport receiverMemoizer = new ClassCodeVisitorSupport() {
            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }

            @Override
            public void visitField(final FieldNode node) {
                if (visitor != null) visitor.visitField(node);
                ClassNode declaringClass = node.getDeclaringClass();
                if (declaringClass != null) {
                    if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(declaringClass, ClassHelper.LIST_TYPE)) {
                        boolean spread = declaringClass.getDeclaredField(node.getName()) != node;
                        pexp.setSpreadSafe(spread);
                    }
                    rType.set(declaringClass);
                }
            }

            @Override
            public void visitMethod(final MethodNode node) {
                if (visitor != null) visitor.visitMethod(node);
                ClassNode declaringClass = node.getDeclaringClass();
                if (declaringClass != null) {
                    if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(declaringClass, ClassHelper.LIST_TYPE)) {
                        List<MethodNode> properties = declaringClass.getDeclaredMethods(node.getName());
                        boolean spread = true;
                        for (MethodNode mn : properties) {
                            if (node == mn) {
                                spread = false;
                                break;
                            }
                        }
                        // it's no real property but a property of the component
                        pexp.setSpreadSafe(spread);
                    }
                    rType.set(declaringClass);
                }
            }

            @Override
            public void visitProperty(final PropertyNode node) {
                if (visitor != null) visitor.visitProperty(node);
                ClassNode declaringClass = node.getDeclaringClass();
                if (declaringClass != null) {
                    if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(declaringClass, ClassHelper.LIST_TYPE)) {
                        List<PropertyNode> properties = declaringClass.getProperties();
                        boolean spread = true;
                        for (PropertyNode propertyNode : properties) {
                            if (propertyNode == node) {
                                spread = false;
                                break;
                            }
                        }
                        // it's no real property but a property of the component
                        pexp.setSpreadSafe(spread);
                    }
                    rType.set(declaringClass);
                }
            }
        };

        boolean exists = super.existsProperty(pexp, checkForReadOnly, receiverMemoizer);
        if (exists) {
            objectExpressionType = rType.get();
            if (objectExpression.getNodeMetaData(PROPERTY_OWNER) == null) {
                objectExpression.putNodeMetaData(PROPERTY_OWNER, objectExpressionType);
            }
            if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(objectExpressionType, ClassHelper.LIST_TYPE)) {
                objectExpression.putNodeMetaData(COMPONENT_TYPE, inferComponentType(objectExpressionType, ClassHelper.int_TYPE));
            }
        }
        return exists;
    }

    @Override
    protected MethodNode findMethodOrFail(final Expression expr, final ClassNode receiver, final String name, final ClassNode... args) {
        MethodNode methodNode = super.findMethodOrFail(expr, receiver, name, args);
        if (expr instanceof BinaryExpression && methodNode != null) {
            expr.putNodeMetaData(BINARY_EXP_TARGET, new Object[]{methodNode, name});
        }
        return methodNode;
    }

    //--------------------------------------------------------------------------

    public static boolean isStaticallyCompiled(final AnnotatedNode node) {
        if (node != null && node.getNodeMetaData(STATIC_COMPILE_NODE) != null) {
            return Boolean.TRUE.equals(node.getNodeMetaData(STATIC_COMPILE_NODE));
        }
        if (node instanceof MethodNode) {
            // GROOVY-6851, GROOVY-9151, GROOVY-10104
            if (!Boolean.TRUE.equals(node.getNodeMetaData(DEFAULT_PARAMETER_GENERATED))) {
                return isStaticallyCompiled(node.getDeclaringClass());
            }
        } else if (node instanceof ClassNode) {
            return isStaticallyCompiled(((ClassNode) node).getOuterClass());
        }
        return false;
    }

    private static void addDynamicOuterClassAccessorsCallback(final ClassNode outer) {
        if (outer != null) {
            if (!isStaticallyCompiled(outer) && outer.getNodeMetaData(DYNAMIC_OUTER_NODE_CALLBACK) == null) {
                outer.putNodeMetaData(DYNAMIC_OUTER_NODE_CALLBACK, (IPrimaryClassNodeOperation) (source, context, classNode) -> {
                    if (classNode == outer) {
                        addPrivateBridgeMethods(classNode);
                        addPrivateFieldsAccessors(classNode);
                    }
                });
            }
            // GROOVY-9328: apply to outer classes
            addDynamicOuterClassAccessorsCallback(outer.getOuterClass());
        }
    }

    private static void addPrivateFieldAndMethodAccessors(final ClassNode node) {
        addPrivateBridgeMethods(node);
        addPrivateFieldsAccessors(node);
        for (Iterator<InnerClassNode> it = node.getInnerClasses(); it.hasNext(); ) {
            addPrivateFieldAndMethodAccessors(it.next());
        }
    }

    /**
     * Adds special accessors and mutators for private fields so that inner classes can get/set them.
     */
    @Deprecated(since = "5.0.0")
    private static void addPrivateFieldsAccessors(final ClassNode node) {
        Map<String, MethodNode> privateFieldAccessors = node.getNodeMetaData(PRIVATE_FIELDS_ACCESSORS);
        Map<String, MethodNode> privateFieldMutators = node.getNodeMetaData(PRIVATE_FIELDS_MUTATORS);
        if (privateFieldAccessors != null || privateFieldMutators != null) {
            // already added
            return;
        }
        Set<ASTNode> accessedFields = node.getNodeMetaData(PV_FIELDS_ACCESS);
        Set<ASTNode> mutatedFields = node.getNodeMetaData(PV_FIELDS_MUTATION);
        if (accessedFields == null && mutatedFields == null) return;
        // GROOVY-9385: mutation includes access in case of compound assignment or pre/post-increment/decrement
        if (mutatedFields != null) {
            accessedFields = new HashSet<>(Optional.ofNullable(accessedFields).orElseGet(Collections::emptySet));
            accessedFields.addAll(mutatedFields);
        }

        int acc = -1;
        privateFieldAccessors = (accessedFields != null ? new HashMap<>() : null);
        privateFieldMutators = (mutatedFields != null ? new HashMap<>() : null);
        for (FieldNode fieldNode : node.getFields()) {
            boolean generateAccessor = accessedFields != null && accessedFields.contains(fieldNode);
            boolean generateMutator = mutatedFields != null && mutatedFields.contains(fieldNode);
            if (generateAccessor) {
                acc += 1;
                Parameter param = new Parameter(node.getPlainNodeReference(), "$that");
                Expression receiver = fieldNode.isStatic() ? classX(node) : varX(param);
                Statement body = returnS(attrX(receiver, constX(fieldNode.getName())));
                MethodNode accessor = node.addMethod("pfaccess$" + acc, ACC_STATIC | ACC_SYNTHETIC, fieldNode.getOriginType(), new Parameter[]{param}, ClassNode.EMPTY_ARRAY, body);
                accessor.setNodeMetaData(STATIC_COMPILE_NODE, Boolean.TRUE);
                privateFieldAccessors.put(fieldNode.getName(), accessor);
            }
            if (generateMutator) {
                // increment acc if it hasn't been incremented in the current iteration
                if (!generateAccessor) acc += 1;
                Parameter param = new Parameter(node.getPlainNodeReference(), "$that");
                Expression receiver = fieldNode.isStatic() ? classX(node) : varX(param);
                Parameter value = new Parameter(fieldNode.getOriginType(), "$value");
                Statement body = assignS(attrX(receiver, constX(fieldNode.getName())), varX(value));
                MethodNode mutator = node.addMethod("pfaccess$0" + acc, ACC_STATIC | ACC_SYNTHETIC, fieldNode.getOriginType(), new Parameter[]{param, value}, ClassNode.EMPTY_ARRAY, body);
                mutator.setNodeMetaData(STATIC_COMPILE_NODE, Boolean.TRUE);
                privateFieldMutators.put(fieldNode.getName(), mutator);
            }
        }
        if (privateFieldAccessors != null) {
            node.setNodeMetaData(PRIVATE_FIELDS_ACCESSORS, privateFieldAccessors);
        }
        if (privateFieldMutators != null) {
            node.setNodeMetaData(PRIVATE_FIELDS_MUTATORS, privateFieldMutators);
        }
    }

    /**
     * Adds bridge methods for private or protected methods of a class so that
     * any nestmate is capable of calling them. It does basically the same job
     * as access$000 like methods in Java.
     */
    private static void addPrivateBridgeMethods(final ClassNode node) {
        Set<ASTNode> accessedMethods = node.getNodeMetaData(PV_METHODS_ACCESS);
        if (accessedMethods == null) return;
        List<MethodNode> methods = new ArrayList<>(node.getAllDeclaredMethods());
        methods.addAll(node.getDeclaredConstructors());
        Map<MethodNode, MethodNode> privateBridgeMethods = node.getNodeMetaData(PRIVATE_BRIDGE_METHODS);
        if (privateBridgeMethods != null) {
            // bridge methods already added
            return;
        }
        privateBridgeMethods = new HashMap<>();
        int i = -1;
        for (MethodNode method : methods) {
            if (accessedMethods.contains(method)) {
                i += 1;
                ClassNode declaringClass = method.getDeclaringClass();
                Map<String, ClassNode> genericsSpec = createGenericsSpec(node);
                genericsSpec = addMethodGenerics(method, genericsSpec);
                extractSuperClassGenerics(node, declaringClass, genericsSpec);
                List<String> methodSpecificGenerics = methodSpecificGenerics(method);
                Parameter[] methodParameters = method.getParameters();
                Parameter[] newParams = new Parameter[methodParameters.length + 1];
                for (int j = 1; j < newParams.length; j += 1) {
                    Parameter orig = methodParameters[j - 1];
                    newParams[j] = new Parameter(
                            correctToGenericsSpecRecurse(genericsSpec, orig.getOriginType(), methodSpecificGenerics),
                            orig.getName()
                    );
                }
                Expression arguments;
                if (methodParameters.length == 0) {
                    arguments = ArgumentListExpression.EMPTY_ARGUMENTS;
                } else {
                    List<Expression> args = new ArrayList<>();
                    for (Parameter parameter : methodParameters) {
                        args.add(varX(parameter));
                    }
                    arguments = args(args);
                }

                MethodNode bridge;
                if (method instanceof ConstructorNode) {
                    // create constructor with a nested class as the first parameter, creating one if necessary
                    ClassNode thatType = null;
                    Iterator<InnerClassNode> innerClasses = node.getInnerClasses();
                    if (innerClasses.hasNext()) {
                        thatType = innerClasses.next();
                    } else {
                        thatType = new InnerClassNode(node.redirect(), node.getName() + "$1", ACC_STATIC | ACC_SYNTHETIC, ClassHelper.OBJECT_TYPE);
                        node.getModule().addClass(thatType);
                    }
                    newParams[0] = new Parameter(thatType.getPlainNodeReference(), "$that");
                    Statement body = ctorThisS(arguments);

                    bridge = node.addConstructor(ACC_SYNTHETIC, newParams, ClassNode.EMPTY_ARRAY, body);
                } else {
                    newParams[0] = new Parameter(node.getPlainNodeReference(), "$that");
                    Expression receiver = method.isStatic() ? classX(node) : varX(newParams[0]);
                    MethodCallExpression call = callX(receiver, method.getName(), arguments);
                    call.setMethodTarget(method);
                    ExpressionStatement body = new ExpressionStatement(call);

                    bridge = node.addMethod(
                            "access$" + i,
                            ACC_STATIC | ACC_SYNTHETIC,
                            correctToGenericsSpecRecurse(genericsSpec, method.getReturnType(), methodSpecificGenerics),
                            newParams,
                            method.getExceptions(),
                            body);
                }
                GenericsType[] origGenericsTypes = method.getGenericsTypes();
                if (origGenericsTypes != null) {
                    bridge.setGenericsTypes(applyGenericsContextToPlaceHolders(genericsSpec, origGenericsTypes));
                }
                bridge.setNodeMetaData(STATIC_COMPILE_NODE, Boolean.TRUE);
                privateBridgeMethods.put(method, bridge);
            }
        }
        if (!privateBridgeMethods.isEmpty()) {
            node.setNodeMetaData(PRIVATE_BRIDGE_METHODS, privateBridgeMethods);
        }
    }

    private static List<String> methodSpecificGenerics(final MethodNode method) {
        List<String> genericTypeNames = new ArrayList<>();
        GenericsType[] genericsTypes = method.getGenericsTypes();
        if (genericsTypes != null) {
            for (GenericsType gt : genericsTypes) {
                genericTypeNames.add(gt.getName());
            }
        }
        return genericTypeNames;
    }

    private static void memorizeInitialExpressions(final MethodNode node) {
        // add node metadata for default parameters because they are erased by the Verifier
        if (node.getParameters() != null) {
            for (Parameter parameter : node.getParameters()) {
                parameter.putNodeMetaData(INITIAL_EXPRESSION, parameter.getInitialExpression());
            }
        }
    }
}

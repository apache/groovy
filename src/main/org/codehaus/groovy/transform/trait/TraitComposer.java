/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform.trait;

import groovy.transform.CompileStatic;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpecRecurse;

/**
 * This class contains a static utility method {@link #doExtendTraits(org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.control.SourceUnit, org.codehaus.groovy.control.CompilationUnit)}
 * aimed at generating code for a classnode implementing a trait.
 *
 * @author Cédric Champeau
 * @since 2.3.0
 */
public abstract class TraitComposer {
    /**
     * This comparator is used to make sure that generated direct getters appear first in the list of method
     * nodes.
     */
    private static final Comparator<MethodNode> GETTER_FIRST_COMPARATOR = new Comparator<MethodNode>() {
        public int compare(final MethodNode o1, final MethodNode o2) {
            if (o1.getName().endsWith(Traits.DIRECT_GETTER_SUFFIX)) return -1;
            return 1;
        }
    };
    public static final ClassNode COMPILESTATIC_CLASSNODE = ClassHelper.make(CompileStatic.class);

    /**
     * Given a class node, if this class node implements a trait, then generate all the appropriate
     * code which delegates calls to the trait. It is safe to call this method on a class node which
     * does not implement a trait.
     * @param cNode a class node
     * @param unit the source unit
     */
    public static void doExtendTraits(final ClassNode cNode, final SourceUnit unit, final CompilationUnit cu) {
        if (cNode.isInterface()) return;
        boolean isItselfTrait = Traits.isTrait(cNode);
        SuperCallTraitTransformer superCallTransformer = new SuperCallTraitTransformer(unit);
        if (isItselfTrait) {
            checkTraitAllowed(cNode, unit);
            return;
        }
        if (!cNode.getNameWithoutPackage().endsWith(Traits.TRAIT_HELPER)) {
            List<ClassNode> traits = findTraits(cNode);
            for (ClassNode trait : traits) {
                TraitHelpersTuple helpers = Traits.findHelpers(trait);
                applyTrait(trait, cNode, helpers);
                superCallTransformer.visitClass(cNode);
                if (unit!=null) {
                    ASTTransformationCollectorCodeVisitor collector = new ASTTransformationCollectorCodeVisitor(unit, cu.getTransformLoader());
                    collector.visitClass(cNode);
                }
            }
        }
    }

    private static List<ClassNode> findTraits(ClassNode cNode) {
        LinkedHashSet<ClassNode> interfaces = new LinkedHashSet<ClassNode>();
        Traits.collectAllInterfacesReverseOrder(cNode, interfaces);
        List<ClassNode> traits = new LinkedList<ClassNode>();
        for (ClassNode candidate : interfaces) {
            if (Traits.isAnnotatedWithTrait(candidate)) {
                traits.add(candidate);
            }
        }
        return traits;
    }

    private static void checkTraitAllowed(final ClassNode bottomTrait, final SourceUnit unit) {
        ClassNode superClass = bottomTrait.getSuperClass();
        if (superClass==null || ClassHelper.OBJECT_TYPE.equals(superClass)) return;
        if (!Traits.isTrait(superClass)) {
            unit.addError(new SyntaxException("A trait can only inherit from another trait", superClass.getLineNumber(), superClass.getColumnNumber()));
        }
    }

    private static void applyTrait(final ClassNode trait, final ClassNode cNode, final TraitHelpersTuple helpers) {
        ClassNode helperClassNode = helpers.getHelper();
        ClassNode fieldHelperClassNode = helpers.getFieldHelper();
        Map genericsSpec = GenericsUtils.createGenericsSpec(cNode, new HashMap());
        genericsSpec = GenericsUtils.createGenericsSpec(trait, genericsSpec);

        for (MethodNode methodNode : helperClassNode.getAllDeclaredMethods()) {
            String name = methodNode.getName();
            Parameter[] helperMethodParams = methodNode.getParameters();
            boolean isAbstract = methodNode.isAbstract();
            if (!isAbstract && helperMethodParams.length > 0 && ((methodNode.getModifiers() & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) && !name.contains("$")) {
                ArgumentListExpression argList = new ArgumentListExpression();
                argList.addExpression(new VariableExpression("this"));
                Parameter[] origParams = new Parameter[helperMethodParams.length - 1];
                Parameter[] params = new Parameter[helperMethodParams.length - 1];
                for (int i = 1; i < helperMethodParams.length; i++) {
                    Parameter parameter = helperMethodParams[i];
                    ClassNode originType = parameter.getOriginType();
                    ClassNode fixedType = correctToGenericsSpecRecurse(genericsSpec, originType);
                    Parameter newParam = new Parameter(fixedType, "arg" + i);
                    List<AnnotationNode> copied = new LinkedList<AnnotationNode>();
                    List<AnnotationNode> notCopied = new LinkedList<AnnotationNode>();
                    GeneralUtils.copyAnnotatedNodeAnnotations(parameter, copied, notCopied);
                    newParam.addAnnotations(copied);
                    params[i - 1] = newParam;
                    origParams[i-1] = parameter;
                    argList.addExpression(new VariableExpression(params[i - 1]));
                }
                createForwarderMethod(trait, cNode, methodNode, helperClassNode, genericsSpec, helperMethodParams, origParams, params, argList);
            }
        }
        cNode.addObjectInitializerStatements(new ExpressionStatement(
                new MethodCallExpression(
                        new ClassExpression(helperClassNode),
                        Traits.INIT_METHOD,
                        new ArgumentListExpression(new VariableExpression("this")))
        ));
        cNode.addStaticInitializerStatements(Collections.<Statement>singletonList(new ExpressionStatement(
                new MethodCallExpression(
                        new ClassExpression(helperClassNode),
                        Traits.STATIC_INIT_METHOD,
                        new ArgumentListExpression(new VariableExpression("this")))
        )), false);
        if (fieldHelperClassNode != null) {
            // we should implement the field helper interface too
            cNode.addInterface(fieldHelperClassNode);
            // implementation of methods
            List<MethodNode> declaredMethods = fieldHelperClassNode.getAllDeclaredMethods();
            Collections.sort(declaredMethods, GETTER_FIRST_COMPARATOR);
            for (MethodNode methodNode : declaredMethods) {
                String fieldName = methodNode.getName();
                if (fieldName.endsWith(Traits.DIRECT_GETTER_SUFFIX) || fieldName.endsWith(Traits.DIRECT_SETTER_SUFFIX)) {
                    int suffixIdx = fieldName.lastIndexOf("$");
                    fieldName = fieldName.substring(0, suffixIdx);
                    String operation = methodNode.getName().substring(suffixIdx + 1);
                    boolean getter = "get".equals(operation);
                    ClassNode returnType = correctToGenericsSpecRecurse(genericsSpec, methodNode.getReturnType());
                    int isStatic = 0;
                    boolean publicField = true;
                    FieldNode helperField = fieldHelperClassNode.getField(Traits.FIELD_PREFIX + Traits.PUBLIC_FIELD_PREFIX + fieldName);
                    if (helperField==null) {
                        publicField = false;
                        helperField = fieldHelperClassNode.getField(Traits.FIELD_PREFIX + Traits.PRIVATE_FIELD_PREFIX + fieldName);
                    }
                    if (helperField==null) {
                        publicField = true;
                        // try to find a static one
                        helperField = fieldHelperClassNode.getField(Traits.STATIC_FIELD_PREFIX+Traits.PUBLIC_FIELD_PREFIX+fieldName);
                        if (helperField==null) {
                            publicField = false;
                            helperField = fieldHelperClassNode.getField(Traits.STATIC_FIELD_PREFIX+Traits.PRIVATE_FIELD_PREFIX +fieldName);
                        }
                        isStatic = Opcodes.ACC_STATIC;
                    }
                    if (getter) {
                        // add field
                        if (helperField!=null) {
                            List<AnnotationNode> copied = new LinkedList<AnnotationNode>();
                            List<AnnotationNode> notCopied = new LinkedList<AnnotationNode>();
                            GeneralUtils.copyAnnotatedNodeAnnotations(helperField, copied, notCopied);
                            FieldNode fieldNode = cNode.addField(fieldName, (publicField?Opcodes.ACC_PUBLIC:Opcodes.ACC_PRIVATE) | isStatic, returnType, null);
                            fieldNode.addAnnotations(copied);
                        }
                    }
                    Parameter[] newParams;
                    if (getter) {
                        newParams = Parameter.EMPTY_ARRAY;
                    } else {
                        ClassNode originType = methodNode.getParameters()[0].getOriginType();
                        ClassNode fixedType = originType.isGenericsPlaceHolder()?ClassHelper.OBJECT_TYPE:correctToGenericsSpecRecurse(genericsSpec, originType);
                        newParams = new Parameter[]{new Parameter(fixedType, "val")};
                    }

                    Expression fieldExpr = new VariableExpression(cNode.getField(fieldName));
                    Statement body =
                            getter ? new ReturnStatement(fieldExpr) :
                                    new ExpressionStatement(
                                            new BinaryExpression(
                                                    fieldExpr,
                                                    Token.newSymbol(Types.EQUAL, 0, 0),
                                                    new VariableExpression(newParams[0])
                                            )
                                    );
                    MethodNode impl = new MethodNode(
                            methodNode.getName(),
                            Opcodes.ACC_PUBLIC | isStatic,
                            returnType,
                            newParams,
                            ClassNode.EMPTY_ARRAY,
                            body
                    );
                    impl.addAnnotation(new AnnotationNode(COMPILESTATIC_CLASSNODE));
                    cNode.addMethod(impl);
                }
            }
        }
    }

    private static void createForwarderMethod(
            ClassNode trait,
            ClassNode targetNode,
            MethodNode helperMethod,
            ClassNode helperClassNode,
            Map genericsSpec,
            Parameter[] helperMethodParams,
            Parameter[] traitMethodParams,
            Parameter[] forwarderParams,
            ArgumentListExpression helperMethodArgList) {
        ClassNode[] exceptionNodes = copyExceptions(helperMethod.getExceptions());
        MethodCallExpression mce = new MethodCallExpression(
                new ClassExpression(helperClassNode),
                helperMethod.getName(),
                helperMethodArgList
        );
        mce.setImplicitThis(false);
        ClassNode fixedReturnType = correctToGenericsSpecRecurse(genericsSpec, helperMethod.getReturnType());
        Expression forwardExpression = genericsSpec.isEmpty()?mce:new CastExpression(fixedReturnType,mce);
        int access = helperMethod.getModifiers();
        if (!helperMethodParams[0].getOriginType().equals(ClassHelper.CLASS_Type)) {
            // we could rely on the first parameter name ($static$self) but that information is not
            // guaranteed to be always present
            access = access ^ Opcodes.ACC_STATIC;
        }
        MethodNode forwarder = new MethodNode(
                helperMethod.getName(),
                access,
                fixedReturnType,
                forwarderParams,
                exceptionNodes,
                new ExpressionStatement(forwardExpression)
        );
        List<AnnotationNode> copied = new LinkedList<AnnotationNode>();
        List<AnnotationNode> notCopied = Collections.emptyList(); // at this point, should *always* stay empty
        GeneralUtils.copyAnnotatedNodeAnnotations(helperMethod, copied, notCopied);
        if (!copied.isEmpty()) {
            forwarder.addAnnotations(copied);
        }

        // add a helper annotation indicating that it is a bridge method
        AnnotationNode bridgeAnnotation = new AnnotationNode(Traits.TRAITBRIDGE_CLASSNODE);
        bridgeAnnotation.addMember("traitClass", new ClassExpression(trait));
        bridgeAnnotation.addMember("desc", new ConstantExpression(BytecodeHelper.getMethodDescriptor(helperMethod.getReturnType(), traitMethodParams)));
        forwarder.addAnnotation(
                bridgeAnnotation
        );

        if (!shouldSkipMethod(targetNode, forwarder.getName(), forwarderParams)) {
            targetNode.addMethod(forwarder);
        }

        createSuperForwarder(targetNode, forwarder, genericsSpec);
    }

    /**
     * Creates, if necessary, a super forwarder method, for stackable traits.
     * @param forwarder a forwarder method
     * @param genericsSpec
     */
    private static void createSuperForwarder(ClassNode targetNode, MethodNode forwarder, final Map genericsSpec) {
        List<ClassNode> interfaces = new ArrayList<ClassNode>(Traits.collectAllInterfacesReverseOrder(targetNode, new LinkedHashSet<ClassNode>()));
        String name = forwarder.getName();
        Parameter[] forwarderParameters = forwarder.getParameters();
        LinkedHashSet<ClassNode> traits = new LinkedHashSet<ClassNode>();
        List<MethodNode> superForwarders = new LinkedList<MethodNode>();
        for (ClassNode node : interfaces) {
            if (Traits.isTrait(node)) {
                MethodNode method = node.getDeclaredMethod(name, forwarderParameters);
                if (method!=null) {
                    // a similar method exists, we need a super bridge
                    // trait$super$foo(Class currentTrait, ...)
                    traits.add(node);
                    superForwarders.add(method);
                }
            }
        }
        for (MethodNode superForwarder : superForwarders) {
            doCreateSuperForwarder(targetNode, superForwarder, traits.toArray(new ClassNode[traits.size()]), genericsSpec);
        }
    }

    /**
     * Creates a method to dispatch to "super traits" in a "stackable" fashion. The generated method looks like this:
     * <p>
     * <code>ReturnType trait$super$method(Class clazz, Arg1 arg1, Arg2 arg2, ...) {
     *     if (SomeTrait.is(A) { return SomeOtherTrait$Trait$Helper.method(this, arg1, arg2) }
     *     super.method(arg1,arg2)
     * }</code>
     * </p>
     * @param targetNode
     * @param forwarderMethod
     * @param interfacesToGenerateForwarderFor
     * @param genericsSpec
     */
    private static void doCreateSuperForwarder(ClassNode targetNode, MethodNode forwarderMethod, ClassNode[] interfacesToGenerateForwarderFor, Map genericsSpec) {
        Parameter[] parameters = forwarderMethod.getParameters();
        Parameter[] superForwarderParams = new Parameter[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ClassNode originType = parameter.getOriginType();
            superForwarderParams[i] = new Parameter(correctToGenericsSpecRecurse(genericsSpec, originType), parameter.getName());
        }
        for (int i = 0; i < interfacesToGenerateForwarderFor.length; i++) {
            final ClassNode current = interfacesToGenerateForwarderFor[i];
            final ClassNode next = i<interfacesToGenerateForwarderFor.length-1?interfacesToGenerateForwarderFor[i+1]:null;
            String forwarderName = Traits.getSuperTraitMethodName(current, forwarderMethod.getName());
            if (targetNode.getDeclaredMethod(forwarderName, superForwarderParams)==null) {
            ClassNode returnType = correctToGenericsSpecRecurse(genericsSpec, forwarderMethod.getReturnType());
            Statement delegate = next==null?createSuperFallback(forwarderMethod, returnType):createDelegatingForwarder(forwarderMethod, next);
            targetNode.addMethod(forwarderName, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, returnType, superForwarderParams, ClassNode.EMPTY_ARRAY, delegate);
            }
        }
    }

    private static Statement createSuperFallback(MethodNode forwarderMethod, ClassNode returnType) {
        ArgumentListExpression args = new ArgumentListExpression();
        Parameter[] forwarderMethodParameters = forwarderMethod.getParameters();
        for (final Parameter forwarderMethodParameter : forwarderMethodParameters) {
            args.addExpression(new VariableExpression(forwarderMethodParameter));
        }
        BinaryExpression instanceOfExpr = new BinaryExpression(new VariableExpression("this"), Token.newSymbol(Types.KEYWORD_INSTANCEOF, -1, -1), new ClassExpression(Traits.GENERATED_PROXY_CLASSNODE));
        MethodCallExpression superCall = new MethodCallExpression(
                new VariableExpression("super"),
                forwarderMethod.getName(),
                args
        );
        superCall.setImplicitThis(false);
        CastExpression proxyReceiver = new CastExpression(Traits.GENERATED_PROXY_CLASSNODE, new VariableExpression("this"));
        MethodCallExpression getProxy = new MethodCallExpression(proxyReceiver, "getProxyTarget", ArgumentListExpression.EMPTY_ARGUMENTS);
        getProxy.setImplicitThis(true);
        StaticMethodCallExpression proxyCall = new StaticMethodCallExpression(
                ClassHelper.make(InvokerHelper.class),
                "invokeMethod",
                new ArgumentListExpression(getProxy, new ConstantExpression(forwarderMethod.getName()), new ArrayExpression(ClassHelper.OBJECT_TYPE, args.getExpressions()))
        );
        IfStatement stmt = new IfStatement(
                new BooleanExpression(instanceOfExpr),
                new ExpressionStatement(new CastExpression(returnType,proxyCall)),
                new ExpressionStatement(superCall)
        );
        return stmt;
    }

    private static Statement createDelegatingForwarder(final MethodNode forwarderMethod, final ClassNode next) {
        // generates --> next$Trait$Helper.method(this, arg1, arg2)
        TraitHelpersTuple helpers = Traits.findHelpers(next);
        ArgumentListExpression args = new ArgumentListExpression();
        args.addExpression(new VariableExpression("this"));
        Parameter[] forwarderMethodParameters = forwarderMethod.getParameters();
        for (final Parameter forwarderMethodParameter : forwarderMethodParameters) {
            args.addExpression(new VariableExpression(forwarderMethodParameter));
        }
        StaticMethodCallExpression delegateCall = new StaticMethodCallExpression(
                helpers.getHelper(),
                forwarderMethod.getName(),
                args
        );
        Statement result;
        if (ClassHelper.VOID_TYPE.equals(forwarderMethod.getReturnType())) {
            BlockStatement stmt = new BlockStatement();
            stmt.addStatement(new ExpressionStatement(delegateCall));
            stmt.addStatement(new ReturnStatement(new ConstantExpression(null)));
            result = stmt;
        } else {
            result = new ReturnStatement(delegateCall);
        }
        return result;
    }

    private static ClassNode[] copyExceptions(final ClassNode[] sourceExceptions) {
        ClassNode[] exceptionNodes = new ClassNode[sourceExceptions == null ? 0 : sourceExceptions.length];
        System.arraycopy(sourceExceptions, 0, exceptionNodes, 0, exceptionNodes.length);
        return exceptionNodes;
    }

    private static boolean shouldSkipMethod(final ClassNode cNode, final String name, final Parameter[] params) {
        if (isExistingProperty(name, cNode, params) || cNode.getDeclaredMethod(name, params)!=null) {
            // override exists in the weaved class itself
            return true;
        }
        return false;
    }

    /**
     * An utility method which tries to find a method with default implementation (in the Java 8 semantics).
     * @param cNode a class node
     * @param name the name of the method
     * @param params the parameters of the method
     * @return a method node corresponding to a default method if it exists
     */
    private static MethodNode findDefaultMethodFromInterface(final ClassNode cNode, final String name, final Parameter[] params) {
        if (cNode == null) {
            return null;
        }
        if (cNode.isInterface()) {
            MethodNode method = cNode.getMethod(name, params);
            if (method!=null && !method.isAbstract()) {
                // this is a Java 8 only behavior!
                return method;
            }
        }
        ClassNode[] interfaces = cNode.getInterfaces();
        for (ClassNode anInterface : interfaces) {
            MethodNode res = findDefaultMethodFromInterface(anInterface, name, params);
            if (res!=null) {
                return res;
            }
        }
        return findDefaultMethodFromInterface(cNode.getSuperClass(), name, params);
    }

    private static boolean isExistingProperty(final String methodName, final ClassNode cNode, final Parameter[] params) {
        String propertyName = methodName;
        boolean getter = false;
        if (methodName.startsWith("get")) {
            propertyName = propertyName.substring(3);
            getter = true;
        } else if (methodName.startsWith("is")) {
            propertyName = propertyName.substring(2);
            getter = true;
        } else if (methodName.startsWith("set")) {
            propertyName = propertyName.substring(3);
        } else {
            return false;
        }
        if (getter && params.length>0) {
            return false;
        }
        if (!getter && params.length!=1) {
            return false;
        }
        if (propertyName.length()==0) {
            return false;
        }
        propertyName = MetaClassHelper.convertPropertyName(propertyName);
        PropertyNode pNode = cNode.getProperty(propertyName);
        return pNode != null;
    }
}

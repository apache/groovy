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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class contains a static utility method {@link #doExtendTraits(org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.control.SourceUnit)}
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

    /**
     * Given a class node, if this class node implements a trait, then generate all the appropriate
     * code which delegates calls to the trait. It is safe to call this method on a class node which
     * does not implement a trait.
     * @param cNode a class node
     * @param unit the source unit
     */
    public static void doExtendTraits(final ClassNode cNode, final SourceUnit unit) {
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
            }
        }
    }

    /**
     * Collects all interfaces of a class node, but reverses the order of the declaration of direct interfaces
     * of this class node. This is used to make sure a trait implementing A,B where both A and B have the same
     * method will take the method from B (latest), aligning the behavior with categories.
     * @param cNode a class node
     * @param interfaces ordered set of interfaces
     */
    private static void collectAllInterfacesReverseOrder(ClassNode cNode, LinkedHashSet<ClassNode> interfaces) {
        if (cNode.isInterface())
            interfaces.add(cNode);

        ClassNode[] directInterfaces = cNode.getInterfaces();
        for (int i = directInterfaces.length-1; i >=0 ; i--) {
            final ClassNode anInterface = directInterfaces[i];
            interfaces.add(anInterface);
            collectAllInterfacesReverseOrder(anInterface, interfaces);
        }
    }

    private static List<ClassNode> findTraits(ClassNode cNode) {
        LinkedHashSet<ClassNode> interfaces = new LinkedHashSet<ClassNode>();
        collectAllInterfacesReverseOrder(cNode, interfaces);
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
        boolean isTraitForceOverride = !trait.getAnnotations(Traits.FORCEOVERRIDE_CLASSNODE).isEmpty();
        ClassNode helperClassNode = helpers.getHelper();
        ClassNode fieldHelperClassNode = helpers.getFieldHelper();
        Map genericsSpec = GenericsUtils.createGenericsSpec(cNode, new HashMap());
        genericsSpec = GenericsUtils.createGenericsSpec(trait, genericsSpec);

        for (MethodNode methodNode : helperClassNode.getAllDeclaredMethods()) {
            boolean isForceOverride = isTraitForceOverride || Traits.isForceOverride(methodNode);
            String name = methodNode.getName();
            int access = methodNode.getModifiers();
            Parameter[] argumentTypes = methodNode.getParameters();
            ClassNode[] exceptions = methodNode.getExceptions();
            ClassNode returnType = methodNode.getReturnType();
            boolean isAbstract = methodNode.isAbstract();
            if (!isAbstract && argumentTypes.length > 0 && ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) && !name.contains("$")) {
                ArgumentListExpression argList = new ArgumentListExpression();
                argList.addExpression(new VariableExpression("this"));
                Parameter[] params = new Parameter[argumentTypes.length - 1];
                for (int i = 1; i < argumentTypes.length; i++) {
                    Parameter parameter = argumentTypes[i];
                    ClassNode originType = parameter.getOriginType();
                    ClassNode fixedType = AbstractASTTransformation.correctToGenericsSpecRecurse(genericsSpec, originType);
                    Parameter newParam = new Parameter(fixedType, "arg" + i);
                    List<AnnotationNode> copied = new LinkedList<AnnotationNode>();
                    List<AnnotationNode> notCopied = new LinkedList<AnnotationNode>();
                    AbstractASTTransformation.copyAnnotatedNodeAnnotations(parameter, copied, notCopied);
                    newParam.addAnnotations(copied);
                    params[i - 1] = newParam;
                    argList.addExpression(new VariableExpression(params[i - 1]));
                }
                MethodNode existingMethod = cNode.getMethod(name, params);
                if (!isForceOverride && (existingMethod != null || isExistingProperty(name, cNode, params))) {
                    // override exists in the weaved class or any parent
                    continue;
                }
                if (isForceOverride && cNode.getDeclaredMethod(name, params)!=null) {
                    // override exists in the weaved class itself
                    continue;
                }
                ClassNode[] exceptionNodes = new ClassNode[exceptions == null ? 0 : exceptions.length];
                System.arraycopy(exceptions, 0, exceptionNodes, 0, exceptionNodes.length);
                MethodCallExpression mce = new MethodCallExpression(
                        new ClassExpression(helperClassNode),
                        name,
                        argList
                );
                mce.setImplicitThis(false);
                ClassNode fixedReturnType = AbstractASTTransformation.correctToGenericsSpecRecurse(genericsSpec, returnType);
                Expression forwardExpression = genericsSpec.isEmpty()?mce:new CastExpression(fixedReturnType,mce);
                MethodNode forwarder = new MethodNode(
                        name,
                        access ^ Opcodes.ACC_STATIC,
                        fixedReturnType,
                        params,
                        exceptionNodes,
                        new ExpressionStatement(forwardExpression)
                );
                cNode.addMethod(forwarder);
            }
        }
        cNode.addObjectInitializerStatements(new ExpressionStatement(
                new MethodCallExpression(
                        new ClassExpression(helperClassNode),
                        Traits.STATIC_INIT_METHOD,
                        new ArgumentListExpression(new VariableExpression("this")))
        ));
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
                    ClassNode returnType = AbstractASTTransformation.correctToGenericsSpecRecurse(genericsSpec, methodNode.getReturnType());
                    if (getter) {
                        // add field
                        cNode.addField(fieldName, Opcodes.ACC_PRIVATE, returnType, null);
                    }
                    Parameter[] newParams;
                    if (getter) {
                        newParams = Parameter.EMPTY_ARRAY;
                    } else {
                        ClassNode originType = methodNode.getParameters()[0].getOriginType();
                        ClassNode fixedType = originType.isGenericsPlaceHolder()?ClassHelper.OBJECT_TYPE:AbstractASTTransformation.correctToGenericsSpecRecurse(genericsSpec, originType);
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
                            Opcodes.ACC_PUBLIC,
                            returnType,
                            newParams,
                            ClassNode.EMPTY_ARRAY,
                            body
                    );
                    impl.addAnnotation(new AnnotationNode(ClassHelper.make(CompileStatic.class)));
                    cNode.addMethod(impl);
                }
            }
        }
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

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
package org.codehaus.groovy.transform.trait;

import groovy.transform.CompileStatic;
import org.apache.groovy.ast.tools.MethodNodeUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor;
import org.codehaus.groovy.transform.sc.StaticCompileTransformation;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * This class contains a static utility method {@link #doExtendTraits(org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.control.SourceUnit, org.codehaus.groovy.control.CompilationUnit)}
 * aimed at generating code for a classnode implementing a trait.
 *
 * @since 2.3.0
 */
public abstract class TraitComposer {

    public static final ClassNode COMPILESTATIC_CLASSNODE = ClassHelper.make(CompileStatic.class);

    /**
     * Given a class node, if this class node implements a trait, then generate
     * all the appropriate code which delegates calls to the trait.  It is safe
     * to call this method on a class node which does not implement a trait.
     */
    public static void doExtendTraits(final ClassNode cn, final SourceUnit su, final CompilationUnit cu) {
        if (cn.isInterface()) return;
        if (Traits.isTrait(cn)) {
            checkTraitAllowed(cn, su);
            return;
        }
        if (!cn.getNameWithoutPackage().endsWith(Traits.TRAIT_HELPER)) {
            GroovyClassVisitor visitor = new SuperCallTraitTransformer(su);
            for (ClassNode trait : Traits.findTraits(cn)) {
                applyTrait(trait, cn, Traits.findHelpers(trait), su);
                visitor.visitClass(cn);
            }
            if (su != null) {
                visitor = new ASTTransformationCollectorCodeVisitor(su, cu.getTransformLoader());
                visitor.visitClass(cn);
            }
        }
    }

    private static void checkTraitAllowed(final ClassNode bottomTrait, final SourceUnit unit) {
        ClassNode superClass = bottomTrait.getSuperClass();
        if (superClass == null || ClassHelper.isObjectType(superClass)) return;
        if (!Traits.isTrait(superClass)) {
            unit.addError(new SyntaxException("A trait can only inherit from another trait", superClass.getLineNumber(), superClass.getColumnNumber()));
        }
    }

    private static void applyTrait(final ClassNode trait, final ClassNode cNode, final TraitHelpersTuple helpers, SourceUnit unit) {
        ClassNode helperClassNode = helpers.getHelper();
        ClassNode fieldHelperClassNode = helpers.getFieldHelper();
        ClassNode staticFieldHelperClassNode = helpers.getStaticFieldHelper();
        Map<String, ClassNode> genericsSpec = GenericsUtils.createGenericsSpec(trait, GenericsUtils.createGenericsSpec(cNode));

        List<MethodNode> hMethods = helperClassNode.getMethods();
        if (!hMethods.isEmpty()) {
            hMethods.sort(Comparator.comparing(MethodNodeUtils::methodDescriptorWithoutReturnType));
        }
        for (MethodNode methodNode : hMethods) {
            String name = methodNode.getName();
            Parameter[] helperMethodParams = methodNode.getParameters();
            int nParams = helperMethodParams.length;
            if (nParams > 0 && methodNode.isStatic() && !methodNode.isAbstract()
                    && (!name.contains("$") || (methodNode.getModifiers() & Opcodes.ACC_SYNTHETIC) == 0)) {
                ArgumentListExpression argList = new ArgumentListExpression();
                argList.addExpression(varX("this"));
                Parameter[] origParams = new Parameter[nParams - 1];
                Parameter[] params = new Parameter[nParams - 1];
                System.arraycopy(methodNode.getParameters(), 1, params, 0, params.length);

                MethodNode originalMethod = trait.getMethod(name, params);
                Map<String, ClassNode> methodGenericsSpec = GenericsUtils.addMethodGenerics(
                        Optional.ofNullable(originalMethod).orElse(methodNode), genericsSpec);

                for (int i = 1; i < nParams; i += 1) {
                    Parameter parameter = helperMethodParams[i];
                    ClassNode originType = parameter.getOriginType();
                    ClassNode fixedType = GenericsUtils.correctToGenericsSpecRecurse(methodGenericsSpec, originType);
                    Parameter newParam = new Parameter(fixedType, parameter.getName());
                    List<AnnotationNode> copied = new LinkedList<>();
                    List<AnnotationNode> notCopied = new LinkedList<>();
                    GeneralUtils.copyAnnotatedNodeAnnotations(parameter, copied, notCopied);
                    newParam.addAnnotations(copied);
                    params[i - 1] = newParam;
                    origParams[i - 1] = parameter;
                    argList.addExpression(varX(newParam));
                }
                createForwarderMethod(trait, cNode, methodNode, originalMethod, helperClassNode, methodGenericsSpec, helperMethodParams, origParams, params, argList, unit);
            }
        }

        MethodCallExpression staticInitCall = callX(
                classX(helperClassNode),
                Traits.STATIC_INIT_METHOD,
                classX(cNode));
        MethodNode staticInitMethod = new MethodNode(
                Traits.STATIC_INIT_METHOD, Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, ClassHelper.VOID_TYPE,
                new Parameter[] {new Parameter(ClassHelper.CLASS_Type,"clazz")}, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        staticInitMethod.setDeclaringClass(helperClassNode);
        staticInitCall.setMethodTarget(staticInitMethod);
        cNode.addStaticInitializerStatements(Collections.singletonList(stmt(staticInitCall)), false);
        if (fieldHelperClassNode != null && !cNode.declaresInterface(fieldHelperClassNode)) {
            // we should implement the field helper interface too
            cNode.addInterface(fieldHelperClassNode);
            // implementation of methods
            List<MethodNode> declaredMethods = new LinkedList<>();
            int pos = 0; // keep direct getters at start but in declaration order
            List<MethodNode> fhMethods = fieldHelperClassNode.getMethods();
            if (!fhMethods.isEmpty()) {
                fhMethods.sort(Comparator.comparing(MethodNodeUtils::methodDescriptorWithoutReturnType));
            }
            for (MethodNode declaredMethod : fhMethods) {
                if (declaredMethod.getName().endsWith(Traits.DIRECT_GETTER_SUFFIX)) {
                    declaredMethods.add(pos++, declaredMethod);
                } else {
                    declaredMethods.add(declaredMethod);
                }
            }

            if (staticFieldHelperClassNode != null) {
                List<MethodNode> sfhMethods = staticFieldHelperClassNode.getMethods();
                if (!sfhMethods.isEmpty()) {
                    sfhMethods.sort(Comparator.comparing(MethodNodeUtils::methodDescriptorWithoutReturnType));
                }
                for (MethodNode declaredMethod : sfhMethods) {
                    if (declaredMethod.getName().endsWith(Traits.DIRECT_GETTER_SUFFIX)) {
                        declaredMethods.add(pos++, declaredMethod);
                    } else {
                        declaredMethods.add(declaredMethod);
                    }
                }
            }

            for (MethodNode methodNode : declaredMethods) {
                String fieldName = methodNode.getName();
                if (fieldName.endsWith(Traits.DIRECT_GETTER_SUFFIX) || fieldName.endsWith(Traits.DIRECT_SETTER_SUFFIX)) {
                    int suffixIdx = fieldName.lastIndexOf('$');
                    fieldName = fieldName.substring(0, suffixIdx);
                    String operation = methodNode.getName().substring(suffixIdx + 1);
                    boolean getter = "get".equals(operation);
                    ClassNode returnType = GenericsUtils.correctToGenericsSpecRecurse(genericsSpec, methodNode.getReturnType());
                    int fieldMods = 0;
                    int isStatic = 0;
                    boolean publicField = true;
                    FieldNode helperField = null;
                    fieldMods = 0;
                    isStatic = 0;

                    // look first for field with encoded modifier information
                    for (Integer mod : Traits.FIELD_PREFIXES) {
                        helperField = fieldHelperClassNode.getField(String.format("$0x%04x", mod) + fieldName);
                        if (helperField != null) {
                            if ((mod & Opcodes.ACC_STATIC) != 0) isStatic = Opcodes.ACC_STATIC;
                            fieldMods = fieldMods | mod;
                            break;
                        }
                    }

                    if (helperField == null) {
                        // look for possible legacy fields (trait compiled pre 2.4.8)
                        helperField = fieldHelperClassNode.getField(Traits.FIELD_PREFIX + Traits.PUBLIC_FIELD_PREFIX + fieldName);
                        if (helperField == null) {
                            publicField = false;
                            helperField = fieldHelperClassNode.getField(Traits.FIELD_PREFIX + Traits.PRIVATE_FIELD_PREFIX + fieldName);
                        }
                        if (helperField == null) {
                            publicField = true;
                            // try to find a static one
                            helperField = fieldHelperClassNode.getField(Traits.STATIC_FIELD_PREFIX+Traits.PUBLIC_FIELD_PREFIX + fieldName);
                            if (helperField == null) {
                                publicField = false;
                                helperField = fieldHelperClassNode.getField(Traits.STATIC_FIELD_PREFIX+Traits.PRIVATE_FIELD_PREFIX + fieldName);
                            }
                            fieldMods = fieldMods | Opcodes.ACC_STATIC;
                            isStatic = Opcodes.ACC_STATIC;
                        }
                        fieldMods = fieldMods | (publicField?Opcodes.ACC_PUBLIC:Opcodes.ACC_PRIVATE);
                    }
                    if (getter) {
                        // add field
                        if (helperField!=null) {
                            List<AnnotationNode> copied = new LinkedList<>();
                            List<AnnotationNode> notCopied = new LinkedList<>();
                            GeneralUtils.copyAnnotatedNodeAnnotations(helperField, copied, notCopied);
                            FieldNode fieldNode = cNode.addField(fieldName, fieldMods, returnType, null);
                            fieldNode.addAnnotations(copied);
                            // getInitialExpression above will be null if not in same source unit
                            // so instead set within (static) initializer
                            if (fieldNode.isFinal()) {
                                String baseName = fieldNode.isStatic() ? Traits.STATIC_INIT_METHOD : Traits.INIT_METHOD;
                                StaticMethodCallExpression mce = callX(helperClassNode, baseName + fieldNode.getName(), args(varX("this")));
                                if (helperClassNode.hasPossibleStaticMethod(mce.getMethod(), mce.getArguments())) {
                                    Statement stmt = stmt(assignX(varX(fieldNode.getName(), fieldNode.getType()), mce));
                                    if (isStatic == 0) {
                                        cNode.addObjectInitializerStatements(stmt);
                                    } else {
                                        List<Statement> staticStatements = new ArrayList<>();
                                        staticStatements.add(stmt);
                                        cNode.addStaticInitializerStatements(staticStatements, true);
                                    }
                                }
                            }
                        }
                    }
                    Parameter[] newParams;
                    if (getter) {
                        newParams = Parameter.EMPTY_ARRAY;
                    } else {
                        ClassNode originType = methodNode.getParameters()[0].getOriginType();
                        ClassNode fixedType = originType.isGenericsPlaceHolder()?ClassHelper.OBJECT_TYPE:GenericsUtils.correctToGenericsSpecRecurse(genericsSpec, originType);
                        newParams = new Parameter[]{new Parameter(fixedType, "val")};
                    }

                    Expression fieldExpr = varX(cNode.getField(fieldName));
                    boolean finalSetter = !getter && (fieldMods & Opcodes.ACC_FINAL) != 0;
                    Statement body =
                            getter ? returnS(fieldExpr) :
                                    (finalSetter ? null : stmt(
                                            assignX(
                                                    fieldExpr,
                                                    varX(newParams[0])
                                            )
                                    ));
                    // add getter/setter even though setter not strictly needed for final fields
                    // but add empty body for setter for legacy compatibility
                    MethodNode impl = new MethodNode(
                            methodNode.getName(),
                            Opcodes.ACC_PUBLIC | isStatic,
                            returnType,
                            newParams,
                            ClassNode.EMPTY_ARRAY,
                            body
                    );
                    AnnotationNode an = new AnnotationNode(COMPILESTATIC_CLASSNODE);
                    impl.addAnnotation(an);
                    cNode.addTransform(StaticCompileTransformation.class, an);
                    addGeneratedMethod(cNode, impl);
                }
            }
        }
        cNode.addObjectInitializerStatements(stmt(
                callX(classX(helperClassNode), Traits.INIT_METHOD, varX("this"))
        ));
    }

    private static void createForwarderMethod(
            ClassNode trait, ClassNode targetNode, MethodNode helperMethod, MethodNode originalMethod, ClassNode helperClassNode, Map<String, ClassNode> genericsSpec,
            Parameter[] helperMethodParams, Parameter[] traitMethodParams, Parameter[] forwarderParams, ArgumentListExpression helperMethodArgList, SourceUnit unit) {
        MethodCallExpression mce = callX(
                classX(helperClassNode),
                helperMethod.getName(),
                helperMethodArgList
        );
        mce.setImplicitThis(false);

        ClassNode[] exceptionTypes = GenericsUtils.correctToGenericsSpecRecurse(genericsSpec, copyExceptions(helperMethod.getExceptions()));
        ClassNode returnType = GenericsUtils.correctToGenericsSpecRecurse(genericsSpec, helperMethod.getReturnType());
        boolean castRequired = !genericsSpec.isEmpty() && !helperMethod.isVoidMethod();

        boolean isHelperForStaticMethod = ClassHelper.isClassType(helperMethodParams[0].getOriginType());
        if (helperMethod.isPrivate() && !isHelperForStaticMethod) {
            // GROOVY-7213: do not create forwarder for private methods
            return;
        }
        int modifiers = helperMethod.getModifiers();
        if (!isHelperForStaticMethod) {
            modifiers &= ~Opcodes.ACC_STATIC;
        }
        // GROOVY-10467: added by classgen
        modifiers &= ~Opcodes.ACC_VARARGS;

        MethodNode forwarder = new MethodNode(
                helperMethod.getName(),
                modifiers,
                returnType,
                forwarderParams,
                exceptionTypes,
                stmt(castRequired ? castX(returnType, mce) : mce)
        );
        List<AnnotationNode> copied = new LinkedList<>();
        List<AnnotationNode> notCopied = Collections.emptyList(); // at this point, should *always* stay empty
        GeneralUtils.copyAnnotatedNodeAnnotations(helperMethod, copied, notCopied);
        if (!copied.isEmpty()) {
            forwarder.addAnnotations(copied);
        }
        if (originalMethod != null) {
            GenericsType[] newGt = GenericsUtils.applyGenericsContextToPlaceHolders(genericsSpec, originalMethod.getGenericsTypes());
            newGt = removeNonPlaceHolders(newGt);
            forwarder.setGenericsTypes(newGt);
        } else {
            // null indicates a static method which may still need generics correction
            GenericsType[] genericsTypes = helperMethod.getGenericsTypes();
            if (genericsTypes != null) {
                Map<String, ClassNode> methodSpec = GenericsUtils.addMethodGenerics(helperMethod, Collections.emptyMap());
                GenericsType[] newGt = GenericsUtils.applyGenericsContextToPlaceHolders(methodSpec, helperMethod.getGenericsTypes());
                forwarder.setGenericsTypes(newGt);
            }
        }
        // add a helper annotation indicating that it is a bridge method
        AnnotationNode bridgeAnnotation = new AnnotationNode(Traits.TRAITBRIDGE_CLASSNODE);
        bridgeAnnotation.addMember("traitClass", classX(trait));
        bridgeAnnotation.addMember("desc", constX(BytecodeHelper.getMethodDescriptor(helperMethod.getReturnType(), traitMethodParams)));
        forwarder.addAnnotation(bridgeAnnotation);

        MethodNode existingMethod = findExistingMethod(targetNode, forwarder);
        if (existingMethod != null) {
            if (!forwarder.isStatic() && existingMethod.isStatic()) {
                // found an existing static method that is going to conflict with interface
                unit.addError(createException(trait, targetNode, forwarder, existingMethod));
                return;
            }
        }

        if (!shouldSkipMethod(targetNode, forwarder.getName(), forwarderParams)) {
            targetNode.addMethod(forwarder);
        }

        createSuperForwarder(targetNode, forwarder, genericsSpec);
    }

    private static SyntaxException createException(ClassNode trait, ClassNode targetNode, MethodNode forwarder, MethodNode existingMethod) {
        String middle;
        ASTNode errorTarget;
        if (existingMethod.getLineNumber() == -1) {
            // came from a trait
            errorTarget = targetNode;
            List<AnnotationNode> allAnnos = existingMethod.getAnnotations(Traits.TRAITBRIDGE_CLASSNODE);
            AnnotationNode bridgeAnno = allAnnos == null ? null : allAnnos.get(0);
            String fromTrait = null;
            if (bridgeAnno != null) {
                Expression traitClass = bridgeAnno.getMember("traitClass");
                if (traitClass instanceof ClassExpression) {
                    fromTrait = traitClass.getType().getNameWithoutPackage();
                }
            }
            middle = "in '" + targetNode.getNameWithoutPackage();
            if (fromTrait != null) {
                middle += "' from trait '" + fromTrait;
            }
        } else {
            errorTarget = existingMethod;
            middle = "declared in '" + targetNode.getNameWithoutPackage();
        }
        String message = "The static '" + forwarder.getName() + "' method " + middle +
                "' conflicts with the instance method having the same signature from trait '" + trait.getNameWithoutPackage() + "'";
        return new SyntaxException(message, errorTarget);
    }

    private static GenericsType[] removeNonPlaceHolders(GenericsType[] oldTypes) {
        if (oldTypes==null || oldTypes.length==0) return oldTypes;
        ArrayList<GenericsType> l = new ArrayList<>(Arrays.asList(oldTypes));
        Iterator<GenericsType> it = l.iterator();
        boolean modified = false;
        while (it.hasNext()) {
            GenericsType gt = it.next();
            if (!gt.isPlaceholder()) {
                it.remove();
                modified = true;
            }
        }
        if (!modified) return oldTypes;
        if (l.isEmpty()) return null;
        return l.toArray(GenericsType.EMPTY_ARRAY);
    }

    /**
     * Creates, if necessary, a super forwarder method, for stackable traits.
     * @param forwarder a forwarder method
     * @param genericsSpec
     */
    private static void createSuperForwarder(ClassNode targetNode, MethodNode forwarder, final Map<String,ClassNode> genericsSpec) {
        List<ClassNode> interfaces = new ArrayList<>(Traits.collectAllInterfacesReverseOrder(targetNode, new LinkedHashSet<>()));
        String name = forwarder.getName();
        Parameter[] forwarderParameters = forwarder.getParameters();
        LinkedHashSet<ClassNode> traits = new LinkedHashSet<>();
        List<MethodNode> superForwarders = new LinkedList<>();
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
            doCreateSuperForwarder(targetNode, superForwarder, traits.toArray(ClassNode.EMPTY_ARRAY), genericsSpec);
        }
    }

    /**
     * Creates a method to dispatch to "super traits" in a "stackable" fashion. The generated method looks like this:
     * <pre>ReturnType trait$super$method(Class clazz, Arg1 arg1, Arg2 arg2, ...) {
     *     if (SomeTrait.is(A) return SomeOtherTrait$Trait$Helper.method(this, arg1, arg2)
     *     super.method(arg1,arg2)
     * }
     * </pre>
     * @param targetNode
     * @param forwarderMethod
     * @param interfacesToGenerateForwarderFor
     * @param genericsSpec
     */
    private static void doCreateSuperForwarder(final ClassNode targetNode, final MethodNode forwarderMethod, final ClassNode[] interfacesToGenerateForwarderFor, final Map<String,ClassNode> genericsSpec) {
        Parameter[] parameters = forwarderMethod.getParameters();
        Parameter[] superForwarderParams = new Parameter[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ClassNode originType = parameter.getOriginType();
            superForwarderParams[i] = new Parameter(GenericsUtils.correctToGenericsSpecRecurse(genericsSpec, originType), parameter.getName());
        }
        for (int i = 0; i < interfacesToGenerateForwarderFor.length; i++) {
            final ClassNode current = interfacesToGenerateForwarderFor[i];
            final ClassNode next = i < interfacesToGenerateForwarderFor.length - 1 ? interfacesToGenerateForwarderFor[i + 1] : null;
            String forwarderName = Traits.getSuperTraitMethodName(current, forwarderMethod.getName());
            if (targetNode.getDeclaredMethod(forwarderName, superForwarderParams) == null) {
                ClassNode returnType = GenericsUtils.correctToGenericsSpecRecurse(genericsSpec, forwarderMethod.getReturnType());
                Statement delegate = next == null ? createSuperFallback(forwarderMethod, returnType) : createDelegatingForwarder(forwarderMethod, next);

                MethodNode methodNode = addGeneratedMethod(targetNode, forwarderName, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, returnType, superForwarderParams, ClassNode.EMPTY_ARRAY, delegate);
                methodNode.setGenericsTypes(forwarderMethod.getGenericsTypes());
            }
        }
    }

    private static Statement createSuperFallback(final MethodNode forwarderMethod, final ClassNode returnType) {
        ArgumentListExpression paramTuple = args(Arrays.stream(forwarderMethod.getParameters()).map(p -> varX(p)).toArray(Expression[]::new));

        MethodCallExpression proxyTarget = callX(castX(Traits.GENERATED_PROXY_CLASSNODE, varX("this")), "getProxyTarget");
        proxyTarget.setImplicitThis(false);

        Expression proxyCall = callX(ClassHelper.make(InvokerHelper.class), "invokeMethod",
                args(proxyTarget, constX(forwarderMethod.getName()), new ArrayExpression(ClassHelper.OBJECT_TYPE, paramTuple.getExpressions()))
        );

        MethodCallExpression superCall = callX(varX("super"), forwarderMethod.getName(), paramTuple);
        superCall.putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, Boolean.TRUE); //GROOVY-10478
        superCall.setImplicitThis(false);

        // if (this instanceof GeneratedGroovyProxy)
        //   (ReturnType) InvokerHelper.invokeMethod(((GeneratedGroovyProxy) this).getProxyTarget(), "targetMethod", new Object[]{arguments})
        // else
        //   super.targetMethod(arguments)
        return ifElseS(
                isInstanceOfX(varX("this"), Traits.GENERATED_PROXY_CLASSNODE),
                stmt(castX(returnType, proxyCall)),
                stmt(superCall)
        );
    }

    private static Statement createDelegatingForwarder(final MethodNode forwarderMethod, final ClassNode next) {
        // generates --> next$Trait$Helper.method(this, arg1, arg2)
        ArgumentListExpression args = new ArgumentListExpression();
        args.addExpression(varX("this"));
        for (Parameter p : forwarderMethod.getParameters()) args.addExpression(varX(p));
        Expression delegateCall = callX(Traits.findHelper(next), forwarderMethod.getName(), args);

        return forwarderMethod.isVoidMethod() ? block(stmt(delegateCall), returnS(nullX())) : returnS(delegateCall);
    }

    private static ClassNode[] copyExceptions(final ClassNode[] sourceExceptions) {
        ClassNode[] exceptionNodes = new ClassNode[sourceExceptions == null ? 0 : sourceExceptions.length];
        System.arraycopy(sourceExceptions, 0, exceptionNodes, 0, exceptionNodes.length);
        return exceptionNodes;
    }

    private static MethodNode findExistingMethod(final ClassNode cNode, final MethodNode forwarder) {
        return findExistingMethod(cNode, forwarder.getName(), forwarder.getParameters());
    }

    private static MethodNode findExistingMethod(final ClassNode cNode, final String name, final Parameter[] params) {
        MethodNode mNode = cNode.getDeclaredMethod(name, params);
        if (mNode == null) { // GROOVY-11548: check for final method
            mNode = Optional.ofNullable(cNode.getMethod(name, params))
                    .filter(m -> m.isFinal() && !m.isPrivate() && !m.isStatic()).orElse(null);
        }
        return mNode;
    }

    private static boolean shouldSkipMethod(final ClassNode cNode, final String name, final Parameter[] params) {
        if (isExistingProperty(name, cNode, params) || findExistingMethod(cNode, name, params) != null) {
            // override exists in the weaved class itself
            return true;
        }
        return false;
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

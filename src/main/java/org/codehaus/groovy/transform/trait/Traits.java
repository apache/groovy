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

import groovy.lang.GeneratedGroovyProxy;
import groovy.transform.SelfType;
import groovy.transform.Trait;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;

/**
 * A collection of utility methods used to deal with traits.
 *
 * @since 2.3.0
 */
public abstract class Traits {

    /**
     * Class node for {@link Implemented}.
     */
    public static final ClassNode IMPLEMENTED_CLASSNODE = ClassHelper.make(Implemented.class);

    /**
     * Class node for {@link TraitBridge}.
     */
    public static final ClassNode TRAITBRIDGE_CLASSNODE = ClassHelper.make(TraitBridge.class);

    /**
     * Runtime annotation type that marks Groovy traits.
     */
    public static final Class<Trait> TRAIT_CLASS = Trait.class;

    /**
     * Class node for {@link Trait}.
     */
    public static final ClassNode TRAIT_CLASSNODE = ClassHelper.make(TRAIT_CLASS);

    /**
     * Class node for {@link GeneratedGroovyProxy}.
     */
    public static final ClassNode GENERATED_PROXY_CLASSNODE = ClassHelper.make(GeneratedGroovyProxy.class);

    /**
     * Class node for {@link SelfType}.
     */
    public static final ClassNode SELFTYPE_CLASSNODE = ClassHelper.make(SelfType.class);

    /**
     * Display name used in diagnostics for the trait annotation.
     */
    static final String TRAIT_TYPE_NAME = "@" + TRAIT_CLASSNODE.getNameWithoutPackage();

    /**
     * Suffix of the synthetic helper class generated for a trait.
     */
    static final String TRAIT_HELPER = "$Trait$Helper";

    /**
     * Suffix of the synthetic helper class that manages instance trait fields.
     */
    static final String FIELD_HELPER = "$Trait$FieldHelper";

    /**
     * Suffix of the synthetic helper class that manages static trait fields.
     */
    static final String STATIC_FIELD_HELPER = "$Trait$StaticFieldHelper";

    /**
     * Suffix appended to synthetic direct field setter methods.
     */
    static final String DIRECT_SETTER_SUFFIX = "$set";

    /**
     * Suffix appended to synthetic direct field getter methods.
     */
    static final String DIRECT_GETTER_SUFFIX = "$get";

    /**
     * Name of the synthetic instance initialization method.
     */
    static final String INIT_METHOD = "$init$";

    /**
     * Name of the synthetic static initialization method.
     */
    static final String STATIC_INIT_METHOD = "$static$init$";

    /**
     * Synthetic parameter name for the current trait receiver.
     */
    public static final String THIS_OBJECT = "$self";

    /**
     * Synthetic parameter name for the current static trait receiver.
     */
    public static final String STATIC_THIS_OBJECT = "$static$self";

    /**
     * Prefix used when remapping static trait fields.
     */
    static final String STATIC_FIELD_PREFIX = "$static";

    /**
     * Prefix used when remapping instance trait fields.
     */
    static final String FIELD_PREFIX = "$ins";

    /**
     * Prefix fragment used for remapped public trait fields.
     */
    static final String PUBLIC_FIELD_PREFIX = "$0";

    /**
     * Prefix fragment used for remapped private trait fields.
     */
    static final String PRIVATE_FIELD_PREFIX = "$1";
    // TODO decide if we should support VOLATILE
//    def hex(s) {new BigInteger(s, 16).intValue()}
//    def optionals = [[0, 1], [0, 1], [0, 1], [0, 1]].combinations{ a, b, c, d ->
//            (a ? hex('80') : 0) + (b ? hex('10') : 0) + (c ? hex('8') : 0) + (d ? hex('2') : hex('1'))
//    }.sort()
    /**
     * Supported modifier encodings used when remapping trait field names.
     */
    static final List<Integer> FIELD_PREFIXES = Arrays.asList(1, 2, 9, 10, 17, 18, 25, 26, 129, 130, 137, 138, 145, 146, 153, 154);

    /**
     * Modifier mask applied when computing remapped field prefixes.
     */
    static final int FIELD_PREFIX_MASK = ACC_PRIVATE | ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_TRANSIENT;

    /**
     * Separator used in synthetic method names that dispatch to a super trait.
     */
    static final String SUPER_TRAIT_METHOD_PREFIX = "trait$super$";

    /**
     * Returns the generated instance field helper class name for the supplied trait.
     *
     * @param traitNode the trait class node
     * @return the generated field helper class name
     */
    static String fieldHelperClassName(final ClassNode traitNode) {
        return traitNode.getName() + FIELD_HELPER;
    }

    /**
     * Returns the generated static field helper class name for the supplied trait.
     *
     * @param traitNode the trait class node
     * @return the generated static field helper class name
     */
    static String staticFieldHelperClassName(final ClassNode traitNode) {
        return traitNode.getName() + STATIC_FIELD_HELPER;
    }

    /**
     * Returns the synthetic direct getter name for a trait field.
     *
     * @param field the remapped trait field
     * @return the synthetic getter name
     */
    static String helperGetterName(final FieldNode field) {
        return remappedFieldName(unwrapOwner(field.getOwner()), field.getName()) + DIRECT_GETTER_SUFFIX;
    }

    /**
     * Returns the synthetic direct setter name for a trait field.
     *
     * @param field the remapped trait field
     * @return the synthetic setter name
     */
    static String helperSetterName(final FieldNode field) {
        return remappedFieldName(unwrapOwner(field.getOwner()), field.getName()) + DIRECT_SETTER_SUFFIX;
    }

    /**
     * Returns the generated helper class name for the supplied trait.
     *
     * @param traitNode the trait class node
     * @return the generated helper class name
     */
    static String helperClassName(final ClassNode traitNode) {
        return traitNode.getName() + TRAIT_HELPER;
    }

    /**
     * Returns the remapped backing-field name used for a trait field.
     *
     * @param traitNode the owning trait class node
     * @param name the declared field name
     * @return the remapped field name
     */
    static String remappedFieldName(final ClassNode traitNode, final String name) {
        return traitNode.getName().replace('.','_')+"__"+name;
    }

    private static ClassNode unwrapOwner(ClassNode owner) {
        if (ClassHelper.isClassType(owner) && owner.getGenericsTypes() != null && owner.getGenericsTypes().length == 1) {
            return owner.getGenericsTypes()[0].getType();
        }
        return owner;
    }

    /**
     * Returns the generated helper class for a trait.
     *
     * @param trait the trait class node
     * @return the helper class node
     */
    public static ClassNode findHelper(final ClassNode trait) {
        return findHelpers(trait).getHelper();
    }

    /**
     * Returns the generated instance field helper class for a trait.
     *
     * @param trait the trait class node
     * @return the field helper class node, or {@code null} if none exists
     */
    public static ClassNode findFieldHelper(final ClassNode trait) {
        return findHelpers(trait).getFieldHelper();
    }

    /**
     * Returns the generated static field helper class for a trait.
     *
     * @param trait the trait class node
     * @return the static field helper class node, or {@code null} if none exists
     */
    public static ClassNode findStaticFieldHelper(final ClassNode trait) {
        return findHelpers(trait).getStaticFieldHelper();
    }

    /**
     * Returns the helper classes associated with a trait.
     *
     * @param trait the trait class node
     * @return the helper tuple for the trait
     */
    static TraitHelpersTuple findHelpers(final ClassNode trait) {
        ClassNode helperClassNode = null;
        ClassNode fieldHelperClassNode = null;
        ClassNode staticFieldHelperClassNode = null;

        if (trait.isPrimaryClassNode()) { // GROOVY-11743
            var ici = trait.redirect().getInnerClasses();
            while (ici.hasNext()) { ClassNode icn = ici.next();
                if (icn.getName().endsWith(Traits.TRAIT_HELPER)) {
                    helperClassNode = icn;
                } else if (icn.getName().endsWith(Traits.FIELD_HELPER)) {
                    fieldHelperClassNode = icn;
                } else if (icn.getName().endsWith(Traits.STATIC_FIELD_HELPER)) {
                    staticFieldHelperClassNode = icn;
                }
            }
        } else { // pre-compiled trait
            try {
                ClassLoader classLoader = trait.getTypeClass().getClassLoader();
                helperClassNode = ClassHelper.make(classLoader.loadClass(Traits.helperClassName(trait)));
                try {
                    fieldHelperClassNode = ClassHelper.make(classLoader.loadClass(Traits.fieldHelperClassName(trait)));
                    staticFieldHelperClassNode = ClassHelper.make(classLoader.loadClass(Traits.staticFieldHelperClassName(trait)));
                } catch (ClassNotFoundException e) {
                    // field helper(s) may be absent
                }
            } catch (ClassNotFoundException e) {
                throw new GroovyBugError("Couldn't find trait helper classes on compile classpath!", e);
            }
        }

        GenericsType[] typeArguments = trait.getGenericsTypes();
        if (helperClassNode != null) {
            helperClassNode = GenericsUtils.makeClassSafe0(helperClassNode, typeArguments);
        } else { // GROOVY-7909: stub helper
            helperClassNode = new ClassNode(
                Traits.helperClassName(trait),
                ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_SYNTHETIC,
                ClassHelper.OBJECT_TYPE
            ){{
                isPrimaryNode = false;
                setGenericsTypes(typeArguments);
            }};
        }
        if (fieldHelperClassNode != null) {
            fieldHelperClassNode = GenericsUtils.makeClassSafe0(fieldHelperClassNode, typeArguments);
        }

        return new TraitHelpersTuple(helperClassNode, fieldHelperClassNode, staticFieldHelperClassNode);
    }

    /**
     * Returns true if the specified class node is a trait.
     * @param cNode a class node to test
     * @return true if the classnode represents a trait
     */
    public static boolean isTrait(final ClassNode cNode) {
        return cNode != null && isAnnotatedWithTrait(cNode);
    }

    /**
     * Returns true if the specified class is a trait.
     * @param clazz a class to test
     * @return true if the classnode represents a trait
     */
    public static boolean isTrait(final Class<?> clazz) {
        return clazz != null && clazz.getAnnotation(Trait.class) != null;
    }

    /**
     * Returns true if the specified class node is annotated with the {@link Trait} interface.
     * @param cNode a class node
     * @return true if the specified class node is annotated with the {@link Trait} interface.
     */
    public static boolean isAnnotatedWithTrait(final ClassNode cNode) {
        List<AnnotationNode> traitAnn = cNode.getAnnotations(Traits.TRAIT_CLASSNODE);
        return traitAnn != null && !traitAnn.isEmpty();
    }

    /**
     * Indicates whether a method in a trait interface has a default implementation.
     * @param method a method node
     * @return true if the method has a default implementation in the trait
     */
    public static boolean hasDefaultImplementation(final MethodNode method) {
        return !method.getAnnotations(IMPLEMENTED_CLASSNODE).isEmpty();
    }

    /**
     * Indicates whether a method in a trait interface has a default implementation.
     * @param method a method node
     * @return true if the method has a default implementation in the trait
     */
    public static boolean hasDefaultImplementation(final Method method) {
        return method.getAnnotation(Implemented.class)!=null;
    }

    /**
     * Reflection API to indicate whether some method is a bridge method to the default implementation
     * of a trait.
     *
     * @param someMethod a method
     * @return {@code true} if the method bridges to a trait default implementation
     */
    public static boolean isBridgeMethod(Method someMethod) {
        TraitBridge annotation = someMethod.getAnnotation(TraitBridge.class);
        return annotation!=null;
    }

    /**
     * Reflection API to find the method corresponding to the default implementation of a trait, given a bridge method.
     *
     * @param someMethod a method
     * @return null if it is not a method implemented in a trait. If it is, returns the method from the trait class.
     */
    public static Method getBridgeMethodTarget(Method someMethod) {
        TraitBridge annotation = someMethod.getAnnotation(TraitBridge.class);
        if (annotation==null) {
            return null;
        }
        Class<?> aClass = annotation.traitClass();
        String desc = annotation.desc();
        for (Method method : aClass.getDeclaredMethods()) {
            String methodDescriptor = BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameterTypes());
            if (desc.equals(methodDescriptor)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Converts a class implementing some trait into a target class. If the trait is a dynamic proxy and
     * that the target class is assignable to the target object of the proxy, then the target object is
     * returned. Otherwise, falls back to {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#asType(java.lang.Object, Class)}
     * @param self an object to be coerced to some class
     * @param clazz the class to be coerced to
     * @return the object coerced to the target class, or the proxy instance if it is compatible with the target class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAsType(Object self, Class<T> clazz) {
        if (self instanceof GeneratedGroovyProxy) {
            Object proxyTarget = ((GeneratedGroovyProxy)self).getProxyTarget();
            if (clazz.isAssignableFrom(proxyTarget.getClass())) {
                return (T) proxyTarget;
            }
        }
        return DefaultGroovyMethods.asType(self, clazz);
    }

    /**
     * Returns the trait and method names derived from super-trait name scheme
     * or {@code null} if the method name doesn't correspond to a trait method.
     */
    public static String[] decomposeSuperCallName(final String methodName) {
        if (methodName != null) {
            int endIndex = methodName.indexOf(SUPER_TRAIT_METHOD_PREFIX);
            if (endIndex != -1) {
                String tName = methodName.substring(0, endIndex).replace('_', '.').replace("..", "_");
                String fName = methodName.substring(endIndex + SUPER_TRAIT_METHOD_PREFIX.length());
                return new String[]{tName, fName};
            }
        }
        return null;
    }

    /**
     * Collects all interfaces of a class node, but reverses the order of the declaration of direct interfaces
     * of this class node. This is used to make sure a trait implementing A,B where both A and B have the same
     * method will take the method from B (latest), aligning the behavior with categories.
     * @param cNode a class node
     * @param interfaces ordered set of interfaces
     */
    public static LinkedHashSet<ClassNode> collectAllInterfacesReverseOrder(final ClassNode cNode, final LinkedHashSet<ClassNode> interfaces) {
        if (cNode.isInterface()) interfaces.add(cNode);
        ClassNode[] directInterfaces = cNode.getInterfaces();
        for (int i = directInterfaces.length - 1; i >= 0; i -= 1) {
            ClassNode iNode = directInterfaces[i];
            if (cNode.isRedirectNode()) // GROOVY-11012
                iNode = GenericsUtils.parameterizeType(cNode, iNode);
            if (interfaces.add(iNode)) collectAllInterfacesReverseOrder(iNode, interfaces);
        }
        return interfaces;
    }

    /**
     * Collects all the self types that a type should extend or implement, given
     * the traits is implements. Collects from interfaces and superclasses too.
     * @param receiver a class node that may implement a trait
     * @param selfTypes a set where the self types will be put
     * @return the {@code selfTypes} collection
     *
     * @since 2.4.0
     */
    public static LinkedHashSet<ClassNode> collectSelfTypes(final ClassNode receiver, final LinkedHashSet<ClassNode> selfTypes) {
        return collectSelfTypes(receiver, selfTypes, true, true);
    }

    /**
     * Collects all the self types that a type should extend or implement, given
     * the traits is implements.
     * @param receiver a class node that may implement a trait
     * @param selfTypes a set where the self types will be put
     * @param checkInterfaces should the interfaces that the node implements be collected too
     * @param checkSuperClass should we collect from the superclass too
     * @return the {@code selfTypes} collection
     *
     * @since 2.4.0
     */
    public static LinkedHashSet<ClassNode> collectSelfTypes(final ClassNode receiver, final LinkedHashSet<ClassNode> selfTypes, final boolean checkInterfaces, final boolean checkSuperClass) {
        if (Traits.isTrait(receiver)) {
            List<AnnotationNode> annotations = receiver.getAnnotations(SELFTYPE_CLASSNODE);
            for (AnnotationNode annotation : annotations) {
                Expression value = annotation.getMember("value");
                if (value instanceof ClassExpression) {
                    ClassNode selfType = value.getType();
                    if (selfTypes.add(selfType)) {
                        collectSelfTypes(selfType, selfTypes, checkInterfaces, checkSuperClass);
                    }
                } else if (value instanceof ListExpression) {
                    for (Expression expression : ((ListExpression) value).getExpressions()) {
                        if (expression instanceof ClassExpression) {
                            ClassNode selfType = expression.getType();
                            if (selfTypes.add(selfType)) {
                                collectSelfTypes(selfType, selfTypes, checkInterfaces, checkSuperClass);
                            }
                        }
                    }
                }
            }
        }
        if (checkInterfaces) {
            ClassNode[] interfaces = receiver.getInterfaces();
            for (ClassNode interFace : interfaces) {
                if (!selfTypes.contains(interFace)) {
                    collectSelfTypes(interFace, selfTypes, true, checkSuperClass);
                }
            }
        }
        if (checkSuperClass) {
            ClassNode superClass = receiver.getSuperClass();
            if (superClass != null && !ClassHelper.isObjectType(superClass)) {
                collectSelfTypes(superClass, selfTypes, checkInterfaces, true);
            }
        }
        return selfTypes;
    }

    /**
     * Returns the synthetic method name used to dispatch to a super-trait implementation.
     *
     * @param trait the trait declaring the super call
     * @param method the original method name
     * @return the synthetic super-trait method name
     */
    static String getSuperTraitMethodName(ClassNode trait, String method) {
        return trait.getName().replace("_","__").replace('.','_')+SUPER_TRAIT_METHOD_PREFIX+method;
    }

    /**
     * Find all traits associated with the given type.
     *
     * @param cNode the given classnode
     * @return the list of ordered trait classnodes
     */
    public static List<ClassNode> findTraits(final ClassNode cNode) {
        LinkedHashSet<ClassNode> interfaces = new LinkedHashSet<>();
        collectAllInterfacesReverseOrder(cNode, interfaces);
        List<ClassNode> traits = new LinkedList<>();
        for (ClassNode candidate : interfaces) {
            if (isAnnotatedWithTrait(candidate)) {
                traits.add(candidate);
            }
        }
        return traits;
    }

    /**
     * Internal annotation used to indicate which methods in a trait interface have a
     * default implementation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Implemented {}

    /**
     * Internal annotation used to indicate that a method is a bridge method to a trait
     * default implementation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
     public @interface TraitBridge {
        /**
         * @return the trait class
         */
        Class<?> traitClass();

        /**
         * @return The method descriptor of the method from the trait
         */
        String desc();
    }
}

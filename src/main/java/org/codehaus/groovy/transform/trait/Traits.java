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
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of utility methods used to deal with traits.
 *
 * @since 2.3.0
 */
public abstract class Traits {
    public static final ClassNode IMPLEMENTED_CLASSNODE = ClassHelper.make(Implemented.class);
    public static final ClassNode TRAITBRIDGE_CLASSNODE = ClassHelper.make(TraitBridge.class);
    public static final Class<Trait> TRAIT_CLASS = Trait.class;
    public static final ClassNode TRAIT_CLASSNODE = ClassHelper.make(TRAIT_CLASS);
    public static final ClassNode GENERATED_PROXY_CLASSNODE = ClassHelper.make(GeneratedGroovyProxy.class);
    public static final ClassNode SELFTYPE_CLASSNODE = ClassHelper.make(SelfType.class);

    static final String TRAIT_TYPE_NAME = "@" + TRAIT_CLASSNODE.getNameWithoutPackage();
    static final String TRAIT_HELPER = "$Trait$Helper";
    static final String FIELD_HELPER = "$Trait$FieldHelper";
    static final String STATIC_FIELD_HELPER = "$Trait$StaticFieldHelper";
    static final String DIRECT_SETTER_SUFFIX = "$set";
    static final String DIRECT_GETTER_SUFFIX = "$get";
    static final String INIT_METHOD = "$init$";
    static final String STATIC_INIT_METHOD = "$static$init$";
    public static final String THIS_OBJECT = "$self";
    public static final String STATIC_THIS_OBJECT = "$static$self";
    static final String STATIC_FIELD_PREFIX = "$static";
    static final String FIELD_PREFIX = "$ins";
    static final String PUBLIC_FIELD_PREFIX = "$0";
    static final String PRIVATE_FIELD_PREFIX = "$1";
    // TODO decide if we should support VOLATILE
//    def hex(s) {new BigInteger(s, 16).intValue()}
//    def optionals = [[0, 1], [0, 1], [0, 1], [0, 1]].combinations{ a, b, c, d ->
//            (a ? hex('80') : 0) + (b ? hex('10') : 0) + (c ? hex('8') : 0) + (d ? hex('2') : hex('1'))
//    }.sort()
    static final List<Integer> FIELD_PREFIXES = Arrays.asList(1, 2, 9, 10, 17, 18, 25, 26, 129, 130, 137, 138, 145, 146, 153, 154);
    static final int FIELD_PREFIX_MASK = Opcodes.ACC_PRIVATE | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_TRANSIENT;
    static final String SUPER_TRAIT_METHOD_PREFIX = "trait$super$";

    static String fieldHelperClassName(final ClassNode traitNode) {
        return traitNode.getName() + FIELD_HELPER;
    }

    static String staticFieldHelperClassName(final ClassNode traitNode) {
        return traitNode.getName() + STATIC_FIELD_HELPER;
    }

    static String helperGetterName(final FieldNode field) {
        return remappedFieldName(unwrapOwner(field.getOwner()), field.getName()) + DIRECT_GETTER_SUFFIX;
    }

    static String helperSetterName(final FieldNode field) {
        return remappedFieldName(unwrapOwner(field.getOwner()), field.getName()) + DIRECT_SETTER_SUFFIX;
    }

    static String helperClassName(final ClassNode traitNode) {
        return traitNode.getName() + TRAIT_HELPER;
    }

    static String remappedFieldName(final ClassNode traitNode, final String name) {
        return traitNode.getName().replace('.','_')+"__"+name;
    }

    private static ClassNode unwrapOwner(ClassNode owner) {
        if (ClassHelper.CLASS_Type.equals(owner) && owner.getGenericsTypes()!=null && owner.getGenericsTypes().length==1) {
            return owner.getGenericsTypes()[0].getType();
        }
        return owner;
    }

    public static ClassNode findHelper(final ClassNode trait) {
        return findHelpers(trait).getHelper();
    }

    public static ClassNode findFieldHelper(final ClassNode trait) {
        return findHelpers(trait).getFieldHelper();
    }

    public static ClassNode findStaticFieldHelper(final ClassNode trait) {
        return findHelpers(trait).getStaticFieldHelper();
    }

    static TraitHelpersTuple findHelpers(final ClassNode trait) {
        ClassNode helperClassNode = null;
        ClassNode fieldHelperClassNode = null;
        ClassNode staticFieldHelperClassNode = null;
        Iterator<InnerClassNode> innerClasses = trait.redirect().getInnerClasses();
        if (innerClasses != null && innerClasses.hasNext()) {
            // trait defined in same source unit
            while (innerClasses.hasNext()) {
                ClassNode icn = innerClasses.next();
                if (icn.getName().endsWith(Traits.FIELD_HELPER)) {
                    fieldHelperClassNode = icn;
                } else if (icn.getName().endsWith(Traits.STATIC_FIELD_HELPER)) {
                    staticFieldHelperClassNode = icn;
                } else if (icn.getName().endsWith(Traits.TRAIT_HELPER)) {
                    helperClassNode = icn;
                }
            }
        } else {
            // precompiled trait
            try {
                final ClassLoader classLoader = trait.getTypeClass().getClassLoader();
                String helperClassName = Traits.helperClassName(trait);
                helperClassNode = ClassHelper.make(Class.forName(helperClassName, false, classLoader));
                try {
                    fieldHelperClassNode = ClassHelper.make(classLoader.loadClass(Traits.fieldHelperClassName(trait)));
                    staticFieldHelperClassNode = ClassHelper.make(classLoader.loadClass(Traits.staticFieldHelperClassName(trait)));
                } catch (ClassNotFoundException e) {
                    // not a problem, the field helpers may be absent
                }
            } catch (ClassNotFoundException e) {
                throw new GroovyBugError("Couldn't find trait helper classes on compile classpath!",e);
            }
        }
        return new TraitHelpersTuple(helperClassNode,  fieldHelperClassNode, staticFieldHelperClassNode);
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
        return clazz!=null && clazz.getAnnotation(Trait.class)!=null;
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
     * @param someMethod a method node
     * @return null if it is not a method implemented in a trait. If it is, returns the method from the trait class.
     */
    public static boolean isBridgeMethod(Method someMethod) {
        TraitBridge annotation = someMethod.getAnnotation(TraitBridge.class);
        return annotation!=null;
    }

    /**
     * Reflection API to find the method corresponding to the default implementation of a trait, given a bridge method.
     * @param someMethod a method node
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
     * Returns the name of a method without the super trait specific prefix. If the method name
     * doesn't correspond to a super trait method call, the result will be null.
     * @param origName the name of a method
     * @return null if the name doesn't start with the super trait prefix, otherwise the name without the prefix
     */
    public static String[] decomposeSuperCallName(String origName) {
        if (origName.contains(SUPER_TRAIT_METHOD_PREFIX)) {
            int endIndex = origName.indexOf(SUPER_TRAIT_METHOD_PREFIX);
            String tName = origName.substring(0, endIndex).replace('_','.').replace("..","_");
            String fName = origName.substring(endIndex+SUPER_TRAIT_METHOD_PREFIX.length());
            return new String[]{tName, fName};
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
    public static LinkedHashSet<ClassNode> collectAllInterfacesReverseOrder(ClassNode cNode, LinkedHashSet<ClassNode> interfaces) {
        if (cNode.isInterface())
            interfaces.add(cNode);

        ClassNode[] directInterfaces = cNode.getInterfaces();
        for (int i = directInterfaces.length-1; i >=0 ; i--) {
            final ClassNode anInterface = directInterfaces[i];
            interfaces.add(GenericsUtils.parameterizeType(cNode,anInterface));
            collectAllInterfacesReverseOrder(anInterface, interfaces);
        }
        return interfaces;
    }

    /**
     * Collects all the self types that a type should extend or implement, given
     * the traits is implements. Collects from interfaces and superclasses too.
     * @param receiver a class node that may implement a trait
     * @param selfTypes a collection where the list of self types will be written
     * @return the selfTypes collection itself
     * @since 2.4.0
     */
    public static LinkedHashSet<ClassNode> collectSelfTypes(
            ClassNode receiver,
            LinkedHashSet<ClassNode> selfTypes) {
        return collectSelfTypes(receiver, selfTypes, true, true);
    }

    /**
     * Collects all the self types that a type should extend or implement, given
     * the traits is implements.
     * @param receiver a class node that may implement a trait
     * @param selfTypes a collection where the list of self types will be written
     * @param checkInterfaces should the interfaces that the node implements be collected too
     * @param checkSuper should we collect from the superclass too
     * @return the selfTypes collection itself
     * @since 2.4.0
     */
    public static LinkedHashSet<ClassNode> collectSelfTypes(
            ClassNode receiver,
            LinkedHashSet<ClassNode> selfTypes,
            boolean checkInterfaces,
            boolean checkSuper) {
        if (Traits.isTrait(receiver)) {
            List<AnnotationNode> annotations = receiver.getAnnotations(SELFTYPE_CLASSNODE);
            for (AnnotationNode annotation : annotations) {
                Expression value = annotation.getMember("value");
                if (value instanceof ClassExpression) {
                    selfTypes.add(value.getType());
                } else if (value instanceof ListExpression) {
                    List<Expression> expressions = ((ListExpression) value).getExpressions();
                    for (Expression expression : expressions) {
                        if (expression instanceof ClassExpression) {
                            selfTypes.add(expression.getType());
                        }
                    }
                }
            }
        }
        if (checkInterfaces) {
            ClassNode[] interfaces = receiver.getInterfaces();
            for (ClassNode anInterface : interfaces) {
                collectSelfTypes(anInterface, selfTypes, true, checkSuper);
            }
        }

        if (checkSuper) {
            ClassNode superClass = receiver.getSuperClass();
            if (superClass != null) {
                collectSelfTypes(superClass, selfTypes, checkInterfaces, true);
            }
        }
        return selfTypes;
    }

    static String getSuperTraitMethodName(ClassNode trait, String method) {
        return trait.getName().replace("_","__").replace('.','_')+SUPER_TRAIT_METHOD_PREFIX+method;
    }

    /**
     * Find all traits associated with the given classnode
     *
     * @param cNode the given classnode
     * @return the list of ordered trait classnodes
     */
    public static List<ClassNode> findTraits(ClassNode cNode) {
        LinkedHashSet<ClassNode> interfaces = new LinkedHashSet<ClassNode>();
        collectAllInterfacesReverseOrder(cNode, interfaces);
        List<ClassNode> traits = new LinkedList<ClassNode>();
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

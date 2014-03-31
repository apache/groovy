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

import groovy.transform.ForceOverride;
import groovy.transform.Trait;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of utility methods used to deal with traits.
 *
 * @author Cédric Champeau
 * @since 2.3.0
 */
public abstract class Traits {
    public static final ClassNode FORCEOVERRIDE_CLASSNODE = ClassHelper.make(ForceOverride.class);
    public static final ClassNode IMPLEMENTED_CLASSNODE = ClassHelper.make(Implemented.class);
    public static final Class TRAIT_CLASS = Trait.class;
    public static final ClassNode TRAIT_CLASSNODE = ClassHelper.make(TRAIT_CLASS);

    static final String TRAIT_TYPE_NAME = "@" + TRAIT_CLASSNODE.getNameWithoutPackage();
    static final String TRAIT_HELPER = "$Trait$Helper";
    static final String FIELD_HELPER = "$Trait$FieldHelper";
    static final String DIRECT_SETTER_SUFFIX = "$set";
    static final String DIRECT_GETTER_SUFFIX = "$get";
    static final String STATIC_INIT_METHOD = "$init$";
    static final String THIS_OBJECT = "$self";

    static String fieldHelperClassName(final ClassNode traitNode) {
        return traitNode.getName() + FIELD_HELPER;
    }

    static String helperGetterName(final FieldNode field) {
        return field.getName() + DIRECT_GETTER_SUFFIX;
    }

    static String helperSetterName(final FieldNode field) {
        return field.getName() + DIRECT_SETTER_SUFFIX;
    }

    static String helperClassName(final ClassNode traitNode) {
        return traitNode.getName() + TRAIT_HELPER;
    }

    static String remappedFieldName(final ClassNode traitNode, final String name) {
        return traitNode.getName().replace('.','_')+"__"+name;
    }

    static TraitHelpersTuple findHelpers(final ClassNode trait) {
        ClassNode helperClassNode = null;
        ClassNode fieldHelperClassNode = null;
        Iterator<InnerClassNode> innerClasses = trait.redirect().getInnerClasses();
        if (innerClasses != null && innerClasses.hasNext()) {
            // trait defined in same source unit
            while (innerClasses.hasNext()) {
                ClassNode icn = innerClasses.next();
                if (icn.getName().endsWith(Traits.FIELD_HELPER)) {
                    fieldHelperClassNode = icn;
                } else if (icn.getName().endsWith(Traits.TRAIT_HELPER)) {
                    helperClassNode = icn;
                }
            }
        } else {
            // precompiled trait
            try {
                final ClassLoader classLoader = trait.getTypeClass().getClassLoader();
                String helperClassName = Traits.helperClassName(trait);
                helperClassNode = ClassHelper.make(classLoader.loadClass(helperClassName));
                try {
                    fieldHelperClassNode = ClassHelper.make(classLoader.loadClass(Traits.fieldHelperClassName(trait)));
                } catch (ClassNotFoundException e) {
                    // not a problem, the field helper may be absent
                }
            } catch (ClassNotFoundException e) {
                throw new GroovyBugError("Couldn't find trait helper classes on compile classpath!",e);
            }
        }
        return new TraitHelpersTuple(helperClassNode,  fieldHelperClassNode);
    }

    /**
     * Returns true if the specified class node is a trait.
     * @param cNode a class node to test
     * @return true if the classnode represents a trait
     */
    public static boolean isTrait(final ClassNode cNode) {
        return cNode!=null
                && ((cNode.isInterface() && !cNode.getAnnotations(TRAIT_CLASSNODE).isEmpty())
                    || isAnnotatedWithTrait(cNode));
    }

    /**
     * Returns true if the specified class is a trait.
     * @param clazz a class to test
     * @return true if the classnode represents a trait
     */
    public static boolean isTrait(final Class clazz) {
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
     * Returns true if the specified method node is annotated with {@link ForceOverride}
     * @param methodNode a method node
     * @return  true if the specified method node is annotated with {@link ForceOverride}
     */
    public static boolean isForceOverride(final MethodNode methodNode) {
        return !methodNode.getAnnotations(FORCEOVERRIDE_CLASSNODE).isEmpty()
                || !methodNode.getDeclaringClass().getAnnotations(FORCEOVERRIDE_CLASSNODE).isEmpty();
    }

    /**
     * Returns true if the specified method is annotated with {@link ForceOverride}
     * @param methodNode a method
     * @return  true if the specified method is annotated with {@link ForceOverride}
     */
    public static boolean isForceOverride(final Method methodNode) {
        return methodNode.getAnnotation(ForceOverride.class)!=null ||
                methodNode.getDeclaringClass().getAnnotation(ForceOverride.class)!=null ;
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
     * Internal annotation used to indicate which methods in a trait interface have a
     * default implementation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Implemented {}
}

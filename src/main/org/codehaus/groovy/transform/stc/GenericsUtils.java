/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;

import java.util.Set;

/**
 * Utility methods to deal with generic types.
 *
 * @author Cedric Champeau
 */
public class GenericsUtils {
    public static final GenericsType[] EMPTY_GENERICS_ARRAY = new GenericsType[0];

    /**
     * Given a {@link ClassNode}, collects all super classes and interfaces with generic type information.
     * For example, if a class node represents a <pre>LinkedList&lt;String&gt;</pre>, then this method
     * will collect implemented interfaces like <pre>List&lt;String&gt;, Collection&lt;String&gt;</pre>, ...
     * @param classNode the class node for which to collect generic types
     * @param collector a set where to store generic types.
     */
    public static void collectParameterizedClassInfo(ClassNode classNode, ClassNodeCollector collector) {
        if (classNode == null || ClassHelper.OBJECT_TYPE.equals(classNode)) return;
        if (!collector.collect(classNode)) return;
        ClassNode classNodeRedirect = classNode.redirect();
        GenericsType[] redirectGenericTypes = classNodeRedirect.getGenericsTypes();
        ClassNode[] unresolvedInterfaces = classNode.getUnresolvedInterfaces();
        GenericsType[] parameterizedTypes = classNode.getGenericsTypes();
        if (parameterizedTypes == null) parameterizedTypes = EMPTY_GENERICS_ARRAY;
        for (ClassNode unresolvedInterface : unresolvedInterfaces) {
            if (unresolvedInterface.isUsingGenerics()) {
                ClassNode copy = ClassHelper.makeWithoutCaching(unresolvedInterface.getTypeClass(), false);
                GenericsType[] generics = alignGenericTypes(redirectGenericTypes, parameterizedTypes, unresolvedInterface.getGenericsTypes());
                copy.setGenericsTypes(generics);
                collectParameterizedClassInfo(copy, collector);
            }
        }
        ClassNode superClass = classNode.getUnresolvedSuperClass();
        if (superClass != null) {
            if (superClass.isUsingGenerics()) {
                if (redirectGenericTypes != null) {
                    ClassNode copy = ClassHelper.makeWithoutCaching(superClass.getTypeClass(), false);
                    GenericsType[] generics = alignGenericTypes(redirectGenericTypes, parameterizedTypes, superClass.getGenericsTypes());
                    copy.setGenericsTypes(generics);
                    copy.setGenericsPlaceHolder(true);
                    collectParameterizedClassInfo(copy, collector);
                } else {
                    collectParameterizedClassInfo(superClass, collector);
                }
            } else {
                collectParameterizedClassInfo(superClass, collector);
            }
        }
    }

    /**
     * Given a parameterized type and a generic type information, aligns actual type parameters. For example, if a
     * class uses generic type <pre>&lt;T,U,V&gt;</pre> (redirectGenericTypes), is used with actual type parameters
     * <pre>&lt;java.lang.String, U,V&gt;</pre>, then a class or interface using generic types <pre>&lt;T,V&gt;</pre>
     * will be aligned to <pre>&lt;java.lang.String,V&gt;</pre>
     * @param redirectGenericTypes the type arguments or the redirect class node
     * @param parameterizedTypes the actual type arguments used on this class node
     * @param alignmentTarget the generic type arguments to which we want to align to
     * @return aligned type arguments
     */
    public static GenericsType[] alignGenericTypes(final GenericsType[] redirectGenericTypes, final GenericsType[] parameterizedTypes, final GenericsType[] alignmentTarget) {
        if (alignmentTarget==null) return EMPTY_GENERICS_ARRAY;
        if (parameterizedTypes==null) return alignmentTarget;
        GenericsType[] generics = new GenericsType[alignmentTarget.length];
        for (int i = 0, scgtLength = alignmentTarget.length; i < scgtLength; i++) {
            final GenericsType superGenericType = alignmentTarget[i];
            GenericsType match = null;
            if (redirectGenericTypes!=null) {
                for (int j = 0; j < redirectGenericTypes.length && match == null; j++) {
                    GenericsType redirectGenericType = redirectGenericTypes[j];
                    if (redirectGenericType.getName().equals(superGenericType.getName())) {
                        match = parameterizedTypes[j];
                    }
                }
            }
            if (match == null) {
                match = superGenericType;
            }
            generics[i]=match;
        }
        return generics;
    }

    public interface ClassNodeCollector {
        /**
         * Collects a class node.
         * @param node the node to be collected
         * @return true if collection should continue, false if collection should be stopped
         */
        boolean collect(ClassNode node);
    }
}

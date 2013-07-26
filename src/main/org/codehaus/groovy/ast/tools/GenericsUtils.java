/*
 * Copyright 2003-2012 the original author or authors.
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
package org.codehaus.groovy.ast.tools;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods to deal with generic types.
 *
 * @author Cedric Champeau
 */
public class GenericsUtils {
    public static final GenericsType[] EMPTY_GENERICS_ARRAY = new GenericsType[0];

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
        if (parameterizedTypes==null || parameterizedTypes.length==0) return alignmentTarget;
        GenericsType[] generics = new GenericsType[alignmentTarget.length];
        for (int i = 0, scgtLength = alignmentTarget.length; i < scgtLength; i++) {
            final GenericsType currentTarget = alignmentTarget[i];
            GenericsType match = null;
            if (redirectGenericTypes!=null) {
                for (int j = 0; j < redirectGenericTypes.length && match == null; j++) {
                    GenericsType redirectGenericType = redirectGenericTypes[j];
                    if (redirectGenericType.isCompatibleWith(currentTarget.getType())) {
                        if (currentTarget.isPlaceholder() && redirectGenericType.isPlaceholder() && !currentTarget.getName().equals(redirectGenericType.getName())) {
                            // check if there's a potential better match
                            boolean skip = false;
                            for (int k=j+1; k<redirectGenericTypes.length && !skip; k++) {
                                GenericsType ogt = redirectGenericTypes[k];
                                if (ogt.isPlaceholder() && ogt.isCompatibleWith(currentTarget.getType()) && ogt.getName().equals(currentTarget.getName())) {
                                    skip = true;
                                }
                            }
                            if (skip) continue;
                        }
                        match = parameterizedTypes[j];
                        if (currentTarget.isWildcard()) {
                            // if alignment target is a wildcard type
                            // then we must make best effort to return a parameterized
                            // wildcard
                            ClassNode lower = currentTarget.getLowerBound()!=null?match.getType():null;
                            ClassNode[] currentUpper = currentTarget.getUpperBounds();
                            ClassNode[] upper = currentUpper !=null?new ClassNode[currentUpper.length]:null;
                            if (upper!=null) {
                                for (int k = 0; k < upper.length; k++) {
                                    upper[k] = currentUpper[k].isGenericsPlaceHolder()?match.getType():currentUpper[k];
                                }
                            }
                            match = new GenericsType(ClassHelper.makeWithoutCaching("?"), upper, lower);
                            match.setWildcard(true);
                        }
                    }
                }
            }
            if (match == null) {
                match = currentTarget;
            }
            generics[i]=match;
        }
        return generics;
    }

    /**
     * Generates a wildcard generic type in order to be used for checks against class nodes.
     * See {@link GenericsType#isCompatibleWith(org.codehaus.groovy.ast.ClassNode)}.
     * @param types the type to be used as the wildcard upper bound
     * @return a wildcard generics type
     */
    public static GenericsType buildWildcardType(final ClassNode... types) {
        ClassNode base = ClassHelper.makeWithoutCaching("?");
        GenericsType gt = new GenericsType(base, types, null);
        gt.setWildcard(true);
        return gt;
    }

    public static Map<String, GenericsType> extractPlaceholders(ClassNode cn) {
        HashMap<String, GenericsType> ret = new HashMap<String, GenericsType>();
        extractPlaceholders(cn, ret);
        return ret;
    }

    /**
     * For a given classnode, fills in the supplied map with the parameterized
     * types it defines.
     * @param node
     * @param map
     */
    public static void extractPlaceholders(ClassNode node, Map<String, GenericsType> map) {
        if (node == null) return;
        if (!node.isUsingGenerics() || !node.isRedirectNode()) return;
        GenericsType[] parameterized = node.getGenericsTypes();
        if (parameterized == null || parameterized.length == 0) return;
        GenericsType[] redirectGenericsTypes = node.redirect().getGenericsTypes();
        if (redirectGenericsTypes==null) redirectGenericsTypes = parameterized;
        for (int i = 0; i < redirectGenericsTypes.length; i++) {
            GenericsType redirectType = redirectGenericsTypes[i];
            if (redirectType.isPlaceholder()) {
                String name = redirectType.getName();
                if (!map.containsKey(name)) map.put(name, parameterized[i]);
            }
        }
        if (node.isArray()) {
            extractPlaceholders(node.getComponentType(), map);
        }
    }

    /**
     * Interface class nodes retrieved from {@link org.codehaus.groovy.ast.ClassNode#getInterfaces()}
     * or {@link org.codehaus.groovy.ast.ClassNode#getAllInterfaces()} are returned with generic type
     * arguments. This method allows returning a parameterized interface given the parameterized class
     * node which implements this interface.
     * @param hint the class node where generics types are parameterized
     * @param target the interface we want to parameterize generics types
     * @return a parameterized interface class node
     * @deprecated Use #parameterizeType instead
     */
    public static ClassNode parameterizeInterfaceGenerics(final ClassNode hint, final ClassNode target) {
        return parameterizeType(hint, target);
    }

    /**
     * Interface class nodes retrieved from {@link org.codehaus.groovy.ast.ClassNode#getInterfaces()}
     * or {@link org.codehaus.groovy.ast.ClassNode#getAllInterfaces()} are returned with generic type
     * arguments. This method allows returning a parameterized interface given the parameterized class
     * node which implements this interface.
     * @param hint the class node where generics types are parameterized
     * @param target the interface we want to parameterize generics types
     * @return a parameterized interface class node
     */
    public static ClassNode parameterizeType(final ClassNode hint, final ClassNode target) {
        ClassNode interfaceFromClassNode = null;
        if (hint.equals(target)) interfaceFromClassNode = hint;
        if (ClassHelper.OBJECT_TYPE.equals(target) && target.isUsingGenerics() && target.getGenericsTypes()!=null
                && target.getGenericsTypes()[0].isPlaceholder()) {
            // Object<T>
            return ClassHelper.getWrapper(hint);
        }
        if (interfaceFromClassNode==null) {
            ClassNode[] interfaces = hint.getInterfaces();
            for (ClassNode node : interfaces) {
                if (node.equals(target)) {
                    interfaceFromClassNode = node;
                    break;
                } else if (node.implementsInterface(target)) {
                    // ex: classNode = LinkedList<A> , node=List<E> , anInterface = Iterable<T>
                    return parameterizeType(parameterizeType(hint, node), target);
                }
            }
        }
        if (interfaceFromClassNode==null && hint.getUnresolvedSuperClass()!=null) {
            return parameterizeType(hint.getUnresolvedSuperClass(), target);
        }
        if (interfaceFromClassNode==null) {

//            return target;
            interfaceFromClassNode = hint;
        }
        Map<String,GenericsType> parameters = new HashMap<String, GenericsType>();
        extractPlaceholders(hint, parameters);
        ClassNode node = target.getPlainNodeReference();
        GenericsType[] interfaceGTs = interfaceFromClassNode.getGenericsTypes();
        if (interfaceGTs==null) return target;
        GenericsType[] types = new GenericsType[interfaceGTs.length];
        for (int i = 0; i < interfaceGTs.length; i++) {
            GenericsType interfaceGT = interfaceGTs[i];
            types[i] = interfaceGT;
            if (interfaceGT.isPlaceholder()) {
                String name = interfaceGT.getName();
                if (parameters.containsKey(name)) {
                    types[i] = parameters.get(name);
                }
            }
        }
        node.setGenericsTypes(types);
        return node;
    }
}

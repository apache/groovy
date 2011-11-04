/*
 * Copyright 2003-2011 the original author or authors.
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

package org.codehaus.groovy.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to describe generic type signatures for ClassNodes.
 *
 * @author Jochen Theodorou
 * @see ClassNode
 */
public class GenericsType extends ASTNode {
    private final ClassNode[] upperBounds;
    private final ClassNode lowerBound;
    private ClassNode type;
    private String name;
    private boolean placeholder;
    private boolean resolved;
    private boolean wildcard;

    public GenericsType(ClassNode type, ClassNode[] upperBounds, ClassNode lowerBound) {
        this.type = type;
        this.name = type.isGenericsPlaceHolder() ? type.getUnresolvedName() : type.getName();
        this.upperBounds = upperBounds;
        this.lowerBound = lowerBound;
        placeholder = type.isGenericsPlaceHolder();
        resolved = false;
    }

    public GenericsType(ClassNode basicType) {
        this(basicType, null, null);
    }

    public ClassNode getType() {
        return type;
    }

    public void setType(ClassNode type) {
        this.type = type;
    }

    public String toString() {
        Set<Integer> visited = new HashSet<Integer>();
        return toString(visited);
    }
    private String toString(Set<Integer> visited) {
        String ret = (type == null || placeholder || wildcard) ? name : genericsBounds(type, visited);
        if (upperBounds != null) {
            ret += " extends ";
            for (int i = 0; i < upperBounds.length; i++) {
                ret += genericsBounds(upperBounds[i], visited);
                if (i + 1 < upperBounds.length) ret += " & ";
            }
        } else if (lowerBound != null) {
            ret += " super " + genericsBounds(lowerBound, visited);
        }
        return ret;
    }

    private String genericsBounds(ClassNode theType, Set<Integer> visited) {
        if (visited.contains(System.identityHashCode(theType))) {
            return "...";
        }
        visited.add(System.identityHashCode(theType));
        String ret = theType.getName();
        GenericsType[] genericsTypes = theType.getGenericsTypes();
        if (genericsTypes == null || genericsTypes.length == 0) return ret;
        ret += "<";
        for (int i = 0; i < genericsTypes.length; i++) {
            if (i != 0) ret += ", ";
            ret += genericsTypes[i].toString(visited);
        }
        ret += ">";
        return ret;
    }

    public ClassNode[] getUpperBounds() {
        return upperBounds;
    }

    public String getName() {
        return name;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
        type.setGenericsPlaceHolder(placeholder);
    }

    public boolean isResolved() {
        return resolved || placeholder;
    }

    public void setResolved(boolean res) {
        resolved = res;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }

    public ClassNode getLowerBound() {
        return lowerBound;
    }

    /**
     * Tells if the provided class node is compatible with this generic type definition
     * @param classNode the class node to be checked
     * @return true if the class node is compatible with this generics type definition
     */
    public boolean isCompatibleWith(ClassNode classNode) {
        return new GenericsTypeMatcher().matches(classNode);
    }

    /**
     * Implements generics type comparison.
     */
    private class GenericsTypeMatcher {

        /**
         * Compares this generics type with the one represented by the provided class node. If the provided
         * classnode is compatible with the generics specification, returns true. Otherwise, returns false.
         * The check is complete, meaning that we also check "nested" generics.
         * @param classNode the classnode to be checked
         * @return true iff the classnode is compatible with this generics specification
         */
        public boolean matches(ClassNode classNode) {
            if (classNode.isGenericsPlaceHolder()) {
                // if the classnode we compare to is a generics placeholder (like <E>) then we
                // only need to check that the names are equal
                GenericsType[] genericsTypes = classNode.getGenericsTypes();
                return genericsTypes==null||genericsTypes[0].getName().equals(name);
            }
            if (wildcard || placeholder) {
                // if the current generics spec is a wildcard spec or a placeholder spec
                // then we must check upper and lower bounds
                if (upperBounds != null) {
                    // check that the provided classnode is a subclass of all provided upper bounds
                    boolean upIsOk = true;
                    for (int i = 0, upperBoundsLength = upperBounds.length; i < upperBoundsLength && upIsOk; i++) {
                        final ClassNode upperBound = upperBounds[i];
                        upIsOk = (classNode.isDerivedFrom(upperBound) ||
                                (upperBound.isInterface() && classNode.implementsInterface(upperBound)));
                    }
                    // if the provided classnode is a subclass of the upper bound
                    // then check that the generic types supplied by the class node are compatible with
                    // this generics specification
                    // for example, we could have the spec saying List<String> but provided classnode
                    // saying List<Integer>
                    upIsOk = upIsOk && checkGenerics(classNode);
                    return upIsOk;
                }
                if (lowerBound != null) {
                    // if a lower bound is declared, then we must perform the same checks that for an upper bound
                    // but with reversed arguments
                    return (lowerBound.isDerivedFrom(classNode) ||
                            (classNode.isInterface() && lowerBound.implementsInterface(lowerBound))) && checkGenerics(classNode);
                }
            }
            // if this is not a generics placeholder, first compare that types represent the same type
            if ((type!=null && !type.equals(classNode))) {
                return false;
            }
            // last, we could have the spec saying List<String> and a classnode saying List<Integer> so
            // we must check that generics are compatible.
            // The null check is normally not required but done to prevent from NPEs
            return type == null || compareGenericsWithBound(classNode, type);
        }

        /**
         * Iterates over each generics bound of this generics specification, and checks
         * that the generics defined by the bound are compatible with the generics specified
         * by the type.
         * @param classNode the classnode the bounds should be compared with
         * @return true if generics from bounds are compatible
         */
        private boolean checkGenerics(final ClassNode classNode) {
            if (upperBounds!=null) {
                for (ClassNode upperBound : upperBounds) {
                    if (!compareGenericsWithBound(classNode, upperBound)) return false;
                }
            }
            if (lowerBound!=null) {
                if (!lowerBound.redirect().isUsingGenerics()) {
                    if (!compareGenericsWithBound(classNode, lowerBound)) return false;
                }
            }
            return true;
        }

        /**
         * Given a parameterized type (List&lt;String&gt; for example), checks that its
         * generic types are compatible with those from a bound.
         * @param classNode the classnode from which we will compare generics types
         * @param bound the bound to which the types will be compared
         * @return true if generics are compatible
         */
        private boolean compareGenericsWithBound(final ClassNode classNode, final ClassNode bound) {
            if (classNode==null) return false;
            if (!bound.isUsingGenerics()) {
                // if the bound is not using generics, there's nothing to compare with
                return true;
            }
            if (!classNode.equals(bound)) {
                 // the class nodes are on different types
                // in this situation, we must choose the correct execution path : either the bound
                // is an interface and we must find the implementing interface from the classnode
                // to compare their parameterized generics, or the bound is a regular class and we
                // must compare the bound with a superclass
                if (bound.isInterface()) {
                    if (classNode.isInterface()) {
                        return compareGenericsWithBound(classNode.getUnresolvedSuperClass(), bound);
                    }
                    Set<ClassNode> interfaces = classNode.getAllInterfaces();
                    // iterate over all interfaces to check if any corresponds to the bound we are
                    // comparing to
                    for (ClassNode anInterface : interfaces) {
                        if (anInterface.equals(bound)) {
                            // when we obtain an interface, the types represented by the interface
                            // class node are not parameterized. This means that we must create a
                            // new class node with the parameterized types that the current class node
                            // has defined.
                            Map<String,ClassNode> parameters = new HashMap<String, ClassNode>();
                            collectParameter(classNode, parameters);
                            ClassNode node = ClassHelper.makeWithoutCaching(anInterface.getTypeClass(), false);
                            GenericsType[] interfaceGTs = anInterface.getGenericsTypes();
                            GenericsType[] types = new GenericsType[interfaceGTs.length];
                            for (int i = 0; i < interfaceGTs.length; i++) {
                                GenericsType interfaceGT = interfaceGTs[i];
                                types[i] = interfaceGT;
                                if (interfaceGT.isPlaceholder()) {
                                    String name = interfaceGT.getName();
                                    if (parameters.containsKey(name)) {
                                        types[i] = new GenericsType(parameters.get(name));
                                    }
                                }
                            }
                            node.setGenericsTypes(types);
                            return compareGenericsWithBound(node, bound);
                        }
                    }
                }
                return compareGenericsWithBound(classNode.getUnresolvedSuperClass(), bound);
            }
            GenericsType[] cnTypes = classNode.getGenericsTypes();
            GenericsType[] uBTypes = bound.getGenericsTypes();
            Map<String, ClassNode> resolvedPlaceholders = placeholderToParameterizedType();
            boolean match = true;
            for (int i = 0; i < uBTypes.length && match; i++) {
                GenericsType uBType = uBTypes[i];
                GenericsType cnType = cnTypes[i];
                if (cnType.isPlaceholder()) {
                    String name = cnType.getName();
                    if (resolvedPlaceholders.containsKey(name)) cnType=new GenericsType(resolvedPlaceholders.get(name));
                }
                match = uBType.isWildcard() || cnType.isCompatibleWith(uBType.getType());
            }
            if (!match) return false;
            return true;
        }

        /**
         * Iterates through the type, its upper and lower bounds, and returns a map
         * which has for key a placeholder name, and as a value the corresponding
         * parameterized type. For example, E -> java.lang.String
         * @return
         */
        private Map<String,ClassNode> placeholderToParameterizedType() {
            Map<String, ClassNode> result = new HashMap<String, ClassNode>();
            collectParameter(type, result);
            if (upperBounds!=null) {
                for (ClassNode upperBound : upperBounds) {
                    collectParameter(upperBound, result);
                }
            }
            if (lowerBound!=null) {
                collectParameter(lowerBound, result);
            }
            return result;
        }

        /**
         * For a given classnode, fills in the supplied map with the parameterized
         * types it defines.
         * @param node
         * @param map
         */
        private void collectParameter(ClassNode node, Map<String, ClassNode> map) {
            if (node == null) return;
            if (!node.isUsingGenerics() || !node.isRedirectNode()) return;
            GenericsType[] parameterized = node.getGenericsTypes();
            if (parameterized == null) return;
            GenericsType[] genericsTypes = node.redirect().getGenericsTypes();
            for (int i = 0; i < genericsTypes.length; i++) {
                GenericsType genericsType = genericsTypes[i];
                if (genericsType.isPlaceholder()) {
                    String name = genericsType.getName();
                    map.put(name, parameterized[i].getType());
                }
            }
        }
    }
}

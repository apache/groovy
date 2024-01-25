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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.ast.tools.WideningCategories;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf;

/**
 * This class is used to describe generic type signatures for ClassNodes.
 *
 * @see ClassNode
 */
public class GenericsType extends ASTNode {
    public static final GenericsType[] EMPTY_ARRAY = new GenericsType[0];

    private String name;
    private ClassNode type;
    private final ClassNode lowerBound;
    private final ClassNode[] upperBounds;
    private boolean placeholder, resolved, wildcard;

    public GenericsType(final ClassNode type, final ClassNode[] upperBounds, final ClassNode lowerBound) {
        setType(type);
        this.lowerBound = lowerBound;
        this.upperBounds = upperBounds;
        this.placeholder = type.isGenericsPlaceHolder();
        setName(placeholder ? type.getUnresolvedName() : type.getName());
    }

    public GenericsType(final ClassNode basicType) {
        this(basicType, null, null);
    }

    public ClassNode getType() {
        return type;
    }

    public void setType(final ClassNode type) {
        this.type = Objects.requireNonNull(type); // TODO: ensure type is not primitive
    }

    @Override
    public String toString() {
        return toString(this, new HashSet<>());
    }

    private static String toString(final GenericsType gt, final Set<String> visited) {
        String name = gt.getName();
        ClassNode type = gt.getType();
        boolean wildcard = gt.isWildcard();
        boolean placeholder = gt.isPlaceholder();
        ClassNode lowerBound = gt.getLowerBound();
        ClassNode[] upperBounds = gt.getUpperBounds();

        if (placeholder) visited.add(name);

        StringBuilder ret = new StringBuilder(wildcard || placeholder ? name : genericsBounds(type, visited));
        if (lowerBound != null) {
            ret.append(" super ").append(genericsBounds(lowerBound, visited));
        } else if (upperBounds != null
                // T extends Object should just be printed as T
                && !(placeholder && upperBounds.length == 1 && !upperBounds[0].isGenericsPlaceHolder() && upperBounds[0].getName().equals(ClassHelper.OBJECT))) {
            ret.append(" extends ");
            for (int i = 0, n = upperBounds.length; i < n; i += 1) {
                if (i != 0) ret.append(" & ");
                ret.append(genericsBounds(upperBounds[i], visited));
            }
        }
        return ret.toString();
    }

    private static String genericsBounds(final ClassNode theType, final Set<String> visited) {
        if (theType instanceof WideningCategories.LowestUpperBoundClassNode) {
            var ret = new java.util.StringJoiner(" & ");
            for (ClassNode type : theType.asGenericsType().getUpperBounds()) {
                ret.add(genericsBounds(type, visited));
            }
            return ret.toString();
        }

        StringBuilder ret = appendName(theType, new StringBuilder());
        GenericsType[] genericsTypes = theType.getGenericsTypes();
        if (genericsTypes != null && genericsTypes.length > 0
                && !theType.isGenericsPlaceHolder()) { // GROOVY-10583
            ret.append('<');
            for (int i = 0, n = genericsTypes.length; i < n; i += 1) {
                if (i != 0) ret.append(", ");
                GenericsType type = genericsTypes[i];
                if (type.isPlaceholder() && visited.contains(type.getName())) {
                    ret.append(type.getName());
                } else {
                    ret.append(toString(type, visited));
                }
            }
            ret.append('>');
        }
        return ret.toString();
    }

    private static StringBuilder appendName(final ClassNode theType, final StringBuilder sb) {
        if (theType.isArray()) {
            appendName(theType.getComponentType(), sb).append("[]");
        } else if (theType.isGenericsPlaceHolder()) {
            sb.append(theType.getUnresolvedName());
        } else if (theType.getOuterClass() != null) {
            String parentClassNodeName = theType.getOuterClass().getName();
            if (Modifier.isStatic(theType.getModifiers()) || theType.isInterface()) {
                sb.append(parentClassNodeName);
            } else {
                ClassNode outerClass = theType.getNodeMetaData("outer.class");
                if (outerClass == null) outerClass = theType.getOuterClass();
                sb.append(genericsBounds(outerClass, new HashSet<>()));
            }
            sb.append('.');
            sb.append(theType.getName(), parentClassNodeName.length() + 1, theType.getName().length());
        } else {
            sb.append(theType.getName());
        }
        return sb;
    }

    public String getName() {
        return (isWildcard() ? "?" : name);
    }

    public void setName(final String name) {
        this.name = Objects.requireNonNull(name);
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(final boolean resolved) {
        this.resolved = resolved;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(final boolean placeholder) {
        this.placeholder = placeholder;
        this.resolved = resolved || placeholder;
        this.wildcard = wildcard && !placeholder;
        getType().setGenericsPlaceHolder(placeholder);
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public void setWildcard(final boolean wildcard) {
        this.wildcard = wildcard;
        this.placeholder = placeholder && !wildcard;
    }

    public ClassNode getLowerBound() {
        return lowerBound;
    }

    public ClassNode[] getUpperBounds() {
        return upperBounds;
    }

    //--------------------------------------------------------------------------

    /**
     * Determines if the provided type is compatible with this specification.
     * The check is complete, meaning that nested generics are also checked.
     */
    public boolean isCompatibleWith(final ClassNode classNode) {
        GenericsType[] genericsTypes = classNode.getGenericsTypes();
        if (genericsTypes != null && genericsTypes.length == 0) {
            return true; // diamond always matches
        }
        if (classNode.isGenericsPlaceHolder()) {
            if (genericsTypes == null) {
                return true;
            }
            String name = genericsTypes[0].getName();
            if (!isWildcard()) {
                return name.equals(getName());
            }
            if (getLowerBound() != null) {
                // check for "? super T" vs "T"
                ClassNode lowerBound = getLowerBound();
                if (name.equals(lowerBound.getUnresolvedName())) {
                    return true;
                }
            } else if (getUpperBounds() != null) {
                // check for "? extends T & I" vs "T" or "I"
                for (ClassNode upperBound : getUpperBounds()) {
                    if (name.equals(upperBound.getUnresolvedName())) {
                        return true;
                    }
                }
            }
            // check for "? extends/super X" vs "T extends/super X"
            return checkGenerics(classNode);
        }
        if (isWildcard() || isPlaceholder()) {
            ClassNode lowerBound = getLowerBound();
            if (lowerBound != null) {
                // test bound and type in reverse for lower bound vs upper bound
                if (!implementsInterfaceOrIsSubclassOf(lowerBound, classNode)) {
                    return false;
                }
                return checkGenerics(classNode);
            }
            ClassNode[] upperBounds = getUpperBounds();
            if (upperBounds != null) {
                // check that provided type extends or implements all upper bounds
                for (ClassNode upperBound : upperBounds) {
                    if (!implementsInterfaceOrIsSubclassOf(classNode, upperBound)) {
                        return false;
                    }
                }
                // if the provided type is a subclass of the upper bound(s) then
                // check that the generic types supplied are compatible with this
                // for example, this spec could be "Type<X>" but type is "Type<Y>"
                return checkGenerics(classNode);
            }
            // if there are no bounds, the generic type is basically Object and everything is compatible
            return true;
        }
        // not placeholder or wildcard; no covariance allowed for type or bound(s)
        return classNode.equals(getType()) && compareGenericsWithBound(classNode, getType());
    }

    /**
     * Compares the bounds of this generics specification against the given type
     * for compatibility.  Ex: String would satisfy &lt;? extends CharSequence>.
     */
    private boolean checkGenerics(final ClassNode classNode) {
        ClassNode lowerBound = getLowerBound();
        if (lowerBound != null) {
            return compareGenericsWithBound(classNode, lowerBound);
        }
        ClassNode[] upperBounds = getUpperBounds();
        if (upperBounds != null) {
            for (ClassNode upperBound : upperBounds) {
                if (!compareGenericsWithBound(classNode, upperBound)) {
                    return false;
                }
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
    private static boolean compareGenericsWithBound(final ClassNode classNode, final ClassNode bound) {
        if (classNode == null) return false;
        if (bound.getGenericsTypes() == null
                || classNode.isGenericsPlaceHolder() // GROOVY-10556: "T" vs "C<T extends C<?>>" bound
                || (classNode.getGenericsTypes() == null && classNode.redirect().getGenericsTypes() != null))
            // if the bound is not using generics or the class node is a raw type, there's nothing to compare
            return true;

        if (!classNode.equals(bound)) {
             // the class nodes are on different types
            // in this situation, we must choose the correct execution path : either the bound
            // is an interface and we must find the implementing interface from the classnode
            // to compare their parameterized generics, or the bound is a regular class and we
            // must compare the bound with a superclass
            if (bound.isInterface()) {
                // iterate over all interfaces to check if any corresponds to the bound we are
                // comparing to
                for (ClassNode face : classNode.getAllInterfaces()) {
                    if (face.equals(bound)) {
                        // when we obtain an interface, the types represented by the interface
                        // class node are not parameterized. This means that we must create a
                        // new class node with the parameterized types that the current class node
                        // has defined.
                        if (face.getGenericsTypes() != null) {
                            face = GenericsUtils.parameterizeType(classNode, face);
                        }
                        return compareGenericsWithBound(face, bound);
                    }
                }
            }
            if (bound instanceof WideningCategories.LowestUpperBoundClassNode) {
                // another special case here, where the bound is a "virtual" type
                // we must then check the superclass and the interfaces
                if (compareGenericsWithBound(classNode, bound.getSuperClass())
                        && Arrays.stream(bound.getInterfaces()).allMatch(face -> compareGenericsWithBound(classNode, face))) {
                    return true;
                }
            }
            if (isObjectType(classNode)) {
                return false;
            }
            ClassNode superClass = classNode.getUnresolvedSuperClass();
            if (superClass == null) {
                superClass = ClassHelper.OBJECT_TYPE;
            } else if (superClass.getGenericsTypes() != null) {
                superClass = GenericsUtils.parameterizeType(classNode, superClass);
            }
            return compareGenericsWithBound(superClass, bound);
        }

        GenericsType[] cnTypes = classNode.getGenericsTypes();
        if (cnTypes == null) {
            cnTypes = classNode.redirect().getGenericsTypes();
        }
        if (cnTypes == null) {
            // may happen if generic type is Foo<T extends Foo> and ClassNode is Foo -> Foo
            return true;
        }

        GenericsType[] redirectBoundGenericTypes = bound.redirect().getGenericsTypes();
        Map<GenericsTypeName, GenericsType> boundPlaceHolders = GenericsUtils.extractPlaceholders(bound);
        Map<GenericsTypeName, GenericsType> classNodePlaceholders = GenericsUtils.extractPlaceholders(classNode);
        boolean match = true;
        for (int i = 0; redirectBoundGenericTypes != null && i < redirectBoundGenericTypes.length && match; i += 1) {
            GenericsType redirectBoundType = redirectBoundGenericTypes[i];
            GenericsType classNodeType = cnTypes[i];
            if (classNodeType.isPlaceholder()) {
                GenericsTypeName name = new GenericsTypeName(classNodeType.getName());
                if (redirectBoundType.isPlaceholder()) {
                    GenericsTypeName gtn = new GenericsTypeName(redirectBoundType.getName());
                    match = name.equals(gtn)
                            || name.equals(new GenericsTypeName("#" + redirectBoundType.getName()));
                    if (!match) {
                        GenericsType boundGenericsType = boundPlaceHolders.get(gtn);
                        if (boundGenericsType != null) {
                            if (boundGenericsType.isPlaceholder()) {
                                match = true;
                            } else if (boundGenericsType.isWildcard()) {
                                if (boundGenericsType.getUpperBounds() != null) { // ? supports single bound only
                                    match = classNodeType.isCompatibleWith(boundGenericsType.getUpperBounds()[0]);
                                } else if (boundGenericsType.getLowerBound() != null) {
                                    match = classNodeType.isCompatibleWith(boundGenericsType.getLowerBound());
                                } else {
                                    match = true;
                                }
                            }
                        }
                    }
                } else {
                    match = classNodePlaceholders.getOrDefault(name, classNodeType).isCompatibleWith(redirectBoundType.getType());
                }
            } else {
                if (redirectBoundType.isPlaceholder()) {
                    if (classNodeType.isPlaceholder()) {
                        match = classNodeType.getName().equals(redirectBoundType.getName());
                    } else {
                        GenericsTypeName name = new GenericsTypeName(redirectBoundType.getName());
                        if (boundPlaceHolders.containsKey(name)) {
                            redirectBoundType = boundPlaceHolders.get(name);
                            if (redirectBoundType.isPlaceholder()) {
                                redirectBoundType = classNodePlaceholders.getOrDefault(name, redirectBoundType);

                            } else if (redirectBoundType.isWildcard()) {
                                if (redirectBoundType.getLowerBound() != null) {
                                    // ex: class Comparable<Integer> <=> bound Comparable<? super T>
                                    GenericsType gt = new GenericsType(redirectBoundType.getLowerBound());
                                    if (gt.isPlaceholder()) {
                                        // check for recursive generic typedef, like in <T extends Comparable<? super T>>
                                        gt = classNodePlaceholders.getOrDefault(new GenericsTypeName(gt.getName()), gt);
                                    }
                                    // GROOVY-6095, GROOVY-9338
                                    if (classNodeType.isWildcard()) {
                                        if (classNodeType.getLowerBound() != null
                                                || classNodeType.getUpperBounds() != null) {
                                            match = classNodeType.checkGenerics(gt.getType());
                                        } else {
                                            match = false; // "?" (from Comparable<?>) does not satisfy anything
                                        }
                                    } else {
                                        match = implementsInterfaceOrIsSubclassOf(gt.getType(), classNodeType.getType());
                                    }
                                } else if (redirectBoundType.getUpperBounds() != null) {
                                    // ex: class Comparable<Integer> <=> bound Comparable<? extends T & I>
                                    for (ClassNode upperBound : redirectBoundType.getUpperBounds()) {
                                        GenericsType gt = new GenericsType(upperBound);
                                        if (gt.isPlaceholder()) {
                                            // check for recursive generic typedef, like in <T extends Comparable<? super T>>
                                            gt = classNodePlaceholders.getOrDefault(new GenericsTypeName(gt.getName()), gt);
                                        }
                                        // GROOVY-6095, GROOVY-9338
                                        if (classNodeType.isWildcard()) {
                                            if (classNodeType.getLowerBound() != null) {
                                                match = gt.checkGenerics(classNodeType.getLowerBound());
                                            } else if (classNodeType.getUpperBounds() != null) {
                                                match = gt.checkGenerics(classNodeType.getUpperBounds()[0]);
                                            } else { // GROOVY-10576: "?" vs "? extends Object" (citation required) or no match
                                                match = (!gt.isPlaceholder() && !gt.isWildcard() && isObjectType(gt.getType()));
                                            }
                                        } else {
                                            match = implementsInterfaceOrIsSubclassOf(classNodeType.getType(), gt.getType());
                                        }
                                        if (!match) break;
                                    }
                                }
                                continue; // GROOVY-10010
                            }
                        }
                        match = redirectBoundType.isCompatibleWith(classNodeType.getType());
                    }
                } else {
                    // TODO: the check for isWildcard should be replaced with a more complete check
                    match = redirectBoundType.isWildcard() || classNodeType.isCompatibleWith(redirectBoundType.getType());
                }
            }
        }
        return match;
    }

    /**
     * Represents {@link GenericsType} name.
     * <p>
     * TODO: In order to distinguish GenericsType with same name, we should add a property to keep the declaring class.
     * <ol>
     * <li> change the signature of constructor GenericsTypeName to `GenericsTypeName(String name, ClassNode declaringClass)`
     * <li> try to fix all compilation errors(if `GenericsType` has declaringClass property, the step would be a bit easy to fix...)
     * <li> run all tests to see whether the change breaks anything
     * <li> if all tests pass, congratulations! but if some tests are broken, try to debug and find why...
     * </ol>
     * We should find a way to set declaring class for `GenericsType` first, it can be completed at the resolving phase.
     */
    public static class GenericsTypeName {
        private final String name;

        public GenericsTypeName(final String name) {
            this.name = Objects.requireNonNull(name);
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object that) {
            if (this == that) return true;
            if (!(that instanceof GenericsTypeName)) return false;
            return getName().equals(((GenericsTypeName) that).getName());
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}

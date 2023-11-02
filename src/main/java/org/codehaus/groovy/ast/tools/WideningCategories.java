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
package org.codehaus.groovy.ast.tools;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static java.util.stream.IntStream.range;
import static org.codehaus.groovy.ast.ClassHelper.BigDecimal_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.BigInteger_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Number_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getNextSuperClass;
import static org.codehaus.groovy.ast.ClassHelper.getUnwrapper;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isNumberType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;

/**
 * This class provides helper methods to determine the type from a widening
 * operation for example for a plus operation.
 * <p>
 * To determine the resulting type of for example a=exp1+exp2 we look at the
 * conditions {@link #isIntCategory(ClassNode)}, {@link #isLongCategory(ClassNode)},
 * {@link #isBigIntCategory(ClassNode)}, {@link #isDoubleCategory(ClassNode)} and
 * {@link #isBigDecCategory(ClassNode)} in that order. The first case applying to
 * exp1 and exp2 is defining the result type of the expression.
 * <p>
 * If for example you look at x = 1 + 2l we have the first category applying to
 * the number 1 being int, since the 1 is an int. The 2l is a long, therefore the
 * int category will not apply and the result type can't be int. The next category
 * in the list is long, and since both apply to long, the result type is a long.
 */
public class WideningCategories {

    private static final Map<ClassNode, Integer> NUMBER_TYPES_PRECEDENCE = org.apache.groovy.util.Maps.of(
        double_TYPE, 0,
        float_TYPE,  1,
        long_TYPE,   2,
        int_TYPE,    3,
        short_TYPE,  4,
        byte_TYPE,   5
    );

    /**
     * Checks if type is an int.
     *
     * @since 2.0.0
     */
    public static boolean isInt(final ClassNode type) {
        return int_TYPE == type;
    }

    /**
     * Checks if type is a float.
     *
     * @since 2.0.0
     */
    public static boolean isFloat(final ClassNode type) {
        return float_TYPE == type;
    }

    /**
     * Checks if type is a double.
     *
     * @since 2.0.0
     */
    public static boolean isDouble(final ClassNode type) {
        return double_TYPE == type;
    }

    /**
     * Checks if type is an int, byte, char or short.
     *
     * @since 2.0.0
     */
    public static boolean isIntCategory(final ClassNode type) {
        return int_TYPE == type || byte_TYPE == type || char_TYPE == type || short_TYPE == type;
    }

    /**
     * Checks if type is a long, int, byte, char or short.
     *
     * @since 2.0.0
     */
    public static boolean isLongCategory(final ClassNode type) {
        return long_TYPE == type || isIntCategory(type);
    }

    /**
     * Checks if type is a BigInteger, long, int, byte, char or short.
     *
     * @since 2.0.0
     */
    public static boolean isBigIntCategory(final ClassNode type) {
        return BigInteger_TYPE == type || isLongCategory(type);
    }

    /**
     * Checks if type is a BigDecimal, BigInteger, long, int, byte, char or short.
     *
     * @since 2.0.0
     */
    public static boolean isBigDecCategory(final ClassNode type) {
        return BigDecimal_TYPE == type || isBigIntCategory(type);
    }

    /**
     * Checks if type is a float, double or BigDecimal (category).
     *
     * @since 2.0.0
     */
    public static boolean isDoubleCategory(final ClassNode type) {
        return float_TYPE == type || double_TYPE == type || isBigDecCategory(type);
    }

    /**
     * Checks if type is a float or double.
     *
     * @since 2.0.0
     */
    public static boolean isFloatingCategory(final ClassNode type) {
        return float_TYPE == type || double_TYPE == type;
    }

    /**
     * Checks if type is a BigDecimal (category) or Number.
     *
     * @since 2.0.0
     */
    public static boolean isNumberCategory(final ClassNode type) {
        return isBigDecCategory(type) || type.isDerivedFrom(Number_TYPE);
    }

    /**
     * Given a list of types, returns the first common supertype. For example,
     * Double and Float would return Number, while Set and String would return
     * Object.
     *
     * @since 2.0.0
     */
    public static ClassNode lowestUpperBound(final List<ClassNode> nodes) {
        int n = nodes.size();
        if (n == 1) return nodes.get(0);
        if (n == 2) return lowestUpperBound(nodes.get(0), nodes.get(1));
        return lowestUpperBound(nodes.get(0), lowestUpperBound(nodes.subList(1, n)));
    }

    /**
     * Given two types, returns the first common supertype, or the class itself
     * if there are equal. For example, Double and Float would return Number,
     * while Set and String would return Object.
     * <p>
     * This method is not guaranteed to return a class node which corresponds to a
     * real type. For example, if two types have more than one interface in common
     * and are not in the same hierarchy branch, then the returned type will be a
     * virtual type implementing all those interfaces.
     * <p>
     * Calls to this method are supposed to be made with resolved generics. This means
     * that you can have wildcards, but no placeholder.
     *
     * @since 2.0.0
     */
    public static ClassNode lowestUpperBound(final ClassNode a, final ClassNode b) {
        ClassNode lub = lowestUpperBound(a, b, null, null);
        if (lub == null || !lub.isUsingGenerics()
                || lub.isGenericsPlaceHolder()) { // GROOVY-10330
            return lub;
        }
        // types may be parameterized; if so, ensure that generic type arguments
        // are made compatible
        if (lub instanceof LowestUpperBoundClassNode) {
            ClassNode superClass = lub.getUnresolvedSuperClass();
            if (superClass.redirect().getGenericsTypes() != null) {
                superClass = parameterizeLowestUpperBound(superClass, a, b, lub);
            }
            ClassNode[] interfaces = lub.getInterfaces().clone();
            for (int i = 0, n = interfaces.length; i < n; i += 1) {
                ClassNode icn = interfaces[i];
                if (icn.redirect().getGenericsTypes() != null) {
                    interfaces[i] = parameterizeLowestUpperBound(icn, a, b, lub);
                }
            }
            return new LowestUpperBoundClassNode(((LowestUpperBoundClassNode)lub).name, superClass, interfaces);
        } else {
            return parameterizeLowestUpperBound(lub, a, b, lub);
        }
    }

    /**
     * Given a lowest upper bound computed without generic type information but which requires to be parameterized
     * and the two implementing classnodes which are parameterized with potentially two different types, returns
     * a parameterized lowest upper bound.
     *
     * For example, if LUB is Set&lt;T&gt; and a is Set&lt;String&gt; and b is Set&lt;StringBuffer&gt;, this
     * will return a LUB which parameterized type matches Set&lt;? extends CharSequence&gt;
     * @param lub the type to be parameterized
     * @param a parameterized type a
     * @param b parameterized type b
     * @param fallback if we detect a recursive call, use this LUB as the parameterized type instead of computing a value
     * @return the class node representing the parameterized lowest upper bound
     */
    private static ClassNode parameterizeLowestUpperBound(final ClassNode lub, final ClassNode a, final ClassNode b, final ClassNode fallback) {
        if (!lub.isUsingGenerics()) return lub;
        // a common super type exists, all we have to do is to parameterize
        // it according to the types provided by the two class nodes
        ClassNode holderForA = findGenericsTypeHolderForClass(a, lub);
        ClassNode holderForB = findGenericsTypeHolderForClass(b, lub);
        // let's compare their generics
        GenericsType[] agt = holderForA == null ? null : holderForA.getGenericsTypes();
        GenericsType[] bgt = holderForB == null ? null : holderForB.getGenericsTypes();
        if (agt == null || bgt == null || agt.length != bgt.length
                || Arrays.toString(agt).equals(Arrays.toString(bgt))) {
            return lub;
        }
        int n = agt.length; GenericsType[] lubGTs = new GenericsType[n];
        for (int i = 0; i < n; i += 1) {
            ClassNode t1 = upperBound(agt[i]);
            ClassNode t2 = upperBound(bgt[i]);
            ClassNode basicType;
            if (areEqualWithGenerics(t1, isPrimitiveType(a)?getWrapper(a):a) && areEqualWithGenerics(t2, isPrimitiveType(b)?getWrapper(b):b)) {
                // "String implements Comparable<String>" and "StringBuffer implements Comparable<StringBuffer>"
                basicType = fallback; // do not loop
            } else {
                basicType = lowestUpperBound(t1, t2);
            }
            if (agt[i].isWildcard() || bgt[i].isWildcard() || !t1.equals(t2)) {
                lubGTs[i] = GenericsUtils.buildWildcardType(basicType);
            } else {
                lubGTs[i] = basicType.asGenericsType();
            }
        }
        return GenericsUtils.makeClassSafe0(lub, lubGTs);
    }

    private static ClassNode findGenericsTypeHolderForClass(ClassNode source, final ClassNode target) {
        if (isPrimitiveType(source)) source = getWrapper(source);
        if (source.equals(target)) {
            return source;
        }
        if (target.isInterface() ? source.implementsInterface(target) : source.isDerivedFrom(target)) {
            ClassNode sc;
            do {
                sc = getNextSuperClass(source, target);
                if (GenericsUtils.hasUnresolvedGenerics(sc)) {
                    sc = GenericsUtils.correctToGenericsSpecRecurse(GenericsUtils.createGenericsSpec(source), sc);
                }
            } while (!(source = sc).equals(target));

            return sc;
        }
        return null;
    }

    private static ClassNode upperBound(final GenericsType gt) {
        if (gt.isPlaceholder() || gt.isWildcard()) {
            ClassNode[] ub = gt.getUpperBounds();
            if (ub != null) return ub[0];
        }
        return gt.getType();
    }

    private static ClassNode lowestUpperBound(final ClassNode a, final ClassNode b, Set<ClassNode> interfacesImplementedByA, Set<ClassNode> interfacesImplementedByB) {
        // first test special cases
        if (a == null || b == null) {
            // this is a corner case, you should not
            // compare two class nodes if one of them is null
            return null;
        }
        if (a.isArray() && b.isArray()) {
            return lowestUpperBound(a.getComponentType(), b.getComponentType(), interfacesImplementedByA, interfacesImplementedByB).makeArray();
        }
        if (a.equals(OBJECT_TYPE) || b.equals(OBJECT_TYPE)) {
            // one of the objects is at the top of the hierarchy
            GenericsType[] gta = a.getGenericsTypes();
            GenericsType[] gtb = b.getGenericsTypes();
            if (gta != null && gtb != null && gta.length == 1 && gtb.length == 1) {
                if (gta[0].getName().equals(gtb[0].getName())) {
                    return a;
                }
            }
            return OBJECT_TYPE;
        }
        if (a.equals(VOID_TYPE) || b.equals(VOID_TYPE)) {
            if (!b.equals(a)) {
                // one class is void, the other is not
                return OBJECT_TYPE;
            }
            return VOID_TYPE;
        }

        // handle primitives
        boolean aIsPrimitive = isPrimitiveType(a);
        boolean bIsPrimitive = isPrimitiveType(b);
        if (aIsPrimitive || bIsPrimitive) {
            if (a.equals(b)) return a;
            Integer pa = NUMBER_TYPES_PRECEDENCE.get(aIsPrimitive ? a : getUnwrapper(a));
            Integer pb = NUMBER_TYPES_PRECEDENCE.get(bIsPrimitive ? b : getUnwrapper(b));
            ClassNode wa = aIsPrimitive ? getWrapper(a) : a;
            ClassNode wb = bIsPrimitive ? getWrapper(b) : b;
            if (pa != null && pb != null) { // coercion
                if (pa.compareTo(pb) <= 0) {
                    return bIsPrimitive ? a : wa;
                } else {
                    return aIsPrimitive ? b : wb;
                }
            }
            return lowestUpperBound(wa, wb, null, null);
        }

        if (isNumberType(a) && isNumberType(b)) { // GROOVY-8965: TODO
            Integer pa = NUMBER_TYPES_PRECEDENCE.get(getUnwrapper(a));
            Integer pb = NUMBER_TYPES_PRECEDENCE.get(getUnwrapper(b));
            if (pa != null && pb != null) {
                return (pa.compareTo(pb) <= 0 ? a : b);
            }
        }

        // handle interfaces
        boolean aIsInterface = a.isInterface();
        boolean bIsInterface = b.isInterface();
        if (aIsInterface && bIsInterface) {
            if (a.equals(b)) return a;
            if (b.implementsInterface(a)) {
                return a;
            }
            if (a.implementsInterface(b)) {
                return b;
            }
            if (interfacesImplementedByA == null)
                interfacesImplementedByA = GeneralUtils.getInterfacesAndSuperInterfaces(a);
            if (interfacesImplementedByB == null)
                interfacesImplementedByB = GeneralUtils.getInterfacesAndSuperInterfaces(b);

            // each interface may have one or more "extends", so we must find those which are common
            List<ClassNode> common = keepLowestCommonInterfaces(interfacesImplementedByA, interfacesImplementedByB);
            if (common.size() == 1) {
                return common.get(0);
            } else if (common.size() > 1) {
                return buildTypeWithInterfaces(a, b, common);
            }

            // we have two interfaces, but none inherits from the other
            // so the only possible return type is Object
            return OBJECT_TYPE;
        } else if (bIsInterface) {
            return lowestUpperBound(b, a, null, null);
        } else if (aIsInterface) {
            // a is an interface, b is not

            // a ClassNode superclass for an interface is not
            // another interface but always Object. This implies that
            // "extends" for an interface is understood as "implements"
            // for a ClassNode. Therefore, even if b doesn't implement
            // interface a, a could "implement" other interfaces that b
            // implements too, so we must create a list of matching interfaces
            List<ClassNode> matchingInterfaces = new LinkedList<>();
            extractMostSpecificImplementedInterfaces(b, a, matchingInterfaces);
            if (matchingInterfaces.isEmpty()) {
                // no interface in common
                return OBJECT_TYPE;
            }
            if (matchingInterfaces.size() == 1) {
                // a single match, which should be returned
                return matchingInterfaces.get(0);
            }
            return buildTypeWithInterfaces(a, b, matchingInterfaces);
        }
        // both classes do not represent interfaces
        if (a.equals(b)) {
            return buildTypeWithInterfaces(a, b, keepLowestCommonInterfaces(interfacesImplementedByA, interfacesImplementedByB));
        }
        // test if one class inherits from the other
        if (a.isDerivedFrom(b) || b.isDerivedFrom(a)) {
            return buildTypeWithInterfaces(a, b, keepLowestCommonInterfaces(interfacesImplementedByA, interfacesImplementedByB));
        }

        ClassNode sa = a.getUnresolvedSuperClass();
        ClassNode sb = b.getUnresolvedSuperClass();

        if (interfacesImplementedByA == null)
            interfacesImplementedByA = GeneralUtils.getInterfacesAndSuperInterfaces(a);
        if (interfacesImplementedByB == null)
            interfacesImplementedByB = GeneralUtils.getInterfacesAndSuperInterfaces(b);

        // check if no superclass is defined, meaning that we reached the top of the object hierarchy
        if (sa == null || sb == null) {
            return buildTypeWithInterfaces(OBJECT_TYPE, OBJECT_TYPE, keepLowestCommonInterfaces(interfacesImplementedByA, interfacesImplementedByB));
        }

        if (GenericsUtils.hasUnresolvedGenerics(sa))
            sa = GenericsUtils.correctToGenericsSpecRecurse(GenericsUtils.createGenericsSpec(a), sa);
        if (GenericsUtils.hasUnresolvedGenerics(sb))
            sb = GenericsUtils.correctToGenericsSpecRecurse(GenericsUtils.createGenericsSpec(b), sb);

        // if one superclass is derived (or equals) another then it is the common super type
        if (sa.isDerivedFrom(sb) || sb.isDerivedFrom(sa)) {
            return buildTypeWithInterfaces(sa, sb, keepLowestCommonInterfaces(interfacesImplementedByA, interfacesImplementedByB));
        }
        // superclasses are on distinct hierarchy branches, so we recurse on them
        return lowestUpperBound(sa, sb, interfacesImplementedByA, interfacesImplementedByB);
    }

    /**
     * Given the interfaces implemented by two types, returns a list of the most
     * specific common implemented interfaces.
     */
    private static List<ClassNode> keepLowestCommonInterfaces(final Set<ClassNode> fromA, final Set<ClassNode> fromB) {
        if (fromA == null || fromB == null) return Collections.emptyList();
        Set<ClassNode> common = new HashSet<>(fromA);
        common.retainAll(fromB);
        List<ClassNode> result = new ArrayList<>(common.size());
        for (ClassNode classNode : common) {
            addMostSpecificInterface(classNode, result);
        }
        return result;
    }

    private static void addMostSpecificInterface(final ClassNode interfaceNode, final List<ClassNode> nodes) {
        if (nodes.isEmpty()) nodes.add(interfaceNode);
        for (int i = 0, n = nodes.size(); i < n; i += 1) { ClassNode node = nodes.get(i);
            if (node.equals(interfaceNode) || node.implementsInterface(interfaceNode)) {
                // a more specific interface exists in the list, keep it
                return;
            }
            if (interfaceNode.implementsInterface(node)) {
                // the interface being added is more specific than the one in the list, replace it
                nodes.set(i, interfaceNode);
                return;
            }
        }
        // if we reach this point, this means the interface is new
        nodes.add(interfaceNode);
    }

    private static void extractMostSpecificImplementedInterfaces(final ClassNode type, final ClassNode inode, final List<ClassNode> result) {
        if (type.implementsInterface(inode)) result.add(inode);
        else {
            ClassNode[] interfaces = inode.getInterfaces();
            for (ClassNode interfaceNode : interfaces) {
                if (type.implementsInterface(interfaceNode)) result.add(interfaceNode);
            }
            if (result.isEmpty() && interfaces.length > 0) {
                // none if the direct interfaces match, but we must check "upper" in the hierarchy
                for (ClassNode interfaceNode : interfaces) {
                    extractMostSpecificImplementedInterfaces(type, interfaceNode, result);
                }
            }
        }
    }

    /**
     * Given two class nodes supposedly at the upper common level, returns a class node which is able to represent
     * their lowest upper bound.
     * @param baseType1
     * @param baseType2
     * @param interfaces interfaces both class nodes share, which their lowest common super class do not implement.
     * @return the class node representing the lowest upper bound
     */
    private static ClassNode buildTypeWithInterfaces(final ClassNode baseType1, final ClassNode baseType2, final Collection<ClassNode> interfaces) {
        if (interfaces.isEmpty()) {
            if (baseType2.isDerivedFrom(baseType1)) return baseType1;
            if (baseType1.isDerivedFrom(baseType2)) return baseType2;
        }

        if (baseType1.equals(OBJECT_TYPE) && baseType2.equals(OBJECT_TYPE) && interfaces.size() == 1) {
            return interfaces.iterator().next();
        }

        String name; ClassNode superClass;
        if (baseType1.equals(baseType2)) {
            superClass = baseType1;
            if (baseType1.equals(OBJECT_TYPE)) {
                name = "Virtual$Object";
            } else {
                name = "Virtual$" + baseType1.getName();
            }
        } else {
            if (baseType1.isDerivedFrom(baseType2)) {
                superClass = baseType2;
            } else if (baseType2.isDerivedFrom(baseType1)) {
                superClass = baseType1;
            } else {
                superClass = OBJECT_TYPE;
            }
            name = "CommonAssignOf$" + baseType1.getName() + "$" + baseType2.getName();
        }

        interfaces.removeIf(i -> superClass.equals(i) || superClass.implementsInterface(i));

        int nInterfaces = interfaces.size();
        if (nInterfaces == 0) return superClass;
        if (nInterfaces == 1 && superClass.equals(OBJECT_TYPE)) return interfaces.iterator().next();

        ClassNode[] interfaceArray = interfaces.toArray(ClassNode.EMPTY_ARRAY);
        Arrays.sort(interfaceArray, INTERFACE_CLASSNODE_COMPARATOR);
        return new LowestUpperBoundClassNode(name, superClass, interfaceArray);
    }

    /**
     * A comparator which is used in case we generate a virtual lower upper bound class node. In that case,
     * since a concrete implementation should be used at compile time, we must ensure that interfaces are
     * always sorted. It is not important what sort is used, as long as the result is constant.
     */
    private static final Comparator<ClassNode> INTERFACE_CLASSNODE_COMPARATOR = (o1, o2) -> {
        int interfaceCountForO1 = o1.getInterfaces().length;
        int interfaceCountForO2 = o2.getInterfaces().length;
        if (interfaceCountForO1 > interfaceCountForO2) return -1;
        if (interfaceCountForO1 < interfaceCountForO2) return 1;
        int methodCountForO1 = o1.getMethods().size();
        int methodCountForO2 = o2.getMethods().size();
        if (methodCountForO1 > methodCountForO2) return -1;
        if (methodCountForO1 < methodCountForO2) return 1;
        return o1.getName().compareTo(o2.getName());
    };

    /**
     * This {@link ClassNode} specialization is used when the lowest upper bound of two types
     * cannot be represented by an existing type. For example, if B extends A,  C extends A
     * and both C and B implement a common interface not implemented by A, then we use this class
     * to represent the bound.
     *
     * At compile time, some classes like {@link org.codehaus.groovy.classgen.AsmClassGenerator} need
     * to know about a real class node, so we compute a "compile time" node which will be used
     * to return a name and a type class.
     *
     */
    public static class LowestUpperBoundClassNode extends ClassNode {
        private final String name;
        private final String text;
        private final ClassNode upper;
        private final ClassNode[] interfaces;
        private final ClassNode compileTimeClassNode;

        public LowestUpperBoundClassNode(final String name, final ClassNode upper, final ClassNode... interfaces) {
            super(name, ACC_PUBLIC | ACC_FINAL, upper, interfaces, null);
            this.name = name;
            this.upper = upper;
            this.interfaces = interfaces;
            Arrays.sort(interfaces, (cn1, cn2) -> {
                String n1 = cn1 instanceof LowestUpperBoundClassNode ? ((LowestUpperBoundClassNode) cn1).name : cn1.getName();
                String n2 = cn2 instanceof LowestUpperBoundClassNode ? ((LowestUpperBoundClassNode) cn2).name : cn2.getName();
                return n1.compareTo(n2);
            });
            compileTimeClassNode = upper.equals(OBJECT_TYPE) && interfaces.length > 0 ? interfaces[0] : upper;

            StringJoiner sj = new StringJoiner(" or ", "(", ")");
            if (!upper.equals(OBJECT_TYPE)) sj.add(upper.getText());
            for (ClassNode i : interfaces) sj.add(i.getText());
            this.text = sj.toString();

            boolean usesGenerics = upper.isUsingGenerics();
            List<GenericsType[]> genericsTypesList = new ArrayList<>();
            genericsTypesList.add(upper.getGenericsTypes());
            for (ClassNode anInterface : interfaces) {
                usesGenerics |= anInterface.isUsingGenerics();
                genericsTypesList.add(anInterface.getGenericsTypes());
            }
            setUsingGenerics(usesGenerics);
            if (usesGenerics) {
                List<GenericsType> flatList = new ArrayList<>();
                for (GenericsType[] gts : genericsTypesList) {
                    if (gts != null) {
                        Collections.addAll(flatList, gts);
                    }
                }
                setGenericsTypes(flatList.toArray(GenericsType.EMPTY_ARRAY));
            }
        }

        public String getLubName() {
            return name;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getName() {
            return compileTimeClassNode.getName();
        }

        @Override
        public Class getTypeClass() {
            return compileTimeClassNode.getTypeClass();
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + (name != null ? name.hashCode() : 0);
        }

        @Override
        public GenericsType asGenericsType() {
            ClassNode[] ubs;
            if (upper.equals(OBJECT_TYPE)) {
                ubs = interfaces; // Object is implicit
            } else {
                ubs = new ClassNode[interfaces.length + 1]; ubs[0] = upper;
                System.arraycopy(interfaces, 0, ubs, 1, interfaces.length);
            }
            GenericsType gt = new GenericsType(makeWithoutCaching("?"), ubs, null);
            gt.setWildcard(true);
            return gt;
        }

        @Override
        public ClassNode getPlainNodeReference() {
            ClassNode[] faces = interfaces.clone();
            for (int i = 0; i < interfaces.length; i += 1) {
                faces[i] = interfaces[i].getPlainNodeReference();
            }
            return new LowestUpperBoundClassNode(name, upper.getPlainNodeReference(), faces);
        }
    }

    /**
     * Compares two class nodes, but including their generics types.
     * @param a
     * @param b
     * @return true if the class nodes are equal, false otherwise
     */
    private static boolean areEqualWithGenerics(final ClassNode a, final ClassNode b) {
        if (a==null) return b==null;
        if (!a.equals(b)) return false;
        if (a.isUsingGenerics() && !b.isUsingGenerics()) return false;
        GenericsType[] gta = a.getGenericsTypes();
        GenericsType[] gtb = b.getGenericsTypes();
        if (gta == null && gtb != null) return false;
        if (gtb == null && gta != null) return false;
        if (gta != null && gtb != null) {
            if (gta.length != gtb.length) return false;
            for (int i = 0, n = gta.length; i < n; i += 1) {
                GenericsType gta_i = gta[i];
                GenericsType gtb_i = gtb[i];
                ClassNode[] upperA = gta_i.getUpperBounds();
                ClassNode[] upperB = gtb_i.getUpperBounds();
                if (gta_i.isPlaceholder() != gtb_i.isPlaceholder()
                        || gta_i.isWildcard() != gtb_i.isWildcard()
                        || !gta_i.getName().equals(gtb_i.getName())
                        || !areEqualWithGenerics(gta_i.getType(), gtb_i.getType())
                        || !areEqualWithGenerics(gta_i.getLowerBound(), gtb_i.getLowerBound())
                        || (upperA == null ? upperB != null : upperB.length != upperA.length
                            || range(0, upperA.length).anyMatch(j -> !areEqualWithGenerics(upperA[j], upperB[j])))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if the source class implements an interface or subclasses the
     * target type. This method takes the {@link LowestUpperBoundClassNode
     * lowest upper bound class node} type into account, allowing removal of
     * unnecessary casts.
     *
     * @since 2.3.0
     */
    public static boolean implementsInterfaceOrSubclassOf(final ClassNode source, final ClassNode target) {
        if (source.isDerivedFrom(target) || source.implementsInterface(target)) {
            return true;
        }
        if (target instanceof LowestUpperBoundClassNode) {
            LowestUpperBoundClassNode lub = (LowestUpperBoundClassNode) target;
            if (implementsInterfaceOrSubclassOf(source, lub.getSuperClass())) {
                return true;
            }
            for (ClassNode classNode : lub.getInterfaces()) {
                if (source.implementsInterface(classNode)) {
                    return true;
                }
            }
        }
        return false;
    }
}

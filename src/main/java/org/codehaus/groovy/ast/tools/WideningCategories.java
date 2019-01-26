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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.codehaus.groovy.ast.ClassHelper.BigDecimal_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.BigInteger_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Number_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getUnwrapper;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isNumberType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;
import static org.codehaus.groovy.ast.GenericsType.GenericsTypeName;
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
 *
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 * @author Cedric Champeau
 */
public class WideningCategories {

    private static final List<ClassNode> EMPTY_CLASSNODE_LIST = Collections.emptyList();

    private static final Map<ClassNode, Integer> NUMBER_TYPES_PRECEDENCE = Collections.unmodifiableMap(new HashMap<ClassNode, Integer>() {
        private static final long serialVersionUID = -5178744121420941913L;

        {
        put(ClassHelper.double_TYPE, 0);
        put(ClassHelper.float_TYPE, 1);
        put(ClassHelper.long_TYPE, 2);
        put(ClassHelper.int_TYPE, 3);
        put(ClassHelper.short_TYPE, 4);
        put(ClassHelper.byte_TYPE, 5);
    }});

    /**
     * A comparator which is used in case we generate a virtual lower upper bound class node. In that case,
     * since a concrete implementation should be used at compile time, we must ensure that interfaces are
     * always sorted. It is not important what sort is used, as long as the result is constant.
     */
    private static final Comparator<ClassNode> INTERFACE_CLASSNODE_COMPARATOR = new Comparator<ClassNode>() {
        public int compare(final ClassNode o1, final ClassNode o2) {
            int interfaceCountForO1 = o1.getInterfaces().length;
            int interfaceCountForO2 = o2.getInterfaces().length;
            if (interfaceCountForO1 > interfaceCountForO2) return -1;
            if (interfaceCountForO1 < interfaceCountForO2) return 1;
            int methodCountForO1 = o1.getMethods().size();
            int methodCountForO2 = o2.getMethods().size();
            if (methodCountForO1 > methodCountForO2) return -1;
            if (methodCountForO1 < methodCountForO2) return 1;
            return o1.getName().compareTo(o2.getName());
        }
    };

    /**
     * Used to check if a type is an int or Integer.
     * @param type the type to check
     */
    public static boolean isInt(ClassNode type) {
        return int_TYPE == type;
    }

    /**
     * Used to check if a type is an double or Double.
     * @param type the type to check
     */
    public static boolean isDouble(ClassNode type) {
        return double_TYPE == type;
    }

    /**
     * Used to check if a type is a float or Float.
     * @param type the type to check
     */
    public static boolean isFloat(ClassNode type) {
        return float_TYPE == type;
    }

    /**
     * It is of an int category, if the provided type is a
     * byte, char, short, int.
     */
    public static boolean isIntCategory(ClassNode type) {
        return  type==byte_TYPE     ||  type==char_TYPE     ||
                type==int_TYPE      ||  type==short_TYPE;
    }
    /**
     * It is of a long category, if the provided type is a
     * long, its wrapper or if it is a long category. 
     */
    public static boolean isLongCategory(ClassNode type) {
        return  type==long_TYPE     ||  isIntCategory(type);
    }
    /**
     * It is of a BigInteger category, if the provided type is a
     * long category or a BigInteger. 
     */
    public static boolean isBigIntCategory(ClassNode type) {
        return  type==BigInteger_TYPE || isLongCategory(type);
    }
    /**
     * It is of a BigDecimal category, if the provided type is a
     * BigInteger category or a BigDecimal. 
     */
    public static boolean isBigDecCategory(ClassNode type) {
        return  type==BigDecimal_TYPE || isBigIntCategory(type);
    }
    /**
     * It is of a double category, if the provided type is a
     * BigDecimal, a float, double. C(type)=double
     */
    public static boolean isDoubleCategory(ClassNode type) {
        return  type==float_TYPE    ||  type==double_TYPE   ||
                isBigDecCategory(type);
    }

    /**
     * It is of a floating category, if the provided type is a
     * a float, double. C(type)=float
     */
    public static boolean isFloatingCategory(ClassNode type) {
        return  type==float_TYPE    ||  type==double_TYPE;
    }

    public static boolean isNumberCategory(ClassNode type) {
        return isBigDecCategory(type) || type.isDerivedFrom(Number_TYPE);
    }

    /**
     * Given a list of class nodes, returns the first common supertype.
     * For example, Double and Float would return Number, while
     * Set and String would return Object.
     * @param nodes the list of nodes for which to find the first common super type.
     * @return first common supertype
     */
    public static ClassNode lowestUpperBound(List<ClassNode> nodes) {
        if (nodes.size()==1) return nodes.get(0);
        return lowestUpperBound(nodes.get(0), lowestUpperBound(nodes.subList(1, nodes.size())));
    }

    /**
     * Given two class nodes, returns the first common supertype, or the class itself
     * if there are equal. For example, Double and Float would return Number, while
     * Set and String would return Object.
     *
     * This method is not guaranteed to return a class node which corresponds to a
     * real type. For example, if two types have more than one interface in common
     * and are not in the same hierarchy branch, then the returned type will be a
     * virtual type implementing all those interfaces.
     *
     * Calls to this method are supposed to be made with resolved generics. This means
     * that you can have wildcards, but no placeholder.
     *
     * @param a first class node
     * @param b second class node
     * @return first common supertype
     */
    public static ClassNode lowestUpperBound(ClassNode a, ClassNode b) {
        ClassNode lub = lowestUpperBound(a, b, null, null);
        if (lub==null || !lub.isUsingGenerics()) return lub;
        // types may be parameterized. If so, we must ensure that generic type arguments
        // are made compatible

        if (lub instanceof LowestUpperBoundClassNode) {
            // no parent super class representing both types could be found
            // or both class nodes implement common interfaces which may have
            // been parameterized differently.
            // We must create a classnode for which the "superclass" is potentially parameterized
            // plus the interfaces
            ClassNode superClass = lub.getSuperClass();
            ClassNode psc = superClass.isUsingGenerics()?parameterizeLowestUpperBound(superClass, a, b, lub):superClass;

            ClassNode[] interfaces = lub.getInterfaces();
            ClassNode[] pinterfaces = new ClassNode[interfaces.length];
            for (int i = 0, interfacesLength = interfaces.length; i < interfacesLength; i++) {
                final ClassNode icn = interfaces[i];
                if (icn.isUsingGenerics()) {
                    pinterfaces[i] = parameterizeLowestUpperBound(icn, a, b, lub);
                } else {
                    pinterfaces[i] = icn;
                }
            }

            return new LowestUpperBoundClassNode(((LowestUpperBoundClassNode)lub).name, psc, pinterfaces);
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
        // let's compare their generics type
        GenericsType[] agt = holderForA == null ? null : holderForA.getGenericsTypes();
        GenericsType[] bgt = holderForB == null ? null : holderForB.getGenericsTypes();
        if (agt==null || bgt==null || agt.length!=bgt.length) {
            return lub;
        }
        GenericsType[] lubgt = new GenericsType[agt.length];
        for (int i = 0; i < agt.length; i++) {
            ClassNode t1 = agt[i].getType();
            ClassNode t2 = bgt[i].getType();
            ClassNode basicType;
            if (areEqualWithGenerics(t1, a) && areEqualWithGenerics(t2,b)) {
                // we are facing a self referencing type !
                basicType = fallback;
            } else {
                 basicType = lowestUpperBound(t1, t2);
            }
            if (t1.equals(t2)) {
                lubgt[i] = new GenericsType(basicType);
            } else {
                lubgt[i] = GenericsUtils.buildWildcardType(basicType);
            }
        }
        ClassNode plain = lub.getPlainNodeReference();
        plain.setGenericsTypes(lubgt);
        return plain;
    }

    private static ClassNode findGenericsTypeHolderForClass(ClassNode source, ClassNode type) {
        if (isPrimitiveType(source)) source = getWrapper(source);
        if (source.equals(type)) return source;
        if (type.isInterface()) {
            for (ClassNode interfaceNode : source.getAllInterfaces()) {
                if (interfaceNode.equals(type)) {
                    ClassNode parameterizedInterface = GenericsUtils.parameterizeType(source, interfaceNode);
                    return parameterizedInterface;
                }
            }
        }
        ClassNode superClass = source.getUnresolvedSuperClass();
        // copy generic type information if available
        if (superClass!=null && superClass.isUsingGenerics()) {
            Map<GenericsTypeName, GenericsType> genericsTypeMap = GenericsUtils.extractPlaceholders(source);
            GenericsType[] genericsTypes = superClass.getGenericsTypes();
            if (genericsTypes!=null) {
                GenericsType[] copyTypes = new GenericsType[genericsTypes.length];
                for (int i = 0; i < genericsTypes.length; i++) {
                    GenericsType genericsType = genericsTypes[i];
                    GenericsTypeName gtn = new GenericsTypeName(genericsType.getName());
                    if (genericsType.isPlaceholder() && genericsTypeMap.containsKey(gtn)) {
                        copyTypes[i] = genericsTypeMap.get(gtn);
                    } else {
                        copyTypes[i] = genericsType;
                    }
                }
                superClass = superClass.getPlainNodeReference();
                superClass.setGenericsTypes(copyTypes);
            }
        }
        if (superClass!=null) return findGenericsTypeHolderForClass(superClass, type);
        return null;
    }

    private static ClassNode lowestUpperBound(ClassNode a, ClassNode b, List<ClassNode> interfacesImplementedByA, List<ClassNode> interfacesImplementedByB) {
        // first test special cases
        if (a==null || b==null) {
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
            if (gta !=null && gtb !=null && gta.length==1 && gtb.length==1) {
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

        // now handle primitive types
        boolean isPrimitiveA = isPrimitiveType(a);
        boolean isPrimitiveB = isPrimitiveType(b);
        if (isPrimitiveA && !isPrimitiveB) {
            return lowestUpperBound(getWrapper(a), b, null, null);
        }
        if (isPrimitiveB && !isPrimitiveA) {
            return lowestUpperBound(a, getWrapper(b), null, null);
        }
        if (isPrimitiveA && isPrimitiveB) {
            Integer pa = NUMBER_TYPES_PRECEDENCE.get(a);
            Integer pb = NUMBER_TYPES_PRECEDENCE.get(b);
            if (pa!=null && pb!=null) {
                if (pa<=pb) return a;
                return b;
            }
            return a.equals(b)?a:lowestUpperBound(getWrapper(a), getWrapper(b), null, null);
        }
        if (isNumberType(a.redirect()) && isNumberType(b.redirect())) {
            ClassNode ua = getUnwrapper(a);
            ClassNode ub = getUnwrapper(b);
            Integer pa = NUMBER_TYPES_PRECEDENCE.get(ua);
            Integer pb = NUMBER_TYPES_PRECEDENCE.get(ub);
            if (pa!=null && pb!=null) {
                if (pa<=pb) return a;
                return b;
            }
        }

        // handle interfaces
        boolean isInterfaceA = a.isInterface();
        boolean isInterfaceB = b.isInterface();
        if (isInterfaceA && isInterfaceB) {
            if (a.equals(b)) return a;
            if (b.implementsInterface(a)) {
                return a;
            }
            if (a.implementsInterface(b)) {
                return b;
            }
            // each interface may have one or more "extends", so we must find those
            // which are common
            ClassNode[] interfacesFromA = a.getInterfaces();
            ClassNode[] interfacesFromB = b.getInterfaces();
            Set<ClassNode> common = new HashSet<>();
            Collections.addAll(common, interfacesFromA);
            Set<ClassNode> fromB = new HashSet<>();
            Collections.addAll(fromB, interfacesFromB);
            common.retainAll(fromB);

            if (common.size()==1) {
                return common.iterator().next();
            } else if (common.size()>1) {
                return buildTypeWithInterfaces(a, b, common);
            }

            // we have two interfaces, but none inherits from the other
            // so the only possible return type is Object
            return OBJECT_TYPE;
        } else if (isInterfaceB) {
            return lowestUpperBound(b, a, null, null);
        } else if (isInterfaceA) {
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
            if (matchingInterfaces.size()==1) {
                // a single match, which should be returned
                return matchingInterfaces.get(0);
            }
            return buildTypeWithInterfaces(a,b, matchingInterfaces);
        }
        // both classes do not represent interfaces
        if (a.equals(b)) {
            return buildTypeWithInterfaces(a,b, keepLowestCommonInterfaces(interfacesImplementedByA, interfacesImplementedByB));
        }
        // test if one class inherits from the other
        if (a.isDerivedFrom(b) || b.isDerivedFrom(a)) {
            return buildTypeWithInterfaces(a,b, keepLowestCommonInterfaces(interfacesImplementedByA, interfacesImplementedByB));
        }

        // Look at the super classes
        ClassNode sa = a.getUnresolvedSuperClass();
        ClassNode sb = b.getUnresolvedSuperClass();

        // extract implemented interfaces before "going up"
        Set<ClassNode> ifa = new HashSet<>();
        extractInterfaces(a, ifa);
        Set<ClassNode> ifb = new HashSet<>();
        extractInterfaces(b, ifb);
        interfacesImplementedByA = interfacesImplementedByA==null? new LinkedList<>(ifa):interfacesImplementedByA;
        interfacesImplementedByB = interfacesImplementedByB==null? new LinkedList<>(ifb):interfacesImplementedByB;

        // check if no superclass is defined
        // meaning that we reached the top of the object hierarchy
        if (sa==null || sb==null) return buildTypeWithInterfaces(OBJECT_TYPE, OBJECT_TYPE, keepLowestCommonInterfaces(interfacesImplementedByA, interfacesImplementedByB));


        // if one superclass is derived (or equals) another
        // then it is the common super type
        if (sa.isDerivedFrom(sb) || sb.isDerivedFrom(sa)) {
            return buildTypeWithInterfaces(sa, sb, keepLowestCommonInterfaces(interfacesImplementedByA, interfacesImplementedByB));
        }
        // superclasses are on distinct hierarchy branches, so we
        // recurse on them
        return lowestUpperBound(sa, sb, interfacesImplementedByA, interfacesImplementedByB);
    }

    private static void extractInterfaces(ClassNode node, Set<ClassNode> interfaces) {
        if (node==null) return;
        Collections.addAll(interfaces, node.getInterfaces());
        extractInterfaces(node.getSuperClass(), interfaces);
    }
    
    /**
     * Given the list of interfaces implemented by two class nodes, returns the list of the most specific common
     * implemented interfaces.
     * @param fromA
     * @param fromB
     * @return the list of the most specific common implemented interfaces
     */
    private static List<ClassNode> keepLowestCommonInterfaces(List<ClassNode> fromA, List<ClassNode> fromB) {
        if (fromA==null||fromB==null) return EMPTY_CLASSNODE_LIST;
        Set<ClassNode> common = new HashSet<>(fromA);
        common.retainAll(fromB);
        List<ClassNode> result = new ArrayList<>(common.size());
        for (ClassNode classNode : common) {
            addMostSpecificInterface(classNode, result);
        }
        return result;
    }

    private static void addMostSpecificInterface(ClassNode interfaceNode, List<ClassNode> nodes) {
        if (nodes.isEmpty()) nodes.add(interfaceNode);
        for (int i = 0, nodesSize = nodes.size(); i < nodesSize; i++) {
            final ClassNode node = nodes.get(i);
            if (node.equals(interfaceNode)||node.implementsInterface(interfaceNode)) {
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
            if (result.isEmpty() && interfaces.length>0) {
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
    private static ClassNode buildTypeWithInterfaces(ClassNode baseType1, ClassNode baseType2, Collection<ClassNode> interfaces) {
        boolean noInterface = interfaces.isEmpty();
        if (noInterface) {
            if (baseType1.equals(baseType2)) return baseType1;
            if (baseType1.isDerivedFrom(baseType2)) return baseType2;
            if (baseType2.isDerivedFrom(baseType1)) return baseType1;
        }
        if (OBJECT_TYPE.equals(baseType1) && OBJECT_TYPE.equals(baseType2) && interfaces.size()==1) {
            if (interfaces instanceof List) {
                return ((List<ClassNode>) interfaces).get(0);
            }
            return interfaces.iterator().next();
        }
        LowestUpperBoundClassNode type;
        ClassNode superClass;
        String name;
        if (baseType1.equals(baseType2)) {
            if (OBJECT_TYPE.equals(baseType1)) {
                superClass = baseType1;
                name = "Virtual$Object";
            } else {
                superClass = baseType1;
                name = "Virtual$"+baseType1.getName();
            }
        } else {
            superClass = OBJECT_TYPE;
            if (baseType1.isDerivedFrom(baseType2)) {
                superClass = baseType2;
            } else if (baseType2.isDerivedFrom(baseType1)) {
                superClass = baseType1;
            }
            name = "CommonAssignOf$"+baseType1.getName()+"$"+baseType2.getName();
        }
        Iterator<ClassNode> itcn = interfaces.iterator();
        while (itcn.hasNext()) {
            ClassNode next = itcn.next();
            if (superClass.isDerivedFrom(next) || superClass.implementsInterface(next)) {
                itcn.remove();
            }
        }
        ClassNode[] interfaceArray = interfaces.toArray(ClassNode.EMPTY_ARRAY);
        Arrays.sort(interfaceArray, INTERFACE_CLASSNODE_COMPARATOR);
        type = new LowestUpperBoundClassNode(name, superClass, interfaceArray);
        return type;
    }

    /**
     * This {@link ClassNode} specialization is used when the lowest upper bound of two types
     * cannot be represented by an existing type. For example, if B extends A,  C extends A
     * and both C & B implement a common interface not implemented by A, then we use this class
     * to represent the bound.
     *
     * At compile time, some classes like {@link org.codehaus.groovy.classgen.AsmClassGenerator} need
     * to know about a real class node, so we compute a "compile time" node which will be used
     * to return a name and a type class.
     *
     */
    public static class LowestUpperBoundClassNode extends ClassNode {
        private static final Comparator<ClassNode> CLASS_NODE_COMPARATOR = new Comparator<ClassNode>() {
            public int compare(final ClassNode o1, final ClassNode o2) {
                String n1 = o1 instanceof LowestUpperBoundClassNode?((LowestUpperBoundClassNode)o1).name:o1.getName();
                String n2 = o2 instanceof LowestUpperBoundClassNode?((LowestUpperBoundClassNode)o2).name:o2.getName();
                return n1.compareTo(n2);
            }
        };
        private final ClassNode compileTimeClassNode;
        private final String name;
        private final String text;

        private final ClassNode upper;
        private final ClassNode[] interfaces;

        public LowestUpperBoundClassNode(String name, ClassNode upper, ClassNode... interfaces) {
            super(name, ACC_PUBLIC|ACC_FINAL, upper, interfaces, null);
            this.upper = upper;
            this.interfaces = interfaces;
            boolean usesGenerics;
            Arrays.sort(interfaces, CLASS_NODE_COMPARATOR);
            compileTimeClassNode = upper.equals(OBJECT_TYPE) && interfaces.length>0?interfaces[0]:upper;
            this.name = name;
            usesGenerics = upper.isUsingGenerics();
            List<GenericsType[]> genericsTypesList = new LinkedList<>();
            genericsTypesList.add(upper.getGenericsTypes());
			for (ClassNode anInterface : interfaces) {
                usesGenerics |= anInterface.isUsingGenerics();
                genericsTypesList.add(anInterface.getGenericsTypes());
				for (MethodNode methodNode : anInterface.getMethods()) {
                    MethodNode method = addMethod(methodNode.getName(), methodNode.getModifiers(), methodNode.getReturnType(), methodNode.getParameters(), methodNode.getExceptions(), methodNode.getCode());
                    method.setDeclaringClass(anInterface); // important for static compilation!
                }
			}
            setUsingGenerics(usesGenerics);
            if (usesGenerics) {
                List<GenericsType> asArrayList = new ArrayList<>();
                for (GenericsType[] genericsTypes : genericsTypesList) {
                    if (genericsTypes!=null) {
                        Collections.addAll(asArrayList, genericsTypes);
                    }
                }
                setGenericsTypes(asArrayList.toArray(GenericsType.EMPTY_ARRAY));
            }
            StringBuilder sb = new StringBuilder();
            if (!upper.equals(OBJECT_TYPE)) sb.append(upper.getName());
            for (ClassNode anInterface : interfaces) {
                if (sb.length()>0) {
                    sb.append(" or ");
                }
                sb.append(anInterface.getName());
            }
            this.text = sb.toString();
        }

        public String getLubName() {
            return this.name;
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
            int result = super.hashCode();
//            result = 31 * result + (compileTimeClassNode != null ? compileTimeClassNode.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public ClassNode getPlainNodeReference() {
            ClassNode[] intf = interfaces==null?null:new ClassNode[interfaces.length];
            if (intf!=null) {
                for (int i = 0; i < interfaces.length; i++) {
                    intf[i] = interfaces[i].getPlainNodeReference();
                }
            }
            LowestUpperBoundClassNode plain = new LowestUpperBoundClassNode(name, upper.getPlainNodeReference(), intf);
            return plain;
        }
    }

    /**
     * Compares two class nodes, but including their generics types.
     * @param a
     * @param b
     * @return true if the class nodes are equal, false otherwise
     */
    private static boolean areEqualWithGenerics(ClassNode a, ClassNode b) {
        if (a==null) return b==null;
        if (!a.equals(b)) return false;
        if (a.isUsingGenerics() && !b.isUsingGenerics()) return false;
        GenericsType[] gta = a.getGenericsTypes();
        GenericsType[] gtb = b.getGenericsTypes();
        if (gta==null && gtb!=null) return false;
        if (gtb==null && gta!=null) return false;
        if (gta!=null && gtb!=null) {
            if (gta.length!=gtb.length) return false;
            for (int i = 0; i < gta.length; i++) {
                GenericsType ga = gta[i];
                GenericsType gb = gtb[i];
                boolean result = ga.isPlaceholder()==gb.isPlaceholder() && ga.isWildcard()==gb.isWildcard();
                result = result && ga.isResolved() && gb.isResolved();
                result = result && ga.getName().equals(gb.getName());
                result = result && areEqualWithGenerics(ga.getType(), gb.getType());
                result = result && areEqualWithGenerics(ga.getLowerBound(), gb.getLowerBound());
                if (result) {
                    ClassNode[] upA = ga.getUpperBounds();
                    if (upA!=null) {
                        ClassNode[] upB = gb.getUpperBounds();
                        if (upB==null || upB.length!=upA.length) return false;
                        for (int j = 0; j < upA.length; j++) {
                            if (!areEqualWithGenerics(upA[j],upB[j])) return false;
                        }
                    }
                }
                if (!result) return false;
            }
        }
        return true;
    }
    
    /**
     * Determines if the source class implements an interface or subclasses the target type.
     * This method takes the {@link org.codehaus.groovy.ast.tools.WideningCategories.LowestUpperBoundClassNode lowest
     * upper bound class node} type into account, allowing to remove unnecessary casts.
     * @param source the type of interest
     * @param targetType the target type of interest
     */
    public static boolean implementsInterfaceOrSubclassOf(final ClassNode source, final ClassNode targetType) {
        if (source.isDerivedFrom(targetType) || source.implementsInterface(targetType)) return true;
        if (targetType instanceof WideningCategories.LowestUpperBoundClassNode) {
            WideningCategories.LowestUpperBoundClassNode lub = (WideningCategories.LowestUpperBoundClassNode) targetType;
            if (implementsInterfaceOrSubclassOf(source, lub.getSuperClass())) return true;
            for (ClassNode classNode : lub.getInterfaces()) {
                if (source.implementsInterface(classNode)) return true;
            }
        }
        return false;
    }
}

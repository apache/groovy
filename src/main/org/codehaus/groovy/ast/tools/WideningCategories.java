/*
 * Copyright 2003-2010 the original author or authors.
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

import static org.codehaus.groovy.ast.ClassHelper.*;

import org.codehaus.groovy.ast.ClassNode;

import java.util.*;

/**
 * This class provides helper methods to determine the type from a widening 
 * operation for example for a plus operation.<br/>
 * To determine the resulting type of for example a=exp1+exp2 we look at the
 * conditions {@link #isIntCategory(ClassNode)}, {@link #isLongCategory(ClassNode)},
 * {@link #isBigIntCategory(ClassNode)}, {@link #isDoubleCategory(ClassNode)} and
 * {@link #isBigDecCategory(ClassNode)} in that order. The first case applying to
 * exp1 and exp2 is defining the result type of the expression. <br/>
 * If for example you look at x = 1 + 2l we have the first category applying to 
 * the number 1 being int, since the 1 is an int. The 2l is a long, therefore the
 * int category will not apply and the result type can't be int. The next category 
 * in the list is long, and since both apply to long, the result type is a long.<br/>
 * 
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 * @author Cedric Champeau
 */
public class WideningCategories {

    private static final List<ClassNode> EMPTY_CLASSNODE_LIST = Collections.emptyList();

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
     * It is of an int category, if the provided type is a
     * byte, char, short, int or any of the wrapper.   
     */
    public static boolean isIntCategory(ClassNode type) {
        return  type==byte_TYPE     ||  type==Byte_TYPE      ||
                type==char_TYPE     ||  type==Character_TYPE ||
                type==int_TYPE      ||  type==Integer_TYPE   ||
                type==short_TYPE    ||  type==Short_TYPE;
    }
    /**
     * It is of a long category, if the provided type is a
     * long, its wrapper or if it is a long category. 
     */
    public static boolean isLongCategory(ClassNode type) {
        return  type==long_TYPE     ||  type==Long_TYPE     ||
                isIntCategory(type);
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
     * BigDecimal, a float, double or a wrapper of those. C(type)=double
     */
    public static boolean isDoubleCategory(ClassNode type) {
        return  type==float_TYPE    ||  type==Float_TYPE    ||
                type==double_TYPE   ||  type==Double_TYPE   ||
                isBigDecCategory(type);
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
     * todo : handle generics too, so that the LUB of List&lt;String&gt; and List&lt;Integer&gt;
     * is List&lt;? extends Serializable&gt;
     * 
     * @param a first class node
     * @param b second class node
     * @return first common supertype
     */
    public static ClassNode lowestUpperBound(ClassNode a, ClassNode b) {
        return lowestUpperBound(a, b, null, null);
    }

    private static ClassNode lowestUpperBound(ClassNode a, ClassNode b, List<ClassNode> interfacesImplementedByA, List<ClassNode> interfacesImplementedByB) {
        // first test special cases
        if (a==null || b==null) {
            // this is a corner case, you should not
            // compare two class nodes if one of them is null
            return null;
        }
        if (a.equals(OBJECT_TYPE) || b.equals(OBJECT_TYPE)) {
            // one of the objects is at the top of the hierarchy
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
            Set<ClassNode> common = new HashSet<ClassNode>();
            Collections.addAll(common, interfacesFromA);
            Set<ClassNode> fromB = new HashSet<ClassNode>();
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
            List<ClassNode> matchingInterfaces = new LinkedList<ClassNode>();
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
        ClassNode[] interfacesFromA = a.getInterfaces();
        ClassNode[] interfacesFromB = b.getInterfaces();
        if (interfacesFromA.length>0 || interfacesFromB.length>0) {
            if (interfacesImplementedByA==null) {
                interfacesImplementedByA = new ArrayList<ClassNode>();
            }
            if (interfacesImplementedByB==null) {
                interfacesImplementedByB = new ArrayList<ClassNode>();
            }
            Collections.addAll(interfacesImplementedByA, interfacesFromA);
            Collections.addAll(interfacesImplementedByB, interfacesFromB);
        }

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

    /**
     * Given the list of interfaces implemented by two class nodes, returns the list of the most specific common
     * implemented interfaces.
     * @param fromA
     * @param fromB
     * @return
     */
    private static List<ClassNode> keepLowestCommonInterfaces(List<ClassNode> fromA, List<ClassNode> fromB) {
        if (fromA==null||fromB==null) return EMPTY_CLASSNODE_LIST;
        HashSet<ClassNode> common = new HashSet<ClassNode>(fromA);
        common.retainAll(fromB);
        List<ClassNode> result = new ArrayList<ClassNode>(common.size());
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
                // the interface beeing added is more specific than the one in the list, replace it
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
     * Given two class nodes supposely at the upper common level, returns a class node which is able to represent
     * their lowest upper bound.
     * @param baseType1
     * @param baseType2
     * @param interfaces interfaces both class nodes share, which their lowest common super class do not implement.
     * @return
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
        ClassNode[] interfaceArray = interfaces.toArray(new ClassNode[interfaces.size()]);
        Arrays.sort(interfaceArray, INTERFACE_CLASSNODE_COMPARATOR);
        if (baseType1.equals(baseType2)) {
            if (OBJECT_TYPE.equals(baseType1)) {
                type = new LowestUpperBoundClassNode("Virtual$Object", OBJECT_TYPE, interfaceArray);
            } else {
                type = new LowestUpperBoundClassNode("Virtual$"+baseType1.getName(), baseType1, interfaceArray);
            }
        } else {
            type = new LowestUpperBoundClassNode("CommonAssignOf$"+baseType1.getName()+"$"+baseType2.getName(), OBJECT_TYPE, interfaceArray);
        }
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
    private static class LowestUpperBoundClassNode extends ClassNode {
        private final ClassNode compileTimeClassNode;

        public LowestUpperBoundClassNode(String name, ClassNode upper, ClassNode... interfaces) {
            super(name, ACC_PUBLIC|ACC_FINAL, upper, interfaces, null);
            compileTimeClassNode = upper.equals(OBJECT_TYPE) && interfaces.length>0?interfaces[0]:upper;
        }

        @Override
        public String getNameWithoutPackage() {
            return compileTimeClassNode.getNameWithoutPackage();
        }

        @Override
        public String getName() {
            return compileTimeClassNode.getName();
        }

        @Override
        public Class getTypeClass() {
            return compileTimeClassNode.getTypeClass();
        }
    }
}

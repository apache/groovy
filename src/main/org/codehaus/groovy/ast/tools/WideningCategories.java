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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;

import java.util.List;

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
 */
public class WideningCategories {
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

    public static boolean isNumberCategory(ClassNode type) {
        return isBigDecCategory(type) || type.isDerivedFrom(Number_TYPE);
    }

    /**
     * Given two class nodes, returns the first common supertype, or the class itself
     * if there are equal. For example, Double and Float would return Number, while
     * Set and String would return Object.
     * @param a first class node
     * @param b second class node
     * @return first common supertype
     */
    public static ClassNode firstCommonSuperType(ClassNode a, ClassNode b) {
        if (a == null || b == null) return ClassHelper.OBJECT_TYPE;
        if (a == b || a.equals(b)) return a;
        if (isPrimitiveType(a) && !isPrimitiveType(b)) {
            if (isNumberType(a) && isNumberType(b)) {
                return firstCommonSuperType(ClassHelper.getWrapper(a), b);
            } else {
                return OBJECT_TYPE;
            }
        } else if (isPrimitiveType(b) && !isPrimitiveType(a)) {
            if (isNumberType(b) && isNumberType(a)) {
                return firstCommonSuperType(ClassHelper.getWrapper(b), a);
            } else {
                return OBJECT_TYPE;
            }
        } else if (isPrimitiveType(a) && isPrimitiveType(b)) {
            return firstCommonSuperType(ClassHelper.getWrapper(a), ClassHelper.getWrapper(b));
        }
        ClassNode superA = a.getSuperClass();
        ClassNode superB = b.getSuperClass();
        if (a == superB || a.equals(superB)) return superB;
        if (b == superA || b.equals(superA)) return superA;
        return firstCommonSuperType(superA, superB);
    }

    /**
     * Given a list of class nodes, returns the first common supertype.
     * For example, Double and Float would return Number, while
     * Set and String would return Object.
     * @param nodes the list of nodes for which to find the first common super type.
     * @return first common supertype
     */
    public static ClassNode firstCommonSuperType(List<ClassNode> nodes) {
        if (nodes.size()==1) return nodes.get(0);
        return firstCommonSuperType(nodes.get(0), firstCommonSuperType(nodes.subList(1, nodes.size())));
    }
}

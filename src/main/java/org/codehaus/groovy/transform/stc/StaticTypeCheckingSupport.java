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
package org.codehaus.groovy.transform.stc;

import groovy.lang.GroovyClassLoader;
import org.apache.groovy.util.Maps;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GenericsType.GenericsTypeName;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.ast.tools.ParameterUtils;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.runtime.ArrayGroovyMethods;
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.tools.GroovyClass;
import org.codehaus.groovy.transform.trait.Traits;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.BaseStream;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.apache.groovy.ast.tools.ClassNodeUtils.samePackageName;
import static org.apache.groovy.ast.tools.ExpressionUtils.isNullConstant;
import static org.codehaus.groovy.ast.ClassHelper.BigInteger_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.CLASS_Type;
import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.COLLECTION_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Character_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.DEPRECATED_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Enum_Type;
import static org.codehaus.groovy.ast.ClassHelper.Float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.GSTRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Integer_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Number_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Short_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.findSAM;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getUnwrapper;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isBigDecimalType;
import static org.codehaus.groovy.ast.ClassHelper.isBigIntegerType;
import static org.codehaus.groovy.ast.ClassHelper.isClassType;
import static org.codehaus.groovy.ast.ClassHelper.isGStringType;
import static org.codehaus.groovy.ast.ClassHelper.isGroovyObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isNumberType;
import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.isSAMType;
import static org.codehaus.groovy.ast.ClassHelper.isStringType;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperBoolean;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.void_WRAPPER_TYPE;
import static org.codehaus.groovy.ast.tools.WideningCategories.implementsInterfaceOrSubclassOf;
import static org.codehaus.groovy.ast.tools.WideningCategories.isFloatingCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isLongCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.lowestUpperBound;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;
import static org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport.closeQuietly;
import static org.codehaus.groovy.syntax.Types.BITWISE_AND;
import static org.codehaus.groovy.syntax.Types.BITWISE_AND_EQUAL;
import static org.codehaus.groovy.syntax.Types.BITWISE_OR;
import static org.codehaus.groovy.syntax.Types.BITWISE_OR_EQUAL;
import static org.codehaus.groovy.syntax.Types.BITWISE_XOR;
import static org.codehaus.groovy.syntax.Types.BITWISE_XOR_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_GREATER_THAN;
import static org.codehaus.groovy.syntax.Types.COMPARE_GREATER_THAN_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_IDENTICAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_LESS_THAN;
import static org.codehaus.groovy.syntax.Types.COMPARE_LESS_THAN_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_IDENTICAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_IN;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_INSTANCEOF;
import static org.codehaus.groovy.syntax.Types.COMPARE_TO;
import static org.codehaus.groovy.syntax.Types.DIVIDE;
import static org.codehaus.groovy.syntax.Types.DIVIDE_EQUAL;
import static org.codehaus.groovy.syntax.Types.IMPLIES;
import static org.codehaus.groovy.syntax.Types.INTDIV;
import static org.codehaus.groovy.syntax.Types.INTDIV_EQUAL;
import static org.codehaus.groovy.syntax.Types.KEYWORD_IN;
import static org.codehaus.groovy.syntax.Types.KEYWORD_INSTANCEOF;
import static org.codehaus.groovy.syntax.Types.LEFT_SHIFT;
import static org.codehaus.groovy.syntax.Types.LEFT_SHIFT_EQUAL;
import static org.codehaus.groovy.syntax.Types.LEFT_SQUARE_BRACKET;
import static org.codehaus.groovy.syntax.Types.LOGICAL_AND;
import static org.codehaus.groovy.syntax.Types.LOGICAL_OR;
import static org.codehaus.groovy.syntax.Types.MATCH_REGEX;
import static org.codehaus.groovy.syntax.Types.MINUS;
import static org.codehaus.groovy.syntax.Types.MINUS_EQUAL;
import static org.codehaus.groovy.syntax.Types.MOD;
import static org.codehaus.groovy.syntax.Types.MOD_EQUAL;
import static org.codehaus.groovy.syntax.Types.MULTIPLY;
import static org.codehaus.groovy.syntax.Types.MULTIPLY_EQUAL;
import static org.codehaus.groovy.syntax.Types.PLUS;
import static org.codehaus.groovy.syntax.Types.PLUS_EQUAL;
import static org.codehaus.groovy.syntax.Types.POWER;
import static org.codehaus.groovy.syntax.Types.POWER_EQUAL;
import static org.codehaus.groovy.syntax.Types.REMAINDER;
import static org.codehaus.groovy.syntax.Types.REMAINDER_EQUAL;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_EQUAL;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_UNSIGNED;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_UNSIGNED_EQUAL;

/**
 * Support methods for {@link StaticTypeCheckingVisitor}.
 */
public abstract class StaticTypeCheckingSupport {

    protected static final ClassNode Matcher_TYPE = makeWithoutCaching(Matcher.class);
    protected static final ClassNode ArrayList_TYPE = makeWithoutCaching(ArrayList.class);
    protected static final ClassNode BaseStream_TYPE = makeWithoutCaching(BaseStream.class);
    protected static final ClassNode Collection_TYPE = COLLECTION_TYPE; // TODO: deprecate?
    protected static final ClassNode Deprecated_TYPE = DEPRECATED_TYPE; // TODO: deprecate?
    protected static final ClassNode LinkedHashMap_TYPE = makeWithoutCaching(LinkedHashMap.class);
    protected static final ClassNode LinkedHashSet_TYPE = makeWithoutCaching(LinkedHashSet.class);

    protected static final Map<ClassNode, Integer> NUMBER_TYPES = Maps.of(
            byte_TYPE,    0,
            Byte_TYPE,    0,
            short_TYPE,   1,
            Short_TYPE,   1,
            int_TYPE,     2,
            Integer_TYPE, 2,
            long_TYPE,    3,
            Long_TYPE,    3,
            float_TYPE,   4,
            Float_TYPE,   4,
            double_TYPE,  5,
            Double_TYPE,  5
    );

    protected static final Map<String, Integer> NUMBER_OPS = Maps.of(
            "plus",       PLUS,
            "minus",      MINUS,
            "multiply",   MULTIPLY,
            "div",        DIVIDE,
            "or",         BITWISE_OR,
            "and",        BITWISE_AND,
            "xor",        BITWISE_XOR,
            "mod",        MOD,
            "remainder",  REMAINDER,
            "intdiv",     INTDIV,
            "leftShift",  LEFT_SHIFT,
            "rightShift", RIGHT_SHIFT,
            "rightShiftUnsigned", RIGHT_SHIFT_UNSIGNED
    );

    protected static final ClassNode GSTRING_STRING_CLASSNODE = lowestUpperBound(
            STRING_TYPE,
            GSTRING_TYPE
    );

    /**
     * This is for internal use only. When an argument method is null, we cannot determine its type, so
     * we use this one as a wildcard.
     */
    protected static final ClassNode UNKNOWN_PARAMETER_TYPE = make("<unknown parameter type>");

    /**
     * This comparator is used when we return the list of methods from DGM which name correspond to a given
     * name. As we also look up for DGM methods of superclasses or interfaces, it may be possible to find
     * two methods which have the same name and the same arguments. In that case, we should not add the method
     * from superclass or interface otherwise the system won't be able to select the correct method, resulting
     * in an ambiguous method selection for similar methods.
     */
    protected static final Comparator<MethodNode> DGM_METHOD_NODE_COMPARATOR = (mn1, mn2) -> {
        if (mn1.getName().equals(mn2.getName())) {
            Parameter[] pa1 = mn1.getParameters();
            Parameter[] pa2 = mn2.getParameters();
            if (pa1.length == pa2.length) {
                boolean allEqual = true;
                for (int i = 0, n = pa1.length; i < n && allEqual; i += 1) {
                    allEqual = pa1[i].getType().equals(pa2[i].getType());
                }
                if (allEqual) {
                    if (mn1 instanceof ExtensionMethodNode && mn2 instanceof ExtensionMethodNode) {
                        return StaticTypeCheckingSupport.DGM_METHOD_NODE_COMPARATOR.compare(((ExtensionMethodNode) mn1).getExtensionMethodNode(), ((ExtensionMethodNode) mn2).getExtensionMethodNode());
                    }
                    return 0;
                }
            } else {
                return pa1.length - pa2.length;
            }
        }
        return 1;
    };

    protected static final ExtensionMethodCache EXTENSION_METHOD_CACHE = ExtensionMethodCache.INSTANCE;
    public static void clearExtensionMethodCache(final ClassLoader loader) {
        EXTENSION_METHOD_CACHE.cache.remove(loader);
    }
    public static void clearExtensionMethodCache() {
        EXTENSION_METHOD_CACHE.cache.clearAll();
    }

    /**
     * Returns true for expressions of the form x[...]
     *
     * @param expression an expression
     * @return true for array access expressions
     */
    protected static boolean isArrayAccessExpression(final Expression expression) {
        return expression instanceof BinaryExpression && isArrayOp(((BinaryExpression) expression).getOperation().getType());
    }

    /**
     * Called on method call checks in order to determine if a method call corresponds to the
     * idiomatic o.with { ... } structure
     *
     * @param name      name of the method called
     * @param arguments method call arguments
     * @return true if the name is "with" and arguments consist of a single closure
     */
    public static boolean isWithCall(final String name, final Expression arguments) {
        if ("with".equals(name) && arguments instanceof ArgumentListExpression) {
            List<Expression> args = ((ArgumentListExpression) arguments).getExpressions();
            if (args.size() == 1 && args.get(0) instanceof ClosureExpression) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a variable expression, returns the ultimately accessed variable.
     *
     * @param ve a variable expression
     * @return the target variable
     */
    protected static Variable findTargetVariable(final VariableExpression ve) {
        Variable accessedVariable = ve.getAccessedVariable();
        if (accessedVariable != null && accessedVariable != ve) {
            if (accessedVariable instanceof VariableExpression) {
                return findTargetVariable((VariableExpression) accessedVariable);
            }
            return accessedVariable;
        }
        return ve;
    }

    /**
     * @deprecated Use {@link #findDGMMethodsForClassNode(ClassLoader, ClassNode, String)} instead
     */
    @Deprecated(forRemoval = true, since = "2.2.0")
    protected static Set<MethodNode> findDGMMethodsForClassNode(final ClassNode clazz, final String name) {
        return findDGMMethodsForClassNode(MetaClassRegistryImpl.class.getClassLoader(), clazz, name);
    }

    public static Set<MethodNode> findDGMMethodsForClassNode(final ClassLoader loader, final ClassNode clazz, final String name) {
        TreeSet<MethodNode> accumulator = new TreeSet<>(DGM_METHOD_NODE_COMPARATOR);
        findDGMMethodsForClassNode(loader, clazz, name, accumulator);
        return accumulator;
    }

    /**
     * @deprecated Use {@link #findDGMMethodsForClassNode(ClassLoader, ClassNode, String, TreeSet)} instead
     */
    @Deprecated(forRemoval = true, since = "2.2.0")
    protected static void findDGMMethodsForClassNode(final ClassNode clazz, final String name, final TreeSet<MethodNode> accumulator) {
        findDGMMethodsForClassNode(MetaClassRegistryImpl.class.getClassLoader(), clazz, name, accumulator);
    }

    protected static void findDGMMethodsForClassNode(final ClassLoader loader, final ClassNode clazz, final String name, final TreeSet<MethodNode> accumulator) {
        List<MethodNode> fromDGM = EXTENSION_METHOD_CACHE.get(loader).get(clazz.getName());
        if (fromDGM != null) {
            for (MethodNode node : fromDGM) {
                if (node.getName().equals(name)) accumulator.add(node);
            }
        }
        for (ClassNode node : clazz.getInterfaces()) {
            findDGMMethodsForClassNode(loader, node, name, accumulator);
        }
        if (clazz.isArray()) {
            ClassNode componentClass = clazz.getComponentType();
            if (!isObjectType(componentClass) && !isPrimitiveType(componentClass)) {
                if (componentClass.isInterface()) {
                    findDGMMethodsForClassNode(loader, OBJECT_TYPE.makeArray(), name, accumulator);
                } else {
                    findDGMMethodsForClassNode(loader, componentClass.getSuperClass().makeArray(), name, accumulator);
                }
            }
        }
        if (clazz.getSuperClass() != null) {
            findDGMMethodsForClassNode(loader, clazz.getSuperClass(), name, accumulator);
        } else if (!isObjectType(clazz)) {
            findDGMMethodsForClassNode(loader, OBJECT_TYPE, name, accumulator);
        }
    }

    /**
     * Determines if the specific argument types fit the parameters.
     *
     * @return -1 if arguments do not match, 0 if arguments are of the exact type,
     * and &gt; 0 when one or more argument is not of the exact type but still match
     */
    public static int allParametersAndArgumentsMatch(Parameter[] parameters, final ClassNode[] argumentTypes) {
        if (parameters == null) parameters = Parameter.EMPTY_ARRAY;
        final int nParameters = parameters.length;
        if (argumentTypes.length < nParameters) {
            return -1;
        }
        int dist = 0;
        for (int i = 0; i < nParameters; i += 1) { // extra argument(s) ignored!
            ClassNode aType = argumentTypes[i], pType = parameters[i].getType();
            if (!isAssignableTo(aType, pType)) {
                return -1;
            } else if (!aType.equals(pType)) {
                dist += getDistance(aType, pType);
            }
        }
        return dist;
    }

    /**
     * Checks that excess arguments match the vararg signature parameter.
     *
     * @return -1 if no match, 0 if all arguments matches the vararg type and >0 if one or more vararg argument is
     * assignable to the vararg type, but still not an exact match
     */
    static int excessArgumentsMatchesVargsParameter(final Parameter[] parameters, final ClassNode[] argumentTypes) {
        // we already know parameter length is bigger zero and last is a vargs
        // the excess arguments are all put in an array for the vargs call
        // so check against the component type
        int dist = 0;
        ClassNode vargsBase = parameters[parameters.length - 1].getType().getComponentType();
        for (int i = parameters.length; i < argumentTypes.length; i += 1) {
            if (!isAssignableTo(argumentTypes[i], vargsBase)) return -1;
            else dist += getClassDistance(vargsBase, argumentTypes[i]);
        }
        return dist;
    }

    /**
     * Checks if the last argument matches the vararg type.
     *
     * @return -1 if no match, 0 if the last argument is exactly the vararg type and 1 if of an assignable type
     */
    @SuppressWarnings("removal")
    private static int lastArgMatchesVarg(final Parameter[] parameters, final ClassNode... argumentTypes) {
        if (!isVargs(parameters)) return -1;
        int lastParamIndex = parameters.length - 1;
        if (lastParamIndex == argumentTypes.length) return 0;
        // two cases remain:
        // the argument is wrapped in the vargs array or
        // the argument is an array that can be used for the vargs part directly
        // testing only the wrapping case since the non-wrapping is done already
        ClassNode arrayType = parameters[lastParamIndex].getType();
        ClassNode elementType = arrayType.getComponentType();
        ClassNode argumentType = argumentTypes[argumentTypes.length - 1];
        if (isNumberType(elementType) && isNumberType(argumentType) && !getWrapper(elementType).equals(getWrapper(argumentType))) return -1;
        return isAssignableTo(argumentType, elementType) ? Math.min(getDistance(argumentType, arrayType), getDistance(argumentType, elementType)) : -1;
    }

    /**
     * Checks if a class node is assignable to another. This is used for example in
     * assignment checks where you want to verify that the assignment is valid.
     *
     * @return true if the class node is assignable to the other class node, false otherwise
     */
    public static boolean isAssignableTo(ClassNode type, ClassNode toBeAssignedTo) {
        if (type == toBeAssignedTo || type == UNKNOWN_PARAMETER_TYPE) return true;
        if (isPrimitiveType(type)) type = getWrapper(type);
        if (isPrimitiveType(toBeAssignedTo)) toBeAssignedTo = getWrapper(toBeAssignedTo);
        Integer source= NUMBER_TYPES.get(type), target= NUMBER_TYPES.get(toBeAssignedTo);
        if (source != null && target != null) { return (source.compareTo(target) <= 0); }
        // GROOVY-8325, GROOVY-8488: float or double can be assigned a BigDecimal literal
        if (isBigDecimalType(type) && isFloatingCategory(getUnwrapper(toBeAssignedTo))) {
            return true;
        }
        if (type.isArray() && toBeAssignedTo.isArray()) {
            ClassNode sourceComponent = type.getComponentType(), targetComponent = toBeAssignedTo.getComponentType();
            return isPrimitiveType(targetComponent) ? sourceComponent.equals(targetComponent) // GROOVY-11053: strict
                : !isPrimitiveType(sourceComponent) && isAssignableTo(sourceComponent, targetComponent); // GROOVY-10720
        }
        if (type.isDerivedFrom(GSTRING_TYPE) && isStringType(toBeAssignedTo)) {
            return true;
        }
        if (isStringType(type) && toBeAssignedTo.isDerivedFrom(GSTRING_TYPE)) {
            return true;
        }
        if (implementsInterfaceOrIsSubclassOf(type, toBeAssignedTo)) {
            if (toBeAssignedTo.getGenericsTypes() != null) { // perform additional check on generics
                GenericsType gt = toBeAssignedTo.isGenericsPlaceHolder() ? toBeAssignedTo.getGenericsTypes()[0] : GenericsUtils.buildWildcardType(toBeAssignedTo);
                return gt.isCompatibleWith(type);
            }
            return true;
        }
        // GROOVY-10067: unresolved argument like "N extends Number" for parameter like "Integer"
        if (type.isGenericsPlaceHolder() && type.getUnresolvedName().charAt(0) == '#') {
            return type.asGenericsType().isCompatibleWith(toBeAssignedTo);
        }
        return (type.isDerivedFrom(CLOSURE_TYPE) && isSAMType(toBeAssignedTo));
    }

    @Deprecated(forRemoval = true, since = "4.0.4")
    static boolean isVargs(final Parameter[] parameters) {
        return ParameterUtils.isVargs(parameters);
    }

    public static boolean isCompareToBoolean(final int op) {
        return op == COMPARE_LESS_THAN || op == COMPARE_LESS_THAN_EQUAL
                || op == COMPARE_GREATER_THAN || op == COMPARE_GREATER_THAN_EQUAL;
    }

    static boolean isArrayOp(final int op) {
        return op == LEFT_SQUARE_BRACKET;
    }

    static boolean isBoolIntrinsicOp(final int op) {
        switch (op) {
            case LOGICAL_AND:
            case LOGICAL_OR:
            case COMPARE_NOT_IDENTICAL:
            case COMPARE_IDENTICAL:
            case MATCH_REGEX:
            case KEYWORD_INSTANCEOF:
            case COMPARE_NOT_INSTANCEOF:
            case IMPLIES:
                return true;
            default:
                return false;
        }
    }

    static boolean isPowerOperator(final int op) {
        return op == POWER || op == POWER_EQUAL;
    }

    static String getOperationName(final int op) {
        switch (op) {
            case COMPARE_EQUAL:
            case COMPARE_NOT_EQUAL:
                // this is only correct in this specific context; normally
                // we would have to compile against compareTo if available
                // but since we don't compile here, this one is enough
                return "equals";

            case COMPARE_TO:
            case COMPARE_LESS_THAN:
            case COMPARE_LESS_THAN_EQUAL:
            case COMPARE_GREATER_THAN:
            case COMPARE_GREATER_THAN_EQUAL:
                return "compareTo";

            case BITWISE_AND:
            case BITWISE_AND_EQUAL:
                return "and";

            case BITWISE_OR:
            case BITWISE_OR_EQUAL:
                return "or";

            case BITWISE_XOR:
            case BITWISE_XOR_EQUAL:
                return "xor";

            case PLUS:
            case PLUS_EQUAL:
                return "plus";

            case MINUS:
            case MINUS_EQUAL:
                return "minus";

            case MULTIPLY:
            case MULTIPLY_EQUAL:
                return "multiply";

            case DIVIDE:
            case DIVIDE_EQUAL:
                return "div";

            case INTDIV:
            case INTDIV_EQUAL:
                return "intdiv";

            case MOD:
            case MOD_EQUAL:
                return "mod";

            case REMAINDER:
            case REMAINDER_EQUAL:
                return "remainder";

            case POWER:
            case POWER_EQUAL:
                return "power";

            case LEFT_SHIFT:
            case LEFT_SHIFT_EQUAL:
                return "leftShift";

            case RIGHT_SHIFT:
            case RIGHT_SHIFT_EQUAL:
                return "rightShift";

            case RIGHT_SHIFT_UNSIGNED:
            case RIGHT_SHIFT_UNSIGNED_EQUAL:
                return "rightShiftUnsigned";

            case KEYWORD_IN:
                return "isCase";

            case COMPARE_NOT_IN:
                return "isNotCase";

            default:
                return null;
        }
    }

    static boolean isShiftOperation(final String name) {
        return "leftShift".equals(name) || "rightShift".equals(name) || "rightShiftUnsigned".equals(name);
    }

    /**
     * Returns true for operations that are of the class, that given a common type class for left and right, the
     * operation "left op right" will have a result in the same type class In Groovy on numbers that is +,-,* as well as
     * their variants with equals.
     */
    static boolean isOperationInGroup(final int op) {
        switch (op) {
            case PLUS:
            case PLUS_EQUAL:
            case MINUS:
            case MINUS_EQUAL:
            case MULTIPLY:
            case MULTIPLY_EQUAL:
                return true;
            default:
                return false;
        }
    }

    static boolean isBitOperator(final int op) {
        switch (op) {
            case BITWISE_OR_EQUAL:
            case BITWISE_OR:
            case BITWISE_AND_EQUAL:
            case BITWISE_AND:
            case BITWISE_XOR_EQUAL:
            case BITWISE_XOR:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAssignment(final int op) {
        return Types.isAssignment(op);
    }

    /**
     * Returns true or false depending on whether the right classnode can be assigned to the left classnode. This method
     * should not add errors by itself: we let the caller decide what to do if an incompatible assignment is found.
     *
     * @param left  the class to be assigned to
     * @param right the assignee class
     * @return false if types are incompatible
     */
    public static boolean checkCompatibleAssignmentTypes(final ClassNode left, final ClassNode right) {
        return checkCompatibleAssignmentTypes(left, right, null);
    }

    public static boolean checkCompatibleAssignmentTypes(final ClassNode left, final ClassNode right, final Expression rightExpression) {
        return checkCompatibleAssignmentTypes(left, right, rightExpression, true);
    }

    /**
     * Everything that can be done by {@code castToType} should be allowed for assignment.
     *
     * @see org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation#castToType(Object,Class)
     */
    public static boolean checkCompatibleAssignmentTypes(final ClassNode left, final ClassNode right, final Expression rightExpression, final boolean allowConstructorCoercion) {
        if (!isPrimitiveType(left) && isNullConstant(rightExpression)) {
            return true;
        }

        if (left.isArray()) {
            if (right.isArray()) {
                ClassNode leftComponent = left.getComponentType(), rightComponent = right.getComponentType();
                return checkCompatibleAssignmentTypes(leftComponent, rightComponent, rightExpression, false);
            }
            if (GeneralUtils.isOrImplements(right, Collection_TYPE) && !(rightExpression instanceof ListExpression)) {
                GenericsType elementType = GenericsUtils.parameterizeType(right, Collection_TYPE).getGenericsTypes()[0];
                return OBJECT_TYPE.equals(left.getComponentType()) // Object[] can accept any collection element type(s)
                    || (elementType.getLowerBound() == null && isCovariant(extractType(elementType), left.getComponentType()));
                    //  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ GROOVY-8984: "? super T" is only compatible with an Object[] target
            }
            if (GeneralUtils.isOrImplements(right, BaseStream_TYPE)) {
                GenericsType elementType = GenericsUtils.parameterizeType(right, BaseStream_TYPE).getGenericsTypes()[0];
                return isObjectType(left.getComponentType()) // Object[] can accept any stream API element type(s)
                    || (elementType.getLowerBound() == null && isCovariant(extractType(elementType), getWrapper(left.getComponentType())));
            }
        }

        ClassNode leftRedirect = left.redirect();
        ClassNode rightRedirect = right.redirect();
        if (leftRedirect == rightRedirect) return true;

        if (leftRedirect == VOID_TYPE) return rightRedirect == void_WRAPPER_TYPE;
        if (leftRedirect == void_WRAPPER_TYPE) return rightRedirect == VOID_TYPE;

        if (isLongCategory(getUnwrapper(leftRedirect))) {
            // byte, char, int, long or short can be assigned any base number
            if (isNumberType(rightRedirect) /*|| rightRedirect == char_TYPE*/) {
                return true;
            }
            if (leftRedirect == char_TYPE && rightRedirect == Character_TYPE) return true;
            if (leftRedirect == Character_TYPE && rightRedirect == char_TYPE) return true;
            if ((leftRedirect == char_TYPE || leftRedirect == Character_TYPE) && rightRedirect == STRING_TYPE) {
                return rightExpression instanceof ConstantExpression && rightExpression.getText().length() == 1;
            }
        } else if (isFloatingCategory(getUnwrapper(leftRedirect))) {
            // float or double can be assigned any base number type or BigDecimal
            if (isNumberType(rightRedirect) || isBigDecimalType(rightRedirect)) {
                return true;
            }
        } else if (left.isGenericsPlaceHolder()) { // must precede non-final types
            return right.getUnresolvedName().charAt(0) != '#' // RHS not adaptable
                    ? left.asGenericsType().isCompatibleWith(right) // GROOVY-7307, GROOVY-9952, GROOVY-11026
                    : implementsInterfaceOrSubclassOf(leftRedirect, rightRedirect); // GROOVY-10067, GROOVY-10342

        } else if (isBigDecimalType(leftRedirect) || Number_TYPE.equals(leftRedirect)) {
            // BigDecimal or Number can be assigned any derivative of java.lang.Number
            if (isNumberType(rightRedirect) || rightRedirect.isDerivedFrom(Number_TYPE)) {
                return true;
            }
        } else if (isBigIntegerType(leftRedirect)) {
            // BigInteger can be assigned byte, char, int, long, short or BigInteger
            if (isLongCategory(getUnwrapper(rightRedirect)) || rightRedirect.isDerivedFrom(BigInteger_TYPE)) {
                return true;
            }
        } else if (leftRedirect.isDerivedFrom(Enum_Type)) {
            // Enum types can be assigned String or GString (triggers `valueOf` call)
            if (rightRedirect == STRING_TYPE || isGStringOrGStringStringLUB(rightRedirect)) {
                return true;
            }
        } else if (isWildcardLeftHandSide(leftRedirect)) {
            // Object, String, [Bb]oolean or Class can be assigned anything (except null to boolean)
            return !(leftRedirect == boolean_TYPE && isNullConstant(rightExpression));
        }

        // if right is array, map or collection we try invoking the constructor
        if (allowConstructorCoercion && isGroovyConstructorCompatible(rightExpression)) {
            // TODO: in case of the array we could maybe make a partial check
            if (rightRedirect.isArray() && !leftRedirect.isArray()) {
                return false;
            }
            return true;
        }

        if (implementsInterfaceOrSubclassOf(getWrapper(right), left)) {
            return true;
        }

        if (right.isDerivedFrom(CLOSURE_TYPE) && isSAMType(left)) {
            return true;
        }

        // GROOVY-7316, GROOVY-10256: "Type x = m()" given "def <T> T m()"; T adapts to target
        return right.isGenericsPlaceHolder() && right.asGenericsType().isCompatibleWith(left);
    }

    private static boolean isGroovyConstructorCompatible(final Expression rightExpression) {
        return rightExpression instanceof ListExpression
                || rightExpression instanceof MapExpression
                || rightExpression instanceof ArrayExpression;
    }

    /**
     * Tells if a class is one of the "accept all" classes as the left hand side of an
     * assignment.
     *
     * @param node the classnode to test
     * @return true if it's an Object, String, boolean, Boolean or Class.
     */
    public static boolean isWildcardLeftHandSide(final ClassNode node) {
        return (isObjectType(node)
                || isStringType(node)
                || isPrimitiveBoolean(node)
                || isWrapperBoolean(node)
                || isClassType(node));
    }

    public static boolean isBeingCompiled(final ClassNode node) {
        return (node.getCompileUnit() != null);
    }

    @Deprecated(forRemoval = true, since = "3.0.0")
    static boolean checkPossibleLooseOfPrecision(final ClassNode left, final ClassNode right, final Expression rightExpr) {
        return checkPossibleLossOfPrecision(left, right, rightExpr);
    }

    static boolean checkPossibleLossOfPrecision(final ClassNode left, final ClassNode right, final Expression rightExpr) {
        if (left == right || left.equals(right)) return false; // identical types
        int leftIndex = NUMBER_TYPES.get(left);
        int rightIndex = NUMBER_TYPES.get(right);
        if (leftIndex >= rightIndex) return false;
        // here we must check if the right number is short enough to fit in the left type
        if (rightExpr instanceof ConstantExpression) {
            Object value = ((ConstantExpression) rightExpr).getValue();
            if (!(value instanceof Number)) return true;
            Number number = (Number) value;
            switch (leftIndex) {
                case 0: { // byte
                    byte val = number.byteValue();
                    if (number instanceof Short) {
                        return !Short.valueOf(val).equals(number);
                    }
                    if (number instanceof Integer) {
                        return !Integer.valueOf(val).equals(number);
                    }
                    if (number instanceof Long) {
                        return !Long.valueOf(val).equals(number);
                    }
                    if (number instanceof Float) {
                        return !Float.valueOf(val).equals(number);
                    }
                    return !Double.valueOf(val).equals(number);
                }
                case 1: { // short
                    short val = number.shortValue();
                    if (number instanceof Integer) {
                        return !Integer.valueOf(val).equals(number);
                    }
                    if (number instanceof Long) {
                        return !Long.valueOf(val).equals(number);
                    }
                    if (number instanceof Float) {
                        return !Float.valueOf(val).equals(number);
                    }
                    return !Double.valueOf(val).equals(number);
                }
                case 2: { // integer
                    int val = number.intValue();
                    if (number instanceof Long) {
                        return !Long.valueOf(val).equals(number);
                    }
                    if (number instanceof Float) {
                        return !Float.valueOf(val).equals(number);
                    }
                    return !Double.valueOf(val).equals(number);
                }
                case 3: { // long
                    long val = number.longValue();
                    if (number instanceof Float) {
                        return !Float.valueOf(val).equals(number);
                    }
                    return !Double.valueOf(val).equals(number);
                }
                case 4: { // float
                    float val = number.floatValue();
                    return !Double.valueOf(val).equals(number);
                }
                default: // double
                    return false; // no possible loss here
            }
        }
        return true; // possible loss of precision
    }

    static String toMethodParametersString(final String methodName, ClassNode... parameters) {
        if (parameters == null) parameters = ClassNode.EMPTY_ARRAY;
        var joiner = new StringJoiner(", ", methodName + "(", ")");
        for (ClassNode parameter : parameters) {
            joiner.add(prettyPrintType(parameter));
        }
        return joiner.toString();
    }

    /**
     * Returns string representation of type with generics. Arrays are indicated
     * with trailing "[]".
     */
    static String prettyPrintType(final ClassNode type) {
        if (type instanceof UnionTypeClassNode) {
            StringJoiner joiner = new StringJoiner(" or "); // GROOVY-11289
            for (ClassNode cn : ((UnionTypeClassNode) type).getDelegates())
                joiner.add(prettyPrintType(cn));
            return joiner.toString();
        }
        if (type.getUnresolvedName().charAt(0) == '#') {
            return type.redirect().toString(false);
        }
        return type.toString(false);
    }

    /**
     * Returns string representation of type without any type arguments. Arrays
     * are indicated with trailing "[]".
     */
    static String prettyPrintTypeName(final ClassNode type) {
        if (type.isArray()) {
            return prettyPrintTypeName(type.getComponentType()) + "[]";
        }
        return type.isGenericsPlaceHolder() ? type.getUnresolvedName() : type.getText();
    }

    public static boolean implementsInterfaceOrIsSubclassOf(final ClassNode type, final ClassNode superOrInterface) {
        if (type.isArray() && superOrInterface.isArray()) {
            return implementsInterfaceOrIsSubclassOf(type.getComponentType(), superOrInterface.getComponentType());
        }
        if (type == UNKNOWN_PARAMETER_TYPE // aka null
                || type.isDerivedFrom(superOrInterface)
                || type.implementsInterface(superOrInterface)) {
            return true;
        }
        if (superOrInterface instanceof WideningCategories.LowestUpperBoundClassNode) {
            if (implementsInterfaceOrIsSubclassOf(type, superOrInterface.getSuperClass())
                    && Arrays.stream(superOrInterface.getInterfaces()).allMatch(type::implementsInterface)) {
                return true;
            }
        } else if (superOrInterface instanceof UnionTypeClassNode) {
            for (ClassNode delegate : ((UnionTypeClassNode) superOrInterface).getDelegates()) {
                if (implementsInterfaceOrIsSubclassOf(type, delegate)) {
                    return true;
                }
            }
        }
        if (isGroovyObjectType(superOrInterface) && isBeingCompiled(type) && !type.isInterface()) {//TODO: !POJO !Trait
            return true;
        }
        return false;
    }

    private static final Integer NON_NUMBER_DEFAULT = 9; // GROOVY-10869: boolean, char, ...

    static int getPrimitiveDistance(final ClassNode primA, final ClassNode primB) {
        return Math.abs(NUMBER_TYPES.getOrDefault(primA, NON_NUMBER_DEFAULT)
                      - NUMBER_TYPES.getOrDefault(primB, NON_NUMBER_DEFAULT));
    }

    static int getDistance(final ClassNode actual, final ClassNode expect) {
        if (actual.isArray() && expect.isArray()) {
            return getDistance(actual.getComponentType(), expect.getComponentType());
        }
        if (isGStringOrGStringStringLUB(actual) && isStringType(expect)) {
            return 3; // GROOVY-6668, GROOVY-8212: closer than Object and GroovyObjectSupport
        }
        int dist = 0;
        ClassNode unwrapActual = getUnwrapper(actual);
        ClassNode unwrapExpect = getUnwrapper(expect);
        if (isPrimitiveType(unwrapActual)
                && isPrimitiveType(unwrapExpect)
                && unwrapActual != unwrapExpect) {
            dist = getPrimitiveDistance(unwrapActual, unwrapExpect);
        }
        // Add a penalty against boxing/unboxing to get a resolution similar to JLS 15.12.2
        // (http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.2).
        if (isPrimitiveType(actual) ^ isPrimitiveType(expect)) {
            dist = (dist + 1) << 1;
        }
        if (unwrapExpect.equals(unwrapActual)
                || actual == UNKNOWN_PARAMETER_TYPE) {
            return dist;
        }
        if (actual.isArray()) {
            dist += 131; // GROOVY-5114, GROOVY-11073: Object[] vs Object
        }
        if (expect.isInterface()) { MethodNode sam;
            if (actual.implementsInterface(expect)) {
                return dist + getMaximumInterfaceDistance(actual, expect);
            } else if (actual.equals(CLOSURE_TYPE) && (sam = findSAM(expect)) != null) {
                // in case of multiple overloads, give preference to same parameter count
                // with fuzzy matching of count for implicit-parameter closures / lambdas
                Integer closureParamCount = actual.getNodeMetaData(StaticTypesMarker.CLOSURE_ARGUMENTS);
                if (closureParamCount != null) { int samParamCount = sam.getParameters().length;
                    if ((closureParamCount == samParamCount) ||                                  // GROOVY-9881
                        (closureParamCount == -1 && (samParamCount == 0 || samParamCount == 1))) // GROOVY-10905
                        dist -= 1;
                    else if (closureParamCount != -1)
                        dist += 2; // GROOVY-11121: Object or T may match better
                }

                return dist + 13; // GROOVY-9852: @FunctionalInterface vs Object
            }
        }
        ClassNode cn = isPrimitiveType(actual) && !isPrimitiveType(expect) ? getWrapper(actual) : actual;
        while (cn != null && !cn.equals(expect)) {
            cn = cn.getSuperClass();
            dist += 1;
            if (isObjectType(cn))
                dist += 1;
            dist = (dist + 1) << 1;
        }
        return dist;
    }

    private static int getMaximumInterfaceDistance(final ClassNode c, final ClassNode interfaceClass) {
        // -1 means a mismatch
        if (c == null) return -1;
        // 0 means a direct match
        if (c.equals(interfaceClass)) return 0;
        ClassNode[] interfaces = c.getInterfaces();
        int max = -1;
        for (ClassNode anInterface : interfaces) {
            int sub = getMaximumInterfaceDistance(anInterface, interfaceClass);
            // we need to keep the -1 to track the mismatch, a +1
            // by any means could let it look like a direct match
            // we want to add one, because there is an interface between
            // the interface we search for and the interface we are in.
            if (sub != -1) {
                sub += 1;
            }
            // we are interested in the longest path only
            max = Math.max(max, sub);
        }
        // we do not add one for super classes, only for interfaces
        int superClassMax = getMaximumInterfaceDistance(c.getSuperClass(), interfaceClass);
        return Math.max(max, superClassMax);
    }

    /**
     * @deprecated Use {@link #findDGMMethodsByNameAndArguments(ClassLoader, ClassNode, String, ClassNode[], List)} instead
     */
    @Deprecated(forRemoval = true, since = "2.2.0")
    public static List<MethodNode> findDGMMethodsByNameAndArguments(final ClassNode receiver, final String name, final ClassNode[] args) {
        return findDGMMethodsByNameAndArguments(MetaClassRegistryImpl.class.getClassLoader(), receiver, name, args);
    }

    public static List<MethodNode> findDGMMethodsByNameAndArguments(final ClassLoader loader, final ClassNode receiver, final String name, final ClassNode[] args) {
        return findDGMMethodsByNameAndArguments(loader, receiver, name, args, new LinkedList<>());
    }

    /**
     * @deprecated Use {@link #findDGMMethodsByNameAndArguments(ClassLoader, ClassNode, String, ClassNode[], List)} instead
     */
    @Deprecated(forRemoval = true, since = "2.2.0")
    public static List<MethodNode> findDGMMethodsByNameAndArguments(final ClassNode receiver, final String name, final ClassNode[] args, final List<MethodNode> methods) {
        return findDGMMethodsByNameAndArguments(MetaClassRegistryImpl.class.getClassLoader(), receiver, name, args, methods);
    }

    public static List<MethodNode> findDGMMethodsByNameAndArguments(final ClassLoader loader, final ClassNode receiver, final String name, final ClassNode[] args, final List<MethodNode> methods) {
        methods.addAll(findDGMMethodsForClassNode(loader, receiver, name));
        return methods.isEmpty() ? methods : chooseBestMethod(receiver, methods, args);
    }

    /**
     * Returns true if the provided class node, when considered as a receiver of a message or as a parameter,
     * is using a placeholder in its generics type. In this case, we're facing unchecked generics and type
     * checking is limited (ex: void foo(Set s) { s.keySet() }
     *
     * @param node the node to test
     * @return true if it is using any placeholder in generics types
     */
    public static boolean isUsingUncheckedGenerics(final ClassNode node) {
        return GenericsUtils.hasUnresolvedGenerics(node);
    }

    /**
     * Returns the method(s) which best fit the argument types.
     *
     * @return zero or more results
     */
    public static List<MethodNode> chooseBestMethod(final ClassNode receiver, Collection<MethodNode> methods, final ClassNode... argumentTypes) {
        if (!asBoolean(methods)) {
            return Collections.emptyList();
        }

        // GROOVY-8965: type disjunction
        boolean duckType = receiver instanceof UnionTypeClassNode;
        if (methods.size() > 1 && !methods.iterator().next().isConstructor())
            methods = removeCovariantsAndInterfaceEquivalents(methods, duckType);

        Set<MethodNode> bestMethods = new HashSet<>(); // choose best method(s) for each possible receiver
        for (ClassNode rcvr : duckType ? ((UnionTypeClassNode) receiver).getDelegates() : new ClassNode[]{receiver}) {
            bestMethods.addAll(chooseBestMethods(rcvr, methods, argumentTypes));
        }
        return new LinkedList<>(bestMethods); // assumes caller wants remove to be inexpensive
    }

    private static List<MethodNode> chooseBestMethods(final ClassNode receiver, Collection<MethodNode> methods, final ClassNode[] argumentTypes) {
        List<MethodNode> bestMethods = new ArrayList<>();

        // phase 1: argument-parameter distance classifier
        int bestDist = Integer.MAX_VALUE;
        for (MethodNode method : methods) {
            Parameter[] parameters = method.getParameters();
            int nParameters = parameters.length;
            if (nParameters > 0) {
                parameters = parameters.clone();
                for (int i = 0; i < nParameters; i += 1) {
                    Parameter p = parameters[i];
                    ClassNode t = p.getOriginType();
                    if (t.isGenericsPlaceHolder() || isUsingGenericsOrIsArrayUsingGenerics(t))
                        parameters[i] = new Parameter(GenericsUtils.nonGeneric(t), p.getName());
                }
            }

            int dist = measureParametersAndArgumentsDistance(parameters, argumentTypes);
            if (dist >= 0 && dist <= bestDist) {
                if (dist < bestDist) {
                    bestDist = dist;
                    bestMethods.clear();
                }
                bestMethods.add(method);
            }
        }

        // phase 2: receiver-provider distance classifier
        if (bestMethods.size() > 1 && receiver != null) {
            methods = bestMethods;
            bestDist = Integer.MAX_VALUE;
            bestMethods = new ArrayList<>();
            for (MethodNode method : methods) {
                int dist = getClassDistance(method.getDeclaringClass(), receiver);
                if (dist <= bestDist) {
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestMethods.clear();
                    }
                    bestMethods.add(method);
                }
            }
        }

        // phase 3: prefer extension method in case of tie
        if (bestMethods.size() > 1) {
            List<MethodNode> extensionMethods = new ArrayList<>();
            for (MethodNode method : bestMethods) {
                if (method instanceof ExtensionMethodNode) {
                    extensionMethods.add(method);
                }
            }
            if (extensionMethods.size() == 1) {
                bestMethods = extensionMethods;
            }
        }

        return bestMethods;
    }

    @SuppressWarnings("removal")
    private static int measureParametersAndArgumentsDistance(final Parameter[] parameters, final ClassNode[] argumentTypes) {
        int dist = -1;
        if (parameters.length == argumentTypes.length) {
            dist = allParametersAndArgumentsMatch(parameters, argumentTypes);
            if (isVargs(parameters) && firstParametersAndArgumentsMatch(parameters, argumentTypes) >= 0) {
                int endDist = lastArgMatchesVarg(parameters, argumentTypes);
                if (endDist >= 0) {
                    endDist += getVarargsDistance(parameters);
                    dist = (dist < 0 ? endDist : Math.min(dist, endDist)); // GROOVY-8737
                }
            }
        } else if (isVargs(parameters)) {
            dist = firstParametersAndArgumentsMatch(parameters, argumentTypes);
            if (dist >= 0) {
                // varargs methods must not be preferred to methods without varargs
                // for example :
                // int sum(int x) should be preferred to int sum(int x, int... y)
                dist += getVarargsDistance(parameters);
                // there are three case for vargs
                // (1) varg part is left out (there's one less argument than there are parameters)
                // (2) last argument is put in the vargs array
                //     that case is handled above already when params and args have the same length
                if (parameters.length < argumentTypes.length) {
                    // (3) there is more than one argument for the vargs array
                    int excessArgumentsDistance = excessArgumentsMatchesVargsParameter(parameters, argumentTypes);
                    if (excessArgumentsDistance >= 0) {
                        dist += excessArgumentsDistance;
                    } else {
                        dist = -1;
                    }
                }
            }
        }
        return dist;
    }

    private static int firstParametersAndArgumentsMatch(final Parameter[] parameters, final ClassNode[] safeArgumentTypes) {
        int dist = 0;
        // check first parameters
        if (parameters.length > 0) {
            Parameter[] firstParams = new Parameter[parameters.length - 1];
            System.arraycopy(parameters, 0, firstParams, 0, firstParams.length);
            dist = allParametersAndArgumentsMatch(firstParams, safeArgumentTypes);
        }
        return dist;
    }

    private static int getVarargsDistance(final Parameter[] parameters) {
        return 256 - parameters.length; // ensure exact matches are preferred over vargs
    }

    private static int getClassDistance(final ClassNode declaringClassForDistance, final ClassNode actualReceiverForDistance) {
        if (actualReceiverForDistance.equals(declaringClassForDistance)) {
            return 0;
        }
        return getDistance(actualReceiverForDistance, declaringClassForDistance);
    }

    private static List<MethodNode> removeCovariantsAndInterfaceEquivalents(final Collection<MethodNode> collection, final boolean disjoint) {
        List<MethodNode> list = new ArrayList<>(new LinkedHashSet<>(collection)), toBeRemoved = new ArrayList<>();
        for (int i = 0, n = list.size(); i < n - 1; i += 1) {
            MethodNode one = list.get(i);
            for (int j = i + 1; j < n && !toBeRemoved.contains(one); j += 1) {
                MethodNode two = list.get(j);
                if (toBeRemoved.contains(two)) continue;
                if (one.getParameters().length == two.getParameters().length) {
                    ClassNode oneDC = one.getDeclaringClass(), twoDC = two.getDeclaringClass();
                    if (oneDC == twoDC || isSynthetic(one,two)||isSynthetic(two,one)) { // GROOVY-11341
                        if (ParameterUtils.parametersEqual(one.getParameters(), two.getParameters())) {
                            ClassNode oneRT = one.getReturnType(), twoRT = two.getReturnType();
                            if (isCovariant(oneRT, twoRT)) {
                                toBeRemoved.add(two);
                            } else if (isCovariant(twoRT, oneRT)) {
                                toBeRemoved.add(one);
                            }
                        } else {
                            // imperfect solution to determining if two methods are
                            // equivalent, for example String#compareTo(Object) and
                            // String#compareTo(String) -- in that case, the Object
                            // version is marked as synthetic
                            if (isSynthetic(one, two)) {
                                toBeRemoved.add(one);
                            } else if (isSynthetic(two, one)) {
                                toBeRemoved.add(two);
                            }
                        }
                    } else if (!oneDC.equals(twoDC)) {
                        if (ParameterUtils.parametersEqual(one.getParameters(), two.getParameters())) {
                            // GROOVY-6882, GROOVY-6970: drop overridden or interface equivalent method
                            if (!twoDC.isInterface() ? oneDC.isDerivedFrom(twoDC) : oneDC.implementsInterface(twoDC) || // GROOVY-10897: concrete vs. abstract
                                                                                    (!disjoint && !one.isAbstract() && !(two instanceof ExtensionMethodNode))) {
                                toBeRemoved.add(two);
                            } else if (oneDC.isInterface() ? (disjoint ? twoDC.implementsInterface(oneDC) : twoDC.isInterface()) : twoDC.isDerivedFrom(oneDC)) {
                                toBeRemoved.add(one);
                            }
                        }
                    }
                }
            }
        }
        if (toBeRemoved.isEmpty()) return list;

        List<MethodNode> result = new LinkedList<>(list);
        result.removeAll(toBeRemoved);
        return result;
    }

    private static boolean isCovariant(final ClassNode one, final ClassNode two) {
        if (one.isArray() && two.isArray()) {
            return isCovariant(one.getComponentType(), two.getComponentType());
        }
        return (one.isDerivedFrom(two) || one.implementsInterface(two));
    }

    private static boolean isSynthetic(final MethodNode one, final MethodNode two) {
        return ((one.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0)
            && ((two.getModifiers() & Opcodes.ACC_SYNTHETIC) == 0);
    }

    /**
     * Given a receiver and a method node, parameterize the method arguments using
     * available generic type information.
     *
     * @param receiver the class
     * @param m        the method
     * @return the parameterized arguments
     */
    public static Parameter[] parameterizeArguments(final ClassNode receiver, final MethodNode m) {
        Map<GenericsTypeName, GenericsType> genericFromReceiver = GenericsUtils.extractPlaceholders(receiver);
        Map<GenericsTypeName, GenericsType> contextPlaceholders = extractGenericsParameterMapOfThis(m);
        Parameter[] methodParameters = m.getParameters();
        Parameter[] params = new Parameter[methodParameters.length];
        for (int i = 0, n = methodParameters.length; i < n; i += 1) {
            Parameter methodParameter = methodParameters[i];
            ClassNode paramType = methodParameter.getType();
            params[i] = buildParameter(genericFromReceiver, contextPlaceholders, methodParameter, paramType);
        }
        return params;
    }

    /**
     * Given a parameter, builds a new parameter for which the known generics placeholders are resolved.
     *
     * @param genericFromReceiver      resolved generics from the receiver of the message
     * @param placeholdersFromContext  resolved generics from the method context
     * @param methodParameter          the method parameter for which we want to resolve generic types
     * @param paramType                the (unresolved) type of the method parameter
     * @return a new parameter with the same name and type as the original one, but with resolved generic types
     */
    private static Parameter buildParameter(final Map<GenericsTypeName, GenericsType> genericFromReceiver, final Map<GenericsTypeName, GenericsType> placeholdersFromContext, final Parameter methodParameter, final ClassNode paramType) {
        if (genericFromReceiver.isEmpty() && (placeholdersFromContext == null || placeholdersFromContext.isEmpty())) {
            return methodParameter;
        }
        if (paramType.isArray()) {
            ClassNode componentType = paramType.getComponentType();
            Parameter subMethodParameter = new Parameter(componentType, methodParameter.getName());
            Parameter component = buildParameter(genericFromReceiver, placeholdersFromContext, subMethodParameter, componentType);
            return new Parameter(component.getType().makeArray(), component.getName());
        }
        ClassNode resolved = resolveClassNodeGenerics(genericFromReceiver, placeholdersFromContext, paramType);

        return new Parameter(resolved, methodParameter.getName());
    }

    /**
     * Returns true if a class makes use of generic types. If node represents an
     * array type, then checks if the component type is using generics.
     *
     * @param cn a class node for which to check if it is using generics
     * @return true if the type (or component type) is using generics
     */
    public static boolean isUsingGenericsOrIsArrayUsingGenerics(final ClassNode cn) {
        if (cn.isArray()) {
            return isUsingGenericsOrIsArrayUsingGenerics(cn.getComponentType());
        }
        if (cn.isUsingGenerics()) {
            if (cn.getGenericsTypes() != null) {
                return true;
            }
            ClassNode oc = cn.getOuterClass();
            if (oc != null && oc.getGenericsTypes() != null
                    && (cn.getModifiers() & Opcodes.ACC_STATIC) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a generics type representing SomeClass&lt;T,V&gt; and a resolved placeholder map, returns a new generics type
     * for which placeholders are resolved recursively.
     */
    protected static GenericsType fullyResolve(GenericsType gt, final Map<GenericsTypeName, GenericsType> placeholders) {
        if (gt.isPlaceholder()) {
            gt = placeholders.getOrDefault(new GenericsTypeName(gt.getName()), gt);
        }
        if (!gt.isPlaceholder() && !gt.isWildcard()) {
            return new GenericsType(fullyResolveType(gt.getType(), placeholders), null, null);
        }

        ClassNode lowerBound = gt.getLowerBound();
        ClassNode[] upperBounds = gt.getUpperBounds();
        if (lowerBound != null) {
            lowerBound = fullyResolveType(lowerBound, placeholders);
            upperBounds = null; // should be the case; let's be sure
        } else if (upperBounds != null) {
            upperBounds = upperBounds.clone();
            for (int i = 0, n = upperBounds.length; i < n; i += 1) {
                upperBounds[i] = fullyResolveType(upperBounds[i], placeholders);
            }
        } else {
            upperBounds = new ClassNode[]{fullyResolveType(gt.getType(), placeholders)};
        }

        GenericsType genericsType = new GenericsType(makeWithoutCaching("?"), upperBounds, lowerBound);
        genericsType.getType().setRedirect(OBJECT_TYPE);
        genericsType.setWildcard(true);
        return genericsType;
    }

    protected static ClassNode fullyResolveType(final ClassNode type, final Map<GenericsTypeName, GenericsType> placeholders) {
        if (type.isArray()) {
            return fullyResolveType(type.getComponentType(), placeholders).makeArray();
        }
        if (!type.isUsingGenerics()) {
            return type;
        }
        if (type.isGenericsPlaceHolder()) {
            GenericsType gt = placeholders.get(new GenericsTypeName(type.getUnresolvedName()));
            if (gt != null) {
                return gt.getType();
            }
            ClassNode cn = extractType(type.asGenericsType()); // GROOVY-10756
            return cn != type ? cn : OBJECT_TYPE; // do not return placeholder
        }

        GenericsType[] gts = type.getGenericsTypes();
        if (ArrayGroovyMethods.asBoolean(gts)) {
            gts = gts.clone();
            for (int i = 0, n = gts.length; i < n; i += 1) {
                GenericsType gt = gts[i];
                if (gt.isPlaceholder()) { String name = gt.getName();
                    gt = placeholders.get(new GenericsTypeName(name));
                    if (gt == null) gt = extractType(gts[i]).asGenericsType();
                    // GROOVY-10364: skip placeholder from the enclosing context
                    if (gt.isPlaceholder() && gt.getName().equals(name)) continue;

                    gts[i] = gt;
                } else {
                    gts[i] = fullyResolve(gt, placeholders);
                }
            }
        }

        ClassNode cn = type.getPlainNodeReference();
        cn.setGenericsTypes(gts);
        return cn;
    }

    /**
     * Checks that the parameterized generics of an argument are compatible with the generics of the parameter.
     *
     * @param parameterType the parameter type of a method
     * @param argumentType  the type of the argument passed to the method
     */
    protected static boolean typeCheckMethodArgumentWithGenerics(final ClassNode parameterType, final ClassNode argumentType, final boolean lastArg) {
        if (UNKNOWN_PARAMETER_TYPE == argumentType) { // argument is null
            return !isPrimitiveType(parameterType);
        }
        boolean isArrayParameter = parameterType.isArray();
        if (!isAssignableTo(argumentType, parameterType)) {
            if (!lastArg || !isArrayParameter
                    || !isAssignableTo(argumentType, parameterType.getComponentType())) {
                return false; // incompatible assignment
            }
        }
        if (isArrayParameter && argumentType.isArray()) {
            // verify component type
            return typeCheckMethodArgumentWithGenerics(parameterType.getComponentType(), argumentType.getComponentType(), lastArg);

        } else if (isArrayParameter && lastArg) {
            // verify component type, but if we reach that point, the only possibility is that the argument is
            // the last one of the call, so we're in the cast of a vargs call
            // (otherwise, we face a type checker bug)
            return typeCheckMethodArgumentWithGenerics(parameterType.getComponentType(), argumentType, lastArg);

        } else if (parameterType.isUsingGenerics() && argumentType.isUsingGenerics()) {
            if (!GenericsUtils.buildWildcardType(parameterType).isCompatibleWith(argumentType)) {
                boolean samCoercion = argumentType.equals(CLOSURE_TYPE) && isSAMType(parameterType);
                if (!samCoercion) return false; // else assume parameters and return checked earlier
            }
        }
        return true;
    }

    protected static boolean typeCheckMethodsWithGenerics(final ClassNode receiver, final ClassNode[] argumentTypes, final MethodNode candidateMethod) {
        if (candidateMethod instanceof ExtensionMethodNode) {
            ClassNode[] realTypes = new ClassNode[argumentTypes.length + 1];
            realTypes[0] = receiver; // object expression is implicit argument
            System.arraycopy(argumentTypes, 0, realTypes, 1, argumentTypes.length);
            MethodNode realMethod = ((ExtensionMethodNode) candidateMethod).getExtensionMethodNode();
            return typeCheckMethodsWithGenerics(realMethod.getDeclaringClass(), realTypes, realMethod, true);
        }

        if (receiver.isUsingGenerics()
                && isClassType(receiver)
                && !isClassType(candidateMethod.getDeclaringClass())) {
            return typeCheckMethodsWithGenerics(receiver.getGenericsTypes()[0].getType(), argumentTypes, candidateMethod);
        }

        return typeCheckMethodsWithGenerics(StaticTypeCheckingVisitor.wrapTypeIfNecessary(receiver), argumentTypes, candidateMethod, false);
    }

    private static boolean typeCheckMethodsWithGenerics(final ClassNode receiver, final ClassNode[] argumentTypes, final MethodNode candidateMethod, final boolean isExtensionMethod) {
        Parameter[] parameters = candidateMethod.getParameters();
        if (parameters.length == 0 || parameters.length > argumentTypes.length) {
            // this is a limitation that must be removed in a future version; we
            // cannot check generic type arguments if there is default argument!
            return true;
        }

        boolean failure = false;
        Set<GenericsTypeName> fixedPlaceHolders = Collections.emptySet();
        Map<GenericsTypeName, GenericsType> candidateGenerics = new HashMap<>();
        // correct receiver for inner class
        // we assume the receiver is an instance of the declaring class of the
        // candidate method, but findMethod() returns also outer class methods
        // for that receiver; for now we skip receiver-based checks in that case
        boolean skipBecauseOfInnerClassNotReceiver = !implementsInterfaceOrIsSubclassOf(receiver, candidateMethod.getDeclaringClass());
        if (!skipBecauseOfInnerClassNotReceiver) {
            if (candidateMethod instanceof ConstructorNode) {
                candidateGenerics = GenericsUtils.extractPlaceholders(receiver);
                fixedPlaceHolders = new HashSet<>(candidateGenerics.keySet());
            } else {
                failure = inferenceCheck(fixedPlaceHolders, candidateGenerics, candidateMethod.getDeclaringClass(), receiver, false);

                GenericsType[] gts = candidateMethod.getGenericsTypes();
                if (candidateMethod.isStatic()) {
                    candidateGenerics.clear(); // not in scope
                } else if (gts != null) {
                    // first remove hidden params
                    for (GenericsType gt : gts) {
                        candidateGenerics.remove(new GenericsTypeName(gt.getName()));
                    }
                    // GROOVY-8034: non-static method may use class generics
                    gts = applyGenericsContext(candidateGenerics, gts);
                }
                GenericsUtils.extractPlaceholders(GenericsUtils.makeClassSafe0(OBJECT_TYPE, gts), candidateGenerics);

                // the outside context parts till now define placeholder we are not allowed to
                // generalize, thus we save that for later use...
                // extension methods are special, since they set the receiver as
                // first parameter. While we normally allow generalization for the first
                // parameter, in case of an extension method we must not.
                fixedPlaceHolders = extractResolvedPlaceHolders(candidateGenerics);
            }
        }

        int lastParamIndex = parameters.length - 1;
        for (int i = 0, n = argumentTypes.length; i < n; i += 1) {
            ClassNode parameterType = parameters[Math.min(i, lastParamIndex)].getOriginType();
            ClassNode argumentType = StaticTypeCheckingVisitor.wrapTypeIfNecessary(argumentTypes[i]);
            failure |= inferenceCheck(fixedPlaceHolders, candidateGenerics, parameterType, argumentType, i >= lastParamIndex);

            if (i == 0 && isExtensionMethod) { // re-load fixed names for extension
                fixedPlaceHolders = extractResolvedPlaceHolders(candidateGenerics);
            }
        }
        return !failure;
    }

    private static Set<GenericsTypeName> extractResolvedPlaceHolders(final Map<GenericsTypeName, GenericsType> resolvedMethodGenerics) {
        if (resolvedMethodGenerics.isEmpty()) return Collections.emptySet();
        Set<GenericsTypeName> result = new HashSet<>();
        for (Map.Entry<GenericsTypeName, GenericsType> entry : resolvedMethodGenerics.entrySet()) {
            GenericsType value = entry.getValue();
            if (value.isPlaceholder()) continue;
            result.add(entry.getKey());
        }
        return result;
    }

    private static boolean inferenceCheck(final Set<GenericsTypeName> fixedPlaceHolders, final Map<GenericsTypeName, GenericsType> resolvedMethodGenerics, ClassNode type, final ClassNode wrappedArgument, final boolean lastArg) {
        // GROOVY-8090, GROOVY-11003: handle vararg generics like "T x = ...; Arrays.asList(x)"
        if (lastArg && type.isArray() && dimensions(type) != dimensions(wrappedArgument)
                && isUsingGenericsOrIsArrayUsingGenerics(type.getComponentType())) {
            type = type.getComponentType();
        }
        // the context we compare with in the end is the one of the callsite
        // so far we specified the context of the method declaration only
        // thus for each argument, we try to find the connected generics first
        Map<GenericsTypeName, GenericsType> connections = new HashMap<>();
        extractGenericsConnections(connections, wrappedArgument, type);

        // each new connection must comply with previous connections
        for (Map.Entry<GenericsTypeName, GenericsType> entry : connections.entrySet()) {
            GenericsType candidate = entry.getValue(), resolved = resolvedMethodGenerics.get(entry.getKey());
            if (resolved == null || (candidate.isPlaceholder() && !hasNonTrivialBounds(candidate))) continue;

            if (!compatibleConnection(resolved, candidate)) {
                if (!resolved.isPlaceholder() && !resolved.isWildcard()
                        && !fixedPlaceHolders.contains(entry.getKey())) {
                    // GROOVY-5692, GROOVY-10006: multiple witnesses
                    if (compatibleConnection(candidate, resolved)) {
                        // was "T=Integer" and now is "T=Number" or "T=Object"
                        resolvedMethodGenerics.put(entry.getKey(), candidate);
                        continue;
                    } else if (!candidate.isPlaceholder() && !candidate.isWildcard()) {
                        // combine "T=Integer" and "T=String" to produce "T=? extends Serializable & Comparable<...>"
                        ClassNode lub = lowestUpperBound(candidate.getType(), resolved.getType());
                        resolvedMethodGenerics.put(entry.getKey(), lub.asGenericsType());
                        continue;
                    }
                }
                return true; // incompatible
            }
        }

        connections.keySet().removeAll(fixedPlaceHolders); // GROOVY-10337

        // apply the new information to refine the method level information so
        // that the information slowly becomes information for the callsite
        applyGenericsConnections(connections, resolvedMethodGenerics);
        // since it is possible that the callsite uses some generics as well,
        // we may have to add additional elements here
        addMissingEntries(connections, resolvedMethodGenerics);
        // to finally see if the parameter and the argument fit together,
        // we use the provided information to transform the parameter
        // into something that can exist in the callsite context
        ClassNode resolvedType = applyGenericsContext(resolvedMethodGenerics, type);
        return !typeCheckMethodArgumentWithGenerics(resolvedType, wrappedArgument, lastArg);
    }

    private static int dimensions(ClassNode cn) {
        int dims = 0;
        while (cn.isArray()) {
            cn = cn.getComponentType();
            dims += 1;
        }
        return dims;
    }

    private static boolean compatibleConnection(final GenericsType resolved, final GenericsType connection) {
        if (resolved.isPlaceholder()
                &&  resolved.getUpperBounds() != null
                &&  resolved.getUpperBounds().length == 1
                && !resolved.getUpperBounds()[0].isGenericsPlaceHolder()
                &&  resolved.getUpperBounds()[0].getName().equals(OBJECT)) {
            return true;
        }

        ClassNode resolvedType;
        if (hasNonTrivialBounds(resolved)) {
            resolvedType = getCombinedBoundType(resolved);
            resolvedType = resolvedType.getPlainNodeReference();
        } else if (!resolved.isPlaceholder()) {
            resolvedType = resolved.getType().getPlainNodeReference();
        } else {
            return true;
        }

        GenericsType gt;
        if (connection.isWildcard()) {
            gt = connection;
        } else if (!connection.isPlaceholder() && connection.getType().equals(CLOSURE_TYPE) && isSAMType(resolvedType)) {
            return true; // GROOVY-10270: SAM-type (placeholder) accepts closure
        } else { // test compatibility with "? super Type"
            ClassNode lowerBound = connection.getType().getPlainNodeReference();
            if (hasNonTrivialBounds(connection)) {
                lowerBound.setGenericsTypes(new GenericsType[] {connection});
            }
            gt = new GenericsType(makeWithoutCaching("?"), null, lowerBound);
            gt.setWildcard(true);
        }
        return gt.isCompatibleWith(resolvedType);
    }

    private static void addMissingEntries(final Map<GenericsTypeName, GenericsType> connections, final Map<GenericsTypeName, GenericsType> resolved) {
        for (Map.Entry<GenericsTypeName, GenericsType> entry : connections.entrySet()) {
            if (resolved.containsKey(entry.getKey())) continue;
            GenericsType gt = entry.getValue();
            ClassNode cn = gt.getType();
            if (cn.redirect() == UNKNOWN_PARAMETER_TYPE) continue;
            resolved.put(entry.getKey(), gt);
        }
    }

    public static ClassNode resolveClassNodeGenerics(Map<GenericsTypeName, GenericsType> resolvedPlaceholders, final Map<GenericsTypeName, GenericsType> placeholdersFromContext, final ClassNode currentType) {
        ClassNode type = currentType; // GROOVY-10280, et al.
        type = applyGenericsContext(resolvedPlaceholders, type);
        type = applyGenericsContext(placeholdersFromContext, type);
        return type;
    }

    static void applyGenericsConnections(final Map<GenericsTypeName, GenericsType> connections, final Map<GenericsTypeName, GenericsType> resolvedPlaceholders) {
        if (connections == null || connections.isEmpty()) return;
        for (Map.Entry<GenericsTypeName, GenericsType> entry : resolvedPlaceholders.entrySet()) {
            // entry could be T=T, T=T extends U, T=V, T=String, T=? extends String, etc.
            GenericsType oldValue = entry.getValue();
            if (oldValue.isPlaceholder()) { // T=T or V, not T=String or ? ...
                GenericsTypeName name = new GenericsTypeName(oldValue.getName());
                GenericsType newValue = connections.get(name); // find "V" in T=V
                if (newValue == oldValue) continue;
                if (newValue == null) {
                    newValue = connections.get(entry.getKey());
                    if (newValue != null) { // GROOVY-10315, GROOVY-10317
                        newValue = getCombinedGenericsType(oldValue, newValue);
                    }
                }
                if (newValue == null) {
                    entry.setValue(newValue = applyGenericsContext(connections, oldValue));
                } else if (!newValue.isPlaceholder() || newValue != resolvedPlaceholders.get(name)) {
                    // GROOVY-6787: Don't override the original if the replacement doesn't respect the bounds otherwise
                    // the original bounds are lost, which can result in accepting an incompatible type as an argument!
                    ClassNode replacementType = extractType(newValue);
                    ClassNode suitabilityType = !replacementType.isGenericsPlaceHolder()
                            ? replacementType : Optional.ofNullable(replacementType.getGenericsTypes())
                                    .map(gts -> extractType(gts[0])).orElse(replacementType.redirect());

                    if (oldValue.isCompatibleWith(suitabilityType)) {
                        if (newValue.isWildcard() && newValue.getLowerBound() == null && newValue.getUpperBounds() == null) {
                            // GROOVY-9998: apply upper/lower bound for unknown
                            entry.setValue(replacementType.asGenericsType());
                        } else {
                            entry.setValue(newValue);
                        }
                    }
                }
            }
        }
    }

    private static ClassNode extractType(GenericsType gt) {
        ClassNode cn;
        if (!gt.isPlaceholder()) {
            cn = getCombinedBoundType(gt);
        } else {
            // discard the placeholder
            cn = gt.getType().redirect();

            if (gt.getType().getGenericsTypes() != null)
                gt = gt.getType().getGenericsTypes()[0];

            if (gt.getLowerBound() != null) {
                cn = gt.getLowerBound();
            } else if (asBoolean(gt.getUpperBounds())) {
                cn = gt.getUpperBounds()[0];
            }
        }
        return cn;
    }

    private static boolean equalIncludingGenerics(final GenericsType one, final GenericsType two) {
        if (one == two) return true;
        if (one.isWildcard() != two.isWildcard()) return false;
        if (one.isPlaceholder() != two.isPlaceholder()) return false;
        if (!equalIncludingGenerics(one.getType(), two.getType())) return false;
        ClassNode lower1 = one.getLowerBound();
        ClassNode lower2 = two.getLowerBound();
        if ((lower1 == null) ^ (lower2 == null)) return false;
        if (lower1 != lower2) {
            if (!equalIncludingGenerics(lower1, lower2)) return false;
        }
        ClassNode[] upper1 = one.getUpperBounds();
        ClassNode[] upper2 = two.getUpperBounds();
        if ((upper1 == null) ^ (upper2 == null)) return false;
        if (upper1 != upper2) {
            if (upper1.length != upper2.length) return false;
            for (int i = 0, n = upper1.length; i < n; i += 1) {
                if (!equalIncludingGenerics(upper1[i], upper2[i])) return false;
            }
        }
        return true;
    }

    private static boolean equalIncludingGenerics(final ClassNode one, final ClassNode two) {
        if (one == two) return true;
        if (one.isGenericsPlaceHolder() != two.isGenericsPlaceHolder()) return false;
        if (!one.equals(two)) return false;
        GenericsType[] gt1 = one.getGenericsTypes();
        GenericsType[] gt2 = two.getGenericsTypes();
        if ((gt1 == null) ^ (gt2 == null)) return false;
        if (gt1 != gt2) {
            if (gt1.length != gt2.length) return false;
            for (int i = 0, n = gt1.length; i < n; i += 1) {
                if (!equalIncludingGenerics(gt1[i], gt2[i])) return false;
            }
        }
        return true;
    }

    /**
     * Uses supplied type to make a connection from usage to declaration.
     * <p>
     * The method operates in two modes:
     * <ul>
     * <li>For type !instanceof target a structural compare will be done
     *     (for example Type&lt;T&gt; and List&lt;E&gt; to get E -> T)
     * <li>If type equals target, a structural match is done as well
     *     (for example Collection&lt;T&gt; and Collection&lt;E&gt; to get E -> T)
     * <li>Otherwise we climb the hierarchy to find a case of type equals target
     *     to then execute the structural match, while applying possibly existing
     *     generics contexts on the way (for example for IntRange and Collection&lt;E&gt;
     *     to get E -> Integer, since IntRange is an AbstractList&lt;Integer&gt;)
     * </ul>
     * Should the target not have any generics this method does nothing.
     */
    static void extractGenericsConnections(final Map<GenericsTypeName, GenericsType> connections, final ClassNode type, final ClassNode target) {
        if (target == null || target == type || (!target.isGenericsPlaceHolder() && !isUsingGenericsOrIsArrayUsingGenerics(target))) return;
        if (type == null || type == UNKNOWN_PARAMETER_TYPE) return;

        if (target.isGenericsPlaceHolder()) {
            storeGenericsConnection(connections, target.getUnresolvedName(), new GenericsType(type));

        } else if (type.isGenericsPlaceHolder()) {
            // "T extends java.util.List<X> -> java.util.List<E>" vs "java.util.List<E>"
            extractGenericsConnections(connections, extractType(new GenericsType(type)), target);

        } else if (type.isArray() && target.isArray()) {
            extractGenericsConnections(connections, type.getComponentType(), target.getComponentType());

        } else if (type.equals(CLOSURE_TYPE) && isSAMType(target)) {
            // GROOVY-9974, GROOVY-10052: Lambda, Closure, Pointer or Reference for SAM-type receiver
            ClassNode returnType = StaticTypeCheckingVisitor.wrapTypeIfNecessary(GenericsUtils.parameterizeSAM(target).getV2());
            extractGenericsConnections(connections, type.getGenericsTypes(), new GenericsType[]{ returnType.asGenericsType() });

        } else if (type.equals(target)) {
            extractGenericsConnections(connections, type.getGenericsTypes(), target.getGenericsTypes());
            extractGenericsConnections(connections, type.getNodeMetaData("outer.class"), target.getOuterClass()); //GROOVY-10646

        } else if (implementsInterfaceOrIsSubclassOf(type, target)) {
            ClassNode goal = GenericsUtils.parameterizeType(type, target);
            extractGenericsConnections(connections, goal.getGenericsTypes(), target.getGenericsTypes());
        }
    }

    private static void extractGenericsConnections(final Map<GenericsTypeName, GenericsType> connections, final GenericsType[] usage, final GenericsType[] declaration) {
        // if declaration does not provide generics, there is no connection to make
        if (usage == null || declaration == null || declaration.length == 0) return;
        if (usage.length != declaration.length) return;

        // both have generics
        for (int i = 0, n = usage.length; i < n; i += 1) {
            GenericsType ui = usage[i], di = declaration[i];
            if (di.isPlaceholder()) {     // di like "T"
                storeGenericsConnection(connections, di.getName(), ui);
            } else if (di.isWildcard()) { // di like "?", "? super T", "? extends T"
                if (isUnboundedWildcard(di)) continue;
                boolean lowerBound = (di.getLowerBound() != null);
                ClassNode boundType = lowerBound ? di.getLowerBound() : di.getUpperBounds()[0];
                if (ui.isWildcard()) {    // ui like "?", "? super Type", "? extends One & Two"
                    if (lowerBound)
                        extractGenericsConnections(connections, ui.getLowerBound(), boundType);
                    else if (ui.getUpperBounds()!=null) for (ClassNode ub: ui.getUpperBounds())
                        extractGenericsConnections(connections, ub, boundType); // GROOVY-10911
                } else {
                    if (boundType.isGenericsPlaceHolder()) { // di like "? extends/super T"
                        // 6731,7992,8983,9998,10047,10499,10749,10765,...
                        ui = new GenericsType(ui.getType()); // erase type
                        if (lowerBound) ui.setWildcard(true); // binds weak
                        String placeholderName = boundType.getUnresolvedName();
                        storeGenericsConnection(connections, placeholderName, ui);
                    } else { // di like "? extends Iterable<T>" and ui like "List<Type>"
                        extractGenericsConnections(connections, ui.getType(), boundType);
                    }
                }
            } else { // di like "List<T>", "List<Type>", "List<? extends T>", ...
                extractGenericsConnections(connections, ui.getType(), di.getType());
            }
        }
    }

    private static void storeGenericsConnection(final Map<GenericsTypeName, GenericsType> connections, final String placeholderName, final GenericsType gt) {
      //connections.merge(new GenericsTypeName(placeholderName), gt, (gt1, gt2) -> getCombinedGenericsType(gt1, gt2));
        connections.put(new GenericsTypeName(placeholderName), gt);
    }

    static GenericsType[] getGenericsWithoutArray(final ClassNode type) {
        if (type.isArray()) return getGenericsWithoutArray(type.getComponentType());
        return type.getGenericsTypes();
    }

    static GenericsType[] applyGenericsContext(final Map<GenericsTypeName, GenericsType> spec, final GenericsType[] gts) {
        if (gts == null || spec == null || spec.isEmpty()) return gts;

        int n = gts.length;
        if (n == 0) return gts;
        GenericsType[] newGTs = new GenericsType[n];
        for (int i = 0; i < n; i += 1) {
            newGTs[i] = applyGenericsContext(spec, gts[i]);
        }
        return newGTs;
    }

    private static GenericsType applyGenericsContext(final Map<GenericsTypeName, GenericsType> spec, final GenericsType gt) {
        ClassNode type = gt.getType();

        if (gt.isPlaceholder()) {
            GenericsTypeName name = new GenericsTypeName(gt.getName());
            GenericsType specType = spec.get(name);
            if (specType != null) return specType;
            if (hasNonTrivialBounds(gt)) {
                GenericsType newGT = new GenericsType(type, applyGenericsContext(spec, gt.getUpperBounds()), applyGenericsContext(spec, gt.getLowerBound()));
                newGT.setPlaceholder(true);
                return newGT;
            }
            return gt;
        }

        if (gt.isWildcard()) { // TODO: What if a bound itself resolves to a wildcard?
            ClassNode[] upperBounds = applyGenericsContext(spec, gt.getUpperBounds());
            ClassNode   lowerBound = applyGenericsContext(spec, gt.getLowerBound());
            GenericsType newGT = new GenericsType(type, upperBounds, lowerBound);
            newGT.setWildcard(true);
            return newGT;
        }

        ClassNode newType;
        if (type.isArray()) {
            newType = applyGenericsContext(spec, type.getComponentType()).makeArray();
        } else if (type.getGenericsTypes() == null//type.isGenericsPlaceHolder()
                && type.getOuterClass() == null) {
            return gt;
        } else {
            newType = type.getPlainNodeReference();
            newType.setGenericsPlaceHolder(type.isGenericsPlaceHolder());
            newType.setGenericsTypes(applyGenericsContext(spec, type.getGenericsTypes()));

            // GROOVY-10646: non-static inner class + outer class type parameter
            if ((type.getModifiers() & Opcodes.ACC_STATIC) == 0) {
                Optional.ofNullable(type.getOuterClass())
                    .filter(oc -> oc.getGenericsTypes()!=null)
                    .map(oc -> applyGenericsContext(spec, oc))
                    .ifPresent(oc -> newType.putNodeMetaData("outer.class", oc));
            }
        }
        return new GenericsType(newType);
    }

    private static boolean hasNonTrivialBounds(final GenericsType gt) {
        if (gt.isWildcard()) {
            return true;
        }
        if (gt.getLowerBound() != null) {
            return true;
        }
        ClassNode[] upperBounds = gt.getUpperBounds();
        if (upperBounds != null) {
            return (upperBounds.length != 1 || upperBounds[0].isGenericsPlaceHolder() || !isObjectType(upperBounds[0]));
        }
        return false;
    }

    static ClassNode[] applyGenericsContext(final Map<GenericsTypeName, GenericsType> spec, final ClassNode[] types) {
        if (types == null) return null;
        final int nTypes = types.length;
        ClassNode[] newTypes = new ClassNode[nTypes];
        for (int i = 0; i < nTypes; i += 1) {
            newTypes[i] = applyGenericsContext(spec, types[i]);
        }
        return newTypes;
    }

    static ClassNode applyGenericsContext(final Map<GenericsTypeName, GenericsType> spec, final ClassNode type) {
        if (type == null || !isUsingGenericsOrIsArrayUsingGenerics(type)) {
            return type;
        }
        if (type.isArray()) {
            return applyGenericsContext(spec, type.getComponentType()).makeArray();
        }

        GenericsType[] gt = type.getGenericsTypes();
        if (asBoolean(spec)) {
            gt = applyGenericsContext(spec, gt);
        }
        if (!type.isGenericsPlaceHolder()) { // convert Type<T> to Type<...>
            ClassNode cn = type.getPlainNodeReference();
            cn.setGenericsTypes(gt);
            return cn;
        }

        if (!gt[0].isPlaceholder()) { // convert T to Type or Type<...>
            return getCombinedBoundType(gt[0]);
        }

        if (type.getGenericsTypes()[0] != gt[0]) { // convert T to X
            ClassNode cn = make(gt[0].getName()) , erasure = getCombinedBoundType(gt[0]).redirect();
            cn.setGenericsPlaceHolder(true);
            cn.setGenericsTypes(gt);
            cn.setRedirect(erasure);
            return cn;
        }

        return type; // nothing to do
    }

    static ClassNode getCombinedBoundType(final GenericsType genericsType) {
        // TODO: This method should really return some kind of meta ClassNode
        // representing the combination of all bounds. The code here just picks
        // something out to be able to proceed and is not actually correct.
        if (hasNonTrivialBounds(genericsType)) {
            if (genericsType.getLowerBound() != null) return OBJECT_TYPE; // GROOVY-10328
            if (genericsType.getUpperBounds() != null) return genericsType.getUpperBounds()[0];
        }
        return genericsType.getType();
    }

    static GenericsType getCombinedGenericsType(final GenericsType gt1, final GenericsType gt2) {
        // GROOVY-7992, GROOVY-10765: "? super T" for gt1 or gt2?
        if (isUnboundedWildcard(gt1) != isUnboundedWildcard(gt2)) return isUnboundedWildcard(gt2) ? gt1 : gt2;
        // GROOVY-11028, et al.: empty list / map for gt1 or gt2?
        if (gt2.isPlaceholder() && gt2.getName().startsWith("#")) return gt1;
        if (gt1.isPlaceholder() && gt1.getName().startsWith("#")) return gt2;
        // GROOVY-10315, GROOVY-10317, GROOVY-10339, ...
        ClassNode cn1 = GenericsUtils.makeClassSafe0(CLASS_Type, gt1);
        ClassNode cn2 = GenericsUtils.makeClassSafe0(CLASS_Type, gt2);
        ClassNode lub = lowestUpperBound(cn1, cn2);
        return lub.getGenericsTypes()[0];
    }

    /**
     * Apply the bounds from the declared type when the using type simply declares a parameter as an unbounded wildcard.
     *
     * @param type A parameterized type
     * @return A parameterized type with more precise wildcards
     */
    static ClassNode boundUnboundedWildcards(final ClassNode type) {
        if (type.isArray()) {
            return boundUnboundedWildcards(type.getComponentType()).makeArray();
        }
        ClassNode redirect = type.redirect();
        if (redirect == null || redirect == type || !isUsingGenericsOrIsArrayUsingGenerics(redirect)) {
            return type;
        }
        ClassNode newType = type.getPlainNodeReference();
        newType.setGenericsPlaceHolder(type.isGenericsPlaceHolder());
        newType.setGenericsTypes(boundUnboundedWildcards(type.getGenericsTypes(), redirect.getGenericsTypes()));
        return newType;
    }

    private static GenericsType[] boundUnboundedWildcards(final GenericsType[] actual, final GenericsType[] declared) {
        int n = actual.length; GenericsType[] newTypes = new GenericsType[n];
        for (int i = 0; i < n; i += 1) {
            newTypes[i] = boundUnboundedWildcard(actual[i], declared[i]);
        }
        return newTypes;
    }

    private static GenericsType boundUnboundedWildcard(final GenericsType actual, final GenericsType declared) {
        if (!isUnboundedWildcard(actual)) return actual;
        ClassNode   lowerBound = declared.getLowerBound();
        ClassNode[] upperBounds = declared.getUpperBounds();
        if (lowerBound != null) {
            assert upperBounds == null;
        } else if (upperBounds == null) {
            upperBounds = new ClassNode[]{OBJECT_TYPE};
        } else if (declared.isPlaceholder()) {
            upperBounds = upperBounds.clone();
            for (int i = 0, n = upperBounds.length; i < n; i += 1) {
                // GROOVY-10055, GROOVY-10619: purge self references
                if (GenericsUtils.extractPlaceholders(upperBounds[i])
                    .containsKey(new GenericsTypeName(declared.getName())))
                        upperBounds[i] = upperBounds[i].getPlainNodeReference();
            }
        }
        GenericsType newType = new GenericsType(makeWithoutCaching("?"), upperBounds, lowerBound);
        newType.setWildcard(true);
        return newType;
    }

    public static boolean isUnboundedWildcard(final GenericsType gt) {
        if (gt.isWildcard() && gt.getLowerBound() == null) {
            ClassNode[] upperBounds = gt.getUpperBounds();
            return (upperBounds == null || upperBounds.length == 0 || (upperBounds.length == 1
                    && isObjectType(upperBounds[0]) && !upperBounds[0].isGenericsPlaceHolder()));
        }
        return false;
    }

    static Map<GenericsTypeName, GenericsType> extractGenericsParameterMapOfThis(final TypeCheckingContext context) {
        ClassNode  cn = context.getEnclosingClassNode();
        MethodNode mn = context.getEnclosingMethod();
        // GROOVY-9570: find the innermost class or method
        if (cn != null && cn.getEnclosingMethod() == mn) {
            return extractGenericsParameterMapOfThis(cn);
        } else {
            return extractGenericsParameterMapOfThis(mn);
        }
    }

    private static Map<GenericsTypeName, GenericsType> extractGenericsParameterMapOfThis(final MethodNode mn) {
        if (mn == null) return null;

        Map<GenericsTypeName, GenericsType> map = null;
        if (!mn.isStatic()) {
            map = extractGenericsParameterMapOfThis(mn.getDeclaringClass());
        }

        GenericsType[] gts = mn.getGenericsTypes();
        if (gts != null) {
            if (map == null) map = new HashMap<>();
            for (GenericsType gt : gts) {
                assert gt.isPlaceholder();
                map.put(new GenericsTypeName(gt.getName()), gt);
            }
        }

        return map;
    }

    private static Map<GenericsTypeName, GenericsType> extractGenericsParameterMapOfThis(final ClassNode cn) {
        if (cn == null) return null;

        Map<GenericsTypeName, GenericsType> map = null;
        if ((cn.getModifiers() & Opcodes.ACC_STATIC) == 0) {
            if (cn.getEnclosingMethod() != null) {
                map = extractGenericsParameterMapOfThis(cn.getEnclosingMethod());
            } else if (cn.getOuterClass() != null) {
                map = extractGenericsParameterMapOfThis(cn.getOuterClass());
            }
        }

        if (!(cn instanceof InnerClassNode && ((InnerClassNode) cn).isAnonymous())) {
            GenericsType[] gts = cn.getGenericsTypes();
            if (gts != null) {
                if (map == null) map = new HashMap<>();
                for (GenericsType gt : gts) {
                    assert gt.isPlaceholder();
                    map.put(new GenericsTypeName(gt.getName()), gt);
                }
            }
        }

        return map;
    }

    /**
     * Filter methods according to visibility
     *
     * @param methodNodeList method nodes to filter
     * @param enclosingClassNode the enclosing class
     * @return filtered method nodes
     * @since 3.0.0
     */
    public static List<MethodNode> filterMethodsByVisibility(final List<MethodNode> methodNodeList, final ClassNode enclosingClassNode) {
        if (!asBoolean(methodNodeList)) {
            return StaticTypeCheckingVisitor.EMPTY_METHODNODE_LIST;
        }

        List<MethodNode> result = new LinkedList<>();

        boolean isEnclosingInnerClass = enclosingClassNode instanceof InnerClassNode;
        List<ClassNode> outerClasses = enclosingClassNode.getOuterClasses();

        outer:
        for (MethodNode methodNode : methodNodeList) {
            if (methodNode instanceof ExtensionMethodNode) {
                result.add(methodNode);
                continue;
            }

            ClassNode declaringClass = methodNode.getDeclaringClass();

            if (isEnclosingInnerClass) {
                for (ClassNode outerClass : outerClasses) {
                    if (outerClass.isDerivedFrom(declaringClass)) {
                        if (outerClass.equals(declaringClass)) {
                            result.add(methodNode);
                            continue outer;
                        } else {
                            if (methodNode.isPublic() || methodNode.isProtected()) {
                                result.add(methodNode);
                                continue outer;
                            }
                        }
                    }
                }
            }

            if (declaringClass instanceof InnerClassNode) {
                if (declaringClass.getOuterClasses().contains(enclosingClassNode)) {
                    result.add(methodNode);
                    continue;
                }
            }

            if (methodNode.isPrivate() && !enclosingClassNode.equals(declaringClass)) {
                continue;
            }
            if (methodNode.isProtected()
                    && !enclosingClassNode.isDerivedFrom(declaringClass)
                    && !samePackageName(enclosingClassNode, declaringClass)) {
                continue;
            }
            if (methodNode.isPackageScope() && !samePackageName(enclosingClassNode, declaringClass)) {
                continue;
            }

            result.add(methodNode);
        }

        return result;
    }

    /**
     * @return true if the class node is either a GString or the LUB of String and GString.
     */
    public static boolean isGStringOrGStringStringLUB(final ClassNode node) {
        return isGStringType(node) || GSTRING_STRING_CLASSNODE.equals(node);
    }

    /**
     * @param node the node to be tested
     * @return true if the node is using generics types and one of those types is a gstring or string/gstring lub
     */
    public static boolean isParameterizedWithGStringOrGStringString(final ClassNode node) {
        if (node.isArray()) return isParameterizedWithGStringOrGStringString(node.getComponentType());
        if (node.isUsingGenerics()) {
            GenericsType[] genericsTypes = node.getGenericsTypes();
            if (genericsTypes != null) {
                for (GenericsType genericsType : genericsTypes) {
                    if (isGStringOrGStringStringLUB(genericsType.getType())) return true;
                }
            }
        }
        return node.getSuperClass() != null && isParameterizedWithGStringOrGStringString(node.getUnresolvedSuperClass());
    }

    /**
     * @param node the node to be tested
     * @return true if the node is using generics types and one of those types is a string
     */
    public static boolean isParameterizedWithString(final ClassNode node) {
        if (node.isArray()) return isParameterizedWithString(node.getComponentType());
        if (node.isUsingGenerics()) {
            GenericsType[] genericsTypes = node.getGenericsTypes();
            if (genericsTypes != null) {
                for (GenericsType genericsType : genericsTypes) {
                    if (STRING_TYPE.equals(genericsType.getType())) return true;
                }
            }
        }
        return node.getSuperClass() != null && isParameterizedWithString(node.getUnresolvedSuperClass());
    }

    /**
     * Determines if node is a raw type or references any generics placeholders.
     */
    public static boolean missesGenericsTypes(ClassNode cn) {
        while (cn.isArray()) cn = cn.getComponentType();
        GenericsType[] cnGenerics = cn.getGenericsTypes();
        GenericsType[] rnGenerics = cn.redirect().getGenericsTypes();
        return cnGenerics == null || cnGenerics.length == 0 ? rnGenerics != null : GenericsUtils.hasUnresolvedGenerics(cn);
    }

    /**
     * @deprecated Use {@link #evaluateExpression(Expression, CompilerConfiguration, GroovyClassLoader)} instead
     */
    @Deprecated(forRemoval = true, since = "4.0.7")
    public static Object evaluateExpression(final Expression expr, final CompilerConfiguration config) {
        return evaluateExpression(expr, config, null);
    }

    /**
     * Evaluates expressions as found in annotation parameters.  For example, it
     * will evaluate a constant, be it referenced directly as an integer or as a
     * reference to a field.
     * <p>
     * If the expression cannot be evaluated on its own, an exception is thrown.
     *
     * @param expr   the expression to be evaluated
     * @param config the compiler configuration
     * @param loader the compiler class loader
     * @return the result of the expression
     * @throws GroovyBugError
     */
    public static Object evaluateExpression(final Expression expr, final CompilerConfiguration config, /*@Nullable*/ final GroovyClassLoader loader) {
        Expression ce = expr instanceof CastExpression ? ((CastExpression) expr).getExpression() : expr;
        if (ce instanceof ConstantExpression) {
            if (expr.getType().equals(getWrapper(ce.getType())) || ((ConstantExpression) ce).isNullExpression())
                return ((ConstantExpression) ce).getValue(); // boolean, number, string, or null
        } else if (ce instanceof ListExpression) {
            if (expr.getType().isArray() && isStringType(expr.getType().getComponentType()))
                return ((ListExpression) ce).getExpressions().stream().map(e -> evaluateExpression(e, config, loader)).toArray(String[]::new);
        }

        String className = "Expression$"+UUID.randomUUID().toString().replace('-', '$');
        ClassNode classNode = new ClassNode(className, Opcodes.ACC_PUBLIC, OBJECT_TYPE);
        addGeneratedMethod(classNode, "eval", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new ReturnStatement(expr));

        // adjust configuration so class can be executed by this JVM
        CompilerConfiguration cc = new CompilerConfiguration(config);
        cc.setPreviewFeatures(false);
        cc.setScriptBaseClass(null);
        cc.setTargetBytecode(CompilerConfiguration.DEFAULT.getTargetBytecode());

        CompilationUnit cu = new CompilationUnit(cc, null, loader);
        try {
            cu.addClassNode(classNode);
            cu.compile(Phases.CLASS_GENERATION);
            GroovyClass gc = cu.getClasses().get(0);
            Class<?> c = cu.getClassLoader().defineClass(className, gc.getBytes());
            // invoke method to produce return value
            return c.getMethod("eval").invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new GroovyBugError(e);
        } finally {
            if (loader == null)
                closeQuietly(cu.getClassLoader());
        }
    }

    /**
     * Collects all interfaces of a class node, including those defined by the
     * super class.
     *
     * @param node a class for which we want to retrieve all interfaces
     * @return a set of interfaces implemented by this class node
     */
    @Deprecated(forRemoval = true, since = "4.0.0")
    public static Set<ClassNode> collectAllInterfaces(final ClassNode node) {
        return GeneralUtils.getInterfacesAndSuperInterfaces(node);
    }

    @Deprecated(forRemoval = true, since = "4.0.0")
    public static ClassNode getCorrectedClassNode(final ClassNode cn, final ClassNode sc, final boolean completed) {
        if (completed && GenericsUtils.hasUnresolvedGenerics(cn)) return sc.getPlainNodeReference();
        return GenericsUtils.correctToGenericsSpecRecurse(GenericsUtils.createGenericsSpec(cn), sc);
    }

    /**
     * Returns true if the class node represents a class node for the Class class
     * and if the parametrized type is a neither a placeholder nor a wildcard. For example,
     * the class node Class&lt;Foo&gt; where Foo is a class would return true, but the class
     * node for Class&lt;?&gt; would return false.
     *
     * @param classNode a class node to be tested
     * @return true if it is the class node for Class and its generic type is a real class
     */
    public static boolean isClassClassNodeWrappingConcreteType(final ClassNode classNode) {
        GenericsType[] genericsTypes = classNode.getGenericsTypes();
        return isClassType(classNode)
                && classNode.isUsingGenerics()
                && genericsTypes != null
                && !genericsTypes[0].isPlaceholder()
                && !genericsTypes[0].isWildcard();
    }

    @Deprecated(since = "5.0.0")
    public static List<MethodNode> findSetters(final ClassNode cn, final String setterName, final boolean voidOnly) {
        List<MethodNode> result = new ArrayList<>();
        if (!cn.isInterface()) {
            for (MethodNode method : cn.getMethods(setterName)) {
                if (isSetter(method, voidOnly)) result.add(method);
            }
        }
        for (ClassNode in : cn.getAllInterfaces()) {
            for (MethodNode method : in.getDeclaredMethods(setterName)) {
                if (isSetter(method, voidOnly)) result.add(method);
            }
        }
        return result;
    }

    private static boolean isSetter(final MethodNode mn, final boolean voidOnly) {
        return (!voidOnly || mn.isVoidMethod()) && mn.getParameters().length == 1;
    }

    public static ClassNode isTraitSelf(final VariableExpression vexp) {
        if (Traits.THIS_OBJECT.equals(vexp.getName())) {
            Variable accessedVariable = vexp.getAccessedVariable();
            ClassNode type = accessedVariable != null ? accessedVariable.getType() : null;
            if (accessedVariable instanceof Parameter
                    && Traits.isTrait(type)) {
                return type;
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------

    /**
     * A DGM-like class which adds support for method calls which are handled by
     * the Groovy compiler.
     */
    public static class ObjectArrayStaticTypesHelper {
        public static <T> T getAt(final T[] array, final int index) {
            @SuppressWarnings("unchecked")
            T t = (T)BytecodeInterface8.objectArrayGet(array, index);
            return t;
        }
        public static <T, U extends T> void putAt(final T[] array, final int index, final U value) {
            BytecodeInterface8.objectArraySet(array, index, value);
        }
    }

    public static class BooleanArrayStaticTypesHelper {
        public static boolean getAt(final boolean[] array, final int index) {
            return BytecodeInterface8.zArrayGet(array, index);
        }
        @Deprecated(since = "5.0.0")
        public static Boolean getAt$$bridge(final boolean[] array, final int index) {
            return array != null ? array[index] : null;
        }
        public static void putAt(final boolean[] array, final int index, final boolean value) {
            BytecodeInterface8.zArraySet(array, index, value);
        }
    }

    public static class CharArrayStaticTypesHelper {
        public static char getAt(final char[] array, final int index) {
            return BytecodeInterface8.cArrayGet(array, index);
        }
        @Deprecated(since = "5.0.0")
        public static Character getAt$$bridge(final char[] array, final int index) {
            return array != null ? array[index] : null;
        }
        public static void putAt(final char[] array, final int index, final char value) {
            BytecodeInterface8.cArraySet(array, index, value);
        }
    }

    public static class ByteArrayStaticTypesHelper {
        public static byte getAt(final byte[] array, final int index) {
            return BytecodeInterface8.bArrayGet(array, index);
        }
        @Deprecated(since = "5.0.0")
        public static Byte getAt$$bridge(final byte[] array, final int index) {
            return array != null ? array[index] : null;
        }
        public static void putAt(final byte[] array, final int index, final byte value) {
            BytecodeInterface8.bArraySet(array, index, value);
        }
    }

    public static class ShortArrayStaticTypesHelper {
        public static short getAt(final short[] array, final int index) {
            return BytecodeInterface8.sArrayGet(array, index);
        }
        @Deprecated(since = "5.0.0")
        public static Short getAt$$bridge(final short[] array, final int index) {
            return array != null ? array[index] : null;
        }
        public static void putAt(final short[] array, final int index, final short value) {
            BytecodeInterface8.sArraySet(array, index, value);
        }
    }

    public static class IntArrayStaticTypesHelper {
        public static int getAt(final int[] array, final int index) {
            return BytecodeInterface8.intArrayGet(array, index);
        }
        @Deprecated(since = "5.0.0")
        public static Integer getAt$$bridge(final int[] array, final int index) {
            return array != null ? array[index] : null;
        }
        public static void putAt(final int[] array, final int index, final int value) {
            BytecodeInterface8.intArraySet(array, index, value);
        }
    }

    public static class LongArrayStaticTypesHelper {
        public static long getAt(final long[] array, final int index) {
            return BytecodeInterface8.lArrayGet(array, index);
        }
        @Deprecated(since = "5.0.0")
        public static Long getAt$$bridge(final long[] array, final int index) {
            return array != null ? array[index] : null;
        }
        public static void putAt(final long[] array, final int index, final long value) {
            BytecodeInterface8.lArraySet(array, index, value);
        }
    }

    public static class FloatArrayStaticTypesHelper {
        public static float getAt(final float[] array, final int index) {
            return BytecodeInterface8.fArrayGet(array, index);
        }
        @Deprecated(since = "5.0.0")
        public static Float getAt$$bridge(final float[] array, final int index) {
            return array != null ? array[index] : null;
        }
        public static void putAt(final float[] array, final int index, final float value) {
            BytecodeInterface8.fArraySet(array, index, value);
        }
    }

    public static class DoubleArrayStaticTypesHelper {
        public static double getAt(final double[] array, final int index) {
            return BytecodeInterface8.dArrayGet(array, index);
        }
        @Deprecated(since = "5.0.0")
        public static Double getAt$$bridge(final double[] array, final int index) {
            return array != null ? array[index] : null;
        }
        public static void putAt(final double[] array, final int index, final double value) {
            BytecodeInterface8.dArraySet(array, index, value);
        }
    }
}

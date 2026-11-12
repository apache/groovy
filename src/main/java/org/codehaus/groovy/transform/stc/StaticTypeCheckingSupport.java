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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.IntPredicate;
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
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.findSAM;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getNextSuperClass;
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
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asList;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.first;
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

    /** Cached {@link Matcher} type. */
    protected static final ClassNode Matcher_TYPE = makeWithoutCaching(Matcher.class);
    /** Cached {@link ArrayList} type. */
    protected static final ClassNode ArrayList_TYPE = makeWithoutCaching(ArrayList.class);
    /** Cached {@link BaseStream} type. */
    protected static final ClassNode BaseStream_TYPE = makeWithoutCaching(BaseStream.class);
    protected static final ClassNode Collection_TYPE = COLLECTION_TYPE; // TODO: deprecate?
    protected static final ClassNode Deprecated_TYPE = DEPRECATED_TYPE; // TODO: deprecate?
    /** Cached {@link LinkedHashMap} type. */
    protected static final ClassNode LinkedHashMap_TYPE = makeWithoutCaching(LinkedHashMap.class);
    /** Cached {@link LinkedHashSet} type. */
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

    /** Shared cache of extension methods visible to the static type checker. */
    protected static final ExtensionMethodCache EXTENSION_METHOD_CACHE = ExtensionMethodCache.INSTANCE;

    /**
     * Drops cached extension methods for the supplied class loader,
     * including the per-receiver name index and the preemptive-name set.
     */
    public static void clearExtensionMethodCache(final ClassLoader loader) {
        EXTENSION_METHOD_CACHE.invalidate(loader);
    }

    /**
     * Drops all cached extension methods and derived indexes.
     */
    public static void clearExtensionMethodCache() {
        EXTENSION_METHOD_CACHE.invalidateAll();
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

    /**
     * Returns extension methods with the supplied name for the receiver hierarchy.
     * Uses the name-indexed extension-method cache; repeated lookups of the same
     * name on the same type do not rescan that type's DGM methods.
     */
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

    /**
     * Collects extension methods with the supplied name for the receiver hierarchy.
     * Named lookup uses the per-receiver index built when the loader's extension
     * methods were scanned, so it does not walk every DGM method on the type.
     */
    protected static void findDGMMethodsForClassNode(final ClassLoader loader, final ClassNode clazz, final String name, final TreeSet<MethodNode> accumulator) {
        accumulator.addAll(EXTENSION_METHOD_CACHE.get(loader, clazz.getName(), name));
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
            } else if (Boolean.TRUE.equals(aType.getNodeMetaData(StaticTypesMarker.METHOD_REFERENCE_TYPE))
                    && CLOSURE_TYPE.getName().equals(pType.getName())) {
                // as a call argument, a method reference (::) targets a functional interface, not
                // a Closure parameter; strongly disfavour a Closure parameter so a functional-
                // interface overload is preferred (otherwise the Closure overload wins the
                // distance tie-break and the method reference then fails to coerce)
                dist += 256;
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
     * Checks if a value of one type can be passed for a parameter of another: a subtype, or a
     * number type that converts without narrowing, ignoring boxing. This is the rule for method
     * arguments. An assignment or a return of a value uses
     * {@link #checkCompatibleAssignmentTypes(ClassNode, ClassNode, Expression, boolean)} instead.
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
        // GROOVY-12230: the String/GString LUB arises for conditionals like "flag ? gstring : string"
        // and converts to String just as a GString does; see also #checkCompatibleAssignmentTypes
        if ((type.isDerivedFrom(GSTRING_TYPE) || isGStringOrGStringStringLUB(type)) && isStringType(toBeAssignedTo)) {
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

    /**
     * Indicates whether the parameter list uses a varargs parameter.
     */
    @Deprecated(forRemoval = true, since = "4.0.4")
    static boolean isVargs(final Parameter[] parameters) {
        return ParameterUtils.isVargs(parameters);
    }

    /**
     * Indicates whether the operation maps to a boolean {@code compareTo} result.
     */
    public static boolean isCompareToBoolean(final int op) {
        return op == COMPARE_LESS_THAN || op == COMPARE_LESS_THAN_EQUAL
                || op == COMPARE_GREATER_THAN || op == COMPARE_GREATER_THAN_EQUAL;
    }

    /**
     * Indicates whether the token represents array access.
     */
    static boolean isArrayOp(final int op) {
        return op == LEFT_SQUARE_BRACKET;
    }

    /**
     * Indicates whether the token is an intrinsic boolean operation.
     */
    static boolean isBoolIntrinsicOp(final int op) {
        return switch (op) {
            case LOGICAL_AND, LOGICAL_OR, COMPARE_NOT_IDENTICAL, COMPARE_IDENTICAL, MATCH_REGEX, KEYWORD_INSTANCEOF,
                 COMPARE_NOT_INSTANCEOF, IMPLIES -> true;
            default -> false;
        };
    }

    /**
     * Indicates whether the token represents Groovy's power operator.
     */
    static boolean isPowerOperator(final int op) {
        return op == POWER || op == POWER_EQUAL;
    }

    /**
     * Returns the method name associated with the supplied operator token.
     */
    static String getOperationName(final int op) {
        return switch (op) {
            case COMPARE_EQUAL, COMPARE_NOT_EQUAL ->
                // this is only correct in this specific context; normally
                // we would have to compile against compareTo if available
                // but since we don't compile here, this one is enough
                "equals";
            case COMPARE_TO, COMPARE_LESS_THAN, COMPARE_LESS_THAN_EQUAL, COMPARE_GREATER_THAN,
                 COMPARE_GREATER_THAN_EQUAL -> "compareTo";
            case BITWISE_AND, BITWISE_AND_EQUAL -> "and";
            case BITWISE_OR, BITWISE_OR_EQUAL -> "or";
            case BITWISE_XOR, BITWISE_XOR_EQUAL -> "xor";
            case PLUS, PLUS_EQUAL -> "plus";
            case MINUS, MINUS_EQUAL -> "minus";
            case MULTIPLY, MULTIPLY_EQUAL -> "multiply";
            case DIVIDE, DIVIDE_EQUAL -> "div";
            case INTDIV, INTDIV_EQUAL -> "intdiv";
            case MOD, MOD_EQUAL -> "mod";
            case REMAINDER, REMAINDER_EQUAL -> "remainder";
            case POWER, POWER_EQUAL -> "power";
            case LEFT_SHIFT, LEFT_SHIFT_EQUAL -> "leftShift";
            case RIGHT_SHIFT, RIGHT_SHIFT_EQUAL -> "rightShift";
            case RIGHT_SHIFT_UNSIGNED, RIGHT_SHIFT_UNSIGNED_EQUAL -> "rightShiftUnsigned";
            case KEYWORD_IN -> "isCase";
            case COMPARE_NOT_IN -> "isNotCase";
            default -> null;
        };
    }

    /**
     * GEP-15: returns the dedicated compound-assignment method name for a compound-assign
     * operator token (e.g. {@code PLUS_EQUAL} -> {@code "plusAssign"}), or {@code null} if
     * the token is not one of the twelve operators in scope. {@code INTDIV_EQUAL},
     * {@code MOD_EQUAL}, {@code ELVIS_EQUAL}, {@code NULL_COALESCE_EQUAL}, {@code LOGICAL_OR_EQUAL} and
     * {@code LOGICAL_AND_EQUAL} are intentionally excluded.
     *
     * @since 6.0.0
     */
    public static String getAssignOperationName(final int op) {
        return switch (op) {
            case PLUS_EQUAL                 -> "plusAssign";
            case MINUS_EQUAL                -> "minusAssign";
            case MULTIPLY_EQUAL             -> "multiplyAssign";
            case DIVIDE_EQUAL               -> "divAssign";
            case REMAINDER_EQUAL            -> "remainderAssign";
            case POWER_EQUAL                -> "powerAssign";
            case LEFT_SHIFT_EQUAL           -> "leftShiftAssign";
            case RIGHT_SHIFT_EQUAL          -> "rightShiftAssign";
            case RIGHT_SHIFT_UNSIGNED_EQUAL -> "rightShiftUnsignedAssign";
            case BITWISE_AND_EQUAL          -> "andAssign";
            case BITWISE_OR_EQUAL           -> "orAssign";
            case BITWISE_XOR_EQUAL          -> "xorAssign";
            default                         -> null;
        };
    }

    /**
     * Indicates whether the supplied method name is a shift operation.
     */
    static boolean isShiftOperation(final String name) {
        return "leftShift".equals(name) || "rightShift".equals(name) || "rightShiftUnsigned".equals(name);
    }

    /**
     * Returns true for operations that are of the class, that given a common type class for left and right, the
     * operation "left op right" will have a result in the same type class In Groovy on numbers that is +,-,* as well as
     * their variants with equals.
     */
    static boolean isOperationInGroup(final int op) {
        return switch (op) {
            case PLUS, PLUS_EQUAL, MINUS, MINUS_EQUAL, MULTIPLY, MULTIPLY_EQUAL -> true;
            default -> false;
        };
    }

    /**
     * Indicates whether the token represents a bitwise operation.
     */
    static boolean isBitOperator(final int op) {
        return switch (op) {
            case BITWISE_OR_EQUAL, BITWISE_OR, BITWISE_AND_EQUAL, BITWISE_AND, BITWISE_XOR_EQUAL, BITWISE_XOR -> true;
            default -> false;
        };
    }

    /**
     * Indicates whether the token represents any assignment form.
     */
    public static boolean isAssignment(final int op) {
        return Types.isAssignment(op);
    }

    /**
     * Returns true or false depending on whether the right classnode can be assigned to the left classnode. This method
     * should not add errors by itself: we let the caller decide what to do if an incompatible assignment is found.
     * <p>
     * This is the rule for an assignment and for the return of a value by a method, closure, lambda or method
     * reference, where a conversion between number types is then checked for loss of precision. Method arguments
     * use {@link #isAssignableTo(ClassNode, ClassNode)}, and casts use {@code StaticTypeCheckingVisitor#checkCast}.
     * A new check of a value against a type should use the one of these that matches, rather than rules of its own,
     * so that the same value is accepted in the same context everywhere.
     *
     * @param left  the class to be assigned to
     * @param right the assignee class
     * @return false if types are incompatible
     */
    public static boolean checkCompatibleAssignmentTypes(final ClassNode left, final ClassNode right) {
        return checkCompatibleAssignmentTypes(left, right, null);
    }

    /**
     * Checks assignment compatibility using the supplied right-hand expression for additional context.
     */
    public static boolean checkCompatibleAssignmentTypes(final ClassNode left, final ClassNode right, final Expression rightExpression) {
        return checkCompatibleAssignmentTypes(left, right, rightExpression, true);
    }

    /**
     * Everything that can be done by {@code castToType} should be allowed for assignment.
     *
     * @see org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation#castToType(Object,Class)
     */
    public static boolean checkCompatibleAssignmentTypes(final ClassNode left, final ClassNode right, final Expression rightExpression, final boolean allowConstructorCoercion) {
        if (isNullConstant(rightExpression)) { // TODO: left is @NonNull
            return (!isPrimitiveType(left) || isPrimitiveBoolean(left)); // GROOVY-6577
        }

        if (left.isArray()) {
            ClassNode leftItemType = left.getComponentType();
            if (right.isArray()) {
                ClassNode rightItemType = right.getComponentType();
                return (isPrimitiveType(leftItemType) && !isPrimitiveBoolean(leftItemType))
                        ? isPrimitiveType(rightItemType) // GROOVY-11371: primitive array only
                        : checkCompatibleAssignmentTypes(leftItemType, rightItemType, rightExpression, false);
            }
            if (rightExpression instanceof ListExpression) {
                return true; // addPrecisionErrors checks values
            }
            if (GeneralUtils.isOrImplements(right, Collection_TYPE)) {
                var elementType = GenericsUtils.parameterizeType(right, Collection_TYPE).getGenericsTypes()[0];
                return isObjectType(leftItemType) // Object[] can accept any collection element type(s)
                    || (elementType.getLowerBound() == null && isCovariant(extractType(elementType), getWrapper(leftItemType)));
                    //  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ GROOVY-8984: "? super T" is only compatible with an Object[] target
            }
            if (GeneralUtils.isOrImplements(right, BaseStream_TYPE)) {
                var elementType = GenericsUtils.parameterizeType(right, BaseStream_TYPE).getGenericsTypes()[0];
                return isObjectType(leftItemType) // Object[] can accept any stream API element type(s)
                    || (elementType.getLowerBound() == null && isCovariant(extractType(elementType), getWrapper(leftItemType)));
            }
        }

        ClassNode leftRedirect = left.redirect();
        ClassNode rightRedirect = right.redirect();
        // JLS 4.4: two type variables are the same type only when they come
        // from the same declaration. Unbounded variables share the Object
        // erasure, so redirect identity is not type-variable identity.
        if (GenericsType.areDistinctTypeVariables(left, right)) {
            return false;
        }
        if (leftRedirect == rightRedirect) return true;

        if (leftRedirect == VOID_TYPE) return rightRedirect == void_WRAPPER_TYPE;
        if (leftRedirect == void_WRAPPER_TYPE) return rightRedirect == VOID_TYPE;

        if (leftRedirect == char_TYPE || leftRedirect == Character_TYPE) {
            // GROOVY-10079, GROOVY-10095, GROOVY-10359: assign Character, char or int to char or Character
            if (rightRedirect == Character_TYPE || rightRedirect == char_TYPE || rightRedirect == int_TYPE) {
                return true;
            }
            if (rightRedirect == STRING_TYPE) {
                return rightExpression instanceof ConstantExpression && rightExpression.getText().length() == 1;
            }
        } else if (isLongCategory(getUnwrapper(leftRedirect))) {
            // byte, int, long or short can be assigned any base number type
            if (isNumberType(rightRedirect)) {
                return true;
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
            // Object, String, [Bb]oolean or Class can be assigned any non-null value
            return true;
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

    /**
     * Indicates whether the class node is still attached to a compile unit.
     */
    public static boolean isBeingCompiled(final ClassNode node) {
        return (node.getCompileUnit() != null);
    }

    /**
     * Checks whether assigning the right type could lose numeric precision.
     */
    static boolean checkPossibleLossOfPrecision(final ClassNode left, final ClassNode right, final Expression rightExpr) {
        if (left == right || left.equals(right)) return false; // identical types
        int leftIndex = NUMBER_TYPES.get(left);
        int rightIndex = NUMBER_TYPES.get(right);
        if (leftIndex >= rightIndex) return false;

        // check if the right number will fit in the left type
        Expression valueExpr = rightExpr;
        if (valueExpr instanceof CastExpression) // "(short) 1234"
            valueExpr = ((CastExpression) valueExpr).getExpression();
        if (valueExpr instanceof ConstantExpression) {
            Object value = ((ConstantExpression) valueExpr).getValue();
            if (!(value instanceof Number number)) return true; // null or ...
            switch (leftIndex) {
              case 0: // byte
                  return switch (rightIndex) {
                      case 1 -> Short.compare(number.byteValue(), number.shortValue()) != 0;
                      case 2 -> Integer.compare(number.byteValue(), number.intValue()) != 0;
                      case 3 -> Long.compare(number.byteValue(), number.longValue()) != 0;
                      case 4 -> Float.compare(number.byteValue(), number.floatValue()) != 0;
                      default -> Double.compare(number.byteValue(), number.doubleValue()) != 0;
                  };
              case 1: // short
                  return switch (rightIndex) {
                      case 2 -> Integer.compare(number.shortValue(), number.intValue()) != 0;
                      case 3 -> Long.compare(number.shortValue(), number.longValue()) != 0;
                      case 4 -> Float.compare(number.shortValue(), number.floatValue()) != 0;
                      default -> Double.compare(number.shortValue(), number.doubleValue()) != 0;
                  };
              case 2: // int
                  return switch (rightIndex) {
                      case 3 -> Long.compare(number.intValue(), number.longValue()) != 0;
                      case 4 -> Float.compare(number.intValue(), number.floatValue()) != 0;
                      default -> Double.compare(number.intValue(), number.doubleValue()) != 0;
                  };
              case 3: // long
                  return switch (rightIndex) {
                      case 4 -> Float.compare(number.longValue(), number.floatValue()) != 0;
                      default -> Double.compare(number.longValue(), number.doubleValue()) != 0;
                  };
              case 4: // float
                return Double.compare(number.floatValue(), number.doubleValue()) != 0;
              default: // double
                return false; // no possible loss here
            }
        }
        return true; // possible loss of precision
    }

    /**
     * Formats a method name and parameter types for diagnostics.
     */
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
            return normalizeUnboundedWildcard(type.redirect().toString(false));
        }
        return normalizeUnboundedWildcard(type.toString(false));
    }

    /**
     * JLS 4.5.1: {@code ?} and {@code ? extends Object} are the same wildcard.
     * Diagnostics use the short form. A longer bound ({@code Object[]},
     * {@code Object<V>}, an intersection) is left as written.
     */
    static String normalizeUnboundedWildcard(final String printed) {
        if (printed == null || printed.isEmpty()) return printed;
        final String token = "? extends java.lang.Object";
        StringBuilder sb = null;
        int i = 0;
        while (i < printed.length()) {
            int j = printed.indexOf(token, i);
            if (j < 0) {
                if (sb == null) return printed;
                sb.append(printed, i, printed.length());
                break;
            }
            int end = j + token.length();
            if (!isSoleObjectBound(printed, end)) {
                if (sb == null) sb = new StringBuilder(printed.length());
                sb.append(printed, i, end);
                i = end;
                continue;
            }
            if (sb == null) sb = new StringBuilder(printed.length());
            sb.append(printed, i, j).append('?');
            i = end;
        }
        return sb == null ? printed : sb.toString();
    }

    private static boolean isSoleObjectBound(final String printed, final int end) {
        int k = end;
        while (k < printed.length() && printed.charAt(k) == ' ') k += 1;
        if (k >= printed.length()) return true;
        char c = printed.charAt(k);
        return c != '&' && c != '<' && c != '[' && c != '.' && !Character.isJavaIdentifierPart(c);
    }

    /**
     * Diagnostic name of a wildcard capture, for example
     * {@code capture-of ? super java.lang.Integer}.
     */
    static String prettyPrintCapture(final GenericsType wildcard) {
        return normalizeUnboundedWildcard("capture-of " + wildcard);
    }

    /**
     * Returns string representation of type without any type arguments. Arrays
     * are indicated with trailing "[]".
     */
    static String prettyPrintTypeName(final ClassNode type) {
        if (type.isArray()) {
            return prettyPrintTypeName(type.getComponentType()) + "[]";
        }
        if (type instanceof UnionTypeClassNode union) {
            var sj = new StringJoiner(" | ", "(", ")");
            for (ClassNode cn : union.getDelegates()) {
                sj.add(prettyPrintTypeName(cn));
            }
            return sj.toString();
        }
        if (type.isGenericsPlaceHolder()) {
            // a "#T" placeholder stands for a type variable with no witness; name its bound
            ClassNode bound = type.redirect();
            if (type.getUnresolvedName().charAt(0) == '#' && bound != type) {
                return prettyPrintTypeName(bound);
            }
            return type.getUnresolvedName();
        }
        return type.getText();
    }

    /**
     * Indicates whether {@code type} implements or inherits from {@code superOrInterface}.
     */
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
            if (implementsInterfaceOrIsSubclassOf(type, superOrInterface.getUnresolvedSuperClass())
                    && Arrays.stream(superOrInterface.getInterfaces()).allMatch(type::implementsInterface)) {
                return true;
            }
        } else if (superOrInterface instanceof UnionTypeClassNode union) {
            for (ClassNode delegate : union.getDelegates()) {
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

    /**
     * Returns the numeric widening distance between two primitive-like types.
     */
    static int getPrimitiveDistance(final ClassNode primA, final ClassNode primB) {
        return Math.abs(NUMBER_TYPES.getOrDefault(primA, NON_NUMBER_DEFAULT)
                      - NUMBER_TYPES.getOrDefault(primB, NON_NUMBER_DEFAULT));
    }

    /**
     * Returns the relative conversion distance from the actual type to the expected type.
     */
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

    /**
     * Returns matching extension methods for the supplied receiver and argument types.
     */
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

    /**
     * Adds matching extension methods for the supplied receiver and argument types.
     */
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
        if (!duckType) {
            if (methods.size() > 1 && !first(methods).isConstructor())
                methods = removeCovariantsAndInterfaceEquivalents(methods, false);
            if (argumentTypes == null)
                return asList(methods); // GROOVY-11683: no covariants or equivalents
        }

        // GROOVY-12156: MethodNode has identity equals/hashCode, so an unordered set would
        // return the candidates in identity-hash order; that order varies with the compiling
        // thread and its allocation history, and callers that accept more than one candidate
        // (union-type receiver, method reference, macro dispatch) let it reach generated code
        Set<MethodNode> bestMethods = new LinkedHashSet<>(); // choose best method(s) for each possible receiver
        for (ClassNode rcvr : duckType ? ((UnionTypeClassNode) receiver).getDelegates() : new ClassNode[]{receiver}) {
            var view = methods;
            if (duckType) {
                view = methods.stream().filter(m -> implementsInterfaceOrSubclassOf(rcvr, m.getDeclaringClass())).toList();
                if (view.size() > 1) { // GROOVY-11888
                    view = removeCovariantsAndInterfaceEquivalents(view, true);
                }
            }
            view = chooseBestMethods(rcvr, view, argumentTypes);
            if (view.isEmpty()) {
                return Collections.emptyList(); // GROOVY-10702
            }
            bestMethods.addAll(view);
        }
        return new LinkedList<>(bestMethods); // assumes caller wants remove to be inexpensive
    }

    /**
     * Node metadata key for the type of a functional argument (a method reference or
     * pointer, or a closure or lambda with declared parameters): an {@link IntPredicate}
     * telling whether it can be called with a given number of arguments.
     */
    static final Object FUNCTOR_ARITY = new Object();

    /**
     * Drops the candidates with a functional-interface parameter whose abstract method
     * takes a number of arguments the functional argument given for it cannot be called
     * with, as only a functor of fitting arity is compatible with the interface (cf. JLS
     * 15.12.2.1); keeps every candidate if none fits.
     */
    private static Collection<MethodNode> filterByFunctorArity(final Collection<MethodNode> methods, final ClassNode[] argumentTypes) {
        if (Arrays.stream(argumentTypes).noneMatch(t -> t != null && t.getNodeMetaData(FUNCTOR_ARITY) != null)) {
            return methods;
        }
        List<MethodNode> fitting = new ArrayList<>(methods.size());
        for (MethodNode method : methods) {
            Parameter[] parameters = parametersForDistance(method);
            boolean fits = true;
            for (int i = 0, n = Math.min(argumentTypes.length, parameters.length); i < n && fits; i += 1) {
                IntPredicate arity = argumentTypes[i] == null ? null : argumentTypes[i].getNodeMetaData(FUNCTOR_ARITY);
                if (arity != null) {
                    MethodNode sam = findSAM(parameters[i].getType());
                    fits = (sam == null || arity.test(sam.getParameters().length));
                }
            }
            if (fits) fitting.add(method);
        }
        return fitting.isEmpty() ? methods : fitting;
    }

    private static List<MethodNode> chooseBestMethods(final ClassNode receiver, Collection<MethodNode> methods, final ClassNode[] argumentTypes) {
        List<MethodNode> bestMethods = new ArrayList<>();

        // phase 0: functional-argument arity classifier
        if (methods.size() > 1) {
            methods = filterByFunctorArity(methods, argumentTypes);
        }

        // phase 1: argument-parameter distance classifier
        int bestDist = Integer.MAX_VALUE;
        for (MethodNode method : methods) {
            int dist = measureParametersAndArgumentsDistance(parametersForDistance(method), argumentTypes);
            if (dist >= 0 && dist <= bestDist) {
                if (dist < bestDist) {
                    bestDist = dist;
                    bestMethods.clear();
                }
                bestMethods.add(method);
            }
        }
        if (bestMethods.size() > 1) { // GROOVY-11258
            for (var iter = bestMethods.iterator(); iter.hasNext(); ) {
                MethodNode outer = iter.next(); // if redundant, remove
                for (MethodNode inner : bestMethods) {
                    if (inner != outer
                            && !ParameterUtils.parametersEqual(inner.getParameters(), outer.getParameters())
                            &&  ParameterUtils.parametersCompatible(inner.getParameters(), outer.getParameters())) {
                        iter.remove(); // for the given argument type(s), inner can accept everything that outer can
                        break;
                    }
                }
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

    /**
     * Distance measurement treats generic parameters as their erasure so a
     * {@code List<T>} parameter does not reject a {@code List} argument.
     * <p>
     * <b>PERFORMANCE &amp; SAFETY CONTRACT:</b> To avoid redundant array allocations during
     * overload resolution, this method reuses and returns {@link MethodNode#getParameters()}
     * directly whenever no generic erasure is needed. The returned array is a cloned copy
     * <i>only</i> when one or more parameters require generic erasure.
     * <p>
     * <b>CRITICAL MUTATION WARNING:</b> Callers of this method and all downstream methods in
     * the distance measurement chain (e.g. {@link #measureParametersAndArgumentsDistance(Parameter[], ClassNode[])})
     * <b>MUST NEVER</b> mutate the returned {@code Parameter[]} array or its elements in place.
     * In-place mutation would silently corrupt the {@link MethodNode}'s parameter definitions
     * for all subsequent compilation phases across the compiler.
     */
    private static Parameter[] parametersForDistance(final MethodNode method) {
        Parameter[] parameters = method.getParameters();
        Parameter[] erased = null;
        for (int i = 0, n = parameters.length; i < n; i += 1) {
            ClassNode t = parameters[i].getOriginType();
            if (t.isGenericsPlaceHolder() || isUsingGenericsOrIsArrayUsingGenerics(t)) {
                if (erased == null) {
                    erased = parameters.clone();
                }
                erased[i] = new Parameter(GenericsUtils.nonGeneric(t), parameters[i].getName());
            }
        }
        // WARNING: Returning the un-cloned `parameters` array here is safe ONLY because all callers
        // in the distance-measurement chain treat the array as strictly read-only.
        return erased == null ? parameters : erased;
    }

    /**
     * Measures parameter-to-argument distance. Note: The supplied {@code parameters} array
     * may be the internal backing array of a {@link MethodNode} (see {@link #parametersForDistance(MethodNode)});
     * this method and all helper methods it calls must treat {@code parameters} as strictly read-only.
     */
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
                    if (oneDC == twoDC) {
                        if (ParameterUtils.parametersEqual(one.getParameters(), two.getParameters())) {
                            ClassNode oneRT = one.getReturnType(), twoRT = two.getReturnType();
                            if (isCovariant(oneRT, twoRT)) {
                                toBeRemoved.add(two);
                            } else if (isCovariant(twoRT, oneRT)) {
                                toBeRemoved.add(one);
                            }
                        } else {
                            // imperfect solution of determining if two methods are
                            // equivalent; for example String#compareTo(Object) and
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
                                // GROOVY-10109, GROOVY-11341, GROOVY-11746: multi-level covariant and synthetic overrides
                                if (isSynthetic(one, two) && !two.isAbstract() && !(two instanceof ExtensionMethodNode)) {
                                    ClassNode oneRT = one.getReturnType(), twoRT = two.getReturnType();
                                    if ((twoRT.isGenericsPlaceHolder() ? !oneRT.isGenericsPlaceHolder() : !oneRT.equals(twoRT)) && isCovariant(twoRT, oneRT)) {
                                        toBeRemoved.add(one); // edge case: two is covariant or generic
                                        continue;
                                    }
                                }
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

    /**
     * Resolves placeholders and wildcard bounds within the supplied type.
     */
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
                if (gt.isWildcard()) return new ClassNode("capture-of " + gt, 0, getCombinedBoundType(gt));
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

    /**
     * JLS 4.5.1: a concrete type argument does not contain a wildcard or a
     * different parameterization. {@code List<Number> = List<? extends Number>}
     * and {@code List<List<?>> = List<List<String>>} are both rejected.
     * A type variable may still capture a wildcard, so placeholders are skipped.
     * Inference of free type variables is not this check.
     */
    static boolean concreteArgumentContainsWildcard(final ClassNode left, final ClassNode right) {
        if (left == null || right == null || !left.equals(right)) return false;
        GenericsType[] leftArgs = left.getGenericsTypes();
        GenericsType[] rightArgs = right.getGenericsTypes();
        if (leftArgs == null || rightArgs == null || leftArgs.length != rightArgs.length) return false;
        for (int i = 0; i < leftArgs.length; i += 1) {
            GenericsType leftArg = leftArgs[i];
            GenericsType rightArg = rightArgs[i];
            if (leftArg.isPlaceholder() || rightArg.isPlaceholder()) continue;
            if (leftArg.isWildcard()) {
                // "? extends List<Number>" does not contain "List<? extends Number>".
                ClassNode[] upper = leftArg.getUpperBounds();
                if (leftArg.getLowerBound() == null && upper != null && upper.length == 1
                        && !upper[0].isGenericsPlaceHolder()
                        && !rightArg.isWildcard()
                        && nestedConcreteVersusWildcard(upper[0], rightArg.getType())) {
                    return true;
                }
                continue;
            }
            if (rightArg.isWildcard()) return true;
            // ArrayList<#E> versus List<Element> is subtyping, not a failed identity.
            if (leftArg.getType() == null || rightArg.getType() == null
                    || !leftArg.getType().equals(rightArg.getType())) {
                continue;
            }
            if (!sameTypeArgument(leftArg.getType(), rightArg.getType())) return true;
        }
        return false;
    }

    /**
     * JLS 4.5.1: a type argument that is a type variable, as {@code T} in {@code Class<T>},
     * contains only that type variable, so a wildcard such as the {@code ?} of
     * {@code Class<? extends Object>} is not within it, nor within {@code ? extends T}. An
     * inference variable ({@code #T}) may still take a wildcard.
     */
    static boolean typeVariableArgumentGetsWildcard(final ClassNode left, final ClassNode right) {
        if (left == null || right == null || !left.equals(right)) return false;
        GenericsType[] leftArgs = left.getGenericsTypes();
        GenericsType[] rightArgs = right.getGenericsTypes();
        if (leftArgs == null || rightArgs == null || leftArgs.length != rightArgs.length) return false;
        for (int i = 0; i < leftArgs.length; i += 1) {
            if (isUnboundedWildcard(rightArgs[i]) && leftArgs[i].isWildcard() && leftArgs[i].getLowerBound() == null) {
                // "?" is not within "? extends T"
                ClassNode bound = firstBound(leftArgs[i].getUpperBounds());
                if (bound != null && bound.isGenericsPlaceHolder() && !isInferencePlaceholder(bound)) return true;
                continue;
            }
            if (!leftArgs[i].isPlaceholder() || leftArgs[i].getName().startsWith("#") || !rightArgs[i].isWildcard()) continue;
            if (rightArgs[i].getLowerBound() != null && isInferencePlaceholder(rightArgs[i].getLowerBound())) continue;
            if (rightArgs[i].getUpperBounds() != null && Arrays.stream(rightArgs[i].getUpperBounds()).anyMatch(StaticTypeCheckingSupport::isInferencePlaceholder)) continue;
            return true;
        }
        return false;
    }

    /**
     * True when {@code target} has a concrete type argument in a position where
     * {@code source} has a wildcard, so {@code source} is not a subtype of {@code target}.
     */
    private static boolean nestedConcreteVersusWildcard(final ClassNode target, final ClassNode source) {
        if (target == null || source == null || !target.equals(source)) return false;
        GenericsType[] targetArgs = target.getGenericsTypes();
        GenericsType[] sourceArgs = source.getGenericsTypes();
        if (targetArgs == null || sourceArgs == null || targetArgs.length != sourceArgs.length) return false;
        for (int i = 0; i < targetArgs.length; i += 1) {
            if (targetArgs[i].isWildcard() || targetArgs[i].isPlaceholder()) continue;
            if (sourceArgs[i].isWildcard()) return true;
            if (nestedConcreteVersusWildcard(targetArgs[i].getType(), sourceArgs[i].getType())) return true;
        }
        return false;
    }

    /**
     * Non-wildcard type arguments match only when they are the same type.
     * {@code ?} and {@code ? extends Object} are the same wildcard.
     * A raw type argument stays an unchecked conversion.
     */
    private static boolean sameTypeArgument(final ClassNode left, final ClassNode right) {
        if (left == right) return true;
        if (left == null || right == null) return false;
        if (left.isArray() || right.isArray()) {
            return left.isArray() && right.isArray()
                    && sameTypeArgument(left.getComponentType(), right.getComponentType());
        }
        if (!left.equals(right)) return false;
        // Outer<T>.Inner and Outer.Inner are different types; the enclosing type of a member's
        // type is not substituted from the receiver, so Outer<T>.Inner may be Outer<String>.Inner
        ClassNode leftOuter = left.getOuterClassType(), rightOuter = right.getOuterClassType();
        if (isParameterized(leftOuter) != isParameterized(rightOuter)) return false;
        if (isParameterized(leftOuter) && !GenericsUtils.hasUnresolvedGenerics(leftOuter)
                && !GenericsUtils.hasUnresolvedGenerics(rightOuter) && !sameTypeArgument(leftOuter, rightOuter)) return false;
        GenericsType[] leftArgs = left.getGenericsTypes();
        GenericsType[] rightArgs = right.getGenericsTypes();
        // Null or empty arguments are a raw type or an uninferred diamond.
        if (leftArgs == null || rightArgs == null || leftArgs.length == 0 || rightArgs.length == 0) return true;
        if (leftArgs.length != rightArgs.length) return false;
        for (int i = 0; i < leftArgs.length; i += 1) {
            if (!sameGenericsArgument(leftArgs[i], rightArgs[i])) return false;
        }
        return true;
    }

    private static boolean sameGenericsArgument(final GenericsType left, final GenericsType right) {
        if (left.isWildcard() != right.isWildcard() || left.isPlaceholder() != right.isPlaceholder()) return false;
        if (left.isPlaceholder()) return left.getName().equals(right.getName());
        if (left.isWildcard()) {
            ClassNode leftLower = left.getLowerBound();
            ClassNode rightLower = right.getLowerBound();
            if ((leftLower == null) != (rightLower == null)) return false;
            if (leftLower != null) return sameTypeArgument(leftLower, rightLower);
            boolean leftUnbounded = unboundedOrObject(left);
            boolean rightUnbounded = unboundedOrObject(right);
            if (leftUnbounded || rightUnbounded) return leftUnbounded && rightUnbounded;
            return sameUpperBounds(left.getUpperBounds(), right.getUpperBounds());
        }
        return sameTypeArgument(left.getType(), right.getType());
    }

    private static boolean unboundedOrObject(final GenericsType wildcard) {
        ClassNode[] upper = wildcard.getUpperBounds();
        return upper == null || upper.length == 0
                || (upper.length == 1 && isObjectType(upper[0]) && !upper[0].isGenericsPlaceHolder());
    }

    private static boolean sameUpperBounds(final ClassNode[] left, final ClassNode[] right) {
        if (left == null || right == null || left.length != right.length) return false;
        for (int i = 0; i < left.length; i += 1) {
            if (!sameTypeArgument(left[i], right[i])) return false;
        }
        return true;
    }

    /**
     * {@code List<? extends T>} assigned to {@code List<? extends Number}} is
     * legal only when {@code T}'s bound is a subtype of {@code Number}.
     * Raw types and diamonds (no type arguments yet) are not this check.
     * Two type variables are left to inference.
     *
     * @return {@code true} when the assignment should be rejected
     */
    static boolean typeVariableBoundEscapes(final ClassNode target, final ClassNode source) {
        if (target == null || source == null) return false;
        if (!target.equals(source)) {
            if (!implementsInterfaceOrIsSubclassOf(source, target)) return false;
            ClassNode viewed = GenericsUtils.parameterizeType(source, target);
            if (viewed == null || viewed == source) return false;
            return typeVariableBoundEscapes(target, viewed);
        }
        GenericsType[] targetArgs = target.getGenericsTypes();
        GenericsType[] sourceArgs = source.getGenericsTypes();
        if (targetArgs == null || sourceArgs == null || targetArgs.length == 0 || sourceArgs.length == 0
                || targetArgs.length != sourceArgs.length) {
            return false;
        }
        for (int i = 0; i < targetArgs.length; i += 1) {
            if (typeVariableArgumentEscapes(targetArgs[i], sourceArgs[i])) return true;
        }
        return false;
    }

    private static boolean typeVariableArgumentEscapes(final GenericsType target, final GenericsType source) {
        if (target == null || source == null || !mentionsTypeVariable(source)) return false;
        // "#T" is an inference variable (diamond, literal), not a declared bound.
        if (isInferencePlaceholder(source)) return false;
        if (source.isPlaceholder() || (source.getType() != null && source.getType().isGenericsPlaceHolder() && !source.isWildcard())) {
            return !targetAdmitsTypeVariable(target, typeVariableBound(source.getType()));
        }
        if (source.isWildcard()) {
            ClassNode upper = firstBound(source.getUpperBounds());
            if (upper != null && isInferencePlaceholder(upper)) return false;
            if (upper != null && (upper.isGenericsPlaceHolder() || GenericsUtils.hasUnresolvedGenerics(upper))) {
                return !targetAdmitsUpperVariable(target, typeVariableBound(upper));
            }
            ClassNode lower = source.getLowerBound();
            if (lower != null && isInferencePlaceholder(lower)) return false;
            if (lower != null && (lower.isGenericsPlaceHolder() || GenericsUtils.hasUnresolvedGenerics(lower))) {
                return !targetAdmitsLowerVariable(target, lower);
            }
            return false;
        }
        return typeVariableBoundEscapes(target.getType(), source.getType());
    }

    /**
     * JLS 4.5.1: {@code ? extends A} is contained by {@code ? extends B} only when
     * {@code A} is a subtype of {@code B}. A raw type is not a subtype of a
     * parameterization of a generic type (JLS 4.8, 4.10.2), and a type that is not
     * a type variable is not a subtype of one. So {@code Class<? extends List>} is
     * not a {@code Class<? extends List<String>>}, and {@code Class<? extends Number>}
     * is not a {@code Class<? extends T>} for {@code T extends Number}. A type variable
     * is taken as its bound, and within the bounds a raw type argument is not a
     * parameterized one: {@code List<List>} is not a {@code List<List<String>>}.
     * A raw type that is not within the bound of a wildcard stays an unchecked conversion.
     *
     * @return {@code true} when the assignment should be rejected
     */
    static boolean upperBoundEscapes(final ClassNode target, final ClassNode source) {
        return upperBoundEscapes(target, source, false);
    }

    /**
     * @param withinBound {@code target} and {@code source} are the bounds of two wildcards, or type arguments of those bounds
     */
    private static boolean upperBoundEscapes(final ClassNode target, final ClassNode source, final boolean withinBound) {
        if (target == null || source == null) return false;
        if (!target.equals(source)) {
            if (source.getGenericsTypes() == null || !implementsInterfaceOrIsSubclassOf(source, target)) return false;
            ClassNode viewed = GenericsUtils.parameterizeType(source, target);
            if (viewed == null || viewed == source || !target.equals(viewed)) return false;
            return upperBoundEscapes(target, viewed, withinBound);
        }
        GenericsType[] targetArgs = target.getGenericsTypes();
        GenericsType[] sourceArgs = source.getGenericsTypes();
        if (targetArgs == null || sourceArgs == null || targetArgs.length != sourceArgs.length) return false;
        for (int i = 0; i < targetArgs.length; i += 1) {
            ClassNode targetBound = soleUpperBound(targetArgs[i]);
            ClassNode sourceBound = soleUpperBound(sourceArgs[i]);
            if (targetBound != null && sourceBound != null) {
                if (upperBoundNotWithin(sourceBound, targetBound)) return true;
            } else if (targetBound != null && !sourceArgs[i].isWildcard() && !sourceArgs[i].isPlaceholder()
                    && isParameterized(targetBound.getOuterClassType()) && targetBound.equals(sourceArgs[i].getType())
                    && !isParameterized(sourceArgs[i].getType().getOuterClassType())) {
                return true; // Outer.Inner is not within "? extends Outer<T>.Inner"
            } else if (withinBound && rawTypeArgument(sourceArgs[i], targetArgs[i])) {
                return true;
            }
        }
        return false;
    }

    /** {@code B} for a wildcard {@code ? extends B}, otherwise {@code null}. */
    private static ClassNode soleUpperBound(final GenericsType type) {
        if (type.isPlaceholder() || !type.isWildcard() || type.getLowerBound() != null) return null;
        ClassNode[] upper = type.getUpperBounds();
        return upper != null && upper.length == 1 ? upper[0] : null;
    }

    private static boolean upperBoundNotWithin(ClassNode source, ClassNode target) {
        while (source.isArray() && target.isArray()) {
            source = source.getComponentType();
            target = target.getComponentType();
        }
        // "#T" is still to be inferred
        if (isInferencePlaceholder(source) || isInferencePlaceholder(target)) return false;
        if (source.isGenericsPlaceHolder()) {
            if (target.isGenericsPlaceHolder()) return false; // two type variables are checked through their bounds
            source = typeVariableBound(source); // JLS 4.10.2: a type variable is a subtype of its bound
        } else if (target.isGenericsPlaceHolder()) {
            return true;
        }
        boolean rareType = isParameterized(target.getOuterClassType()); // Outer<T>.Inner
        if ((rareType || isParameterized(target)) && isRawType(source)) return true;
        if (rareType && source.equals(target) && source.getOuterClassType() == null) return true;
        return upperBoundEscapes(target, source, true);
    }

    /**
     * True when {@code source} is a raw type and {@code target} is a parameterization
     * of the same generic type, for two type arguments that are not wildcards, or
     * when that holds for a type argument of theirs.
     */
    private static boolean rawTypeArgument(final GenericsType source, final GenericsType target) {
        if (source.isWildcard() || source.isPlaceholder() || target.isWildcard() || target.isPlaceholder()) return false;
        ClassNode sourceType = source.getType(), targetType = target.getType();
        while (sourceType.isArray() && targetType.isArray()) {
            sourceType = sourceType.getComponentType();
            targetType = targetType.getComponentType();
        }
        if (!targetType.equals(sourceType)) return false;
        if (isParameterized(targetType) && isRawType(sourceType)) return true;
        return upperBoundEscapes(targetType, sourceType, true);
    }

    private static boolean isParameterized(final ClassNode type) {
        return type != null && ArrayGroovyMethods.asBoolean(type.getGenericsTypes());
    }

    private static boolean isRawType(final ClassNode type) {
        return type.getGenericsTypes() == null && type.redirect().getGenericsTypes() != null;
    }

    private static boolean isInferencePlaceholder(final GenericsType type) {
        if (type == null) return false;
        if (type.isPlaceholder() && type.getName() != null && type.getName().startsWith("#")) return true;
        return isInferencePlaceholder(type.getType());
    }

    private static boolean isInferencePlaceholder(final ClassNode type) {
        if (type == null || !type.isGenericsPlaceHolder()) return false;
        String name = type.getUnresolvedName();
        return name != null && name.startsWith("#");
    }

    /** {@code T <= ? extends B} when {@code bound(T) <: B}. A concrete target does not contain {@code T}. */
    private static boolean targetAdmitsTypeVariable(final GenericsType target, final ClassNode sourceBound) {
        if (target.isPlaceholder()) return true;
        if (!target.isWildcard()) return false;
        if (target.getLowerBound() != null) return false;
        ClassNode upper = firstBound(target.getUpperBounds());
        if (upper == null || isUnboundedUpper(upper)) return true;
        if (upper.isGenericsPlaceHolder()) return true;
        return boundWithin(sourceBound, upper);
    }

    /** {@code ? extends T <= ? extends B} iff {@code bound(T) <: B}. */
    private static boolean targetAdmitsUpperVariable(final GenericsType target, final ClassNode sourceBound) {
        if (target.isPlaceholder()) return true;
        if (target.isWildcard()) {
            if (target.getLowerBound() != null) return false;
            ClassNode upper = firstBound(target.getUpperBounds());
            if (upper == null || isUnboundedUpper(upper)) return true;
            if (upper.isGenericsPlaceHolder()) return true;
            return boundWithin(sourceBound, upper);
        }
        return false;
    }

    /**
     * {@code ? super A <= ?}, {@code ? super A <= ? extends Object}, and
     * {@code ? super A <= ? super S} when {@code S} and {@code A} are the same
     * type. A method reference is typed as the target, so {@code Predicate<? super T>}
     * must be assignable to itself.
     */
    private static boolean targetAdmitsLowerVariable(final GenericsType target, final ClassNode sourceLower) {
        if (!target.isWildcard()) return false;
        ClassNode targetLower = target.getLowerBound();
        if (targetLower == null) {
            ClassNode upper = firstBound(target.getUpperBounds());
            return upper == null || isUnboundedUpper(upper);
        }
        return sameLowerBound(targetLower, sourceLower);
    }

    private static boolean sameLowerBound(final ClassNode targetLower, final ClassNode sourceLower) {
        if (targetLower == null || sourceLower == null) return false;
        if (targetLower.isGenericsPlaceHolder() || sourceLower.isGenericsPlaceHolder()) {
            if (!targetLower.isGenericsPlaceHolder() || !sourceLower.isGenericsPlaceHolder()) return false;
            String targetName = targetLower.getUnresolvedName();
            String sourceName = sourceLower.getUnresolvedName();
            return targetName != null && targetName.equals(sourceName);
        }
        return sameTypeArgument(targetLower, sourceLower);
    }

    private static boolean boundWithin(final ClassNode sourceBound, final ClassNode targetBound) {
        if (sourceBound == null || targetBound == null) return false;
        if (isUnboundedUpper(targetBound)) return true;
        return GenericsUtils.buildWildcardType(targetBound).isCompatibleWith(sourceBound);
    }

    private static ClassNode typeVariableBound(final ClassNode placeholder) {
        if (placeholder == null) return OBJECT_TYPE;
        if (!placeholder.isGenericsPlaceHolder()) return placeholder;
        GenericsType declared = placeholder.asGenericsType();
        ClassNode upper = firstBound(declared.getUpperBounds());
        if (upper != null && upper != placeholder) {
            if (upper.isGenericsPlaceHolder()) return typeVariableBound(upper);
            if (!isObjectType(upper)) return upper;
        }
        ClassNode redirect = placeholder.redirect();
        if (redirect != null && redirect != placeholder && !redirect.isGenericsPlaceHolder() && !isObjectType(redirect)) {
            return redirect;
        }
        return OBJECT_TYPE;
    }

    private static boolean mentionsTypeVariable(final GenericsType type) {
        if (type.isPlaceholder()) return true;
        if (type.getType() != null && type.getType().isGenericsPlaceHolder()) return true;
        if (type.getLowerBound() != null && (type.getLowerBound().isGenericsPlaceHolder()
                || GenericsUtils.hasUnresolvedGenerics(type.getLowerBound()))) return true;
        ClassNode[] upper = type.getUpperBounds();
        if (upper != null) {
            for (ClassNode bound : upper) {
                if (bound != null && (bound.isGenericsPlaceHolder() || GenericsUtils.hasUnresolvedGenerics(bound))) return true;
            }
        }
        return !type.isWildcard() && type.getType() != null && GenericsUtils.hasUnresolvedGenerics(type.getType());
    }

    private static ClassNode firstBound(final ClassNode[] bounds) {
        return bounds == null || bounds.length == 0 ? null : bounds[0];
    }

    private static boolean isUnboundedUpper(final ClassNode upper) {
        return upper == null || (isObjectType(upper) && !upper.isGenericsPlaceHolder());
    }

    /**
     * Checks whether the candidate method remains compatible after applying generics.
     */
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
            if (candidateMethod instanceof ConstructorNode && isDiamondReceiver(receiver)) {
                // "new C<>(...)": the type parameters of C are inferred from the arguments, like those of a generic method
                GenericsUtils.extractPlaceholders(GenericsUtils.makeClassSafe0(OBJECT_TYPE, candidateMethod.getDeclaringClass().getGenericsTypes()), candidateGenerics);
            } else if (candidateMethod instanceof ConstructorNode) {
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
            // Declared parameters only. The resolved type inside inferenceCheck
            // is where diamonds and Collectors.toList() still carry wildcards
            // that are not containment errors.
            if (!GenericsUtils.hasUnresolvedGenerics(parameterType)
                    && !GenericsUtils.hasUnresolvedGenerics(argumentType)
                    && (concreteArgumentContainsWildcard(parameterType, argumentType)
                        || upperBoundEscapes(parameterType, argumentType))) {
                failure = true;
            }
            // a parameter typed by type variables in scope, not by the method's own, is checked as an assignment;
            // take its type before the argument is used to infer anything
            if (!failure && !skipBecauseOfInnerClassNotReceiver && !isRawReceiver(receiver) && !isDiamondReceiver(receiver)
                    && !mentionsTypeParameterOf(parameterType, candidateMethod)) {
                ClassNode resolvedType = applyGenericsContext(candidateGenerics, parameterType);
                if (GenericsUtils.hasUnresolvedGenerics(resolvedType) && !GenericsUtils.hasUnresolvedGenerics(argumentType)
                        && (upperBoundEscapes(resolvedType, argumentType) || typeVariableArgumentGetsWildcard(resolvedType, argumentType))) {
                    failure = true;
                }
            }
            failure |= inferenceCheck(fixedPlaceHolders, candidateGenerics, parameterType, argumentType, i >= lastParamIndex);

            if (i == 0 && isExtensionMethod) { // re-load fixed names for extension
                fixedPlaceHolders = extractResolvedPlaceHolders(candidateGenerics);
            }
        }
        return !failure;
    }

    private static boolean isRawReceiver(final ClassNode receiver) {
        return receiver.getGenericsTypes() == null && receiver.redirect().getGenericsTypes() != null && !receiver.isGenericsPlaceHolder();
    }

    /**
     * For {@code new C<>(...)} the type parameters of {@code C} are inferred from the arguments.
     */
    private static boolean isDiamondReceiver(final ClassNode receiver) {
        return receiver.getGenericsTypes() != null && receiver.getGenericsTypes().length == 0;
    }

    private static boolean mentionsTypeParameterOf(final ClassNode type, final MethodNode method) {
        GenericsType[] typeParameters = method.getGenericsTypes();
        if (typeParameters == null || typeParameters.length == 0) return false;
        Set<String> used = new HashSet<>();
        collectTypeVariableNames(type, used);
        for (GenericsType tp : typeParameters) {
            if (used.contains(tp.getName())) return true;
        }
        return false;
    }

    private static void collectTypeVariableNames(final ClassNode type, final Set<String> names) {
        if (type == null) return;
        if (type.isArray()) {
            collectTypeVariableNames(type.getComponentType(), names);
        } else if (type.isGenericsPlaceHolder()) {
            names.add(type.getUnresolvedName());
        } else if (type.getGenericsTypes() != null) {
            for (GenericsType gt : type.getGenericsTypes()) {
                if (gt.isPlaceholder()) {
                    names.add(gt.getName());
                } else if (gt.isWildcard()) {
                    collectTypeVariableNames(gt.getLowerBound(), names);
                    if (gt.getUpperBounds() != null) for (ClassNode bound : gt.getUpperBounds()) collectTypeVariableNames(bound, names);
                } else {
                    collectTypeVariableNames(gt.getType(), names);
                }
            }
        }
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

            if (resolved.isPlaceholder() && hasNonTrivialBounds(resolved)) {
                int index = indexOfCapturedWildcard(wrappedArgument, candidate);
                if (index >= 0) {
                    if (compatibleCapture(resolved, type, wrappedArgument, index, lastArg, resolvedMethodGenerics)) continue;
                    return true; // incompatible
                }
            }

            if (!compatibleConnection(resolved, candidate)) {
                if (!resolved.isPlaceholder() && !resolved.isWildcard()
                        && !fixedPlaceHolders.contains(entry.getKey())) {
                    // GROOVY-5692, GROOVY-10006: multiple witnesses
                    if (compatibleConnection(candidate, resolved)) {
                        // was "T=Integer" and now is "T=Number" or "T=Object"
                        resolvedMethodGenerics.put(entry.getKey(), candidate);
                        continue;
                    } else if (!candidate.isPlaceholder() && !candidate.isWildcard()) {
                        // combine "T=Integer" and "T=String" to produce "T=(Serializable & ... )"
                        ClassNode lub = lowestUpperBound(candidate.getType(), resolved.getType());
                        resolvedMethodGenerics.put(entry.getKey(), new GenericsType(lub));
                        continue;
                    }
                }
                return true; // incompatible
            }
            if (!fixedPlaceHolders.contains(entry.getKey())
                    && !resolved.isPlaceholder() && !resolved.isWildcard()
                    && !candidate.isPlaceholder() && !candidate.isWildcard()) {
                // resolved "T=Object" and candidate "T=Object" or "T=String"; or
                // GROOVY-11664: resolved "T=Class<A>" and candidate "T=Class<B>"
                ClassNode oldT = resolved.getType(), newT = candidate.getType();
                resolvedMethodGenerics.put(entry.getKey(), new GenericsType(lowestUpperBound(oldT, newT)));
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
        if (resolved.isWildcard()
                // TODO: figure out capture of super
                && resolved.getLowerBound() == null) {
            return false; // GROOVY-8084, GROOVY-9074, GROOVY-10588
        }
        // JLS 5.1.10: capture of "? super T" has lower bound T. A concrete
        // witness is compatible only when it is a subtype of T. The upper
        // bound is Object, which would accept every reference. Wildcard
        // witnesses stay on the historical path so inference of "? super T"
        // is unchanged.
        if (resolved.isWildcard() && resolved.getLowerBound() != null
                && !connection.isWildcard() && !connection.isPlaceholder()) {
            ClassNode candidate = connection.getType();
            ClassNode lowerBound = resolved.getLowerBound();
            if (candidate == null || lowerBound == null) return false;
            // A raw witness keeps the historical raw subtype test. A parameterized
            // witness must be a subtype of T, including its own type arguments.
            if (isPrimitiveType(candidate)) candidate = getWrapper(candidate);
            if (isPrimitiveType(lowerBound)) lowerBound = getWrapper(lowerBound);
            if (candidate.getGenericsTypes() == null || lowerBound.getGenericsTypes() == null) {
                return implementsInterfaceOrIsSubclassOf(candidate, lowerBound);
            }
            return GenericsUtils.buildWildcardType(lowerBound).isCompatibleWith(candidate);
        }
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

    /**
     * Returns the position of {@code witness} among the type arguments of {@code argumentType}
     * when it is a wildcard that capture conversion replaces (JLS 5.1.10), otherwise {@code -1}.
     * A wildcard nested in a type argument is not captured.
     */
    private static int indexOfCapturedWildcard(final ClassNode argumentType, final GenericsType witness) {
        if (!witness.isWildcard()) return -1;
        if (witness.getUpperBounds() != null) {
            for (ClassNode bound : witness.getUpperBounds()) {
                if (isInferencePlaceholder(bound)) return -1; // still to be inferred
            }
        }
        GenericsType[] typeArguments = argumentType.getGenericsTypes();
        GenericsType[] typeParameters = argumentType.redirect().getGenericsTypes();
        if (typeArguments == null || typeParameters == null || typeParameters == typeArguments
                || typeParameters.length != typeArguments.length) {
            return -1;
        }
        for (int i = 0, n = typeArguments.length; i < n; i += 1) {
            if (typeArguments[i] == witness) return i;
        }
        return -1;
    }

    /**
     * Checks the capture of the wildcard at {@code index} of {@code argumentType} as the witness
     * for {@code typeParameter}. The capture is a fresh type variable whose upper bounds are the
     * upper bound of the wildcard, if it has one, and the bounds declared for that type argument
     * (JLS 5.1.10). It must be within every bound of the type parameter, where the bound may
     * mention the type parameter itself or another one, and the captured argument must fit the
     * parameter (JLS 18.5.1).
     */
    private static boolean compatibleCapture(final GenericsType typeParameter, final ClassNode parameterType, final ClassNode argumentType, final int index, final boolean lastArg, final Map<GenericsTypeName, GenericsType> resolvedMethodGenerics) {
        GenericsType[] typeArguments = argumentType.getGenericsTypes();
        GenericsType wildcard = typeArguments[index];
        ClassNode capture = new ClassNode("capture-of " + wildcard, 0, OBJECT_TYPE);
        GenericsType witness = new GenericsType(capture);

        List<ClassNode> captureBounds = new ArrayList<>();
        if (wildcard.getUpperBounds() != null) {
            for (ClassNode bound : wildcard.getUpperBounds()) {
                captureBounds.add(typeVariableBound(bound));
            }
        }
        GenericsType[] typeParameters = argumentType.redirect().getGenericsTypes();
        if (typeParameters[index].getUpperBounds() != null) {
            Map<GenericsTypeName, GenericsType> spec = new HashMap<>();
            for (int i = 0, n = typeParameters.length; i < n; i += 1) {
                spec.put(new GenericsTypeName(typeParameters[i].getName()), i == index ? witness : typeArguments[i]);
            }
            Collections.addAll(captureBounds, applyGenericsContext(spec, typeParameters[index].getUpperBounds()));
        }
        if (captureBounds.isEmpty()) captureBounds.add(OBJECT_TYPE);
        // the capture stands in for the type parameter below, so give it supertypes
        List<ClassNode> interfaces = new ArrayList<>();
        for (ClassNode bound : captureBounds) {
            if (bound.isInterface()) {
                interfaces.add(bound);
            } else if (!bound.isArray() && isObjectType(capture.getUnresolvedSuperClass())) {
                capture.setSuperClass(bound);
            }
        }
        capture.setInterfaces(interfaces.toArray(ClassNode.EMPTY_ARRAY));

        Map<GenericsTypeName, GenericsType> spec = Collections.singletonMap(new GenericsTypeName(typeParameter.getName()), witness);
        for (ClassNode bound : typeParameter.getUpperBounds()) {
            if (!captureWithinBound(captureBounds, wildcard, applyGenericsContext(spec, bound), resolvedMethodGenerics, new HashSet<>())) return false;
        }

        ClassNode capturedType = argumentType.getPlainNodeReference();
        GenericsType[] capturedArguments = typeArguments.clone();
        capturedArguments[index] = witness;
        capturedType.setGenericsTypes(capturedArguments);
        return typeCheckMethodArgumentWithGenerics(applyGenericsContext(spec, parameterType), capturedType, lastArg);
    }

    /**
     * The capture is a subtype of {@code bound} when one of its upper bounds is. The first
     * upper bound that derives from the class of {@code bound} decides, type arguments included.
     * A bound that is or mentions another type parameter, such as {@code T} or {@code List<T>}
     * for {@code U} in {@code <T extends Number, U extends T>} or {@code <T extends Number,
     * U extends List<T>>}, is checked through the bounds of that type parameter.
     */
    private static boolean captureWithinBound(final List<ClassNode> captureBounds, final GenericsType wildcard, final ClassNode bound,
            final Map<GenericsTypeName, GenericsType> resolvedMethodGenerics, final Set<String> visiting) {
        if (isUnboundedUpper(bound)) return true;
        if (bound.isGenericsPlaceHolder()) {
            String name = bound.getUnresolvedName();
            if (wildcard != null && wildcard.getUpperBounds() != null) {
                for (ClassNode upper : wildcard.getUpperBounds()) { // "? extends T" is within T
                    if (upper.isGenericsPlaceHolder() && name.equals(upper.getUnresolvedName())) return true;
                }
            }
            if (!visiting.add(name)) return true;
            // another argument may widen the type parameter, so its bounds are the constraint
            GenericsType other = resolvedMethodGenerics.get(new GenericsTypeName(name));
            GenericsType declared = other != null && other.isPlaceholder() ? other : bound.asGenericsType();
            ClassNode[] upperBounds = declared.getUpperBounds();
            if (upperBounds != null) {
                for (ClassNode upper : upperBounds) {
                    if (upper == bound) continue;
                    if (!captureWithinBound(captureBounds, wildcard, upper, resolvedMethodGenerics, visiting)) return false;
                }
            }
            return true;
        }
        for (ClassNode captureBound : captureBounds) {
            if (implementsInterfaceOrIsSubclassOf(captureBound, bound)) {
                ClassNode target = bound;
                if (GenericsUtils.hasUnresolvedGenerics(bound)) {
                    // infer the type parameters that the bound mentions from the capture's bound
                    Map<GenericsTypeName, GenericsType> connections = new HashMap<>();
                    extractGenericsConnections(connections, captureBound, bound);
                    for (Map.Entry<GenericsTypeName, GenericsType> entry : connections.entrySet()) {
                        GenericsType value = entry.getValue();
                        if (value.isWildcard()) return false; // a type parameter is not inferred as a wildcard
                        if (value.isPlaceholder()) continue;
                        ClassNode placeholder = make(entry.getKey().getName());
                        placeholder.setGenericsPlaceHolder(true);
                        if (!captureWithinBound(Collections.singletonList(typeVariableBound(value.getType())), null,
                                placeholder, resolvedMethodGenerics, visiting)) {
                            return false;
                        }
                    }
                    target = applyGenericsContext(connections, bound);
                }
                return GenericsUtils.buildWildcardType(target).isCompatibleWith(captureBound);
            }
        }
        return false;
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

    /**
     * Applies resolved placeholders and the surrounding context to the supplied type.
     */
    public static ClassNode resolveClassNodeGenerics(Map<GenericsTypeName, GenericsType> resolvedPlaceholders, final Map<GenericsTypeName, GenericsType> placeholdersFromContext, final ClassNode currentType) {
        ClassNode type = currentType; // GROOVY-10280, et al.
        type = applyGenericsContext(resolvedPlaceholders, type);
        type = applyGenericsContext(placeholdersFromContext, type);
        return type;
    }

    /**
     * Applies new generics connections to previously resolved placeholders.
     */
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
                    GenericsType[] replacementGenerics = replacementType.getGenericsTypes();
                    ClassNode suitabilityType = !replacementType.isGenericsPlaceHolder()
                            ? replacementType
                            : (replacementGenerics != null ? extractType(replacementGenerics[0]) : replacementType.redirect());

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
            } else if (ArrayGroovyMethods.asBoolean(gt.getUpperBounds())) {
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
            extractGenericsConnections(connections, type.getOuterClassType(), target.getOuterClass()); //GROOVY-10646

        } else if (implementsInterfaceOrIsSubclassOf(type, target)) {
            ClassNode superClass = getNextSuperClass(type, target);
            if (GenericsUtils.hasUnresolvedGenerics(superClass)) {
                // propagate type arguments to the super class or interface
                Map<GenericsTypeName, GenericsType> spec = new HashMap<>();
                if (type.getGenericsTypes() != null) {
                    extractGenericsConnections(spec, type, type.redirect());
                } else if (type.redirect().getGenericsTypes() != null) {
                    for (GenericsType tp : type.redirect().getGenericsTypes()) {
                        spec.put(new GenericsTypeName(tp.getName()), new GenericsType(extractType(tp))); //GROOVY-10651
                    }
                }
                superClass = applyGenericsContext(spec, superClass);
            }
            extractGenericsConnections(connections, superClass, target);
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

    /**
     * Returns the generics for the innermost component type of an array.
     */
    static GenericsType[] getGenericsWithoutArray(final ClassNode type) {
        if (type.isArray()) return getGenericsWithoutArray(type.getComponentType());
        return type.getGenericsTypes();
    }

    /**
     * Tracks placeholder names whose bounds are currently being expanded by
     * {@link #applyGenericsContext}, so the recursion can break cycles caused
     * by F-bounded type parameters whose self-reference appears inside a
     * wildcard bound (GROOVY-11022). The backing deque is allocated lazily —
     * the cycle-breaking machinery is only needed when an F-bounded
     * placeholder is actually encountered, which is rare on the STC hot path.
     */
    private static final class BoundExpansionContext {
        private Deque<GenericsTypeName> expanding;

        boolean isExpanding(final GenericsTypeName name) {
            return expanding != null && expanding.contains(name);
        }

        void enter(final GenericsTypeName name) {
            if (expanding == null) expanding = new ArrayDeque<>();
            expanding.push(name);
        }

        void exit() {
            expanding.pop();
        }
    }

    /**
     * Applies the supplied generics context to the given generic arguments.
     */
    static GenericsType[] applyGenericsContext(final Map<GenericsTypeName, GenericsType> spec, final GenericsType[] gts) {
        if (gts == null || spec == null || spec.isEmpty()) return gts;
        return applyGenericsContext(new BoundExpansionContext(), spec, gts);
    }

    private static GenericsType applyGenericsContext(final Map<GenericsTypeName, GenericsType> spec, final GenericsType gt) {
        return applyGenericsContext(new BoundExpansionContext(), spec, gt);
    }

    private static GenericsType[] applyGenericsContext(final BoundExpansionContext ctx, final Map<GenericsTypeName, GenericsType> spec, final GenericsType[] gts) {
        if (gts == null) return null;
        int n = gts.length;
        if (n == 0) return gts;
        GenericsType[] newGTs = new GenericsType[n];
        for (int i = 0; i < n; i += 1) {
            newGTs[i] = applyGenericsContext(ctx, spec, gts[i]);
        }
        return newGTs;
    }

    private static GenericsType applyGenericsContext(final BoundExpansionContext ctx, final Map<GenericsTypeName, GenericsType> spec, final GenericsType gt) {
        ClassNode type = gt.getType();

        if (gt.isPlaceholder()) {
            GenericsTypeName name = new GenericsTypeName(gt.getName());
            GenericsType specType = spec.get(name);
            if (specType != null) return specType;
            if (hasNonTrivialBounds(gt)) {
                // GROOVY-11022: avoid infinite recursion on F-bounded type
                // parameters whose self-reference appears inside a wildcard
                // bound (e.g. `<K extends Comparable<? super K>>`)
                if (ctx.isExpanding(name)) return gt;
                ctx.enter(name);
                try {
                    GenericsType newGT = new GenericsType(type, applyGenericsContext(ctx, spec, gt.getUpperBounds()), applyGenericsContext(ctx, spec, gt.getLowerBound()));
                    newGT.setPlaceholder(true);
                    newGT.setGenericDeclaration(gt.getGenericDeclaration());
                    return newGT;
                } finally {
                    ctx.exit();
                }
            }
            return gt;
        }

        if (gt.isWildcard()) { // TODO: What if a bound itself resolves to a wildcard?
            ClassNode[] upperBounds = applyGenericsContext(ctx, spec, gt.getUpperBounds());
            ClassNode   lowerBound = applyGenericsContext(ctx, spec, gt.getLowerBound());
            GenericsType newGT = new GenericsType(type, upperBounds, lowerBound);
            newGT.setWildcard(true);
            return newGT;
        }

        ClassNode newType;
        if (type.isArray()) {
            newType = applyGenericsContext(ctx, spec, type.getComponentType()).makeArray();
        } else if (type.getGenericsTypes() == null//type.isGenericsPlaceHolder()
                && type.getOuterClass() == null) {
            return gt;
        } else {
            newType = type.getPlainNodeReference();
            newType.setGenericsPlaceHolder(type.isGenericsPlaceHolder());
            newType.setGenericsTypes(applyGenericsContext(ctx, spec, type.getGenericsTypes()));

            // GROOVY-10646: non-static inner class + outer class type parameter
            if ((type.getModifiers() & Opcodes.ACC_STATIC) == 0) {
                ClassNode oc = type.getOuterClass();
                if (oc != null && oc.getGenericsTypes() != null) {
                    newType.setOuterClassType(applyGenericsContext(ctx, spec, oc));
                }
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

    /**
     * Applies the supplied generics context to the given types.
     */
    static ClassNode[] applyGenericsContext(final Map<GenericsTypeName, GenericsType> spec, final ClassNode[] types) {
        if (types == null) return null;
        return applyGenericsContext(new BoundExpansionContext(), spec, types);
    }

    private static ClassNode[] applyGenericsContext(final BoundExpansionContext ctx, final Map<GenericsTypeName, GenericsType> spec, final ClassNode[] types) {
        if (types == null) return null;
        final int nTypes = types.length;
        ClassNode[] newTypes = new ClassNode[nTypes];
        for (int i = 0; i < nTypes; i += 1) {
            newTypes[i] = applyGenericsContext(ctx, spec, types[i]);
        }
        return newTypes;
    }

    /**
     * Applies the supplied generics context to the given type.
     */
    static ClassNode applyGenericsContext(final Map<GenericsTypeName, GenericsType> spec, final ClassNode type) {
        if (type == null || !isUsingGenericsOrIsArrayUsingGenerics(type)) {
            return type;
        }
        return applyGenericsContext(new BoundExpansionContext(), spec, type);
    }

    private static ClassNode applyGenericsContext(final BoundExpansionContext ctx, final Map<GenericsTypeName, GenericsType> spec, final ClassNode type) {
        if (type == null || !isUsingGenericsOrIsArrayUsingGenerics(type)) {
            return type;
        }
        if (type.isArray()) {
            return applyGenericsContext(ctx, spec, type.getComponentType()).makeArray();
        }

        GenericsType[] gt = type.getGenericsTypes();
        if (gt == null && type.isGenericsPlaceHolder()) {
            gt = new GenericsType[]{type.asGenericsType()};
        }
        if (asBoolean(spec)) {
            gt = applyGenericsContext(ctx, spec, gt);
        }
        if (!type.isGenericsPlaceHolder()) { // convert Type<T> to Type<...>
            ClassNode cn = type.getPlainNodeReference();
            cn.setGenericsTypes(gt);
            return cn;
        }

        if (gt != null && gt.length > 0) {
            if (!gt[0].isPlaceholder()) { // convert T to Type or Type<...>
                return getCombinedBoundType(gt[0]);
            }

            if (type.getGenericsTypes() == null || type.getGenericsTypes()[0] != gt[0]) { // convert T to X
                ClassNode cn = make(gt[0].getName()), erasure = getCombinedBoundType(gt[0]).redirect();
                cn.setGenericsPlaceHolder(true);
                cn.setGenericsTypes(gt);
                cn.setRedirect(erasure);
                return cn;
            }
        }

        return type; // nothing to do
    }

    /**
     * Returns the most representative bound for the supplied generics type.
     */
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

    /**
     * Combines two generics witnesses into a single representative type.
     */
    static GenericsType getCombinedGenericsType(final GenericsType gt1, final GenericsType gt2) {
        // GROOVY-7992, GROOVY-10765: "? super T" for gt1 or gt2?
        if (isUnboundedWildcard(gt1) != isUnboundedWildcard(gt2)) return isUnboundedWildcard(gt2) ? gt1 : gt2;
        // GROOVY-11028, et al.: empty list / map for gt1 or gt2?
        if (gt2.isPlaceholder() && gt2.getName().startsWith("#")) return gt1;
        if (gt1.isPlaceholder() && gt1.getName().startsWith("#")) return gt2;
        // GROOVY-10315, GROOVY-10317, GROOVY-10339, GROOVY-11616
        if (!gt1.isPlaceholder() && !gt1.isWildcard()
                && !gt2.isPlaceholder() && !gt2.isWildcard()) {
            ClassNode lub = lowestUpperBound(gt1.getType(), gt2.getType());
            if (!(lub instanceof WideningCategories.LowestUpperBoundClassNode)) {
                return new GenericsType(lub);
            }
        }
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

    /**
     * Indicates whether the wildcard has no explicit bounds.
     */
    public static boolean isUnboundedWildcard(final GenericsType gt) {
        if (gt.isWildcard() && gt.getLowerBound() == null) {
            ClassNode[] upperBounds = gt.getUpperBounds();
            return (upperBounds == null || upperBounds.length == 0 || (upperBounds.length == 1
                    && isObjectType(upperBounds[0]) && !upperBounds[0].isGenericsPlaceHolder()));
        }
        return false;
    }

    /**
     * Extracts generics placeholders visible from the current {@code this} context.
     */
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

        // adjust configuration so class can be executed by this JVM; the class node below is
        // added without a source unit, so a customizer expecting one would fail, and customizers
        // have no business running on an internal, throw-away expression holder in any case
        CompilerConfiguration cc = new CompilerConfiguration(config, false);
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

    /**
     * Corrects the supplied class node against a generics specification.
     */
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

    /**
     * Returns matching setter methods from the class and its interfaces.
     */
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

    /**
     * Returns the trait self type referenced by the supplied variable, if any.
     */
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
    /**
     * DGM-like helpers for object arrays.
     */
    public static class ObjectArrayStaticTypesHelper {
        /**
         * Returns the element at the supplied index.
         */
        public static <T> T getAt(final T[] array, final int index) {
            @SuppressWarnings("unchecked")
            T t = (T)BytecodeInterface8.objectArrayGet(array, index);
            return t;
        }
        /**
         * Stores the element at the supplied index.
         */
        public static <T, U extends T> void putAt(final T[] array, final int index, final U value) {
            BytecodeInterface8.objectArraySet(array, index, value);
        }
    }

    /**
     * DGM-like helpers for {@code boolean[]} access.
     */
    public static class BooleanArrayStaticTypesHelper {
        /**
         * Returns the element at the supplied index.
         */
        public static boolean getAt(final boolean[] array, final int index) {
            return BytecodeInterface8.zArrayGet(array, index);
        }
        /**
         * Returns the boxed element at the supplied index.
         */
        @Deprecated(since = "5.0.0")
        public static Boolean getAt$$bridge(final boolean[] array, final int index) {
            return array != null ? array[index] : null;
        }
        /**
         * Stores the element at the supplied index.
         */
        public static void putAt(final boolean[] array, final int index, final boolean value) {
            BytecodeInterface8.zArraySet(array, index, value);
        }
    }

    /**
     * DGM-like helpers for {@code char[]} access.
     */
    public static class CharArrayStaticTypesHelper {
        /**
         * Returns the element at the supplied index.
         */
        public static char getAt(final char[] array, final int index) {
            return BytecodeInterface8.cArrayGet(array, index);
        }
        /**
         * Returns the boxed element at the supplied index.
         */
        @Deprecated(since = "5.0.0")
        public static Character getAt$$bridge(final char[] array, final int index) {
            return array != null ? array[index] : null;
        }
        /**
         * Stores the element at the supplied index.
         */
        public static void putAt(final char[] array, final int index, final char value) {
            BytecodeInterface8.cArraySet(array, index, value);
        }
    }

    /**
     * DGM-like helpers for {@code byte[]} access.
     */
    public static class ByteArrayStaticTypesHelper {
        /**
         * Returns the element at the supplied index.
         */
        public static byte getAt(final byte[] array, final int index) {
            return BytecodeInterface8.bArrayGet(array, index);
        }
        /**
         * Returns the boxed element at the supplied index.
         */
        @Deprecated(since = "5.0.0")
        public static Byte getAt$$bridge(final byte[] array, final int index) {
            return array != null ? array[index] : null;
        }
        /**
         * Stores the element at the supplied index.
         */
        public static void putAt(final byte[] array, final int index, final byte value) {
            BytecodeInterface8.bArraySet(array, index, value);
        }
    }

    /**
     * DGM-like helpers for {@code short[]} access.
     */
    public static class ShortArrayStaticTypesHelper {
        /**
         * Returns the element at the supplied index.
         */
        public static short getAt(final short[] array, final int index) {
            return BytecodeInterface8.sArrayGet(array, index);
        }
        /**
         * Returns the boxed element at the supplied index.
         */
        @Deprecated(since = "5.0.0")
        public static Short getAt$$bridge(final short[] array, final int index) {
            return array != null ? array[index] : null;
        }
        /**
         * Stores the element at the supplied index.
         */
        public static void putAt(final short[] array, final int index, final short value) {
            BytecodeInterface8.sArraySet(array, index, value);
        }
    }

    /**
     * DGM-like helpers for {@code int[]} access.
     */
    public static class IntArrayStaticTypesHelper {
        /**
         * Returns the element at the supplied index.
         */
        public static int getAt(final int[] array, final int index) {
            return BytecodeInterface8.intArrayGet(array, index);
        }
        /**
         * Returns the boxed element at the supplied index.
         */
        @Deprecated(since = "5.0.0")
        public static Integer getAt$$bridge(final int[] array, final int index) {
            return array != null ? array[index] : null;
        }
        /**
         * Stores the element at the supplied index.
         */
        public static void putAt(final int[] array, final int index, final int value) {
            BytecodeInterface8.intArraySet(array, index, value);
        }
    }

    /**
     * DGM-like helpers for {@code long[]} access.
     */
    public static class LongArrayStaticTypesHelper {
        /**
         * Returns the element at the supplied index.
         */
        public static long getAt(final long[] array, final int index) {
            return BytecodeInterface8.lArrayGet(array, index);
        }
        /**
         * Returns the boxed element at the supplied index.
         */
        @Deprecated(since = "5.0.0")
        public static Long getAt$$bridge(final long[] array, final int index) {
            return array != null ? array[index] : null;
        }
        /**
         * Stores the element at the supplied index.
         */
        public static void putAt(final long[] array, final int index, final long value) {
            BytecodeInterface8.lArraySet(array, index, value);
        }
    }

    /**
     * DGM-like helpers for {@code float[]} access.
     */
    public static class FloatArrayStaticTypesHelper {
        /**
         * Returns the element at the supplied index.
         */
        public static float getAt(final float[] array, final int index) {
            return BytecodeInterface8.fArrayGet(array, index);
        }
        /**
         * Returns the boxed element at the supplied index.
         */
        @Deprecated(since = "5.0.0")
        public static Float getAt$$bridge(final float[] array, final int index) {
            return array != null ? array[index] : null;
        }
        /**
         * Stores the element at the supplied index.
         */
        public static void putAt(final float[] array, final int index, final float value) {
            BytecodeInterface8.fArraySet(array, index, value);
        }
    }

    /**
     * DGM-like helpers for {@code double[]} access.
     */
    public static class DoubleArrayStaticTypesHelper {
        /**
         * Returns the element at the supplied index.
         */
        public static double getAt(final double[] array, final int index) {
            return BytecodeInterface8.dArrayGet(array, index);
        }
        /**
         * Returns the boxed element at the supplied index.
         */
        @Deprecated(since = "5.0.0")
        public static Double getAt$$bridge(final double[] array, final int index) {
            return array != null ? array[index] : null;
        }
        /**
         * Stores the element at the supplied index.
         */
        public static void putAt(final double[] array, final int index, final double value) {
            BytecodeInterface8.dArraySet(array, index, value);
        }
    }
}

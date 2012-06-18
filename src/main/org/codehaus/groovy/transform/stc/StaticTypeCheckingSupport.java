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
package org.codehaus.groovy.transform.stc;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.runtime.m12n.ExtensionModule;
import org.codehaus.groovy.runtime.m12n.ExtensionModuleRegistry;
import org.codehaus.groovy.runtime.m12n.MetaInfExtensionModule;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.objectweb.asm.Opcodes;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;

import static org.codehaus.groovy.ast.ClassHelper.*;
import static org.codehaus.groovy.syntax.Types.*;

/**
 * Static support methods for {@link StaticTypeCheckingVisitor}.
 */
public abstract class StaticTypeCheckingSupport {
    final static ClassNode
            Collection_TYPE = makeWithoutCaching(Collection.class);
    final static ClassNode Deprecated_TYPE = makeWithoutCaching(Deprecated.class);
    final static ClassNode Matcher_TYPE = makeWithoutCaching(Matcher.class);
    final static ClassNode ArrayList_TYPE = makeWithoutCaching(ArrayList.class);
    final static ExtensionMethodCache EXTENSION_METHOD_CACHE = new ExtensionMethodCache();
    final static Map<ClassNode, Integer> NUMBER_TYPES = Collections.unmodifiableMap(
            new HashMap<ClassNode, Integer>() {{
                put(ClassHelper.byte_TYPE, 0);
                put(ClassHelper.Byte_TYPE, 0);
                put(ClassHelper.short_TYPE, 1);
                put(ClassHelper.Short_TYPE, 1);
                put(ClassHelper.int_TYPE, 2);
                put(ClassHelper.Integer_TYPE, 2);
                put(ClassHelper.Long_TYPE, 3);
                put(ClassHelper.long_TYPE, 3);
                put(ClassHelper.float_TYPE, 4);
                put(ClassHelper.Float_TYPE, 4);
                put(ClassHelper.double_TYPE, 5);
                put(ClassHelper.Double_TYPE, 5);
            }});

    /**
     * This is for internal use only. When an argument method is null, we cannot determine its type, so
     * we use this one as a wildcard.
     */
    final static ClassNode UNKNOWN_PARAMETER_TYPE = ClassHelper.make("<unknown parameter type>");

    /**
     * This comparator is used when we return the list of methods from DGM which name correspond to a given
     * name. As we also lookup for DGM methods of superclasses or interfaces, it may be possible to find
     * two methods which have the same name and the same arguments. In that case, we should not add the method
     * from superclass or interface otherwise the system won't be able to select the correct method, resulting
     * in an ambiguous method selection for similar methods.
     */
    private static final Comparator<MethodNode> DGM_METHOD_NODE_COMPARATOR = new Comparator<MethodNode>() {
        public int compare(final MethodNode o1, final MethodNode o2) {
            if (o1.getName().equals(o2.getName())) {
                Parameter[] o1ps = o1.getParameters();
                Parameter[] o2ps = o2.getParameters();
                if (o1ps.length == o2ps.length) {
                    boolean allEqual = true;
                    for (int i = 0; i < o1ps.length && allEqual; i++) {
                        allEqual = o1ps[i].getType().equals(o2ps[i].getType());
                    }
                    if (allEqual) {
                        if (o1 instanceof ExtensionMethodNode && o2 instanceof ExtensionMethodNode) {
                            return compare(((ExtensionMethodNode) o1).getExtensionMethodNode(), ((ExtensionMethodNode) o2).getExtensionMethodNode());
                        }
                        return 0;
                    }
                } else {
                    return o1ps.length - o2ps.length;
                }
            }
            return 1;
        }
    };

    /**
     * Returns true for expressions of the form x[...]
     * @param expression an expression
     * @return true for array access expressions
     */
    static boolean isArrayAccessExpression(Expression expression) {
        return expression instanceof BinaryExpression && isArrayOp(((BinaryExpression) expression).getOperation().getType());
    }

    /**
     * Called on method call checks in order to determine if a method call corresponds to the
     * idiomatic o.with { ... } structure
     * @param name name of the method called
     * @param callArguments arguments of the method
     * @return true if the name is "with" and arguments consist of a single closure
     */
    public static boolean isWithCall(final String name, final Expression callArguments) {
        boolean isWithCall = "with".equals(name) && callArguments instanceof ArgumentListExpression;
        if (isWithCall) {
            ArgumentListExpression argList = (ArgumentListExpression) callArguments;
            List<Expression> expressions = argList.getExpressions();
            isWithCall = expressions.size() == 1 && expressions.get(0) instanceof ClosureExpression;
        }
        return isWithCall;
    }

    /**
     * Given a variable expression, returns the ultimately accessed variable.
     * @param ve a variable expression
     * @return the target variable
     */
    static Variable findTargetVariable(VariableExpression ve) {
        final Variable accessedVariable = ve.getAccessedVariable() != null ? ve.getAccessedVariable() : ve;
        if (accessedVariable != ve) {
            if (accessedVariable instanceof VariableExpression)
                return findTargetVariable((VariableExpression) accessedVariable);
        }
        return accessedVariable;
    }


    static Set<MethodNode> findDGMMethodsForClassNode(ClassNode clazz, String name) {
        TreeSet<MethodNode> accumulator = new TreeSet<MethodNode>(DGM_METHOD_NODE_COMPARATOR);
        findDGMMethodsForClassNode(clazz, name, accumulator);
        return accumulator;
    }

    static void findDGMMethodsForClassNode(ClassNode clazz, String name, TreeSet<MethodNode> accumulator) {
        List<MethodNode> fromDGM = EXTENSION_METHOD_CACHE.getExtensionMethods().get(clazz.getName());
        if (fromDGM != null) {
            for (MethodNode node : fromDGM) {
                if (node.getName().equals(name)) accumulator.add(node);
            }
        }
        for (ClassNode node : clazz.getInterfaces()) {
            findDGMMethodsForClassNode(node, name, accumulator);
        }
        if (clazz.isArray()) {
            ClassNode componentClass = clazz.getComponentType();
            if (!componentClass.equals(OBJECT_TYPE)) {
                if (componentClass.isInterface() || componentClass.getSuperClass()==null) {
                    findDGMMethodsForClassNode(OBJECT_TYPE.makeArray(), name, accumulator);
                } else {
                    findDGMMethodsForClassNode(componentClass.getSuperClass().makeArray(), name, accumulator);
                }
            }
        }
        if (clazz.getSuperClass() != null) {
            findDGMMethodsForClassNode(clazz.getSuperClass(), name, accumulator);
        } else if (!clazz.equals(ClassHelper.OBJECT_TYPE)) {
            findDGMMethodsForClassNode(ClassHelper.OBJECT_TYPE, name, accumulator);
        }
    }

    /**
     * Checks that arguments and parameter types match.
     * @param params method parameters
     * @param args type arguments
     * @return -1 if arguments do not match, 0 if arguments are of the exact type and >0 when one or more argument is
     * not of the exact type but still match
     */
    public static int allParametersAndArgumentsMatch(Parameter[] params, ClassNode[] args) {
        if (params==null) return args.length==0?0:-1;
        int dist = 0;
        // we already know the lengths are equal
        for (int i = 0; i < params.length; i++) {
            ClassNode paramType = params[i].getType();
            if (!isAssignableTo(args[i], paramType)) return -1;
            else {
                if (!paramType.equals(args[i])) dist+=getDistance(args[i], paramType);
            }
        }
        return dist;
    }

    /**
     * Checks that arguments and parameter types match, expecting that the number of parameters is strictly greater
     * than the number of arguments, allowing possible inclusion of default parameters.
     * @param params method parameters
     * @param args type arguments
     * @return -1 if arguments do not match, 0 if arguments are of the exact type and >0 when one or more argument is
     * not of the exact type but still match
     */
    static int allParametersAndArgumentsMatchWithDefaultParams(Parameter[] params, ClassNode[] args) {
        int dist = 0;
        ClassNode ptype = null;
        // we already know the lengths are equal
        for (int i = 0, j=0; i < params.length; i++) {
            Parameter param = params[i];
            ClassNode paramType = param.getType();
            ClassNode arg = j>=args.length?null:args[j];
            if (arg==null || !isAssignableTo(arg, paramType)){
                if (!param.hasInitialExpression() && (ptype==null || !ptype.equals(paramType))) {
                    return -1; // no default value
                }
                // a default value exists, we can skip this param
                ptype = null;
            } else {
                j++;
                if (!paramType.equals(arg)) dist+=getDistance(arg, paramType);
                if (param.hasInitialExpression()) {
                    ptype = arg;
                } else {
                    ptype = null;
                }
            }
        }
        return dist;
    }

    /**
     * Checks that excess arguments match the vararg signature parameter.
     * @param params
     * @param args
     * @return -1 if no match, 0 if all arguments matches the vararg type and >0 if one or more vararg argument is
     * assignable to the vararg type, but still not an exact match
     */
    static int excessArgumentsMatchesVargsParameter(Parameter[] params, ClassNode[] args) {
        // we already know parameter length is bigger zero and last is a vargs
        // the excess arguments are all put in an array for the vargs call
        // so check against the component type
        int dist = 0;
        ClassNode vargsBase = params[params.length - 1].getType().getComponentType();
        for (int i = params.length; i < args.length; i++) {
            if (!isAssignableTo(args[i],vargsBase)) return -1;
            else if (!args[i].equals(vargsBase)) dist++;
        }
        return dist;
    }

    /**
     * Checks if the last argument matches the vararg type.
     * @param params
     * @param args
     * @return -1 if no match, 0 if the last argument is exactly the vararg type and 1 if of an assignable type
     */
    static int lastArgMatchesVarg(Parameter[] params, ClassNode... args) {
        if (!isVargs(params)) return -1;
        // case length ==0 handled already
        // we have now two cases,
        // the argument is wrapped in the vargs array or
        // the argument is an array that can be used for the vargs part directly
        // we test only the wrapping part, since the non wrapping is done already
        ClassNode ptype = params[params.length - 1].getType().getComponentType();
        ClassNode arg = args[args.length - 1];
        if (isNumberType(ptype) && isNumberType(arg) && !ptype.equals(arg)) return -1;
        return isAssignableTo(arg, ptype)?(ptype.equals(arg)?0:1):-1;
    }

    /**
     * Checks if a class node is assignable to another. This is used for example in
     * assignment checks where you want to verify that the assignment is valid.
     * @param type
     * @param toBeAssignedTo
     * @return
     */
    static boolean isAssignableTo(ClassNode type, ClassNode toBeAssignedTo) {
        if (UNKNOWN_PARAMETER_TYPE==type) return true;
        if (toBeAssignedTo.redirect() == STRING_TYPE && type.redirect() == GSTRING_TYPE) {
            return true;
        }
        if (isPrimitiveType(toBeAssignedTo)) toBeAssignedTo = getWrapper(toBeAssignedTo);
        if (isPrimitiveType(type)) type = getWrapper(type);
        if (ClassHelper.Double_TYPE==toBeAssignedTo) {
            return type.isDerivedFrom(Number_TYPE);
        }
        if (ClassHelper.Float_TYPE==toBeAssignedTo) {
            return type.isDerivedFrom(Number_TYPE) && ClassHelper.Double_TYPE!=type;
        }
        if (ClassHelper.Long_TYPE==toBeAssignedTo) {
            return type.isDerivedFrom(Number_TYPE)
                    && ClassHelper.Double_TYPE!=type
                    && ClassHelper.Float_TYPE!=type;
        }
        if (ClassHelper.Integer_TYPE==toBeAssignedTo) {
            return type.isDerivedFrom(Number_TYPE)
                    && ClassHelper.Double_TYPE!=type
                    && ClassHelper.Float_TYPE!=type
                    && ClassHelper.Long_TYPE!=type;
        }
        if (ClassHelper.Short_TYPE==toBeAssignedTo) {
            return type.isDerivedFrom(Number_TYPE)
                    && ClassHelper.Double_TYPE!=type
                    && ClassHelper.Float_TYPE!=type
                    && ClassHelper.Long_TYPE!=type
                    && ClassHelper.Integer_TYPE!=type;
        }
        if (ClassHelper.Byte_TYPE==toBeAssignedTo) {
            return type == ClassHelper.Byte_TYPE;
        }
        if (type.isArray() && toBeAssignedTo.isArray()) {
            return isAssignableTo(type.getComponentType(),toBeAssignedTo.getComponentType());
        }
        if (implementsInterfaceOrIsSubclassOf(type, toBeAssignedTo)) {
            if (OBJECT_TYPE.equals(toBeAssignedTo)) return true;
            if (toBeAssignedTo.isUsingGenerics()) {
                // perform additional check on generics
                // ? extends toBeAssignedTo
                GenericsType gt = GenericsUtils.buildWildcardType(toBeAssignedTo);
                return gt.isCompatibleWith(type);
            }
            return true;
        } else {
            return false;
        }
    }

    static boolean isVargs(Parameter[] params) {
        if (params.length == 0) return false;
        if (params[params.length - 1].getType().isArray()) return true;
        return false;
    }

    static boolean isCompareToBoolean(int op) {
        return op == COMPARE_GREATER_THAN ||
                op == COMPARE_GREATER_THAN_EQUAL ||
                op == COMPARE_LESS_THAN ||
                op == COMPARE_LESS_THAN_EQUAL;
    }

    static boolean isArrayOp(int op) {
        return op == LEFT_SQUARE_BRACKET;
    }

    static boolean isBoolIntrinsicOp(int op) {
        return op == LOGICAL_AND || op == LOGICAL_OR ||
                op == MATCH_REGEX || op == KEYWORD_INSTANCEOF;
    }

    static boolean isPowerOperator(int op) {
        return op == POWER || op == POWER_EQUAL;
    }

    static String getOperationName(int op) {
        switch (op) {
            case COMPARE_EQUAL:
            case COMPARE_NOT_EQUAL:
                // this is only correct in this context here, normally
                // we would have to compile against compareTo if available
                // but since we don't compile here, this one is enough
                return "equals";

            case COMPARE_TO:
            case COMPARE_GREATER_THAN:
            case COMPARE_GREATER_THAN_EQUAL:
            case COMPARE_LESS_THAN:
            case COMPARE_LESS_THAN_EQUAL:
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

            default:
                return null;
        }
    }

    static boolean isShiftOperation(String name) {
        return "leftShift".equals(name) || "rightShift".equals(name) || "rightShiftUnsigned".equals(name);
    }

    /**
     * Returns true for operations that are of the class, that given a common type class for left and right, the
     * operation "left op right" will have a result in the same type class In Groovy on numbers that is +,-,* as well as
     * their variants with equals.
     */
    static boolean isOperationInGroup(int op) {
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

    static boolean isBitOperator(int op) {
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

    public static boolean isAssignment(int op) {
        switch (op) {
            case ASSIGN:
            case LOGICAL_OR_EQUAL:
            case LOGICAL_AND_EQUAL:
            case PLUS_EQUAL:
            case MINUS_EQUAL:
            case MULTIPLY_EQUAL:
            case DIVIDE_EQUAL:
            case INTDIV_EQUAL:
            case MOD_EQUAL:
            case POWER_EQUAL:
            case LEFT_SHIFT_EQUAL:
            case RIGHT_SHIFT_EQUAL:
            case RIGHT_SHIFT_UNSIGNED_EQUAL:
            case BITWISE_OR_EQUAL:
            case BITWISE_AND_EQUAL:
            case BITWISE_XOR_EQUAL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns true or false depending on whether the right classnode can be assigned to the left classnode. This method
     * should not add errors by itself: we let the caller decide what to do if an incompatible assignment is found.
     *
     * @param left  the class to be assigned to
     * @param right the assignee class
     * @return false if types are incompatible
     */
    public static boolean checkCompatibleAssignmentTypes(ClassNode left, ClassNode right) {
        return checkCompatibleAssignmentTypes(left, right, null);
    }

    public static boolean checkCompatibleAssignmentTypes(ClassNode left, ClassNode right, Expression rightExpression) {
        ClassNode leftRedirect = left.redirect();
        ClassNode rightRedirect = right.redirect();

        if (right==VOID_TYPE||right==void_WRAPPER_TYPE) {
            return left==VOID_TYPE||left==void_WRAPPER_TYPE;
        }

        if ((isNumberType(rightRedirect)||WideningCategories.isNumberCategory(rightRedirect))) {
           if (BigDecimal_TYPE==leftRedirect) {
               // any number can be assigned to a big decimal
               return true;
           }
            if (BigInteger_TYPE==leftRedirect) {
                return WideningCategories.isBigIntCategory(rightRedirect);
            }
        }

        // if rightExpression is null and leftExpression is not a primitive type, it's ok
        boolean rightExpressionIsNull = rightExpression instanceof ConstantExpression && ((ConstantExpression) rightExpression).getValue()==null;
        if (rightExpressionIsNull && !isPrimitiveType(left)) {
            return true;
        }

        // on an assignment everything that can be done by a GroovyCast is allowed

        // anything can be assigned to an Object, String, boolean, Boolean
        // or Class typed variable
        if (leftRedirect == OBJECT_TYPE ||
                leftRedirect == STRING_TYPE ||
                leftRedirect == boolean_TYPE ||
                leftRedirect == Boolean_TYPE ||
                leftRedirect == CLASS_Type) {
            return true;
        }

        // char as left expression
        if (leftRedirect == char_TYPE && rightRedirect==STRING_TYPE) {
            if (rightExpression!=null && rightExpression instanceof ConstantExpression) {
                String value = rightExpression.getText();
                return value.length()==1;
            }
        }
        if (leftRedirect == Character_TYPE && (rightRedirect==STRING_TYPE||rightExpressionIsNull)) {
            return rightExpressionIsNull || (rightExpression instanceof ConstantExpression && rightExpression.getText().length()==1);
        }

        // if left is Enum and right is String or GString we do valueOf
        if (leftRedirect.isDerivedFrom(Enum_Type) &&
                (rightRedirect == GSTRING_TYPE || rightRedirect == STRING_TYPE)) {
            return true;
        }

        // if right is array, map or collection we try invoking the
        // constructor
        if (rightRedirect.implementsInterface(MAP_TYPE) ||
                rightRedirect.implementsInterface(Collection_TYPE) ||
                rightRedirect.equals(MAP_TYPE) ||
                rightRedirect.equals(Collection_TYPE) ||
                rightRedirect.isArray()) {
            //TODO: in case of the array we could maybe make a partial check
            if (leftRedirect.isArray() && rightRedirect.isArray()) {
                return checkCompatibleAssignmentTypes(leftRedirect.getComponentType(), rightRedirect.getComponentType());
            } else if (rightRedirect.isArray() && !leftRedirect.isArray()) {
                return false;
            }
            return true;
        }

        // simple check on being subclass
        if (right.isDerivedFrom(left) || (left.isInterface() && right.implementsInterface(left))) return true;

        // if left and right are primitives or numbers allow
        if (isPrimitiveType(leftRedirect) && isPrimitiveType(rightRedirect)) return true;
        if (isNumberType(leftRedirect) && isNumberType(rightRedirect)) return true;

        // left is a float/double and right is a BigDecimal
        if (WideningCategories.isFloatingCategory(leftRedirect) && BigDecimal_TYPE.equals(rightRedirect)) {
            return true;
        }

        return false;
    }

    static boolean checkPossibleLooseOfPrecision(ClassNode left, ClassNode right, Expression rightExpr) {
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
                    return false; // no possible loose here
            }
        }
        return true; // possible loose of precision
    }

    static String toMethodParametersString(String methodName, ClassNode... parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(methodName).append("(");
        if (parameters != null) {
            for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
                final ClassNode parameter = parameters[i];
                sb.append(parameter.toString(false));
                if (i < parametersLength - 1) sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static boolean implementsInterfaceOrIsSubclassOf(ClassNode type, ClassNode superOrInterface) {
        boolean result = type.equals(superOrInterface)
                || type.isDerivedFrom(superOrInterface)
                || type.implementsInterface(superOrInterface)
                || type == UNKNOWN_PARAMETER_TYPE;
        if (result) {
            return true;
        }
        if (superOrInterface instanceof WideningCategories.LowestUpperBoundClassNode) {
            WideningCategories.LowestUpperBoundClassNode cn = (WideningCategories.LowestUpperBoundClassNode) superOrInterface;
            result = implementsInterfaceOrIsSubclassOf(type, cn.getSuperClass());
            if (result) {
                for (ClassNode interfaceNode : cn.getInterfaces()) {
                    result = type.implementsInterface(interfaceNode);
                    if (!result) break;
                }
            }
            if (result) return true;
        } else if (superOrInterface instanceof UnionTypeClassNode) {
            UnionTypeClassNode union = (UnionTypeClassNode) superOrInterface;
            for (ClassNode delegate : union.getDelegates()) {
                if (implementsInterfaceOrIsSubclassOf(type, delegate)) return true;
            }
        }
        if (type.isArray() && superOrInterface.isArray()) {
            return implementsInterfaceOrIsSubclassOf(type.getComponentType(), superOrInterface.getComponentType());
        }
        return false;
    }

    static int getDistance(final ClassNode receiver, final ClassNode compare) {
        int dist = 0;
        ClassNode unwrapReceiver = ClassHelper.getUnwrapper(receiver);
        ClassNode unwrapCompare = ClassHelper.getUnwrapper(compare);
        if (ClassHelper.isPrimitiveType(unwrapReceiver)
                && ClassHelper.isPrimitiveType(unwrapCompare)
                && unwrapReceiver!=unwrapCompare) {
            dist = 1;
        }
        ClassNode ref = compare;
        while (ref!=null) {
            if (receiver.equals(ref) || receiver == UNKNOWN_PARAMETER_TYPE) {
                break;
            }
            if (ref.isInterface() && receiver.implementsInterface(ref)) {
                dist += getMaximumInterfaceDistance(receiver, ref);
                break;
            }
            ref = ref.getSuperClass();
            if (ref == null) dist += 2;
            dist = (dist+1)<<1;
        }
        return dist;
    }

    private static int getMaximumInterfaceDistance(ClassNode c, ClassNode interfaceClass) {
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
            if (sub != -1) sub++;
            // we are interested in the longest path only
            max = Math.max(max, sub);
        }
        // we do not add one for super classes, only for interfaces
        int superClassMax = getMaximumInterfaceDistance(c.getSuperClass(), interfaceClass);
        return Math.max(max, superClassMax);
    }

    public static List<MethodNode> findDGMMethodsByNameAndArguments(final ClassNode receiver, final String name, final ClassNode[] args) {
        return findDGMMethodsByNameAndArguments(receiver, name, args, new LinkedList<MethodNode>());
    }

    public static List<MethodNode> findDGMMethodsByNameAndArguments(final ClassNode receiver, final String name, final ClassNode[] args, final List<MethodNode> methods) {
        final List<MethodNode> chosen;
        methods.addAll(findDGMMethodsForClassNode(receiver, name));

        chosen = chooseBestMethod(receiver, methods, args);
        // specifically for DGM-like methods, we may have a generic type as the first argument of the DGM method
        // for example: DGM#getAt(T[], int) or DGM#putAt(T[], int, U)
        // in that case, we must verify that the chosen method match generic type information
        Iterator<MethodNode> iterator = chosen.iterator();
        while (iterator.hasNext()) {
            ExtensionMethodNode emn = (ExtensionMethodNode) iterator.next();
            MethodNode dgmMethod = emn.getExtensionMethodNode(); // this is the method from DGM
            GenericsType[] methodGenericTypes = dgmMethod.getGenericsTypes();
            if (methodGenericTypes !=null && methodGenericTypes.length>0) {
                Parameter[] parameters = dgmMethod.getParameters();
                ClassNode dgmOwnerType = parameters[0].getOriginType();
                if (dgmOwnerType.isGenericsPlaceHolder() || dgmOwnerType.isArray() && dgmOwnerType.getComponentType().isGenericsPlaceHolder()) {
                    // first parameter of DGM method is a generic type or an array of generic type

                    ClassNode receiverBase = receiver.isArray() ? receiver.getComponentType() : receiver;
                    ClassNode receiverBaseRedirect = dgmOwnerType.isArray()?dgmOwnerType.getComponentType():dgmOwnerType;
                    boolean mismatch = false;
                    // ex: <T, U extends T> void putAt(T[], int, U)
                    for (int i = 1; i < parameters.length && !mismatch; i++) {
                        final int k = i - 1; // index of the actual parameter because of the extra receiver parameter in DGM
                        ClassNode type = parameters[i].getOriginType();
                        if (isUsingGenericsOrIsArrayUsingGenerics(type)) {
                            // in a DGM-like method, the first parameter is the receiver. Because of type erasure,
                            // it can only be T or T[]
                            String receiverPlaceholder = receiverBaseRedirect.getGenericsTypes()[0].getName();
                            ClassNode parameterBaseType = args[k].isArray() ? args[k].getComponentType() : args[k];
                            ClassNode parameterBaseTypeRedirect = type.isArray() ? type.getComponentType() : type;
                            GenericsType[] paramRedirectGenericsTypes = parameterBaseTypeRedirect.getGenericsTypes();
                            GenericsType[] paramGenericTypes = parameterBaseType.getGenericsTypes();
                            if (paramGenericTypes==null) {
                                paramGenericTypes = new GenericsType[paramRedirectGenericsTypes.length];
                                Arrays.fill(paramGenericTypes, new GenericsType(OBJECT_TYPE));
                            } else {
                                for (int j = 0; j < paramGenericTypes.length; j++) {
                                    GenericsType paramGenericType = paramGenericTypes[j];
                                    if (paramGenericType.isWildcard() || paramGenericType.isPlaceholder()) {
                                        // this may happen if an argument has been used without specifying a generic type
                                        // for example, foo(List) instead of foo(List<Object>)
                                        paramGenericTypes[j] = new GenericsType(OBJECT_TYPE);
                                    }
                                }
                            }
                            for (int j = 0, genericsTypesLength = paramRedirectGenericsTypes.length; j < genericsTypesLength && !mismatch; j++) {
                                final GenericsType gt = paramRedirectGenericsTypes[j];
                                if (gt.isPlaceholder()) {
                                    List<GenericsType> fromMethodGenerics = new LinkedList<GenericsType>();
                                    for (GenericsType methodGenericType : methodGenericTypes) {
                                        if (methodGenericType.getName().equals(gt.getName())) {
                                            fromMethodGenerics.add(methodGenericType);
                                            break;
                                        }
                                    }
                                    while (!fromMethodGenerics.isEmpty()) {
                                        // type must either be T or a derived type from T (ex: U extends T)
                                        GenericsType test = fromMethodGenerics.remove(0);
                                        if (test.getName().equals(receiverPlaceholder)) {
                                            if (!implementsInterfaceOrIsSubclassOf(getWrapper(args[k]), getWrapper(receiverBase))) {
                                                mismatch = true;
                                                break;
                                            }
                                        } else if (test.getUpperBounds()!=null) {
                                            for (ClassNode classNode : test.getUpperBounds()) {
                                                GenericsType[] genericsTypes = classNode.getGenericsTypes();
                                                if (genericsTypes!=null) {
                                                    for (GenericsType genericsType : genericsTypes) {
                                                        if (genericsType.isPlaceholder()) {
                                                            for (GenericsType methodGenericType : methodGenericTypes) {
                                                                if (methodGenericType.getName().equals(genericsType.getName())) {
                                                                    fromMethodGenerics.add(methodGenericType);
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (mismatch) {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        }
        return chosen;
    }

    /**
     * Given a list of candidate methods, returns the one which best matches the argument types
     *
     * @param receiver
     * @param methods candidate methods
     * @param args argument types
     * @return the list of methods which best matches the argument types. It is still possible that multiple
     * methods match the argument types.
     */
    public static List<MethodNode> chooseBestMethod(final ClassNode receiver, Collection<MethodNode> methods, ClassNode... args) {
        if (methods.isEmpty()) return Collections.emptyList();
        List<MethodNode> bestChoices = new LinkedList<MethodNode>();
        int bestDist = Integer.MAX_VALUE;
        ClassNode actualReceiver;
        Collection<MethodNode> choicesLeft = removeCovariants(methods);
        for (MethodNode m : choicesLeft) {
            final ClassNode declaringClass = m.getDeclaringClass();
            actualReceiver = receiver!=null?receiver: declaringClass;
            // todo : corner case
            /*
                class B extends A {}

                Animal foo(A o) {...}
                Person foo(B i){...}

                B  a = new B()
                Person p = foo(b)
             */

            Parameter[] params = parameterizeArguments(actualReceiver, m);
            if (params.length > args.length && ! isVargs(params)) {
                // GROOVY-5231
                int dist = allParametersAndArgumentsMatchWithDefaultParams(params, args);
                if (dist>=0 && !actualReceiver.equals(declaringClass)) dist+=getDistance(actualReceiver, declaringClass);
                if (dist>=0 && dist<bestDist) {
                    bestChoices.clear();
                    bestChoices.add(m);
                    bestDist = dist;
                } else if (dist>=0 && dist==bestDist) {
                    bestChoices.add(m);
                }
            } else if (params.length == args.length) {
                int allPMatch = allParametersAndArgumentsMatch(params, args);
                int lastArgMatch = isVargs(params)?lastArgMatchesVarg(params, args):-1;
                if (lastArgMatch>=0) lastArgMatch++; // ensure exact matches are preferred over vargs
                int dist = allPMatch>=0?Math.max(allPMatch, lastArgMatch):lastArgMatch;
                if (dist>=0 && !actualReceiver.equals(declaringClass)) dist+=getDistance(actualReceiver, declaringClass);
                if (dist>=0 && dist<bestDist) {
                    bestChoices.clear();
                    bestChoices.add(m);
                    bestDist = dist;
                } else if (dist>=0 && dist==bestDist) {
                    bestChoices.add(m);
                }
            } else if (isVargs(params)) {
                boolean firstParamMatches = true;
                // check first parameters
                if (args.length > 0) {
                    Parameter[] firstParams = new Parameter[params.length - 1];
                    System.arraycopy(params, 0, firstParams, 0, firstParams.length);
                    firstParamMatches = allParametersAndArgumentsMatch(firstParams, args) >= 0;
                }
                if (firstParamMatches) {
                    // there are three case for vargs
                    // (1) varg part is left out
                    if (params.length == args.length + 1) {
                        if (bestDist > 1) {
                            bestChoices.clear();
                            bestChoices.add(m);
                            bestDist = 1;
                        }
                    } else {
                        // (2) last argument is put in the vargs array
                        //      that case is handled above already
                        // (3) there is more than one argument for the vargs array
                        int dist = excessArgumentsMatchesVargsParameter(params, args);
                        if (dist >= 0 && !actualReceiver.equals(declaringClass)) dist++;
                        // varargs methods must not be preferred to methods without varargs
                        // for example :
                        // int sum(int x) should be preferred to int sum(int x, int... y)
                        dist++;
                        if (params.length < args.length && dist >= 0) {
                            if (dist >= 0 && dist < bestDist) {
                                bestChoices.clear();
                                bestChoices.add(m);
                                bestDist = dist;
                            } else if (dist >= 0 && dist == bestDist) {
                                bestChoices.add(m);
                            }
                        }
                    }
                }
            }
        }
        return bestChoices;
    }

    private static Collection<MethodNode> removeCovariants(Collection<MethodNode> collection) {
        if (collection.size()<=1) return collection;
        List<MethodNode> toBeRemoved = new LinkedList<MethodNode>();
        List<MethodNode> list = new LinkedList<MethodNode>(new HashSet<MethodNode>(collection));
        for (int i=0;i<list.size()-1;i++) {
            MethodNode one = list.get(i);
            if (toBeRemoved.contains(one)) continue;
            for (int j=i+1;j<list.size();j++) {
                MethodNode two = list.get(j);
                if (toBeRemoved.contains(two)) continue;
                if (one.getName().equals(two.getName()) && one.getDeclaringClass()==two.getDeclaringClass()) {
                    Parameter[] onePars = one.getParameters();
                    Parameter[] twoPars = two.getParameters();
                    if (onePars.length == twoPars.length) {
                        boolean sameTypes = true;
                        for (int k = 0; k < onePars.length; k++) {
                            Parameter onePar = onePars[k];
                            Parameter twoPar = twoPars[k];
                            if (!onePar.getType().equals(twoPar.getType())) {
                                sameTypes = false;
                                break;
                            }
                        }
                        if (sameTypes) {
                            ClassNode oneRT = one.getReturnType();
                            ClassNode twoRT = two.getReturnType();
                            if (oneRT.isDerivedFrom(twoRT) || oneRT.implementsInterface(twoRT)) {
                                toBeRemoved.add(two);
                            } else if (twoRT.isDerivedFrom(oneRT) || twoRT.implementsInterface(oneRT)) {
                                toBeRemoved.add(one);
                            }
                        }
                    }
                }                
            }
        }
        if (toBeRemoved.isEmpty()) return list;
        List<MethodNode> result = new LinkedList<MethodNode>(list);
        result.removeAll(toBeRemoved);
        return result;
    }
    
    /**
     * Given a receiver and a method node, parameterize the method arguments using
     * available generic type information.
     *
     * @param receiver the class
     * @param m the method
     * @return the parameterized arguments
     */
    public static Parameter[] parameterizeArguments(final ClassNode receiver, final MethodNode m) {
        MethodNode mn = m;
        ClassNode actualReceiver = receiver;
        /*if (m instanceof ExtensionMethodNode) {
            ExtensionMethodNode emn = (ExtensionMethodNode) m;
            mn = emn.getExtensionMethodNode();
            actualReceiver = emn.getDeclaringClass();
        }*/
        List<GenericsType> redirectTypes = new ArrayList<GenericsType>();
//        if (mn.getGenericsTypes()!=null) Collections.addAll(redirectTypes,mn.getGenericsTypes());
        if (actualReceiver.redirect().getGenericsTypes()!=null) {
            Collections.addAll(redirectTypes,actualReceiver.redirect().getGenericsTypes());
        }

        if (redirectTypes.isEmpty()) {
            return m.getParameters();
        }
        GenericsType[] redirectReceiverTypes = redirectTypes.toArray(new GenericsType[redirectTypes.size()]);

        Parameter[] methodParameters = mn.getParameters();
        Parameter[] params = new Parameter[methodParameters.length];
        GenericsType[] receiverParameterizedTypes = actualReceiver.getGenericsTypes();
        if (receiverParameterizedTypes==null) {
            receiverParameterizedTypes = redirectReceiverTypes;
        }
        for (int i = 0; i < methodParameters.length; i++) {
            Parameter methodParameter = methodParameters[i];
            ClassNode paramType = methodParameter.getType();
            if (paramType.isUsingGenerics()) {
                GenericsType[] alignmentTypes = paramType.getGenericsTypes();
                GenericsType[] genericsTypes = GenericsUtils.alignGenericTypes(redirectReceiverTypes, receiverParameterizedTypes, alignmentTypes);
                if (genericsTypes.length==1) {
                    ClassNode parameterizedCN;
                    if (paramType.equals(OBJECT_TYPE)) {
                        parameterizedCN = genericsTypes[0].getType();
                    } else {
                        parameterizedCN= paramType.getPlainNodeReference();
                        parameterizedCN.setGenericsTypes(genericsTypes);
                    }
                    params[i] = new Parameter(
                            parameterizedCN,
                            methodParameter.getName()
                    );
                } else {
                    params[i] = methodParameter;
                }
            } else {
                params[i] = methodParameter;
            }
        }
        /*if (m instanceof ExtensionMethodNode) {
            // the parameter array we're using is the one from the extension
            // but we want to return an array for the regular method
            Parameter[] result = new Parameter[params.length-1];
            // 0 is the receiver
            // 1..n are what we want to return
            System.arraycopy(params, 1, result, 0, result.length);
            return result;
        }*/
        return params;
    }

    static boolean isUsingGenericsOrIsArrayUsingGenerics(ClassNode cn) {
        return cn.isUsingGenerics() || cn.isArray() && cn.getComponentType().isUsingGenerics();
    }

    /**
     * A DGM-like method which adds support for method calls which are handled
     * specifically by the Groovy compiler.
     */
    private static class ObjectArrayStaticTypesHelper {
        public static <T> T getAt(T[] arr, int index) { return null;} 
        public static <T,U extends T> void putAt(T[] arr, int index, U object) { }
    }

    /**
     * This class is used to make extension methods lookup faster. Basically, it will only
     * collect the list of extension methods (see {@link ExtensionModule} if the list of
     * extension modules has changed. It avoids recomputing the whole list each time we perform
     * a method lookup.
     */
    private static class ExtensionMethodCache {

        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private List<ExtensionModule> modules = Collections.emptyList();
        private Map<String, List<MethodNode>> cachedMethods = null;

        public Map<String, List<MethodNode>> getExtensionMethods() {
            lock.readLock().lock();
            MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
            if (registry instanceof MetaClassRegistryImpl) {
                MetaClassRegistryImpl impl = (MetaClassRegistryImpl) registry;
                ExtensionModuleRegistry moduleRegistry = impl.getModuleRegistry();
                if (!modules.equals(moduleRegistry.getModules())) {
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                    try {
                        if (!modules.equals(moduleRegistry.getModules())) {
                            modules = moduleRegistry.getModules();
                            cachedMethods = getDGMMethods(registry);
                        }
                    } finally {
                        lock.writeLock().unlock();
                        lock.readLock().lock();
                    }
                } else if (cachedMethods==null) {
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                    try {
                        cachedMethods = getDGMMethods(registry);
                    } finally {
                        lock.writeLock().unlock();
                        lock.readLock().lock();
                    }
                }
            }
            try {
                return Collections.unmodifiableMap(cachedMethods);
            } finally {
                lock.readLock().unlock();
            }
        }

        /**
         * Returns a map which contains, as the key, the name of a class. The value
         * consists of a list of MethodNode, one for each default groovy method found
         * which is applicable for this class.
         * @return
         * @param registry
         */
        private static Map<String, List<MethodNode>> getDGMMethods(final MetaClassRegistry registry) {
           Set<Class> instanceExtClasses = new LinkedHashSet<Class>();
           Set<Class> staticExtClasses = new LinkedHashSet<Class>();
            if (registry instanceof MetaClassRegistryImpl) {
                MetaClassRegistryImpl impl = (MetaClassRegistryImpl) registry;
                List<ExtensionModule> modules = impl.getModuleRegistry().getModules();
                for (ExtensionModule module : modules) {
                    if (module instanceof MetaInfExtensionModule) {
                        MetaInfExtensionModule extensionModule = (MetaInfExtensionModule) module;
                        instanceExtClasses.addAll(extensionModule.getInstanceMethodsExtensionClasses());
                        staticExtClasses.addAll(extensionModule.getStaticMethodsExtensionClasses());
                    }
                }
            }
            Map<String, List<MethodNode>> methods = new HashMap<String, List<MethodNode>>();
            Collections.addAll(instanceExtClasses, DefaultGroovyMethods.DGM_LIKE_CLASSES);
            Collections.addAll(instanceExtClasses, DefaultGroovyMethods.additionals);
            staticExtClasses.add(DefaultGroovyStaticMethods.class);
            instanceExtClasses.add(ObjectArrayStaticTypesHelper.class);
            List<Class> allClasses = new ArrayList<Class>(instanceExtClasses.size()+staticExtClasses.size());
            allClasses.addAll(instanceExtClasses);
            allClasses.addAll(staticExtClasses);
            for (Class dgmLikeClass : allClasses) {
                ClassNode cn = ClassHelper.makeWithoutCaching(dgmLikeClass, true);
                for (MethodNode metaMethod : cn.getMethods()) {
                    Parameter[] types = metaMethod.getParameters();
                    if (metaMethod.isStatic() && metaMethod.isPublic() && types.length > 0
                            && metaMethod.getAnnotations(Deprecated_TYPE).isEmpty()) {
                        Parameter[] parameters = new Parameter[types.length - 1];
                        System.arraycopy(types, 1, parameters, 0, parameters.length);
                        MethodNode node = new ExtensionMethodNode(
                                metaMethod,
                                metaMethod.getName(),
                                metaMethod.getModifiers(),
                                metaMethod.getReturnType(),
                                parameters,
                                ClassNode.EMPTY_ARRAY, null);
                        if (staticExtClasses.contains(dgmLikeClass)) {
                            node.setModifiers(node.getModifiers() | Opcodes.ACC_STATIC);
                        }
                        node.setGenericsTypes(metaMethod.getGenericsTypes());
                        ClassNode declaringClass = types[0].getType();
                        String declaringClassName = declaringClass.getName();
                        node.setDeclaringClass(declaringClass);

                        List<MethodNode> nodes = methods.get(declaringClassName);
                        if (nodes == null) {
                            nodes = new LinkedList<MethodNode>();
                            methods.put(declaringClassName, nodes);
                        }
                        nodes.add(node);
                    }
                }
            }
            return methods;
        }

    }
}

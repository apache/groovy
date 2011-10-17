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
package org.codehaus.groovy.transform;

import java.util.*;
import java.util.regex.Matcher;

import groovy.lang.MetaMethod;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.classgen.ReturnAdder;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

import static org.codehaus.groovy.ast.ClassHelper.*;
import static org.codehaus.groovy.syntax.Types.*;
import static org.codehaus.groovy.ast.tools.WideningCategories.*;

/**
 * Handles the implementation of the {@link groovy.transform.StaticTypes} transformation.
 *
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 * @author Cedric Champeau
 * @author Guillaume Laforge
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class StaticTypesTransformation implements ASTTransformation {

    private static final String STATIC_ERROR_PREFIX = "[Static type checking] - ";

//    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
//        AnnotationNode annotationInformation = (AnnotationNode) nodes[0];
        AnnotatedNode node = (AnnotatedNode) nodes[1];
        if (node instanceof ClassNode) {
            Visitor visitor = new Visitor(source, (ClassNode) node);
            visitor.visitClass((ClassNode) node);
        } else if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode)node;
            Visitor visitor = new Visitor(source, methodNode.getDeclaringClass());
            visitor.visitMethod(methodNode);
        } else {
            source.addError(new SyntaxException(STATIC_ERROR_PREFIX + "Unimplemented node type", node.getLineNumber(), node.getColumnNumber()));
        }
    }

    public final static class StaticTypesMarker{}

    private static class Visitor extends ClassCodeVisitorSupport {
        private final static Map<String,List<MethodNode>> VIRTUAL_DGM_METHODS = getDGMMethods();
        private final static ClassNode
            Collection_TYPE = makeWithoutCaching(Collection.class),
            Number_TYPE     = makeWithoutCaching(Number.class),
            Matcher_TYPE    = makeWithoutCaching(Matcher.class),
            ArrayList_TYPE  = makeWithoutCaching(ArrayList.class);

        private SourceUnit source;
        private ClassNode classNode;
        private MethodNode methodNode;

        private final ReturnAdder returnAdder = new ReturnAdder(new ReturnAdder.ReturnStatementListener() {
            public void returnStatementAdded(final ReturnStatement returnStatement) {
                checkReturnType(returnStatement);
            }
        });

        public Visitor(SourceUnit source, ClassNode cn){
            this.source = source;
            this.classNode = cn;
        }

//        @Override
        protected SourceUnit getSourceUnit() {
            return source;
        }

        @Override
        public void visitVariableExpression(VariableExpression vexp) {
            super.visitVariableExpression(vexp);
            if (    vexp!=VariableExpression.THIS_EXPRESSION &&
                    vexp!=VariableExpression.SUPER_EXPRESSION)
            {
                if (vexp.getName().equals("this")) storeType(vexp, classNode);
                if (vexp.getName().equals("super")) storeType(vexp, classNode.getSuperClass());
            }
        }

        @Override
        public void visitBinaryExpression(BinaryExpression expression) {
            super.visitBinaryExpression(expression);
            ClassNode lType = getType(expression.getLeftExpression(), classNode);
            ClassNode rType = getType(expression.getRightExpression(), classNode);
            int op = expression.getOperation().getType();
            ClassNode resultType = getResultType(lType, op, rType, expression);
            if (resultType==null) {
                addStaticTypeError("tbd...", expression);
                resultType = lType;
            }
            // todo : if assignment of a primitive to an object (def, Object, whatever),
            // the type inference engine should return an Object (not a primitive type)
            storeType(expression, resultType);
            if (isAssignment(op)) {
                ClassNode leftRedirect = expression.getLeftExpression().getType().redirect();
                final boolean compatible = checkCompatibleAssignmentTypes(leftRedirect, resultType);
                if (!compatible) {
                    addStaticTypeError("Cannot assign value of type " + resultType + " to variable of type " + leftRedirect, expression);
                }
                storeType(expression.getLeftExpression(), resultType);
            }
        }

        @Override
        public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
            super.visitBitwiseNegationExpression(expression);
            ClassNode type = getType(expression, classNode);
            ClassNode typeRe = type.redirect();
            ClassNode resultType;
            if (isBigIntCategory(typeRe)) {
                // allow any internal number that is not a floating point one
                resultType = type;
            } else if (typeRe==STRING_TYPE  || typeRe==GSTRING_TYPE) {
                resultType = PATTERN_TYPE;
            } else if (typeRe==ArrayList_TYPE) {
                resultType = ArrayList_TYPE;
            } else {
                MethodNode mn = findMethodOrFail(expression, type, "bitwiseNegate");
                resultType = mn.getReturnType();
            }
            storeType(expression, resultType);
        }

        @Override
        public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
            super.visitUnaryPlusExpression(expression);
            negativeOrPositiveUnary(expression, "positive");
        }

        @Override
        public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
            super.visitUnaryMinusExpression(expression);
            negativeOrPositiveUnary(expression, "negative");
        }

        private void negativeOrPositiveUnary(Expression expression, String name) {
            ClassNode type = getType(expression, classNode);
            ClassNode typeRe = type.redirect();
            ClassNode resultType;
            if (isBigDecCategory(typeRe)) {
                resultType = type;
            } else if (typeRe==ArrayList_TYPE) {
                resultType = ArrayList_TYPE;
            } else {
                MethodNode mn = findMethodOrFail(expression, type, name);
                resultType = mn.getReturnType();
            }
            storeType(expression, resultType);
        }

        @Override
        protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
            this.methodNode = node;
            super.visitConstructorOrMethod(node, isConstructor);
            if (!isConstructor) returnAdder.visitMethod(node);
            this.methodNode = null;
        }

        @Override
        public void visitReturnStatement(ReturnStatement statement) {
            super.visitReturnStatement(statement);
            checkReturnType(statement);
        }

        private void checkReturnType(final ReturnStatement statement) {
            ClassNode type = getType(statement.getExpression(), classNode);
            if (methodNode != null) {
                if (!checkCompatibleAssignmentTypes(methodNode.getReturnType(), type)) {
                    addStaticTypeError("Cannot return value of type " + type + " on method returning type " + methodNode.getReturnType(), statement.getExpression());
                }
            }
        }

        @Override
        public void visitConstructorCallExpression(ConstructorCallExpression call) {
            super.visitConstructorCallExpression(call);
            ClassNode receiver = call.getType();
            ClassNode[] args = getArgumentTypes(InvocationWriter.makeArgumentList(call.getArguments()), receiver);
            findMethodOrFail(call, receiver, "<init>", args);
        }

        private static ClassNode[] getArgumentTypes(ArgumentListExpression args, ClassNode current) {
            List<Expression> arglist = args.getExpressions();
            ClassNode[] ret = new ClassNode[arglist.size()];
            int i=0;
            for (Expression exp : arglist) {
                ret[i] = getType(exp, current);
                i++;
            }
            return ret;
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            super.visitMethodCallExpression(call);
            if (call.getMethodAsString()==null) {
                addStaticTypeError("cannot resolve dynamic method name at compile time.", call.getMethod());
            } else {
                ClassNode[] args = getArgumentTypes(InvocationWriter.makeArgumentList(call.getArguments()), classNode);
                MethodNode mn = findMethodOrFail(call, getType(call.getObjectExpression(), classNode), call.getMethodAsString(), args);
                if (mn==null) return;
                storeType(call, mn.getReturnType());
            }
        }

        private void storeType(Expression exp, ClassNode cn) {
            exp.putNodeMetaData(StaticTypesMarker.class, cn);
            if (exp instanceof VariableExpression) {
                final Variable accessedVariable = ((VariableExpression) exp).getAccessedVariable();
                if (accessedVariable!=null && accessedVariable!=exp && accessedVariable instanceof VariableExpression) {
                    storeType((Expression)accessedVariable, cn);
                }
            }
        }

        private ClassNode getResultType(ClassNode left, int op, ClassNode right, BinaryExpression expr) {
            ClassNode leftRedirect = left.redirect();
            ClassNode rightRedirect = right.redirect();

            if (op==ASSIGN) {
                return rightRedirect;
            } else if (isBoolIntrinsicOp(op)) {
                return boolean_TYPE;
            } else if (isArrayOp(op)) {
                //TODO: do getAt and setAt
            } else if (op==FIND_REGEX) {
                // this case always succeeds the result is a Matcher
                return Matcher_TYPE;
            }
            // the left operand is determining the result of the operation
            // for primitives and their wrapper we use a fixed table here
            else if (isNumberType(leftRedirect) && isNumberType(rightRedirect)) {
                if (isOperationInGroup(op)) {
                    if (isIntCategory(leftRedirect)    && isIntCategory(rightRedirect))       return int_TYPE;
                    if (isLongCategory(leftRedirect)   && isLongCategory(rightRedirect))      return Long_TYPE;
                    if (isBigIntCategory(leftRedirect) && isBigIntCategory(rightRedirect))    return BigInteger_TYPE;
                    if (isBigDecCategory(leftRedirect) && isBigDecCategory(rightRedirect))    return BigDecimal_TYPE;
                    if (isDoubleCategory(leftRedirect) && isDoubleCategory(rightRedirect))    return double_TYPE;
                } else if (isPowerOperator(op)) {
                    return Number_TYPE;
                } else if (isBitOperator(op)) {
                    if (isIntCategory(leftRedirect)    && isIntCategory(rightRedirect))       return int_TYPE;
                    if (isLongCategory(leftRedirect)   && isLongCategory(rightRedirect))      return Long_TYPE;
                    if (isBigIntCategory(leftRedirect) && isBigIntCategory(rightRedirect))    return BigInteger_TYPE;
                }
            }


            // try to find a method for the operation
            String operationName = getOperationName(op);
            MethodNode method = findMethodOrFail(expr, leftRedirect, operationName, rightRedirect);
            if (method!=null) {
                if (isCompareToBoolean(op)) return boolean_TYPE;
                if (op==COMPARE_TO)         return int_TYPE;
                return method.getReturnType();
            }
            //TODO: other cases
            return null;
        }

        private static Map<String,List<MethodNode>> getDGMMethods() {
            Map<String, List<MethodNode>> methods = new HashMap<String, List<MethodNode>>();
           CachedClass cachedClass = ReflectionCache.getCachedClass(DefaultGroovyMethods.class);
            for (MetaMethod metaMethod : cachedClass.getMethods()) {
                CachedClass[] types = metaMethod.getParameterTypes();
                if (metaMethod.isStatic() && metaMethod.isPublic() && types.length>0) {
                Parameter[] parameters = new Parameter[types.length-1];
                for (int i = 1; i < types.length; i++) {
                    CachedClass type = types[i];
                    parameters[i-1] = new Parameter(ClassHelper.make(type.getTheClass()), "p"+i);
                }
                MethodNode node = new MethodNode(
                        metaMethod.getName(),
                        metaMethod.getModifiers(),
                        ClassHelper.make(metaMethod.getReturnType()),
                        parameters,
                        ClassNode.EMPTY_ARRAY, null);

                    String name = types[0].getName();
                    List<MethodNode> nodes = methods.get(name);
                    if (nodes==null) {
                        nodes = new LinkedList<MethodNode>();
                        methods.put(name, nodes);
                    }
                    nodes.add(node);
                }
            }
            return methods;
        }

        private static Set<MethodNode> findDGMMethodsForClassNode(ClassNode clazz) {
            Set<MethodNode> result = new HashSet<MethodNode>();
            List<MethodNode> fromDGM = VIRTUAL_DGM_METHODS.get(clazz.getName());
            if (fromDGM!=null) result.addAll(fromDGM);
            for (ClassNode node : clazz.getInterfaces()) {
                result.addAll(findDGMMethodsForClassNode(node));
            }
            if (clazz.getSuperClass()!=null) {
                result.addAll(findDGMMethodsForClassNode(clazz.getSuperClass()));
            } else if (!clazz.equals(ClassHelper.OBJECT_TYPE)) {
                result.addAll(findDGMMethodsForClassNode(ClassHelper.OBJECT_TYPE));
            }

            return result;
        }

        private MethodNode findMethodOrFail(
                Expression expr,
                ClassNode receiver, String name, ClassNode... args)
        {
            List<MethodNode> methods;
            if ("<init>".equals(name)) {
                methods = new ArrayList<MethodNode>(receiver.getDeclaredConstructors());
                if (methods.isEmpty()) {
                    return new MethodNode("<init>", Opcodes.ACC_PUBLIC, receiver, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
                }
            } else {
                methods = receiver.getMethods(name);
            }
            Set<MethodNode> fromDGM = findDGMMethodsForClassNode(receiver);

            for (MethodNode methodNode : fromDGM) {
                if (methodNode.getName().equals(name)) methods.add(methodNode);
            }

            for (MethodNode m : methods) {
                // we return the first method that may match
                // we don't need the exact match here for now

                // todo : corner case
                /*
                    class B extends A {}

                    Animal foo(A o) {...}
                    Person foo(B i){...}

                    B  a = new B()
                    Person p = foo(b)
                 */

                Parameter[] params = m.getParameters();
                if (params.length == args.length) {
                    if (    allParametersAndArgumentsMatch(params,args) ||
                            lastArgMatchesVarg(params,args))
                    {
                        return m;
                    }
                } else if (isVargs(params)) {
                    // there are three case for vargs
                    // (1) varg part is left out
                    if (params.length == args.length+1) return m;
                    // (2) last argument is put in the vargs array
                    //      that case is handled above already
                    // (3) there is more than one argument for the vargs array
                    if (    params.length < args.length &&
                            excessArgumentsMatchesVargsParameter(params,args))
                    {
                        return m;
                    }
                }
            }
            if (receiver==ClassHelper.GSTRING_TYPE) return findMethodOrFail(expr, ClassHelper.STRING_TYPE, name, args);
            addStaticTypeError("Cannot find matching method "+receiver.getName()+"#" + toMethodParametersString(name, args), expr);
            return null;
        }

        private static boolean allParametersAndArgumentsMatch(Parameter[] params, ClassNode[] args) {
            // we already know the lengths are equal
            for (int i=0; i<params.length; i++) {
                if (!isAssignableTo(params[i].getType(), args[i])) return false;
            }
            return true;
        }

        private static boolean excessArgumentsMatchesVargsParameter(Parameter[] params, ClassNode[] args) {
            // we already know parameter length is bigger zero and last is a vargs
            // the excess arguments are all put in an array for the vargs call
            // so check against the component type
            ClassNode vargsBase = params[params.length-1].getType().getComponentType();
            for (int i=params.length; i<args.length; i++) {
                if (!isAssignableTo(vargsBase, args[i])) return false;
            }
            return true;
        }

        private static boolean lastArgMatchesVarg(Parameter[] params, ClassNode... args) {
            if (!isVargs(params)) return false;
            // case length ==0 handled already
            // we have now two cases, 
            // the argument is wrapped in the vargs array or
            // the argument is an array that can be used for the vargs part directly
            // we test only the wrapping part, since the non wrapping is done already
            ClassNode ptype=params[params.length-1].getType().getComponentType();
            ClassNode arg = args[args.length-1];
            return isAssignableTo(ptype,arg);
        }

        private static boolean isAssignableTo(ClassNode type, ClassNode toBeAssignedTo) {
            if (toBeAssignedTo.isDerivedFrom(type)) return true;
            if (toBeAssignedTo.redirect()==STRING_TYPE && type.redirect()==GSTRING_TYPE) {
                return true;
            }
            toBeAssignedTo = getWrapper(toBeAssignedTo);
            type = getWrapper(type);
            if (    toBeAssignedTo.isDerivedFrom(Number_TYPE) &&
                    type.isDerivedFrom(Number_TYPE))
            {
                return true;
            }
            if (toBeAssignedTo.isInterface() && type.implementsInterface(toBeAssignedTo)) {
                return true;
            }
            return false;
        }

        private static boolean isVargs(Parameter[] params) {
            if (params.length==0) return false;
            if (params[params.length-1].getType().isArray()) return true;
            return false;
        }

        private static boolean isCompareToBoolean(int op) {
            return  op==COMPARE_GREATER_THAN        ||
                    op==COMPARE_GREATER_THAN_EQUAL  ||
                    op==COMPARE_LESS_THAN           ||
                    op==COMPARE_LESS_THAN_EQUAL;
        }

        private static boolean isArrayOp(int op) {
            return  op==LEFT_SQUARE_BRACKET;
        }

        private static boolean isBoolIntrinsicOp(int op) {
            return  op==LOGICAL_AND     || op==LOGICAL_OR       ||
                    op==MATCH_REGEX     || op==KEYWORD_INSTANCEOF;
        }

        private static boolean isPowerOperator(int op) {
            return  op==POWER           ||  op==POWER_EQUAL;
        }

        /**
         * Returns true for operations that are of the class, that given a
         * common type class for left and right, the operation "left op right"
         * will have a result in the same type class
         * In Groovy on numbers that is +,-,* as well as their variants
         * with equals.
         */
        private static boolean isOperationInGroup(int op) {
            switch (op) {
                case PLUS:      case PLUS_EQUAL:
                case MINUS:     case MINUS_EQUAL:
                case MULTIPLY:  case MULTIPLY_EQUAL:
                    return true;
                default:
                    return false;
            }
        }

        private static boolean isBitOperator(int op) {
            switch (op) {
                case BITWISE_OR_EQUAL:   case BITWISE_OR:
                case BITWISE_AND_EQUAL:   case BITWISE_AND:
                case BITWISE_XOR_EQUAL:   case BITWISE_XOR:
                    return true;
                default:
                    return false;
            }
        }

        private static boolean isAssignment(int op) {
            switch (op) {
                case ASSIGN:
                case LOGICAL_OR_EQUAL:    case LOGICAL_AND_EQUAL:
                case PLUS_EQUAL:          case MINUS_EQUAL:
                case MULTIPLY_EQUAL:      case DIVIDE_EQUAL:
                case INTDIV_EQUAL:        case MOD_EQUAL:
                case POWER_EQUAL:
                case LEFT_SHIFT_EQUAL:    case RIGHT_SHIFT_EQUAL:
                case RIGHT_SHIFT_UNSIGNED_EQUAL:
                case BITWISE_OR_EQUAL:    case BITWISE_AND_EQUAL:
                case BITWISE_XOR_EQUAL:
                    return true;
                default: return false;
            }
        }

        private static ClassNode getType(Expression exp, ClassNode current) {
            ClassNode cn = (ClassNode) exp.getNodeMetaData(StaticTypesMarker.class);
            if (cn!=null) return cn;
            if (exp instanceof VariableExpression){
                VariableExpression vexp = (VariableExpression) exp;
                if (vexp==VariableExpression.THIS_EXPRESSION) return current;
                if (vexp==VariableExpression.SUPER_EXPRESSION) return current.getSuperClass();
                final Variable variable = vexp.getAccessedVariable();
                if (variable!=null && variable !=vexp && variable instanceof VariableExpression) {
                    return getType((Expression) variable, current);
                }
            } else if (exp instanceof PropertyExpression) {
                PropertyExpression pexp = (PropertyExpression) exp;
                if (pexp.getObjectExpression().getType().isEnum()) return pexp.getObjectExpression().getType();
            }
            return exp.getType();
        }

        /**
         * Returns true or false depending on whether the right classnode can be assigned to the left classnode.
         * This method should not add errors by itself: we let the caller decide what to do if an incompatible
         * assignment is found.
         *
         * @param left the class to be assigned to
         * @param right the assignee class
         * @return false if types are incompatible
         */
        private static boolean checkCompatibleAssignmentTypes(ClassNode left, ClassNode right) {
            ClassNode leftRedirect = left.redirect();
            ClassNode rightRedirect = right.redirect();
            // on an assignment everything that can be done by a GroovyCast is allowed

            // anything can be assigned to an Object, String, boolean, Boolean
            // or Class typed variable
            if  (   leftRedirect==OBJECT_TYPE   ||
                    leftRedirect==STRING_TYPE   ||
                    leftRedirect==boolean_TYPE  ||
                    leftRedirect==Boolean_TYPE  ||
                    leftRedirect==CLASS_Type) {
                return true;
            }

            // if left is Enum and right is String or GString we do valueOf
            if (    leftRedirect.isDerivedFrom(Enum_Type) &&
                    (rightRedirect==GSTRING_TYPE || rightRedirect==STRING_TYPE)) {
                return true;
            }

            // if right is array, map or collection we try invoking the 
            // constructor
            if (    rightRedirect.implementsInterface(MAP_TYPE)         ||
                    rightRedirect.implementsInterface(Collection_TYPE)  ||
                    rightRedirect.isArray()) {
                //TODO: in case of the array we could maybe make a partial check
                return true;
            }

            // simple check on being subclass
            if (right.isDerivedFrom(left) || left.implementsInterface(right)) return true;

            // if left and right are primitives or numbers allow 
            if (isPrimitiveType(leftRedirect) && isPrimitiveType(rightRedirect)) return true;
            if (isNumberType(leftRedirect) && isNumberType(rightRedirect)) return true;

            return false;
        }

        protected void addStaticTypeError(final String msg, final ASTNode expr) {
            if (expr.getColumnNumber()>0 && expr.getLineNumber()>0) {
                addError(STATIC_ERROR_PREFIX + msg, expr);
            } else {
                // ignore errors which are related to unknown source locations
                // because they are likely related to generated code
            }
        }

        private static String toMethodParametersString(String methodName, ClassNode... parameters) {
            StringBuilder sb = new StringBuilder();
            sb.append(methodName).append("(");
            if (parameters!=null) {
                for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
                    final ClassNode parameter = parameters[i];
                    sb.append(parameter.getName());
                    if (i<parametersLength-1) sb.append(", ");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    private static String getOperationName(int op) {
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

}

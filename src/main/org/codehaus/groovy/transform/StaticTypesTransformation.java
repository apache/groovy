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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.control.*;

import static org.codehaus.groovy.ast.ClassHelper.*;
import static org.codehaus.groovy.syntax.Types.*;

/**
 * Handles the implementation of the @StaticTypes transformation
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class StaticTypesTransformation implements ASTTransformation {

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
//        AnnotationNode annotationInformation = (AnnotationNode) nodes[0];
        AnnotatedNode node = (AnnotatedNode) nodes[1];
        Visitor visitor = new Visitor(source, (ClassNode) node);
        if (node instanceof ClassNode) {
            visitor.visitClass((ClassNode) node);
        } else {
            node.visit(visitor);
        }
    }
    
    public final static class StaticTypesMarker{}
    
    private static class Visitor extends ClassCodeVisitorSupport {
        private final static ClassNode 
            Collection_TYPE = makeWithoutCaching(Collection.class),
            Number_TYPE     = makeWithoutCaching(Number.class),
            Matcher_TYPE    = makeWithoutCaching(Matcher.class),
            ArrayList_TYPE  = makeWithoutCaching(ArrayList.class);
        
        private SourceUnit source;
        private ClassNode classNode;
        
        public Visitor(SourceUnit source, ClassNode cn){
            this.source = source;
            this.classNode = cn;
        }
        
        @Override
        protected SourceUnit getSourceUnit() {
            return source;
        }
        
        @Override
        public void visitBinaryExpression(BinaryExpression expression) {
            super.visitBinaryExpression(expression);
            ClassNode lType = getType(expression.getLeftExpression());
            ClassNode rType = getType(expression.getRightExpression());
            int op = expression.getOperation().getType();
            ClassNode resultType = getResultType(lType, op, rType, expression);
            if (resultType==null) {
                addError("tbd...", expression);
                resultType = lType;
            } 
            storeType(expression, resultType);
            if (!isAssignment(op)) return;
            checkCompatibleAssignmenTypes(lType,resultType,expression);
        }
        
        @Override
        public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
            super.visitBitwiseNegationExpression(expression);
            ClassNode type = getType(expression);
            ClassNode typeRe = type.redirect();
            ClassNode resultType;
            if (isClassBigInt(typeRe)) {
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
            ClassNode type = getType(expression);
            ClassNode typeRe = type.redirect();
            ClassNode resultType;
            if (isClassBigDec(typeRe)) {
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
        public void visitConstructorCallExpression(ConstructorCallExpression call) {
            super.visitConstructorCallExpression(call);
            ClassNode[] args = getArgumentTypes(InvocationWriter.makeArgumentList(call.getArguments()));
            findMethodOrFail(call, classNode, "init", args);
        }
        
        private static ClassNode[] getArgumentTypes(ArgumentListExpression args) {
            List<Expression> arglist = args.getExpressions();
            ClassNode[] ret = new ClassNode[arglist.size()];
            int i=0;
            for (Expression exp : arglist) {
                ret[i] = getType(exp);
                i++;
            }
            return ret;
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            super.visitMethodCallExpression(call);
            if (call.getMethodAsString()==null) {
                addError("cannot resolve dynamic method name at compile time.", call.getMethod());
            } else {
                ClassNode[] args = getArgumentTypes(InvocationWriter.makeArgumentList(call.getArguments()));
                MethodNode mn = findMethodOrFail(call, classNode, call.getMethodAsString(), args);
                if (mn==null) return;
                storeType(call, mn.getReturnType());
            }
        }
        
        private void storeType(Expression exp, ClassNode cn) {
            exp.setNodeMetaData(StaticTypesMarker.class, cn);
        }

        private ClassNode getResultType(ClassNode left, int op, ClassNode right, BinaryExpression expr) {
            ClassNode leftRedirect = left.redirect();
            ClassNode rightRedirect = right.redirect();

            if (op==ASSIGN) {
                checkCompatibleAssignmenTypes(leftRedirect,rightRedirect,expr);
                // we don't check the other assignments here, because 
                // we need first to check the operation itself.
                // assignment return type is lhs type
                return left;
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
                    if (isClassInt(leftRedirect)    && isClassInt(rightRedirect))       return int_TYPE;
                    if (isClassLong(leftRedirect)   && isClassLong(rightRedirect))      return Long_TYPE;
                    if (isClassBigInt(leftRedirect) && isClassBigInt(rightRedirect))    return BigInteger_TYPE;
                    if (isClassBigDec(leftRedirect) && isClassBigDec(rightRedirect))    return BigDecimal_TYPE;
                    if (isClassDouble(leftRedirect) && isClassDouble(rightRedirect))    return double_TYPE;
                } else if (isPowerOperator(op)) {
                    return Number_TYPE;
                } else if (isBitOperator(op)) {
                    if (isClassInt(leftRedirect)    && isClassInt(rightRedirect))       return int_TYPE;
                    if (isClassLong(leftRedirect)   && isClassLong(rightRedirect))      return Long_TYPE;
                    if (isClassBigInt(leftRedirect) && isClassBigInt(rightRedirect))    return BigInteger_TYPE;
                }  
            }

            
            // try to find a method for the operation
            String operationName = getOperationName(op);
            MethodNode method = findMethodOrFail(expr, leftRedirect, operationName, leftRedirect, rightRedirect);
            if (method!=null) {
                if (isCompareToBoolean(op)) return boolean_TYPE;
                if (op==COMPARE_TO)         return int_TYPE;
                return method.getReturnType();
            }
            //TODO: other cases
            return null;
        }
        
        private MethodNode findMethodOrFail(
                Expression expr,
                ClassNode receiver, String name, ClassNode... args) 
        {
            List<MethodNode> methods = receiver.getMethods(name);
            for (MethodNode m : methods) {
                // we return the first method that may match
                // we don't need the exact match here for now
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
            addError("Cannot find matching method "+name,expr);
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
        
        private static boolean isAssignableTo(ClassNode toBeAssignedTo, ClassNode type) {
            if (toBeAssignedTo.isDerivedFrom(type)) return true;
            if (toBeAssignedTo.redirect()==STRING_TYPE && type.redirect()==GSTRING_TYPE) {
                return true;
            }
            toBeAssignedTo = getWrapper(toBeAssignedTo);
            type = getWrapper(type);
            if (    toBeAssignedTo.implementsInterface(Number_TYPE) && 
                    type.implementsInterface(Number_TYPE))
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

        private static boolean isClassInt(ClassNode type) {
            return  type==byte_TYPE     ||  type==Byte_TYPE      ||
                    type==char_TYPE     ||  type==Character_TYPE ||
                    type==int_TYPE      ||  type==Integer_TYPE   ||
                    type==short_TYPE    ||  type==Short_TYPE;
        }
        
        private static boolean isClassLong(ClassNode type) {
            return  type==long_TYPE     ||  type==Long_TYPE     ||
                    isClassInt(type);
        }
        
        private static boolean isClassBigInt(ClassNode type) {
            return  type==BigInteger_TYPE || isClassLong(type);
        }
        
        private static boolean isClassBigDec(ClassNode type) {
            return  type==BigDecimal_TYPE || isClassBigInt(type);
        }
        
        private static boolean isClassDouble(ClassNode type) {
            return  type==float_TYPE    ||  type==Float_TYPE    ||
                    type==double_TYPE   ||  type==Double_TYPE   ||
                    isClassBigDec(type);
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
        
        private static ClassNode getType(Expression exp) {
            ClassNode cn = (ClassNode) exp.getNodeMetaData(StaticTypesMarker.class);
            if (cn!=null) return cn;
            return exp.getType();
        }
        
        private void checkCompatibleAssignmenTypes(ClassNode left, ClassNode right, Expression expr) {
            ClassNode leftRedirect = left.redirect();
            ClassNode rightRedirect = right.redirect();
            // on an assignment everything that can be done by a GroovyCast is allowed
            
            // anything can be assigned to an Object, String, boolean, Boolean
            // or Class typed variable
            if  (   leftRedirect==OBJECT_TYPE   || 
                    leftRedirect==STRING_TYPE   ||
                    leftRedirect==boolean_TYPE || 
                    leftRedirect==Boolean_TYPE  ||
                    leftRedirect==CLASS_Type) {
                return;
            }
            
            // if left is Enum and right is String or GString we do valueOf
            if (    leftRedirect.isDerivedFrom(Enum_Type) &&
                    (rightRedirect==GSTRING_TYPE || rightRedirect==STRING_TYPE)) {
                return;
            }
            
            // if right is array, map or collection we try invoking the 
            // constructor
            if (    rightRedirect.implementsInterface(MAP_TYPE)         ||
                    rightRedirect.implementsInterface(Collection_TYPE)  ||
                    rightRedirect.isArray()) {
                //TODO: in case of the array we could maybe make a partial check
                return;
            }
            
            // simple check on being subclass
            if (right.isDerivedFrom(left) || left.implementsInterface(right)) return;
            
            // if left and right are primitives or numbers allow 
            if (isPrimitiveType(leftRedirect) && isPrimitiveType(rightRedirect)) return;
            if (isNumberType(leftRedirect) && isNumberType(rightRedirect)) return;
            addError("Cannot assign value of type "+right+" to variable of type "+left, expr);
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

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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.classgen.ReturnAdder;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.StaticTypesTransformation;
import org.objectweb.asm.Opcodes;

import java.util.*;

import static org.codehaus.groovy.ast.ClassHelper.*;
import static org.codehaus.groovy.ast.tools.WideningCategories.*;
import static org.codehaus.groovy.syntax.Types.*;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.*;

/**
 * The main class code visitor responsible for static type checking. It will perform various inspections like checking
 * assignment types, type inference, ... Eventually, class nodes may be annotated with inferred type information.
 *
 * @author Cedric Champeau
 * @author Jochen Theodorou
 */
public class StaticTypeCheckingVisitor extends ClassCodeVisitorSupport {

    private SourceUnit source;
    private ClassNode classNode;
    private MethodNode methodNode;

    // whenever a "with" method call is detected, this list is updated
    // with the receiver type of the with method
    private LinkedList<ClassNode> withReceiverList = new LinkedList<ClassNode>();
    /**
     * The type of the last encountered "it" implicit parameter
     */
    private ClassNode lastImplicitItType;

    /**
     * Stores information which is only valid in the "if" branch of an if-then-else statement. This is used when the if
     * condition expression makes use of an instanceof check
     */
    private Stack<Map<Object, List<ClassNode>>> temporaryIfBranchTypeInformation;


    private final ReturnAdder returnAdder = new ReturnAdder(new ReturnAdder.ReturnStatementListener() {
        public void returnStatementAdded(final ReturnStatement returnStatement) {
            checkReturnType(returnStatement);
        }
    });

    public StaticTypeCheckingVisitor(SourceUnit source, ClassNode cn) {
        this.source = source;
        this.classNode = cn;
        this.temporaryIfBranchTypeInformation = new Stack<Map<Object, List<ClassNode>>>();
        pushTemporaryTypeInfo();
    }

    //        @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    @Override
    public void visitVariableExpression(VariableExpression vexp) {
        super.visitVariableExpression(vexp);
        if (vexp != VariableExpression.THIS_EXPRESSION &&
                vexp != VariableExpression.SUPER_EXPRESSION) {
            if (vexp.getName().equals("this")) storeType(vexp, classNode);
            if (vexp.getName().equals("super")) storeType(vexp, classNode.getSuperClass());
        }
        if (vexp.getAccessedVariable() instanceof DynamicVariable) {
            // a dynamic variable is either an undeclared variable
            // or a member of a class used in a 'with'
            DynamicVariable dyn = (DynamicVariable) vexp.getAccessedVariable();
            // first, we must check the 'with' context
            String dynName = dyn.getName();
            for (ClassNode node : withReceiverList) {
                if (node.getProperty(dynName) != null) {
                    storeType(vexp, node.getProperty(dynName).getType());
                    return;
                }
                if (node.getField(dynName) != null) {
                    storeType(vexp, node.getField(dynName).getType());
                    return;
                }
            }
            addStaticTypeError("The variable [" + vexp.getName() + "] is undeclared.", vexp);
        }
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        super.visitBinaryExpression(expression);
        final Expression leftExpression = expression.getLeftExpression();
        if (leftExpression instanceof PropertyExpression) {
            // check that property exists
            PropertyExpression pexp = (PropertyExpression) leftExpression;
            if (!existsProperty(pexp)) {
                Expression objectExpression = pexp.getObjectExpression();
                addStaticTypeError("No such property: " + pexp.getPropertyAsString() +
                        " for class: " + findCurrentInstanceOfClass(objectExpression, objectExpression.getType()), leftExpression);
                return;
            }
        }
        ClassNode lType = getType(leftExpression, classNode);
        final Expression rightExpression = expression.getRightExpression();
        ClassNode rType = getType(rightExpression, classNode);
        int op = expression.getOperation().getType();
        ClassNode resultType = getResultType(lType, op, rType, expression);
        if (resultType == null) {
            addStaticTypeError("tbd...", expression);
            resultType = lType;
        }
        // todo : if assignment of a primitive to an object (def, Object, whatever),
        // the type inference engine should return an Object (not a primitive type)
        storeType(expression, resultType);
        if (isAssignment(op)) {
            ClassNode leftRedirect;
            if (isArrayAccessExpression(leftExpression) || leftExpression instanceof PropertyExpression
                    || (leftExpression instanceof VariableExpression
                    && ((VariableExpression) leftExpression).getAccessedVariable() instanceof DynamicVariable)) {
                // in case the left expression is in the form of an array access, we should use
                // the inferred type instead of the left expression type.
                // In case we have a variable expression which accessed variable is a dynamic variable, we are
                // in the "with" case where the type must be taken from the inferred type
                leftRedirect = lType;
            } else {
                leftRedirect = leftExpression.getType().redirect();
            }
            boolean compatible = checkCompatibleAssignmentTypes(leftRedirect, resultType);
            if (!compatible) {
                addStaticTypeError("Cannot assign value of type " + resultType.getName() + " to variable of type " + lType.getName(), expression);
            } else {
                boolean possibleLooseOfPrecision = false;
                if (isNumberType(leftRedirect) && isNumberType(resultType)) {
                    possibleLooseOfPrecision = checkPossibleLooseOfPrecision(leftRedirect, resultType, rightExpression);
                    if (possibleLooseOfPrecision) {
                        addStaticTypeError("Possible loose of precision from " + resultType + " to " + leftRedirect, rightExpression);
                    }
                }
                // if left type is array, we should check the right component types
                if (!possibleLooseOfPrecision && lType.isArray()) {
                    ClassNode leftComponentType = lType.getComponentType();
                    ClassNode rightRedirect = rightExpression.getType().redirect();
                    if (rightRedirect.isArray()) {
                        ClassNode rightComponentType = rightRedirect.getComponentType();
                        if (!checkCompatibleAssignmentTypes(leftComponentType, rightComponentType)) {
                            addStaticTypeError("Cannot assign value of type " + rightComponentType + " into array of type " + lType, expression);
                        }
                    } else if (rightExpression instanceof ListExpression) {
                        for (Expression element : ((ListExpression) rightExpression).getExpressions()) {
                            ClassNode rightComponentType = element.getType().redirect();
                            if (!checkCompatibleAssignmentTypes(leftComponentType, rightComponentType)) {
                                addStaticTypeError("Cannot assign value of type " + rightComponentType + " into array of type " + lType, expression);
                            }
                        }
                    }
                }

                // if left type is not a list but right type is a list, then we're in the case of a groovy
                // constructor type : Dimension d = [100,200]
                // In that case, more checks can be performed
                if (!leftRedirect.implementsInterface(ClassHelper.LIST_TYPE) && rightExpression instanceof ListExpression) {
                    ArgumentListExpression argList = new ArgumentListExpression(((ListExpression) rightExpression).getExpressions());
                    ClassNode[] args = getArgumentTypes(argList, classNode);
                    checkGroovyStyleConstructor(leftRedirect, args);
                } else if (resultType.implementsInterface(Collection_TYPE) && !isAssignableTo(leftRedirect, resultType)) {
                    addStaticTypeError("Cannot assign value of type " + resultType.getName() + " to variable of type " + lType.getName(), expression);
                }

                // if left type is not a list but right type is a map, then we're in the case of a groovy
                // constructor type : A a = [x:2, y:3]
                // In this case, more checks can be performed
                if (!leftRedirect.implementsInterface(ClassHelper.MAP_TYPE) && rightExpression instanceof MapExpression) {
                    ArgumentListExpression argList = new ArgumentListExpression(rightExpression);
                    ClassNode[] args = getArgumentTypes(argList, classNode);
                    checkGroovyStyleConstructor(leftRedirect, args);
                    // perform additional type checking on arguments
                    MapExpression mapExpression = (MapExpression) rightExpression;
                    for (MapEntryExpression entryExpression : mapExpression.getMapEntryExpressions()) {
                        Expression keyExpr = entryExpression.getKeyExpression();
                        if (!(keyExpr instanceof ConstantExpression) ) {
                            addStaticTypeError("Dynamic keys in map-style constructors are unsupported in static type checking", keyExpr);
                        } else {
                            String property = keyExpr.getText();
                            ClassNode currentNode = leftRedirect;
                            PropertyNode propertyNode = null;
                            while (propertyNode==null && currentNode!=null) {
                                propertyNode = currentNode.getProperty(property);
                                currentNode = currentNode.getSuperClass();
                            }
                            if (propertyNode==null) {
                                addStaticTypeError("No such property: " + property +
                                    " for class: " + leftRedirect.getName(), leftExpression);
                            } else {
                                ClassNode valueType = getType(entryExpression.getValueExpression(), classNode);
                                if (!isAssignableTo(valueType, propertyNode.getType())) {
                                    addStaticTypeError("Cannot assign value of type " + valueType.getName() + " to field of type " + propertyNode.getType().getName(), entryExpression);
                                }
                            }
                        }
                    }
                }
            }

            storeType(leftExpression, resultType);
        } else if (op == KEYWORD_INSTANCEOF) {
            final Map<Object, List<ClassNode>> tempo = temporaryIfBranchTypeInformation.peek();
            Object key = extractTemporaryTypeInfoKey(leftExpression);
            List<ClassNode> potentialTypes = tempo.get(key);
            if (potentialTypes == null) {
                potentialTypes = new LinkedList<ClassNode>();
                tempo.put(key, potentialTypes);
            }
            potentialTypes.add(rightExpression.getType());
        }
    }

    /**
     * Checks that a constructor style expression is valid regarding the number of arguments and the argument types.
     * @param node the class node for which we will try to find a matching constructor
     * @param arguments the constructor arguments
     */
    private void checkGroovyStyleConstructor(final ClassNode node, final ClassNode[] arguments) {
        if (node.equals(ClassHelper.OBJECT_TYPE) || node.equals(ClassHelper.DYNAMIC_TYPE)) {
            // in that case, we are facing a list constructor assigned to a def or object
            return;
        }
        List<ConstructorNode> constructors = node.getDeclaredConstructors();
        if (constructors.isEmpty() && arguments.length==0) return;
        MethodNode constructor = findMethod(node, "<init>", arguments);
        if (constructor==null) {
            addStaticTypeError("No matching constructor found: "+node+toMethodParametersString("<init>", arguments), classNode);
        }
    }

    /**
     * When instanceof checks are found in the code, we store temporary type information data in the {@link
     * #temporaryIfBranchTypeInformation} table. This method computes the key which must be used to store this type
     * info.
     *
     * @param expression the expression for which to compute the key
     * @return a key to be used for {@link #temporaryIfBranchTypeInformation}
     */
    private Object extractTemporaryTypeInfoKey(final Expression expression) {
        return expression instanceof VariableExpression ? findTargetVariable((VariableExpression) expression) : expression.getText();
    }

    /**
     * A helper method which determines which receiver class should be used in error messages when a field or attribute
     * is not found. The returned type class depends on whether we have temporary type information availble (due to
     * instanceof checks) and whether there is a single candidate in that case.
     *
     * @param expr the expression for which an unknown field has been found
     * @param type the type of the expression (used as fallback type)
     * @return if temporary information is available and there's only one type, returns the temporary type class
     *         otherwise falls back to the provided type class.
     */
    private ClassNode findCurrentInstanceOfClass(final Expression expr, final ClassNode type) {
        if (!temporaryIfBranchTypeInformation.empty()) {
            Object key = extractTemporaryTypeInfoKey(expr);
            List<ClassNode> nodes = temporaryIfBranchTypeInformation.peek().get(key);
            if (nodes != null && nodes.size() == 1) return nodes.get(0);
        }
        return type;
    }

    /**
     * Checks whether a property exists on the receiver, or on any of the possible receiver classes (found in the
     * temporary type information table)
     *
     * @param pexp a property expression
     * @return true if the property is defined in any of the possible receiver classes
     */
    private boolean existsProperty(final PropertyExpression pexp) {
        Expression objectExpression = pexp.getObjectExpression();
        ClassNode clazz = objectExpression.getType().redirect();
        List<ClassNode> tests = new LinkedList<ClassNode>();
        tests.add(clazz);
        if (!temporaryIfBranchTypeInformation.empty()) {
            Map<Object, List<ClassNode>> info = temporaryIfBranchTypeInformation.peek();
            Object key = extractTemporaryTypeInfoKey(objectExpression);
            List<ClassNode> classNodes = info.get(key);
            if (classNodes != null) tests.addAll(classNodes);
        }
        if (lastImplicitItType != null
                && pexp.getObjectExpression() instanceof VariableExpression
                && ((VariableExpression) pexp.getObjectExpression()).getName().equals("it")) {
            tests.add(lastImplicitItType);
        }
        boolean hasProperty = false;
        String propertyName = pexp.getPropertyAsString();
        boolean isAttributeExpression = pexp instanceof AttributeExpression;
        for (ClassNode testClass : tests) {
            // maps have special handling for property expressions
            if (!testClass.implementsInterface(ClassHelper.MAP_TYPE)) {
                ClassNode current = testClass;
                while (current!=null && !hasProperty) {
                    current = current.redirect();
                    PropertyNode propertyNode = current.getProperty(propertyName);
                    if (propertyNode != null) {
                        hasProperty = true;
                        break;
                    }
                    if (!isAttributeExpression) {
                        FieldNode field = current.getDeclaredField(propertyName);
                        if (field != null) {
                            hasProperty = true;
                            break;
                        }
                    }
                    // if the property expression is an attribute expression (o.@attr), then
                    // we stop now, otherwise we must check the parent class
                    current = isAttributeExpression ?null:current.getSuperClass();
                }
            } else {
                hasProperty = true;
            }
        }
        return hasProperty;
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        super.visitForLoop(forLoop);
        final ClassNode collectionType = getType(forLoop.getCollectionExpression(), classNode);
        ClassNode componentType = collectionType.getComponentType();
        if (componentType == null) {
            if (componentType == ClassHelper.STRING_TYPE) {
                componentType = ClassHelper.Character_TYPE;
            } else {
                componentType = ClassHelper.OBJECT_TYPE;
            }
        }
        if (!checkCompatibleAssignmentTypes(forLoop.getVariableType(), componentType)) {
            addStaticTypeError("Cannot loop with element of type " + forLoop.getVariableType() + " with collection of type " + collectionType, forLoop);
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
        } else if (typeRe == STRING_TYPE || typeRe == GSTRING_TYPE) {
            resultType = PATTERN_TYPE;
        } else if (typeRe == ArrayList_TYPE) {
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
        } else if (typeRe == ArrayList_TYPE) {
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

    private ClassNode[] getArgumentTypes(ArgumentListExpression args, ClassNode current) {
        List<Expression> arglist = args.getExpressions();
        ClassNode[] ret = new ClassNode[arglist.size()];
        int i = 0;
        for (Expression exp : arglist) {
            ret[i] = getType(exp, current);
            i++;
        }
        return ret;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        final String name = call.getMethodAsString();
        if (name == null) {
            addStaticTypeError("cannot resolve dynamic method name at compile time.", call.getMethod());
            return;
        }

        final Expression objectExpression = call.getObjectExpression();
        objectExpression.visit(this);
        call.getMethod().visit(this);

        final ClassNode rememberLastItType = lastImplicitItType;
        Expression callArguments = call.getArguments();

        boolean isWithCall = isWithCall(name, callArguments);

        if (!isWithCall) {
            // if it is not a "with" call, arguments should be visited first
            callArguments.visit(this);
        }

        ClassNode[] args = getArgumentTypes(InvocationWriter.makeArgumentList(callArguments), classNode);
        final ClassNode receiver = getType(objectExpression, classNode);

        if (isWithCall) {
            withReceiverList.add(0, receiver); // must be added first in the list
            lastImplicitItType = receiver;
            // if the provided closure uses an explicit parameter definition, we can
            // also check that the provided type is correct
            if (callArguments instanceof ArgumentListExpression) {
                ArgumentListExpression argList = (ArgumentListExpression) callArguments;
                ClosureExpression closure = (ClosureExpression) argList.getExpression(0);
                Parameter[] parameters = closure.getParameters();
                if (parameters.length > 1) {
                    addStaticTypeError("Unexpected number of parameters for a with call", argList);
                } else if (parameters.length == 1) {
                    Parameter param = parameters[0];
                    if (!param.isDynamicTyped() && !isAssignableTo(param.getType().redirect(), receiver)) {
                        addStaticTypeError("Expected parameter type: " + receiver + " but was: " + param.getType().redirect(), param);
                    }
                }
            }
        }

        try {
            if (isWithCall) {
                // in case of a with call, arguments (the closure) should be visited now that we checked
                // the arguments
                callArguments.visit(this);
            }

            // method call receivers are :
            //   - possible "with" receivers
            //   - the actual receiver as found in the method call expression
            //   - any of the potential receivers found in the instanceof temporary table
            // in that order
            List<ClassNode> receivers = new LinkedList<ClassNode>();
            if (!withReceiverList.isEmpty()) receivers.addAll(withReceiverList);
            receivers.add(receiver);
            if (!temporaryIfBranchTypeInformation.empty()) {
                final Map<Object, List<ClassNode>> tempo = temporaryIfBranchTypeInformation.peek();
                Object key = extractTemporaryTypeInfoKey(objectExpression);
                List<ClassNode> potentialReceiverType = tempo.get(key);
                if (potentialReceiverType != null) receivers.addAll(potentialReceiverType);
            }
            MethodNode mn = null;
            for (ClassNode currentReceiver : receivers) {
                mn = findMethod(currentReceiver, name, args);
                if (mn != null) break;
            }
            if (mn == null) {
                addStaticTypeError("Cannot find matching method " + receiver.getName() + "#" + toMethodParametersString(name, args), call);
            } else {
                storeType(call, mn.getReturnType());
            }
        } finally {
            if (isWithCall) {
                lastImplicitItType = rememberLastItType;
                withReceiverList.removeFirst();
            }
        }
    }

    @Override
    public void visitIfElse(final IfStatement ifElse) {
        // create a new temporary element in the if-then-else type info
        pushTemporaryTypeInfo();
        visitStatement(ifElse);
        ifElse.getBooleanExpression().visit(this);
        ifElse.getIfBlock().visit(this);

        // pop if-then-else temporary type info
        temporaryIfBranchTypeInformation.pop();

        Statement elseBlock = ifElse.getElseBlock();
        if (elseBlock instanceof EmptyStatement) {
            // dispatching to EmptyStatement will not call back visitor,
            // must call our visitEmptyStatement explicitly
            visitEmptyStatement((EmptyStatement) elseBlock);
        } else {
            elseBlock.visit(this);
        }
    }

    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        // create a new temporary element in the if-then-else type info
        pushTemporaryTypeInfo();
        expression.getBooleanExpression().visit(this);
        expression.getTrueExpression().visit(this);
        // pop if-then-else temporary type info
        temporaryIfBranchTypeInformation.pop();
        expression.getFalseExpression().visit(this);
        // store type information
        final ClassNode typeOfTrue = getType(expression.getTrueExpression(), classNode);
        final ClassNode typeOfFalse = getType(expression.getFalseExpression(), classNode);
        storeType(expression, firstCommonSuperType(typeOfTrue, typeOfFalse));
    }

    private void pushTemporaryTypeInfo() {
        Map<Object, List<ClassNode>> potentialTypes = new HashMap<Object, List<ClassNode>>();
        temporaryIfBranchTypeInformation.push(potentialTypes);
    }


    private void storeType(Expression exp, ClassNode cn) {
        exp.putNodeMetaData(StaticTypesTransformation.StaticTypesMarker.class, cn);
        if (exp instanceof VariableExpression) {
            final Variable accessedVariable = ((VariableExpression) exp).getAccessedVariable();
            if (accessedVariable != null && accessedVariable != exp && accessedVariable instanceof VariableExpression) {
                storeType((Expression) accessedVariable, cn);
            }
        }
    }

    private ClassNode getResultType(ClassNode left, int op, ClassNode right, BinaryExpression expr) {
        ClassNode leftRedirect = left.redirect();
        ClassNode rightRedirect = right.redirect();

        if (op == ASSIGN) {
            return leftRedirect.isArray() && !rightRedirect.isArray() ? leftRedirect : rightRedirect;
        } else if (isBoolIntrinsicOp(op)) {
            return boolean_TYPE;
        } else if (isArrayOp(op)) {
            ClassNode arrayType = getType(expr.getLeftExpression(), classNode);
            final ClassNode componentType = arrayType.getComponentType();
            return componentType == null ? ClassHelper.OBJECT_TYPE : componentType;
        } else if (op == FIND_REGEX) {
            // this case always succeeds the result is a Matcher
            return Matcher_TYPE;
        }
        // the left operand is determining the result of the operation
        // for primitives and their wrapper we use a fixed table here
        else if (isNumberType(leftRedirect) && isNumberType(rightRedirect)) {
            if (isOperationInGroup(op)) {
                if (isIntCategory(leftRedirect) && isIntCategory(rightRedirect)) return int_TYPE;
                if (isLongCategory(leftRedirect) && isLongCategory(rightRedirect)) return Long_TYPE;
                if (isBigIntCategory(leftRedirect) && isBigIntCategory(rightRedirect)) return BigInteger_TYPE;
                if (isBigDecCategory(leftRedirect) && isBigDecCategory(rightRedirect)) return BigDecimal_TYPE;
                if (isDoubleCategory(leftRedirect) && isDoubleCategory(rightRedirect)) return double_TYPE;
            } else if (isPowerOperator(op)) {
                return Number_TYPE;
            } else if (isBitOperator(op)) {
                if (isIntCategory(leftRedirect) && isIntCategory(rightRedirect)) return int_TYPE;
                if (isLongCategory(leftRedirect) && isLongCategory(rightRedirect)) return Long_TYPE;
                if (isBigIntCategory(leftRedirect) && isBigIntCategory(rightRedirect)) return BigInteger_TYPE;
            }
        }


        // try to find a method for the operation
        String operationName = getOperationName(op);
        MethodNode method = findMethodOrFail(expr, leftRedirect, operationName, rightRedirect);
        if (method != null) {
            if (isCompareToBoolean(op)) return boolean_TYPE;
            if (op == COMPARE_TO) return int_TYPE;
            return method.getReturnType();
        }
        //TODO: other cases
        return null;
    }

    private MethodNode findMethodOrFail(
            Expression expr,
            ClassNode receiver, String name, ClassNode... args) {
        final MethodNode method = findMethod(receiver, name, args);
        if (method == null) {
            addStaticTypeError("Cannot find matching method " + receiver.getName() + "#" + toMethodParametersString(name, args), expr);
        }
        return method;
    }

    private MethodNode findMethod(
            ClassNode receiver, String name, ClassNode... args) {
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
                if (allParametersAndArgumentsMatch(params, args) ||
                        lastArgMatchesVarg(params, args)) {
                    return m;
                }
            } else if (isVargs(params)) {
                // there are three case for vargs
                // (1) varg part is left out
                if (params.length == args.length + 1) return m;
                // (2) last argument is put in the vargs array
                //      that case is handled above already
                // (3) there is more than one argument for the vargs array
                if (params.length < args.length &&
                        excessArgumentsMatchesVargsParameter(params, args)) {
                    return m;
                }
            }
        }
        if (receiver == ClassHelper.GSTRING_TYPE) return findMethod(ClassHelper.STRING_TYPE, name, args);
        return null;
    }

    private ClassNode getType(Expression exp, ClassNode current) {
        ClassNode cn = (ClassNode) exp.getNodeMetaData(StaticTypesTransformation.StaticTypesMarker.class);
        if (cn != null) return cn;
        if (exp instanceof VariableExpression) {
            VariableExpression vexp = (VariableExpression) exp;
            if (vexp == VariableExpression.THIS_EXPRESSION) return current;
            if (vexp == VariableExpression.SUPER_EXPRESSION) return current.getSuperClass();
            final Variable variable = vexp.getAccessedVariable();
            if (variable != null && variable != vexp && variable instanceof VariableExpression) {
                return getType((Expression) variable, current);
            }
        } else if (exp instanceof PropertyExpression) {
            PropertyExpression pexp = (PropertyExpression) exp;
            if (pexp.getObjectExpression().getType().isEnum()) {
                return pexp.getObjectExpression().getType();
            } else {
                ClassNode clazz = pexp.getObjectExpression().getType().redirect();
                List<ClassNode> candidates = new LinkedList<ClassNode>();
                candidates.add(clazz);
                if (!temporaryIfBranchTypeInformation.empty()) {
                    List<ClassNode> classNodes = temporaryIfBranchTypeInformation.peek().get(extractTemporaryTypeInfoKey(pexp.getObjectExpression()));
                    if (classNodes != null && !classNodes.isEmpty()) candidates.addAll(classNodes);
                }
                String propertyName = pexp.getPropertyAsString();
                boolean isAttributeExpression = pexp instanceof AttributeExpression;
                for (ClassNode candidate : candidates) {
                    ClassNode parent = candidate;
                    while (parent!=null) {
                        parent = parent.redirect();
                        PropertyNode propertyNode = parent.getProperty(propertyName);
                        if (propertyNode != null) return propertyNode.getType();
                        if (!isAttributeExpression) {
                            FieldNode field = parent.getDeclaredField(propertyName);
                            if (field != null) return field.getType();
                        }
                        parent = isAttributeExpression?null:parent.getSuperClass();
                    }
                }
                return ClassHelper.OBJECT_TYPE;
            }
        }
        return exp.getType();
    }

    protected void addStaticTypeError(final String msg, final ASTNode expr) {
        if (expr.getColumnNumber() > 0 && expr.getLineNumber() > 0) {
            addError(StaticTypesTransformation.STATIC_ERROR_PREFIX + msg, expr);
        } else {
            // ignore errors which are related to unknown source locations
            // because they are likely related to generated code
        }
    }

}

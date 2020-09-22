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

import groovy.lang.MetaProperty;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Handy methods when working with the Groovy AST
 */
public class GeneralUtils {
    public static final Token ASSIGN = Token.newSymbol(Types.ASSIGN, -1, -1);
    public static final Token EQ = Token.newSymbol(Types.COMPARE_EQUAL, -1, -1);
    public static final Token NE = Token.newSymbol(Types.COMPARE_NOT_EQUAL, -1, -1);
    public static final Token NOT_IDENTICAL = Token.newSymbol(Types.COMPARE_NOT_IDENTICAL, -1, -1);
    public static final Token LT = Token.newSymbol(Types.COMPARE_LESS_THAN, -1, -1);
    public static final Token AND = Token.newSymbol(Types.LOGICAL_AND, -1, -1);
    public static final Token OR = Token.newSymbol(Types.LOGICAL_OR, -1, -1);
    public static final Token CMP = Token.newSymbol(Types.COMPARE_TO, -1, -1);
    public static final Token INSTANCEOF = Token.newSymbol(Types.KEYWORD_INSTANCEOF, -1, -1);
    public static final Token PLUS = Token.newSymbol(Types.PLUS, -1, -1);
    private static final Token INDEX = Token.newSymbol("[", -1, -1);

    public static BinaryExpression andX(final Expression lhv, final Expression rhv) {
        return binX(lhv, AND, rhv);
    }

    public static ArgumentListExpression args(final Expression... expressions) {
        List<Expression> list = new ArrayList<>(expressions.length);
        Collections.addAll(list, expressions);
        return args(list);
    }

    public static ArgumentListExpression args(final List<Expression> expressions) {
        return new ArgumentListExpression(expressions);
    }

    public static ArgumentListExpression args(final Parameter... parameters) {
        return new ArgumentListExpression(parameters);
    }

    public static ArgumentListExpression args(final String... names) {
        return args(Arrays.stream(names).map(GeneralUtils::varX).toArray(Expression[]::new));
    }

    public static CastExpression asX(final ClassNode type, final Expression expression) {
        return CastExpression.asExpression(type, expression);
    }

    public static Statement assignS(final Expression target, final Expression value) {
        return stmt(assignX(target, value));
    }

    public static Statement assignNullS(final Expression target) {
        return assignS(target, ConstantExpression.EMPTY_EXPRESSION);
    }

    public static Expression assignX(final Expression target, final Expression value) {
        return binX(target, ASSIGN, value);
    }

    public static Expression attrX(final Expression oe, final Expression prop) {
        return new AttributeExpression(oe, prop);
    }

    public static BinaryExpression binX(final Expression left, final Token token, final Expression right) {
        return new BinaryExpression(left, token, right);
    }

    public static BlockStatement block(final VariableScope scope, final Statement... stmts) {
        BlockStatement block = new BlockStatement();
        block.setVariableScope(scope);
        for (Statement stmt : stmts) {
            block.addStatement(stmt);
        }
        return block;
    }

    public static BlockStatement block(final VariableScope scope, final List<Statement> stmts) {
        BlockStatement block = new BlockStatement();
        block.setVariableScope(scope);
        for (Statement stmt : stmts) {
            block.addStatement(stmt);
        }
        return block;
    }

    public static BlockStatement block(final Statement... stmts) {
        BlockStatement block = new BlockStatement();
        for (Statement stmt : stmts) {
            block.addStatement(stmt);
        }
        return block;
    }

    public static BooleanExpression boolX(final Expression expr) {
        return new BooleanExpression(expr);
    }

    public static BytecodeExpression bytecodeX(final Consumer<MethodVisitor> writer) {
        return new BytecodeExpression() {
            @Override
            public void visit(final MethodVisitor visitor) {
                writer.accept(visitor);
            }
        };
    }

    public static BytecodeExpression bytecodeX(final ClassNode type, final Consumer<MethodVisitor> writer) {
        BytecodeExpression expression = bytecodeX(writer);
        expression.setType(type);
        return expression;
    }

    public static MethodCallExpression callSuperX(final String methodName) {
        return callSuperX(methodName, MethodCallExpression.NO_ARGUMENTS);
    }

    public static MethodCallExpression callSuperX(final String methodName, final Expression args) {
        return callX(varX("super"), methodName, args);
    }

    public static MethodCallExpression callThisX(final String methodName) {
        return callThisX(methodName, MethodCallExpression.NO_ARGUMENTS);
    }

    public static MethodCallExpression callThisX(final String methodName, final Expression args) {
        return callX(varX("this"), methodName, args);
    }

    public static MethodCallExpression callX(final Expression receiver, final String methodName) {
        return callX(receiver, methodName, MethodCallExpression.NO_ARGUMENTS);
    }

    public static MethodCallExpression callX(final Expression receiver, final String methodName, final Expression args) {
        return new MethodCallExpression(receiver, methodName, args);
    }

    public static MethodCallExpression callX(final Expression receiver, final Expression method, final Expression args) {
        return new MethodCallExpression(receiver, method, args);
    }

    public static StaticMethodCallExpression callX(final ClassNode receiver, final String methodName) {
        return callX(receiver, methodName, MethodCallExpression.NO_ARGUMENTS);
    }

    public static StaticMethodCallExpression callX(final ClassNode receiver, final String methodName, final Expression args) {
        return new StaticMethodCallExpression(receiver, methodName, args);
    }

    public static CastExpression castX(final ClassNode type, final Expression expression) {
        return new CastExpression(type, expression);
    }

    public static CastExpression castX(final ClassNode type, final Expression expression, final boolean ignoreAutoboxing) {
        return new CastExpression(type, expression, ignoreAutoboxing);
    }

    public static CatchStatement catchS(final Parameter variable, final Statement code) {
        return new CatchStatement(variable, code);
    }

    public static ClassExpression classX(final ClassNode clazz) {
        return new ClassExpression(clazz);
    }

    public static ClassExpression classX(final Class<?> clazz) {
        return classX(ClassHelper.make(clazz).getPlainNodeReference());
    }

    public static ClosureExpression closureX(final Parameter[] params, final Statement code) {
        return new ClosureExpression(params, code);
    }

    public static ClosureExpression closureX(final Statement code) {
        return closureX(Parameter.EMPTY_ARRAY, code);
    }

    /**
     * Builds a binary expression that compares two values.
     *
     * @param lhv expression for the value to compare from
     * @param rhv expression for the value value to compare to
     * @return the expression comparing two values
     */
    public static BinaryExpression cmpX(final Expression lhv, final Expression rhv) {
        return binX(lhv, CMP, rhv);
    }

    public static ConstantExpression constX(final Object val) {
        return new ConstantExpression(val);
    }

    public static ConstantExpression constX(final Object val, final boolean keepPrimitive) {
        return new ConstantExpression(val, keepPrimitive);
    }

    public static ConstructorCallExpression ctorX(final ClassNode type, final Expression args) {
        return new ConstructorCallExpression(type, args);
    }

    public static ConstructorCallExpression ctorX(final ClassNode type) {
        return ctorX(type, ArgumentListExpression.EMPTY_ARGUMENTS);
    }

    public static Statement ctorSuperS(final Expression args) {
        return stmt(ctorSuperX(args));
    }

    public static ConstructorCallExpression ctorSuperX(Expression args) {
        return ctorX(ClassNode.SUPER, args);
    }

    public static Statement ctorThisS(final Expression args) {
        return stmt(ctorThisX(args));
    }

    public static ConstructorCallExpression ctorThisX(Expression args) {
        return ctorX(ClassNode.THIS, args);
    }

    public static Statement ctorSuperS() {
        return stmt(ctorSuperX());
    }

    public static ConstructorCallExpression ctorSuperX() {
        return ctorX(ClassNode.SUPER);
    }

    public static Statement ctorThisS() {
        return stmt(ctorThisX());
    }

    public static ConstructorCallExpression ctorThisX() {
        return ctorX(ClassNode.THIS);
    }

    public static Statement declS(final Expression target, final Expression init) {
        return stmt(declX(target, init));
    }

    public static DeclarationExpression declX(final Expression target, final Expression init) {
        return new DeclarationExpression(target, ASSIGN, init);
    }

    public static MapEntryExpression entryX(final Expression key, final Expression value) {
        return new MapEntryExpression(key, value);
    }

    public static BinaryExpression eqX(final Expression lhv, final Expression rhv) {
        return binX(lhv, EQ, rhv);
    }

    public static BooleanExpression equalsNullX(final Expression argExpr) {
        return boolX(eqX(argExpr, nullX()));
    }

    public static FieldExpression fieldX(final FieldNode fieldNode) {
        return new FieldExpression(fieldNode);
    }

    public static FieldExpression fieldX(final ClassNode owner, final String fieldName) {
        return new FieldExpression(owner.getField(fieldName));
    }

    public static Expression findArg(final String argName) {
        return propX(varX("args"), argName);
    }

    public static List<MethodNode> getAllMethods(final ClassNode type) {
        ClassNode node = type;
        List<MethodNode> result = new ArrayList<>();
        while (node != null) {
            result.addAll(node.getMethods());
            node = node.getSuperClass();
        }
        return result;
    }

    public static List<PropertyNode> getAllProperties(final ClassNode type) {
        ClassNode node = type;
        List<PropertyNode> result = new ArrayList<>();
        while (node != null) {
            result.addAll(node.getProperties());
            node = node.getSuperClass();
        }
        return result;
    }

    public static List<FieldNode> getInstanceNonPropertyFields(final ClassNode cNode) {
        List<FieldNode> result = new ArrayList<>();
        for (FieldNode fNode : cNode.getFields()) {
            if (!fNode.isStatic() && cNode.getProperty(fNode.getName()) == null) {
                result.add(fNode);
            }
        }
        return result;
    }

    public static List<String> getInstanceNonPropertyFieldNames(final ClassNode cNode) {
        List<FieldNode> fList = getInstanceNonPropertyFields(cNode);
        List<String> result = new ArrayList<>(fList.size());
        for (FieldNode fNode : fList) {
            result.add(fNode.getName());
        }
        return result;
    }

    public static List<PropertyNode> getInstanceProperties(final ClassNode cNode) {
        List<PropertyNode> result = new ArrayList<>();
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode);
            }
        }
        return result;
    }

    public static List<String> getInstancePropertyNames(final ClassNode cNode) {
        List<PropertyNode> pList = BeanUtils.getAllProperties(cNode, false, false, true);
        List<String> result = new ArrayList<>(pList.size());
        for (PropertyNode pNode : pList) {
            result.add(pNode.getName());
        }
        return result;
    }

    public static List<FieldNode> getInstancePropertyFields(final ClassNode cNode) {
        List<FieldNode> result = new ArrayList<>();
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode.getField());
            }
        }
        return result;
    }

    public static Set<ClassNode> getInterfacesAndSuperInterfaces(final ClassNode type) {
        Set<ClassNode> res = new LinkedHashSet<>();
        if (type.isInterface()) {
            res.add(type);
            return res;
        }
        ClassNode next = type;
        while (next != null) {
            res.addAll(next.getAllInterfaces());
            next = next.getSuperClass();
        }
        return res;
    }

    public static List<FieldNode> getSuperNonPropertyFields(final ClassNode cNode) {
        List<FieldNode> result;
        if (cNode == ClassHelper.OBJECT_TYPE) {
            result = new ArrayList<>();
        } else {
            result = getSuperNonPropertyFields(cNode.getSuperClass());
        }
        for (FieldNode fNode : cNode.getFields()) {
            if (!fNode.isStatic() && cNode.getProperty(fNode.getName()) == null) {
                result.add(fNode);
            }
        }
        return result;
    }

    public static List<FieldNode> getSuperPropertyFields(final ClassNode cNode) {
        List<FieldNode> result;
        if (cNode == ClassHelper.OBJECT_TYPE) {
            result = new ArrayList<>();
        } else {
            result = getSuperPropertyFields(cNode.getSuperClass());
        }
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode.getField());
            }
        }
        return result;
    }

    public static List<PropertyNode> getAllProperties(final Set<String> names, final ClassNode cNode, final boolean includeProperties, final boolean includeFields, final boolean includePseudoGetters, final boolean includePseudoSetters, final boolean traverseSuperClasses, final boolean skipReadonly) {
        return getAllProperties(names, cNode, cNode, includeProperties, includeFields, includePseudoGetters, includePseudoSetters, traverseSuperClasses, skipReadonly);
    }

    public static List<PropertyNode> getAllProperties(final Set<String> names, final ClassNode origType, final ClassNode cNode, final boolean includeProperties, final boolean includeFields, final boolean includePseudoGetters, final boolean includePseudoSetters, final boolean traverseSuperClasses, final boolean skipReadonly) {
        return getAllProperties(names, origType, cNode, includeProperties, includeFields, includePseudoGetters, includePseudoSetters, traverseSuperClasses, skipReadonly, false, false, false);
    }

    public static List<PropertyNode> getAllProperties(final Set<String> names, final ClassNode origType, final ClassNode cNode, final boolean includeProperties,
                                                      final boolean includeFields, final boolean includePseudoGetters, final boolean includePseudoSetters,
                                                      final boolean traverseSuperClasses, final boolean skipReadonly, final boolean reverse, final boolean allNames, final boolean includeStatic) {
        List<PropertyNode> result = new ArrayList<>();
        if (cNode != ClassHelper.OBJECT_TYPE && traverseSuperClasses && !reverse) {
            result.addAll(getAllProperties(names, origType, cNode.getSuperClass(), includeProperties, includeFields, includePseudoGetters, includePseudoSetters, true, skipReadonly));
        }
        if (includeProperties) {
            for (PropertyNode pNode : cNode.getProperties()) {
                if ((!pNode.isStatic() || includeStatic) && !names.contains(pNode.getName())) {
                    result.add(pNode);
                    names.add(pNode.getName());
                }
            }
            if (includePseudoGetters || includePseudoSetters) {
                BeanUtils.addPseudoProperties(origType, cNode, result, names, includeStatic, includePseudoGetters, includePseudoSetters);
            }
        }
        if (includeFields) {
            for (FieldNode fNode : cNode.getFields()) {
                if ((fNode.isStatic() && !includeStatic) || fNode.isSynthetic() || cNode.getProperty(fNode.getName()) != null || names.contains(fNode.getName())) {
                    continue;
                }

                // internal field
                if (fNode.getName().contains("$") && !allNames) {
                    continue;
                }

                if (fNode.isPrivate() && !cNode.equals(origType)) {
                    continue;
                }
                if (fNode.isFinal() && fNode.getInitialExpression() != null && skipReadonly) {
                    continue;
                }
                result.add(new PropertyNode(fNode, fNode.getModifiers(), null, null));
                names.add(fNode.getName());
            }
        }
        if (cNode != ClassHelper.OBJECT_TYPE && traverseSuperClasses && reverse) {
            result.addAll(getAllProperties(names, origType, cNode.getSuperClass(), includeProperties, includeFields, includePseudoGetters, includePseudoSetters, true, skipReadonly));
        }
        return result;
    }

    /**
     * This method is similar to {@link #propX(Expression, Expression)} but will make sure that if the property
     * being accessed is defined inside the classnode provided as a parameter, then a getter call is generated
     * instead of a field access.
     * @param annotatedNode the class node where the property node is accessed from
     * @param pNode the property being accessed
     * @return a method call expression or a property expression
     */
    public static Expression getterThisX(final ClassNode annotatedNode, final PropertyNode pNode) {
        ClassNode owner = pNode.getDeclaringClass();
        if (annotatedNode.equals(owner)) {
            return callThisX(pNode.getGetterNameOrDefault());
        }
        return propX(varX("this"), pNode.getName());
    }

    /**
     * This method is similar to {@link #propX(Expression, Expression)} but will make sure that if the property
     * being accessed is defined inside the classnode provided as a parameter, then a getter call is generated
     * instead of a field access.
     * @param annotatedNode the class node where the property node is accessed from
     * @param receiver the object having the property
     * @param pNode the property being accessed
     * @return a method call expression or a property expression
     */
    public static Expression getterX(final ClassNode annotatedNode, final Expression receiver, final PropertyNode pNode) {
        ClassNode owner = pNode.getDeclaringClass();
        if (annotatedNode.equals(owner)) {
            return callX(receiver, pNode.getGetterNameOrDefault());
        }
        return propX(receiver, pNode.getName());
    }

    public static BinaryExpression hasClassX(final Expression instance, final ClassNode cNode) {
        return eqX(classX(cNode), callX(instance, "getClass"));
    }

    public static BinaryExpression hasEqualFieldX(final FieldNode fNode, final Expression other) {
        return eqX(varX(fNode), propX(other, fNode.getName()));
    }

    public static BinaryExpression hasEqualPropertyX(final ClassNode annotatedNode, final PropertyNode pNode, final VariableExpression other) {
        return eqX(getterThisX(annotatedNode, pNode), getterX(other.getOriginType(), other, pNode));
    }

    @Deprecated
    public static BinaryExpression hasEqualPropertyX(final PropertyNode pNode, final Expression other) {
        String getterName = pNode.getGetterNameOrDefault();
        return eqX(callThisX(getterName), callX(other, getterName));
    }

    public static BooleanExpression hasSameFieldX(final FieldNode fNode, final Expression other) {
        return sameX(varX(fNode), propX(other, fNode.getName()));
    }

    public static BooleanExpression hasSamePropertyX(final PropertyNode pNode, final Expression other) {
        ClassNode cNode = pNode.getDeclaringClass();
        return sameX(getterThisX(cNode, pNode), getterX(cNode, other, pNode));
    }

    @Deprecated
    public static Statement ifElseS$$bridge(final Expression cond, final Statement thenStmt, final Statement elseStmt) {
        return ifElseS(cond, thenStmt, elseStmt);
    }

    public static IfStatement ifElseS(final Expression cond, final Statement thenStmt, final Statement elseStmt) {
        return new IfStatement(
                cond instanceof BooleanExpression ? (BooleanExpression) cond : boolX(cond),
                thenStmt,
                elseStmt
        );
    }

    @Deprecated
    public static Statement ifS$$bridge(final Expression cond, final Expression trueExpr) {
        return ifS(cond, trueExpr);
    }

    public static IfStatement ifS(final Expression cond, final Expression trueExpr) {
        return ifElseS(cond, stmt(trueExpr), EmptyStatement.INSTANCE);
    }

    @Deprecated
    public static Statement ifS$$bridge(final Expression cond, final Statement trueStmt) {
        return ifS(cond, trueStmt);
    }

    public static IfStatement ifS(final Expression cond, final Statement trueStmt) {
        return ifElseS(cond, trueStmt, EmptyStatement.INSTANCE);
    }

    public static Expression indexX(final Expression target, final Expression value) {
        return binX(target, INDEX, value);
    }

    public static BooleanExpression isInstanceOfX(final Expression objectExpression, final ClassNode cNode) {
        return boolX(binX(objectExpression, INSTANCEOF, classX(cNode)));
    }

    /**
     * Alias for {@link #equalsNullX(Expression)}
     */
    public static BooleanExpression isNullX(final Expression expr) {
        return equalsNullX(expr);
    }

    public static BooleanExpression isOneX(final Expression expr) {
        return boolX(binX(expr, EQ, constX(1)));
    }

    public static BooleanExpression isTrueX(final Expression argExpr) {
        return boolX(binX(argExpr, EQ, constX(Boolean.TRUE)));
    }

    public static BooleanExpression isZeroX(final Expression expr) {
        return boolX(binX(expr, EQ, constX(0)));
    }

    public static ListExpression listX(final List<Expression> args) {
        return new ListExpression(args);
    }

    public static ListExpression list2args(final List<?> args) {
        ListExpression result = new ListExpression();
        for (Object o : args) {
            result.addExpression(constX(o));
        }
        return result;
    }

    public static ListExpression classList2args(final List<String> args) {
        ListExpression result = new ListExpression();
        for (Object o : args) {
            result.addExpression(classX(ClassHelper.make(o.toString())));
        }
        return result;
    }

    public static VariableExpression localVarX(final String name) {
        VariableExpression result = varX(name);
        result.setAccessedVariable(result);
        return result;
    }

    public static VariableExpression localVarX(final String name, final ClassNode type) {
        VariableExpression result = varX(name, type);
        result.setAccessedVariable(result);
        return result;
    }

    public static BinaryExpression ltX(final Expression lhv, final Expression rhv) {
        return binX(lhv, LT, rhv);
    }

    public static MapExpression mapX(final List<MapEntryExpression> expressions) {
        return new MapExpression(expressions);
    }

    public static BinaryExpression neX(final Expression lhv, final Expression rhv) {
        return binX(lhv, NE, rhv);
    }

    public static BinaryExpression notIdenticalX(final Expression lhv, final Expression rhv) {
        return binX(lhv, NOT_IDENTICAL, rhv);
    }

    public static BooleanExpression notNullX(final Expression argExpr) {
        return boolX(binX(argExpr, NE, nullX()));
    }

    public static NotExpression notX(final Expression expr) {
        return new NotExpression(expr instanceof BooleanExpression ? expr : boolX(expr));
    }

    public static ConstantExpression nullX() {
        return constX(null);
    }

    public static BinaryExpression orX(final Expression lhv, final Expression rhv) {
        return binX(lhv, OR, rhv);
    }

    public static Parameter param(final ClassNode type, final String name) {
        return param(type, name, null);
    }

    public static Parameter param(final ClassNode type, final String name, final Expression initialExpression) {
        Parameter param = new Parameter(type, name);
        if (initialExpression != null) {
            param.setInitialExpression(initialExpression);
        }
        return param;
    }

    public static Parameter[] params(final Parameter... params) {
        return (params != null ? params : Parameter.EMPTY_ARRAY);
    }

    public static BinaryExpression plusX(final Expression lhv, final Expression rhv) {
        return binX(lhv, PLUS, rhv);
    }

    public static PropertyExpression propX(final Expression owner, final String property) {
        return new PropertyExpression(owner, property);
    }

    @Deprecated
    public static Expression propX$$bridge(final Expression owner, final String property) {
        return propX(owner, property);
    }

    public static PropertyExpression propX(final Expression owner, final Expression property) {
        return new PropertyExpression(owner, property);
    }

    @Deprecated
    public static Expression propX$$bridge(final Expression owner, final Expression property) {
        return propX(owner, property);
    }

    public static PropertyExpression propX(final Expression owner, final Expression property, final boolean safe) {
        return new PropertyExpression(owner, property, safe);
    }

    public static Statement returnS(final Expression expr) {
        return new ReturnStatement(new ExpressionStatement(expr));
    }

    public static Statement safeExpression(final Expression fieldExpr, final Expression expression) {
        return new IfStatement(equalsNullX(fieldExpr), stmt(fieldExpr), stmt(expression));
    }

    public static BooleanExpression sameX(final Expression self, final Expression other) {
        return boolX(callX(self, "is", args(other)));
    }

    public static Statement stmt(final Expression expr) {
        return new ExpressionStatement(expr);
    }

    public static TernaryExpression ternaryX(final Expression cond, final Expression trueExpr, final Expression elseExpr) {
        return new TernaryExpression(
                cond instanceof BooleanExpression ? (BooleanExpression) cond : boolX(cond),
                trueExpr,
                elseExpr);
    }

    public static PropertyExpression thisPropX(final boolean implicit, final String property) {
        PropertyExpression pexp = propX(varX("this"), property);
        pexp.setImplicitThis(implicit);
        return pexp;
    }

    public static ThrowStatement throwS(final Expression expr) {
        return new ThrowStatement(expr);
    }

    public static TryCatchStatement tryCatchS(final Statement tryStatement) {
        return tryCatchS(tryStatement, EmptyStatement.INSTANCE);
    }

    public static TryCatchStatement tryCatchS(final Statement tryStatement, final Statement finallyStatement) {
        return new TryCatchStatement(tryStatement, finallyStatement);
    }

    public static TryCatchStatement tryCatchS(final Statement tryStatement, final Statement finallyStatement, final CatchStatement... catchStatements) {
        TryCatchStatement result = new TryCatchStatement(tryStatement, finallyStatement);
        for (CatchStatement catchStatement : catchStatements) {
            result.addCatch(catchStatement);
        }
        return result;
    }

    public static VariableExpression varX(final String name) {
        return new VariableExpression(name);
    }

    public static VariableExpression varX(final Variable variable) {
        return new VariableExpression(variable);
    }

    public static VariableExpression varX(final String name, final ClassNode type) {
        return new VariableExpression(name, type);
    }

    //--------------------------------------------------------------------------

    public static Parameter[] cloneParams(final Parameter[] parameters) {
        return Arrays.stream(parameters).map(p -> param(p.getOriginType(), p.getName())).toArray(Parameter[]::new);
    }

    /**
     * Copies all <tt>candidateAnnotations</tt> with retention policy {@link java.lang.annotation.RetentionPolicy#RUNTIME}
     * and {@link java.lang.annotation.RetentionPolicy#CLASS}.
     * <p>
     * Annotations with {@link org.codehaus.groovy.runtime.GeneratedClosure} members are not supported at present.
     */
    public static void copyAnnotatedNodeAnnotations(final AnnotatedNode annotatedNode, final List<AnnotationNode> copied, final List<AnnotationNode> notCopied) {
        copyAnnotatedNodeAnnotations(annotatedNode, copied, notCopied, true);
    }

    /**
     * Copies all <tt>candidateAnnotations</tt> with retention policy {@link java.lang.annotation.RetentionPolicy#RUNTIME}
     * and {@link java.lang.annotation.RetentionPolicy#CLASS}.
     * {@link groovy.transform.Generated} annotations will be copied if {@code includeGenerated} is true.
     * <p>
     * Annotations with {@link org.codehaus.groovy.runtime.GeneratedClosure} members are not supported at present.
     */
    public static void copyAnnotatedNodeAnnotations(final AnnotatedNode annotatedNode, final List<AnnotationNode> copied, final List<AnnotationNode> notCopied, final boolean includeGenerated) {
        List<AnnotationNode> annotationList = annotatedNode.getAnnotations();
        for (AnnotationNode annotation : annotationList)  {
            List<AnnotationNode> annotations = annotation.getClassNode().getAnnotations(AbstractASTTransformation.RETENTION_CLASSNODE);
            if (annotations.isEmpty()) continue;

            if (hasClosureMember(annotation)) {
                notCopied.add(annotation);
                continue;
            }

            if (!includeGenerated && annotation.getClassNode().getName().equals("groovy.transform.Generated")) {
                continue;
            }

            AnnotationNode retentionPolicyAnnotation = annotations.get(0);
            Expression valueExpression = retentionPolicyAnnotation.getMember("value");
            if (!(valueExpression instanceof PropertyExpression)) continue;

            PropertyExpression propertyExpression = (PropertyExpression) valueExpression;
            boolean processAnnotation = propertyExpression.getProperty() instanceof ConstantExpression
                    && ("RUNTIME".equals(((ConstantExpression) (propertyExpression.getProperty())).getValue())
                        || "CLASS".equals(((ConstantExpression) (propertyExpression.getProperty())).getValue()));
            if (processAnnotation)  {
                AnnotationNode newAnnotation = new AnnotationNode(annotation.getClassNode());
                for (Map.Entry<String, Expression> member : annotation.getMembers().entrySet())  {
                    newAnnotation.addMember(member.getKey(), member.getValue());
                }
                newAnnotation.setSourcePosition(annotatedNode);

                copied.add(newAnnotation);
            }
        }
    }

    public static Statement createConstructorStatementDefault(final FieldNode fNode) {
        final String name = fNode.getName();
        final ClassNode fType = fNode.getType();
        final Expression fieldExpr = propX(varX("this"), name);
        Expression initExpr = fNode.getInitialValueExpression();
        Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            if (ClassHelper.isPrimitiveType(fType)) {
                assignInit = EmptyStatement.INSTANCE;
            } else {
                assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
            }
        } else {
            assignInit = assignS(fieldExpr, initExpr);
        }
        fNode.setInitialValueExpression(null);
        Expression value = findArg(name);
        return ifElseS(equalsNullX(value), assignInit, assignS(fieldExpr, castX(fType, value)));
    }

    /**
     * Generally preferred to use {@link PropertyNode#getGetterNameOrDefault()} directly.
     */
    public static String getGetterName(final PropertyNode pNode) {
        return pNode.getGetterNameOrDefault();
    }

    /**
     * WARNING: Avoid this method unless just the name and type are available.
     * Use {@link #getGetterName(PropertyNode)} if the propertyNode is available.
     */
    public static String getGetterName(final String name, final Class<?> type) {
        return MetaProperty.getGetterName(name, type);
    }

    /**
     * WARNING: Avoid this method unless just the name is available.
     * Use {@link #getGetterName(PropertyNode)} if the propertyNode is available.
     * Use {@link #getGetterName(String, Class)} if the type is available.
     */
    public static String getGetterName(final String name) {
        return MetaProperty.getGetterName(name, Object.class);
    }

    public static String getSetterName(final String name) {
        return MetaProperty.getSetterName(name);
    }

    /**
     * Converts an expression into the String source. Only some specific expressions like closure expression
     * support this.
     *
     * @param readerSource a source
     * @param expression an expression. Can't be null
     * @return the source the closure was created from
     * @throws java.lang.IllegalArgumentException when expression is null
     * @throws java.lang.Exception when closure can't be read from source
     */
    public static String convertASTToSource(final ReaderSource readerSource, final ASTNode expression) throws Exception {
        if (expression == null) throw new IllegalArgumentException("Null: expression");

        StringBuilder result = new StringBuilder();
        for (int x = expression.getLineNumber(), y = expression.getLastLineNumber(); x <= y; x += 1) {
            String line = readerSource.getLine(x, null);
            if (line == null) {
                throw new Exception(
                        "Error calculating source code for expression. Trying to read line " + x + " from " + readerSource.getClass()
                );
            }
            if (x == expression.getLastLineNumber()) {
                line = line.substring(0, expression.getLastColumnNumber() - 1);
            }
            if (x == expression.getLineNumber()) {
                line = line.substring(expression.getColumnNumber() - 1);
            }
            //restoring line breaks is important b/c of lack of semicolons
            result.append(line).append('\n');
        }

        String source = result.toString().trim();

        return source;
    }

    public static boolean copyStatementsWithSuperAdjustment(final ClosureExpression pre, final BlockStatement body) {
        Statement preCode = pre.getCode();
        boolean changed = false;
        if (preCode instanceof BlockStatement) {
            BlockStatement block = (BlockStatement) preCode;
            List<Statement> statements = block.getStatements();
            for (int i = 0, n = statements.size(); i < n; i += 1) {
                Statement statement = statements.get(i);
                // adjust the first statement if it's a super call
                if (i == 0 && statement instanceof ExpressionStatement) {
                    ExpressionStatement es = (ExpressionStatement) statement;
                    Expression preExp = es.getExpression();
                    if (preExp instanceof MethodCallExpression) {
                        MethodCallExpression mce = (MethodCallExpression) preExp;
                        String name = mce.getMethodAsString();
                        if ("super".equals(name)) {
                            es.setExpression(new ConstructorCallExpression(ClassNode.SUPER, mce.getArguments()));
                            changed = true;
                        }
                    }
                }
                body.addStatement(statement);
            }
        }
        return changed;
    }

    private static boolean hasClosureMember(final AnnotationNode annotation) {
        Map<String, Expression> members = annotation.getMembers();
        for (Map.Entry<String, Expression> member : members.entrySet())  {
            if (member.getValue() instanceof ClosureExpression) return true;

            if (member.getValue() instanceof ClassExpression)  {
                ClassExpression classExpression = (ClassExpression) member.getValue();
                Class<?> typeClass = classExpression.getType().isResolved() ? classExpression.getType().redirect().getTypeClass() : null;
                if (typeClass != null && GeneratedClosure.class.isAssignableFrom(typeClass)) return true;
            }
        }
        return false;
    }

    public static boolean hasDeclaredMethod(final ClassNode cNode, final String name, final int argsCount) {
        List<MethodNode> methods = cNode.getDeclaredMethods(name);
        for (MethodNode method : methods) {
            Parameter[] params = method.getParameters();
            if (params != null && params.length == argsCount) {
                return true;
            }
        }
        return false;
    }

    public static boolean inSamePackage(final ClassNode first, final ClassNode second) {
        PackageNode firstPackage = first.getPackage();
        PackageNode secondPackage = second.getPackage();
        return ((firstPackage == null && secondPackage == null)
                || firstPackage != null && secondPackage != null && firstPackage.getName().equals(secondPackage.getName()));
    }

    public static boolean inSamePackage(final Class<?> first, final Class<?> second) {
        Package firstPackage = first.getPackage();
        Package secondPackage = second.getPackage();
        return ((firstPackage == null && secondPackage == null)
                || firstPackage != null && secondPackage != null && firstPackage.getName().equals(secondPackage.getName()));
    }

    public static boolean isDefaultVisibility(final int modifiers) {
        return (modifiers & (Modifier.PRIVATE | Modifier.PUBLIC | Modifier.PROTECTED)) == 0;
    }

    public static boolean isOrImplements(final ClassNode type, final ClassNode interfaceType) {
        return type.equals(interfaceType) || type.implementsInterface(interfaceType);
    }

    /**
     * @deprecated use MethodNodeUtils#methodDescriptorWithoutReturnType(MethodNode) instead
     */
    @Deprecated
    public static String makeDescriptorWithoutReturnType(final MethodNode mn) {
        StringBuilder sb = new StringBuilder();
        sb.append(mn.getName()).append(':');
        for (Parameter p : mn.getParameters()) {
            sb.append(p.getType()).append(',');
        }
        return sb.toString();
    }
}

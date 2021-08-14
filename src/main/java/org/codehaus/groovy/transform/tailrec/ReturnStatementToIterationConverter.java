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
package org.codehaus.groovy.transform.tailrec;

import groovy.lang.Closure;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Translates all return statements into an invocation of the next iteration. This can be either
 * - "continue LOOP_LABEL": Outside closures
 * - "throw LOOP_EXCEPTION": Inside closures
 * <p>
 * Moreover, before adding the recur statement the iteration parameters (originally the method args)
 * are set to their new value. To prevent variable aliasing parameters will be copied into temp vars
 * before they are changes so that their current iteration value can be used when setting other params.
 * <p>
 * There's probably place for optimizing the amount of variable copying being done, e.g.
 * parameters that are only handed through must not be copied at all.
 */
class ReturnStatementToIterationConverter {
    public ReturnStatementToIterationConverter() {}

    public ReturnStatementToIterationConverter(Statement recurStatement) {
        this.recurStatement = recurStatement;
    }

    public Statement convert(ReturnStatement statement, final Map<Integer, Map> positionMapping) {
        Expression recursiveCall = statement.getExpression();
        if (!isAMethodCalls(recursiveCall)) return statement;

        final Map<String, Map> tempMapping = new LinkedHashMap<String, Map>();
        final Map<String, ExpressionStatement> tempDeclarations = new LinkedHashMap<>();
        final List<ExpressionStatement> argAssignments = new ArrayList<ExpressionStatement>();

        final BlockStatement result = new BlockStatement();
        result.copyStatementLabels(statement);

        /* Create temp declarations for all method arguments.
         * Add the declarations and var mapping to tempMapping and tempDeclarations for further reference.
         */
        DefaultGroovyMethods.eachWithIndex(getArguments(recursiveCall), new Closure<Void>(this, this) {
            public void doCall(Expression expression, int index) {
                ExpressionStatement tempDeclaration = createTempDeclaration(index, positionMapping, tempMapping, tempDeclarations);
                result.addStatement(tempDeclaration);
            }

        });

        /*
         * Assign the iteration variables their new value before recuring
         */
        DefaultGroovyMethods.eachWithIndex(getArguments(recursiveCall), new Closure<Void>(this, this) {
            public void doCall(Expression expression, int index) {
                ExpressionStatement argAssignment = createAssignmentToIterationVariable(expression, index, positionMapping);
                argAssignments.add(argAssignment);
                result.addStatement(argAssignment);
            }

        });

        Set<String> unusedTemps = replaceAllArgUsages(argAssignments, tempMapping);
        for (String temp : unusedTemps) {
            result.getStatements().remove(tempDeclarations.get(temp));
        }

        result.addStatement(recurStatement);

        return result;
    }

    private ExpressionStatement createAssignmentToIterationVariable(Expression expression, int index, Map<Integer, Map> positionMapping) {
        String argName = (String) positionMapping.get(index).get("name");
        ClassNode argAndTempType = DefaultGroovyMethods.asType(positionMapping.get(index).get("type"), ClassNode.class);
        ExpressionStatement argAssignment = (ExpressionStatement) GeneralUtils.assignS(GeneralUtils.varX(argName, argAndTempType), expression);
        return argAssignment;
    }

    private ExpressionStatement createTempDeclaration(int index, Map<Integer, Map> positionMapping, Map<String, Map> tempMapping, Map<String, ExpressionStatement> tempDeclarations) {
        final String argName = (String) positionMapping.get(index).get("name");
        String tempName = "_" + argName + "_";
        ClassNode argAndTempType = DefaultGroovyMethods.asType(positionMapping.get(index).get("type"), ClassNode.class);
        ExpressionStatement tempDeclaration = AstHelper.createVariableAlias(tempName, argAndTempType, argName);
        Map<String, Object> map = new LinkedHashMap<String, Object>(2);
        map.put("name", tempName);
        map.put("type", argAndTempType);
        tempMapping.put(argName, map);
        tempDeclarations.put(tempName, tempDeclaration);
        return tempDeclaration;
    }

    @SuppressWarnings("Instanceof")
    private List<Expression> getArguments(Expression recursiveCall) {
        if (recursiveCall instanceof MethodCallExpression)
            return ((TupleExpression) ((MethodCallExpression) recursiveCall).getArguments()).getExpressions();
        if (recursiveCall instanceof StaticMethodCallExpression)
            return ((TupleExpression) ((StaticMethodCallExpression) recursiveCall).getArguments()).getExpressions();
        return null;
    }

    private boolean isAMethodCalls(Expression expression) {
        Class<?> clazz = expression.getClass();
        return MethodCallExpression.class == clazz || StaticMethodCallExpression.class == clazz;
    }

    private Set<String> replaceAllArgUsages(List<ExpressionStatement> iterationVariablesAssignmentNodes, Map<String, Map> tempMapping) {
        Set<String> unusedTempNames = DefaultGroovyMethods.asType(DefaultGroovyMethods.collect(tempMapping.values(), new Closure<String>(this, this) {
            public String doCall(Map nameAndType) {
                return (String) nameAndType.get("name");
            }

        }), Set.class);
        VariableReplacedListener tracker = new UsedVariableTracker();
        for (ExpressionStatement statement : iterationVariablesAssignmentNodes) {
            replaceArgUsageByTempUsage((BinaryExpression) statement.getExpression(), tempMapping, (UsedVariableTracker) tracker);
        }

        unusedTempNames = DefaultGroovyMethods.minus(unusedTempNames, ((UsedVariableTracker) tracker).getUsedVariableNames());
        return unusedTempNames;
    }

    private void replaceArgUsageByTempUsage(BinaryExpression binary, Map tempMapping, UsedVariableTracker tracker) {
        VariableAccessReplacer replacer = new VariableAccessReplacer(tempMapping, tracker);
        // Replacement must only happen in binary.rightExpression. It's a hack in VariableExpressionReplacer which takes care of that.
        replacer.replaceIn(binary);
    }

    public Statement getRecurStatement() {
        return recurStatement;
    }

    public void setRecurStatement(Statement recurStatement) {
        this.recurStatement = recurStatement;
    }

    private Statement recurStatement = AstHelper.recurStatement();
}


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
package org.codehaus.groovy.transform.tailrec

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement

import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * Translates all return statements into an invocation of the next iteration. This can be either
 * - "continue LOOP_LABEL": Outside closures
 * - "throw LOOP_EXCEPTION": Inside closures
 *
 * Moreover, before adding the recur statement the iteration parameters (originally the method args)
 * are set to their new value. To prevent variable aliasing parameters will be copied into temp vars
 * before they are changes so that their current iteration value can be used when setting other params.
 *
 * There's probably place for optimizing the amount of variable copying being done, e.g.
 * parameters that are only handed through must not be copied at all.
 */
@CompileStatic
class ReturnStatementToIterationConverter {

    Statement recurStatement = AstHelper.recurStatement()

    Statement convert(ReturnStatement statement, Map<Integer, Map> positionMapping) {
        Expression recursiveCall = statement.expression
        if (!isAMethodCalls(recursiveCall))
            return statement

        Map<String, Map> tempMapping = [:]
        Map tempDeclarations = [:]
        List<ExpressionStatement> argAssignments = []

        BlockStatement result = new BlockStatement()
        result.statementLabel = statement.statementLabel

        /* Create temp declarations for all method arguments.
         * Add the declarations and var mapping to tempMapping and tempDeclarations for further reference.
         */
        getArguments(recursiveCall).eachWithIndex { Expression expression, int index ->
            ExpressionStatement tempDeclaration = createTempDeclaration(index, positionMapping, tempMapping, tempDeclarations)
            result.addStatement(tempDeclaration)
        }

        /*
         * Assign the iteration variables their new value before recuring
         */
        getArguments(recursiveCall).eachWithIndex { Expression expression, int index ->
            ExpressionStatement argAssignment = createAssignmentToIterationVariable(expression, index, positionMapping)
            argAssignments.add(argAssignment)
            result.addStatement(argAssignment)
        }

        Set<String> unusedTemps = replaceAllArgUsages(argAssignments, tempMapping)
        for (String temp : unusedTemps) {
            result.statements.remove(tempDeclarations[temp])
        }
        result.addStatement(recurStatement)

        return result
    }

    private ExpressionStatement createAssignmentToIterationVariable(Expression expression, int index, Map<Integer, Map> positionMapping) {
        String argName = positionMapping[index]['name']
        ClassNode argAndTempType = positionMapping[index]['type'] as ClassNode
        ExpressionStatement argAssignment = (ExpressionStatement) assignS(varX(argName, argAndTempType), expression)
        argAssignment
    }

    private ExpressionStatement createTempDeclaration(int index, Map<Integer, Map> positionMapping, Map<String, Map> tempMapping, Map tempDeclarations) {
        String argName = positionMapping[index]['name']
        String tempName = "_${argName}_"
        ClassNode argAndTempType = positionMapping[index]['type'] as ClassNode
        ExpressionStatement tempDeclaration = AstHelper.createVariableAlias(tempName, argAndTempType, argName)
        tempMapping[argName] = [name: tempName, type: argAndTempType]
        tempDeclarations[tempName] = tempDeclaration
        return tempDeclaration
    }

    private List<Expression> getArguments(Expression recursiveCall) {
        if (recursiveCall instanceof MethodCallExpression)
            return ((TupleExpression) ((MethodCallExpression) recursiveCall).arguments).expressions
        if (recursiveCall instanceof StaticMethodCallExpression)
            return ((TupleExpression) ((StaticMethodCallExpression) recursiveCall).arguments).expressions
    }

    private boolean isAMethodCalls(Expression expression) {
        expression.class in [MethodCallExpression, StaticMethodCallExpression]
    }

    private Set<String> replaceAllArgUsages(List<ExpressionStatement> iterationVariablesAssignmentNodes, Map<String, Map> tempMapping) {
        Set<String> unusedTempNames = tempMapping.values().collect { Map nameAndType -> (String) nameAndType['name'] } as Set<String>
        VariableReplacedListener tracker = new UsedVariableTracker()
        for (ExpressionStatement statement : iterationVariablesAssignmentNodes) {
            replaceArgUsageByTempUsage((BinaryExpression) statement.expression, tempMapping, tracker)
        }
        unusedTempNames = unusedTempNames - tracker.usedVariableNames
        return unusedTempNames
    }

    private void replaceArgUsageByTempUsage(BinaryExpression binary, Map tempMapping, UsedVariableTracker tracker) {
        VariableAccessReplacer replacer = new VariableAccessReplacer(nameAndTypeMapping: tempMapping, listener: tracker)
        // Replacement must only happen in binary.rightExpression. It's a hack in VariableExpressionReplacer which takes care of that.
        replacer.replaceIn(binary)
    }
}

@CompileStatic
class UsedVariableTracker implements org.codehaus.groovy.transform.tailrec.VariableReplacedListener {

    final Set<String> usedVariableNames = [] as Set

    @Override
    void variableReplaced(VariableExpression oldVar, VariableExpression newVar) {
        usedVariableNames.add(newVar.name)
    }
}

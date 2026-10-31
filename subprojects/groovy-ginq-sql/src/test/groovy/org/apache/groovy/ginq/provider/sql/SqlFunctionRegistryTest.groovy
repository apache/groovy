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
package org.apache.groovy.ginq.provider.sql

import org.apache.groovy.ginq.provider.sql.ir.SqlBinary
import org.apache.groovy.ginq.provider.sql.ir.SqlCast
import org.apache.groovy.ginq.provider.sql.ir.SqlCastTypes
import org.apache.groovy.ginq.provider.sql.ir.SqlColumn
import org.apache.groovy.ginq.provider.sql.ir.SqlExpr
import org.apache.groovy.ginq.provider.sql.ir.SqlFunction
import org.apache.groovy.ginq.provider.sql.ir.SqlParam
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * The built-in registry is sealed. A copy accepts a new function without
 * changing the mappings the translator uses.
 */
class SqlFunctionRegistryTest {

    @Test
    void testBuiltinNames() {
        def registry = SqlFunctionRegistry.standard()
        assert 'UPPER' == registry.find('toUpperCase', SqlFunctionMapping.Kind.RECEIVER).sqlName
        assert 'COALESCE' == registry.find('coalesce', SqlFunctionMapping.Kind.FREE).sqlName
        assert 'GREATEST' == registry.find('max', SqlFunctionMapping.Kind.MATH).sqlName
        assert 'LEAST' == registry.find('min', SqlFunctionMapping.Kind.MATH).sqlName
        assert registry.find('cast', SqlFunctionMapping.Kind.FREE) != null
        assert registry.find('indexOf', SqlFunctionMapping.Kind.RECEIVER) != null
        assert registry.find('padLeft', SqlFunctionMapping.Kind.RECEIVER) == null
        assert registry.find('signum', SqlFunctionMapping.Kind.RECEIVER) == null
        assert registry.find('max', SqlFunctionMapping.Kind.RECEIVER) == null
    }

    @Test
    void testRegisterOnACopyLeavesTheBuiltinSealed() {
        def extra = SqlFunctionRegistry.standard().copy()
        extra.register(SqlFunctionMapping.receiver('signum', 'SIGN'))
        assert 'SIGN' == extra.find('signum', SqlFunctionMapping.Kind.RECEIVER).sqlName
        assert 'UPPER' == extra.find('toUpperCase', SqlFunctionMapping.Kind.RECEIVER).sqlName
        assert SqlFunctionRegistry.standard().find('signum', SqlFunctionMapping.Kind.RECEIVER) == null

        def duplicate = shouldFail(IllegalArgumentException) {
            extra.register(SqlFunctionMapping.receiver('signum', 'SIGN'))
        }
        assert duplicate.message.contains('already registered')

        def sealed = shouldFail(IllegalStateException) {
            SqlFunctionRegistry.standard().register(SqlFunctionMapping.receiver('signum', 'SIGN'))
        }
        assert sealed.message.contains('sealed')

        def injected = shouldFail(IllegalArgumentException) {
            extra.register(SqlFunctionMapping.receiver('evil', 'UPPER; DROP'))
        }
        assert injected.message.contains('uppercase identifier')
    }

    @Test
    void testRegisteredFunctionTranslatesToItsSqlName() {
        def extra = SqlFunctionRegistry.standard().copy()
        extra.register(SqlFunctionMapping.receiver('signum', 'SIGN'))
        def call = callX(propX(varX('e'), 'salary'), 'signum')
        def expr = extra.find('signum', SqlFunctionMapping.Kind.RECEIVER).translate(call, [], recordingSite())
        assert expr instanceof SqlFunction
        assert 'SIGN' == expr.name
        assert 1 == expr.args.size()
        assert expr.args[0] instanceof SqlColumn
    }

    @Test
    void testCastAndIndexOfShapes() {
        def site = recordingSite()
        def castCall = callX(varX('this'), 'cast', args(propX(varX('e'), 'id'), constX(' decimal(10, 2) ')))
        def castExpr = SqlFunctionRegistry.standard().find('cast', SqlFunctionMapping.Kind.FREE)
                .translate(castCall, [propX(varX('e'), 'id'), constX(' decimal(10, 2) ')], site)
        assert castExpr instanceof SqlCast
        assert 'DECIMAL(10,2)' == castExpr.sqlType

        def indexCall = callX(propX(varX('e'), 'name'), 'indexOf', args(constX('li')))
        def indexExpr = SqlFunctionRegistry.standard().find('indexOf', SqlFunctionMapping.Kind.RECEIVER)
                .translate(indexCall, [constX('li')], site)
        assert indexExpr instanceof SqlBinary
        assert '-' == indexExpr.op
        assert indexExpr.left instanceof SqlFunction
        assert 'POSITION' == indexExpr.left.name
    }

    @Test
    void testCastTypeWhitelist() {
        assert 'DECIMAL' == SqlCastTypes.canonical('decimal')
        assert 'DECIMAL(10,2)' == SqlCastTypes.canonical(' decimal(10, 2) ')
        assert 'DOUBLE PRECISION' == SqlCastTypes.canonical('double precision')
        assert 'VARCHAR(8)' == SqlCastTypes.canonical('varchar(08)')
        assert 'CHARACTER VARYING(20)' == SqlCastTypes.canonical('character varying(20)')
        assert null == SqlCastTypes.canonical('JAVA_OBJECT')
        assert null == SqlCastTypes.canonical('VARCHAR(10); drop table e')
        assert null == SqlCastTypes.canonical('INT(11)')
        assert null == SqlCastTypes.canonical('DECIMAL(2,10)')
        assert null == SqlCastTypes.canonical('VARCHAR(0)')
        assert null == SqlCastTypes.canonical('TIMESTAMP(6)')
        assert null == SqlCastTypes.canonical(null)
        assert null == SqlCastTypes.canonical('   ')

        def rejected = shouldFail(IllegalArgumentException) {
            new SqlCast(new SqlColumn('e', 'id'), 'INTEGER); DROP TABLE employees')
        }
        assert rejected.message.contains('Unsupported SQL cast type')
    }

    private static SqlFunctionSite recordingSite() {
        return new SqlFunctionSite() {
            @Override
            SqlExpr expr(Expression expression) {
                if (expression instanceof PropertyExpression) {
                    return new SqlColumn(expression.objectExpression.text, expression.propertyAsString)
                }
                if (expression instanceof ConstantExpression) {
                    return new SqlParam(expression)
                }
                throw new AssertionError(expression.text)
            }

            @Override
            void fail(String message, ASTNode node) {
                throw new IllegalArgumentException(message)
            }

            @Override
            SqlExpr like(MethodCallExpression call, List<Expression> args, boolean matchBefore, boolean matchAfter) {
                return null
            }

            @Override
            SqlExpr substring(MethodCallExpression call, List<Expression> args) {
                return null
            }
        }
    }
}

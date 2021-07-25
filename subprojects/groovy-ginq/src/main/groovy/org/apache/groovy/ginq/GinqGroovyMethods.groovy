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
package org.apache.groovy.ginq

import groovy.transform.CompileStatic
import org.apache.groovy.ginq.dsl.GinqAstBuilder
import org.apache.groovy.ginq.dsl.GinqAstOptimizer
import org.apache.groovy.ginq.dsl.GinqAstVisitor
import org.apache.groovy.ginq.dsl.expression.AbstractGinqExpression
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.apache.groovy.ginq.provider.collection.GinqAstWalker
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.macro.runtime.Macro
import org.codehaus.groovy.macro.runtime.MacroContext
import org.codehaus.groovy.syntax.SyntaxException

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
/**
 * Declare GINQ macro methods
 *
 * @since 4.0.0
 */
@Incubating
@CompileStatic
class GinqGroovyMethods {
    /**
     * Transform GINQ code to target method invocation
     *
     * @param ctx the macro context
     * @param ginqClosureExpression hold the GINQ code
     * @return target method invocation
     * @since 4.0.0
     */
    @Macro
    static Expression GQ(final MacroContext ctx, final ClosureExpression ginqClosureExpression) {
        GQ(ctx, null, ginqClosureExpression)
    }

    /**
     * Represents the abbreviation of {@code GQ {...}.toList()}, which is very useful when used as list comprehension
     *
     * @param ctx the macro context
     * @param ginqClosureExpression hold the GINQ code
     * @return target method invocation
     * @since 4.0.0
     */
    @Macro
    static Expression GQL(final MacroContext ctx, final ClosureExpression ginqClosureExpression) {
        GQL(ctx, null, ginqClosureExpression)
    }

    /**
     * Transform GINQ code to target method invocation
     *
     * @param ctx the macro context
     * @param ginqConfigurationMapExpression specify the configuration for GINQ, e.g. {@code astWalker}, {@code optimize}, {@code parallel}
     * @param ginqClosureExpression hold the GINQ code
     * @return target method invocation
     * @since 4.0.0
     */
    @Macro
    static Expression GQ(final MacroContext ctx, final MapExpression ginqConfigurationMapExpression, final ClosureExpression ginqClosureExpression) {
        transformGinqCode(ctx.sourceUnit, ginqConfigurationMapExpression, ginqClosureExpression.code)
    }

    /**
     * Represents the abbreviation of {@code GQ {...}.toList()}, which is very useful when used as list comprehension
     *
     * @param ctx the macro context
     * @param ginqConfigurationMapExpression specify the configuration for GINQ, e.g. {@code astWalker}, {@code optimize}, {@code parallel}
     * @param ginqClosureExpression hold the GINQ code
     * @return target method invocation
     * @since 4.0.0
     */
    @Macro
    static Expression GQL(final MacroContext ctx, final MapExpression ginqConfigurationMapExpression, final ClosureExpression ginqClosureExpression) {
        callX(GQ(ctx, ginqConfigurationMapExpression, ginqClosureExpression), 'toList')
    }

    static Expression transformGinqCode(SourceUnit sourceUnit, MapExpression ginqConfigurationMapExpression, Statement code) {
        GinqAstBuilder ginqAstBuilder = new GinqAstBuilder(sourceUnit)

        AbstractGinqExpression ginqExpression = ginqAstBuilder.buildAST(code)

        Map<String, String> configuration = createConfiguration(sourceUnit, ginqConfigurationMapExpression)

        if (ginqExpression instanceof GinqExpression && TRUE_STR == configuration.get(CONF_OPTIMIZE, TRUE_STR)) {
            GinqAstOptimizer ginqAstOptimizer = new GinqAstOptimizer()
            ginqAstOptimizer.visitGinqExpression(ginqExpression)
        }

        Class<?> clazz = GinqGroovyMethods.class.classLoader.loadClass(configuration.get(CONF_AST_WALKER, DEFAULT_AST_WALKER_CLASS_NAME))
        GinqAstVisitor ginqAstWalker = (GinqAstVisitor) clazz.getDeclaredConstructor(SourceUnit.class).newInstance(sourceUnit)
        ginqAstWalker.setConfiguration(configuration)

        return (Expression) ginqAstWalker.visit(ginqExpression)
    }

    private static Map<String, String> createConfiguration(SourceUnit sourceUnit, MapExpression ginqConfigurationMapExpression) {
        Map<String, String> configuration = [:]
        if (!ginqConfigurationMapExpression) return configuration

        for (MapEntryExpression mapEntryExpression : ginqConfigurationMapExpression.getMapEntryExpressions()) {
            def conf = mapEntryExpression.keyExpression.text
            if (conf !in CONF_LIST) {
                collectErrors("Invalid option: ${conf}. (supported options: ${CONF_LIST})", mapEntryExpression.keyExpression, sourceUnit)
            }
            configuration.put(conf, mapEntryExpression.valueExpression.text)
        }
        return configuration
    }

    private static void collectErrors(String message, ASTNode astNode, SourceUnit sourceUnit) {
        InvalidOptionException invalidOptionException = new InvalidOptionException(message)
        SyntaxException e = new SyntaxException(
                invalidOptionException.getMessage(),
                invalidOptionException,
                astNode.lineNumber,
                astNode.columnNumber)
        sourceUnit.getErrorCollector().addFatalError(new SyntaxErrorMessage(e, sourceUnit))
    }

    private GinqGroovyMethods() {}

    public static final String CONF_PARALLEL = 'parallel'
    private static final String CONF_AST_WALKER = 'astWalker'
    private static final String CONF_OPTIMIZE = 'optimize'
    private static final List<String> CONF_LIST = [CONF_PARALLEL, CONF_AST_WALKER, CONF_OPTIMIZE]
    private static final String DEFAULT_AST_WALKER_CLASS_NAME = GinqAstWalker.class.name
    private static final String TRUE_STR = 'true'
}

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
import org.apache.groovy.ginq.dsl.GinqAstVisitor
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.apache.groovy.ginq.provider.collection.GinqAstWalker
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.macro.runtime.Macro
import org.codehaus.groovy.macro.runtime.MacroContext

/**
 * Declare GINQ macro methods
 *
 * @since 4.0.0
 */
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
        GQ(ctx, new ConstantExpression(GinqAstWalker.class.name), ginqClosureExpression)
    }

    /**
     * Transform GINQ code to target method invocation
     *
     * @param ctx the macro context
     * @param ginqAstWalkerFullClassNameConstantExpression specify the qualified class name of GINQ AST walker to generate target method invocation
     * @param ginqClosureExpression hold the GINQ code
     * @return target method invocation
     * @since 4.0.0
     */
    @Macro
    static Expression GQ(final MacroContext ctx, final ConstantExpression ginqAstWalkerFullClassNameConstantExpression, final ClosureExpression ginqClosureExpression) {
        return transformGinqCode(ctx.sourceUnit, ginqAstWalkerFullClassNameConstantExpression, ginqClosureExpression.code)
    }

    static Expression transformGinqCode(SourceUnit sourceUnit, ConstantExpression ginqAstWalkerFullClassNameConstantExpression, Statement code) {
        GinqAstBuilder ginqAstBuilder = new GinqAstBuilder(sourceUnit)
        code.visit(ginqAstBuilder)
        GinqExpression ginqExpression = ginqAstBuilder.getGinqExpression()

        Class<?> clazz = GinqGroovyMethods.class.classLoader.loadClass(ginqAstWalkerFullClassNameConstantExpression.text)
        GinqAstVisitor ginqAstWalker = (GinqAstVisitor) clazz.getDeclaredConstructor(SourceUnit.class).newInstance(sourceUnit)

        return (Expression) ginqAstWalker.visitGinqExpression(ginqExpression)
    }

    private GinqGroovyMethods() {}
}

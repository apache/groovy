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

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.groovy.ginq.dsl.expression.JoinExpression
import org.apache.groovy.ginq.provider.collection.GinqAstWalker
import org.apache.groovy.ginq.provider.collection.runtime.Queryable
import org.apache.groovy.ginq.provider.sql.runtime.SqlGinqRuntime
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.SourceUnit

import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX

/**
 * Runs the in-memory tail of a mixed-mode query. A join that the database
 * already aligned is paired by index, so a null alias stays null. Every
 * other clause is the collection provider's.
 */
@CompileStatic
@PackageScope
class MixedTailWalker extends GinqAstWalker {

    MixedTailWalker(SourceUnit sourceUnit) {
        super(sourceUnit)
    }

    @Override
    MethodCallExpression visitJoinExpression(JoinExpression joinExpression) {
        if (!Boolean.TRUE.equals(joinExpression.getNodeMetaData(GinqToSqlTranslator.INDEX_ALIGNED_JOIN))) {
            return super.visitJoinExpression(joinExpression)
        }
        Expression receiver = (Expression) joinExpression.getNodeMetaData(METHOD_CALL_RECEIVER)
        if (receiver == null) {
            throw new GroovyBugError('index-aligned join has no receiver')
        }
        MethodCallExpression right = callX(classX(QUERYABLE_TYPE), 'from', args(joinExpression.dataSourceExpr))
        MethodCallExpression zip = callX(classX(SQL_GINQ_RUNTIME_TYPE), 'indexZip', args(receiver, right))
        zip.setSourcePosition(joinExpression)
        return zip
    }

    /**
     * Same metadata key {@code GinqAstWalker} stores the current receiver under.
     */
    private static final String METHOD_CALL_RECEIVER = '__METHOD_CALL_RECEIVER'
    private static final ClassNode QUERYABLE_TYPE = ClassHelper.makeCached(Queryable)
    private static final ClassNode SQL_GINQ_RUNTIME_TYPE = ClassHelper.makeCached(SqlGinqRuntime)
}

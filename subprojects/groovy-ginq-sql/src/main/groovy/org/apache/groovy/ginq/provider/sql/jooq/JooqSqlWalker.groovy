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
package org.apache.groovy.ginq.provider.sql.jooq

import groovy.transform.CompileStatic
import org.apache.groovy.ginq.dsl.expression.AbstractGinqExpression
import org.apache.groovy.ginq.provider.sql.GinqSqlWalker
import org.apache.groovy.ginq.provider.sql.ir.SqlQueryNode
import org.apache.groovy.ginq.provider.sql.render.RenderedSql
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.control.SourceUnit

/**
 * The {@code jooq-sql} provider: identical to the native provider except the
 * SQL is rendered by jOOQ for the {@code dialect} option's
 * {@link org.jooq.SQLDialect}, giving dialect-aware SQL, function mapping and
 * limit/offset rendering. jOOQ is required on the compile classpath only.
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
class JooqSqlWalker extends GinqSqlWalker {

    JooqSqlWalker(SourceUnit sourceUnit) {
        super(sourceUnit)
    }

    @Override
    protected String getProviderName() {
        return 'jooq-sql'
    }

    @Override
    protected void validateOptions(AbstractGinqExpression expression) {
        // `dialect` is supported by this provider
    }

    @Override
    protected RenderedSql renderSql(SqlQueryNode queryNode, AbstractGinqExpression expression) {
        String dialectName = getConfiguration().get(JooqSqlGinqProvider.CONF_DIALECT)
        try {
            return JooqRenderer.render(queryNode, dialectName)
        } catch (NoClassDefFoundError ignore) {
            error('the jooq-sql provider requires org.jooq:jooq on the compile classpath', expression)
        } catch (IllegalArgumentException e) {
            error(e.message, expression)
        }
        return null // unreachable: error() reports fatally
    }
}

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
package org.apache.groovy.ginq.provider.sql.jooq;

import org.apache.groovy.ginq.provider.GinqProvider;
import org.apache.groovy.ginq.provider.sql.NativeSqlGinqProvider;
import org.apache.groovy.lang.annotation.Incubating;

import java.util.List;

/**
 * Registers the {@code jooq-sql} GINQ provider. This class deliberately
 * references no jOOQ types so it loads even when jOOQ is absent.
 * {@link #unavailableReason(ClassLoader)} probes the compile classpath, and
 * selection fails with a message naming {@code org.jooq:jooq} when jOOQ
 * cannot be loaded.
 *
 * @since 7.0.0
 */
@Incubating
public class JooqSqlGinqProvider implements GinqProvider {
    /**
     * The option naming the jOOQ {@code SQLDialect} to render for.
     */
    public static final String CONF_DIALECT = "dialect";

    /**
     * Reported when {@code org.jooq.SQLDialect} cannot be loaded.
     */
    static final String MISSING_JOOQ_MESSAGE =
            "the jooq-sql provider requires org.jooq:jooq on the compile classpath";

    @Override
    public String getName() {
        return "jooq-sql";
    }

    @Override
    public String getAstWalkerClassName() {
        return "org.apache.groovy.ginq.provider.sql.jooq.JooqSqlWalker";
    }

    @Override
    public List<String> getAdditionalOptionNames() {
        return List.of(CONF_DIALECT, NativeSqlGinqProvider.CONF_MIXED_MODE);
    }

    /**
     * jOOQ is optional; {@code groovy-sql} is required to execute the rendered SQL.
     */
    @Override
    public String unavailableReason(ClassLoader loader) {
        if (!GinqProvider.canLoad(loader, "org.jooq.SQLDialect")) {
            return MISSING_JOOQ_MESSAGE;
        }
        if (!GinqProvider.canLoad(loader, "groovy.sql.Sql")) {
            return "the jooq-sql provider requires org.apache.groovy:groovy-sql on the compile classpath";
        }
        return null;
    }
}

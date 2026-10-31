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
package org.apache.groovy.ginq.provider.sql;

import org.apache.groovy.ginq.provider.GinqProvider;
import org.apache.groovy.lang.annotation.Incubating;

import java.util.List;

/**
 * Registers the dependency-free {@code native-sql} GINQ provider.
 *
 * @since 6.0.0
 */
@Incubating
public class NativeSqlGinqProvider implements GinqProvider {
    /**
     * Opt-in switch for mixed-mode execution. Absent or {@code false} keeps
     * strict mode: an untranslatable construct is a compile error.
     * {@code true} pushes the translatable clause prefix to the database and
     * finishes the rest in memory.
     */
    public static final String CONF_MIXED_MODE = "mixedMode";

    /**
     * Names a built-in native dialect ({@code ansi}, {@code h2}, {@code postgres},
     * {@code mysql}, {@code mariadb}, {@code sqlite}). Absent means ANSI.
     * Other databases use the {@code jooq-sql} provider.
     */
    public static final String CONF_DIALECT = "dialect";

    @Override
    public String getName() {
        return "native-sql";
    }

    @Override
    public String getAstWalkerClassName() {
        return "org.apache.groovy.ginq.provider.sql.GinqSqlWalker";
    }

    @Override
    public List<String> getAdditionalOptionNames() {
        return List.of(CONF_MIXED_MODE, CONF_DIALECT);
    }
}

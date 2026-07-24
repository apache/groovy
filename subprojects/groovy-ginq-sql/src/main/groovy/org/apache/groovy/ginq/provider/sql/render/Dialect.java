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
package org.apache.groovy.ginq.provider.sql.render;

import org.apache.groovy.lang.annotation.Incubating;

/**
 * Abstracts the dialect-specific concerns of the native SQL renderer.
 * The defaults produce ANSI SQL.
 *
 * @since 6.0.0
 */
@Incubating
public interface Dialect {
    /**
     * Renders an identifier, e.g. a table name, table alias or column name.
     * The default applies no quoting so identifiers follow the database's
     * case-folding of unquoted DDL identifiers.
     *
     * @param identifier the identifier
     * @return the rendered identifier
     */
    default String identifier(String identifier) {
        return identifier;
    }

    /**
     * Maps an ANSI function name, e.g. {@code UPPER} or {@code SUM},
     * to its dialect-specific counterpart.
     *
     * @param ansiName the ANSI function name
     * @return the dialect-specific function name
     */
    default String functionName(String ansiName) {
        return ansiName;
    }

    /**
     * Returns the set operator subtracting one query result from another.
     *
     * @return the operator text, {@code EXCEPT} by default
     */
    default String minusOperator() {
        return "EXCEPT";
    }
}

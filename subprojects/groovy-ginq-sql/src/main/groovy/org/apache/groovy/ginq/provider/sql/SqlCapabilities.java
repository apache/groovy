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

import org.apache.groovy.lang.annotation.Incubating;

/**
 * The dialect facts the translator needs while it is still deciding which
 * clauses can become SQL. Rendering stays on the dialect; this only answers
 * whether a function may be pushed down.
 *
 * @since 7.0.0
 */
@Incubating
public final class SqlCapabilities {
    private final String dialectName;

    private SqlCapabilities(String dialectName) {
        this.dialectName = dialectName;
    }

    /**
     * Capabilities for the configured dialect name. A null or empty name is ANSI.
     *
     * @param dialectName the {@code dialect} option, a jOOQ {@code SQLDialect} name, or {@code null}
     * @return the capabilities
     */
    public static SqlCapabilities of(String dialectName) {
        return new SqlCapabilities(FunctionSupport.canonical(dialectName));
    }

    /**
     * Whether an ANSI function may be emitted for this dialect.
     *
     * @param ansiName the function name
     * @return {@code false} when the dialect cannot render it
     */
    public boolean supportsFunction(String ansiName) {
        return FunctionSupport.supports(dialectName, ansiName);
    }

    /**
     * The dialect family, for diagnostics.
     *
     * @return the canonical dialect name
     */
    public String dialectName() {
        return dialectName;
    }
}

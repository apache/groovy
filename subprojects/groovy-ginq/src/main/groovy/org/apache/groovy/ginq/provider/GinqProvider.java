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
package org.apache.groovy.ginq.provider;

import org.apache.groovy.lang.annotation.Incubating;

/**
 * Represents a named GINQ provider, i.e. an execution back-end for GINQ queries,
 * discovered via {@link java.util.ServiceLoader}. A provider maps a short name,
 * usable as the {@code provider} option of {@code GQ}/{@code GQL}/{@code @GQ},
 * to the {@link org.apache.groovy.ginq.dsl.GinqAstVisitor} implementation that
 * transforms GINQ queries for that back-end.
 * <p>
 * The {@code collection} provider is built in; provider modules, e.g. {@code groovy-ginq-sql},
 * register additional implementations via
 * {@code META-INF/services/org.apache.groovy.ginq.provider.GinqProvider}.
 *
 * @since 6.0.0
 */
@Incubating
public interface GinqProvider {
    /**
     * Returns the provider name, e.g. {@code native-sql}.
     *
     * @return the provider name
     */
    String getName();

    /**
     * Returns the fully qualified name of the provider's
     * {@link org.apache.groovy.ginq.dsl.GinqAstVisitor} implementation.
     *
     * @return the AST walker class name
     */
    String getAstWalkerClassName();
}

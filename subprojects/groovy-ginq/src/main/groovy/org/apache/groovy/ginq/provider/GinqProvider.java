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

import org.apache.groovy.ginq.dsl.GinqAstVisitor;
import org.apache.groovy.lang.annotation.Incubating;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Represents a named GINQ provider, i.e. an execution back-end for GINQ queries,
 * discovered via {@link ServiceLoader}. A provider maps a short name,
 * usable as the {@code provider} option of {@code GQ}/{@code GQL}/{@code @GQ},
 * to the {@link GinqAstVisitor} implementation that
 * transforms GINQ queries for that back-end.
 * <p>
 * The {@code collection} provider is built in; provider modules, e.g. {@code groovy-ginq-sql},
 * register additional implementations via
 * {@code META-INF/services/org.apache.groovy.ginq.provider.GinqProvider}.
 *
 * @since 7.0.0
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
     * {@link GinqAstVisitor} implementation.
     *
     * @return the AST walker class name
     */
    String getAstWalkerClassName();

    /**
     * Returns the names of provider-specific configuration options, e.g.
     * {@code dialect}, accepted in addition to the standard GINQ options.
     * Registering an option here only relaxes validation; interpreting it
     * is up to the provider's AST walker.
     *
     * @return the additional option names, empty by default
     */
    default List<String> getAdditionalOptionNames() {
        return Collections.emptyList();
    }

    /**
     * Checks whether this provider's dependencies can be loaded from {@code loader}.
     * Providers with optional dependencies, such as jOOQ, probe with
     * {@link #canLoad(ClassLoader, String)} and return a message naming the
     * missing artifact. The default implementation has no extra dependencies
     * and returns {@code null}.
     * <p>
     * {@code provider: 'sql'} skips a provider that reports a missing dependency
     * and tries the next candidate. An explicit provider name that reports one
     * is rejected with that message, before its AST walker is loaded.
     *
     * @param loader the class loader that will load the provider implementation
     * @return {@code null} when the provider can be selected, otherwise the reason it cannot
     * @since 7.0.0
     */
    default String unavailableReason(ClassLoader loader) {
        return null;
    }

    /**
     * Returns whether {@code className} can be loaded from {@code loader}
     * without initializing it. A missing class or a linkage error means the
     * dependency is not usable. Provider implementations use this from
     * {@link #unavailableReason(ClassLoader)}.
     *
     * @param loader the class loader to probe; {@code null} is the bootstrap loader
     * @param className the binary name of a class from the dependency
     * @return {@code true} when the class can be loaded
     * @since 7.0.0
     */
    static boolean canLoad(ClassLoader loader, String className) {
        try {
            Class.forName(className, false, loader);
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }
}

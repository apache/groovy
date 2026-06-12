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
package groovy.junit6.plugin

import groovy.transform.CompileStatic
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Delegate object for {@link GroovyEnabledIf} and {@link GroovyDisabledIf} closures,
 * providing convenient access to environment, system properties, and JUnit context.
 *
 * @since 6.0.0
 */
@CompileStatic
class ConditionEvaluationContext {

    /**
     * Process environment variables visible to the current JVM.
     */
    final Map<String, String> systemEnvironment
    /**
     * Java system properties visible to the current JVM.
     */
    final Properties systemProperties
    /**
     * Current Java feature version, for example {@code 17} or {@code 21}.
     */
    final int javaVersion
    /**
     * Tags associated with the current JUnit element.
     */
    final Set<String> junitTags
    /**
     * Display name of the current JUnit element.
     */
    final String junitDisplayName
    /**
     * Unique identifier of the current JUnit element.
     */
    final String junitUniqueId

    /**
     * Creates a closure delegate backed by the supplied JUnit extension context.
     *
     * @param extensionContext the JUnit context currently being evaluated
     */
    ConditionEvaluationContext(ExtensionContext extensionContext) {
        systemEnvironment = System.getenv()
        systemProperties = System.properties
        javaVersion = Runtime.version().feature()
        junitTags = extensionContext.tags
        junitDisplayName = extensionContext.displayName
        junitUniqueId = extensionContext.uniqueId
    }
}

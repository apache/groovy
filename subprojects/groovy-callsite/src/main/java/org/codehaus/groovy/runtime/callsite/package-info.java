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

/**
 * Classic (non-{@code invokedynamic}) call-site caching.
 * <p>
 * <b>Primary purpose (GROOVY-11158):</b> preserve runtime binary
 * compatibility for classes compiled by Groovy 4 and Groovy 5 (and by
 * earlier releases that always used classic call sites). Those classes
 * link against {@link CallSiteArray} and {@link CallSite}; with this
 * optional module on the classpath they must still load and execute on
 * Groovy 6. The public linkage surface of those types is stable for that
 * reason.
 * <p>
 * The same types also support compiling with {@code indy} disabled.
 * Groovy 6 core uses invokedynamic for new compilation by default and
 * does not depend on this package. Add {@code groovy-callsite} only when
 * classic bytecode must still be produced or executed.
 * <p>
 * This entire package is deprecated and planned for removal in a future
 * major version.
 *
 * @deprecated Prefer invokedynamic call sites; do not use for new code.
 * @since 1.5 (relocated to the {@code groovy-callsite} module in 6.0)
 */
@Deprecated
package org.codehaus.groovy.runtime.callsite;

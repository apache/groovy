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
 * Java 8 VM plugin. Compatibility layer for Java 8 features and core implementation of {@code invokedynamic} (Indy) support.
 * <p>
 * This package contains the runtime infrastructure for Groovy's dynamic method dispatch using the {@code invokedynamic}
 * instruction. Key components include:
 * <ul>
 *   <li>{@link org.codehaus.groovy.vmplugin.v8.IndyInterface}: The entry point for bootstrap methods and call-site optimization logic.</li>
 *   <li>{@link org.codehaus.groovy.vmplugin.v8.CacheableCallSite}: Manages the multi-level caching hierarchy (PIC, MRU, LRU) to minimize dispatch overhead.</li>
 *   <li>{@link org.codehaus.groovy.vmplugin.v8.Selector}: Responsible for dynamic method selection, handle creation, and guard generation.</li>
 * </ul>
 */
package org.codehaus.groovy.vmplugin.v8;

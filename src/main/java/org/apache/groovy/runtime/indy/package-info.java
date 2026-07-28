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
 * Scoped invokedynamic SwitchPoint utilities for the Groovy MOP (GROOVY-12191).
 * <p>
 * One SwitchPoint per class (on {@link org.codehaus.groovy.reflection.ClassInfo})
 * models the class-level MetaClass generation used by monomorphic indy sites.
 * MetaClass-aware policy decides exact-class vs hierarchy fan-out
 * (exact-class is an allow-list):
 * <ul>
 *   <li>EMC, modified custom MetaClass, global EMC, interface and array types
 *       → class + loaded subtypes (incl. array lattice)</li>
 *   <li>hierarchy-local {@code MetaClassImpl} pair, per-instance MetaClass
 *       → exact class only</li>
 *   <li>category enter/leave and unscoped events → bulk retire all class domains</li>
 * </ul>
 * Linked call sites carry a single guard — no process-wide MOP SwitchPoint on
 * the hot path. See {@link IndyInvalidation} for the full decision matrix and
 * the relationship to a future MetaClass-owned SwitchPoint design.
 * <p>
 * Hierarchy fan-out is indexed by {@link ClassHierarchyIndex} so typed MetaClass
 * invalidation cost is proportional to the number of loaded subtypes of the
 * changed type, not to the total number of loaded classes.
 * <p>
 * Install guards via {@link IndyInvalidation#guardWithMopSwitchPoints} (public)
 * or {@code IndyInterface.applyMopSwitchPoints} (production link path). The
 * pre-6.0 process-wide {@code IndyInterface.switchPoint} field has been removed
 * ({@code vmplugin} is internal-by-intent).
 *
 * @since 6.0.0
 */
package org.apache.groovy.runtime.indy;

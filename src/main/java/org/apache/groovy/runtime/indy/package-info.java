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
 * <b>Domain model:</b> one SwitchPoint per {@link groovy.lang.MetaClass}
 * instance (weak identity map in {@link IndyInvalidation}). No SwitchPoint field
 * on {@link groovy.lang.MetaClassImpl}; custom MetaClasses use the same map.
 * When no class-level MetaClass is installed yet, link uses a ClassInfo
 * <em>pending</em> domain (retired on first MetaClass install) — defineClass-
 * safe without an always-invalid SwitchPoint (which would monomorphic-loop).
 * MetaClass-aware policy decides exact-class vs hierarchy fan-out:
 * <ul>
 *   <li>EMC, modified custom MetaClass, global EMC, interface and array types
 *       → class + loaded subtypes (incl. array lattice)</li>
 *   <li>hierarchy-local {@code MetaClassImpl} pair, per-instance MetaClass
 *       → exact class only</li>
 *   <li>category enter/leave and unscoped events → bulk retire class-level
 *       MetaClass domains of loaded types</li>
 * </ul>
 * Hierarchy fan-out exists only for the missing-method walk
 * ({@code MetaClassImpl.findMethodInClassHierarchy}). MetaClassImpl continues
 * to observe ancestor MetaClass state on that path (6.0 decision). See
 * {@link IndyInvalidation}.
 * <p>
 * Hierarchy fan-out is indexed by {@link ClassHierarchyIndex}. Install guards
 * via {@link IndyInvalidation#guardWithMopSwitchPoints} or
 * {@code IndyInterface.applyMopSwitchPoints}.
 *
 * @since 6.0.0
 */
package org.apache.groovy.runtime.indy;

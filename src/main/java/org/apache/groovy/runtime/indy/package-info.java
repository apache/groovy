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
 * One SwitchPoint per class (on {@link org.codehaus.groovy.reflection.ClassInfo}):
 * MetaClass changes retire that class and loaded subtypes (including array-type
 * fan-out for {@code Object[]} and similar); category enter/leave bulk-retires
 * loaded class SwitchPoints. Linked call sites carry a single guard — no
 * process-wide MOP SwitchPoint on the hot path.
 * <p>
 * Hierarchy fan-out is indexed by {@link ClassHierarchyIndex} so typed MetaClass
 * invalidation cost is proportional to the number of loaded subtypes of the
 * changed type, not to the total number of loaded classes.
 * <p>
 * External code that previously guarded on
 * {@link org.codehaus.groovy.vmplugin.v8.IndyInterface#switchPoint} must migrate
 * to {@link org.apache.groovy.runtime.indy.IndyInvalidation#guardWithMopSwitchPoints}:
 * the legacy field is rotated only on category / {@code invalidateCallSites()}
 * bulk events, not on per-class MetaClass changes.
 *
 * @since 6.0.0
 */
package org.apache.groovy.runtime.indy;

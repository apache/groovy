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
 * Intermediate representation for SQL queries derived from GINQ queries: a provider-neutral
 * model consumed by renderers. Compile-time only; never present in generated code.
 * <p>
 * Window calls are {@link SqlWindowCall}: a function
 * plus an {@link SqlWindow} (partition, order, and an
 * optional {@code ROWS} or {@code RANGE} frame). Both renderers visit that node.
 * The translator builds one for each window function registered on the sealed
 * standard registry.
 */
package org.apache.groovy.ginq.provider.sql.ir;

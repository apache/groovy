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
 * SQL provider for GINQ (GEP-26): translates GINQ queries into SQL with bound
 * parameters at compile time and executes them against a relational database at runtime.
 * The native renderer defaults to ANSI and can target a small set of open-source dialects.
 * Strict mode is the default; {@code mixedMode: true} pushes the translatable clause
 * prefix and finishes the rest in memory.
 */
package org.apache.groovy.ginq.provider.sql;

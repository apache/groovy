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
 * GINQ, Groovy Integrated Query.
 *
 * <p>
 * Two front ends share the collection query runtime. {@code GQ}, {@code GQL}, and
 * {@code @GQ} are the SQL-like syntax. {@link groovy.ginq.Ginq} and
 * {@link groovy.ginq.Query} are the fluent, LINQ-style method syntax:
 * {@code where}, {@code select}, {@code orderBy}, {@code groupBy}, {@code join},
 * and {@code aggregate}. Database providers are driven by the SQL-like syntax,
 * which keeps an expression tree. The fluent API runs in memory.
 * </p>
 */
package groovy.ginq;

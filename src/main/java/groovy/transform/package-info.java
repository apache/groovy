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
 * Compile-time AST transformations for code generation and type checking.
 *
 * <p>
 * Key annotations: {@link groovy.transform.CompileStatic @CompileStatic} (static compilation),
 * {@link groovy.transform.TypeChecked @TypeChecked} (type checking),
 * {@link groovy.transform.ToString @ToString}, {@link groovy.transform.EqualsAndHashCode @EqualsAndHashCode},
 * {@link groovy.transform.Immutable @Immutable}, {@link groovy.transform.TupleConstructor @TupleConstructor},
 * {@link groovy.transform.Lazy @Lazy}, {@link groovy.transform.Synchronized @Synchronized},
 * and many others for traits, records, sealing, and property handling.
 * </p>
 *
 * <p>
 * Related subpackages: {@code groovy.transform.builder} (builder patterns),
 * {@code groovy.transform.options} (property customization),
 * {@code groovy.transform.stc} (type checking hints).
 * </p>
 */
package groovy.transform;

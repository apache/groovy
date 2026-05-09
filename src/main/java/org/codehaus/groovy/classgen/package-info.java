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
 * Provides classes for generating Java bytecode from Groovy Abstract Syntax Trees (AST).
 * <p>
 * This package contains the core class generation infrastructure that transforms Groovy's AST
 * representation into executable Java bytecode using the ASM library. The main components include:
 * <ul>
 *   <li>AST visitors that traverse and transform Groovy class structures</li>
 *   <li>Bytecode generators that emit JVM instructions</li>
 *   <li>Verifiers that ensure code correctness and compliance with Java/Groovy semantics</li>
 *   <li>Support for inner classes, enums, annotations, and other Groovy language features</li>
 * </ul>
 * <p>
 * The primary entry point for bytecode generation is {@link org.codehaus.groovy.classgen.AsmClassGenerator},
 * which coordinates the entire class generation process. Various visitor classes handle specific aspects
 * such as annotation processing, inner class resolution, variable scoping, and verification.
 *
 * @since 1.0
 */
package org.codehaus.groovy.classgen;

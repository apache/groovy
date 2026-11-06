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
 * Joint-compilation support integrating Groovy compilation units with Java
 * stub generation and javac execution.
 * <p>
 * Stub sources may be kept in memory ({@code memStub}). When
 * {@code memClassOutput} is enabled as well, javac class files stay on the
 * heap too and are published on the compilation class loader, so a joint
 * compile needs neither a stub directory nor a class-output directory.
 * Resolution uses that class loader. A compile stages bytes and commits
 * them only after output succeeds, so a failed or partial compile does not
 * discard the previous classes. Disk {@code stubDir} and target-directory
 * output remain the default. Only the default javac compiler supports
 * in-memory class output.
 */
package org.codehaus.groovy.tools.javac;

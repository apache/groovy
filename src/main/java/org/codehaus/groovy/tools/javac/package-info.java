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
 * heap too, so a joint compile needs neither a stub directory nor a
 * class-output directory. Disk {@code stubDir} and target-directory output
 * remain the default.
 */
package org.codehaus.groovy.tools.javac;

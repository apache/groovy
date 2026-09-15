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
 * Incubating SPI so other Apache-2.0 Groovy language servers and
 * {@code groovy-mcp} can sit on groovy-lsp's compile and protocol loop
 * without forking it. A {@link GroovyLspExtension} is discovered with
 * {@code ServiceLoader} or registered in tests. Inject AST in
 * {@code afterCompile}; read folders and extra settings from
 * {@link GroovyLspSession}. There is no second hover/completion SPI.
 * <p>
 * This is not a slot for replacing the Groovy compiler with Eclipse JDT.
 * Joint Java/Groovy servers remain a parallel stack.
 */
@Incubating
package org.apache.groovy.lsp.spi;

import org.apache.groovy.lang.annotation.Incubating;

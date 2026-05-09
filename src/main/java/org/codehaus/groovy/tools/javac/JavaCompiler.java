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
package org.codehaus.groovy.tools.javac;

import org.codehaus.groovy.control.CompilationUnit;

import java.util.List;

/**
 * Strategy interface for compiling Java sources during Groovy joint
 * compilation.
 */
public interface JavaCompiler {
    /**
     * Compiles the supplied Java source files into the context of the provided
     * Groovy compilation unit.
     *
     * @param files the Java source files to compile
     * @param cu the owning Groovy compilation unit
     */
    void compile(List<String> files, CompilationUnit cu);
}

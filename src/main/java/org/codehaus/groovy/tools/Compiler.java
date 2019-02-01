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
package org.codehaus.groovy.tools;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;

import java.io.File;

/**
 * A convenience front end for getting standard compilations done.
 * All compile() routines generate classes to the filesystem.
 */
public class Compiler {
    // TODO: delete this constant?
    public static final Compiler DEFAULT = new Compiler();

    private final CompilerConfiguration configuration;  // Optional configuration data

    /**
     * Initializes the Compiler with default configuration.
     */
    public Compiler() {
        configuration = null;
    }


    /**
     * Initializes the Compiler with the specified configuration.
     */
    public Compiler(CompilerConfiguration configuration) {
        this.configuration = configuration;
    }


    /**
     * Compiles a single File.
     */
    public void compile(File file) throws CompilationFailedException {
        CompilationUnit unit = new CompilationUnit(configuration);
        unit.addSource(file);
        unit.compile();
    }


    /**
     * Compiles a series of Files.
     */
    public void compile(File[] files) throws CompilationFailedException {
        CompilationUnit unit = new CompilationUnit(configuration);
        unit.addSources(files);
        unit.compile();
    }


    /**
     * Compiles a series of Files from file names.
     */
    public void compile(String[] files) throws CompilationFailedException {
        CompilationUnit unit = new CompilationUnit(configuration);
        unit.addSources(files);
        unit.compile();
    }


    /**
     * Compiles a string of code.
     */
    public void compile(String name, String code) throws CompilationFailedException {
        CompilationUnit unit = new CompilationUnit(configuration);
        unit.addSource(new SourceUnit(name, code, configuration, unit.getClassLoader(), unit.getErrorCollector()));
        unit.compile();
    }

}





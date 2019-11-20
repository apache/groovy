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
package org.codehaus.groovy.control;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.syntax.ParserException;
import org.codehaus.groovy.syntax.Reduction;
import org.codehaus.groovy.util.CharSequenceReader;

import java.io.Reader;

/**
 * A simple extension point to allow us to switch between the classic Groovy parser and the new Antlr based parser(s).
 */
public interface ParserPlugin {

    Reduction parseCST(SourceUnit sourceUnit, Reader reader) throws CompilationFailedException;

    ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) throws ParserException;

    static ModuleNode buildAST(CharSequence source, GroovyClassLoader loader, CompilerConfiguration config, ErrorCollector errors) throws ParserException {
        String scriptName = "Script" + System.nanoTime() + ".groovy";
        SourceUnit sourceUnit = new SourceUnit(scriptName, source.toString(), config, loader, errors);

        ParserPlugin parserPlugin = config.getPluginFactory().createParserPlugin();
        return parserPlugin.buildAST(sourceUnit, loader, parserPlugin.parseCST(sourceUnit, new CharSequenceReader(source)));
    }
}

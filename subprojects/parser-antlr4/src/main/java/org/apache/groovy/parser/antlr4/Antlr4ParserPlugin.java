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
package org.apache.groovy.parser.antlr4;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.io.StringReaderSource;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.syntax.ParserException;
import org.codehaus.groovy.syntax.Reduction;

import java.io.IOException;
import java.io.Reader;

/**
 * A parser plugin for the new parser
 *
 * @author  <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on    2016/08/14
 */
public class Antlr4ParserPlugin implements ParserPlugin {
    private ReaderSource readerSource;
    private AstBuilder builder;

    @Override
    public Reduction parseCST(SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
        handleSourceUnit(sourceUnit, reader);
        this.builder = new AstBuilder(sourceUnit, null);
        builder.buildCST();

        return null;
    }

    @Override
    public ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) throws ParserException {
        handleSourceUnit(sourceUnit, null);

        if (null == this.builder) {
            this.builder = new AstBuilder(sourceUnit, classLoader);
        }

        return builder.buildAST();
    }

    private void handleSourceUnit(SourceUnit sourceUnit, Reader reader) {
        try {
            ReaderSource readerSource = sourceUnit.getSource();
            initReaderSource(sourceUnit, reader, readerSource);

            if (null == readerSource || null == readerSource.getReader()) {
                sourceUnit.setSource(this.readerSource);
            }
        } catch (IOException e) {
            sourceUnit.setSource(this.readerSource);
        }
    }

    private void initReaderSource(SourceUnit sourceUnit, Reader reader, ReaderSource readerSource) throws IOException {
        if (null != this.readerSource) {
            return;
        }

        if (null != readerSource && null != readerSource.getReader()) {
            this.readerSource = readerSource;
            return;
        }

        if (null == reader) {
            return;
        }

        this.readerSource = new StringReaderSource(IOGroovyMethods.getText(reader), sourceUnit.getConfiguration());
    }
}

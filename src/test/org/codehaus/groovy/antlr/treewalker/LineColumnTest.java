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
package org.codehaus.groovy.antlr.treewalker;

import antlr.collections.AST;
import junit.framework.TestCase;
import org.codehaus.groovy.antlr.AntlrASTProcessor;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.AntlrASTProcessSnippets;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;

import java.io.StringReader;

/**
 * Test that will assert each node of Source AST has a correct line/column info
 * given a SourceBuffer
 */
public class LineColumnTest extends TestCase {
    public void testSimpleGroovySource() throws Exception {
        doStuff("def foo()\n" +
                "{ print \"Hello World\" }\n" +
                "\n" +
                "foo()");
    }

    public void doStuff(String input) throws Exception {
        GroovyRecognizer parser;
        SourceBuffer sourceBuffer = new SourceBuffer();
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input), sourceBuffer);
        GroovyLexer lexer = new GroovyLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        String[] tokenNames = parser.getTokenNames();
        parser.compilationUnit();
        AST ast = parser.getAST();
        AntlrASTProcessor snippets = new AntlrASTProcessSnippets();
        ast = snippets.process(ast);
        Visitor visitor = new LineColumnChecker(sourceBuffer, tokenNames);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);
        traverser.process(ast);
    }
}

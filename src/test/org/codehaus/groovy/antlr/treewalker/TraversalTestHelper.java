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
import org.codehaus.groovy.antlr.AntlrASTProcessor;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;

public class TraversalTestHelper {
    public String traverse(String input, Class visitorClass) throws Exception {
        return traverse(input, visitorClass, null);
    }


    // todo - the visitor doesn't always take PrintStreams as constructor params!  Could be a more reusable implementation than this...
    public String traverse(String input, Class visitorClass, Boolean extraParam) throws Exception {
        if (!Visitor.class.isAssignableFrom(visitorClass)) {
            throw new RuntimeException("Invalid class for traversal: " + visitorClass.getName());
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
        Class[] paramTypes;
        Object[] params;
        if (extraParam == null) {
            paramTypes = new Class[]{PrintStream.class, String[].class};
            params = new Object[]{new PrintStream(baos), tokenNames};
        } else {
            paramTypes = new Class[]{PrintStream.class, String[].class, Boolean.TYPE};
            params = new Object[]{new PrintStream(baos), tokenNames, extraParam};
        }
        Constructor constructor = visitorClass.getConstructor(paramTypes);
        Visitor visitor = (Visitor) constructor.newInstance(params);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);
        traverser.process(ast);
        return new String(baos.toByteArray());
    }
}

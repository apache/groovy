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
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.custommonkey.xmlunit.XMLUnit;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Test cases for the composite visitor.
 */
public class CompositeVisitorTest extends TestCase {

    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testCompositeTransparency() throws Exception {
        // TODO: add more tests in here (is there a way to share snippets with SourcePrinterTest?)
        assertCompositeTransparency("public abstract class Foo{}");
        assertCompositeTransparency("int[] primes = new int[5]");
        assertCompositeTransparency("if (foo==~\"bar\"){}");
        assertCompositeTransparency("a=12");
        assertCompositeTransparency("def x=1&2");
        assertCompositeTransparency("x&=2");
        assertCompositeTransparency("def z = ~123");
        assertCompositeTransparency("def y = 1 | 2");
        assertCompositeTransparency("y|=2");
        assertCompositeTransparency("def q = 1 >>> 2");
        assertCompositeTransparency("y>>>=2");
        assertCompositeTransparency("def y = true ^ false");
        assertCompositeTransparency("y^=false");
        assertCompositeTransparency("switch(foo){case bar:x=1}");
        assertCompositeTransparency("class Foo{def bar}");
        assertCompositeTransparency("[1,2,3].each{println it}");
        assertCompositeTransparency("def x = foo.bar(mooky) {x,y-> wibble(y,x)}");
        assertCompositeTransparency("1<=>2");
        assertCompositeTransparency("class Foo{Foo(int x) {this()}}");
        assertCompositeTransparency("class Foo{Foo(x) {this()}}");
        assertCompositeTransparency("class Foo {private Foo() {}}");
        assertCompositeTransparency("--b");
        assertCompositeTransparency("1/2");
        assertCompositeTransparency("x/=2");
        assertCompositeTransparency("java.util.Date d = new java.util.Date()");
        assertCompositeTransparency("class Foo extends java.util.Date {}");
        assertCompositeTransparency("foo.bar.mooky()");
        assertCompositeTransparency("package foo.bar");
        assertCompositeTransparency("import java.util.Date");
        assertCompositeTransparency("import java.io.*");
        assertCompositeTransparency("@foo.Bar mooky");
        assertCompositeTransparency("def foo() throws bar.MookyException{}");
        assertCompositeTransparency("def x = \"${foo.bar}\"");
    }

    private void assertCompositeTransparency(String input) throws Exception {
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

        // determine direct result
        Visitor directVisitor = new SourcePrinter(new PrintStream(baos), tokenNames, false);
        AntlrASTProcessor traverser = new SourceCodeTraversal(directVisitor);
        traverser.process(ast);
        String directResult = new String(baos.toByteArray());

        // determine composite result
        baos.reset();
        List wrappedVisitors = new ArrayList();
        wrappedVisitors.add(directVisitor);
        Visitor compositeVisitor = new CompositeVisitor(wrappedVisitors);
        traverser = new SourceCodeTraversal(compositeVisitor);
        traverser.process(ast);
        String compositeResult = new String(baos.toByteArray());

        assertEquals(directResult, compositeResult);
    }

}

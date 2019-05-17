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

import junit.framework.TestCase;

import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;

import static groovy.util.XmlAssert.assertXmlEquals;

/**
 * Testcases for the antlr AST visitor that prints groovy source code nodes as Freemind compatible XML.
 */
public class MindMapPrinterTest extends TestCase implements GroovyTokenTypes{

    private static final String HEADER = "<map version='0.7.1'><node TEXT='AST'>";
    private static final String FOOTER = "</node></map>";

    public void testAbstract() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<node TEXT='MODIFIERS &lt;" + MODIFIERS + "&gt;' POSITION='right' COLOR=\"#000000\">" +
                        "  <node TEXT='public  &lt;" + LITERAL_public + "&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "</node>" +
                        "<node TEXT='abstract  &lt;" + ABSTRACT + "&gt;' POSITION='right' COLOR=\"#006699\"></node>" +
                        "<node TEXT='CLASS_DEF &lt;" + CLASS_DEF + "&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "<node TEXT='an identifier &lt;" + IDENT + "&gt; : Foo' POSITION='right' COLOR=\"#006699\"></node>" +
                        "<node TEXT='EXTENDS_CLAUSE &lt;" + EXTENDS_CLAUSE + "&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "<node TEXT='IMPLEMENTS_CLAUSE &lt;" + IMPLEMENTS_CLAUSE + "&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "<node TEXT='OBJBLOCK &lt;" + OBJBLOCK + "&gt;' POSITION='right' COLOR=\"#006699\"></node>" +
                        FOOTER,
                pretty("public abstract class Foo{}"));

    }

    public void testArrayDeclarator() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<node TEXT='VARIABLE_DEF &lt;" + VARIABLE_DEF + "&gt; : primes' POSITION='right' COLOR=\"#000000\">" +
                        "  <node TEXT='TYPE &lt;" + TYPE + "&gt;' POSITION='right' COLOR=\"#17178B\">" +
                        "    <node TEXT='ARRAY_DECLARATOR &lt;" + ARRAY_DECLARATOR + "&gt; : [' POSITION='right' COLOR=\"#000000\">" +
                        "      <node TEXT='int  &lt;" + LITERAL_int + "&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "    </node>" +
                        "  </node>" +
                        "  <node TEXT='an identifier &lt;" + IDENT + "&gt; : primes' POSITION='right' COLOR=\"#006699\"></node>" +
                        "  <node TEXT='=  &lt;" + ASSIGN + "&gt;' POSITION='right' COLOR=\"#000000\">" +
                        "    <node TEXT='new  &lt;" + LITERAL_new + "&gt;' POSITION='right' COLOR=\"#17178B\">" +
                        "      <node TEXT='int  &lt;" + LITERAL_int + "&gt;' POSITION='right' COLOR=\"#17178B\"></node>" +
                        "      <node TEXT='ARRAY_DECLARATOR &lt;" + ARRAY_DECLARATOR + "&gt; : [' POSITION='right' COLOR=\"#000000\">" +
                        "        <node TEXT='a numeric literal &lt;" + NUM_INT + "&gt; : 5' POSITION='right' COLOR=\"#006699\"></node>" +
                        "      </node>" +
                        "    </node>" +
                        "  </node>" +
                        "</node>" +
                        FOOTER,
                pretty("int[] primes = new int[5]"));
    }

    public void testRegexMatch() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<node TEXT='if  &lt;" + LITERAL_if + "&gt;' POSITION='right' COLOR=\"#17178B\">" +
                        "  <node TEXT='an identifier &lt;" + IDENT + "&gt; : foo' POSITION='right' COLOR=\"#006699\"></node>" +
                        "  <node TEXT='==~  &lt;" + REGEX_MATCH + "&gt;' POSITION='right' COLOR=\"#000000\"></node>" +
                        "  <node TEXT='a string literal &lt;" + STRING_LITERAL + "&gt; : bar' POSITION='right' COLOR=\"#008000\"></node>" +
                        "</node>" +
                        "<node TEXT='SLIST &lt;" + SLIST + "&gt; : {' POSITION='right' COLOR=\"#006699\"></node>" +
                        FOOTER,
                pretty("if (foo==~\"bar\"){}"));
    }

    private String pretty(String input) throws Exception {
        TraversalTestHelper traverser = new TraversalTestHelper();
        return traverser.traverse(input, MindMapPrinter.class);
    }

}

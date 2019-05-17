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

import static groovy.util.XmlAssert.assertXmlEquals;

/**
 * Testcases for the antlr AST visitor that prints groovy source code nodes as HTML.
 */
public class NodeAsHTMLPrinterTest extends TestCase {

    private static final String HEADER = "<html><head></head><body><pre>";
    private static final String FOOTER = "</pre></body></html>";

    public void testAbstract() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<code title='MODIFIERS'><font color='#000000'>" +
                        "  <code title='\"public\"'><font color='#17178B'></font></code>" +
                        "</font></code>" +
                        "<code title='\"abstract\"'><font color='#000000'></font></code>" +
                        "<code title='CLASS_DEF'><font color='#17178B'></font></code>" +
                        "<code title='an identifier'><font color='#000000'></font></code>" +
                        "<code title='EXTENDS_CLAUSE'><font color='#17178B'></font></code>" +
                        "<code title='IMPLEMENTS_CLAUSE'><font color='#17178B'></font></code>" +
                        "<code title='OBJBLOCK'><font color='#000000'></font></code>" +
                        FOOTER,
                pretty("public abstract class Foo{}"));

    }

    public void testArrayDeclarator() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<code title='VARIABLE_DEF'><font color='#000000'>" +
                        "  <code title='TYPE'><font color='#17178B'>" +
                        "    <code title='ARRAY_DECLARATOR'><font color='#000000'>" +
                        "      <code title='\"int\"'><font color='#17178B'></font></code>" +
                        "    </font></code>" +
                        "  </font></code>" +
                        "  <code title='an identifier'><font color='#000000'></font></code>" +
                        "  <code title=\"'='\"><font color='#000000'>" +
                        "    <code title='\"new\"'><font color='#17178B'>" +
                        "      <code title='\"int\"'><font color='#17178B'></font></code>" +
                        "      <code title='ARRAY_DECLARATOR'><font color='#000000'>" +
                        "        <code title='a numeric literal'><font color='#000000'></font></code>" +
                        "      </font></code>" +
                        "    </font></code>" +
                        "  </font></code>" +
                        "</font></code>" +
                        FOOTER,
                pretty("int[] primes = new int[5]"));
    }

    public void testRegexMatch() throws Exception {
        assertXmlEquals(
                HEADER +
                        "<code title='\"if\"'><font color='#17178B'>" +
                        "  <code title='an identifier'><font color='#000000'></font></code>" +
                        "  <code title=\"'==~'\"><font color='#000000'></font></code>" +
                        "  <code title='a string literal'><font color='#008000'></font></code>" +
                        "</font></code>" +
                        "<code title='SLIST'><font color='#000000'></font></code>" +
                        FOOTER,
                pretty("if (foo==~\"bar\"){}"));
    }

    private String pretty(String input) throws Exception {
        TraversalTestHelper traverser = new TraversalTestHelper();
        return traverser.traverse(input, NodeAsHTMLPrinter.class);
    }

}

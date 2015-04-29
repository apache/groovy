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
import org.custommonkey.xmlunit.XMLUnit;

/**
 * Testcases for the antlr AST visitor that prints groovy source code nodes as Pseudo XML.
 */
public class NodePrinterTest extends TestCase {

    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testAbstract() throws Exception {
        assertPseudoXmlEquals("<MODIFIERS><\"public\"></\"public\"></MODIFIERS><\"abstract\"></\"abstract\"><CLASS_DEF></CLASS_DEF><an identifier></an identifier><EXTENDS_CLAUSE></EXTENDS_CLAUSE><IMPLEMENTS_CLAUSE></IMPLEMENTS_CLAUSE><OBJBLOCK></OBJBLOCK>",
                nodify("public abstract class Foo{}"));
    }

    public void testArrayDeclarator() throws Exception {
        assertPseudoXmlEquals(
                "<VARIABLE_DEF><TYPE><ARRAY_DECLARATOR><\"int\"></\"int\"></ARRAY_DECLARATOR></TYPE><an identifier></an identifier><'='><\"new\"><\"int\"></\"int\"></\"new\"><ARRAY_DECLARATOR><a numeric literal></a numeric literal></ARRAY_DECLARATOR></\"new\"></'='></VARIABLE_DEF>",
                nodify("int[] primes = new int[5]"));
    }

    public void testRegexMatch() throws Exception {
        assertPseudoXmlEquals(
                "<\"if\"><an identifier></an identifier><'==~'></'==~'><a string literal></a string literal></\"if\"><SLIST></SLIST>",
                nodify("if (foo==~\"bar\"){}"));
    }

    private void assertPseudoXmlEquals(String expected, String actual) throws Exception {
        assertEquals(expected, actual);
    }

    private String nodify(String input) throws Exception {
        TraversalTestHelper traverser = new TraversalTestHelper();
        return traverser.traverse(input, NodePrinter.class);
    }

}

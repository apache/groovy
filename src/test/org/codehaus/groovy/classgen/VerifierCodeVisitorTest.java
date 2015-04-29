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
package org.codehaus.groovy.classgen;

import junit.framework.TestCase;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.syntax.RuntimeParserException;

/**
 */
public class VerifierCodeVisitorTest extends TestCase {
    public void testValidNames() {
        assertValidName("a");
        assertValidName("a1234");
        assertValidName("a_b_c");
        assertValidName("a____1234");
    }

    public void testInvalidNames() {
        assertInvalidName("1");
        assertInvalidName("100");
        assertInvalidName("1a");
        assertInvalidName("a!");
        assertInvalidName("a.");
        assertInvalidName("$");
    }

    protected void assertValidName(String name) {
        VerifierCodeVisitor.assertValidIdentifier(name, "variable name", new ASTNode());
    }

    protected void assertInvalidName(String name) {
        try {
            VerifierCodeVisitor.assertValidIdentifier(name, "variable name", new ASTNode());
            fail("Should have thrown exception due to invalid name: " + name);
        }
        catch (RuntimeParserException e) {
            System.out.println("Caught invalid exception: " + e);
        }
    }
}

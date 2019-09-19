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
package org.codehaus.groovy.antlr;

import groovy.test.GroovyTestCase;

public class GroovySourceASTTest extends GroovyTestCase {
    GroovySourceAST a;
    GroovySourceAST b;

    protected void setUp() throws Exception {
        a = new GroovySourceAST();
        a.setLine(3);
        a.setColumn(3);

        b = new GroovySourceAST();
        b.setLine(4);
        b.setColumn(2);
    }

    public void testLessThan() throws Exception {
        assertTrue(a.compareTo(b) < 0);
    }

    public void testEquality() throws Exception {
        assertTrue(a.equals(a));
        assertTrue(a.compareTo(a) == 0);
    }

    public void testGreaterThan() throws Exception {
        assertTrue(b.compareTo(a) > 0);
    }
}

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
package groovy.util;

import junit.framework.TestCase;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 * Testing the simple Groovy integration with Eval.
 */
public class EvalTest extends TestCase {
    public void testMeSimple() throws CompilationFailedException {
        Object result = Eval.me("10");
        assertEquals("10", result.toString());
    }

    public void testMeWithSymbolAndObject() throws CompilationFailedException {
        Object result = Eval.me("x", new Integer(10), "x");
        assertEquals("10", result.toString());
    }

    public void testX() throws CompilationFailedException {
        Object result = Eval.x(new Integer(10), "x");
        assertEquals("10", result.toString());
    }

    public void testXY() throws CompilationFailedException {
        Integer ten = new Integer(10);
        Object result = Eval.xy(ten, ten, "x+y");
        assertEquals("20", result.toString());
    }

    public void testXYZ() throws CompilationFailedException {
        Integer ten = new Integer(10);
        Object result = Eval.xyz(ten, ten, ten, "x+y+z");
        assertEquals("30", result.toString());
    }
}

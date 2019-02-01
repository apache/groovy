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
package groovy.lang;

import groovy.util.GroovyTestCase;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.List;

/**
 * Tests the use of the structured Attribute type
 */
public class SequenceTest extends GroovyTestCase {

    public void testConstruction() {
        Sequence sequence = new Sequence(String.class);
        sequence.add("James");
        sequence.add("Bob");

        assertEquals("Size", 2, sequence.size());
        assertEquals("Element", "James", sequence.get(0));
        assertEquals("Element", "Bob", sequence.get(1));

        // now lets try some methods on each item in the list
        List answer = (List) InvokerHelper.invokeMethod(sequence, "startsWith", new Object[]{"Ja"});
        assertArrayEquals(new Object[]{Boolean.TRUE, Boolean.FALSE}, answer.toArray());

        answer = (List) InvokerHelper.invokeMethod(sequence, "length", null);
        assertArrayEquals(new Object[]{new Integer(5), new Integer(3)}, answer.toArray());
    }

    public void testAddingWrongTypeFails() {
        try {
            Sequence sequence = new Sequence(String.class);
            sequence.add(new Integer(5));

            fail("Should have thrown exception");
        }
        catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e);
        }
    }

    public void testAddingNullFails() {
        try {
            Sequence sequence = new Sequence(String.class);
            sequence.add(null);

            fail("Should have thrown exception");
        }
        catch (NullPointerException e) {
            System.out.println("Caught: " + e);
        }
    }

}

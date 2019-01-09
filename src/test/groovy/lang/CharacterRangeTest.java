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
/**
 *
 */
package groovy.lang;

import junit.framework.TestCase;

import java.util.Iterator;

/**
 * Provides a few unit tests for {@link ObjectRange}s of {@link Character}s.  More tests are needed.
 */
public class CharacterRangeTest extends TestCase {
    /**
     * The range to test.
     */
    private ObjectRange range = null;

    /**
     * The first character in the range.
     */
    private final Character FROM = new Character('a');

    /**
     * The last character in the range.
     */
    private final Character TO = new Character('d');

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        range = new ObjectRange(FROM, TO);
    }

    /**
     * Tests iterating through the range.
     */
    public void testIterate() {
        Iterator iter = range.iterator();
        assertEquals(FROM, iter.next());
        for (char expected = (char) (FROM.charValue() + 1); expected <= TO.charValue(); expected++) {
            assertEquals(expected, ((Character)iter.next()).charValue());
        }
    }

    /**
     * Tests getting the 'from' value.
     */
    public void testGetFrom() {
        assertEquals("wrong 'from' value", FROM, range.getFrom());
    }

    /**
     * Tests getting the 'to' value.
     */
    public void testGetTo() {
        assertEquals("wrong 'to' value", TO, range.getTo());
    }

}

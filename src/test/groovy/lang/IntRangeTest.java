/*
 * Copyright 2003-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.lang;

/**
 * Provides unit tests for the <code>IntRange</code> class.
 *
 * @author James Strachan
 * @version $Revision$
 */
public class IntRangeTest extends NumberRangeTest {

    public void testCreateTooBigRange() {
        try {
            createRange(0, Integer.MAX_VALUE);
            fail("too large range accepted");
        }
        catch (IllegalArgumentException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Tests providing invalid arguments to the protected constructor.
     */
    public void testInvalidArgumentsToConstructor() {
        try {
            new IntRange(2, 1, true);
            fail("invalid range created");
        }
        catch (IllegalArgumentException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Tests getting the to and from values as <code>int</code>s.
     */
    public void testGetToFromInt() {
        final int from = 3, to = 7;
        final IntRange range = new IntRange(from, to);
        assertEquals("wrong 'from'", from, range.getFromInt());
        assertEquals("wrong 'to'", to, range.getToInt());
    }

    /**
     * {@inheritDoc}
     */
    protected Comparable createValue(int value) {
        return new Integer(value);
    }

    /**
     * {@inheritDoc}
     */
    protected Range createRange(int from, int to) {
        return new IntRange(from, to);
    }
}

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

/**
 * Tests {@link NumberRange}s of {@link Long}s.
 */
public class LongNumberRangeTest extends NumberRangeTestCase {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Range createRange(int from, int to) {
        return new NumberRange(new Long(from), new Long(to));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Comparable createValue(int value) {
        return new Long(value);
    }

    public void testSizeWithLongTo() {
        assertEquals(3, new NumberRange(new Integer(Integer.MAX_VALUE), new Long(Integer.MAX_VALUE + 2L)).size());
    }

    // GROOVY-4973: Range made-up of from: Integer and to: Long should have 'from' promoted to type Long.
    protected void checkRangeValues(Integer from, Comparable to, Range range) {
        assertEquals("wrong 'from' value", Long.valueOf(from.longValue()), range.getFrom());
        assertEquals("wrong 'to' value", to, range.getTo());
    }
}

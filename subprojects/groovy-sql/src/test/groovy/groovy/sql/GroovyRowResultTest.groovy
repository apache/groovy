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
package groovy.sql

import groovy.test.GroovyTestCase

class GroovyRowResultTest extends GroovyTestCase {

    void testMap() {
        def row = createRow();
        def row2 = createRow();

        /**
         * Test for implementing Map
         */
        assert row instanceof Map, "BUG! GroovyRowResult doesn't implement Map!"

        /**
         * Test for put and accessing the new property
         */
        row.put("john", "Doe")
        assert row.john == "Doe"
        assert row["john"] == "Doe"
        assert row['john'] == 'Doe'
        assert row.containsKey("john")
        assert row.containsKey("JOHN")
        assert row.containsKey("John")
        assert !row2.containsKey("john")
        assert row.containsValue("Doe")
        assert !row2.containsKey("Doe")

        /**
         * Test for equality (1) and size
         */
        assert row != row2, "rows unexpectedly equal"
        assert row.size() == 7
        assert row2.size() == 6

        /**
         * Test for remove, equality (2) and isEmpty (1)
         */
        row.remove("john")
        assert row == row2, "rows different after remove"
        assert !row.isEmpty(), "row empty after remove"

        /**
         * Test for clear, equality (3) and isEmpty (2)
         */
        row.clear()
        row2.clear()
        assert row == row2, "rows different after clear"
        assert row.isEmpty(), "row not empty after clear"
    }

    void testProperties() {
        def row = createRow()
        assert row.miXed == "quick"
        assert row.mixed == "quick"
        assert row.lower == "brown"
        assert row.LOWER == "brown"
        assert row.upper == "fox"
        assert row.UPPER == "fox"

        shouldFail(MissingPropertyException) {
            row.foo
        }

        /**
         * This is for GROOVY-1296
         */
        assert row.nullMixed == null
        assert row[1] == null
        assert row.nulllower == null
        assert row[3] == null
        assert row.NULLUPPER == null
        assert row[5] == null

        assert row.containsKey('upper')
        assert row.containsKey('UPPER')
        assert row.remove('upper')
        assert !row.containsKey('upper')
        assert !row.containsKey('UPPER')
    }

    void testOrder() {
        def row = createRow()
        assert row[0] == "quick"
        assert row[1] == null
        assert row[2] == "brown"
        assert row[3] == null
        assert row[4] == "fox"
        assert row[5] == null
        assert row[27] == null
        assert row[-1] == null
        assert row[-2] == "fox"
    }

    protected def createRow() {
        def map = new LinkedHashMap()
        map.put("miXed", "quick")
        map.put("nullMixed", null)
        map.put("lower", "brown")
        map.put("nulllower", null)
        map.put("UPPER", "fox")
        map.put("NULLUPPER", null)
        def row = new GroovyRowResult(map)
        assert row != null, "failed to load GroovyRowResult class"
        return row
    }

    void testCaseInsensitivePut() {
        def row = new GroovyRowResult(SOMEKEY: "v1")
        assert row.someKEY == 'v1'
        row.somekey = "v2"
        assert row.someKEY == 'v2'
        assert row.put('sOmEkEy', 'v3') == 'v2'
        assert row.someKEY == 'v3'
        assert row.size() == 1
    }
}

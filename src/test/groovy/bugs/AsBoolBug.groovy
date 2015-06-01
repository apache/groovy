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
package groovy.bugs

/**
 * Test to fix the Jira issues GROOVY-810 and GROOVY-811.
 * Test of "string as Boolean" against the issue GROOVY-812.
 *
 * @author Pilho Kim
 */

public class AsBoolBug extends GroovyTestCase {

    void testMapAsBool() {
        def a = ["A":123]
        println ("$a : ${a as Boolean}")
        assert a as Boolean == true
        a = [:]
        println ("$a : ${a as Boolean}")
        assert a as Boolean == false
    }

    void testListAsBool() {
        def b = [123]
        println ("$b : ${b as Boolean}")
        assert b as Boolean == true
        b = []
        println ("$b : ${b as Boolean}")
        assert b as Boolean == false
    }

    /**
     * void testStringAsBool().
     *
     * <code>string as Boolean</code> is equivalent to
     *     <code>string != null && string.length() > 0</code>.
     */
    // Unfortunately, it contradicts several other test cases, and
    // it has already been decided to handle string-to-boolean conversions
    // differently. Commented out temporarily on 10 May 2005.
    // This is a test case against GROOVY-812
    void testStringAsBool() {
        def c = "false"
        println ("$c : ${c as Boolean}")
        assert c as Boolean == true
        assert c as Boolean == (c != null && c.length() > 0)
        boolean z = c
        println ("$z")
        assert z == true
        if (c)
           println "It is true!!"
        else
           println "It is false!!"

        c = "123"
        println ("$c : ${c as Boolean}")
        assert c as Boolean == true
        assert c as Boolean == (c != null && c.length() > 0)

        c = "False"
        println ("$c : ${c as Boolean}")
        assert c as Boolean == true
        assert c as Boolean == (c != null && c.length() > 0)
        if (c)
           println "It is true!!"
        else
           println "It is false!!"
        z = c
        println ("$z")
        assert z
    }
}

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
package groovy

import groovy.test.GroovyTestCase

class GeneratorTest extends GroovyTestCase {

    void testGenerator() {
        def x = this.&sampleGenerator
        //System.out.println("x: " + x)

        def result = ''
        for (i in x) {
            result = result + i
        }

        assert result == "ABC"
    }

    void testFindAll() {
        def x = this.&sampleGenerator

        def value = x.findAll { item -> return item == "C" }
        assert value == ["C"]

        value = x.findAll { item -> return item != "B" }
        assert value == ["A", "C"]
    }

    void testEach() {
        def x = this.&sampleGenerator
        def expected = ['A', 'B', 'C']
        def value = x.each { assert it == expected.remove(0) }
    }

    void testMissingThisBug() {
        def result = ''
        for (i in this.&sampleGenerator) {
            result = result + i
        }

        assert result == "ABC"
    }

    void sampleGenerator(closure) {
        // kinda like yield statements
        closure.call("A")
        closure.call("B")
        closure.call("C")
    }
}

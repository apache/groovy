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

import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import static org.junit.Assume.assumeTrue

@RunWith(JUnit4)
class Groovy7611Bug extends GroovyTestCase {

    @BeforeClass
    static void setUp() {
        assumeTrue('java.util.Optional class is available', hasOptional())
    }

    @Test
    void testOptionalAsBooleanCoersion() {
        assertScript '''
            assert !Optional.empty()
            assert !Optional.ofNullable(null)
            assert Optional.of('foo')
        '''
    }

    private static boolean hasOptional() {
        try {
            Class.forName('java.util.Optional')
            return true
        } catch (any) {
            return false
        }
    }

}

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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy9805 {

    @Test
    void testSameLineAssignments1() {
        assertScript '''
            Object x; x = System.nanoTime(); x = ""
        '''
    }

    @Test
    void testSameLineAssignments2() {
        assertScript '''
            Object x; try { x = System.nanoTime(); x = "x value" } catch (e) { }
        '''
    }

    @Test
    void testSameLineAssignments3() {
        assertScript '''
            Object[] x; try { x = new Long[0]; x = new String[0] } catch (e) { }
        '''
    }

    @Test
    void testSameLineAssignments4() {
        assertScript '''
            CharSequence x; try { x = new StringBuffer(); x = "" } catch (e) { }
        '''
    }
}

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

class Groovy3830Bug extends GroovyTestCase {
    void testCallSitesUsageInAnInterface() {
        assert I3830.i == 2
        assert I3830.i2 == 5
        assert I3830.i3 == 6
    }

    void testCallSitesUsageInNestedInterface() {
        assert C3830.I3830.i == 2
        assert C3830.I3830.i2 == 5
        assert C3830.I3830.i3 == 6

        assert C3830.I3830.I3830_1.i == 4
        assert C3830.I3830.I3830_1.i2 == 7
        assert C3830.I3830.I3830_1.i3 == 12

        assert C3830.I3830.I3830_2.i == 6
        assert C3830.I3830.I3830_2.i2 == 9
        assert C3830.I3830.I3830_2.i3 == 18
    }

}

interface I3830 {
    Integer i = 2
    Integer i2 = i + 3
    Integer i3 = i * 3
}

class C3830 {
    interface I3830 {
        Integer i = 2
        Integer i2 = i + 3
        Integer i3 = i * 3
        interface I3830_1 {
            // ensure inner class number increments for callsites helper
            // anon C3830$I3830$I3830_1$1 and helper C3830$I3830$I3830_1$2
            def x = new Runnable() {
                @Override
                void run() {}
            }
            Integer i = 4
            Integer i2 = i + 3
            Integer i3 = i * 3
        }
        interface I3830_2 {
            Integer i = 6
            Integer i2 = i + 3
            Integer i3 = i * 3
        }
    }
}

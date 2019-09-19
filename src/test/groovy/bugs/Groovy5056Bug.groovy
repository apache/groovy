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

import groovy.test.GroovyTestCase

class Groovy5056Bug extends GroovyTestCase {
    void testASortedSetMinusACollection() {
        def comparator = [compare: {a,b->
                def retVal = a.x.compareTo(b.x)
                return retVal
            }
        ] as Comparator

        def ts1 = new TreeSet(comparator)
        ts1.addAll([
            new ToCompare(x:"1"),
            new ToCompare(x:"2"),
            new ToCompare(x:"3")
        ])

        def ts2 = new TreeSet(comparator)
        ts2.addAll([
            new ToCompare(x:"1"),
            new ToCompare(x:"2"),
            new ToCompare(x:"3")
        ])

        def difference = ts1 - ts2
        assert difference.size() == 0
    }

    void testASortedSetMinusAnItem() {
        def comparator = [compare: {a,b->
                def retVal = a.x.compareTo(b.x)
                return retVal
            }
        ] as Comparator

        def ts1 = new TreeSet(comparator)
        ts1.addAll([
            new ToCompare(x:"1"),
            new ToCompare(x:"2"),
            new ToCompare(x:"3")
        ])

        def difference = ts1 - new ToCompare(x:"3")
        assert difference.size() == 2
    }
}

class ToCompare {
    String x
}

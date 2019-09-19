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

class Groovy3818Bug extends GroovyTestCase {
    void testCreatingSimilarSetandMapWithComparator() {
        def scompare = { a, b -> a.id <=> b.id } as Comparator

        def set = new TreeSet( scompare )

        set << [name: "Han Solo",       id: 1]
        set << [name: "Luke Skywalker", id: 2]
        set << [name: "L. Skywalker",   id: 3]

        def result = set.findAll { elt -> elt.name =~ /Sky/ }
        assert result.size() == 2

        result = set.grep { elt -> elt.name =~ /Sky/ }
        assert result.size() == 2

        def mcompare = { a, b -> a.id <=> b.id } as Comparator

        def map = new TreeMap( mcompare )

        map[[name: "Han Solo", id: 1]] = "Dummy"
        map[[name: "Luke Skywalker", id: 2]] = "Dummy"
        map[[name: "L. Skywalker",   id: 3]] = "Dummy"

        result = map.findAll { elt ->elt.key.name =~ /Sky/ }
        assert result.size() == 2
    }
}

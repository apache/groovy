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
package org.codehaus.groovy.runtime

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Tests for DefaultGroovyStaticMethods
 */
class DefaultGroovyStaticMethodsTest extends GroovyTestCase {

    void testCurrentTimeSeconds() {
	    long timeMillis = System.currentTimeMillis()
        long timeSeconds = System.currentTimeSeconds()
        long timeMillis2 = System.currentTimeMillis()
        assert timeMillis/1000 as int <= timeSeconds
        assert timeMillis2/1000 as int >= timeSeconds
    }

    void testFirst() {
        assert 2 == Stream.of(2, 3, 6, 5).collect(Collectors.first()).get()
    }

    void testLast() {
        assert 5 == Stream.of(2, 3, 6, 5).collect(Collectors.last()).get()
    }

    void testFirstAndLast() {
        Tuple2<Integer, Integer> firstAndLastTuple =
                Stream.of(2, 3, 6, 5)
                        .collect(Tuple.collectors(Collectors.first(), Collectors.last()))
                        .map1(Optional::get).map2(Optional::get)
        assert Tuple.tuple(2, 5) == firstAndLastTuple
    }
}

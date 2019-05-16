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
package org.apache.groovy.json.internal

import groovy.test.GroovyTestCase

class LazyMapTest extends GroovyTestCase {

    // GROOVY-7302
    void testSizeWhenNoBackingMapCreated() {
        def map = new LazyMap()
        map.someProperty = "1"
        map.someProperty = "2"
        map.someProperty = "3"
        assert map.size() == 1
        map.someProperty2 = "4"
        assert map.size() == 2
    }

    void testSizeWhenLazyCreated() {
        def map = new LazyMap()
        map.someProperty1 = '1'
        assert map.@map == null
        map.someProperty2 = '2'
        assert map.@map == null
        map.someProperty3 = '3'
        assert map.@map == null
        map.someProperty4 = '4'
        assert map.@map == null
        map.someProperty5 = '5'
        assert map.@map == null
        map.someProperty6 = '6'
        assert map.@map == null
        map.someProperty7 = '7'
        assert map.someProperty6 == '6'
        assert map.@map?.size() == 7
    }
}

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
package org.codehaus.groovy.ant

import org.junit.jupiter.api.Test

import java.lang.reflect.Field

/**
 * Exercises the {@code excludePackageNames} setter on the Ant Groovydoc
 * task. The private backing list was consumed by {@code generate()} from
 * day one but had no public setter — the parity sweep added one.
 */
class GroovydocExcludeTest {
    private static List<String> readExcludeList(Groovydoc task) {
        Field f = Groovydoc.getDeclaredField('excludePackageNames')
        f.setAccessible(true)
        return (List<String>) f.get(task)
    }

    @Test
    void testSetExcludePackageNamesCommaSeparated() {
        def task = new Groovydoc()
        task.setExcludePackageNames('com.example.internal,com.example.legacy')
        def names = readExcludeList(task)
        assert names == ['com.example.internal', 'com.example.legacy']
    }

    @Test
    void testSetExcludePackageNamesColonSeparated() {
        def task = new Groovydoc()
        // The CLI -exclude flag uses ':' as separator; the Ant setter
        // tolerates both so scripts porting from CLI don't stumble.
        task.setExcludePackageNames('com.example.internal:com.example.legacy')
        def names = readExcludeList(task)
        assert names == ['com.example.internal', 'com.example.legacy']
    }

    @Test
    void testSetExcludePackageNamesAccumulatesAcrossCalls() {
        def task = new Groovydoc()
        task.setExcludePackageNames('com.foo')
        task.setExcludePackageNames('com.bar,com.baz')
        def names = readExcludeList(task)
        assert names == ['com.foo', 'com.bar', 'com.baz']
    }
}

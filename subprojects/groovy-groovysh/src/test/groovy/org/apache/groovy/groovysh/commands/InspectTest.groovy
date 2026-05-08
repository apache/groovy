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
package org.apache.groovy.groovysh.commands

import org.junit.jupiter.api.Test

/**
 * Tests for the {@code /inspect} command. Exercises GroovyCommands and the
 * vendored ObjectInspector — neither of which had any direct test coverage
 * before. The command writes via the printer, so assertions go against
 * {@code printer.output}.
 */
class InspectTest extends SystemTestSupport {

    @Test
    void inspectMethodsListsKnownMembers() {
        console.execute('dummy', "data = [a: 1, b: 2]")
        system.execute('/inspect --methods $data')
        def out = printer.output.join()
        // LinkedHashMap exposes these methods; assert on names without
        // pinning to formatting/columns/parameter shapes.
        assert out.contains('get')
        assert out.contains('put')
        assert out.contains('size')
    }

    @Test
    void inspectInfoListsPropertyCategories() {
        console.execute('dummy', "data = [a: 1, b: 2]")
        system.execute('/inspect --info $data')
        def out = printer.output.join()
        // ObjectInspector.properties() returns a map with these keys.
        assert out.contains('propertyInfo')
        assert out.contains('publicFields')
        assert out.contains('classProps')
    }
}

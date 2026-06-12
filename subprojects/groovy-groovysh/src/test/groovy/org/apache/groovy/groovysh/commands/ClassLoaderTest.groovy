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
 * Tests for the {@code /classloader} command.
 *
 * The command renders the engine's class loader using the {@code COLUMNS}
 * option of {@code Printer.println(options, object)}; the support class's
 * {@code DummyPrinter} extracts each column as a {@code name=value} entry
 * so substring assertions remain straightforward.
 */
class ClassLoaderTest extends SystemTestSupport {

    @Test
    void viewExposesColumnDataForLoadedClassLoader() {
        // Define a type so the class loader has at least one entry to render.
        engine.execute('class ClassLoaderProbe {}')
        system.execute('/classloader')
        def out = printer.output.join()
        // Each column name from the /classloader command appears as a
        // `name=...` entry. Don't assert on values' exact shape (lists vary
        // by JDK and previous test state); just confirm the columns rendered.
        assert out.contains('loadedClasses=')
        assert out.contains('definedPackages=')
        assert out.contains('classPath=')
    }
}

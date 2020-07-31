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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.SourceUnit
import org.junit.Test

final class Groovy9666 {
    @Test
    void testCanIterateImportsWhileAdding() {
        def mn = new ModuleNode((SourceUnit)null)

        mn.addImport('Integer', ClassHelper.Integer_TYPE)
        assert mn.imports.size() == 1
        for (importNode in mn.imports) {
            mn.addImport('Natural', ClassHelper.Integer_TYPE)
        }
        assert mn.imports.size() == 2
    }

    @Test
    void testSimulateImportCaseChangingTransform() {
        def mn = new ModuleNode((SourceUnit)null)

        mn.addStarImport('foo.bar')
        assert mn.starImports.size() == 1
        assert mn.starImports*.text == ['import foo.bar*']

        // simulate xform that manipulates imports as per some DSL context
        def copy = mn.starImports.clone()
        mn.starImports.clear()
        for (starImport in copy) {
            mn.addStarImport(starImport.packageName.toUpperCase())
        }

        assert mn.starImports.size() == 1
        assert mn.starImports*.text == ['import FOO.BAR*']
    }
}

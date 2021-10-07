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
package groovy.script

import org.junit.Ignore
import org.junit.Test

import static org.apache.groovy.util.ScriptRunner.runScript

@Ignore('disabled pending rework')
final class RuntimeResolveTests {

    @Test
    void testResolveOuterStaticNestedClass() {
        runScript('/groovy/bugs/groovy4287/Main.groovy')
    }

    @Test
    void testResolveOuterStaticNestedClassAlias() {
        runScript('/groovy/bugs/groovy4287/Main2.groovy')
    }

    @Test
    void testResolvePublicStaticField() {
        runScript('/groovy/bugs/groovy4386/StaticField.groovy')
    }

    @Test
    void testResolveStaticProperty() {
        runScript('/groovy/bugs/groovy4386/StaticProperty.groovy')
    }

    @Test
    void testResolveStaticMembers() {
        runScript('/groovy/bugs/groovy4386/StaticStarImport.groovy')
    }

    @Test
    void testResolveOuterNestedClass() {
        runScript('/groovy/bugs/groovy7812/Main.groovy')
    }

    @Test @Ignore('exception in script causes problem for build')
    void testUnexistingInnerClass() {
        try {
            runScript('/groovy/bugs/groovy7812/MainWithErrors.groovy')
        } catch (Throwable t) {
            assert t.getMessage().contains('unable to resolve class Outer.Inner123')
        }
    }

    @Test
    void testResolvePrecedence() {
        runScript('/groovy/bugs/groovy9236/Main.groovy')
    }

    @Test
    void testResolveNestedClassFromBaseType() {
        runScript('/groovy/bugs/groovy9243/Main.groovy')
    }
}

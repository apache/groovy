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

import org.junit.Test

import static groovy.test.GroovyAssert.isAtLeastJdk
import static org.apache.groovy.util.ScriptRunner.runScript
import static org.junit.Assume.assumeTrue

final class RuntimeResolveTests {

    @Test
    void testResolveStaticImportOfOuterMember1() {
        runScript('/groovy/bugs/groovy4287/Main.groovy')
    }

    @Test
    void testResolveStaticImportOfOuterMember2() {
        runScript('/groovy/bugs/groovy4287/Main2.groovy')
    }

    @Test
    void testResolveStaticImportOfOuterMember3() {
        runScript('/groovy/bugs/groovy4386/StaticField.groovy')
    }

    @Test
    void testResolveStaticImportOfOuterMember4() {
        runScript('/groovy/bugs/groovy4386/StaticProperty.groovy')
    }

    @Test
    void testResolveStaticImportOfOuterMember5() {
        runScript('/groovy/bugs/groovy4386/StaticStarImport.groovy')
    }

    @Test
    void testResolveOuterMemberWithoutAnImport() {
        runScript('/groovy/bugs/groovy7812/Main.groovy')
    }

    @Test
    void testResolvePackagePeerWithoutAnImport() {
        runScript('/groovy/bugs/groovy9236/Main.groovy')
    }

    @Test
    void testResolveOuterMemberWithoutAnImport2() {
        runScript('/groovy/bugs/groovy9243/Main.groovy')
    }

    @Test
    void testResolveOuterMemberWithoutAnImport3() {
        assumeTrue(isAtLeastJdk('9.0')) // System.Logger
        runScript('/groovy/bugs/groovy9866/Main.groovy')
    }

    @Test
    void testResolvePackagePeersAndCompileTrait() {
        runScript('/groovy/bugs/groovyA143/Main.groovy')
        runScript('/groovy/bugs/groovyA144/Main.groovy')
    }

    @Test
    void testResolvePackagePeersAndTypeArgument() {
        runScript('/groovy/bugs/groovy7799/Main.groovy')
    }

    @Test
    void testResolvePackagePeersAndAnnotationValue() {
        runScript('/groovy/bugs/groovyA196/Main.groovy')
    }
}

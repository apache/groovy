/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell

class ReflectionCompletorTest
extends CompletorTestSupport {

    void testEmpty() {
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            assertEquals(-1, completor.complete("", 0, []))
        }
    }

    void testUnknownVar() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            assertEquals(-1, completor.complete("xyz", 2, candidates))
            assertEquals([], candidates)
        }
    }


    void testPackageAccess() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            String prompt = "println(java."
            assertEquals(-1, completor.complete(prompt, prompt.length(), candidates))
            assertEquals([], candidates)
        }
    }

    void testKnownVar() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [xyzabc: ""]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            assertEquals(0, completor.complete("xyz", 2, candidates))
            assertEquals(["xyzabc"], candidates)
        }
    }

    void testKnownVarMultiple() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [bad: "", xyzabc: "", xyzfff: "", nope: ""]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            assertEquals(0, completor.complete("xyz", 2, candidates))
            assertEquals(["xyzabc", "xyzfff"], candidates)
        }
    }

    void testKnownVarAfterMethod() {
        groovyshMocker.demand.getInterp(1) { [evaluate: {}, context: [variables: [xyzabc: ""]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            // xyz cannot be not a var here
            assertEquals(-1, completor.complete("Fo.ba().xyz", "Fo.ba().xyz".length(), candidates))
            assertEquals([], candidates)
        }
    }


    void testLiteralStringMethod() {
        // mock doing the right thing
        groovyshMocker.demand.getInterp(1) { [evaluate: { expr -> "foo" }] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            assertEquals(6, completor.complete("\"foo\".subs", "\"foo\".subs".length(), candidates))
            assertEquals(["substring("], candidates)
        }
    }


}
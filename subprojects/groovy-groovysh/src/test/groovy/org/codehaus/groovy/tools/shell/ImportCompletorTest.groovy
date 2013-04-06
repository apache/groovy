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

import jline.Completor
import org.codehaus.groovy.tools.shell.commands.ImportCommand

class ImportCompletorTest
extends CompletorTestSupport {

    void testEmpty() {
        groovyshMocker.demand.getInterp(1) { [classLoader: new GroovyClassLoader()] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completor completor = iCom.getCompletor()
            assertEquals(0, completor.complete("", 0, []))
        }
    }

    void testUnknownVar() {
        groovyshMocker.demand.getInterp(1) { [classLoader: new GroovyClassLoader()] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completor completor = iCom.getCompletor()
            def candidates = []
            assertEquals(-1, completor.complete("import ", "import ".length(), candidates))
            assertEquals([], candidates)
        }
    }

    void testJ() {
        groovyshMocker.demand.getInterp(1) { [classLoader: new GroovyClassLoader()] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completor completor = iCom.getCompletor()
            def candidates = []
            // argument completor completes after "import "
            assertEquals(7, completor.complete("import j", "import j".length(), candidates))
            // the blank is just the current broken output
            assertEquals(["java. "], candidates)
        }
    }

    void testJavaDot() {
        groovyshMocker.demand.getInterp(1) { [classLoader: new GroovyClassLoader()] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completor completor = iCom.getCompletor()
            def candidates = []
            // argument completor completes after "import "
            assertEquals(7, completor.complete("import java.", "import java.".length(), candidates))
            // the blank is just the current broken output
            assertEquals(["java.lang. "], candidates)
        }
    }

    void testJavaLangDot() {
        groovyshMocker.demand.getInterp(1) { [classLoader: new GroovyClassLoader()] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completor completor = iCom.getCompletor()
            def candidates = []
            // argument completor completes after "import "
            assertEquals(7, completor.complete("import java.lang.", "import java.lang.".length(), candidates))
            assertEquals(["java.lang.System "], candidates)
        }
    }

    void testAs() {
        groovyshMocker.demand.getInterp(1) { [classLoader: new GroovyClassLoader()] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completor completor = iCom.getCompletor()
            def candidates = []
            // this confirms a bug
            assertEquals(-1, completor.complete("import java.lang.System ", "import java.lang.System ".length(), candidates))
            assertEquals([], candidates)
        }
    }
}
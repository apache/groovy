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

import jline.CompletionHandler
import jline.Completor
import jline.ConsoleReader
import jline.History

/**
 * Test the combination of multiple completors via JLine ConsoleReader
 */
class AllCompletorsTest
extends GroovyTestCase {

    IO testio
    BufferedOutputStream mockOut
    BufferedOutputStream mockErr
    List<Completor> completors

    /**
     * code copied from Jline console Handler,
     * need this logic to ensure completors are combined in the right way
     * The Jline contract is that completors are tried in sequence, and as
     * soon as one returns something else than -1, his canidates are used and following
     * completors ignored.
     *
     */
    private List complete(String buffer, cursor) throws IOException {
        // debug ("tab for (" + buf + ")");
        if (completors.size() == 0) {
            return null;
        }
        List candidates = new LinkedList();
        String bufstr = buffer;
        int position = -1;
        for (Iterator i = completors.iterator(); i.hasNext();) {
            Completor comp = (Completor) i.next();
            if ((position = comp.complete(bufstr, cursor, candidates)) != -1) {
                break;
            }
        }
        // no candidates? Fail.
        if (candidates.size() == 0) {
            return null;
        }
        return [candidates, position]
    }

    void setUp() {
        super.setUp()
        mockOut = new BufferedOutputStream(
                new ByteArrayOutputStream());

        mockErr = new BufferedOutputStream(
                new ByteArrayOutputStream());

        testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)


        Groovysh groovysh = new Groovysh(testio)
        groovysh.history = new History()
        InteractiveShellRunner shellRun = new InteractiveShellRunner(groovysh, { ">"})
        // setup completors in run()
        shellRun.run()
        completors = shellRun.reader.getCompletors()
    }

    void testEmpty() {
        def result = complete("", 0)
        assertTrue('help' in result[0])
        assertTrue('exit' in result[0])
        assertTrue('import' in result[0])
        assertTrue('show' in result[0])
        assertTrue('set' in result[0])
        assertTrue('inspect' in result[0])
        assertEquals(0, result[1])
    }

    void testExitEdit() {
        assertEquals([["exit ", "edit "], 0], complete("e", 0))
    }

    void testShow() {
        String prompt = "show "
        assertEquals([["all", "classes", "imports", "preferences", "variables"], prompt.length()], complete(prompt, prompt.length()))
    }

    void testShowV() {
        String prompt = "show v"
        assertEquals([["variables "], prompt.length() - 1], complete(prompt, prompt.length()))
    }

    void testShowVariables() {
        String prompt = "show variables "
        assertEquals(null, complete(prompt, prompt.length()))
    }

    void testImportJava() {
        // tests interaction with ReflectionCompleter
        String prompt = "import j"
        def result = complete(prompt, prompt.length())
        assertEquals(prompt.length() - 1, result[1])
        assertTrue("java." in result[0])
    }

    void testShowVariablesJava() {
        // tests against interaction with ReflectionCompleter
        String prompt = "show variables java"
        assertEquals(null, complete(prompt, prompt.length()))
    }

    void testKeyword() {
        // tests against interaction with ReflectionCompleter
        String prompt = "pub"
        assertEquals([["public "], 0], complete(prompt, prompt.length()))
    }

    void testCommandAndKeyword() {
        // tests against interaction with ReflectionCompleter
        String prompt = "pu" // purge, public
        assertEquals([["purge "], 0], complete(prompt, prompt.length()))
    }

}
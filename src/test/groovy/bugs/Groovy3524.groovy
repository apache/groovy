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
package bugs

import groovy.ui.GroovyMain
import org.junit.jupiter.api.Test

/**
 * GROOVY-3524: {@code groovy -e} used to fail on a double-quoted string
 * literal with no trailing semicolon ({@code unexpected char: 0xFFFF}
 * or {@code unexpected char: '"'}). The same text in a file compiled
 * fine. The regression is in command-line eval, so this test goes
 * through {@link GroovyMain#main(String[])}.
 */
final class Groovy3524 {

    @Test
    void testDashEDoubleQuotedStringWithoutSemicolon() {
        assert eval('-e', 'println "Hello, World!"').trim() == 'Hello, World!'
    }

    @Test
    void testDashEEmptyDoubleQuotedString() {
        // the ticket's second one-liner produced unexpected char: '"'
        assert eval('-e', 'println ""').normalize() == '\n'
    }

    private static String eval(String... args) {
        def baos = new ByteArrayOutputStream()
        def origOut = System.out
        def origErr = System.err
        def ps = new PrintStream(baos, true)
        System.setOut(ps)
        System.setErr(ps)
        try {
            GroovyMain.main(args)
        } finally {
            System.setOut(origOut)
            System.setErr(origErr)
        }
        def text = baos.toString()
        assert !text.contains('unexpected char'), text
        assert !text.contains('MultipleCompilationErrorsException'), text
        return text
    }
}

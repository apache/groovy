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
 * GROOVY-5900: a script argument of {@code --} used to be swallowed
 * (printed as {@code The args are: []}). A single {@code -} and
 * {@code ---} were already passed through.
 */
final class Groovy5900 {

    @Test
    void testDoubleDashIsPassedAsScriptArgument() {
        assert runScript('foo', 'bar').trim() == 'The args are: [foo, bar]'
        assert runScript().trim() == 'The args are: []'
        assert runScript('-').trim() == 'The args are: [-]'
        assert runScript('--').trim() == 'The args are: [--]'
        assert runScript('---').trim() == 'The args are: [---]'
    }

    private static String runScript(String... scriptArgs) {
        def script = File.createTempFile('groovy5900', '.groovy')
        script.write 'println "The args are: ${args}"\n'
        def baos = new ByteArrayOutputStream()
        def origOut = System.out
        def origErr = System.err
        def ps = new PrintStream(baos, true)
        System.setOut(ps)
        System.setErr(ps)
        try {
            def args = [script.absolutePath] as List
            args.addAll(scriptArgs)
            GroovyMain.main(args as String[])
        } finally {
            System.setOut(origOut)
            System.setErr(origErr)
            script.delete()
        }
        return baos.toString()
    }
}

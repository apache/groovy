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

import groovy.lang.GroovyClassLoader
import groovy.lang.GroovyShell
import groovy.ui.GroovySocketServer
import org.junit.jupiter.api.Test

import java.lang.reflect.Method

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Name-taking overloads stay on the call path, and a class whose simple
 * name is a generated script name plus {@code Customizer} still loads.
 */
final class Groovy8113Compatibility {

    @Test
    void evaluateStringStillCallsNameTakingOverload() {
        // GROOVY-8113
        int[] calls = [0]
        GroovyShell shell = new GroovyShell() {
            @Override
            Object evaluate(String scriptText, String fileName, String codeBase) {
                calls[0]++
                return super.evaluate(scriptText, fileName, codeBase)
            }
        }
        assertEquals(2, shell.evaluate('1 + 1'))
        assertEquals(1, calls[0])
    }

    @Test
    void parseClassStringStillCallsNameTakingOverload() {
        // GROOVY-8113
        int[] calls = [0]
        GroovyClassLoader loader = new GroovyClassLoader() {
            @Override
            Class parseClass(String text, String fileName) {
                calls[0]++
                return super.parseClass(text, fileName)
            }
        }
        Class<?> parsed = loader.parseClass('3 + 4')
        assertEquals(1, calls[0])
        assertTrue(parsed.name ==~ /Script_[0-9a-fA-F]+/)
        assertEquals(7, parsed.getDeclaredConstructor().newInstance().run())
    }

    @Test
    void generatedScriptCanLoadItsOwnCustomizerClass() {
        // GROOVY-8113
        GroovyShell shell = new GroovyShell()
        List<?> loaded = shell.evaluate('''
            class Script1Customizer {
            }
            [this.class.name, this.class.classLoader.loadClass('Script1Customizer').name]
        ''')
        assertEquals(['Script1', 'Script1Customizer'], loaded)
        assertEquals('Script1Customizer', shell.classLoader.loadClass('Script1Customizer').name)
    }

    @Test
    void socketServerMarksInlineScriptNameGenerated() throws Exception {
        // GROOVY-8113
        Method method = GroovySocketServer.getDeclaredMethod('getCodeSource', boolean.class, String.class)
        method.setAccessible(true)
        GroovyCodeSource inline = (GroovyCodeSource) method.invoke(null, false, 'println 1')
        assertTrue(inline.isNameGenerated())
        assertTrue(inline.name ==~ /ServerSocketScript\d+\.groovy/)

        File file = File.createTempFile('Groovy8113Socket', '.groovy')
        file.deleteOnExit()
        file.write('println 2\n')
        GroovyCodeSource fromFile = (GroovyCodeSource) method.invoke(null, true, file.absolutePath)
        assertFalse(fromFile.isNameGenerated())
    }
}

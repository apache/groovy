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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

/**
 * GROOVY-6220: an unterminated slashy regex used to produce a cryptic
 * {@code unexpected char: 0xFFFF} diagnostic at end-of-file, rather than
 * pointing at the opening {@code /}.
 */
final class Groovy6220 {

    @Test
    void testUnterminatedSlashyRegexOnCommandLineIsPointedAt() {
        def err = compileFails '/a'
        assert err.contains('Unclosed string literal')
        assert err.contains('@ line 1, column 1')
        assert !err.contains('0xFFFF')
        assert !err.toLowerCase().contains('unexpected char')
    }

    @Test
    void testUnterminatedRegexInReplaceAllPointsAtTheSlash() {
        // Exact MWE from the ticket / SillyClass.groovy attachment
        def err = compileFails '''
            package silly
            public class SillyClass {
                public SillyClass() {
                    println "testString".replaceAll(/\\B[A-Z]) { it.toLowerCase() }
                }
            }
        '''
        assert err.contains('Unclosed string literal')
        assert err.contains('replaceAll')
        assert !err.contains('0xFFFF')
        assert !err.toLowerCase().contains('unexpected char')
    }

    private static String compileFails(String src) {
        try {
            new GroovyClassLoader().parseClass(src)
            assert false, 'expected a compilation failure'
            return ''
        } catch (MultipleCompilationErrorsException e) {
            return e.message
        }
    }
}

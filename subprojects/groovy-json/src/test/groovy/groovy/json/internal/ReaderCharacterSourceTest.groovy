/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.json.internal;

class ReaderCharacterSourceTest extends GroovyTestCase {

    void testFindNextCharWithRecursionHadEscape() {
        int[] control = '"\\'.toCharArray() as int[]

        // create a ReaderCharacterSource with a very small buffer, findNextChar must be invoked recursive

        // use a string where escape-character is in last invocation
        ReaderCharacterSource cs = new ReaderCharacterSource(new StringReader('"value\\u0026"'), 6)
        cs.findNextChar(control[0], control[1])

        assert cs.hadEscape()

        // use a string where escape-character is in first invocation
        cs = new ReaderCharacterSource(new StringReader('"\\u0026value"'), 6)
        cs.findNextChar(control[0], control[1])

        assert cs.hadEscape()
    }
}

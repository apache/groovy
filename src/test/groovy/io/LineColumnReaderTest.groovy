/*
 * Copyright 2003-2011 the original author or authors.
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
package groovy.io

import java.nio.CharBuffer

/**
 * @author Guillaume Laforge
 */
class LineColumnReaderTest extends GroovyTestCase {

    def reader = new LineColumnReader(new FileReader(new File('src/test/groovy/io/sample-text-file.txt')))

    void testReadLine() {
        reader.withReader { LineColumnReader r ->
            assert r.readLine() == "L'invitation au voyage"
            assert r.line == 1 && r.column == 23
            assert r.readLine() == ""
            assert r.line == 2 && r.column == 1
            assert r.readLine() == "Mon enfant, ma soeur,"
            assert r.line == 3 && r.column == 22
        }
    }

    void testReadLineWholeFile() {
        reader.withReader { LineColumnReader r ->
            int linesRead = 0
            String line = ""
            for(;;) {
                if (line == null) break
                line = r.readLine()
                if (line != null) linesRead++
            }

            assert linesRead == 49
        }
    }

    void testSkip() {
        reader.withReader { LineColumnReader r ->
            r.skip(13)
            assert r.readLine() == "au voyage"
        }
    }

    void testWindowsNewLine() {
        def reader = new LineColumnReader(new StringReader("12345\r\nABCDEF\r1234"))
        reader.withReader { LineColumnReader r ->
            assert r.readLine() == "12345"
            assert r.line == 1 && r.column == 6
            assert r.readLine() == "ABCDEF"
            assert r.line == 2 && r.column == 7
            assert r.readLine() == "1234"
            assert r.line == 3 && r.column == 5
        }
    }

    void testReadCharBuffer() {
        shouldFail(UnsupportedOperationException) {
            reader.withReader { LineColumnReader r ->
                def buffer = CharBuffer.allocate(13)
                r.read(buffer)
            }
        }
    }
}

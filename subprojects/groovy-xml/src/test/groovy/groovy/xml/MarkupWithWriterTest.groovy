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
package groovy.xml

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * This test uses GroovyMarkup with writers other than System.out
 */
final class MarkupWithWriterTest extends TestXmlSupport {

    @Test
    void testSmallTreeWithStringWriter() {
        def writer = new java.io.StringWriter()
        def b = new MarkupBuilder(writer)

        b.root1(a:5, b:7) {
            elem1('hello1')
            elem2('hello2')
            elem3(x:7)
        }
        assertEquals "<root1 a='5' b='7'>\n" +
                "  <elem1>hello1</elem1>\n" +
                "  <elem2>hello2</elem2>\n" +
                "  <elem3 x='7' />\n" +
                "</root1>", writer.toString()
    }

    @Test
    void testWriterUseInScriptFile() {
        assertScriptFile 'src/test/groovy/groovy/xml/UseMarkupWithWriterScript.groovy'
    }
}

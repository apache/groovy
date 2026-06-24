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

import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * The markup builders escape element text and attribute values, but
 * processing-instruction and comment bodies cannot use entity escaping.
 * These tests check that content which would close such a construct early
 * (and inject sibling markup) is rejected instead of emitted.
 */
final class MarkupBuilderInjectionTest {

    @Test
    void commentRejectsCommentTerminator() {
        assertThrows(IllegalArgumentException) {
            new MarkupBuilder(new StringWriter()).root { mkp.comment('legit --> <injected/>') }
        }
        assertThrows(IllegalArgumentException) {
            new MarkupBuilder(new StringWriter()).root { mkp.comment('a -- b') }
        }
    }

    @Test
    void commentKeepsPlainTextLiteral() {
        def writer = new StringWriter()
        new MarkupBuilder(writer).root { mkp.comment('plain > & < text') }
        assertTrue(writer.toString().contains('<!-- plain > & < text -->'))
    }

    @Test
    void streamingProcessingInstructionRejectsTerminator() {
        def smb = new StreamingMarkupBuilder()
        assertThrows(GroovyRuntimeException) {
            smb.bind { mkp.pi('xml-stylesheet': [href: 'a', type: 'b?><injected/>']); root('x') }.toString()
        }
    }

    @Test
    void streamingProcessingInstructionKeepsPlainData() {
        def smb = new StreamingMarkupBuilder()
        def result = smb.bind { mkp.pi('xml-stylesheet': [href: 'style.css', type: 'text/css']); root('x') }.toString()
        assertTrue(result.contains('<?xml-stylesheet') && result.contains('style.css') && result.contains('?>'))
    }
}

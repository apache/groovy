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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-9411: an incomplete {@code \\u} sequence in a comment used to fail
 * parsing with "Did not find four digit hex character code". Such comments
 * must be ignored; Unicode escapes remain meaningful inside string literals.
 */
final class Groovy9411 {

    @Test
    void testIncompleteUnicodeEscapeInLineCommentIsIgnored() {
        // Build the source so this test file itself does not contain a raw \u escape.
        def slashU = '\\' + 'u'
        def src = '//' + ' ' + slashU + '\nassert true\n'
        assert src.contains(slashU)
        assertScript src
    }

    @Test
    void testIncompleteUnicodeEscapeInBlockCommentIsIgnored() {
        def slashU = '\\' + 'u'
        def src = '/* ' + slashU + ' */\nassert true\n'
        assert src.contains(slashU)
        assertScript src
    }

    @Test
    void testUnicodeEscapeStillWorksInsideStringLiteral() {
        def slashU = '\\' + 'u'
        assertScript "assert '" + slashU + "0041' == 'A'\n"
    }

    @Test
    void testIncompleteUnicodeEscapeInsideStringLiteralIsStillAnError() {
        def slashU = '\\' + 'u'
        def src = "assert '" + slashU + "'\n"
        def err = shouldFail(src)
        assert err.message.contains('Illegal escape character')
    }
}

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

/**
 * GROOVY-8395: a line comment containing a Windows path with an accidental
 * {@code \\u} sequence (as in a path segment starting with {@code uniphi})
 * used to fail parsing with "Did not find four digit hex character code".
 * Such comments must be ignored.
 */
final class Groovy8395 {

    @Test
    void testBackslashUInCommentIsNotAUnicodeEscapeError() {
        // Build the source so this test file itself does not contain a raw \u escape.
        def slash = '\\'
        def src = '// Unable to delete \'C:' + slash + 'j2' + slash + 'uniphi5-ws' + slash + 'sqR2011a-64\'\nassert \'hi\' == \'hi\'\n'
        assert src.contains(slash + 'uniphi')
        assertScript src
    }

    @Test
    void testBackslashUnitsInCommentIsNotAUnicodeEscapeError() {
        def slash = '\\'
        def src = '// Location: D:' + slash + 'Simple' + slash + 'units\nassert \'ok\' == \'ok\'\n'
        assert src.contains(slash + 'units')
        assertScript src
    }
}

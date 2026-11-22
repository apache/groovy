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

import java.util.regex.Pattern

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-8824: groovy-json version detection must treat Java 10+
 * ({@code java.version=10.0.2}) as 1.8-or-later so JsonSlurper maps
 * keep insertion order via {@code LinkedHashMap}.
 */
final class Groovy8824 {

    @Test
    void testJava10VersionPattern() {
        def p = Pattern.compile(/^([1-9]\d*(\.\d+)?)/)
        def m = p.matcher('10.0.2')
        assert m.find()
        assert m.group(0) == '10.0'
        assert new BigDecimal(m.group(0)) >= new BigDecimal('1.8')
    }

    @Test
    void testJsonSlurperPreservesInsertionOrder() {
        assertScript '''
            import groovy.json.JsonSlurper
            def json = new JsonSlurper().parseText('{"z":1,"a":2,"m":3}')
            assert (json.keySet() as List) == ['z', 'a', 'm']
        '''
    }
}

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
package groovy.csv

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Jackson 3 raised its default string length to 100,000,000; groovy-csv keeps the
 * 20,000,000 characters a field could hold on Jackson 2.
 */
class CsvLimitsTest {

    @Test
    void testFieldLengthLimitIsKept() {
        assert new CsvSlurper().parseText('h\n' + 'x' * 19_000_000 + '\n')[0].h.size() == 19_000_000
        def e = shouldFail {
            new CsvSlurper().parseText('h\n' + 'x' * 21_000_000 + '\n')
        }
        assert e.message.contains('20000000')
    }
}

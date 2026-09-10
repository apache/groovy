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
package groovy.toml

import groovy.json.JsonDateHandling
import org.junit.jupiter.api.Test

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class TomlSlurperDateHandlingTest {

    private static final String TOML = 'when = 2026-09-04T10:00:00+10:00'

    @Test
    void testDefaultLeavesDateTimeAsString() {
        // GROOVY-12357
        def slurper = new TomlSlurper()
        assert slurper.dateHandling == JsonDateHandling.STRING
        assert slurper.parseText(TOML).when instanceof String
    }

    @Test
    void testDateHandlingChoices() {
        // GROOVY-12357
        def slurp = { JsonDateHandling h ->
            new TomlSlurper().setDateHandling(h).parseText(TOML).when
        }
        assert slurp(JsonDateHandling.STRING) instanceof String
        assert slurp(JsonDateHandling.UTIL_DATE) instanceof Date
        assert slurp(JsonDateHandling.INSTANT) instanceof Instant

        // only OFFSET_DATE_TIME keeps the offset the document carried
        def odt = slurp(JsonDateHandling.OFFSET_DATE_TIME)
        assert odt instanceof OffsetDateTime
        assert odt.offset == ZoneOffset.ofHours(10)
    }

    @Test
    void testDateWithoutTimeLeftAsString() {
        // GROOVY-12357
        [JsonDateHandling.STRING, JsonDateHandling.UTIL_DATE,
         JsonDateHandling.INSTANT, JsonDateHandling.OFFSET_DATE_TIME].each { h ->
            assert new TomlSlurper().setDateHandling(h)
                    .parseText('d = 2026-09-04').d instanceof String
        }
    }

    @Test
    void testNullResetsToStringDefault() {
        // GROOVY-12357
        def slurper = new TomlSlurper().setDateHandling(JsonDateHandling.INSTANT)
        assert slurper.dateHandling == JsonDateHandling.INSTANT
        slurper.setDateHandling(null)
        assert slurper.dateHandling == JsonDateHandling.STRING
        assert slurper.parseText(TOML).when instanceof String
    }

    @Test
    void testSetterIsFluent() {
        // GROOVY-12357
        def slurper = new TomlSlurper()
        assert slurper.setDateHandling(JsonDateHandling.INSTANT).is(slurper)
        assert slurper.dateHandling == JsonDateHandling.INSTANT
    }
}

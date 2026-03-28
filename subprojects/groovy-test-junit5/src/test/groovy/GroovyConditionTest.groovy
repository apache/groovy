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

import groovy.junit5.plugin.GroovyDisabledIf
import groovy.junit5.plugin.GroovyEnabledIf
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assertions.fail

class GroovyConditionTest {

    @Test
    @GroovyEnabledIf({ true })
    void enabledIfTrue() {
        assertTrue(true)
    }

    @Test
    @GroovyEnabledIf({ false })
    void enabledIfFalseSkipped() {
        fail('This test should be skipped')
    }

    @Test
    @GroovyDisabledIf({ true })
    void disabledIfTrueSkipped() {
        fail('This test should be skipped')
    }

    @Test
    @GroovyDisabledIf({ false })
    void disabledIfFalse() {
        assertTrue(true)
    }

    @Test
    @GroovyEnabledIf({ javaVersion >= 17 })
    void enabledOnJava17Plus() {
        assertTrue(Runtime.version().feature() >= 17)
    }

    @Test
    @GroovyDisabledIf({ javaVersion < 10 })
    void notDisabledOnModernJava() {
        assertTrue(Runtime.version().feature() >= 10)
    }

    @Test
    @GroovyEnabledIf({ systemProperties['os.name'] != null })
    void enabledWithSystemProperty() {
        assertTrue(System.getProperty('os.name') != null)
    }

    @Test
    @GroovyEnabledIf({ systemEnvironment instanceof Map })
    void enabledWithEnvironment() {
        assertTrue(true)
    }

    @Test
    @GroovyEnabledIf({ junitDisplayName != null })
    void enabledWithJunitContext() {
        assertTrue(true)
    }

    @Test
    @GroovyEnabledIf({ 2 * 3 == 6 })
    void enabledWithExpression() {
        assertTrue(true)
    }

    @Test
    @GroovyDisabledIf({ systemProperties['os.arch']?.contains('NONEXISTENT') })
    void notDisabledForNonMatchingArch() {
        assertTrue(true)
    }
}

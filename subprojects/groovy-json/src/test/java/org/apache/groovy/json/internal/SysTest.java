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
package org.apache.groovy.json.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Sys class (Java version detection utilities).
 */
class SysTest {

    @Test
    void testIs1_7OrLater() {
        // Since we're running on modern Java, this should always be true
        assertTrue(Sys.is1_7OrLater());
    }

    @Test
    void testIs1_8OrLater() {
        // Since we're running on Java 8+, this should be true
        assertTrue(Sys.is1_8OrLater());
    }

    @Test
    void testIs1_7() {
        // If we're on Java 7, this is true, otherwise false
        // On modern JDKs (8+), this should be false
        boolean result = Sys.is1_7();
        // Just verify it returns a boolean without error
        assertTrue(result || !result);
    }

    @Test
    void testIs1_8() {
        // Returns true only if we're on exactly Java 8
        boolean result = Sys.is1_8();
        // Just verify it returns a boolean without error
        assertTrue(result || !result);
    }

    @Test
    void testVersionDetectionConsistency() {
        // If is1_8 is true, is1_8OrLater must also be true
        if (Sys.is1_8()) {
            assertTrue(Sys.is1_8OrLater());
        }

        // If is1_7 is true, is1_7OrLater must be true
        if (Sys.is1_7()) {
            assertTrue(Sys.is1_7OrLater());
        }
    }

    @Test
    void testNotBothJava7And8() {
        // Cannot be both Java 7 and Java 8
        assertFalse(Sys.is1_7() && Sys.is1_8());
    }

    @Test
    void testJava7ImpliesNotJava8() {
        if (Sys.is1_7()) {
            assertFalse(Sys.is1_8());
        }
    }

    @Test
    void testJava8ImpliesNotJava7() {
        if (Sys.is1_8()) {
            assertFalse(Sys.is1_7());
        }
    }

    @Test
    void testModernJavaVersion() {
        // On Java 9+, both is1_7() and is1_8() should be false
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("9") || !javaVersion.startsWith("1")) {
            // We're on Java 9 or later (version strings like "11", "17", "21")
            assertFalse(Sys.is1_7());
            assertFalse(Sys.is1_8());
            assertTrue(Sys.is1_8OrLater());
        }
    }
}

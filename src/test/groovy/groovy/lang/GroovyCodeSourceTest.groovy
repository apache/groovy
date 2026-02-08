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
package groovy.lang

import groovy.test.GroovyTestCase

class GroovyCodeSourceTest extends GroovyTestCase {
    void testValidEncoding() {
        new GroovyCodeSource(createTemporaryGroovyClassFile(), "UTF-8")
    }

    void testInvalidEncoding() {
        try {
            new GroovyCodeSource(createTemporaryGroovyClassFile(), "non-existant encoding")
            fail("expected exception")
        } catch (UnsupportedEncodingException e) {
            assert "non-existant encoding" == e.getMessage()
        }
    }

    void testInvalidFile() {
        try {
            new GroovyCodeSource(new File("SomeFileThatDoesNotExist" + System.currentTimeMillis()), "UTF-8")
            fail("expected IOException")
        } catch (IOException) {
            assert true
        }
    }

    void testRuntimeException() {
        try {
            new GroovyCodeSource(null, "UTF-8")
            fail("expected NullPointerException")
        } catch (NullPointerException) {
            assert true
        }
    }

    File createTemporaryGroovyClassFile() {
        String testName = "GroovyCodeSourceTest" + System.currentTimeMillis()
        File groovyCode = new File(System.getProperty("java.io.tmpdir"), testName)
        groovyCode.write("class SomeClass { }")
        groovyCode.deleteOnExit()
        return groovyCode
    }
}
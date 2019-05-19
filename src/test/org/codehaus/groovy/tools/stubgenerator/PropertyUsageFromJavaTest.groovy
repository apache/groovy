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
package org.codehaus.groovy.tools.stubgenerator

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import static org.junit.Assume.assumeFalse

/**
 * Tests Groovy properties and how they can be used from Java.
 *
 * @author Guillaume Laforge
 */
@RunWith(JUnit4)
class PropertyUsageFromJavaTest extends StubTestCase {

    @Before
    void setUp() {
//        assumeNotOnTravisCIAndNotJava6()
        super.setUp()
    }

    private assumeNotOnTravisCIAndNotJava6() {
        boolean travisCI = new File('.').absolutePath =~ /travis/
        boolean java6 = System.getProperty('java.version').startsWith('1.6')

        assumeFalse('''Test always fails with "java.lang.IllegalArgumentException: URI is not hierarchical"
            on open jdk used by travis.''', travisCI && java6)
    }

    @Test
    void test() {
        super.testRun()
    }

    void verifyStubs() {
        classes['stubgenerator.propertyUsageFromJava.somepackage.GroovyPogo'].with {
            assert methods['getAge'].signature == "public int getAge()"
            assert methods['getName'].signature == "public java.lang.String getName()"
            assert methods['setAge'].signature == "public void setAge(int value)"
            assert methods['setName'].signature == "public void setName(java.lang.String value)"
        }
    }

    @After
    void tearDown() {
        super.tearDown()
    }
}


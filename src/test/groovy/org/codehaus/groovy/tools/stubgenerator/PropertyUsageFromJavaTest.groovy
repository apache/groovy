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

/**
 * Tests Groovy properties and how they can be used from Java.
 */
class PropertyUsageFromJavaTest extends StubTestCase {

    void verifyStubs() {
        classes['stubgenerator.propertyUsageFromJava.somepackage.GroovyPogo'].with {
            assert methods['getAge'].signature == "public int getAge()"
            assert methods['getName'].signature == "public java.lang.String getName()"
            assert methods['setAge'].signature == "public void setAge(int value)"
            assert methods['setName'].signature == "public void setName(java.lang.String value)"
        }
    }
}


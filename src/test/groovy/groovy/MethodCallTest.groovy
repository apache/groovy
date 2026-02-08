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
package groovy

import groovy.test.GroovyTestCase

class MethodCallTest extends GroovyTestCase {

    void testMethodCall() {
        assert Math.max(5, 7) == 7
    }

    void testObjectMethodCall() {
        def c = getClass()
        assert c != null
        assert c.name.endsWith("MethodCallTest")
        assert c.getName().endsWith("MethodCallTest")
    }

    void testObjectMethodCall2() {
        def s = "hello"
        def c = s.getClass()
        assert c != null
        assert c.name == "java.lang.String"
        assert c.getName() == "java.lang.String"
    }

    void testGetNameBug() {
        def c = getClass()
        def n = c.getName()
        assert c.getName().endsWith("MethodCallTest")
        assert n.endsWith("MethodCallTest")
    }
}

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
package org.codehaus.groovy.macro

import groovy.test.GroovyTestCase
import groovy.transform.CompileStatic

@CompileStatic
class MacroTransformationTest extends GroovyTestCase {

    void testSimple() {
        assertScript """
        def nullObject = null
        
        assert null == safe(nullObject.hashcode())
"""
    }

    void testMacroInClosure() {
        assertScript """
        def cl = {
            return safe(it.hashcode())
        }

        assert null == cl(null)
"""
    }

    void testCascade() {
        assertScript """
        def nullObject = null
        assert null == safe(safe(nullObject.hashcode()).toString())
"""
    }

    void testMethodName() {
        assertScript """
        assert "toString" == methodName(123.toString())

        assert "getInteger" == methodName(Integer.getInteger())

        assert "call" == methodName({}())

        assert "after" == methodName((new Date()).&after)
"""
    }

    void testPropertyName() {
        assertScript """
        assert "bytes" == propertyName("".bytes)

        assert "class" == propertyName(123.class)
"""
    }
}

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
package org.codehaus.groovy.jsr223

import groovy.test.GroovyTestCase

import javax.script.ScriptEngineManager

class JavascriptTest extends GroovyTestCase {
    void testIntegrationWithBuiltinJavaScript() {
        def binding = new Binding()
        binding.x = 10
        binding.y = 5
        def js = ScriptEngineManager.javascript
        if (!js) {
            System.err.println("Warning: JavaScript not available on this JVM - test ignored")
        } else {
            def eval = js.&eval.rcurry(binding)
            assert eval('2 * x + y') == 25
            eval 'z = x + y'
            assert binding.z == 15
        }
    }
}
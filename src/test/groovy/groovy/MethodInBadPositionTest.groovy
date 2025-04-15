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

import gls.CompilableTestSupport

class MethodInBadPositionTest extends CompilableTestSupport {
    /** GROOVY-4215 */
    void testMethodDefinitionInClosure() {
        def msg = shouldNotCompile('''
            { ->
                def say(String msg) {
                  println(msg)
                }
            }()
        ''')
        assert msg.contains('Method definition not expected here') || msg.contains("Unexpected input: '('")
    }

    /** GROOVY-4215 */
    void testXMethodDefinitionInSwitch() {
        def msg = shouldNotCompile('''
            switch(1) {
                case 1: def say(){}
            }
        ''')
        assert msg.contains('Method definition not expected here')  || msg.contains("Unexpected input: '('") || msg.contains("Unexpected input: 'switch(1)")
    }
}
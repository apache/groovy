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
package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy7933Bug extends GroovyTestCase {

    void testPrimitiveMethodArgsSelection() {
        assertScript '''
            class Demo {
                String a(boolean b) { 'boolean' }
                String a(byte b) { 'byte' }
                String a(short s) { 'short' }
                String a(char c) { 'char' }
                String a(int i) { 'int' }
                String a(long l) { 'long' }
                String a(float f) { 'float' }
                String a(double d) { 'double' }
                String a(Object o) { 'Object' }
            }

            Demo demo = new Demo()
            assert demo.a(true) == 'boolean'
            assert demo.a('a'.bytes[0]) == 'byte'
            assert demo.a((short)1) == 'short'
            assert demo.a('c'.charAt(0)) == 'char'
            assert demo.a(1) == 'int'
            assert demo.a(1L) == 'long'
            assert demo.a(1.0f) == 'float'
            assert demo.a(1.0d) == 'double'
        '''
    }
}

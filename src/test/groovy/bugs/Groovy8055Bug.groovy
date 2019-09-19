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

class Groovy8055Bug extends GroovyTestCase {
    void test1() {
        assertScript '''
        import groovy.transform.CompileStatic
        
        @CompileStatic
        class Foo {
            void isEqualTo(Number number) { println "Number"; assert true }
            void isEqualTo(Object number) { println "Object"; assert false: 'wrong method invoked' }
        }
        
        @CompileStatic
        class Runner {
            void run() {
                new Foo().isEqualTo(4)
            }
        }
        
        new Runner().run()
        '''
    }
}

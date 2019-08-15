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

class Groovy9075Bug extends GroovyTestCase {
    void testSetMetaClassWithDifferentType() {
        def errMsg = shouldFail '''
            import groovy.transform.CompileStatic
            
            class C1 {
                int x
            }
            class C2 {
                int x
            }
            
            def c1 = new C1()
            def c2 = new C2()
            c1.metaClass = c2.metaClass  //eg: org.springframework.beans.BeanUtils.copyProperties
                                         // or c1.setMetaClass(c2.getMetaClass())
    
            c1.x += 1  // crash here with unclear exception, but c1.setX() works
        '''

        assert 'the new metaClass[class C2] is not compatible with the old metaClass[class C1]' == errMsg
    }

    void testSetInvalidMetaClass() {
        def errMsg = shouldFail '''
            import groovy.transform.CompileStatic
            
            class C1 {
                int x
            }

            def c1 = new C1()
            c1.metaClass = 'hello'
        '''

        assert 'the new metaClass[class java.lang.String] is not an instance of interface groovy.lang.MetaClass' == errMsg
    }
}

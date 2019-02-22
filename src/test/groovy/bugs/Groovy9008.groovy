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

class Groovy9008 extends GroovyTestCase {
    void testMethodReference() {
        assertScript '''
            import java.util.stream.Collectors
            
            void p() {
                def result = [1, 2, 3].stream().map(Object::toString).collect(Collectors.toList())
                assert ['1', '2', '3'] == result
            }
            
            p()
        '''
    }

    void testMethodReferenceSC() {
        assertScript '''
            import java.util.stream.Collectors
            
            @groovy.transform.CompileStatic
            void p() {
                def result = [1, 2, 3].stream().map(Object::toString).collect(Collectors.toList())
                assert ['1', '2', '3'] == result
            }
            
            p()
        '''
    }
}

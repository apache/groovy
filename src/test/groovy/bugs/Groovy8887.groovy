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

class Groovy8887 extends GroovyTestCase {
    void testMultiAssignment() {
        assertScript '''
            @groovy.transform.CompileStatic
            class TtcTuple {
                void multiAssignedByMethodCall() {
                    def (String name, Integer age) = findPersonInfo()
                    
                    assert 'Daniel' == name
                    assert 35 == age
                }
                
                void multiAssignedByVariableAccess() {
                    Tuple2<String, Integer> personInfo = findPersonInfo()
                    def (String name, Integer age) = personInfo
                    
                    assert 'Daniel' == name
                    assert 35 == age
                }
                
                Tuple2<String, Integer> findPersonInfo() {
                    Tuple2<String, Integer> t = new Tuple2<>('Daniel', 35)
                    
                    return t
                }
            }
            
            def tt = new TtcTuple()
            tt.multiAssignedByMethodCall()
            tt.multiAssignedByVariableAccess()
        '''
    }
}

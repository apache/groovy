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
            class StcTuple {
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
                
                void multiAssignedByMap1() {
                    Tuple2<String, Integer> personInfo = new Tuple2<String, Integer>('Daniel', 35)
                    def (String name, Integer age) = personInfo.map1(e -> "${e}.Sun")
                    assert 'Daniel.Sun' == name
                    assert 35 == age
                }
                
                void multiAssignedByMap2() {
                    Tuple2<String, Integer> personInfo = new Tuple2<String, Integer>('Daniel', 35)
                    def (String name, Integer age) = personInfo.map2(e -> e - 1)
                    assert 'Daniel' == name
                    assert 34 == age
                }
                
                void multiAssignedByFluentMap1() {
                    def (String name, Integer age) = findPersonInfo().map1(e -> "${e}.Sun")
                    assert 'Daniel.Sun' == name
                    assert 35 == age
                }
                
                void multiAssignedByFluentMap2() {
                    def (String name, Integer age) = findPersonInfo().map2(e -> e - 1)
                    assert 'Daniel' == name
                    assert 34 == age
                }
                
                void multiAssignedByFactory() {
                    def (String name, Integer age) = Tuple.tuple('Daniel', 35)
                    assert 'Daniel' == name
                    assert 35 == age
                }
                
                void multiAssignedByConstructor() {
                    def (String name, Integer age) = new Tuple2<String, Integer>('Daniel', 35)
                    assert 'Daniel' == name
                    assert 35 == age
                }
                
                Tuple2<String, Integer> findPersonInfo() {
                    Tuple2<String, Integer> t = new Tuple2<>('Daniel', 35)
                    
                    return t
                }
            }
            
            def st = new StcTuple()
            st.multiAssignedByMethodCall()
            st.multiAssignedByVariableAccess()
            st.multiAssignedByMap1()
            st.multiAssignedByMap2()
            st.multiAssignedByFluentMap1()
            st.multiAssignedByFluentMap2()
            st.multiAssignedByFactory()
            st.multiAssignedByConstructor()
        '''
    }
}

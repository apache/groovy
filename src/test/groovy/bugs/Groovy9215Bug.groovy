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

class Groovy9215Bug extends GroovyTestCase {
    void testDuplicatedAnnotations1() {
        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.TypeChecked
            
            @TypeChecked
            @CompileStatic
            class Data {
                void getThing(Closure c){
                    c("hello")
                }
            }
            
            @CompileStatic
            @CompileStatic
            class Op {
                public Data d = new Data()
            
                void aFunc(Closure c){
                    c()
                }
            
                void broken() {
                    aFunc({
                        d.getThing({ String res ->
                            println(" ")
                        })
                    })
                }
            }
            
            assert Op.class
        '''
    }

    void testDuplicatedAnnotations2() {
        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.TypeChecked
            
            @TypeChecked
            @CompileStatic
            class Data {
                void getThing(Closure c){
                    c("hello")
                }
            }
            
            @TypeChecked
            @CompileStatic
            class Op {
                public Data d = new Data()
            
                void aFunc(Closure c){
                    c()
                }
            
                void broken() {
                    aFunc({
                        d.getThing({ String res ->
                            println(" ")
                        })
                    })
                }
            }
            
            assert Op.class
        '''
    }

    void testDuplicatedAnnotations3() {
        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.TypeChecked
            
            @TypeChecked
            @CompileStatic
            class Data {
                void getThing(Closure c){
                    c("hello")
                }
            }
            
            @TypeChecked
            @TypeChecked
            class Op {
                public Data d = new Data()
            
                void aFunc(Closure c){
                    c()
                }
            
                void broken() {
                    aFunc({
                        d.getThing({ String res ->
                            println(" ")
                        })
                    })
                }
            }
            
            assert Op.class
        '''
    }

    void testDuplicatedAnnotations4() {
        assertScript '''
            import groovy.transform.CompileDynamic
            import groovy.transform.CompileStatic
            import groovy.transform.TypeChecked
            
            class Person {
                String name
            }
            
            @CompileStatic  // ignored
            @CompileDynamic // taken effect
            @TypeChecked    // ignored
            def x() {
                Person.metaClass.introduce << {return "I'm $name"}
                def person = new Person(name:"Daniel.Sun")
                assert "I'm Daniel.Sun" == person.introduce()
            }
            
            x()
        '''
    }
}

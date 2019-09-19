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

class Groovy3410Bug extends GroovyTestCase {

    void testClassVerificationErrorsWithBooleanExpUsingPrimitiveFields() {
        assertScript """
            class Groovy3405N1 {
                long id // or float or double
                
                boolean bar() {
                    return (id ? true : false)
                }
            }
            println new Groovy3405N1()     
        """

            assertScript """
            class Groovy3405N2 {
                long id
                def bar() {
                    return (id ? "a" : "b")
                }
            }              
            println new Groovy3405N2()     
        """

            assertScript """
            class Groovy3405N3 {
                long id = 0
                def bar() {
                    assert id, "Done"
                }
            }   
            println new Groovy3405N3()     
        """

        assertScript """
            class Groovy3405N4 {
                long id = 0
                def bar() {
                    while(id){
                        print "here"
                        break
                    }
                }
            }   
            println new Groovy3405N4()     
        """

        assertScript """
            class Groovy3405N5 {
                long id = 0
                def bar() {
                    if(id) {
                        true
                    } else {
                        false
                    }
                }
            }   
            println new Groovy3405N5()     
        """
    }
}

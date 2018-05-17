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

class Groovy6668Bug extends GroovyTestCase{
    void testGroovy6668() {
        assertScript '''
        @groovy.transform.CompileStatic
        class OtherThing {
            OtherThing() {
                Map<String, String> m = [:]
                def k = "foo"
                m["$k"].toUpperCase()
            }
        }
        
        OtherThing
        '''
    }

    void testGroovy6668WithData() {
        assertScript '''
        @groovy.transform.CompileStatic
        class OtherThing {
            OtherThing() {
                Map<String, String> m = (Map<String, String>) [foo: "bar"]
                def k = "foo"
                assert "BAR" == m["$k"].toUpperCase()
            }
        }
        
        new OtherThing()
        '''
    }

    void testInlineGString() {
        assertScript '''
        @groovy.transform.CompileStatic
        class OtherThing {
            OtherThing() {
                Map<String, String> m = (Map<String, String>) [foo: "bar"]
                assert "BAR" == m["${'foo'}"].toUpperCase()
            }
        }
        
        new OtherThing()
        '''
    }
}

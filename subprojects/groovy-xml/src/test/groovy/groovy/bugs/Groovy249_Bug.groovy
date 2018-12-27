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

import groovy.xml.MarkupBuilder

class Groovy249_Bug extends GroovyTestCase {

    void testBug() {
        def t = new Bean249()
        t.b = "hello"
        
        def xml = new MarkupBuilder()
        def root = xml.foo {
            bar {
                // works
                baz("test")
                // fails
                baz(t.b)
                // fails
                baz("${t.b}")
            }
        } 
    }
    
/** @todo don't know why this fails

    void testBugInScript() {
        assertScript <<<EOF
            import groovy.xml.MarkupBuilder;
            
            class Bean {
                String b
            };
            
            def t = new Bean()
            t.b = "hello"
            println t.b
            println "test: ${t.b}"
            
            def xml = new MarkupBuilder()
            root = xml.foo {
                bar {
                    // works
                    baz("test")
                    // fails
                    baz(t.b)
                    // fails
                    baz("${t.b}")
                }
            } 

EOF        
    }
*/
   
}

class Bean249 {
    String b
}

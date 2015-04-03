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
package groovy.xml

/**
 * This test uses the concise syntax to test the generation
 * of SAX events using GroovyMarkup
 */
class SAXTest extends TestXmlSupport {
    
    void testSmallTree() {
        def b = createSAXBuilder()
        
        def root = b.root1(a:5, b:7) {
            elem1('hello1')
            elem2('hello2')
            elem3(x:7)
        }
    }
    
    void testTree() {
        def b = createSAXBuilder()
        
        def root = b.root2(a:5, b:7) {
            elem1('hello1')
            elem2('hello2')
            nestedElem(x:'abc', y:'def') {
                child(z:'def')
                child2()  
            }
            
            nestedElem2(z:'zzz') {
                child(z:'def')
                child2("hello")  
            }
        }
    }
}
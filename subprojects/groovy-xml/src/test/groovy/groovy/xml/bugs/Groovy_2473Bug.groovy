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
package groovy.xml.bugs

import groovy.test.GroovyTestCase
import groovy.xml.StreamingMarkupBuilder

class Groovy_2473Bug extends GroovyTestCase {
    void testBug() {
        def w = new StringWriter()
        def b = new StreamingMarkupBuilder()

        w << b.bind {
            mkp.xmlDeclaration()
            a("\u0083")
        }

        assertEquals("<?xml version='1.0'?>\n<a>&#x83;</a>", w.toString())

        b.encoding = "UTF-8"

        w = new StringWriter()

        w << b.bind {
            mkp.xmlDeclaration()
            a("\u0083")
        }

        assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n<a>\u0083</a>", w.toString())
    }
}
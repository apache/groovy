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
package groovy

import groovy.test.GroovyTestCase

class HeredocsTest extends GroovyTestCase {

    void testHeredocs() {
        def name = "James"
        def s = """
abcd
efg

hijk
     
hello ${name}
        
"""
        assert s != null
        assert s instanceof GString
        assert s.contains("i")
        assert s.contains("James")
        def numlines = s.count('\n')
        assert numlines == 8
    }

    void testDollarEscaping() {
        def s = """
hello \${name}
"""
        assert s != null
        assert s.contains('$')
        def c = s.count('$')
        assert c == 1
        assert s == '\nhello ${name}\n'
    }
}

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
package org.codehaus.groovy.runtime

import groovy.test.GroovyTestCase

/**
 * Test Appendable withFormatter and leftShift DGM methods
 */
class AppendableDgmMethodsTest extends GroovyTestCase {
    List<String> store = []
    Appendable app = new Appendable() {
        Appendable append(char c) { store+="$c"; this }
        Appendable append(CharSequence cs) { store += cs; this }
        Appendable append(CharSequence cs, int i1, int i2) { store += cs.subSequence(i1,i2); this }
    }

    void testFoo() {
        app << "hello "
        app << [a:1, b:2]
        app.withFormatter { Formatter f ->
            f.format(" %tY", Date.parse('dd MM yyyy', '01 01 2001'))
            f.format(Locale.FRANCE, " e = %+10.4f", Math.E)
        }
        assert store.join('') == 'hello [a:1, b:2] 2001 e =    +2,7183'
    }
}

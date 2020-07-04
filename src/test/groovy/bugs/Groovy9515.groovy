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

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Test

@CompileStatic
final class Groovy9515 {

    @Test
    void testSpreadArgsIndy() {
        def config = new CompilerConfiguration()
        config.optimizationOptions.indy = true
        new GroovyShell(config).evaluate '''
def x(int a) {a}
def x(int a, int b) {a + b}
def y(p) {
    x(*p)
}

assert 1 == y([1])
assert 3 == y([1, 2])
'''
    }
}

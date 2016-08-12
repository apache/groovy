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

class ConstructorThisCallBug extends GroovyTestCase {
    // GROOVY-994
    void testCallA() {
        assert new ConstructorCallA("foo").toString() == 'foo'
        assert new ConstructorCallA(9).toString() == '81'
        assert new ConstructorCallA().toString() == '100'
    }

    private static class ConstructorCallA {
        private String a

        ConstructorCallA(String a) { this.a = a }

        ConstructorCallA(int a) { this("" + (a * a)) } // call another constructor

        ConstructorCallA() { this(10) } // call another constructor

        String toString() { a }
    }
}

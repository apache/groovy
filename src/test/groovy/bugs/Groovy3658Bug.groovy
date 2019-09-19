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

class Groovy3658Bug extends GroovyTestCase {
    void testConstructorWithParameterWithInitialValueAsStaticMethodCallResult() {
        Groovy3658BugHelper bug2 = new Groovy3658BugHelper('person', 'tag')
        assert bug2.dump() != null
    }
}

class Groovy3658BugHelper {
    Groovy3658BugHelper(final String name1, final String name2 = f(name1)) { 
        this.name1 = name1
        this.name2 = name2
    }
    static String f(String s) { 
        s 
    }
    final String name1, name2
}

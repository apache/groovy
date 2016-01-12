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

class Groovy3679Bug extends GroovyTestCase {
    void testMapEntryWinOverPvtAndPkgPrivateClassFields() {
        // map entry should win over a package-private field
        def map1 = new HashMap()
        map1["table"] = "Some table"
        assert map1["table"] != null
        
        // map entry should win over a private field
        def map2 = [:]
        map2["header"] = "Some header"
        assert map2["header"] != null
        
        // following is to verify that setting of private fields with .@"$x" syntax is not
        // broken by the fix introduced
        def x = new X3679()
        x.setSomething("foo",2)
        assert x.getAFoo() == 2    
    }
}

class X3679 extends HashMap {
    private foo
    def setSomething(String x,y) {
        this.@"$x" = y
    }
    def getAFoo() {
        return foo
    }
}
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

/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
class MapPropertyTest extends GroovyTestCase {

    void testGetAndSetProperties() {
        def m = [ 'name' : 'James', 'location' : 'London', 'id':1 ]
        
        assert m.name == 'James'
        assert m.location == 'London'
        assert m.id == 1
        
        m.name = 'Bob'
        m.location = 'Atlanta'
        m.id = 2
        
        assert m.name == 'Bob'
        assert m.location == 'Atlanta'
        assert m.id == 2
    }

    void testSetupAndEmptyMap() {
        def m = [:]
        
        m.name = 'Bob'
        m.location = 'Atlanta'
        m.id = 2
        
        assert m.name == 'Bob'
        assert m.location == 'Atlanta'
        assert m.id == 2
    }
    
    void testMapSubclassing() {
        def c = new MyClass()

        c.id = "hello"
        c.class = 1
        c.myMethod()
        assert c.id == "hello"
        assert c.class == 1
        assert c.getClass() != 1
    }
}

class MyClass extends HashMap {
    def myMethod() {
        assert id == "hello"
        assert this.class == 1
    }
}

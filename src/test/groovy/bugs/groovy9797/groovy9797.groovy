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
package groovy.bugs.groovy9797

import groovy.test.GroovyTestCase



/** 
 * Tests GROOVY-9797 fix
 */
class Groovy9797 extends GroovyTestCase {
    
    void testFloat() {
        Float f = -0.0f
        Object o = -0.0f

        assert f.toString() == "-0.0"
        assert o.toString() == "-0.0"
        assert (-0.0f).toString() == "-0.0"

        boolean x = -0.0f == 0.0f   
        assert x == false
 
    }
}
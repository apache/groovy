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
 * test to ensure that overriding getter doesn't throw a NPE on access
 */
class OverridePropertyGetterTest extends GroovyTestCase {
    def cheese

    void testSimpleMethodParameterAccess() {
        def o = new OverridePropertyGetterTest(cheese: 'Edam')
        def p = new OverridePropertyGetterTest(cheese: 'Cheddar')
        try {
            p.cheese = o.cheese
        } catch (Exception e) {
            fail(e.getMessage())
        }
        assert p.cheese == 'Edam'
    }

    String getCheese() {
        return cheese
    }
} 
